package com.wuzuqing.android.mp3player.audioplayer;

/**
 * 作者：士元
 * 时间：2019/2/23 14:43
 * 邮箱：wuzuqing@linghit.com
 * 说明：
 */
public interface OnAudioFileDownloadListener {
    void onFinish(AudioInfo audioInfo, RangeInfo rangeInfo);

    void onError(AudioInfo audioInfo, AudioError error);
}
