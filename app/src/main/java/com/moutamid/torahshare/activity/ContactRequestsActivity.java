package com.moutamid.torahshare.activity;

import static com.bumptech.glide.Glide.with;
import static com.bumptech.glide.load.engine.DiskCacheStrategy.DATA;
import static com.moutamid.torahshare.R.color.lighterGrey;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moutamid.torahshare.R;
import com.moutamid.torahshare.databinding.ActivityContactRequestsBinding;
import com.moutamid.torahshare.model.ContactRequestModel;
import com.moutamid.torahshare.model.UserModel;
import com.moutamid.torahshare.utils.Constants;
import com.moutamid.torahshare.utils.Stash;

public class ContactRequestsActivity extends AppCompatActivity {

    private ActivityContactRequestsBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityContactRequestsBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        b.backBtn.setOnClickListener(view -> {
            finish();
        });

        ContactRequestModel contactRequestModel = (ContactRequestModel) Stash.getObject(Constants.CURRENT_CONTACT_REQUEST, ContactRequestModel.class);

        b.message.setText(contactRequestModel.requester_mcg);

        Constants.databaseReference().child(Constants.USERS)
                .child(contactRequestModel.requester_uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){

                            UserModel userModel = snapshot.getValue(UserModel.class);

                            with(getApplicationContext())
                                    .asBitmap()
                                    .load(userModel.profile_url)
                                    .apply(new RequestOptions()
                                            .placeholder(lighterGrey)
                                            .error(lighterGrey)
                                    )
                                    .diskCacheStrategy(DATA)
                                    .into(b.profile);

                            b.nam

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
}