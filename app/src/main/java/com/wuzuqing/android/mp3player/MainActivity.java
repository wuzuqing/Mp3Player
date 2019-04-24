package com.wuzuqing.android.mp3player;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.wuzuqing.android.mp3player.audioplayer.IPlayer;
import com.wuzuqing.android.mp3player.audioplayer.OnProgressChangeListener;
import com.wuzuqing.android.mp3player.audioplayer.OnStateChangeListener;
import com.wuzuqing.android.mp3player.audioplayer.PlayState;
import com.wuzuqing.android.mp3player.audioplayer.PlayerProgressManager;
import com.wuzuqing.android.mp3player.audioplayer.SimpleIPlayer;
import com.wuzuqing.android.mp3player.audioplayer.util.LogUtils;

public class MainActivity extends AppCompatActivity {

    IPlayer vSimplePlayer = new SimpleIPlayer();
    private EditText vEditText, vEtIndex;
    private SeekBar vSeekBar;
    private ProgressBar progressBar;
    TextView logView;
    private TextView vTotalView, vCurrentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        vEditText = findViewById(R.id.et_length);
        vEtIndex = findViewById(R.id.et_index);
        vSeekBar = findViewById(R.id.seekTo);
        vTotalView = findViewById(R.id.tvTotalTime);
        vCurrentView = findViewById(R.id.tvCurrentTime);
        progressBar = findViewById(R.id.progressBar);
        logView = findViewById(R.id.log);
        PlayerProgressManager.get().bindSeekBar(vSeekBar);
        vSimplePlayer.setOnStateChangeListener(new OnStateChangeListener() {
            @Override
            public void changeState(final PlayState newState) {
                LogUtils.d("changeState:" + newState);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (newState == PlayState.LOADING) {
                            progressBar.setVisibility(View.VISIBLE);
                        } else {
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });
        setUrlToView(DataUtils.urls[0]);
        PlayerProgressManager.get().registerOnProgressChangeListener(vOnProgressChangeListener);
//        PlayerProgressManager.get().bindTextView((TextView) findViewById(R.id.tvCurrentTime), (TextView) findViewById(R.id.tvTotalTime));

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
        PermissionUtil permissionUtil = new PermissionUtil(this);
        permissionUtil.requestStoragePermission(1000);
    }

    private OnProgressChangeListener vOnProgressChangeListener = new OnProgressChangeListener() {
        @Override
        public void changeDuration(String timeStr) {
            vTotalView.setText(timeStr);
        }

        @Override
        public void changeProgress(int position, String timeStr) {
            vCurrentView.setText(timeStr);
        }
    };

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
        vSimplePlayer.changeSpeed(true, 0.2f);
    }

    public void jsSpeed(View view) {
        vSimplePlayer.changeSpeed(false, 0.2f);
    }

    @Override
    protected void onDestroy() {
        PlayerProgressManager.get().unRegisterOnProgressChangeListener(vOnProgressChangeListener);
    }
}
