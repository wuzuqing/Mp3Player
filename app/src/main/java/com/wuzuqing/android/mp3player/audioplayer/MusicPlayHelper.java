package com.wuzuqing.android.mp3player.audioplayer;

/**
 * 作者：士元
 * 时间：2019/4/24 19:25
 * 邮箱：wuzuqing@linghit.com
 * 说明：
 */
public class MusicPlayHelper implements IPlayer {
    private static MusicPlayHelper instance = new MusicPlayHelper();
    private IPlayer vIPlayer;

    public static MusicPlayHelper get() {
        return instance;
    }

    public void init(IPlayer player) {
        this.vIPlayer = player;
    }

    @Override
    public void playUrl(String url) {
        vIPlayer.playUrl(url);
    }

    @Override
    public void playWithPos(String url, int offset) {
        vIPlayer.playWithPos(url, offset);
    }

    @Override
    public void seekTo(int offset) {
        vIPlayer.seekTo(offset);
    }

    @Override
    public void resume() {
        vIPlayer.resume();
    }

    @Override
    public void pause() {
        vIPlayer.pause();
    }

    @Override
    public void stop() {
        vIPlayer.stop();
    }

    @Override
    public void seekToWithOffset(boolean isAdd, int offset) {
        vIPlayer.seekToWithOffset(isAdd, offset);
    }

    @Override
    public int getCurrentPosition() {
        return vIPlayer.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        return vIPlayer.getDuration();
    }

    @Override
    public boolean isPlaying() {
        return vIPlayer.isPlaying();
    }

    @Override
    public void changeSpeed(boolean isAdd, float speed) {
        vIPlayer.changeSpeed(isAdd, speed);
    }

    @Override
    public void resetSpeed() {
        vIPlayer.resetSpeed();
    }

    @Override
    public void setOnStateChangeListener(OnStateChangeListener listener) {
        vIPlayer.setOnStateChangeListener(listener);
    }
}
