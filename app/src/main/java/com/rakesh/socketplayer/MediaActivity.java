package com.rakesh.socketplayer;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.VideoView;

import java.io.File;

public class MediaActivity extends AppCompatActivity {

    Button play, stop;
    boolean audio;
    String path;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    MediaPlayer mediaPlayer;
    VideoView videoView;
    private Handler myHandler = new Handler();;
    SeekBar seekbar;
    boolean firstRun = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);

        path = getIntent().getStringExtra("file");

        int index = path.lastIndexOf(".");
        String ext = path.substring(index + 1);
        File file = new File(path);

        videoView = findViewById(R.id.videoView);
        ImageView image = findViewById(R.id.imageView);
        play = findViewById(R.id.play);
        stop = findViewById(R.id.stop);
        mediaPlayer = new MediaPlayer();

        seekbar = (SeekBar)findViewById(R.id.seekBar);
        seekbar.setClickable(false);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        editor = prefs.edit();

        /**
         * Image
         */
        if (ext.equals("jpeg") || ext.equals("png") || ext.equals("jpg")) {

            if (file.exists()) {
                videoView.setVisibility(View.GONE);
                image.setVisibility(View.VISIBLE);
                Bitmap myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                image.setImageBitmap(myBitmap);
                play.setVisibility(View.GONE);
                stop.setVisibility(View.GONE);
            }
        }

        /**
         * Audio
         */
        if (ext.equals("mp3") || ext.equals("wav") || ext.equals("aac")) {
            audio = true;
            videoView.setBackground(getResources().getDrawable(R.drawable.ic_music, getTheme()));
            try {
                mediaPlayer.setDataSource(path);
                mediaPlayer.prepare();
                if (firstRun) {
                    seekbar.setMax((int) mediaPlayer.getDuration());
                    firstRun = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * Video
         */
        if (ext.equals("mp4") || ext.equals("mpeg") || ext.equals("avi") || ext.equals("flv") || ext.equals("mkv")) {
            audio = false;
            videoView.setVideoPath(path);
            if (firstRun) {
                seekbar.setMax((int) videoView.getDuration());
                firstRun = false;
            }
        }

        seekIfResumed();

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audio) {
                    if(!mediaPlayer.isPlaying()) {
                        mediaPlayer.start();
                        play.setBackground(getResources().getDrawable(R.drawable.ic_pause, getTheme()));
                    } else {
                        mediaPlayer.pause();
                        play.setBackground(getResources().getDrawable(R.drawable.ic_play, getTheme()));
                    }
                    seekbar.setProgress((int)mediaPlayer.getCurrentPosition());
                    myHandler.postDelayed(updateSeekBar,100);
                } else {
                    if(!videoView.isPlaying()) {
                        videoView.start();
                        play.setBackground(getResources().getDrawable(R.drawable.ic_pause, getTheme()));
                    } else {
                        videoView.pause();
                        play.setBackground(getResources().getDrawable(R.drawable.ic_play, getTheme()));
                    }
                    seekbar.setProgress((int)videoView.getCurrentPosition());
                    myHandler.postDelayed(updateSeekBar,100);
                }

            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audio) {
                    if(mediaPlayer.isPlaying()) {
                        play.setBackground(getResources().getDrawable(R.drawable.ic_play, getTheme()));
                        mediaPlayer.seekTo(0);
                        mediaPlayer.pause();
                    }
                } else {
                    if(videoView.isPlaying()) {
                        play.setBackground(getResources().getDrawable(R.drawable.ic_play, getTheme()));
                        videoView.seekTo(0);
                        videoView.pause();
                    }
                }
            }
        });

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(audio && mediaPlayer != null && fromUser){
                    mediaPlayer.seekTo(progress);
                } else if(!audio && videoView != null && fromUser){
                    videoView.seekTo(progress);
                }
            }
        });
    }

    private Runnable updateSeekBar = new Runnable() {
        public void run() {
            seekbar.setProgress((int)mediaPlayer.getCurrentPosition());
            myHandler.postDelayed(this, 100);
        }
    };

    private void seekIfResumed() {
        int audioSeek = prefs.getInt("audio", 0);
        int videoSeek = prefs.getInt("video", 0);
        if (audioSeek!=0) {
            audio = true;
            mediaPlayer.seekTo(audioSeek);
            play.callOnClick();
        } else if (videoSeek!=0) {
            audio = false;
            videoView.seekTo(videoSeek);
            play.callOnClick();
        }
    }

    @Override
    protected void onPause() {
        if (audio && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        } else if (!audio && videoView.isPlaying()) {
            videoView.stopPlayback();
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        editor.putInt("audio", mediaPlayer.getCurrentPosition());
        editor.putInt("video", videoView.getCurrentPosition());
        editor.commit();
        super.onStop();
    }
}
