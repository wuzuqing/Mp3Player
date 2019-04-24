package com.wuzuqing.android.mp3player.audioplayer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MP3Helper {
    // 采样频率对照表

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
            AdtsHeader adtsHeader = new AdtsHeader();
            bitReader.position = 0;
            int syncWord = bitReader.readBits(11); // A
            adtsHeader.mpegVersion = bitReader.readBits(2); // B
            adtsHeader.layer = bitReader.readBits(2); // C
            adtsHeader.protectionAbsent = bitReader.readBits(1); // D

            adtsHeader.bitrate_index = bitReader.readBits(4);  // E
            adtsHeader.sampleFrequencyIndex = bitReader.readBits(2);
            adtsHeader.sampleRate = getSimpleRate(adtsHeader.sampleFrequencyIndex, adtsHeader.mpegVersion); // F
            bitReader.readBits(1); // G
            bitReader.readBits(1); // G
            adtsHeader.channelconfig = bitReader.readBits(2); // H
            adtsHeader.channelValue = adtsHeader.channelconfig == 3 ? 1 : 2;
            adtsHeader.bitrate_value = getBitRate(adtsHeader.bitrate_index, adtsHeader.mpegVersion, adtsHeader.layer);
            return adtsHeader;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static int getSimpleRate(int sampleFrequencyIndex, int mpegVersion) {
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

}