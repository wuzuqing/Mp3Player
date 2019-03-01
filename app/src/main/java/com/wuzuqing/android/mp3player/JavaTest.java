package com.wuzuqing.android.mp3player;

import com.wuzuqing.android.mp3player.audioplayer.AudioCache;
import com.wuzuqing.android.mp3player.audioplayer.AudioCacheDownload;
import com.wuzuqing.android.mp3player.audioplayer.AudioError;
import com.wuzuqing.android.mp3player.audioplayer.AudioInfo;
import com.wuzuqing.android.mp3player.audioplayer.OnAudioFileDownloadListener;
import com.wuzuqing.android.mp3player.audioplayer.RangeInfo;

import java.io.File;

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
        int index = 7;
        AudioInfo audioInfo = AudioCache.getInstance().getAudioInfo(DataUtils.testUrl);
        download.initContentLength(audioInfo);
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
        for (int i = 0; i < splitCount; i++) {
            download.download(audioInfo, i, finishListener);
            Thread.sleep(20);
        }
    }
}
