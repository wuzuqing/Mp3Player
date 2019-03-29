package com.wuzuqing.android.mp3player;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.wuzuqing.android.mp3player.audioplayer.LogUtils;
import com.wuzuqing.android.mp3player.audioplayer.SimplePlayer;

public class MainActivity extends AppCompatActivity {

    SimplePlayer vSimplePlayer = new SimplePlayer();
    private EditText vEditText, vEtIndex;
    private SeekBar vSeekBar;
    TextView logView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        vEditText = findViewById(R.id.et_length);
        vSeekBar = findViewById(R.id.seekTo);
        logView = findViewById(R.id.log);
        vSimplePlayer.bindSeekBar(vSeekBar);

        int index = 0;
        String url = DataUtils.urls[index % DataUtils.urls.length];
        vEditText.setText(url);
        vEditText.setSelection(url.length());
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
}
