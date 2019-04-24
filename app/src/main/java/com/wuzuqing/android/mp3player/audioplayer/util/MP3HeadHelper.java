package com.wuzuqing.android.mp3player.audioplayer.util;

import com.wuzuqing.android.mp3player.audioplayer.AudioFileHeader;

import java.io.IOException;


/**
 * mp3 头部参数工具类
 */
public class MP3HeadHelper {


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

        try {
            AudioFileHeader audioFileHeader = new AudioFileHeader();
            bitReader.position = 0;
            //同步文件头
            int syncWord = bitReader.readBits(11); // A
            audioFileHeader.mpegVersion = bitReader.readBits(2); // B
            audioFileHeader.layer = bitReader.readBits(2); // C
            audioFileHeader.protectionAbsent = bitReader.readBits(1); // D

            audioFileHeader.bitrate_index = bitReader.readBits(4);  // E
            audioFileHeader.sampleFrequencyIndex = bitReader.readBits(2);
            audioFileHeader.sampleRate = getSimpleRateValue(audioFileHeader.sampleFrequencyIndex, audioFileHeader.mpegVersion); // F
            bitReader.readBits(1); // G
            bitReader.readBits(1); // G
            audioFileHeader.channelconfig = bitReader.readBits(2); // H
            audioFileHeader.channelValue = audioFileHeader.channelconfig == 3 ? 1 : 2;
            audioFileHeader.bitrate_value = getBitRate(audioFileHeader.bitrate_index, audioFileHeader.mpegVersion, audioFileHeader.layer);
            return audioFileHeader;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 采样率数值
     *
     * @param sampleFrequencyIndex 索引
     * @param mpegVersion          版本
     * @return
     */
    private static int getSimpleRateValue(int sampleFrequencyIndex, int mpegVersion) {
        int rate = mpegVersion == 3 ? 4 : mpegVersion;
        switch (sampleFrequencyIndex) {
            case 0:
                return 11025 * rate;
            case 1:
                return 12000 * rate;
            case 2:
                return 8000 * rate;
        }
        return 0;
    }

    /**
     * 获取编码率数值
     *
     * @param value       比特率索引
     * @param mpegVersion 文件版本
     * @param layer       层级
     * @return
     */
    private static int getBitRate(int value, int mpegVersion, int layer) {
        switch (value) {
            case 1:
                switch (mpegVersion) {
                    case 3:
                        return 32000;
                    case 2:
                    case 0:
                        if (layer == 3) {
                            return 32000;
                        } else
                            return 8000;
                }
                break;
            case 2:
                switch (mpegVersion) {
                    case 3:
                        if (layer == 3) {
                            return 64000;
                        } else if (layer == 2) {
                            return 48000;
                        } else if (layer == 1) {
                            return 40000;
                        }
                    case 2:
                    case 0:
                        if (layer == 3) {
                            return 48000;
                        } else {
                            return 16000;
                        }
                }
                break;
            case 3:
                switch (mpegVersion) {
                    case 3:
                        if (layer == 3) {
                            return 96000;
                        } else if (layer == 2) {
                            return 56000;
                        } else if (layer == 1) {
                            return 48000;
                        }
                    case 2:
                    case 0:
                        if (layer == 3) {
                            return 56000;
                        } else {
                            return 24000;
                        }
                }
                break;
            case 4:
                switch (mpegVersion) {
                    case 3:
                        if (layer == 3) {
                            return 128000;
                        } else if (layer == 2) {
                            return 64000;
                        } else if (layer == 1) {
                            return 56000;
                        }
                    case 2:
                    case 0:
                        if (layer == 3) {
                            return 64000;
                        } else {
                            return 32000;
                        }
                }
                break;
            case 5:
                switch (mpegVersion) {
                    case 3:
                        if (layer == 3) {
                            return 160000;
                        } else if (layer == 2) {
                            return 80000;
                        } else if (layer == 1) {
                            return 64000;
                        }
                    case 2:
                    case 0:
                        if (layer == 3) {
                            return 80000;
                        } else {
                            return 40000;
                        }
                }
                break;
            case 6:
                switch (mpegVersion) {
                    case 3:
                        if (layer == 3) {
                            return 192000;
                        } else if (layer == 2) {
                            return 96000;
                        } else if (layer == 1) {
                            return 80000;
                        }
                    case 2:
                    case 0:
                        if (layer == 3) {
                            return 96000;
                        } else {
                            return 48000;
                        }
                }
                break;
            case 7:
                switch (mpegVersion) {
                    case 3:
                        if (layer == 3) {
                            return 224000;
                        } else if (layer == 2) {
                            return 112000;
                        } else if (layer == 1) {
                            return 96000;
                        }
                    case 2:
                    case 0:
                        if (layer == 3) {
                            return 112000;
                        } else {
                            return 56000;
                        }
                }
                break;
            case 8:
                switch (mpegVersion) {
                    case 3:
                        if (layer == 3) {
                            return 256000;
                        } else if (layer == 2) {
                            return 128000;
                        } else if (layer == 1) {
                            return 112000;
                        }
                    case 2:
                    case 0:
                        if (layer == 3) {
                            return 128000;
                        } else {
                            return 64000;
                        }
                }
                break;
            case 9:
                switch (mpegVersion) {
                    case 3:
                        if (layer == 3) {
                            return 288000;
                        } else if (layer == 2) {
                            return 160000;
                        } else if (layer == 1) {
                            return 128000;
                        }
                    case 2:
                    case 0:
                        if (layer == 3) {
                            return 144000;
                        } else {
                            return 80000;
                        }
                }
                break;
            case 10:
                switch (mpegVersion) {
                    case 3:
                        if (layer == 3) {
                            return 320000;
                        } else if (layer == 2) {
                            return 192000;
                        } else if (layer == 1) {
                            return 160000;
                        }
                    case 2:
                    case 0:
                        if (layer == 3) {
                            return 160000;
                        } else {
                            return 96000;
                        }
                }
                break;
            case 11:
                switch (mpegVersion) {
                    case 3:
                        if (layer == 3) {
                            return 352000;
                        } else if (layer == 2) {
                            return 224000;
                        } else if (layer == 1) {
                            return 192000;
                        }
                    case 2:
                    case 0:
                        if (layer == 3) {
                            return 176000;
                        } else {
                            return 112000;
                        }
                }
                break;
            case 12:
                switch (mpegVersion) {
                    case 3:
                        if (layer == 3) {
                            return 384000;
                        } else if (layer == 2) {
                            return 256000;
                        } else if (layer == 1) {
                            return 224000;
                        }
                    case 2:
                    case 0:
                        if (layer == 3) {
                            return 192000;
                        } else {
                            return 128000;
                        }
                }
                break;
            case 13:
                switch (mpegVersion) {
                    case 3:
                        if (layer == 3) {
                            return 416000;
                        } else if (layer == 2) {
                            return 320000;
                        } else if (layer == 1) {
                            return 256000;
                        }
                    case 2:
                    case 0:
                        if (layer == 3) {
                            return 224000;
                        } else {
                            return 144000;
                        }
                }
                break;
            case 14:
                switch (mpegVersion) {
                    case 3:
                        if (layer == 3) {
                            return 448000;
                        } else if (layer == 2) {
                            return 384000;
                        } else if (layer == 1) {
                            return 320000;
                        }
                    case 2:
                    case 0:
                        if (layer == 3) {
                            return 256000;
                        } else {
                            return 160000;
                        }
                }
                break;
        }
        return 0;
    }

}