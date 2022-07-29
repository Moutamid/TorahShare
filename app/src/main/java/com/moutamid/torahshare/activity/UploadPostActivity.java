package com.moutamid.torahshare.activity;

import static com.moutamid.torahshare.utils.Stash.toast;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.camera.core.VideoCapture;
import androidx.camera.core.VideoCaptureConfig;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.flexbox.FlexboxLayout;
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
import com.moutamid.torahshare.databinding.ActivityUploadPostBinding;
import com.moutamid.torahshare.model.ChatModel;
import com.moutamid.torahshare.model.PostModel;
import com.moutamid.torahshare.model.UserModel;
import com.moutamid.torahshare.model.VideoModel;
import com.moutamid.torahshare.utils.Constants;
import com.moutamid.torahshare.utils.Stash;
import com.priyankvasa.android.cameraviewex.AudioEncoder;
import com.priyankvasa.android.cameraviewex.ErrorLevel;
import com.priyankvasa.android.cameraviewex.Modes;
import com.priyankvasa.android.cameraviewex.VideoConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import me.originqiu.library.EditTag;
import timerx.Stopwatch;
import timerx.StopwatchBuilder;

// 239, 445, 508
@SuppressLint("RestrictedApi")
public class UploadPostActivity extends AppCompatActivity implements LifecycleOwner {

    private UserModel myUserModel = (UserModel) Stash.getObject(Constants.CURRENT_USER_MODEL, UserModel.class);

    private ActivityUploadPostBinding b;

    boolean recording = false;
    public Uri videoUri;

    public String THUMBNAIL_LINK = Constants.NULL;
    public Dialog thumbnailDialog;
    public Dialog captionDialog;

    public ProgressDialog progressDialog;
    public StorageReference storageRef = FirebaseStorage.getInstance().getReference();
    public String FINAL_VIDEO_LINK = Constants.NULL;

    public int THUMBNAIL_PICK = 3000;
    int CAMERA_PICK = 2000;
    public boolean isDeletable = false;
    FlexboxLayout container;
    //CAMERA CODE
    public static final int REQUEST_CODE_PERMISSIONS = 10;
    public final String[] REQUIRED_PERMISSIONS = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};

    private VideoCapture videoCapture;

    public File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityUploadPostBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        b.topIcon.setOnClickListener(view -> {
//            if (isDeletable) {
//                b.previewLayout.setVisibility(View.GONE);
//                b.cameraLayout.setVisibility(View.VISIBLE);
//                b.topIcon.setImageResource(R.drawable.ic_baseline_close_24);
//            } else {
            finish();
//            }

        });

        // Find the last picture
        String[] projection = new String[]{
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Images.ImageColumns.DATE_TAKEN,
                MediaStore.Images.ImageColumns.MIME_TYPE
        };
        final Cursor cursor = getContentResolver()
                .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null,
                        null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");

        // Put it in the image view
        if (cursor.moveToFirst()) {
//            final ImageView imageView = (ImageView) findViewById(R.id.pictureView);
            String imageLocation = cursor.getString(1);
            File imageFile = new File(imageLocation);
            if (imageFile.exists()) {   // TODO: is there a better way to do this?
                Bitmap bm = BitmapFactory.decodeFile(imageLocation);
                b.galleryBtn.setImageBitmap(bm);
            }
        }

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

        b.videoViewFinal.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
//                b.topIcon.setImageResource(R.drawable.ic_baseline_delete_outline_24);
//                isDeletable = true;
                b.topIcon.setVisibility(View.GONE);
                b.startTopText.setVisibility(View.GONE);
                b.videoViewFinal.start();

//                mediaPlayer.pause();
//                b.videoViewFinal.seekTo(100);
                b.videoViewFinal.pause();
            }
        });

        onCreateStarted();
    }

    //  CAMERA METHODS BELOW
    public void onCreateStarted() {
        file = new File(getExternalMediaDirs()[0], System.currentTimeMillis() + ".mp4");

        // Request camera permissions
        if (allPermissionsGranted()) {
            b.viewFinder.post(() -> {
                startCamera(CameraX.LensFacing.FRONT);
            });
        } else {
            ActivityCompat.requestPermissions(
                    this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        b.declineBtn.setOnClickListener(view -> {
            Toast.makeText(UploadPostActivity.this, "You need to accept terms to upload the video!", Toast.LENGTH_SHORT).show();
        });

        b.acceptBtn.setOnClickListener(view -> {
            b.termsDialog.setVisibility(View.GONE);
            b.shareBtn.setVisibility(View.VISIBLE);
        });

        Stopwatch stopwatch = new StopwatchBuilder().startFormat("MM:SS").onTick(time -> b.timestampTv.setText(time))
                .changeFormatWhen(1, TimeUnit.HOURS, "HH:MM:SS").build();

        b.camerafacingBtn.setOnClickListener(view -> {
            CameraX.unbindAll();
            startCamera(CameraX.LensFacing.BACK);
        });

        b.recordBtn.setOnClickListener(view -> {
            if (recording) {
                b.recordBtn.setImageResource(R.drawable.ic_record_btn);
                stopwatch.stop();
                b.timestampTv.setText("00:00");
                videoCapture.stopRecording();

                b.cameraLayout.setVisibility(View.GONE);
                b.previewLayout.setVisibility(View.VISIBLE);
                b.termsDialog.setVisibility(View.VISIBLE);
            } else {
                recording = true;
                b.recordBtn.setImageResource(R.drawable.ic_pause_btn);
                stopwatch.start();

                videoCapture.startRecording(file, new VideoCapture.OnVideoSavedListener() {
                    @Override
                    public void onVideoSaved(File file) {
                        uploadVideo(videoUri, file);
                    }

                    @Override
                    public void onError(VideoCapture.UseCaseError useCaseError, String message, @Nullable Throwable cause) {
                        Toast.makeText(UploadPostActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
        setupDialogs();
        b.shareBtn.setOnClickListener(view -> {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(thumbnailDialog.getWindow().getAttributes());
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            thumbnailDialog.show();
            thumbnailDialog.getWindow().setAttributes(layoutParams);
        });

    }

    private void startCamera(CameraX.LensFacing lensFacing) {
        // Create configuration object for the viewfinder use case
        PreviewConfig previewConfig = new PreviewConfig.Builder()
                .setLensFacing(lensFacing)
                .build();

        // Build the viewfinder use case
        Preview preview = new Preview(previewConfig);

        VideoCaptureConfig videoCaptureConfig = new VideoCaptureConfig
                .Builder()
                .setTargetRotation(b.viewFinder.getDisplay().getRotation())
                .setLensFacing(lensFacing)
                .build();

        videoCapture = new VideoCapture(videoCaptureConfig);

        preview.setOnPreviewOutputUpdateListener(new Preview.OnPreviewOutputUpdateListener() {
            @Override
            public void onUpdated(Preview.PreviewOutput output) {
                ViewGroup parent = (ViewGroup) b.viewFinder.getParent();
                parent.removeView(b.viewFinder);
                parent.addView(b.viewFinder, 0);
                SurfaceTexture surfaceTexture = output.getSurfaceTexture();
                b.viewFinder.setSurfaceTexture(surfaceTexture);
            }
        });

        // Bind use cases to lifecycle
        CameraX.bindToLifecycle(UploadPostActivity.this, preview, videoCapture);
    }

    private boolean allPermissionsGranted() {
        if (ActivityCompat.checkSelfPermission(UploadPostActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }

        if (ActivityCompat.checkSelfPermission(UploadPostActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }

        return true;
    }

    MaterialButton next_button;
    ImageView imageView;

    public void setupDialogs() {
        b.galleryBtn.setOnClickListener(view -> {
            Intent i = new Intent();
            i.setType("video/*");
            i.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(i, "choose App"), CAMERA_PICK);
        });

        thumbnailDialog = new Dialog(UploadPostActivity.this);
        thumbnailDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        thumbnailDialog.setContentView(R.layout.dialog_upload_thumbnail);
        thumbnailDialog.setCancelable(true);

        thumbnailDialog.findViewById(R.id.thumbnail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // CODE HERE
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, THUMBNAIL_PICK);
            }
        });


        next_button = thumbnailDialog.findViewById(R.id.nextBtn);
        imageView = thumbnailDialog.findViewById(R.id.thumbnail);

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

        captionDialog = new Dialog(UploadPostActivity.this);
        captionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        captionDialog.setContentView(R.layout.dialog_caption_layout);
        captionDialog.setCancelable(false);

        EditText captionEt = captionDialog.findViewById(R.id.captionEt);
        EditTag tagEditText = captionDialog.findViewById(R.id.tagEditText);
        TextView tagsCountTv = captionDialog.findViewById(R.id.tagsCountTv);
        CheckBox followersCheckBox = captionDialog.findViewById(R.id.followersCheckBox);
        CheckBox contactsCheckBox = captionDialog.findViewById(R.id.contactsCheckBox);
        MaterialButton button = captionDialog.findViewById(R.id.nextBtn);

        MaterialButton chooseContactBtn = captionDialog.findViewById(R.id.chooseContactBtn);
        container = captionDialog.findViewById(R.id.containerFlexBox);

        getChosenContactsList();
        contactsCheckBox.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                chooseContactBtn.setVisibility(View.VISIBLE);
                container.setVisibility(View.VISIBLE);
            } else {
                chooseContactBtn.setVisibility(View.GONE);
                container.setVisibility(View.GONE);
            }
        });

        chooseContactBtn.setOnClickListener(view2 -> {
            startActivity(new Intent(UploadPostActivity.this, ChooseContactsActivity.class));
        });

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

            /*if (tagEditText.getTagList().isEmpty()) {
//                toast("Please add some tags");
                tagEditText.addTag("Lorem");
                tagEditText.addTag("Ipsum");
                return;
            }*/

            if (tagEditText.getTagList().size() > 10) {
                toast("Only 10 tags are allowed!");
                return;
            }

            if (!contactsCheckBox.isChecked() || !followersCheckBox.isChecked())
                return;

            PostModel postModel = new PostModel();
            postModel.name = myUserModel.name;
            postModel.date = Stash.getDate();
            postModel.caption = captionEt.getText().toString();
            postModel.share_count = 0;
            postModel.comment_count = 0;
            postModel.tagsList = tagEditText.getTagList();
            postModel.thumbnailUrl = THUMBNAIL_LINK;
            postModel.video_link = FINAL_VIDEO_LINK;
            postModel.profile_link = myUserModel.profile_url;
            postModel.my_uid = Constants.auth().getUid();
            postModel.push_key = Constants.databaseReference().child(Constants.PUBLIC_POSTS).push().getKey();

            Stash.put(Constants.CURRENT_POST_MODEL, postModel);
            startActivity(new Intent(UploadPostActivity.this, UploadPostPreviewActivity.class)
                    .putExtra(Constants.IS_CONTACT_CHECKED, contactsCheckBox.isChecked())
                    .putExtra(Constants.IS_FOLLOWER_CHECKED, followersCheckBox.isChecked())
            );

        });
    }

    private void getChosenContactsList() {
        if (container == null)
            return;
        LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonLayoutParams.setMargins(5, 5, 5, 5);
        ArrayList<ChatModel> list_text = Stash.getArrayList(Constants.CHOSEN_CONTACTS_LIST, ChatModel.class);
        for (int i = 0; i < list_text.size(); i++) {
            final TextView tv = new TextView(getApplicationContext());
            tv.setText(list_text.get(i).other_name);
            tv.setHeight(100);
            tv.setTextSize(16.0f);
            tv.setGravity(Gravity.CENTER);
            tv.setTextColor(getResources().getColor(R.color.default_text_black));//Color.parseColor("#000000")
            tv.setBackground(getResources().getDrawable(R.drawable.rounded_text_view));
            tv.setId(i + 1);
            tv.setLayoutParams(buttonLayoutParams);
            tv.setTag(i + 1);
            tv.setPadding(20, 10, 20, 10);
            container.addView(tv);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getChosenContactsList();
    }

    int tagsCountInt = 0;

    public void uploadVideo(Uri videoUri, File file) {
        progressDialog = new ProgressDialog(UploadPostActivity.this);
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

//                Uri uri = Uri.parse(FINAL_VIDEO_LINK);
//                b.videoViewFinal.setVideoURI(uri);

                if (videoUri == null) {
                    b.videoViewFinal.setVideoURI(Uri.fromFile(file));
                } else {
                    b.videoViewFinal.setVideoURI(videoUri);
                }
            }
        });
    }

    public void uploadImage(Uri imageUri) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();

        ProgressDialog progressDialog2;
        progressDialog2 = new ProgressDialog(UploadPostActivity.this);
        progressDialog2.setCancelable(false);
        progressDialog2.setMessage("Loading...");
        progressDialog2.show();

        final StorageReference filePath = storageReference
                .child(Constants.auth().getUid() + imageUri.getLastPathSegment());

        filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri photoUrl) {
                        imageView.setImageURI(imageUri);
                        THUMBNAIL_LINK = photoUrl.toString();
                        progressDialog2.dismiss();

                        next_button.setAlpha(1);
                    }
                });
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(UploadPostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == THUMBNAIL_PICK) {
                Uri imageUri = data.getData();
                uploadImage(imageUri);
            }

            if (requestCode == CAMERA_PICK) {
                videoUri = data.getData();

                b.videoViewFinal.setVideoURI(videoUri);

                b.cameraLayout.setVisibility(View.GONE);
                b.previewLayout.setVisibility(View.VISIBLE);
                b.termsDialog.setVisibility(View.VISIBLE);

                uploadVideo(videoUri, file);
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                b.viewFinder.post(() -> {
                    startCamera(CameraX.LensFacing.FRONT);
                });
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}