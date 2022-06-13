package com.moutamid.torahshare.activity.approval;

import android.Manifest;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.moutamid.torahshare.R;
import com.moutamid.torahshare.databinding.ActivityApprovalBinding;
import com.moutamid.torahshare.utils.Stash;
import com.priyankvasa.android.cameraviewex.ErrorLevel;
import com.priyankvasa.android.cameraviewex.Modes;

import java.util.concurrent.TimeUnit;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import timerx.Stopwatch;
import timerx.StopwatchBuilder;

public class CameraController {

    public final String[] permissions = {Manifest.permission.CAMERA};
    boolean recording = false;

    public ApprovalActivity activity;
    public ActivityApprovalBinding b;

    public CameraController(ApprovalActivity activity, ActivityApprovalBinding b) {
        this.activity = activity;
        this.b = b;
    }

    //  CAMERA METHODS BELOW
    public void onCreateStarted() {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        setupCamera();

        Stopwatch stopwatch = new StopwatchBuilder()
                // Set the initial format
                .startFormat("MM:SS")
                // Set the tick listener for displaying time
                .onTick(time -> b.timestampTv.setText(time))
                // When time is equal to one hour, change format to "HH:MM:SS"
                .changeFormatWhen(1, TimeUnit.HOURS, "HH:MM:SS")
                .build();

        b.recordBtn.setOnClickListener(view -> {
            if (recording) {
//                b.camera.stopVideoRecording();
                b.recordBtn.setBackgroundResource(R.drawable.ic_record_btn);
                stopwatch.stop();
                b.timestampTv.setText("00:00");
                b.screen4.setVisibility(View.GONE);
                b.screen5.setVisibility(View.VISIBLE);
                Stash.put("isDone", true);
            } else {
                recording = true;
                b.recordBtn.setBackgroundResource(R.drawable.ic_pause_btn);
                stopwatch.start();
                /*File file = new File("");
                b.camera.startVideoRecording(file, new Function1<VideoConfiguration, Unit>() {
                    @Override
                    public Unit invoke(VideoConfiguration videoConfiguration) {
                        videoConfiguration.setVideoFrameRate(30);
                        videoConfiguration.setVideoStabilization(true);
                        return Unit.INSTANCE;
                    }
                });*/
            }
        });
    }

    public void setupCamera() {
// enable only video capture mode
        b.camera.setCameraMode(Modes.CameraMode.VIDEO_CAPTURE);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                b.camera.setFacing(Modes.Facing.FACING_FRONT);
            }
        });
        /*b.camera.addVideoRecordStartedListener(new Function0<Unit>() {
            @Override
            public Unit invoke() {
                return Unit.INSTANCE;
            }
        });*/
        b.camera.addCameraOpenedListener(() -> {
//            Timber.i("Camera opened.");
            return Unit.INSTANCE;
        });

        /*b.camera.addCameraErrorListener((Throwable t, ErrorLevel errorLevel) -> {
         *//*if (errorLevel instanceof ErrorLevel.Warning) Timber.w(t);
            else if (errorLevel instanceof ErrorLevel.Error) Timber.e(t);*//*
            return Unit.INSTANCE;
        });*/

        b.camera.addCameraClosedListener(() -> {
//            Timber.i("Camera closed.");
            return Unit.INSTANCE;
        });
    }

}
