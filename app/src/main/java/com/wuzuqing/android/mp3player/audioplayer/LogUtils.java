package com.wuzuqing.android.mp3player.audioplayer;

/**
 * 作者：士元
 * 时间：2019/2/23 15:23
 * 邮箱：wuzuqing@linghit.com
 * 说明：
 */
public class LogUtils {
    private static final String TAG = "LogUtils";
    public static StringBuilder sb = new StringBuilder();

    public static void d(String msg) {
        System.out.println(String.format("%s : %s", TAG, msg));

        sb.append(msg).append("\n");
        if (vOnLogChangeListener != null) {
            vOnLogChangeListener.newLog(sb.toString());
        }
//        Log.d(TAG, msg);
    }

    private static OnLogChangeListener vOnLogChangeListener;

    public static void setvOnLogChangeListener(OnLogChangeListener vOnLogChangeListener) {
        LogUtils.vOnLogChangeListener = vOnLogChangeListener;
    }

    public static void clearLog() {
        sb.setLength(0);
        if (vOnLogChangeListener != null) {
            vOnLogChangeListener.newLog(sb.toString());
        }
    }

    public interface OnLogChangeListener {
        void newLog(String log);
    }
}
