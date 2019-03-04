package com.wuzuqing.android.mp3player.audioplayer;

/**
 * 作者：士元
 * 时间：2019/2/23 14:43
 * 邮箱：wuzuqing@linghit.com
 * 说明：网络音频文件初始化，用于获取文件的总大小
 */
public interface OnAudioFileInitListener {
    void onInit(AudioInfo audioInfo);
}
