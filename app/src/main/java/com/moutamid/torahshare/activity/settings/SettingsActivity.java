package com.moutamid.torahshare.activity.settings;

import static com.moutamid.torahshare.utils.Stash.toast;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CompoundButton;

import com.moutamid.torahshare.R;
import com.moutamid.torahshare.databinding.ActivitySettingsBinding;
import com.moutamid.torahshare.startup.SplashActivity;
import com.moutamid.torahshare.utils.Constants;
import com.moutamid.torahshare.utils.Stash;

public class SettingsActivity extends AppCompatActivity {
    private ActivitySettingsBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        b.backBtn.setOnClickListener(view -> {
            finish();
        });

        b.editProfileBtn.setOnClickListener(view -> {
            startActivity(new Intent(SettingsActivity.this, EditProfileActivity.class));
        });

        b.termsBtn.setOnClickListener(view -> {
            startActivity(new Intent(SettingsActivity.this, TermsActivity.class));
        });

        b.privacyPolicyBtn.setOnClickListener(view -> {
            toast("Coming soon!");
        });

        b.disclaimerBtn.setOnClickListener(view -> {
            toast("Coming soon!");
        });

        b.changePasswordBtn.setOnClickListener(view -> {
            startActivity(new Intent(SettingsActivity.this, TermsActivity.class));
        });

        b.noContactOptionCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    // FEMALE
                    Constants.databaseReference().child(Constants.USERS)
                            .child(Constants.auth().getUid())
                            .child("gender")
                            .setValue(Constants.GENDER_FEMALE);
                }else {
                    // MALE
                    Constants.databaseReference().child(Constants.USERS)
                            .child(Constants.auth().getUid())
                            .child("gender")
                            .setValue(Constants.GENDER_MALE);
                }
            }
        });

        b.logoutBtn.setOnClickListener(view -> {
            Constants.auth().signOut();
            Stash.clearAll();
            Intent intent = new Intent(SettingsActivity.this, SplashActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            finish();
            startActivity(intent);
        });

    }
}