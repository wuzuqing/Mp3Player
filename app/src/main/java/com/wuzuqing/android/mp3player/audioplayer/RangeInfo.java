package com.wuzuqing.android.mp3player.audioplayer;


import java.util.Locale;


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

    /**
     * 上一个文件的遗留尾部，目前只有AAC格式使用
     */
    private String preDefectFileName;


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

    /**
     * 初始化
     *
     * @param name
     * @param index
     * @param mediaType
     */
    public void init(String name, int index, MediaType mediaType) {
        this.index = index;
        setFrom(index * mediaType.getOneFileTotalSize() + index, mediaType.getOneFileTotalSize());
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