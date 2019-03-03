package com.wuzuqing.android.mp3player.audioplayer;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.SeekBar;
import android.widget.TextView;

import com.wuzuqing.android.mp3player.DataUtils;

import java.io.IOException;
import java.util.Locale;

/**
 * 作者：士元
 * 时间：2019/2/23 9:46
 * 邮箱：wuzuqing@linghit.com
 * 说明：
 */
public class SimplePlayer implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnInfoListener, MediaPlayer.OnErrorListener {
    private MediaPlayer vCurrentMediaPlayer;
    private MediaPlayer vNextMediaPlayer;
    private String lastUrl;
    private boolean isPrepared;
    private int seekToPos;


    private int touchToPos;
    private int currentIndex;
    private AudioInfo vAudioInfo;
    private boolean isLoadEnd;


    private void initPlayer() {
        if (vCurrentMediaPlayer == null) {
            vCurrentMediaPlayer = new MediaPlayer();
            vCurrentMediaPlayer.setOnCompletionListener(this);
            vCurrentMediaPlayer.setOnPreparedListener(this);
            vCurrentMediaPlayer.setOnErrorListener(this);
            vCurrentMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
    }

    private void initNextPlayer() {
        if (vNextMediaPlayer == null) {
            vNextMediaPlayer = new MediaPlayer();
            vNextMediaPlayer.setOnCompletionListener(this);
            vNextMediaPlayer.setOnPreparedListener(this);
            vCurrentMediaPlayer.setOnErrorListener(this);
            vNextMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
    }


    public void resume() {
        if (isPrepared) {
            vCurrentMediaPlayer.start();
            vHandler.removeCallbacksAndMessages(null);
            vHandler.sendEmptyMessage(0);
        }
    }


    public void pause() {
        if (isPrepared) {
            vCurrentMediaPlayer.pause();
            vHandler.removeCallbacksAndMessages(null);
        }
    }

    public void stop() {
        if (vCurrentMediaPlayer != null && isPrepared) {
            LogUtils.d("stop: " + currentIndex + " / " + vAudioInfo.getSplitCount() + " isPrepared:" + isPrepared);
            vCurrentMediaPlayer.stop();
            vCurrentMediaPlayer.reset();
            isPrepared = false;
            vHandler.removeCallbacksAndMessages(null);
        }
        lastUrl = null;
    }


    public void playUrl(String url) {
        currentIndex = 0;
        //重置
        if (vSeekBar != null) {
            vSeekBar.setMax(0);
        }
        playWithPos(url, -1);
    }

    public void seekToWithOffset(boolean isAdd, int offset) {
        LogUtils.d("seekToWithOffset:" + offset);
        if (isPrepared) {
            int currentPosition = vCurrentMediaPlayer.getCurrentPosition();
            int realPosition = currentIndex * vAudioInfo.getMediaType().getOneFileCacheSecond() * 1000 + currentPosition;
            if (isAdd) {
                realPosition += offset;
                int duration = vAudioInfo.getDuration();
                realPosition = realPosition > duration ? duration : realPosition;
            } else {
                realPosition -= offset;
                realPosition = realPosition < 0 ? 0 : realPosition;
            }
            startToDownload(realPosition, vAudioInfo);
        }
    }


    private void start(String url, boolean isCurrent) {
        if (isCurrent) {
            initPlayer();
        } else {
            initNextPlayer();
        }
        try {
            LogUtils.d("start setDataSource:");
            if (isCurrent) {
                vCurrentMediaPlayer.setDataSource(url);
                vCurrentMediaPlayer.prepareAsync();
                lastUrl = url;
            } else {
                vNextMediaPlayer.setDataSource(url);
                vNextMediaPlayer.prepareAsync();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 播放器移动位置
     *
     * @param mp
     */
    private void playerSeekTo(MediaPlayer mp) {
        if (seekToPos != -1) {
            mp.seekTo(seekToPos);
            seekToPos = -1;
        }
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {

        int second = (mp.getCurrentPosition()) / 1000;
        if (second > 175) {
            if (hasNextPrepared) {
                playNext();
            } else {

            }
        }
        LogUtils.d(" what:" + what + " extra:" + extra + " second:" + second + " hasNextDownloadFinishFile:" + hasNextDownloadFinishFile);
        return true;
    }

    /**
     * @param url
     * @param pos 秒
     */
    public void playWithPos(String url, final int pos) {
        this.touchToPos = pos;

        LogUtils.d("playWithPos:" + pos);
        AudioInfo audioInfo = AudioCache.getInstance().getAudioInfo(url);
        if (!audioInfo.isInit()) {
            //初始化
            AudioCacheDownload.getInstance().syncInitContentLength(audioInfo, vAudioFileInitListener);
        } else {
            //开始下载
            startToDownload(pos, audioInfo);
        }
    }

    private void startToDownload(int pos, AudioInfo audioInfo) {
        this.vAudioInfo = audioInfo;
        if (pos == -1) {
            seekToPos = -1;
        } else {
            double ms = (1000d * audioInfo.getMediaType().getOneFileCacheSecond());
            currentIndex = (int) (pos / ms);
            seekToPos = pos % (int) ms;
        }
        LogUtils.d(" startToDownload seekToPos:" + seekToPos + " pos:" + pos + " currentIndex:" + currentIndex);
        download(currentIndex, vOnAudioFileDownloadListener);
    }


    private OnAudioFileInitListener vAudioFileInitListener = new OnAudioFileInitListener() {
        @Override
        public void onInit(AudioInfo audioInfo) {
            startToDownload(touchToPos, audioInfo);
        }
    };


    private OnAudioFileDownloadListener vOnAudioFileDownloadListener = new OnAudioFileDownloadListener() {
        @Override
        public void onFinish(AudioInfo audioInfo, RangeInfo rangeInfo) {
            if (vSeekBar != null && vSeekBar.getMax() == 0) {
                vSeekBar.setMax(audioInfo.getDuration());
            }
            //开始播放
            startWithOffset(AudioCacheDownload.getInstance().getRangeInfoFileName(rangeInfo), seekToPos);
        }

        @Override
        public void onError(AudioInfo audioInfo, AudioError error) {
            startWithOffset(audioInfo.getUrl(), seekToPos);
        }
    };

    private OnAudioFileDownloadListener vOnAudioFileDownloadNextListener = new OnAudioFileDownloadListener() {

        @Override
        public void onFinish(AudioInfo audioInfo, RangeInfo rangeInfo) {
            hasNextDownloadFinishFile = true;
            start(AudioCacheDownload.getInstance().getRangeInfoFileName(rangeInfo), false);
        }

        @Override
        public void onError(AudioInfo audioInfo, AudioError error) {

        }
    };

    private void startWithOffset(String url, int toPos) {
        LogUtils.d("startSeekToWithOffset:" + toPos);
        this.seekToPos = toPos;
        if (url != null && url.equals(lastUrl)) {
            if (isPrepared) {
                playerSeekTo(vCurrentMediaPlayer);
                vCurrentMediaPlayer.start();
                vHandler.removeCallbacksAndMessages(null);
                vHandler.sendEmptyMessage(0);
            } else {
                start(url, true);
            }
        } else {
            stop();
            start(url, true);
        }
    }


    private Handler vHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            updatePos();
            sendEmptyMessageDelayed(0, 1000);
        }
    };


    private void updatePos() {
        AudioInfo audioInfo = vAudioInfo;
        if (vCurrentMediaPlayer != null && isPrepared && vCurrentMediaPlayer.isPlaying()) {
            int currentPosition = vCurrentMediaPlayer.getCurrentPosition();
            int realPosition = currentIndex * audioInfo.getMediaType().getOneFileCacheSecond() * 1000 + currentPosition;
            if (!isTouchSeekBar) {
                if (vSeekBar != null) {
                    vSeekBar.setProgress(realPosition);
                }
            }
            if (vCurrentTextView != null) {
                vCurrentTextView.setText(posToTimeStr(realPosition));
                vTotalTextView.setText(posToTimeStr(audioInfo.getDuration()));
            }
            checkNeedDownload(audioInfo, currentPosition, vCurrentMediaPlayer.getDuration());
        }
    }

    private boolean hasDownloadNext;
    private boolean hasNextDownloadFinishFile = false;
    private boolean hasNextPrepared = false;

    private void checkNeedDownload(AudioInfo audioInfo, int position, int duration) {
        if (currentIndex == audioInfo.getSplitCount() - 1) {
            isLoadEnd = true;
            return;
        }
        if (!isLoadEnd && !hasDownloadNext && position + 8000 > duration) {
            hasDownloadNext = true;
            int nextIndex = currentIndex + 1;
            hasNextDownloadFinishFile = false;
            hasNextPrepared = false;
            download(nextIndex, vOnAudioFileDownloadNextListener);
        }
    }

    private void download(int index, OnAudioFileDownloadListener listener) {
        AudioCacheDownload.getInstance().download(vAudioInfo, index, listener);
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


    @Override
    public void onPrepared(MediaPlayer mp) {
        if (mp == vNextMediaPlayer) {
            hasNextPrepared = true;
            LogUtils.d("onPrepared vNextMediaPlayer:");
            return;
        }
        if (hasNextDownloadFinishFile) {
            playNext();
        } else {
            isPrepared = true;
            playerSeekTo(mp);
            mp.start();
            vHandler.removeCallbacksAndMessages(null);
            vHandler.sendEmptyMessage(999);
            LogUtils.d("onPrepared: lastUrl:" + lastUrl + " duration: " + mp.getDuration() + " seekToPos:" + seekToPos);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        LogUtils.d("onCompletion: " + currentIndex + " / " + vAudioInfo.getSplitCount());
        stop();
        if (hasNextPrepared) {
            playNext();
        } else if (hasNextDownloadFinishFile) {

        } else if (currentIndex != vAudioInfo.getSplitCount() - 1 && !hasDownloadNext) {
            currentIndex++;
            AudioCacheDownload.getInstance().download(vAudioInfo, currentIndex, vOnAudioFileDownloadNextListener);
        } else {
            isLoadEnd = true;
        }
    }

    private void playNext() {
        currentIndex++;
        hasNextPrepared = false;
        LogUtils.d("playNext:"+vCurrentMediaPlayer + " / "+vNextMediaPlayer);
        stop();
        MediaPlayer temp = vCurrentMediaPlayer;
        vCurrentMediaPlayer = vNextMediaPlayer;
        vCurrentMediaPlayer.start();
        vNextMediaPlayer = temp;
        isPrepared = true;
        vHandler.removeCallbacksAndMessages(null);
        vHandler.sendEmptyMessage(999);
        hasNextDownloadFinishFile = false;
        hasDownloadNext = false;
    }

    private boolean isTouchSeekBar;
    private SeekBar vSeekBar;

    private TextView vCurrentTextView, vTotalTextView;

    public void bindTextView(TextView currentTextView, TextView totalTextView) {
        this.vCurrentTextView = currentTextView;
        this.vTotalTextView = totalTextView;
    }

    public void bindSeekBar(SeekBar seekBar) {
        this.vSeekBar = seekBar;
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int touchPos;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                touchPos = seekBar.getProgress();
                isTouchSeekBar = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (seekBar.getMax() > 0 && Math.abs(touchPos - seekBar.getProgress()) > 3000) {
                    vHandler.removeCallbacksAndMessages(null);
                    playWithPos(DataUtils.testUrl, seekBar.getProgress());
                    isTouchSeekBar = false;
                }
            }
        });
    }
}
