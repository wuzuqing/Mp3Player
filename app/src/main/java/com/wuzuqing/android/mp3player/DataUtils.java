package com.wuzuqing.android.mp3player;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;

import java.util.List;

public class DataUtils {

//    public static final String testUrl = "http://m10.music.126.net/20190301114038/0c70ade6aea4e1edf06be7510e266d91/ymusic/7c7e/dca6/533e/3f0d856d1d4d8c44ec4b526a569ab820.mp3";
    public static final String testUrl = "https://nl.ggwan.com/audio/warmflow/0_20190222125935922.aac";


    public static String[] urls = {
            "https://nl.ggwan.com/audio/warmflow/0_20190215143623023.aac" //[-1, -15, 76, 64, 19, -30, 96, 1]
            , "https://nl.ggwan.com/audio/warmflow/0_20190222120225763.aac"
            , "https://nl.ggwan.com/audio/warmflow/0_20190222144925585.aac"
            , "https://nl.ggwan.com/audio/warmflow/0_20190222144503794.aac"  //[-1, -15, 76, 64, 19, -30, 96, 1]
            , "https://nl.ggwan.com/audio/warmflow/0_20190222143121889.aac"
            , "https://nl.ggwan.com/audio/warmflow/0_20190222141053895.aac"
            , "https://nl.ggwan.com/audio/warmflow/0_20190222125935922.aac"
            , "https://nl.ggwan.com/audio/warmflow/0_20190222120039525.aac"
            ,"https://nl.ggwan.com/audio/warmflow/a.mp3"
            , "https://nl.ggwan.com/audio/warmflow/0_20190222115107936.aac"
    };
    public static String[] lengths = {
            "30344197"
            , "28204057"
            , "28599808"
            , "28713175"
            , "30229717"
            , "27862207"
            , "35968345"
            , "29018296"
            , "27076111"
    };
}
