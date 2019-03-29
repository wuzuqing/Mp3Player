package com.wuzuqing.android.mp3player;

import com.wuzuqing.android.mp3player.audioplayer.AudioCache;
import com.wuzuqing.android.mp3player.audioplayer.AudioCacheDownload;
import com.wuzuqing.android.mp3player.audioplayer.AudioError;
import com.wuzuqing.android.mp3player.audioplayer.AudioInfo;
import com.wuzuqing.android.mp3player.audioplayer.LogUtils;
import com.wuzuqing.android.mp3player.audioplayer.OnAudioFileDownloadListener;
import com.wuzuqing.android.mp3player.audioplayer.OnAudioFileInitListener;
import com.wuzuqing.android.mp3player.audioplayer.RangeInfo;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;

/**
 * 作者：士元
 * 时间：2019/2/23 15:20
 * 邮箱：wuzuqing@linghit.com
 * 说明：
 */
public class JavaTest {
    public static void main(String[] args) throws Exception {
        AudioCacheDownload.isAndroid = false;
        final AudioCacheDownload download = AudioCacheDownload.getInstance();
        download.setCacheFileDir(new File("e://test"));
        AudioInfo audioInfo = AudioCache.getInstance().getAudioInfo(DataUtils.urls[0]);
        download.syncInitContentLength(audioInfo, new OnAudioFileInitListener() {
            @Override
            public void onInit(AudioInfo audioInfo) {
                try {
                    startWork(download, audioInfo);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private static void startWork(AudioCacheDownload download, AudioInfo audioInfo) throws InterruptedException {
        final int splitCount = audioInfo.getSplitCount();
        OnAudioFileDownloadListener finishListener = new OnAudioFileDownloadListener() {

            @Override
            public void onFinish(AudioInfo audioInfo, RangeInfo rangeInfo) {

            }

            @Override
            public void onError(AudioInfo audioInfo, AudioError code) {

            }
        };
//        download.download(audioInfo, index, finishListener);
        LinkedList<Integer> indexs = new LinkedList<>();
        for (int i = 0; i < 1; i++) {
            indexs.add(i);
//            download.download(audioInfo, i, finishListener);
//            Thread.sleep(20);
        }
        Collections.shuffle(indexs);
        LogUtils.d(indexs.toString());
        while (!indexs.isEmpty()) {
            download.download(audioInfo, indexs.removeFirst(), finishListener);
            Thread.sleep(200);
        }
    }
}
