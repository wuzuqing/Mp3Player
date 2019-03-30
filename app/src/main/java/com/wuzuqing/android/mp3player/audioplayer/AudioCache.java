package com.wuzuqing.android.mp3player.audioplayer;

import java.util.HashMap;
import java.util.Map;

/**
 * 作者：士元
 * 时间：2019/2/23 13:19
 * 邮箱：wuzuqing@linghit.com
 * 说明：音频文件内存缓存
 */
public class AudioCache {

    private static AudioCache instance;

    private Map<String, AudioInfo> vAudioInfoMap;

    private AudioCache() {
        vAudioInfoMap = new HashMap<>();
    }

    public static synchronized AudioCache getInstance() {
        if (instance == null) {
            instance = new AudioCache();
        }
        return instance;
    }

    public AudioInfo getAudioInfo(String url) {
        AudioInfo audioInfo;
        if (!vAudioInfoMap.containsKey(url)) {
            audioInfo = new AudioInfo();
            audioInfo.setUrl(url);
            vAudioInfoMap.put(url, audioInfo);
        } else {
            audioInfo = vAudioInfoMap.get(url);
        }
        return audioInfo;
    }
}
