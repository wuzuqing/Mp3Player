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

    /**
     * 8
     * 16
     * 24
     * 32
     * 40
     * 48
     * 56
     * 64
     * 80
     * 96
     * 112
     * 128
     * 144
     * 160
     * 176
     * 192
     * 224
     * 228
     * 256
     * 320
     * 352
     * 384
     * 416
     * 448
     */


    private static int getBitRate(int value, int fmpgVersion, int layer) {
        switch (value) {
            case 1:
                switch (layer) {
                    case 1:
                        return 32000;
                    case 2:
                        if (fmpgVersion==1){

                        }
                        break;
                    case 3:
                        break;
                }
                break;
            case 2:
                break;
            case 3:
                break;
            case 4:
                break;
            case 5:
                break;
            case 6:
                break;
            case 7:
                break;
            case 8:
                break;
            case 9:
                break;
            case 10:
                break;
            case 11:
                break;
            case 12:
                break;
            case 13:
                break;
            case 14:
                break;
        }

        return 0;
    }

    private static int getBit(int fmpgVersion, int layer) {
        switch (fmpgVersion) {
            case 1:
                break;
            case 2:
                break;
            case 3:
                break;
        }
        return 0;
    }

    public static AdtsHeader readADTSHeader(byte[] bytes) {
        return readADTSHeader(new BitReader(bytes));
    }
//
//    private static byte otherValue = -125;
//    private static char[] b = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
//
//    private static byte intToHex(int n) {
//        if (n <= 0) {
//            String toHexString = Integer.toHexString(n);
//            LogUtils.d("intToHex:" + toHexString);
//
//            return otherValue;
//        }
//        StringBuffer s = new StringBuffer();
//        String a;
//        while (n != 0) {
//            s = s.append(b[n % 16]);
//            n = n / 16;
//        }
//        a = s.reverse().toString();
//        return Byte.parseByte(a);
//    }

    /**
     * 从AAC文件流中读取ADTS头部
     *
     * @param bitReader
     * @return 是否读取成功
     * @throws IOException
     */
    public static AdtsHeader readADTSHeader(BitReader bitReader) {

        try {
//            unsigned int sync:11;                        //同步信息
//            unsigned int version:2;                      //版本
//            unsigned int layer: 2;                           //层
//            unsigned int error protection:1;           // CRC校验
//            unsigned int bitrate_index:4;              //位率
//            unsigned int sampling_frequency:2;         //采样频率
//            unsigned int padding:1;                    //帧长调节
//            unsigned int private:1;                       //保留字
//            unsigned int mode:2;                         //声道模式
//            unsigned int mode extension:2;        //扩充模式
//            unsigned int copyright:1;                           // 版权
//            unsigned int original:1;                      //原版标志
//            unsigned int emphasis:2;                  //强调模式
            AdtsHeader adtsHeader = new AdtsHeader();
            bitReader.position = 0;
            int syncWord = bitReader.readBits(11); // A
            adtsHeader.mpegVersion = bitReader.readBits(2); // B
            adtsHeader.layer = bitReader.readBits(2); // C
            adtsHeader.protectionAbsent = bitReader.readBits(1); // D

            adtsHeader.bitrate_index = bitReader.readBits(4);  // E
            adtsHeader.sampleFrequencyIndex = bitReader.readBits(2);
            adtsHeader.sampleRate = samplingFrequencyIndexMap.get(adtsHeader.sampleFrequencyIndex); // F
            bitReader.readBits(1); // G
            bitReader.readBits(1); // G
            adtsHeader.channelconfig = bitReader.readBits(2); // H
            return adtsHeader;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}