package com.moutamid.torahshare.activity.settings;

import static com.moutamid.torahshare.utils.Stash.toast;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

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

        if (userModel.gender.equals(Constants.GENDER_FEMALE)){
            b.bioEtSignUp.setVisibility(View.GONE);
            b.bioTitle.setVisibility(View.GONE);
        }

        b.nameEtSignUp.setText(userModel.name == null ? "" : userModel.name);

        b.numberEtSignUp.setText(userModel.number == null ? "" : userModel.number);

        b.emailEtSignUp.setText(userModel.email == null ? "" : userModel.email);

        b.bioEtSignUp.setText(userModel.bio == null ? "" : userModel.bio);

        b.updateBtn.setOnClickListener(view -> {
            String name = b.nameEtSignUp.getText().toString();
            String number = b.numberEtSignUp.getText().toString();
            String email = b.emailEtSignUp.getText().toString();
            String bio = b.bioEtSignUp.getText().toString();

            if (name.isEmpty()){
                toast("Name is empty!");
                return;
            }
            if (number.isEmpty()){
                toast("Number is empty!");
                return;
            }

            if (email.isEmpty()){
                toast("Email is empty!");
                return;
            }
            if (bio.isEmpty()){
                toast("Bio is empty!");
                return;
            }

            Constants.databaseReference().child(Constants.USERS)
                    .child(Constants.auth().getUid())
                    .child("name")
                    .setValue(name);
            Constants.databaseReference().child(Constants.USERS)
                    .child(Constants.auth().getUid())
                    .child("number")
                    .setValue(number);
            Constants.databaseReference().child(Constants.USERS)
                    .child(Constants.auth().getUid())
                    .child("email")
                    .setValue(email);
            Constants.databaseReference().child(Constants.USERS)
                    .child(Constants.auth().getUid())
                    .child("bio")
                    .setValue(bio);

            userModel.name = name;
            userModel.number = number;
            userModel.email = email;
            userModel.bio = bio;

            Stash.put(Constants.CURRENT_USER_MODEL, userModel);

            toast("Success");

        });

    }
}