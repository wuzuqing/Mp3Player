package com.wuzuqing.android.mp3player.audioplayer;


/**
 * 作者：士元
 * 时间：2019/2/23 14:43
 * 邮箱：wuzuqing@linghit.com
 * 说明：音频文件分割的信息
 */
public class RangeInfo {
    /**
     * 分割的文件名
     */
    private String fileName;
    /**
     * 分割的文件索引
     */
    private int index;
    /**
     * 下载的开始索引
     */
    private long from;
    /**
     * 下载的结束索引
     */
    private long to;
    private boolean isEnd;


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

    public long getTo() {
        return to;
    }

    public void setTo(long to) {
        this.to = to;
    }

    @Override
    public String toString() {
        return "RangeInfo{" +
                ", index=" + index +
                ", from=" + from +
                ", to=" + to +
                '}';
    }

    public void setEnd(boolean end) {
        isEnd = end;
    }

    public boolean getEnd() {
        return isEnd;
    }

    public boolean isNotEnd() {
         return !isEnd;
    }
}