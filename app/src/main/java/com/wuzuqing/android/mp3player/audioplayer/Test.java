package com.wuzuqing.android.mp3player.audioplayer;


import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;


public class Test {


    /**
     * Copyright © 2018 eSunny Info. Tech Ltd. All rights reserved.
     *
     * @Package: test
     * @author: QW
     * @date: 2018年7月3日 下午12:00:20
     */

    public static int toInt(byte[] b) {
        return (((b[3] & 0xff) << 24) + ((b[2] & 0xff) << 16) + ((b[1] & 0xff) << 8) + ((b[0] & 0xff) << 0));
    }

    public static short toShort(byte[] b) {
        return (short) ((b[1] << 8) + (b[0] << 0));
    }

    public static byte[] read(RandomAccessFile rdf, int pos, int length) throws IOException {
        rdf.seek(pos);
        byte result[] = new byte[length];
        for (int i = 0; i < length; i++) {
            result[i] = rdf.readByte();
        }
        return result;
    }

    public static void main(String[] args) throws IOException {


        File f = new File("e:/test/a.mp3");
        RandomAccessFile rdf = null;
        rdf = new RandomAccessFile(f, "r");

        System.out.println("audio size: " + toInt(read(rdf, 13, 3))); // 声音尺寸

//        System.out.println("audio format: " + toShort(read(rdf, 20, 2))); // 音频格式 1 = PCM
//
//        System.out.println("num channels: " + toShort(read(rdf, 22, 2))); // 1 单声道 2 双声道
//
//        System.out.println("sample rate: " + toInt(read(rdf, 24, 4)));  // 采样率、音频采样级别 8000 = 8KHz
//
//        System.out.println("byte rate: " + toInt(read(rdf, 28, 4)));  // 每秒波形的数据量
//
//        System.out.println("block align: " + toShort(read(rdf, 32, 2)));  // 采样帧的大小
//
//        System.out.println("bits per sample: " + toShort(read(rdf, 34, 2)));  // 采样位数

        rdf.close();

    }
}