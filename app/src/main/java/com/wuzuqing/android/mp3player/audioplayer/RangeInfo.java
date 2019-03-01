package com.wuzuqing.android.mp3player.audioplayer;


import java.util.Locale;

public class RangeInfo {
    private String fileName;
    private int index;
    private long from;
    private long to;
    private String preDefectFileName;

//    private byte[] sourceBytes;
//    private int firstIndex;
//
//    public int getFirstIndex() {
//        return firstIndex;
//    }
//
//    public void setFirstIndex(int firstIndex) {
//        this.firstIndex = firstIndex;
//    }
//
//    public byte[] getSourceBytes() {
//        return sourceBytes;
//    }
//
//    public void setSourceBytes(byte[] sourceBytes) {
//        this.sourceBytes = sourceBytes;
//    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public long getFrom() {
        return from;
    }

    public void setFrom(long from) {
        this.from = from;
    }

    public void setFrom(long from, long oneFileTotalSize) {
        this.from = from;
        to = from + oneFileTotalSize;
    }

    public String getPreDefectFileName() {
        return preDefectFileName;
    }

    public void setPreDefectFileName(String preDefectFileName) {
        this.preDefectFileName = preDefectFileName;
    }

    public long getTo() {
        return to;
    }

    public void setTo(long to) {
        this.to = to;
    }

    public void init(String name, int index, MediaType mediaType) {
        this.index = index;
        setFrom(index * mediaType.getOneFileTotalSize(), mediaType.getOneFileTotalSize());
        fileName = name.replace("over", String.format(Locale.getDefault(), "%d_%d", index, mediaType.getOneFileCacheSecond()));
        preDefectFileName = name.replace("over", String.format(Locale.getDefault(), "preDefect_%d_%d", index, mediaType.getOneFileCacheSecond()));
    }


    @Override
    public String toString() {
        return "RangeInfo{" +
//                "fileName='" + fileName + '\'' +
                ", index=" + index +
                ", from=" + from +
                ", to=" + to +
                '}';
    }
}