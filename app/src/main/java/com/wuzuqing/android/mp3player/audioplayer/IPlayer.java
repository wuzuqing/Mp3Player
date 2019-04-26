package com.wuzuqing.android.mp3player.audioplayer;

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
     * 播放网络文件并移动到指定位置
     *
     * @param offset
     */
    void seekTo(int offset);

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

    /**
     * 用于边下边播，检查是需要下载下一段音频
     *
     * @param progress
     */
    void notifyProgress(int progress);

}
