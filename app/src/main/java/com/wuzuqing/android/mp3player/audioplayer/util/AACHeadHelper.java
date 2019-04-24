package com.wuzuqing.android.mp3player.audioplayer.util;

import com.wuzuqing.android.mp3player.audioplayer.AudioFileHeader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 读取AAC头部参数
 */
public class AACHeadHelper {
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


    public static AudioFileHeader readADTSHeader(byte[] bytes) {
        return readADTSHeader(new BitReader(bytes));
    }

    /**
     * 从AAC文件流中读取ADTS头部
     *
     * @param bitReader
     * @return 是否读取成功
     * @throws IOException
     */
    public static AudioFileHeader readADTSHeader(BitReader bitReader) {
        if (bitReader.buffer.length < 7) {
            return null;
        }
        try {
            AudioFileHeader audioFileHeader = new AudioFileHeader();
            bitReader.position = 0;
            int syncWord = bitReader.readBits(12); // A
            if (syncWord != 0xfff) {
                throw new IOException("Expected Start Word 0xfff");
            }
            audioFileHeader.mpegVersion = bitReader.readBits(1); // B
            audioFileHeader.layer = bitReader.readBits(2); // C
            audioFileHeader.protectionAbsent = bitReader.readBits(1); // D
            audioFileHeader.profile = bitReader.readBits(2) + 1;  // E
            audioFileHeader.sampleFrequencyIndex = bitReader.readBits(4);
            audioFileHeader.sampleRate = samplingFrequencyIndexMap.get(audioFileHeader.sampleFrequencyIndex); // F
            bitReader.readBits(1); // G
            audioFileHeader.channelconfig = bitReader.readBits(3); // H
            audioFileHeader.channelValue = audioFileHeader.channelconfig == 1 ? 1 : 2; // H
            audioFileHeader.original = bitReader.readBits(1); // I
            audioFileHeader.home = bitReader.readBits(1); // J
            audioFileHeader.copyrightedStream = bitReader.readBits(1); // K
            audioFileHeader.copyrightStart = bitReader.readBits(1); // L
            audioFileHeader.frameLength = bitReader.readBits(13); // M
            audioFileHeader.bufferFullness = bitReader.readBits(11); // 54
            audioFileHeader.numAacFramesPerAdtsFrame = bitReader.readBits(2) + 1; // 56
            if (audioFileHeader.numAacFramesPerAdtsFrame != 1) {
                throw new IOException("This muxer can only work with 1 AAC frame per ADTS frame");
            }
            return audioFileHeader;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}