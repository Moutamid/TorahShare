package com.moutamid.torahsharee.activity;

import static androidx.media3.ui.PlayerView.SHOW_BUFFERING_WHEN_PLAYING;

import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.TrackGroup;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.exoplayer.source.TrackGroupArray;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.exoplayer.trackselection.MappingTrackSelector;
import androidx.media3.ui.PlayerView;

import com.moutamid.torahsharee.databinding.ActivityVideoPlayerBinding;
import com.moutamid.torahsharee.utils.Constants;

public class VideoPlayerActivity extends AppCompatActivity {

    private ActivityVideoPlayerBinding b;
    ExoPlayer player;
    private static final String TAG = "VideoPlayerActivity";

    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityVideoPlayerBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        String link = getIntent().getStringExtra(Constants.PARAMS);


        DefaultTrackSelector trackSelector = new DefaultTrackSelector(this);
        player =
                new ExoPlayer.Builder(this)
                        .setMediaSourceFactory(
                                new DefaultMediaSourceFactory(this).setLiveTargetOffsetMs(5000))
                        .setTrackSelector(trackSelector)
                        .build();

        MediaMetadata mediaMetadata = new MediaMetadata.Builder()
                .setTitle("").setDisplayTitle("")
                .build();

        MediaItem mediaItem =
                new MediaItem.Builder()
                        .setMediaMetadata(mediaMetadata)
                        .setUri(link)
                        .setLiveConfiguration(
                                new MediaItem.LiveConfiguration.Builder().setMaxPlaybackSpeed(1.02f).build())
                        .build();


        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                .build();
        player.setAudioAttributes(audioAttributes, true);
        player.setMediaItem(mediaItem);

        PlayerView playerView = b.playerView;
        playerView.setPlayer(player);
        playerView.setShowBuffering(SHOW_BUFFERING_WHEN_PLAYING);

        player.addListener(new Player.Listener() {

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Player.Listener.super.onPlaybackStateChanged(playbackState);
                Log.d(TAG, "onPlaybackStateChanged:  " + playbackState);
                if (playbackState == Player.STATE_READY) {
                    setAudioTrack(trackSelector);
                }
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                Player.Listener.super.onPlayerError(error);
                Toast.makeText(VideoPlayerActivity.this, error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        });

        player.prepare();

        player.play();

    }

    @OptIn(markerClass = UnstableApi.class)
    private void setAudioTrack(DefaultTrackSelector trackSelector) {
        MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
        if (mappedTrackInfo != null) {
            Log.d(TAG, "setAudioTrack: mappedTrackInfo");
            Log.d(TAG, "setAudioTrack: mappedTrackInfo.getRendererCount()  " + mappedTrackInfo.getRendererCount());
            for (int rendererIndex = 0; rendererIndex < mappedTrackInfo.getRendererCount(); rendererIndex++) {
                TrackGroupArray trackGroups = mappedTrackInfo.getTrackGroups(rendererIndex);
                Log.d(TAG, "trackGroups: " + trackGroups.length);
                for (int groupIndex = 0; groupIndex < trackGroups.length; groupIndex++) {
                    TrackGroup trackGroup = trackGroups.get(groupIndex);
                    for (int trackIndex = 0; trackIndex < trackGroup.length; trackIndex++) {
                        String mimeType = trackGroup.getFormat(trackIndex).sampleMimeType;
                        if (mimeType != null && MimeTypes.getTrackType(mimeType) == C.TRACK_TYPE_AUDIO) {
                            trackSelector.setParameters(
                                    trackSelector.buildUponParameters()
                                            .setRendererDisabled(rendererIndex, false)
                                            .build()
                            );
                        }
                    }
                }
            }
        } else Log.d(TAG, "MappedTrackInfo is null");
    }

    @Override
    protected void onStart() {
        super.onStart();
        player.setPlayWhenReady(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        player.setPlayWhenReady(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.release();
    }
}