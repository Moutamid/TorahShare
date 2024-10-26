package com.moutamid.torahsharee.activity.settings;

import static com.moutamid.torahsharee.utils.Stash.toast;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CompoundButton;

import com.moutamid.torahsharee.R;
import com.moutamid.torahsharee.activity.HomeActivity;
import com.moutamid.torahsharee.databinding.ActivitySettingsBinding;
import com.moutamid.torahsharee.startup.SplashActivity;
import com.moutamid.torahsharee.utils.Constants;
import com.moutamid.torahsharee.utils.Stash;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {
    private ActivitySettingsBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        if (Stash.getString(Constants.CURRENT_LANGUAGE, "en").equals("en")) {
            // ENGLISH
            b.languageSwitch.setChecked(false);
        } else {
            //HEBREW
            b.languageSwitch.setChecked(true);
        }

        b.backBtn.setOnClickListener(view -> {
            if (Stash.getBoolean(Constants.LANGUAGE_CHANGE, false)) {
                Intent intent = new Intent(SettingsActivity.this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                finish();
                startActivity(intent);
            } else {
                finish();
            }
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
            startActivity(new Intent(SettingsActivity.this, ChangePasswordActivity.class));
        });

        b.noContactOptionCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    // ON
                    Constants.databaseReference().child(Constants.USERS)
                            .child(Constants.auth().getUid())
                            .child(Constants.IS_CONTACT_CHECKED)
                            .setValue(true);
                } else {
                    // OFF
                    Constants.databaseReference().child(Constants.USERS)
                            .child(Constants.auth().getUid())
                            .child(Constants.IS_CONTACT_CHECKED)
                            .setValue(false);
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

        b.languageSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    Stash.put(Constants.LANGUAGE_CHANGE, true);
                    Stash.put(Constants.CURRENT_LANGUAGE, "iw");
                    changeLanguage("iw");
                    Intent intent = new Intent(SettingsActivity.this, SettingsActivity.class);
                    finish();
                    startActivity(intent);
                } else {
                    Stash.put(Constants.LANGUAGE_CHANGE, true);
                    Stash.put(Constants.CURRENT_LANGUAGE, "en");
                    changeLanguage("en");
                    Intent intent = new Intent(SettingsActivity.this, SettingsActivity.class);
                    finish();
                    startActivity(intent);
                }
            }
        });

       /*TODO: Decibel
           double amplitudeDb = 1.87687;
        TextView maxValue = new TextView(SettingsActivity.this);

        double highestAmplitude = 0;

        if (amplitudeDb > highestAmplitude)
            highestAmplitude = amplitudeDb;


        // TO BE CALLED WHEN THE STOP BUTTON IS PRESSED
        maxValue.setText(highestAmplitude + "");    */

    }

/*
    boolean isChanged = false;
*/

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
    public void onBackPressed() {
        if (Stash.getBoolean(Constants.LANGUAGE_CHANGE, false)) {
            Intent intent = new Intent(SettingsActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            finish();
            startActivity(intent);
        } else {
            super.onBackPressed();
        }
    }
}