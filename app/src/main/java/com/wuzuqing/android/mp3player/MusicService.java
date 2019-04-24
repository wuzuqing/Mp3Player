package com.wuzuqing.android.mp3player;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * 作者：士元
 * 时间：2019/4/24 19:22
 * 邮箱：wuzuqing@linghit.com
 * 说明：
 */
public class MusicService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }
}
