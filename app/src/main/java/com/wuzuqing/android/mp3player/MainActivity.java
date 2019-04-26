package com.wuzuqing.android.mp3player;

import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.wuzuqing.android.mp3player.audioplayer.MusicPlayHelper;
import com.wuzuqing.android.mp3player.audioplayer.OnProgressChangeListener;
import com.wuzuqing.android.mp3player.audioplayer.OnStateChangeListener;
import com.wuzuqing.android.mp3player.audioplayer.PlayState;
import com.wuzuqing.android.mp3player.audioplayer.util.LogUtils;

public class MainActivity extends AppCompatActivity {

    //    IPlayer MusicPlayHelper.get() = new SimpleIPlayer();
    private EditText vEditText, vEtIndex;
    private SeekBar vSeekBar;
    private ProgressBar progressBar;
    TextView logView;
    private TextView vTotalView, vCurrentView;
    Spinner vSpinner;

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
        vSpinner = findViewById(R.id.urls);
        logView = findViewById(R.id.log);
        MusicPlayHelper.get().bindSeekBar(vSeekBar);
        MusicPlayHelper.get().setOnStateChangeListener(new OnStateChangeListener() {
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
        MusicPlayHelper.get().setOnProgressChangeListener(vOnProgressChangeListener, true);
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
        ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, DataUtils.urls);
        vSpinner.setAdapter(adapter);
        vSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String url = DataUtils.urls[position % DataUtils.urls.length];
                setUrlToView(url);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

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
//        int index = Integer.valueOf(vEtIndex.getText().toString());
//        String url = DataUtils.urls[index % DataUtils.urls.length];
//        setUrlToView(url);
        MusicPlayHelper.get().playUrl(vEditText.getText().toString().trim());
    }

    private void setUrlToView(String url) {
        vEditText.setText(url);
        vEditText.setSelection(url.length());
    }


    public void pause(View view) {
        MusicPlayHelper.get().pause();
    }

    public void stop(View view) {
        MusicPlayHelper.get().stop();
    }

    public void resume(View view) {
        MusicPlayHelper.get().resume();
    }

    public void houTui(View view) {
        MusicPlayHelper.get().seekToWithOffset(false, 15000);
    }

    public void qianJin(View view) {
        MusicPlayHelper.get().seekToWithOffset(true, 15000);
    }

    public void clear(View view) {
        LogUtils.clearLog();
    }

    public void zjSpeed(View view) {
        MusicPlayHelper.get().changeSpeed(true, 0.2f);
    }

    public void jsSpeed(View view) {
        MusicPlayHelper.get().changeSpeed(false, 0.2f);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MusicPlayHelper.get().setOnProgressChangeListener(vOnProgressChangeListener, false);
    }

    public void clearFile(View view) {
        MusicPlayHelper.get().clearCacheFile();
    }
}
