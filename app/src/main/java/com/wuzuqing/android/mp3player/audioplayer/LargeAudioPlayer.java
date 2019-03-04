package com.wuzuqing.android.mp3player.audioplayer;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.Locale;

/**
 * 作者：士元
 * 时间：2019/2/23 9:46
 * 邮箱：wuzuqing@linghit.com
 * 说明：
 */
public class LargeAudioPlayer implements IPlayer {
    private MediaPlayer vCurrentMediaPlayer;
    private MediaPlayer vNextMediaPlayer;
    private String lastUrl;
    private boolean isPrepared;
    private int seekToPos;


    private int touchToPos;
    private int currentIndex;
    private AudioInfo vAudioInfo;
    private boolean isLoadEnd;

    private PlayState vPlayState = PlayState.NONE;

    private void initPlayer() {
        if (vCurrentMediaPlayer == null) {
            vCurrentMediaPlayer = new MediaPlayer();
            vCurrentMediaPlayer.setOnCompletionListener(vOnCompletionListener);
            vCurrentMediaPlayer.setOnPreparedListener(vOnPreparedListener);
            vCurrentMediaPlayer.setOnErrorListener(vOnErrorListener);
            vCurrentMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
    }

    private void initNextPlayer() {
        if (vNextMediaPlayer == null) {
            vNextMediaPlayer = new MediaPlayer();
            vNextMediaPlayer.setOnCompletionListener(vOnCompletionListener);
            vNextMediaPlayer.setOnPreparedListener(vOnPreparedListener);
            vCurrentMediaPlayer.setOnErrorListener(vOnErrorListener);
            vNextMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
    }

    private MediaPlayer.OnCompletionListener vOnCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            LogUtils.d("onCompletion: " + currentIndex + " / " + vAudioInfo.getSplitCount());
            stop();
            if (hasNextPrepared) {
                playNext();
            } else if (currentIndex != vAudioInfo.getSplitCount() - 1 && !hasDownloadNext) {
                currentIndex++;
                AudioCacheDownload.getInstance().download(vAudioInfo, currentIndex, vOnAudioFileDownloadNextListener);
            } else {
                isLoadEnd = true;
            }
        }
    };

    private MediaPlayer.OnPreparedListener vOnPreparedListener = new MediaPlayer.OnPreparedListener() {

        @Override
        public void onPrepared(MediaPlayer mp) {
            if (mp == vNextMediaPlayer) {
                if (!isPrepared) {
                    playNext();
                } else {
                    hasNextPrepared = true;
                }
                LogUtils.d("onPrepared vNextMediaPlayer:");
                return;
            }
            if (hasNextPrepared) {
                playNext();
            } else {
                isPrepared = true;
                innerPlayerSeekTo(mp);
                mp.start();
                vPlayState = PlayState.START;
                resetUpdateProgressHandler();
                LogUtils.d("onPrepared: lastUrl:" + lastUrl + " duration: " + mp.getDuration() + " seekToPos:" + seekToPos);
            }
        }
    };

    private MediaPlayer.OnErrorListener vOnErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            int second = (mp.getCurrentPosition()) / 1000;
            if (second > 175) {
                if (hasNextPrepared) {
                    playNext();
                }
            }
            LogUtils.d(" what:" + what + " extra:" + extra + " second:" + second + " hasNextDownloadFinishFile:" + hasNextDownloadFinishFile + " isPrepared:" + isPrepared);
            return true;
        }
    };


    public void resume() {
        if (isPrepared) {
            vCurrentMediaPlayer.start();
            resetUpdateProgressHandler();
            vPlayState = PlayState.START;
        }
    }


    public void pause() {
        if (isPrepared) {
            vPlayState = PlayState.PAUSE;
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
            vPlayState = PlayState.STOP;
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
            if (hasNextPrepared) {
                hasNextPrepared = false;
                hasDownloadNext = false;
                hasNextDownloadFinishFile = false;
                vNextMediaPlayer.reset();
            }
            innerStartToDownload(realPosition, vAudioInfo);
        }
    }


    private void innerStart(String url, boolean isCurrent) {
        if (isCurrent) {
            initPlayer();
        } else {
            initNextPlayer();
        }
        try {
            LogUtils.d("innerStart setDataSource:");
            if (isCurrent) {
                vCurrentMediaPlayer.reset();
                vCurrentMediaPlayer.setDataSource(url);
                vPlayState = PlayState.PREPARE;
                vCurrentMediaPlayer.prepareAsync();
                lastUrl = url;
            } else {
                vNextMediaPlayer.reset();
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
     * @param mp 需要移动的播放器
     */
    private void innerPlayerSeekTo(MediaPlayer mp) {
        if (seekToPos != -1) {
            mp.seekTo(seekToPos);
            seekToPos = -1;
        }
    }

    /**
     * @param url 网络音频链接
     * @param pos 秒
     */
    public void playWithPos(String url, final int pos) {
        this.touchToPos = pos;

        this.vAudioInfo = AudioCache.getInstance().getAudioInfo(url);
        LogUtils.d("playWithPos:" + pos);
        if (!vAudioInfo.isInit()) {
            //初始化
            AudioCacheDownload.getInstance().syncInitContentLength(vAudioInfo, vAudioFileInitListener);
        } else {
            //开始下载
            innerStartToDownload(pos, vAudioInfo);
        }
    }

    private void innerStartToDownload(int pos, AudioInfo audioInfo) {
        if (pos == -1) {
            seekToPos = -1;
        } else {
            float ms = (1000f * audioInfo.getMediaType().getOneFileCacheSecond());
            currentIndex = (int) (pos / ms);
            seekToPos = pos % (int) ms;
        }
        LogUtils.d(" innerStartToDownload seekToPos:" + seekToPos + " pos:" + pos + " currentIndex:" + currentIndex);
        download(currentIndex, vOnAudioFileDownloadListener);
    }


    private OnAudioFileInitListener vAudioFileInitListener = new OnAudioFileInitListener() {
        @Override
        public void onInit(AudioInfo audioInfo) {
            innerStartToDownload(touchToPos, audioInfo);
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
            innerStart(AudioCacheDownload.getInstance().getRangeInfoFileName(rangeInfo), false);
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
                innerPlayerSeekTo(vCurrentMediaPlayer);
                vCurrentMediaPlayer.start();
                resetUpdateProgressHandler();
            } else {
                innerStart(url, true);
            }
        } else {
            stop();
            innerStart(url, true);
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

    /**
     * 检查是否需要下载下一段文件
     *
     * @param audioInfo
     * @param position
     * @param duration
     */
    private void checkNeedDownload(AudioInfo audioInfo, int position, int duration) {
        if (currentIndex == audioInfo.getSplitCount() - 1) {
            isLoadEnd = true;
            return;
        }
        if (!isLoadEnd && !hasDownloadNext && position + 12000 > duration) {
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


    private void playNext() {
        currentIndex++;
        hasNextPrepared = false;
        stop();
        MediaPlayer temp = vCurrentMediaPlayer;
        vCurrentMediaPlayer = vNextMediaPlayer;
        vCurrentMediaPlayer.start();
        vNextMediaPlayer = temp;
        //重新更新进度提示
        resetUpdateProgressHandler();
        isPrepared = true;
        hasNextDownloadFinishFile = false;
        hasDownloadNext = false;
        vPlayState = PlayState.START;
    }

    private void resetUpdateProgressHandler() {
        vHandler.removeCallbacksAndMessages(null);
        vHandler.sendEmptyMessage(0);
    }

    private boolean isTouchSeekBar;
    private SeekBar vSeekBar;

    private TextView vCurrentTextView, vTotalTextView;


    @Override
    public boolean isPlaying() {
        return vCurrentMediaPlayer != null && vCurrentMediaPlayer.isPlaying();
    }

    @Override
    public int getCurrentPosition() {
        if (vAudioInfo == null) {
            return 0;
        }
        int currentPosition = vCurrentMediaPlayer.getCurrentPosition();
        return currentIndex * vAudioInfo.getMediaType().getOneFileCacheSecond() * 1000 + currentPosition;
    }

    @Override
    public void changeSpeed(boolean isAdd, float speed) {
        if (canSetPlaybackParams()) {
            PlaybackParams params = vCurrentMediaPlayer.getPlaybackParams();
            if (params == null) {
                params = new PlaybackParams();
            }
            float newSpeed;
            if (isAdd) {
                if (params.getSpeed() == 3f) {
                    return;
                }
                newSpeed = params.getSpeed() + speed;
                newSpeed = newSpeed > 3f ? 3f : newSpeed;
            } else {
                if (params.getSpeed() == 0.1f) {
                    return;
                }
                newSpeed = params.getSpeed() - speed;
                newSpeed = newSpeed <= 0.1f ? 0.1f : newSpeed;
            }
            params.setSpeed(newSpeed);
            vCurrentMediaPlayer.setPlaybackParams(params);
        }
    }

    private boolean canSetPlaybackParams() {
        return Build.VERSION.SDK_INT >= 23;
    }

    @Override
    public void resetSpeed() {
        if (canSetPlaybackParams()) {
            PlaybackParams params = vCurrentMediaPlayer.getPlaybackParams();
            if (params == null || params.getSpeed() == 1f) {
                return;
            }
            params.setSpeed(1f);
            vCurrentMediaPlayer.setPlaybackParams(params);
        }
    }

    @Override
    public int getDuration() {
        return vAudioInfo == null ? 0 : vAudioInfo.getDuration();
    }

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
                if (seekBar.getMax() > 0 && Math.abs(touchPos - seekBar.getProgress()) > 3000 && vAudioInfo != null) {
                    pause();
                    playWithPos(vAudioInfo.getUrl(), seekBar.getProgress());
                    isTouchSeekBar = false;
                }
            }
        });
    }
}