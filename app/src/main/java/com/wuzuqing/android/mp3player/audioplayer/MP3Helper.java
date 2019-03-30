package com.wuzuqing.android.mp3player.audioplayer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MP3Helper {
    // 采样频率对照表
    private static Map<Integer, Integer> samplingFrequencyIndexMap = new HashMap<>();

    static {
        samplingFrequencyIndexMap.put(96000, 0);
        samplingFrequencyIndexMap.put(88200, 1);
        samplingFrequencyIndexMap.put(64000, 2);
        samplingFrequencyIndexMap.put(48000, 3);
        samplingFrequencyIndexMap.put(44100, 4);
        samplingFrequencyIndexMap.put(32000, 5);
        samplingFrequencyIndexMap.put(24000, 6);
        samplingFrequencyIndexMap.put(22050, 7);
        samplingFrequencyIndexMap.put(16000, 8);
        samplingFrequencyIndexMap.put(12000, 9);
        samplingFrequencyIndexMap.put(11025, 10);
        samplingFrequencyIndexMap.put(8000, 11);
        samplingFrequencyIndexMap.put(0x0, 96000);
        samplingFrequencyIndexMap.put(0x1, 88200);
        samplingFrequencyIndexMap.put(0x2, 64000);
        samplingFrequencyIndexMap.put(0x3, 48000);
        samplingFrequencyIndexMap.put(0x4, 44100);
        samplingFrequencyIndexMap.put(0x5, 32000);
        samplingFrequencyIndexMap.put(0x6, 24000);
        samplingFrequencyIndexMap.put(0x7, 22050);
        samplingFrequencyIndexMap.put(0x8, 16000);
        samplingFrequencyIndexMap.put(0x9, 12000);
        samplingFrequencyIndexMap.put(0xa, 11025);
        samplingFrequencyIndexMap.put(0xb, 8000);
    }


    public static AdtsHeader readADTSHeader(byte[] bytes) {
        return readADTSHeader(new BitReader(bytes));
    }

    /**
     * 从AAC文件流中读取ADTS头部
     *
     * @param bitReader
     * @return 是否读取成功
     * @throws IOException
     */
    public static AdtsHeader readADTSHeader(BitReader bitReader) {

        try {
            AdtsHeader adtsHeader = new AdtsHeader();
            bitReader.position = 0;
            int syncWord = bitReader.readBits(11); // A
//            if (syncWord != 0xfff) {
//                throw new IOException("Expected Start Word 0xfff");
//            }
            adtsHeader.mpegVersion = bitReader.readBits(2); // B
            adtsHeader.layer = bitReader.readBits(2); // C
            adtsHeader.protectionAbsent = bitReader.readBits(1); // D

            adtsHeader.profile = bitReader.readBits(4) ;  // E
            adtsHeader.sampleFrequencyIndex = bitReader.readBits(2);
            adtsHeader.sampleRate = samplingFrequencyIndexMap.get(adtsHeader.sampleFrequencyIndex); // F
            bitReader.readBits(1); // G
            bitReader.readBits(1); // G
            adtsHeader.channelconfig = bitReader.readBits(2); // H
//            adtsHeader.original = bitReader.readBits(1); // I
//            adtsHeader.home = bitReader.readBits(1); // J
//            adtsHeader.copyrightedStream = bitReader.readBits(1); // K
//            adtsHeader.copyrightStart = bitReader.readBits(1); // L
//            adtsHeader.frameLength = bitReader.readBits(13); // M
//            adtsHeader.bufferFullness = bitReader.readBits(11); // 54
//            adtsHeader.numAacFramesPerAdtsFrame = bitReader.readBits(2) + 1; // 56
//            if (adtsHeader.numAacFramesPerAdtsFrame != 1) {
//                throw new IOException("This muxer can only work with 1 AAC frame per ADTS frame");
//            }
            return adtsHeader;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}