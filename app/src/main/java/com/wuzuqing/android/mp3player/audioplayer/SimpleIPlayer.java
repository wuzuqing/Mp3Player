package com.wuzuqing.android.mp3player.audioplayer;

import android.widget.SeekBar;
import android.widget.TextView;

/**
 * 作者：士元
 * 时间：2019/3/4 10:12
 * 邮箱：wuzuqing@linghit.com
 * 说明：播放器API 内部播放使用 mediaPlayer
 */
public interface IPlayer {
    /**
     * 播放网络文件
     *
     * @param url
     */
    void playUrl(String url);

    /**
     * 播放网络文件并移动到指定位置
     *
     * @param url
     * @param offset
     */
    void playWithPos(String url, int offset);

    /**
     * 暂停后恢复播放
     */
    void resume();

    /**
     * 暂停播放
     */
    void pause();

    /**
     * 停止播放
     */
    void stop();

    /**
     * 快进 快退
     *
     * @param isAdd
     * @param offset
     */
    void seekToWithOffset(boolean isAdd, int offset);

    /**
     * 获取当前播放的位置
     *
     * @return
     */
    int getCurrentPosition();

    /**
     * 获取文件的总时长
     *
     * @return
     */
    int getDuration();

    /**
     * 是否正在播放
     *
     * @return
     */
    boolean isPlaying();

    /**
     * 绑定需要显示时间进度的控件
     *
     * @param currentTextView
     * @param totalTextView
     */
    void bindTextView(TextView currentTextView, TextView totalTextView);

    /**
     * 绑定进度条，已实现拖动进度条的功能
     *
     * @param seekBar
     */
    void bindSeekBar(SeekBar seekBar);

    /**
     * 改变播放速度 仅支持sdk 23 及以上
     *
     * @param isAdd true  加速
     * @param speed false 减速
     */
    void changeSpeed(boolean isAdd, float speed);

    /**
     * 恢复默认播放速度
     */
    void resetSpeed();

    void setOnStateChangeListener(OnStateChangeListener listener);
}
