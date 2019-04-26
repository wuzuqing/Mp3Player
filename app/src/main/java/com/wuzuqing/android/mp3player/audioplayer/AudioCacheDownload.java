package com.wuzuqing.android.mp3player.audioplayer;

import com.wuzuqing.android.mp3player.audioplayer.util.AACHeadHelper;
import com.wuzuqing.android.mp3player.audioplayer.util.LogUtils;
import com.wuzuqing.android.mp3player.audioplayer.util.MP3HeadHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 作者：士元
 * 时间：2019/2/23 10:13
 * 邮箱：wuzuqing@linghit.com
 * 说明：
 */
public class AudioCacheDownload {

    private static final boolean PRINT_BYTES = false;

    public static boolean isAndroid = true;
    private OkHttpClient mOkHttpClient;
    private ExecutorService cachedThreadPool;
    private File cacheFileDir;
    private static AudioCacheDownload instance = new AudioCacheDownload();

    private Queue<DownloadTask> vDownloadTasks;

    public static AudioCacheDownload getInstance() {
        return instance;
    }

    private AudioCacheDownload() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        int timeout = 50;
        builder.connectTimeout(timeout, TimeUnit.SECONDS);
        builder.readTimeout(timeout, TimeUnit.SECONDS);
        builder.writeTimeout(timeout, TimeUnit.SECONDS);
        builder.hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String s, SSLSession sslSession) {
                //支持所有类型https请求
                return true;
            }
        });
        mOkHttpClient = builder.build();


        vDownloadTasks = new LinkedBlockingDeque<>();
        cachedThreadPool = Executors.newCachedThreadPool();
    }

    public void setCacheFileDir(File cacheFileDir) {
        this.cacheFileDir = cacheFileDir;
        if (!cacheFileDir.exists()) {
            cacheFileDir.mkdirs();
        }
    }

    protected String getRangeInfoFileName(RangeInfo rangeInfo) {
        return new File(cacheFileDir, rangeInfo.getFileName()).getAbsolutePath();
    }

    private void initContentLength(AudioInfo audioInfo) throws IOException {
        boolean isAAC = audioInfo.getMediaType() == MediaType.AAC;
        int start = isAAC ? 0 : 45;
        int end = isAAC ? 7 : 48;
        Response response = buildResponse(audioInfo.getUrl(), start, end);
        String contentRange = response.header("Content-Range");
        LogUtils.d("contentRange:" + contentRange);
        long contentLength = Long.parseLong(contentRange.substring(contentRange.lastIndexOf("/") + 1));
        if (response.body() != null) {
            init(audioInfo, response.body().bytes(), contentLength);
        }
    }

    /**
     * 初始化内容
     *
     * @param bytes
     * @param contentLength
     */
    public void init(AudioInfo audioInfo, byte[] bytes, long contentLength) {
        if (audioInfo.isInit()) {
            return;
        }
        audioInfo.setInit(true);
        float duration = 0f;
        audioInfo.setContentLength(contentLength);
        String url = audioInfo.getUrl();
        if (audioInfo.getMediaType() == MediaType.AAC) {
            String name = url.substring(url.lastIndexOf("_") + 1, url.lastIndexOf("."));
            audioInfo.setFinishFileName(String.format("%s_over.aac", name));
            audioInfo.setAudioFileHeader(AACHeadHelper.readADTSHeader(bytes));
            duration = (contentLength * (1024000f / audioInfo.getAudioFileHeader().sampleRate) / audioInfo.getAudioFileHeader().frameLength) / 1000;
            audioInfo.setHeadBytesStr(Arrays.copyOf(bytes, 4));
        } else if (audioInfo.getMediaType() == MediaType.MP3) {
            String name = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("."));
            audioInfo.setFinishFileName(String.format("%s_over.mp3", name));
            audioInfo.setAudioFileHeader(MP3HeadHelper.readADTSHeader(bytes));
            duration = (contentLength * 8f / audioInfo.getAudioFileHeader().getBitrate_value());
            audioInfo.setHeadBytesStr(bytes);
        }
//        LogUtils.d("AudioFileHeader:" + audioInfo.getAudioFileHeader());

        int splitCount = (int) Math.ceil(duration / audioInfo.getMediaType().getOneFileCacheSecond());
        audioInfo.setSplitCount(splitCount);
        audioInfo.setDuration((int) (duration * 1000));
        Map<Integer, RangeInfo> rangeInfoList = new HashMap<>(splitCount);
        RangeInfo rangeInfo;
        for (int i = 0; i < splitCount; i++) {
            rangeInfo = new RangeInfo();
            init(rangeInfo, audioInfo.getFinishFileName(), i, audioInfo.getMediaType());
            if (i == splitCount - 1) {
                rangeInfo.setTo(contentLength);
                rangeInfo.setEnd(true);
            }
            rangeInfoList.put(i, rangeInfo);
        }
        audioInfo.setRangeInfoList(rangeInfoList);
        LogUtils.d("duration:" + duration + " splitCount:" + splitCount +  "\nurl:" + url);
    }


    /**
     * 初始化
     *
     * @param name
     * @param index
     * @param mediaType
     */
    public void init(RangeInfo rangeInfo, String name, int index, MediaType mediaType) {
        rangeInfo.setIndex(index);
        rangeInfo.setFrom(index * mediaType.getOneFileTotalSize() + index, mediaType.getOneFileTotalSize());
        rangeInfo.setFileName(name.replace("over", String.format(Locale.getDefault(), "%d_%d", index, mediaType.getOneFileCacheSecond())));
    }


    public void syncInitContentLength(final AudioInfo audioInfo, final OnAudioFileInitListener listener) {
        cachedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    initContentLength(audioInfo);
                    if (listener != null) {
                        listener.onInit(audioInfo);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }


    private boolean checkFileExists(String fileName) {
        return new File(cacheFileDir, fileName).exists();
    }

    private void printBytes(byte[] bytes) {
        if (PRINT_BYTES) {
            LogUtils.d("printBytes:  bytes" + Arrays.toString(bytes));
        }
    }

    private void downloadIndex(AudioInfo audioInfo, RangeInfo rangeInfo) throws IOException {
        if (checkFileExists(rangeInfo.getFileName())) {
            LogUtils.d("downloadIndex:hasCache ok" + rangeInfo);
            return;
        }
        MediaType mediaType = audioInfo.getMediaType();
        // -1, -15, 76, 64 aac
        int addOffset = rangeInfo.getEnd() ? 0 : IMusicConfig.DOWNLOAD_FILE_ADD_OFFSET;
        Response response = buildResponse(audioInfo.getUrl(), rangeInfo.getFrom(), rangeInfo.getTo() + addOffset);
        if (response.body() == null) {
            return;
        }
        InputStream stream = response.body().byteStream();
        File file = new File(cacheFileDir, rangeInfo.getFileName());

        FileOutputStream fileOutputStream = new FileOutputStream(file);
        byte[] bytes = new byte[IMusicConfig.DOWNLOAD_CACHE_BYTES];
        int len = 0;
        int totalLength = 0;
        if (audioInfo.getMediaType() == MediaType.AAC) {
            int readOneFrame = stream.read(bytes);
            totalLength = readOneFrame;
            printBytes(bytes);
            int firstIndex = findFirstIndex(bytes, audioInfo.getHeadBytesStr());
            if (isAndroid) {
                int offset = firstIndex == -1 ? 0 : firstIndex;
                fileOutputStream.write(bytes, offset, readOneFrame - offset);
            } else {
                fileOutputStream.write(bytes, firstIndex == -1 ? 0 : firstIndex, readOneFrame);
            }
//            LogUtils.d("downloadIndex: firstIndex" + firstIndex + " readOneFrame:" + readOneFrame);
        }

        int index = 0;
        int readLength = bytes.length;
        int maxLength = (int) (mediaType.getOneFileTotalSize());
        while ((len = stream.read(bytes, 0, readLength)) != -1) {
            if (index < 3) {
                printBytes(bytes);
                index++;
            }
            fileOutputStream.write(bytes, 0, len);
            if (rangeInfo.isNotEnd()) {
                totalLength += len;
                if (readLength != bytes.length && readLength == len) {
                    break;
                }
                //计算一个文件的大小
                if (totalLength + readLength > maxLength) {
                    readLength = (maxLength - totalLength);
                }
            }
        }
        if (rangeInfo.isNotEnd()) {
            len = stream.read(bytes);
            boolean hasBytes = len != -1;
            if (hasBytes) {
                int firstIndex = findFirstIndex(bytes, audioInfo.getHeadBytesStr());
                if (firstIndex > 0) {
                    fileOutputStream.write(bytes, 0, firstIndex);
                }
//                LogUtils.d("firstIndex:" + firstIndex);
            }
        }
        fileOutputStream.flush();
        fileOutputStream.close();
        stream.close();
        LogUtils.d("downloadIndex: ok" + rangeInfo);
    }

    private int findFirstIndex(byte[] bytes, byte[] headBytesStr) {
        int maxLength = bytes.length - headBytesStr.length;
        for (int i = 0; i < maxLength; i++) {
            if (check(bytes, i, headBytesStr)) {
                return i;
            }
        }
        return -1;
    }


    private boolean check(byte[] bytes, int startIndex, byte[] headBytesStr) {
        for (int i = 0; i < headBytesStr.length; i++) {
            if (headBytesStr[i] != bytes[startIndex + i]) {
                return false;
            }
        }
        return true;
    }


    private Response buildResponse(String url, long start, long end) throws IOException {
        Request request = new Request.Builder()
                //确定下载的范围,添加此头,则服务器就可以跳过已经下载好的部分
                .addHeader("RANGE", "bytes=" + start + "-" + end)
                .url(url)
                .build();
        Call call = mOkHttpClient.newCall(request);
        return call.execute();
    }

    public void mergeFiles(AudioInfo audioInfo) {
        int splitCount = audioInfo.getSplitCount();
        Map<Integer, RangeInfo> infoList = audioInfo.getRangeInfoList();
        File finishFile = new File(cacheFileDir, audioInfo.getFinishFileName());
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(finishFile, "rw");
            FileInputStream fileInputStream = null;
            byte[] bytes = new byte[2048];
            int len = 0;
            File tempFile = null;
            for (int i = 0; i < splitCount; i++) {
                RangeInfo rangeInfo = infoList.get(i);
                tempFile = new File(cacheFileDir, rangeInfo.getFileName());
                fileInputStream = new FileInputStream(tempFile);
                while ((len = fileInputStream.read(bytes)) != -1) {
                    randomAccessFile.write(bytes, 0, len);
                }
                fileInputStream.close();
            }
            randomAccessFile.close();
            LogUtils.d("mergeFiles: ok " + finishFile.length() + " getContentLength:" + audioInfo.getContentLength());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void clearCacheFile() {
        if (cacheFileDir != null) {
            deleteDirWithFile(cacheFileDir, false);
            LogUtils.d("deleteDirWithFile success");
        }
    }

    public static void deleteDirWithFile(File dir, boolean isDeleteSelf) {
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            return;
        }
        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                file.delete(); // 删除所有文件
            } else if (file.isDirectory()) {
                deleteDirWithFile(file, isDeleteSelf); // 递规的方式删除文件夹
            }
        }
        if (isDeleteSelf) {
            dir.delete();// 删除目录本身
        }
    }

    private static class DownloadTask implements Runnable {
        private AudioInfo audioInfo;
        private int index;
        private OnAudioFileDownloadListener listener;

        public DownloadTask(AudioInfo audioInfo, int index, OnAudioFileDownloadListener listener) {
            this.audioInfo = audioInfo;
            this.index = index;
            this.listener = listener;
        }

        @Override
        public void run() {
            try {
                long start = System.currentTimeMillis();
                if (!audioInfo.isInit()) {
                    AudioCacheDownload.getInstance().initContentLength(audioInfo);
                }
                RangeInfo rangeInfo = audioInfo.getRangeInfoList().get(index);

                AudioCacheDownload.getInstance().downloadIndex(audioInfo, rangeInfo);
                LogUtils.d("download Used: " + (System.currentTimeMillis() - start) + " info:" + rangeInfo);
                if (listener != null) {
                    listener.onFinish(audioInfo, rangeInfo);
                }
                getInstance().doWork();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private boolean isRunning;

    private void doWork() {
        if (!vDownloadTasks.isEmpty()) {
            isRunning = true;
            DownloadTask task = vDownloadTasks.poll();
            cachedThreadPool.execute(task);
        } else {
            isRunning = false;
        }
    }

    public void download(final AudioInfo audioInfo, final int index, final OnAudioFileDownloadListener listener) {
        if (audioInfo.getUrl().contains("aac") || audioInfo.getUrl().contains("mp3")) {
            if (audioInfo.isInit() && index >= audioInfo.getSplitCount()) {
                return;
            }
            RangeInfo rangeInfo = audioInfo.getRangeInfo(index);
            if (getInstance().checkFileExists(rangeInfo.getFileName())) {
                listener.onFinish(audioInfo, rangeInfo);
                LogUtils.d("downloadIndex:hasCache ok" + rangeInfo);
                return;
            }
            listener.onLoading();
            vDownloadTasks.add(new DownloadTask(audioInfo, index, listener));
            if (!isRunning) {
                doWork();
            }
        } else {
            listener.onError(audioInfo, AudioError.UN_SUPER_TYPE);
        }
    }


}
