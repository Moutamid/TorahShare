package com.moutamid.torahshare.startup;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.moutamid.torahshare.activity.HomeActivity;
import com.moutamid.torahshare.authentication.RegistrationActivity;
import com.moutamid.torahshare.utils.Constants;
import com.moutamid.torahshare.utils.Stash;

import java.util.Locale;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";

    public void changeLanguage(String lang) {
        Locale myLocale;
        if (lang.equalsIgnoreCase(""))
            return;
        myLocale = new Locale(lang);
        Locale.setDefault(myLocale);
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.locale = myLocale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        changeLanguage(Stash.getString(Constants.CURRENT_LANGUAGE, "en"));
        if (Stash.getBoolean(Constants.IS_LOGGED_IN, false)) {
            startActivity(new Intent(this, HomeActivity.class));
        } else {
            startActivity(new Intent(this, RegistrationActivity.class));
        }

    }
}
/*I have added the code for diary items to edit or delete but for some reason it isn't working on my mobiles. I was searching all over the internet but there's nothing i could found to edit those items. Check out if it works on your device or not because i had tested on multiple devices.
Don't know what to do. I'll research tomorrow again if i found an*/