package com.wuzuqing.android.mp3player.audioplayer;

/**
 * 作者：士元
 * 时间：2019/4/26 10:02
 * 邮箱：wuzuqing@linghit.com
 * 说明：
 */
public interface IMusicConfig {
    /**
     * 进度条触摸范围
     */
    int STOP_TRACKING_TOUCH_RANGE = 3000;
    /**
     * 禁止频繁调用seekTo方法,加了时间拦截
     */
    int USER_CLICK_SEEK_TO_TIME = 1000;
    /**
     * 配置下载下一段音频剩余的时间
     */
    int NEXT_FILE_LOAD_TIME=12000;
    /**
     * 读写文件的缓存字节大小
     */
    int DOWNLOAD_CACHE_BYTES = 4096;

    /**
     * 每次下载文件时,额外下载的偏移数据
     */
    int DOWNLOAD_FILE_ADD_OFFSET = 500;
}
