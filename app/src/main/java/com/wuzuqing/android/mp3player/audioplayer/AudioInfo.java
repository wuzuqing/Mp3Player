package com.wuzuqing.android.mp3player.audioplayer;


import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class AudioInfo {

    private String url;
    private String finishFileName;
    private int splitCount;
    private Map<Integer, RangeInfo> rangeInfoList;
    private long contentLength;
    private int duration;
    private MediaType vMediaType;
    private byte[] headBytesStr;
    private boolean isInit;

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

    public void init(byte[] bytes, long contentLength) {
        if (isInit) {
            return;
        }
        isInit = true;
        if (url.contains("aac")) {
            vMediaType = MediaType.AAC;
            String name = url.substring(url.lastIndexOf("_") + 1, url.lastIndexOf("."));
            finishFileName = String.format("%s_over.aac", name);
        } else if (url.contains("mp3")) {
            vMediaType = MediaType.MP3;
            String name = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("."));
            finishFileName = String.format("%s_over.mp3", name);
        }

        setUrl(url);
        headBytesStr = bytes;
        setContentLength(contentLength);
        float duration = (contentLength * 1f / vMediaType.getOneSecondSize());
        System.out.println("duration:" + duration + " splitCount:" + splitCount + Arrays.toString(headBytesStr));
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