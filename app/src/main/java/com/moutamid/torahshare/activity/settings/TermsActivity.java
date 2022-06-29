package com.moutamid.torahshare.activity.settings;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.moutamid.torahshare.databinding.ActivityTermsBinding;

public class TermsActivity extends AppCompatActivity {

    private ActivityTermsBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityTermsBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        b.backBtn.setOnClickListener(view -> {
            finish();
        });
    }
}