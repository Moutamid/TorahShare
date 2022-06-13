package com.moutamid.torahshare.startup;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.moutamid.torahshare.activity.HomeActivity;
import com.moutamid.torahshare.authentication.RegistrationActivity;
import com.moutamid.torahshare.utils.Constants;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        if (Constants.auth().getCurrentUser() == null) {
            startActivity(new Intent(this, RegistrationActivity.class));
        } else {
            startActivity(new Intent(this, HomeActivity.class));
        }

    }
}
