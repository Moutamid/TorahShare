package com.moutamid.torahsharee.activity.addbutton;

import static com.moutamid.torahsharee.utils.Stash.toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.moutamid.torahsharee.R;
import com.moutamid.torahsharee.databinding.ActivityApproval2Binding;
import com.moutamid.torahsharee.model.ApprovalRequestModel;
import com.moutamid.torahsharee.model.UserModel;
import com.moutamid.torahsharee.utils.Constants;
import com.moutamid.torahsharee.utils.Stash;

public class ApprovalActivity2 extends AppCompatActivity {

    private ActivityApproval2Binding b;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityApproval2Binding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        if (Stash.getBoolean("isDone")) {
            b.screen1.setVisibility(View.GONE);
            b.screen6.setVisibility(View.VISIBLE);
            return;
        }

/*
        if (Stash.getBoolean("allowed")) {
            b.screen1.setVisibility(View.GONE);
            b.screen6.setVisibility(View.VISIBLE);

        }

*/
        b.getApprovedBtn.setOnClickListener(view -> {
            b.screen1.setVisibility(View.GONE);
            b.screen2.setVisibility(View.VISIBLE);

            initVideoPlayer();

        });

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
                b.emailTextView.setText(" " + userModel.email + "");
                Stash.put("isDone", true);
            }

        });

        b.video1placeholder.setOnClickListener(view -> {
            Intent i = new Intent();
            i.setType("video/*");
            i.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(i, "choose App"), PICK_VIDEO_1);
        });

        b.video2placeholder.setOnClickListener(view -> {
            Intent i = new Intent();
            i.setType("video/*");
            i.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(i, "choose App"), PICK_VIDEO_2);
        });

        if (Stash.getString(Constants.CURRENT_LANGUAGE, "en").equals("en")) {
            // ENGLISH
            b.jjjj.setImageResource(R.drawable.error_placeholder);
            b.jjjj.setBackgroundResource(R.drawable.error_placeholder);
        } else {
            //HEBREW
            b.jjjj.setImageResource(R.drawable.error_placeholder_iw);
            b.jjjj.setBackgroundResource(R.drawable.error_placeholder_iw);
        }


    }

    public void uploadVideo(Uri videoUri, int requestCode) {
        progressDialog = new ProgressDialog(ApprovalActivity2.this);
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
                        b.videoView1.pause();
                        b.videoView1.seekTo(1);
                    }
                    if (requestCode == PICK_VIDEO_2) {
                        VIDEO_LINK_2 = downloadLink;
                        videoCount++;

                        b.video2placeholder.setVisibility(View.GONE);
                        b.videoView2Layout.setVisibility(View.VISIBLE);

                        b.videoView2.setVideoURI(uri);
                        b.videoView2.pause();
                        b.videoView2.seekTo(1);
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

        b.backBtn.setOnClickListener(view -> {
            finish();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_VIDEO_1 || requestCode == PICK_VIDEO_2) {
                Uri videoUri = data.getData();
                uploadVideo(videoUri, requestCode);
            }
        }

    }
}