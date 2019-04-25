package com.wuzuqing.android.mp3player.audioplayer;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.os.Build;

import com.wuzuqing.android.mp3player.audioplayer.util.LogUtils;

import java.io.IOException;

/**
 * 作者：士元
 * 时间：2019/2/23 9:46
 * 邮箱：wuzuqing@linghit.com
 * 说明：大音频文件的播放器，边下边播
 */
public class LargeAudioPlayer implements IPlayer {
    private MediaPlayer vOneMediaPlayer;
    private MediaPlayer vTwoMediaPlayer;
    private MediaPlayer vCurrentMediaPlayer;
    private String lastUrl;
    private boolean isPrepared;
    private int seekToPos;


    private int touchToPos;
    private int currentIndex;
    private AudioInfo vAudioInfo;
    private boolean isLoadEnd;

    private PlayState vPlayState = PlayState.NONE;


    private void initOnePlayer() {
        if (vOneMediaPlayer == null) {
            vOneMediaPlayer = new MediaPlayer();
            vOneMediaPlayer.setOnCompletionListener(vOnCompletionListener);
            vOneMediaPlayer.setOnPreparedListener(vOnNextPreparedListener);
            vOneMediaPlayer.setOnErrorListener(vOnErrorListener);
            vOneMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            PlayerProgressManager.get().bindPlayer(this);
        }
    }

    private void initTwoPlayer() {
        if (vTwoMediaPlayer == null) {
            vTwoMediaPlayer = new MediaPlayer();
            vTwoMediaPlayer.setOnCompletionListener(vOnCompletionListener);
            vTwoMediaPlayer.setOnPreparedListener(vOnNextPreparedListener);
            vTwoMediaPlayer.setOnErrorListener(vOnErrorListener);
            vTwoMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
    }

    private MediaPlayer.OnCompletionListener vOnCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            LogUtils.d("onCompletion: " + currentIndex + " / " + vAudioInfo.getSplitCount() + " hasNextPrepared : " + hasNextPrepared);
            moveToNext(mp);
        }
    };

    private void moveToNext(MediaPlayer mp) {
        if (vPlayState==PlayState.PAUSE){
            return;
        }
        currentIndex++;
        hasNextPrepared = false;
        if (mp == vOneMediaPlayer) {
            vCurrentMediaPlayer = vTwoMediaPlayer;
        } else {
            vCurrentMediaPlayer = vOneMediaPlayer;
        }
        if (vCurrentMediaPlayer == null) {
            return;
        }
        if (!vCurrentMediaPlayer.isPlaying()) {
            vCurrentMediaPlayer.start();
        }
        mp.reset();
        resetUpdateProgressHandler();
        isPrepared = true;
        hasNextDownloadFinishFile = false;
        hasDownloadNext = false;
    }

    private MediaPlayer.OnPreparedListener vOnNextPreparedListener = new MediaPlayer.OnPreparedListener() {

        @Override
        public void onPrepared(MediaPlayer mp) {
            if (vCurrentMediaPlayer == null) {
                vCurrentMediaPlayer = mp;
                isPrepared = true;
                innerPlayerSeekTo(mp);
                oneFileDuration = vAudioInfo.getMediaType().getOneFileCacheSecond() * 1000;
                mp.start();
                _innerCallNewState(PlayState.START);
                PlayerProgressManager.get().prepared(getDuration());
                resetUpdateProgressHandler();
            } else {
                if (vCurrentMediaPlayer == mp) {
                    vCurrentMediaPlayer.setNextMediaPlayer(mp);
                }
                hasNextPrepared = true;
            }
            LogUtils.d("onPrepared isOne:" + ((mp == vOneMediaPlayer) ? "vOneMediaPlayer" : "vTwoMediaPlayer"));
        }
    };

    private MediaPlayer.OnErrorListener vOnErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            int second = (mp.getCurrentPosition()) / 1000;
            if (hasNextPrepared && vAudioInfo != null && second > vAudioInfo.getMediaType().getOneFileCacheSecond() - 5) {
                moveToNext(mp);
            }
            LogUtils.d(" what:" + what + " extra:" + extra + " second:" + second + " hasNextDownloadFinishFile:" + hasNextDownloadFinishFile + " isPrepared:" + isPrepared);
            return true;
        }
    };


    public void resume() {
        if (isPrepared) {
            vCurrentMediaPlayer.start();
            resetUpdateProgressHandler();
            _innerCallNewState(PlayState.START);
        }
    }

    private void _innerCallNewState(PlayState playState) {
        if (playState == vPlayState) {
            return;
        }
        vPlayState = playState;
        if (mOnStateChangeListener != null) {
            mOnStateChangeListener.changeState(playState);
        }
    }

    public void pause() {
        if (isPrepared) {
            vCurrentMediaPlayer.pause();
            PlayerProgressManager.get().pause();
            _innerCallNewState(PlayState.PAUSE);
        }
    }

    public void stop() {
        if (vCurrentMediaPlayer != null && isPrepared) {
            LogUtils.d("stop: " + currentIndex + " / " + vAudioInfo.getSplitCount() + " isPrepared:" + isPrepared);
            vCurrentMediaPlayer.stop();
            vCurrentMediaPlayer.reset();
            vCurrentMediaPlayer = null;
            isPrepared = false;
            PlayerProgressManager.get().pause();
            _innerCallNewState(PlayState.STOP);
            PlayerProgressManager.get().setViewZero();
        }
        lastUrl = null;
    }


    public void playUrl(String url) {
        currentIndex = 0;
        PlayerProgressManager.get().setMax(0);
        playWithPos(url, -1);
    }

    public void seekToWithOffset(boolean isAdd, int offset) {
        LogUtils.d("seekToWithOffset:" + offset);
        if (isPrepared && vCurrentMediaPlayer != null) {
            int currentPosition = vCurrentMediaPlayer.getCurrentPosition();
            int realPosition = currentIndex * oneFileDuration + currentPosition;
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
                if (vCurrentMediaPlayer == vOneMediaPlayer) {
                    vTwoMediaPlayer.reset();
                } else {
                    vOneMediaPlayer.reset();
                }
                vCurrentMediaPlayer.setNextMediaPlayer(null);
            }
            innerStartToDownload(realPosition, vAudioInfo);
        }
    }


    private void innerStart(String url, boolean a) {
        boolean isOne = vCurrentMediaPlayer == null || vCurrentMediaPlayer == vTwoMediaPlayer;
        if (isOne) {
            initOnePlayer();
        } else {
            initTwoPlayer();
        }
        try {
            LogUtils.d("innerStart setDataSource:" + isOne);
            if (isOne) {
                vOneMediaPlayer.reset();
                vOneMediaPlayer.setDataSource(url);
                _innerCallNewState(PlayState.PREPARE);
                vOneMediaPlayer.prepareAsync();
                lastUrl = url;
            } else {
                vTwoMediaPlayer.reset();
                vTwoMediaPlayer.setDataSource(url);
                vTwoMediaPlayer.prepareAsync();
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
        int oldIndex = currentIndex;
        if (pos == -1) {
            seekToPos = -1;
        } else {
            float ms = 1f * oneFileDuration;
            currentIndex = (int) (pos / ms);
            seekToPos = pos % (int) ms;
        }
        if (oldIndex != currentIndex) {
            pause();
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
            //开始播放
            startWithOffset(AudioCacheDownload.getInstance().getRangeInfoFileName(rangeInfo), seekToPos);
        }

        @Override
        public void onError(AudioInfo audioInfo, AudioError error) {
            startWithOffset(audioInfo.getUrl(), seekToPos);
        }

        @Override
        public void onLoading() {
            _innerCallNewState(PlayState.LOADING);
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

        @Override
        public void onLoading() {

        }
    };
    private int oneFileDuration = 0;

    private void startWithOffset(String url, int toPos) {
        LogUtils.d("startSeekToWithOffset:" + toPos);
        this.seekToPos = toPos;
        if (url != null && url.equals(lastUrl)) {
            if (isPrepared) {
                innerPlayerSeekTo(vCurrentMediaPlayer);
                _innerCallNewState(PlayState.START);
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

    @Override
    public void notifyProgress(int progress) {
        checkNeedDownload(vAudioInfo, vCurrentMediaPlayer.getCurrentPosition(), oneFileDuration);
    }

    @Override
    public int getCurrentPosition() {
        if (vAudioInfo == null) {
            return 0;
        }
        int currentPosition = vCurrentMediaPlayer.getCurrentPosition();
        return currentIndex * oneFileDuration + currentPosition;
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
        listener.onLoading();
        AudioCacheDownload.getInstance().download(vAudioInfo, index, listener);
    }


    private void resetUpdateProgressHandler() {
        PlayerProgressManager.get().start();
    }


    @Override
    public boolean isPlaying() {
        return vCurrentMediaPlayer != null && vCurrentMediaPlayer.isPlaying();
    }

    @Override
    public void changeSpeed(boolean isAdd, float speed) {
        if (!isPrepared) {
            return;
        }
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

    private OnStateChangeListener mOnStateChangeListener;

    @Override
    public void setOnStateChangeListener(OnStateChangeListener listener) {
        mOnStateChangeListener = listener;
    }

    @Override
    public void seekTo(int offset) {
        pause();
        playWithPos(vAudioInfo.getUrl(), offset);
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

}
