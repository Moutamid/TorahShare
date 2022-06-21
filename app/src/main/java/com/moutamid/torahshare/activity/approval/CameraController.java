package com.moutamid.torahshare.activity.approval;

import static com.moutamid.torahshare.utils.Stash.toast;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.moutamid.torahshare.R;
import com.moutamid.torahshare.databinding.ActivityApprovalBinding;
import com.moutamid.torahshare.model.VideoModel;
import com.moutamid.torahshare.utils.Constants;
import com.priyankvasa.android.cameraviewex.Modes;
import com.priyankvasa.android.cameraviewex.VideoConfiguration;

import java.io.File;
import java.util.concurrent.TimeUnit;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import me.originqiu.library.EditTag;
import timerx.Stopwatch;
import timerx.StopwatchBuilder;

public class CameraController {

    public final String[] permissions = {Manifest.permission.CAMERA};
    public final String[] permissions2 = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.MANAGE_EXTERNAL_STORAGE};
    boolean recording = false;
    public File file;
    public Uri videoUri;

    public String THUMBNAIL_LINK = Constants.NULL;
    public Dialog thumbnailDialog;
    public Dialog captionDialog;

    public ProgressDialog progressDialog;
    public StorageReference storageRef = FirebaseStorage.getInstance().getReference();
    public String FINAL_VIDEO_LINK = Constants.NULL;

    public int THUMBNAIL_PICK = 3000;

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

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
        } else ActivityCompat.requestPermissions(activity, permissions2, 2);

        b.declineBtn.setOnClickListener(view -> {
            Toast.makeText(activity, "You need to accept terms to upload the video!", Toast.LENGTH_SHORT).show();
        });

        b.acceptBtn.setOnClickListener(view -> {
            b.termsDialog.setVisibility(View.GONE);
            b.shareBtn.setVisibility(View.VISIBLE);
        });

        Stopwatch stopwatch = new StopwatchBuilder()
                // Set the initial format
                .startFormat("MM:SS")
                // Set the tick listener for displaying time
                .onTick(time -> b.timestampTv.setText(time))
                // When time is equal to one hour, change format to "HH:MM:SS"
                .changeFormatWhen(1, TimeUnit.HOURS, "HH:MM:SS")
                .build();

        file = new File(getFilePathString());

        b.camerafacingBtn.setOnClickListener(view -> {
            b.camera.toggleFacing();
        });

        b.idid.setOnClickListener(view -> {
            toast("CLiked idid");
        });
        b.idid2.setOnClickListener(view -> {
            toast("CLiked idid2");
        });
        b.idid3.setOnClickListener(view -> {
            toast("CLiked idid3");
        });

        b.recordBtn.setOnClickListener(view -> {
            toast("Clicked");
            if (recording) {
//                b.camera.stopVideoRecording();
                b.recordBtn.setBackgroundResource(R.drawable.ic_record_btn);
                stopwatch.stop();
                b.timestampTv.setText("00:00");

                b.videoViewFinal.setVideoPath(file.getAbsolutePath());
                b.videoViewFinal.start();

                b.cameraLayout.setVisibility(View.GONE);
                b.previewLayout.setVisibility(View.VISIBLE);
                b.termsDialog.setVisibility(View.VISIBLE);

            }
            else {
                recording = true;
                b.recordBtn.setBackgroundResource(R.drawable.ic_pause_btn);
                stopwatch.start();
                /*b.camera.startVideoRecording(file, new Function1<VideoConfiguration, Unit>() {
                    @Override
                    public Unit invoke(VideoConfiguration videoConfiguration) {
                        videoConfiguration.setVideoFrameRate(30);
                        videoConfiguration.setVideoStabilization(true);
                        return Unit.INSTANCE;
                    }
                });*/

            }
        });

        setupCamera();

        b.shareBtn.setOnClickListener(view -> {
            uploadVideo(videoUri, file);
        });

    }

    private String getFilePathString() {
        String path_save_vid = "";

        if (Build.VERSION.SDK_INT >= 30) {//Build.VERSION_CODES.R
            path_save_vid =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) +
                            File.separator +
                            activity.getResources().getString(R.string.app_name) +
                            File.separator + "Videos";
        } else {
            path_save_vid =
                    Environment.getExternalStorageDirectory().getAbsolutePath() +
                            File.separator +
                            activity.getResources().getString(R.string.app_name) +
                            File.separator + "Videos";
        }
        return path_save_vid;
    }

    public void setupCamera() {
// enable only video capture mode
//        b.camera.setCameraMode(Modes.CameraMode.VIDEO_CAPTURE);

        /*activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                b.camera.setFacing(Modes.Facing.FACING_FRONT);
            }
        });
        b.camera.addVideoRecordStartedListener(new Function0<Unit>() {
            @Override
            public Unit invoke() {
                return Unit.INSTANCE;
            }
        });
        b.camera.addCameraOpenedListener(() -> {
//            Timber.i("Camera opened.");
            return Unit.INSTANCE;
        });

        *//*b.camera.addCameraErrorListener((Throwable t, ErrorLevel errorLevel) -> {
         *//**//*if (errorLevel instanceof ErrorLevel.Warning) Timber.w(t);
            else if (errorLevel instanceof ErrorLevel.Error) Timber.e(t);*//**//*
            return Unit.INSTANCE;
        });*//*

        b.camera.addCameraClosedListener(() -> {
//            Timber.i("Camera closed.");
            return Unit.INSTANCE;
        });*/

        b.galleryBtn.setOnClickListener(view -> {
            toast("CLicked gallery btn");
            Intent i = new Intent();
            i.setType("video/*");
            i.setAction(Intent.ACTION_GET_CONTENT);
            activity.startActivityForResult(Intent.createChooser(i, "choose App"), activity.CAMERA_PICK);
        });

        thumbnailDialog = new Dialog(activity);
        thumbnailDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        thumbnailDialog.setContentView(R.layout.dialog_upload_thumbnail);
        thumbnailDialog.setCancelable(true);

        thumbnailDialog.findViewById(R.id.thumbnail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // CODE HERE
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                activity.startActivityForResult(galleryIntent, THUMBNAIL_PICK);
            }
        });

        thumbnailDialog.findViewById(R.id.nextBtn).setOnClickListener(view -> {
            MaterialButton button = thumbnailDialog.findViewById(R.id.nextBtn);
            if (button.getAlpha() == 1) {
                thumbnailDialog.dismiss();
                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                layoutParams.copyFrom(captionDialog.getWindow().getAttributes());
                layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
                layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
                captionDialog.show();
                captionDialog.getWindow().setAttributes(layoutParams);
            }
        });

        thumbnailDialog.findViewById(R.id.skipBtn).setOnClickListener(view -> {
            thumbnailDialog.dismiss();
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(captionDialog.getWindow().getAttributes());
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            captionDialog.show();
            captionDialog.getWindow().setAttributes(layoutParams);
        });

        captionDialog = new Dialog(activity);
        captionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        captionDialog.setContentView(R.layout.dialog_caption_layout);
        captionDialog.setCancelable(false);

        EditText captionEt = captionDialog.findViewById(R.id.captionEt);
        EditTag tagEditText = captionDialog.findViewById(R.id.tagEditText);
        TextView tagsCountTv = captionDialog.findViewById(R.id.tagsCountTv);
        CheckBox followersCheckBox = captionDialog.findViewById(R.id.followersCheckBox);
        CheckBox contactsCheckBox = captionDialog.findViewById(R.id.contactsCheckBox);
        MaterialButton button = captionDialog.findViewById(R.id.nextBtn);

        tagEditText.setTagAddCallBack(new EditTag.TagAddCallback() {
            @Override
            public boolean onTagAdd(String tagValue) {
                tagsCountInt++;
                if (tagsCountInt == 11)
                    return true;

                tagsCountTv.setText(tagsCountInt + "/10");
                return false;
            }
        });

        tagEditText.setTagDeletedCallback(new EditTag.TagDeletedCallback() {
            @Override
            public void onTagDelete(String deletedTagValue) {
                tagsCountInt--;
                tagsCountTv.setText(tagsCountInt + "/10");
            }
        });

        button.setOnClickListener(view -> {
            if (captionEt.getText().toString().isEmpty()) {
                toast("Please add a caption!");
                return;
            }

            if (tagEditText.getTagList().isEmpty()) {
                toast("Please add some tags");
                return;
            }
            if (tagEditText.getTagList().size() > 10) {
                toast("Only 10 tags are allowed!");
                return;
            }

            VideoModel videoModel = new VideoModel();

            videoModel.videoUrl = FINAL_VIDEO_LINK;
            videoModel.thumbnailUrl = THUMBNAIL_LINK;
            videoModel.caption = captionEt.getText().toString();
            videoModel.tagsList = tagEditText.getTagList();
            videoModel.followers = followersCheckBox.isChecked();
            videoModel.contacts = contactsCheckBox.isChecked();
            progressDialog.show();
            Constants.databaseReference()
                    .child(Constants.PUBLIC_POSTS)
                    .push()
                    .setValue(videoModel)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            progressDialog.dismiss();
                            if (task.isSuccessful()) {
                                b.screen1.setVisibility(View.GONE);
                                b.screen2.setVisibility(View.GONE);
                                b.screen3.setVisibility(View.GONE);
                                b.screen4.setVisibility(View.GONE);
                                b.screen5.setVisibility(View.GONE);
                                b.screen6.setVisibility(View.GONE);
                                b.screen7.setVisibility(View.VISIBLE);
                            }
                        }
                    });

        });
    }

    int tagsCountInt = 0;

    public void uploadVideo(Uri videoUri, File file) {
        progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);

        progressDialog.show();

        UploadTask uploadTask;
        if (videoUri == null) {
            uploadTask = storageRef.child("/videos/" +
                    file.getAbsolutePath() + System.currentTimeMillis()).putFile(Uri.fromFile(file));
        } else {
            uploadTask = storageRef.child("/videos/" +
                    videoUri.getLastPathSegment() + System.currentTimeMillis()).putFile(videoUri);
        }

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
                FINAL_VIDEO_LINK = uriTask.getResult().toString();

                toast("Success");

                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                layoutParams.copyFrom(thumbnailDialog.getWindow().getAttributes());
                layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
                layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
                thumbnailDialog.show();
                thumbnailDialog.getWindow().setAttributes(layoutParams);
            }
        });
    }

    public void uploadImage(Uri imageUri) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();

        progressDialog.show();
        final StorageReference filePath = storageReference
                .child(Constants.auth().getUid() + imageUri.getLastPathSegment());

        filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri photoUrl) {
                        ImageView imageView = thumbnailDialog.findViewById(R.id.thumbnail);
                        imageView.setImageURI(imageUri);
                        THUMBNAIL_LINK = photoUrl.toString();
                        progressDialog.dismiss();

                        MaterialButton button = thumbnailDialog.findViewById(R.id.nextBtn);
                        button.setAlpha(1);
                    }
                });
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }
}
