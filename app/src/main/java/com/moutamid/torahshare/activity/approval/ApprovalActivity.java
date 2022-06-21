package com.moutamid.torahshare.activity.approval;

import static com.moutamid.torahshare.utils.Stash.toast;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.moutamid.torahshare.R;
import com.moutamid.torahshare.databinding.ActivityApprovalBinding;
import com.moutamid.torahshare.utils.Constants;
import com.moutamid.torahshare.utils.Stash;
import com.priyankvasa.android.cameraviewex.ErrorLevel;
import com.priyankvasa.android.cameraviewex.Modes;

import java.util.concurrent.TimeUnit;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import timerx.Stopwatch;
import timerx.StopwatchBuilder;

public class ApprovalActivity extends AppCompatActivity {

//    String link = "https://firebasestorage.googleapis.com/v0/b/sweet-nutrition.appspot.com/o/huitxbozybaymildmqwi.mp4?alt=media&token=e6900e45-6987-4a86-a913-8fa50e453aff";

    private ActivityApprovalBinding b;
    public ApprovalController approvalController;
    public CameraController cameraController;
    public boolean isDeletable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityApprovalBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        if (Stash.getBoolean("allowed")) {
            b.screen1.setVisibility(View.GONE);
            b.screen6.setVisibility(View.VISIBLE);
        }

        Constants.databaseReference().child(Constants.USERS)
                .child(Constants.auth().getUid())
                .child(Constants.IS_APPROVED)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            if (ActivityCompat.checkSelfPermission(ApprovalActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            } else
                                ActivityCompat.requestPermissions(ApprovalActivity.this, cameraController.permissions, 1);
                            b.screen1.setVisibility(View.GONE);
                            b.screen2.setVisibility(View.GONE);
                            b.screen3.setVisibility(View.GONE);
                            b.screen4.setVisibility(View.VISIBLE);
//                            b.recordBtn.requestFocus();
                            b.screen5.setVisibility(View.GONE);
                            b.screen6.setVisibility(View.GONE);
                            cameraController = new CameraController(ApprovalActivity.this, b);
                        } else {
                            if (Stash.getBoolean("isDone")) {
                                Stash.put("allowed", true);
                                b.screen1.setVisibility(View.GONE);
                                b.screen6.setVisibility(View.VISIBLE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        b.getApprovedBtn.setOnClickListener(view -> {
            b.screen1.setVisibility(View.GONE);
            b.screen2.setVisibility(View.VISIBLE);
            approvalController = new ApprovalController(this, b);
        });

        /*b.camera.addVideoRecordStoppedListener(isSuccess -> {
            if (isSuccess) {
                Toast.makeText(this, "Video recorded and saved!", Toast.LENGTH_SHORT).show();
            } else Toast.makeText(this, "Error in saving!", Toast.LENGTH_SHORT).show();

            return Unit.INSTANCE;
        });
*/
        b.backBtn.setOnClickListener(view -> {
            finish();
        });

        b.topIcon.setOnClickListener(view -> {
            if (isDeletable) {
//                b.previewLayout.setVisibility(View.GONE);
                b.cameraLayout.setVisibility(View.VISIBLE);
                b.topIcon.setImageResource(R.drawable.ic_baseline_close_24);
                approvalController.play1 = true;
                approvalController.play2 = true;
                approvalController.play3 = true;
            } else {
                finish();
            }

        });


    }

    int CAMERA_PICK = 2000;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == cameraController.THUMBNAIL_PICK) {
                Uri imageUri = data.getData();
                cameraController.uploadImage(imageUri);
            }
            if (requestCode == approvalController.PICK_VIDEO_1 || requestCode == approvalController.PICK_VIDEO_2) {
                Uri videoUri = data.getData();
                approvalController.uploadVideo(videoUri, requestCode);
            }
            if (requestCode == CAMERA_PICK) {
                cameraController.videoUri = data.getData();

                b.videoViewFinal.setVideoURI(cameraController.videoUri);
                b.videoViewFinal.start();

                b.cameraLayout.setVisibility(View.GONE);
                b.previewLayout.setVisibility(View.VISIBLE);
                b.termsDialog.setVisibility(View.VISIBLE);

//                cameraController.uploadVideo(videoUri);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        b.camera.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            if (b.camera != null) b.camera.start();
        }*/
        b.camera.onResume();
    }

    @Override
    protected void onPause() {
        b.camera.onPause();
//        if (b.camera != null) b.camera.stop();
        super.onPause();
    }

    @Override
    protected void onStop() {
        b.camera.onStop();
        super.onStop();
    }

    /*@Override
    protected void onDestroy() {
        if (b.camera != null) b.camera.destroy();
        super.onDestroy();
    }*/

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        b.camera.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            cameraController.onCreateStarted();
        } else ActivityCompat.requestPermissions(this, permissions, 1);
    }

}