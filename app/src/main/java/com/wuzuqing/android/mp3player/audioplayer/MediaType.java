package com.wuzuqing.android.mp3player.audioplayer;

/**
 * 作者：士元
 * 时间：2019/3/1 10:14
 * 邮箱：wuzuqing@linghit.com
 * 说明：
 */
public enum MediaType {
    // 7571
    AAC(180, 7453), MP3(180, 8001),UNNOT(-1,-1);
    /**
     * 一个文件缓存有多少秒
     */
    private int oneFileCacheSecond;

    /**
     * 一秒字节大小
     */
    private long oneSecondSize;

    /**
     * 一个文件缓存的字节大小
     */
    private long oneFileTotalSize;

    MediaType(int oneFileCacheSecond, int oneSecondSize) {
        this.oneFileCacheSecond = oneFileCacheSecond;
        this.oneSecondSize = oneSecondSize;
        this.oneFileTotalSize = oneSecondSize * oneFileCacheSecond;
    }

    public int getOneFileCacheSecond() {
        return oneFileCacheSecond;
    }


    public long getOneSecondSize() {
        return oneSecondSize;
    }

    public long getOneFileTotalSize() {
        return oneFileTotalSize;
    }


    /**
     *
     * @param url
     * @return
     */
    public static MediaType get(String url){
        if (url.endsWith(".aac")){
            return AAC;
        }else if (url.endsWith( ".mp3")){
            return MP3;
        }
        return UNNOT;
    }
}
