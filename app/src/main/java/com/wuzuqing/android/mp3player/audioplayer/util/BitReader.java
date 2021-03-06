package com.wuzuqing.android.mp3player.audioplayer.util;


import java.util.Arrays;

/**
 * 从byte数组读取bit的工具类
 */
public class BitReader {
    public int position;
    public byte[] buffer;

    public BitReader(byte[] buffer) {
        this.buffer = buffer;
    }

    public int readBits(int i) {
        byte b = buffer[position / 8];
        int v = b < 0 ? b + 256 : b;
        int left = 8 - position % 8;
        int rc;
        if (i <= left) {
            rc = (v << (position % 8) & 0xFF) >> ((position % 8) + (left - i));
            position += i;
        } else {
            int now = left;
            int then = i - left;
            rc = readBits(now);
            rc = rc << then;
            rc += readBits(then);
        }
        return rc;
    }
}