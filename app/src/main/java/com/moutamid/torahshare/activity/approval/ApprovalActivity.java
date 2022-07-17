package com.moutamid.torahshare.activity.approval;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moutamid.torahshare.databinding.ActivityApprovalBinding;
import com.moutamid.torahshare.utils.Constants;
import com.moutamid.torahshare.utils.Stash;

public class ApprovalActivity extends AppCompatActivity {

//    String link = "https://firebasestorage.googleapis.com/v0/b/sweet-nutrition.appspot.com/o/huitxbozybaymildmqwi.mp4?alt=media&token=e6900e45-6987-4a86-a913-8fa50e453aff";

    private ActivityApprovalBinding b;
    public ApprovalController approvalController;
    public CameraController cameraController;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityApprovalBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        Constants.databaseReference().child(Constants.USERS)
                .child(Constants.auth().getUid())
                .child(Constants.IS_APPROVED)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {

//                            b.screen1.setVisibility(View.GONE);
//                            b.screen2.setVisibility(View.GONE);
//                            b.screen3.setVisibility(View.GONE);
//                            b.screen4.setVisibility(View.VISIBLE);
//                            b.recordBtn.requestFocus();
//                            b.screen5.setVisibility(View.GONE);
//                            b.screen6.setVisibility(View.GONE);
                            cameraController = new CameraController(ApprovalActivity.this, b);
                        } else {
                            if (Stash.getBoolean("isDone")) {
                                Stash.put("allowed", true);
//                                b.screen1.setVisibility(View.GONE);
//                                b.screen6.setVisibility(View.VISIBLE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


        /*b.camera.addVideoRecordStoppedListener(isSuccess -> {
            if (isSuccess) {
                Toast.makeText(this, "Video recorded and saved!", Toast.LENGTH_SHORT).show();
            } else Toast.makeText(this, "Error in saving!", Toast.LENGTH_SHORT).show();

            return Unit.INSTANCE;
        });
*/


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {

        }
    }

}