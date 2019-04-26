package com.wuzuqing.android.mp3player.audioplayer;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.SeekBar;
import android.widget.TextView;

import com.wuzuqing.android.mp3player.audioplayer.util.LogUtils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

/**
 * 作者：士元
 * 时间：2019/4/24 18:49
 * 邮箱：wuzuqing@linghit.com
 * 说明：播放器进度帮助类
 */
public class PlayerProgressManager {

    private static final int WHAT_START_WORK = 0;
    private static PlayerProgressManager instance = new PlayerProgressManager();

    private Handler mProgressHandler;
    private IPlayer mPlayer;
    private Set<OnProgressChangeListener> vChangeListeners;

    private boolean isTouchSeekBar;
    private SeekBar vSeekBar;
    private TextView vCurrentTextView, vTotalTextView;

    public static PlayerProgressManager get() {
        return instance;
    }

    private PlayerProgressManager() {
        mProgressHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == WHAT_START_WORK) {
                    refreshProgress();
                }
            }
        };
        vChangeListeners = new HashSet<>();
    }

    public void bindPlayer(IPlayer player) {
        this.mPlayer = player;
    }

    private void refreshProgress() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            int currentPosition = mPlayer.getCurrentPosition();
            setProgressStr(currentPosition);
            mProgressHandler.sendEmptyMessageDelayed(WHAT_START_WORK, 1000);
        }

    }

    public void setProgressStr(int currentPosition) {
        if (!isTouchSeekBar) {
            if (vSeekBar != null) {
                vSeekBar.setProgress(currentPosition);
            }
            _seekBarChangeProgress(true, currentPosition);
        }
    }

    private void _seekBarChangeProgress(boolean needCheck, int currentPosition) {
        String toTimeStr = posToTimeStr(currentPosition);
        if (vCurrentTextView != null) {
            vCurrentTextView.setText(posToTimeStr(currentPosition));
        }
        if (vChangeListeners != null) {
            Iterator<OnProgressChangeListener> iterator = vChangeListeners.iterator();
            while (iterator.hasNext()) {
                OnProgressChangeListener next = iterator.next();
                next.changeProgress(currentPosition, toTimeStr);
                if (needCheck) {
                    mPlayer.notifyProgress(currentPosition);
                }
            }
        }
    }

    void bindTextView(TextView currentTextView, TextView totalTextView) {
        this.vCurrentTextView = currentTextView;
        this.vTotalTextView = totalTextView;
    }

    void bindSeekBar(SeekBar seekBar) {
        this.vSeekBar = seekBar;
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int touchPos;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                _seekBarChangeProgress(false, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                touchPos = seekBar.getProgress();
                isTouchSeekBar = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isTouchSeekBar = false;
                if (seekBar.getMax() > 0 && Math.abs(touchPos - seekBar.getProgress()) > IMusicConfig.STOP_TRACKING_TOUCH_RANGE) {
                    mPlayer.seekTo(seekBar.getProgress());
                }
            }
        });
    }

    public void setDurationStr(int pos) {
        LogUtils.d("setDurationStr:" + pos);
        String toTimeStr = posToTimeStr(pos);
        if (vTotalTextView != null) {
            vTotalTextView.setText(toTimeStr);
        }
        if (vChangeListeners != null) {
            Iterator<OnProgressChangeListener> iterator = vChangeListeners.iterator();
            while (iterator.hasNext()) {
                OnProgressChangeListener next = iterator.next();
                next.changeDuration(toTimeStr);
            }
        }
    }

    public void start() {
        mProgressHandler.removeMessages(WHAT_START_WORK);
        mProgressHandler.sendEmptyMessage(WHAT_START_WORK);
    }

    public void pause() {
        mProgressHandler.removeMessages(WHAT_START_WORK);
    }

    private String posToTimeStr(int pos) {
        int second = pos / 1000;
        int minute = second / 60;
        int hour = minute / 60;
        if (hour > 0) {
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", hour, minute % 60, second % 60);
        }
        return String.format(Locale.getDefault(), "%02d:%02d", minute % 60, second % 60);
    }


    void registerOnProgressChangeListener(OnProgressChangeListener listener) {
        if (!vChangeListeners.contains(listener)) {
            vChangeListeners.add(listener);
        }
    }

    void unRegisterOnProgressChangeListener(OnProgressChangeListener listener) {
        if (vChangeListeners.contains(listener)) {
            vChangeListeners.remove(listener);
        }
    }

    public void prepared(int offset) {
        if (mPlayer == null) return;
        if (vSeekBar != null) {
            if (vSeekBar.getMax() == offset) {
                return;
            }
            vSeekBar.setMax(mPlayer.getDuration());
            if (offset != -1) {
                vSeekBar.setProgress(offset);
            }
        }
        setDurationStr(mPlayer.getDuration());
    }

    public void reset() {
        setDurationStr(0);
        setProgressStr(0);
    }

    public void setMax(int duration) {
        if (vSeekBar != null) {
//        if (vSeekBar != null && vSeekBar.getMax() == 0) {
            vSeekBar.setMax(duration);
        }
    }

    public void setViewZero() {
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            if (vCurrentTextView != null) {
                vCurrentTextView.setText("00:00");
                vTotalTextView.setText("00:00");
            }
        }
    }
}
