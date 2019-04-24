package com.wuzuqing.android.mp3player.audioplayer;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.os.Build;

import java.io.IOException;

/**
 * 作者：士元
 * 时间：2019/3/4 10:12
 * 邮箱：wuzuqing@linghit.com
 * 说明：播放器API 内部播放使用 mediaPlayer
 */
public class SimpleIPlayer implements IPlayer {
    private MediaPlayer mPlayer;
    private PlayState vPlayState = PlayState.NONE;
    private int offset;

    public SimpleIPlayer() {
    }

    private void initPlayer() {
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
            mPlayer.setOnCompletionListener(vOnCompletionListener);
            mPlayer.setOnPreparedListener(vOnPreparedListener);
            mPlayer.setOnErrorListener(vOnErrorListener);
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
        PlayerProgressManager.get().bindPlayer(this);
    }

    private MediaPlayer.OnCompletionListener vOnCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            _innerCallNewState(PlayState.NONE);
        }
    };

    private MediaPlayer.OnPreparedListener vOnPreparedListener = new MediaPlayer.OnPreparedListener() {

        @Override
        public void onPrepared(MediaPlayer mp) {
            if (offset != -1) {
                mp.seekTo(offset);
                offset = -1;
            }
            _innerCallNewState(PlayState.START);
            PlayerProgressManager.get().prepared(offset);
        }
    };


    private MediaPlayer.OnErrorListener vOnErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            _innerCallNewState(PlayState.NONE);
            return true;
        }
    };


    @Override
    public void playUrl(String url) {
        playWithPos(url, -1);
    }

    private String currentUrl;

    @Override
    public void playWithPos(String url, int offset) {
        initPlayer();
        try {
            if (currentUrl != null && currentUrl.equals(url)) {
                mPlayer.seekTo(0);
                if (vPlayState == PlayState.PAUSE) {
                    mPlayer.start();
                }
                return;
            } else if (isPlaying()) {
                mPlayer.stop();
                mPlayer.reset();
            } else if (vPlayState == PlayState.PREPARE) {
                mPlayer.reset();
            }
            mPlayer.setDataSource(url);
            this.offset = offset;
            this.currentUrl = url;
            PlayerProgressManager.get().reset();
            mPlayer.prepareAsync();
            _innerCallNewState(PlayState.PREPARE);
        } catch (IOException e) {
            e.printStackTrace();
            this.offset = -1;
            _innerCallNewState(PlayState.NONE);
        }
    }

    @Override
    public void seekTo(int offset) {
        if (mPlayer != null) {
            mPlayer.seekTo(offset);
        }
    }

    @Override
    public void resume() {
        if (vPlayState == PlayState.PAUSE) {
            _innerCallNewState(PlayState.START);
        } else if (vPlayState == PlayState.START) {
            pause();
        }
    }

    @Override
    public void notifyProgress(int progress) {

    }

    @Override
    public void pause() {
        _innerCallNewState(PlayState.PAUSE);
    }

    @Override
    public void stop() {
        _innerCallNewState(PlayState.STOP);
    }

    @Override
    public void seekToWithOffset(boolean isAdd, int offset) {
        if (vPlayState == PlayState.START || vPlayState == PlayState.PAUSE) {
            int currentPosition = getCurrentPosition();
            int duration = getDuration();
            if (isAdd) {
                currentPosition += offset;
                if (currentPosition > duration) {
                    currentPosition = duration;
                }
            } else {
                currentPosition -= offset;
                if (currentPosition < 0) {
                    currentPosition = 0;
                }
            }
            mPlayer.seekTo(currentPosition);
        }
    }

    @Override
    public int getCurrentPosition() {
        return mPlayer.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        return mPlayer.getDuration();
    }

    @Override
    public boolean isPlaying() {
        return mPlayer.isPlaying();
    }


    @Override
    public void changeSpeed(boolean isAdd, float speed) {
        if (vPlayState == PlayState.NONE) {
            return;
        }
        if (canSetPlaybackParams()) {
            PlaybackParams params = mPlayer.getPlaybackParams();
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
            mPlayer.setPlaybackParams(params);
        }
    }

    @Override
    public void resetSpeed() {
        if (canSetPlaybackParams()) {
            PlaybackParams params = mPlayer.getPlaybackParams();
            if (params == null || params.getSpeed() == 1f) {
                return;
            }
            params.setSpeed(1f);
            mPlayer.setPlaybackParams(params);
        }
    }

    private OnStateChangeListener mOnStateChangeListener;

    private void _innerCallNewState(PlayState playState) {
        vPlayState = playState;
        switch (playState) {
            case START:
                mPlayer.start();
                updatePos(true);
                break;
            case PAUSE:
                mPlayer.pause();
                updatePos(false);
                break;
            case STOP:
                mPlayer.stop();
                mPlayer.reset();
                this.currentUrl = null;
                updatePos(false);
                break;
            case NONE:
                mPlayer.reset();
                updatePos(false);
            default: {
                updatePos(false);
                break;
            }
        }
        if (mOnStateChangeListener != null) {
            mOnStateChangeListener.changeState(playState);
        }
    }

    private void updatePos(boolean start) {
        if (start) {
            PlayerProgressManager.get().start();
        } else {
            PlayerProgressManager.get().pause();
        }
    }

    @Override
    public void setOnStateChangeListener(OnStateChangeListener listener) {
        mOnStateChangeListener = listener;
    }


    private boolean canSetPlaybackParams() {
        return Build.VERSION.SDK_INT >= 23;
    }


}
