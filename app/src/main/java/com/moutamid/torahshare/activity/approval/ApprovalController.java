package com.moutamid.torahshare.activity.approval;

import static com.moutamid.torahshare.utils.Stash.toast;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.camerakit.CameraKit;
import com.camerakit.CameraKitView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.moutamid.torahshare.R;
import com.moutamid.torahshare.databinding.ActivityApprovalBinding;
import com.moutamid.torahshare.model.ApprovalRequestModel;
import com.moutamid.torahshare.model.UserModel;
import com.moutamid.torahshare.utils.Constants;
import com.moutamid.torahshare.utils.Stash;

public class ApprovalController {

    public ApprovalActivity activity;
    public ActivityApprovalBinding b;

    public ProgressDialog progressDialog;
    public StorageReference storageRef = FirebaseStorage.getInstance().getReference();

    public int PICK_VIDEO_1 = 10001;
    public int PICK_VIDEO_2 = 10002;

    public String VIDEO_LINK_1 = Constants.NULL;
    public String VIDEO_LINK_2 = Constants.NULL;

    public int videoCount = 0;

    public String educationStr;
    public String occupationStr;
    public String religionStr;

    public ApprovalController(ApprovalActivity activity, ActivityApprovalBinding b) {
        this.activity = activity;
        this.b = b;

        b.nextBtn.setOnClickListener(view -> {
            educationStr = b.educationEt.getText().toString();
            occupationStr = b.occupationEtSignUp.getText().toString();
            religionStr = b.religionEtSignUp.getText().toString();

            if (educationStr.isEmpty()) {
                toast("Please enter your education!");
                return;
            }
            if (occupationStr.isEmpty()) {
                toast("Please enter your occupation!");
                return;
            }
            if (religionStr.isEmpty()) {
                toast("Please enter your religion!");
                return;
            }

            b.screen2.setVisibility(View.GONE);
            b.screen3.setVisibility(View.VISIBLE);

        });

        b.applyBtn.setOnClickListener(view -> {
            if (b.applyBtn.getAlpha() == 1.0) {
                ApprovalRequestModel model = new ApprovalRequestModel();

                model.education = educationStr;
                model.occupation = occupationStr;
                model.religion = religionStr;
                model.video1 = VIDEO_LINK_1;
                model.video2 = VIDEO_LINK_2;
                model.uid = Constants.auth().getUid();

                Constants.databaseReference()
                        .child(Constants.ADMIN)
                        .child(Constants.VIDEO_APPROVAL_REQUEST)
                        .child(model.uid)
                        .setValue(model);

                b.screen3.setVisibility(View.GONE);
                b.screen5.setVisibility(View.VISIBLE);

                UserModel userModel = (UserModel) Stash.getObject(Constants.CURRENT_USER_MODEL, UserModel.class);
                b.emailTextView.setText(userModel.email);
                Stash.put("isDone", true);
            }

        });

        b.video1placeholder.setOnClickListener(view -> {
            toast("video1placeholder");
            Intent i = new Intent();
            i.setType("video/*");
            i.setAction(Intent.ACTION_GET_CONTENT);
            activity.startActivityForResult(Intent.createChooser(i, "choose App"), PICK_VIDEO_1);
        });

//        b.galleryBtn.setOnClickListener(view -> {
//          toast("galleryBtn");
//            Intent i = new Intent();
//            i.setType("video/*");
//            i.setAction(Intent.ACTION_GET_CONTENT);
//            activity.startActivityForResult(Intent.createChooser(i, "choose App"), activity.CAMERA_PICK);
//        });

        initVideoPlayer();

    }

    public void uploadVideo(Uri videoUri, int requestCode) {
        progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);

        if (videoUri != null) {
            progressDialog.show();
            UploadTask uploadTask = storageRef.child("/videos/" +
                    videoUri.getLastPathSegment() + System.currentTimeMillis()).putFile(videoUri);

            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                    progressDialog.setMessage("Uploaded " + (int) progress + "%");
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful()) ;

                    progressDialog.dismiss();
                    // get the link of video
                    String downloadLink = uriTask.getResult().toString();

                    Uri uri = Uri.parse(downloadLink);
                    if (requestCode == PICK_VIDEO_1) {
                        VIDEO_LINK_1 = downloadLink;
                        videoCount++;

                        b.video1placeholder.setVisibility(View.GONE);
                        b.videoView1Layout.setVisibility(View.VISIBLE);

                        b.videoView1.setVideoURI(uri);
                        b.videoView1.seekTo(100);
                        b.videoView1.start();
                    }
                    if (requestCode == PICK_VIDEO_2) {
                        VIDEO_LINK_2 = downloadLink;
                        videoCount++;

                        b.video2placeholder.setVisibility(View.GONE);
                        b.videoView2Layout.setVisibility(View.VISIBLE);

                        b.videoView2.setVideoURI(uri);
                        b.videoView2.start();
                    }

                    if (videoCount == 2) {
                        // BOTH VIDEO ARE UPLOADED
                        b.applyBtn.setAlpha(1);
                    }

                    toast("Success");
                }
            });
        } else {
            toast("upload failed!");
        }

    }

    boolean play1 = true;
    boolean play2 = true;
    boolean play3 = true;

    public void initVideoPlayer() {
        b.videoView1.setOnClickListener(view -> {
            if (b.videoPlayBtn1.getVisibility() == View.GONE) {
                b.videoPlayBtn1.setVisibility(View.VISIBLE);
                new Handler().postDelayed(() -> {
                    b.videoPlayBtn1.setVisibility(View.GONE);
                }, 3000);
            } else {
                b.videoPlayBtn1.setVisibility(View.GONE);
            }
        });

        b.videoPlayBtn1.setOnClickListener(view -> {
            if (b.videoView1.isPlaying()) {
                // IS PLAYING
                b.videoPlayBtn1.setImageResource(R.drawable.ic_play_btn);
                b.videoView1.pause();

            } else {
                // PAUSED OR NOT STARTED
                b.videoPlayBtn1.setImageResource(R.drawable.ic_pause_btn);
                new Handler().postDelayed(() -> {
                    b.videoPlayBtn1.setVisibility(View.GONE);
                }, 3000);

                b.videoView1.start();
            }
        });

        b.videoView2.setOnClickListener(view -> {
            if (b.videoPlayBtn2.getVisibility() == View.GONE) {
                b.videoPlayBtn2.setVisibility(View.VISIBLE);
                new Handler().postDelayed(() -> {
                    b.videoPlayBtn2.setVisibility(View.GONE);
                }, 3000);
            } else {
                b.videoPlayBtn2.setVisibility(View.GONE);
            }
        });

        b.videoPlayBtn2.setOnClickListener(view -> {
            if (b.videoView2.isPlaying()) {
                // IS PLAYING
                b.videoPlayBtn2.setImageResource(R.drawable.ic_play_btn);
                b.videoView2.pause();

            } else {
                // PAUSED OR NOT STARTED
                b.videoPlayBtn2.setImageResource(R.drawable.ic_pause_btn);
                new Handler().postDelayed(() -> {
                    b.videoPlayBtn2.setVisibility(View.GONE);
                }, 3000);

                b.videoView2.start();
            }
        });
        b.videoViewFinal.setOnClickListener(view -> {
            if (b.videoPlayBtnFinal.getVisibility() == View.GONE) {
                b.videoPlayBtnFinal.setVisibility(View.VISIBLE);
                new Handler().postDelayed(() -> {
                    b.videoPlayBtnFinal.setVisibility(View.GONE);
                }, 3000);
            } else {
                b.videoPlayBtnFinal.setVisibility(View.GONE);
            }
        });

        b.videoPlayBtnFinal.setOnClickListener(view -> {
            if (b.videoViewFinal.isPlaying()) {
                // IS PLAYING
                b.videoPlayBtnFinal.setImageResource(R.drawable.ic_play_btn);
                b.videoViewFinal.pause();

            } else {
                // PAUSED OR NOT STARTED
                b.videoPlayBtnFinal.setImageResource(R.drawable.ic_pause_btn);
                new Handler().postDelayed(() -> {
                    b.videoPlayBtnFinal.setVisibility(View.GONE);
                }, 3000);

                b.videoViewFinal.start();
            }
        });

        b.videoView1.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                if (play1) {
                    play1 = false;
                    mediaPlayer.pause();
                }
            }
        });

        b.videoView2.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                if (play2) {
                    play2 = false;
                    mediaPlayer.pause();
                }
            }
        });

        b.videoViewFinal.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                if (play3) {
                    b.topIcon.setImageResource(R.drawable.ic_baseline_delete_outline_24);
                    activity.isDeletable = true;
                    play3 = false;
                    mediaPlayer.pause();
                }
            }
        });

    }

}
