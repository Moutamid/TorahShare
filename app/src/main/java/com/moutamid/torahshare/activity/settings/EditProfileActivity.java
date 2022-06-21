package com.moutamid.torahshare.activity.settings;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.moutamid.torahshare.databinding.ActivityEditProfileBinding;
import com.moutamid.torahshare.model.UserModel;
import com.moutamid.torahshare.utils.Constants;
import com.moutamid.torahshare.utils.Stash;

public class EditProfileActivity extends AppCompatActivity {

    private ActivityEditProfileBinding b;

    UserModel userModel = (UserModel) Stash.getObject(Constants.CURRENT_USER_MODEL, UserModel.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        b.nameEtSignUp.setText(userModel.name == null ? "" : userModel.name);
        b.numberEtSignUp.setText(userModel.number == null ? "" : userModel.number);
        b.emailEtSignUp.setText(userModel.email == null ? "" : userModel.email);
        b.bioEtSignUp.setText(userModel.bio == null ? "" : userModel.bio);

        b.updateBtn.setOnClickListener(view -> {

            String name = b.nameEtSignUp.getText().toString();
            String number = b.nameEtSignUp.getText().toString();
            String email = b.nameEtSignUp.getText().toString();
            String password = b.nameEtSignUp.getText().toString();

            if ()

        });

    }
}