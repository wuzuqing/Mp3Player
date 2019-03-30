package com.wuzuqing.android.mp3player;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.wuzuqing.android.mp3player.audioplayer.IPlayer;
import com.wuzuqing.android.mp3player.audioplayer.LogUtils;
import com.wuzuqing.android.mp3player.audioplayer.LargeAudioPlayer;

public class MainActivity extends AppCompatActivity {

    IPlayer vSimplePlayer = new LargeAudioPlayer();
    private EditText vEditText, vEtIndex;
    private SeekBar vSeekBar;
    TextView logView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        vEditText = findViewById(R.id.et_length);
        vEtIndex = findViewById(R.id.et_index);
        vSeekBar = findViewById(R.id.seekTo);
        logView = findViewById(R.id.log);
        vSimplePlayer.bindSeekBar(vSeekBar);
        setUrlToView(DataUtils.urls[0]);
        vSimplePlayer.bindTextView((TextView) findViewById(R.id.tvCurrentTime), (TextView) findViewById(R.id.tvTotalTime));
        LogUtils.setvOnLogChangeListener(new LogUtils.OnLogChangeListener() {
            @Override
            public void newLog(final String log) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logView.setText(log);
                    }
                });
            }
        });
    }

    public void start(View view) {
        int index = Integer.valueOf(vEtIndex.getText().toString());
        String url = DataUtils.urls[index % DataUtils.urls.length];
        vSimplePlayer.playUrl(url);
        setUrlToView(url);
    }

    private void setUrlToView(String url) {
        vEditText.setText(url);
        vEditText.setSelection(url.length());
    }


    public void pause(View view) {
        vSimplePlayer.pause();
    }

    public void stop(View view) {
        vSimplePlayer.stop();
    }

    public void resume(View view) {
        vSimplePlayer.resume();
    }

    public void houTui(View view) {
        vSimplePlayer.seekToWithOffset(false, 15000);
    }

    public void qianJin(View view) {
        vSimplePlayer.seekToWithOffset(true, 15000);
    }

    public void clear(View view) {
        LogUtils.clearLog();
    }

    public void zjSpeed(View view) {
        vSimplePlayer.changeSpeed(true,0.2f);
    }

    public void jsSpeed(View view) {
        vSimplePlayer.changeSpeed(false,0.2f);
    }
}
