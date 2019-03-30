package com.wuzuqing.android.mp3player.audioplayer;


import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class AudioInfo {
    /**
     * 当前网络文件的链接
     */
    private String url;
    /**
     * 缓存文件完全下载的本地文件名
     */
    private String finishFileName;
    /**
     * 文件分割的数量
     */
    private int splitCount;
    /**
     * 每段分割文件的信息
     */
    private Map<Integer, RangeInfo> rangeInfoList;
    /**
     * 文件大小
     */
    private long contentLength;
    /**
     * 文件时长
     */
    private int duration;
    /**
     * 文件格式
     */
    private MediaType vMediaType;
    /**
     * 文件头部字节
     */
    private byte[] headBytesStr;
    /**
     * 是否初始化
     */
    private boolean isInit;

    private AACHelper aacHelper;

    public AudioInfo() {

    }

    public boolean isInit() {
        return isInit;
    }

    public void setInit(boolean init) {
        isInit = init;
    }

    public MediaType getMediaType() {
        return vMediaType;
    }

    public void setMediaType(MediaType mediaType) {
        vMediaType = mediaType;
    }

    public byte[] getHeadBytesStr() {
        return headBytesStr;
    }

    public void setHeadBytesStr(byte[] headBytesStr) {
        this.headBytesStr = headBytesStr;
    }

    /**
     * 初始化内容
     *
     * @param bytes
     * @param contentLength
     */
    public void init(byte[] bytes, long contentLength) {
        if (isInit) {
            return;
        }
        isInit = true;
        float duration = 0f;
        setContentLength(contentLength);
        if (url.contains("aac")) {
            vMediaType = MediaType.AAC;
            String name = url.substring(url.lastIndexOf("_") + 1, url.lastIndexOf("."));
            finishFileName = String.format("%s_over.aac", name);
            AdtsHeader adtsHeader = AACHelper.readADTSHeader(bytes);
            LogUtils.d("AdtsHeader:" + adtsHeader);
            duration = (contentLength * (1024000f / adtsHeader.sampleRate) / adtsHeader.frameLength);
            headBytesStr = Arrays.copyOf(bytes,4);
        } else if (url.contains("mp3")) {
            vMediaType = MediaType.MP3;
            String name = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("."));
            finishFileName = String.format("%s_over.mp3", name);
            duration = (contentLength * 1f /vMediaType.getOneSecondSize());
//            AdtsHeader adtsHeader = MP3Helper.readADTSHeader(new byte[]{54,53,53,45});
            AdtsHeader adtsHeader = MP3Helper.readADTSHeader(Arrays.copyOfRange(bytes, 10, 14));
            LogUtils.d("AdtsHeader:" + adtsHeader);
            headBytesStr = bytes;
        }

        setUrl(url);


        splitCount = (int) Math.ceil(duration / vMediaType.getOneFileCacheSecond());
        this.duration = (int) (duration * 1000);
        rangeInfoList = new HashMap<>(splitCount);
        for (int i = 0; i < splitCount; i++) {
            RangeInfo rangeInfo = new RangeInfo();
            rangeInfo.init(finishFileName, i, vMediaType);
            if (i == splitCount - 1) {
                rangeInfo.setTo(contentLength);
            }
            rangeInfoList.put(i, rangeInfo);
        }
        LogUtils.d("duration:" + duration + " splitCount:" + splitCount + Arrays.toString(headBytesStr) + " url:"+url);
    }


    public Map<Integer, RangeInfo> getRangeInfoList() {
        return rangeInfoList;
    }


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFinishFileName() {
        return finishFileName;
    }

    public void setFinishFileName(String finishFileName) {
        this.finishFileName = finishFileName;
    }

    public int getSplitCount() {
        return splitCount;
    }

    public void setSplitCount(int splitCount) {
        this.splitCount = splitCount;

    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;

    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "AudioInfo{" +
                "url='" + url + '\'' +
                ", finishFileName='" + finishFileName + '\'' +
                ", splitCount=" + splitCount +
                ", rangeInfoList=" + rangeInfoList +
                ", contentLength=" + contentLength +
                ", duration=" + duration +
                ", isInit=" + isInit +
                '}';
    }

    public RangeInfo getRangeInfo(int index) {
        return rangeInfoList.get(index);
    }
}