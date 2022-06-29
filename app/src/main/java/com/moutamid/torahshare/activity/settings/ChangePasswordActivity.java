package com.moutamid.torahshare.activity.settings;

import static com.moutamid.torahshare.utils.Stash.toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.moutamid.torahshare.R;
import com.moutamid.torahshare.databinding.ActivityChangePasswordBinding;
import com.moutamid.torahshare.utils.Constants;
import com.moutamid.torahshare.utils.Stash;

public class ChangePasswordActivity extends AppCompatActivity {

    private ActivityChangePasswordBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        b.saveBtn.setOnClickListener(view -> {

            String oldPassword = b.oldPasswordEtSignUp.getText().toString();
            String newPassword = b.newPasswordEtSignUp.getText().toString();
            String confirmPassword = b.confirmPasswordEtSignUp.getText().toString();

            if (oldPassword.isEmpty())
                return;

            String oldStoredPassword = Stash.getString(Constants.MY_PASSWORD, "xxx");
            if (!oldPassword.equals(oldStoredPassword)) {
                toast("Old Password does not match!");
                return;
            }

            if (newPassword.isEmpty())
                return;

            if (confirmPassword.isEmpty())
                return;

            if (!newPassword.equals(confirmPassword)) {
                toast("Password does not match!");
                return;
            }

            ProgressDialog progressDialog;
            progressDialog = new ProgressDialog(ChangePasswordActivity.this);
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Loading...");

            Constants.auth().getCurrentUser().updatePassword(newPassword)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            progressDialog.dismiss();
                            if (task.isSuccessful()) {
                                Stash.put(Constants.MY_PASSWORD, newPassword);
                                b.oldPasswordEtSignUp.setText("");
                                b.newPasswordEtSignUp.setText("");
                                b.confirmPasswordEtSignUp.setText("");
                                toast("Success");
                            } else {
                                toast(task.getException().getMessage());
                            }
                        }
                    });

        });

        b.newPasswordEtSignUp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().isEmpty()) {
                    b.passwordStrengthLayout.setVisibility(View.GONE);
                } else {
                    b.passwordStrengthLayout.setVisibility(View.VISIBLE);

                    if (charSequence.toString().length() < 5) {
                        weak();
                    } else {

                        medium1();
                        if (charSequence.toString().matches(".*\\d.*")) {
                            // contains a number

                            medium2();
                            if (charSequence.toString().length() > 15) {
                                strong();
                            }
                        }

                    }

                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    public void weak() {
        b.passwordStrengthTextview.setText("Weak");
        b.passwordStrengthTextview.setTextColor(getResources().getColor(R.color.red));

        b.passwordStrengthView1.setBackgroundColor(getResources().getColor(R.color.red));
        b.passwordStrengthView2.setBackgroundColor(getResources().getColor(R.color.darkGrey));
        b.passwordStrengthView3.setBackgroundColor(getResources().getColor(R.color.darkGrey));
        b.passwordStrengthView4.setBackgroundColor(getResources().getColor(R.color.darkGrey));

    }

    public void medium1() {
        b.passwordStrengthTextview.setText("Medium");
        b.passwordStrengthTextview.setTextColor(getResources().getColor(R.color.yellow));

        b.passwordStrengthView1.setBackgroundColor(getResources().getColor(R.color.yellow));
        b.passwordStrengthView2.setBackgroundColor(getResources().getColor(R.color.yellow));
        b.passwordStrengthView3.setBackgroundColor(getResources().getColor(R.color.darkGrey));
        b.passwordStrengthView4.setBackgroundColor(getResources().getColor(R.color.darkGrey));

    }

    public void medium2() {
        b.passwordStrengthTextview.setText("Medium");
        b.passwordStrengthTextview.setTextColor(getResources().getColor(R.color.yellow));

        b.passwordStrengthView1.setBackgroundColor(getResources().getColor(R.color.yellow));
        b.passwordStrengthView2.setBackgroundColor(getResources().getColor(R.color.yellow));
        b.passwordStrengthView3.setBackgroundColor(getResources().getColor(R.color.yellow));
        b.passwordStrengthView4.setBackgroundColor(getResources().getColor(R.color.darkGrey));

    }

    public void strong() {
        b.passwordStrengthTextview.setText("Strong");
        b.passwordStrengthTextview.setTextColor(getResources().getColor(R.color.default_green));

        b.passwordStrengthView1.setBackgroundColor(getResources().getColor(R.color.default_green));
        b.passwordStrengthView2.setBackgroundColor(getResources().getColor(R.color.default_green));
        b.passwordStrengthView3.setBackgroundColor(getResources().getColor(R.color.default_green));
        b.passwordStrengthView4.setBackgroundColor(getResources().getColor(R.color.default_green));

    }

}