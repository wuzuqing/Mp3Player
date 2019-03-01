package com.wuzuqing.android.mp3player.audioplayer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import okhttp3.Call;
import okhttp3.Callback;
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
        builder.connectTimeout(10, TimeUnit.SECONDS);
        builder.readTimeout(10, TimeUnit.SECONDS);
        builder.writeTimeout(10, TimeUnit.SECONDS);
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

    public String getRangeInfoFileName(RangeInfo rangeInfo) {
        return new File(cacheFileDir, rangeInfo.getFileName()).getAbsolutePath();
    }

    public void initContentLength(AudioInfo audioInfo) throws IOException {
        Response response = buildResponse(audioInfo.getUrl(), 0, 3);
        String contentRange = response.header("Content-Range");
        LogUtils.d("contentRange:" + contentRange);
        long contentLength = Long.parseLong(contentRange.substring(contentRange.lastIndexOf("/") + 1));
        audioInfo.init(response.body().bytes(), contentLength);
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


    public boolean checkFileExists(String fileName) {
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
        // -1, -15, 76, 64 aac
        Response response = buildResponse(audioInfo.getUrl(), rangeInfo.getFrom(), rangeInfo.getTo());
        InputStream stream = response.body().byteStream();
        File file = new File(cacheFileDir, rangeInfo.getFileName());

        FileOutputStream fileOutputStream = new FileOutputStream(file);
        byte[] bytes = new byte[4096];
        int len = 0;
        int totalSize = 0;
        if (audioInfo.getMediaType() == MediaType.AAC) {
            int readOneFrame = stream.read(bytes);
            printBytes(bytes);
            int firstIndex = findFirstIndex(bytes, audioInfo.getHeadBytesStr());
            if (isAndroid) {
                int offset = firstIndex == -1 ? 0 : firstIndex;
                fileOutputStream.write(bytes, offset, readOneFrame - offset);
            } else {
                fileOutputStream.write(bytes, firstIndex == -1 ? 0 : firstIndex, readOneFrame);
            }
            if (firstIndex > 0) {
                byte[] preDf = Arrays.copyOfRange(bytes, 0, firstIndex - 1);
                FileOutputStream preDfFileOutputStream = new FileOutputStream(new File(cacheFileDir, rangeInfo.getPreDefectFileName()));
                preDfFileOutputStream.write(preDf);
                preDfFileOutputStream.close();
            }
            totalSize += readOneFrame;
            LogUtils.d("downloadIndex: firstIndex" + firstIndex + " readOneFrame:" + readOneFrame);
        }

        int index = 0;
        while ((len = stream.read(bytes)) != -1) {
            if (index < 3) {
                printBytes(bytes);
                index++;
            }
//            totalSize += len;
//            if (totalSize == audioInfo.getContentLength()) {
//                int lastIndex = findLastIndex(bytes, audioInfo.getHeadBytesStr(), len);
//                if (lastIndex != -1) {
//                    fileOutputStream.write(bytes, 0, lastIndex - 1);
//                } else {
//                }
//                fileOutputStream.write(bytes, 0, len);
//            } else {
//            }
            fileOutputStream.write(bytes, 0, len);
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

    private int findLastIndex(byte[] bytes, byte[] headBytesStr, int len) {
        int maxLength = len - headBytesStr.length;
        for (int length = maxLength; length > 0; length--) {
            if (check(bytes, length, headBytesStr)) {
                return length;
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
            for (int i = 0; i < splitCount; i++) {
                RangeInfo rangeInfo = infoList.get(i);
                if (i != 0) {
                    fileInputStream = new FileInputStream(new File(cacheFileDir, rangeInfo.getPreDefectFileName()));
                    while ((len = fileInputStream.read(bytes)) != -1) {
                        randomAccessFile.write(bytes, 0, len);
                    }
                    fileInputStream.close();
                }
                fileInputStream = new FileInputStream(new File(cacheFileDir, rangeInfo.getFileName()));
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
                if (!getInstance().checkFileExists(rangeInfo.getFileName())) {
                    AudioCacheDownload.getInstance().downloadIndex(audioInfo, rangeInfo);
                }
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
            vDownloadTasks.add(new DownloadTask(audioInfo, index, listener));
            if (!isRunning) {
                doWork();
            }
        } else {
            listener.onError(audioInfo, AudioError.UN_SUPER_TYPE);
        }

    }


}
