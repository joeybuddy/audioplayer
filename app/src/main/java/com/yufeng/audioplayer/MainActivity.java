package com.yufeng.audioplayer;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class MainActivity extends Activity implements View.OnClickListener {

    private int realDuration;
    private int duration;
    private Button btnPlay;
    private Button btnStop;

    private TextView tvElapsedDuration;
    private TextView tvDuration;
    private TextView tvTitle;
    private TextView tvArtist;
    private ImageView ivPicture;

    private static MediaPlayer player = new MediaPlayer();
    private static MediaMetadataRetriever mmr = new MediaMetadataRetriever();

    private static SeekBar seekBar;

    private boolean wasPlaying = false;
    private boolean finished = true;

    private static final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvTitle = (TextView)findViewById(R.id.title);
        tvArtist = (TextView)findViewById(R.id.artist);
        ivPicture = (ImageView)findViewById(R.id.picture);
        tvElapsedDuration = (TextView)findViewById(R.id.elapsed_duration);
        tvDuration = (TextView)findViewById(R.id.duration);
        seekBar = (SeekBar)findViewById(R.id.seek_bar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (player != null && fromUser) {
                    // TODO: change the aligned progress text
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (player != null && player.isPlaying()) {
                    player.pause();
                    wasPlaying = true;
                } else {
                    wasPlaying = false;
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (player != null) {
                    int progress = seekBar.getProgress();
//                    Log.d("DEBUG", String.format("dragged to %d while position was %d", progress, player.getCurrentPosition()));
                    player.seekTo(progress * 1000);
                    updateElapsedDuration(progress);
                    if (!player.isPlaying() && wasPlaying) {
                        player.start();
                    }
                }
            }
        });
        btnPlay = (Button)findViewById(R.id.play);
        btnStop = (Button)findViewById(R.id.stop);
        btnPlay.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        initMediaPlayer();
        updateElapsedDurationRunner = new Runnable() {

            @Override
            public void run() {
                if(player != null && wasPlaying){
                    int currentPosition = player.getCurrentPosition();
                    int currentProgress = currentPosition / 1000;
                    if (currentPosition > realDuration - 1000) {
                        // TODO: take action base on loop type
                        resetMediaPlayer();
                        initMediaPlayer();
                    } else {
                        seekBar.setProgress(currentProgress);
                        updateElapsedDuration(currentProgress);
                    }
                }
                handler.postDelayed(this, 1000);
            }
        };
        MainActivity.this.runOnUiThread(updateElapsedDurationRunner);
    }

    private void updateElapsedDuration(int duration) {
        tvElapsedDuration.setText(formatDuration(duration));
    }

    private String formatDuration(int duration) {
        return String.format("%02d:%02d", duration / 60, duration % 60);
    }

    private void initMediaPlayer() {
        try {
            File file = new File(Environment.getExternalStorageDirectory() + "/Music/Feels Like We Only Go Backwards.mp3");
            String path = file.getPath();
            player.setDataSource(path);
            mmr.setDataSource(path);
            tvTitle.setText(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
            tvArtist.setText(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST));
            byte[] pictureBytes = mmr.getEmbeddedPicture();
            Bitmap bm = BitmapFactory.decodeByteArray(pictureBytes, 0, pictureBytes.length);
            ivPicture.setImageBitmap(bm);
            realDuration = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            duration = realDuration / 1000;
            tvDuration.setText(formatDuration(duration));
            Log.d("DEBUG", "duration: " + Integer.toString(realDuration));
            seekBar.setMax(duration);
            player.prepare();
            wasPlaying = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Runnable updateElapsedDurationRunner;

    private void resetMediaPlayer() {
        if (player.isPlaying()) {
            player.stop();
        }
        wasPlaying = false;
        player.reset();
        btnPlay.setText(R.string.play);
        updateElapsedDuration(0);
        seekBar.setProgress(0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play:
                finished = false;
                if (!player.isPlaying()) {
                    player.start();
                    wasPlaying = true;
                    btnPlay.setText(R.string.pause);
                } else {
                    player.pause();
                    wasPlaying = false;
                    btnPlay.setText(R.string.play);
                }
                break;
            case R.id.stop:
                resetMediaPlayer();
                initMediaPlayer();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            resetMediaPlayer();
            handler.removeCallbacksAndMessages(null);
            player.release();
        }
    }
}
