package com.moutamid.torahsharee.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.moutamid.torahsharee.R;

public class WomanApprovalActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_woman_approval);

        findViewById(R.id.okayBtn).setOnClickListener(view -> {
            finish();
        });

    }
}