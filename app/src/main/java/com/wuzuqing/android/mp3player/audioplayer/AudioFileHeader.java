package com.wuzuqing.android.mp3player.audioplayer;

public class AdtsHeader {
    int getSize() {
        return 7 + (protectionAbsent == 0 ? 2 : 0);
    }

    /**
     * 采样率的下标
     */
    int sampleFrequencyIndex;
    /**
     * MPEG Version: 0 for MPEG-4，1 for MPEG-2
     */
    int mpegVersion;
    /**
     * always: '00'
     */
    int layer;
    /**
     * Warning, set to 1 if there is no CRC and 0 if there is CRC
     */
    int protectionAbsent;
    /**
     * 表示使用哪个级别的AAC，如01 Low Complexity(LC) -- AAC LC
     */
    int profile;
    /**
     * 采样率
     */
    int sampleRate;
    /**
     * 声道数，比如2表示立体声双声道
     */
    int channelconfig;

    /**
     * 声道数，比如2表示立体声双声道
     */
    int channelValue;

    int original;
    int home;

    int copyrightedStream;
    int copyrightStart;
    /**
     * 位率
     */
    int bitrate_index;
    /**
     * 位率
     */
    int bitrate_value;
    /**
     * 一个ADTS帧的长度包括ADTS头和AAC原始流。frame length, this value must include 7 or 9 bytes of header length:
     */
    int frameLength;
    /**
     * 0x7FF 说明是码率可变的码流。
     */
    int bufferFullness;
    /**
     * 表示ADTS帧中有number_of_raw_data_blocks_in_frame + 1个AAC原始帧。
     * 所以说number_of_raw_data_blocks_in_frame == 0 表示说ADTS帧中有一个AAC数据块。
     */
    int numAacFramesPerAdtsFrame;

    public int getBitrate_value() {
        return bitrate_value;
    }

    public int getChannelValue() {
        return channelValue;
    }

    public void setChannelValue(int channelValue) {
        this.channelValue = channelValue;
    }

    public void setBitrate_value(int bitrate_value) {
        this.bitrate_value = bitrate_value;
    }

    @Override
    public String toString() {
        return "AdtsHeader{" +
                "sampleFrequencyIndex=" + sampleFrequencyIndex +
                ", mpegVersion=" + mpegVersion +
                ", layer=" + layer +
                ", protectionAbsent=" + protectionAbsent +
                ", profile=" + profile +
                ", sampleRate=" + sampleRate +
                ", channelconfig=" + channelconfig +
                ", channelValue=" + channelValue +
//                ", original=" + original +
//                ", home=" + home +
//                ", copyrightedStream=" + copyrightedStream +
//                ", copyrightStart=" + copyrightStart +
                ", bitrate_index=" + bitrate_index +
                ", bitrate_value=" + bitrate_value +
                ", frameLength=" + frameLength +
                ", bufferFullness=" + bufferFullness +
//                ", numAacFramesPerAdtsFrame=" + numAacFramesPerAdtsFrame +
                '}';
    }
}
