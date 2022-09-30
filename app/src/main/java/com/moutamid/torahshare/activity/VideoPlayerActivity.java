package com.moutamid.torahshare.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import com.khizar1556.mkvideoplayer.MKPlayerActivity;
import com.moutamid.torahshare.databinding.ActivityVideoPlayerBinding;
import com.moutamid.torahshare.utils.Constants;

public class VideoPlayerActivity extends AppCompatActivity {

    private ActivityVideoPlayerBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityVideoPlayerBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        String link = getIntent().getStringExtra(Constants.PARAMS);
        MKPlayerActivity.configPlayer(this).play(link);
//        Uri uri2 = Uri.parse(link);
//        b.videoViewPlayerActivity.setVideoURI(uri2);
//
//        b.videoViewPlayerActivity.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//            @Override
//            public void onPrepared(MediaPlayer mediaPlayer) {
//                mediaPlayer.start();
//            }
//        });
    }
}