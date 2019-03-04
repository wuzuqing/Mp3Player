package com.wuzuqing.android.mp3player.audioplayer;

/**
 * 作者：士元
 * 时间：2019/2/23 14:43
 * 邮箱：wuzuqing@linghit.com
 * 说明： 文件下载回调
 */
public interface OnAudioFileDownloadListener {
    /**
     * 完成下载
     *
     * @param audioInfo 正在下载的文件信息
     * @param rangeInfo 完成下载的文件信息
     */
    void onFinish(AudioInfo audioInfo, RangeInfo rangeInfo);

    /**
     * 下载失败
     *
     * @param audioInfo 正在下载的文件信息
     * @param error     失败原因
     */
    void onError(AudioInfo audioInfo, AudioError error);
}
