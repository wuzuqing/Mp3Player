package com.wuzuqing.android.mp3player;

import com.wuzuqing.android.mp3player.audioplayer.AACHelper;
import com.wuzuqing.android.mp3player.audioplayer.AdtsHeader;
import com.wuzuqing.android.mp3player.audioplayer.BitReader;

public class Test1 {

    public static void main(String[] args) {
        //-1, -15, 76, 64, 19, -30, 96
        //-1, -7, 12, 64, 22, -97, -4

        try {
            byte[] bytes = new byte[]{-1, -15, 76, 64, 0, 0, 0};
            AdtsHeader header = AACHelper.readADTSHeader(new BitReader(bytes));
            System.out.println(header);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        byte[] packet = new byte[8];
//        addADTStoPacket(packet,180);
//        System.out.println(Arrays.toString(packet));
    }

    /**
     * 添加ADTS头
     *
     * @param packet
     * @param packetLen
     */
    private static void addADTStoPacket(byte[] packet, int packetLen) {
        int profile = 1; // AAC LC
        int freqIdx = 3; // 44.1KHz
        int chanCfg = 1; // CPE

        // fill in ADTS data
        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF9;
        packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
        packet[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;
    }

}
