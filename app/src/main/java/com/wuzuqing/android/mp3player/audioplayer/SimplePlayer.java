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
    private MediaPlayer vMediaPlayer;
    private String lastUrl;
    private boolean isPrepared;
    private int seekToPos;


    private int touchToPos;
    private int currentIndex;
    private AudioInfo vAudioInfo;
    private boolean isLoadEnd;


    private void initPlayer() {
        if (vMediaPlayer == null) {
            vMediaPlayer = new MediaPlayer();
            vMediaPlayer.setOnCompletionListener(this);
            vMediaPlayer.setOnPreparedListener(this);
            vMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
    }


    public void resume() {
        if (isPrepared) {
            vMediaPlayer.start();
            vHandler.removeCallbacksAndMessages(null);
            vHandler.sendEmptyMessage(0);
        }
    }


    public void pause() {
        if (isPrepared) {
            vMediaPlayer.pause();
            vHandler.removeCallbacksAndMessages(null);
        }
    }

    public void stop() {
        if (vMediaPlayer != null && isPrepared) {
            LogUtils.d("stop: " + currentIndex + " / " + vAudioInfo.getSplitCount() + " isPrepared:" + isPrepared);
            vMediaPlayer.stop();
            vMediaPlayer.reset();
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
            int currentPosition = vMediaPlayer.getCurrentPosition();
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


    private void start(String url) {
        initPlayer();
        try {
            LogUtils.d("start setDataSource:");
            vMediaPlayer.setDataSource(url);
            vMediaPlayer.setOnErrorListener(this);
            vMediaPlayer.prepareAsync();
            lastUrl = url;
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
//        if (extra==MediaPlayer. MEDIA_ERROR_IO){
//        }
        LogUtils.d("onError"+mp.getCurrentPosition() + "/"+mp.getDuration());
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
                playerSeekTo(vMediaPlayer);
                vMediaPlayer.start();
                vHandler.removeCallbacksAndMessages(null);
                vHandler.sendEmptyMessage(0);
            } else {
                start(url);
            }
        } else {
            stop();
            start(url);
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
        if (vMediaPlayer != null && isPrepared && vMediaPlayer.isPlaying()) {
            int currentPosition = vMediaPlayer.getCurrentPosition();
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
            checkNeedDownload(audioInfo, currentPosition, vMediaPlayer.getDuration());
        }
    }

    private boolean hasDownloadNext;
    private boolean hasNextDownloadFinishFile = false;

    private void checkNeedDownload(AudioInfo audioInfo, int position, int duration) {
        if (currentIndex == audioInfo.getSplitCount() - 1) {
            isLoadEnd = true;
            return;
        }
        if (!isLoadEnd && !hasDownloadNext && position + 8000 > duration) {
            hasDownloadNext = true;
            int nextIndex = currentIndex + 1;
            hasNextDownloadFinishFile = false;
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
        if (hasNextDownloadFinishFile){
            currentIndex++;
            RangeInfo rangeInfo = vAudioInfo.geRangeInfo(currentIndex);
            startWithOffset(AudioCacheDownload.getInstance().getRangeInfoFileName(rangeInfo), -1);
            hasNextDownloadFinishFile = false;
            hasDownloadNext = false;
        }else{
            isPrepared = true;
            playerSeekTo(mp);
            mp.start();
            vHandler.removeCallbacksAndMessages(null);
            vHandler.sendEmptyMessage(999);
            LogUtils.d("onPrepared: lastUrl:" + lastUrl + " duration: " + mp.getDuration() + " seekToPos:" + seekToPos );
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        LogUtils.d("onCompletion: " + currentIndex + " / " + vAudioInfo.getSplitCount());
        stop();
        if (hasNextDownloadFinishFile) {
            currentIndex++;
            RangeInfo rangeInfo = vAudioInfo.geRangeInfo(currentIndex);
            startWithOffset(AudioCacheDownload.getInstance().getRangeInfoFileName(rangeInfo), -1);
            hasNextDownloadFinishFile = false;
            hasDownloadNext = false;
        } else if (currentIndex != vAudioInfo.getSplitCount() - 1 && !hasDownloadNext) {
            currentIndex++;

            AudioCacheDownload.getInstance().download(vAudioInfo, currentIndex, vOnAudioFileDownloadNextListener);
        } else {
            isLoadEnd = true;
        }
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
