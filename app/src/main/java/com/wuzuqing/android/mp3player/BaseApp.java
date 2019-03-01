package com.wuzuqing.android.mp3player;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

import com.wuzuqing.android.mp3player.audioplayer.AudioCacheDownload;

import java.io.File;

/**
 * 作者：士元
 * 时间：2019/2/23 10:17
 * 邮箱：wuzuqing@linghit.com
 * 说明：
 */
public class BaseApp extends Application {
    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this;
        File cacheFileDir = new File(Environment.getExternalStorageDirectory(), "cacheAac");
        AudioCacheDownload.getInstance().setCacheFileDir(cacheFileDir);
    }

    public static Context getContext() {
        return sContext;
    }
}
