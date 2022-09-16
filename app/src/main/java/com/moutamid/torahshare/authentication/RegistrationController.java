package com.moutamid.torahshare.authentication;

import static com.moutamid.torahshare.utils.Stash.toast;

import android.app.ProgressDialog;
import android.content.Intent;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moutamid.torahshare.R;
import com.moutamid.torahshare.databinding.ActivitySignUpBinding;
import com.moutamid.torahshare.model.UserModel;
import com.moutamid.torahshare.utils.Constants;
import com.moutamid.torahshare.utils.Stash;

import java.util.Locale;

public class RegistrationController {
    private static final String TAG = "RegistrationController";
    private RegistrationActivity activity;
    private ActivitySignUpBinding b;
    public ProgressDialog progressDialog;

    public RegistrationController(RegistrationActivity activity, ActivitySignUpBinding b) {
        this.activity = activity;
        this.b = b;
        progressDialog = new ProgressDialog(activity);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Please wait...");

    }

    public void weak() {
        b.passwordStrengthTextview.setText("Weak");
        b.passwordStrengthTextview.setTextColor(activity.getResources().getColor(R.color.red));

        b.passwordStrengthView1.setBackgroundColor(activity.getResources().getColor(R.color.red));
        b.passwordStrengthView2.setBackgroundColor(activity.getResources().getColor(R.color.darkGrey));
        b.passwordStrengthView3.setBackgroundColor(activity.getResources().getColor(R.color.darkGrey));
        b.passwordStrengthView4.setBackgroundColor(activity.getResources().getColor(R.color.darkGrey));

    }

    public void medium1() {
        b.passwordStrengthTextview.setText("Medium");
        b.passwordStrengthTextview.setTextColor(activity.getResources().getColor(R.color.yellow));

        b.passwordStrengthView1.setBackgroundColor(activity.getResources().getColor(R.color.yellow));
        b.passwordStrengthView2.setBackgroundColor(activity.getResources().getColor(R.color.yellow));
        b.passwordStrengthView3.setBackgroundColor(activity.getResources().getColor(R.color.darkGrey));
        b.passwordStrengthView4.setBackgroundColor(activity.getResources().getColor(R.color.darkGrey));

    }

    public void medium2() {
        b.passwordStrengthTextview.setText("Medium");
        b.passwordStrengthTextview.setTextColor(activity.getResources().getColor(R.color.yellow));

        b.passwordStrengthView1.setBackgroundColor(activity.getResources().getColor(R.color.yellow));
        b.passwordStrengthView2.setBackgroundColor(activity.getResources().getColor(R.color.yellow));
        b.passwordStrengthView3.setBackgroundColor(activity.getResources().getColor(R.color.yellow));
        b.passwordStrengthView4.setBackgroundColor(activity.getResources().getColor(R.color.darkGrey));

    }

    public void strong() {
        b.passwordStrengthTextview.setText("Strong");
        b.passwordStrengthTextview.setTextColor(activity.getResources().getColor(R.color.default_green));

        b.passwordStrengthView1.setBackgroundColor(activity.getResources().getColor(R.color.default_green));
        b.passwordStrengthView2.setBackgroundColor(activity.getResources().getColor(R.color.default_green));
        b.passwordStrengthView3.setBackgroundColor(activity.getResources().getColor(R.color.default_green));
        b.passwordStrengthView4.setBackgroundColor(activity.getResources().getColor(R.color.default_green));

    }

    public void loginUser(String emailStr, String passwordStr) {
        progressDialog.show();

        if (Constants.auth().getCurrentUser() != null) {
            Stash.clearAll();
            Constants.auth().signOut();
        }

        Constants.auth().signInWithEmailAndPassword(emailStr, passwordStr)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "onComplete: sign in completed");
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: susseccfull");
                            Stash.put(Constants.MY_PASSWORD, passwordStr);
                            Constants.databaseReference().child(Constants.USERS)
                                    .child(Constants.auth().getUid())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()){
                                                Log.d(TAG, "onDataChange: got user data");
                                                UserModel userModel = snapshot.getValue(UserModel.class);
                                                Stash.put(Constants.CURRENT_USER_MODEL, userModel);
                                                activity.onCompleteMethod();
                                            }else {
                                                Stash.clearAll();
                                                Constants.auth().signOut();
                                                Log.d(TAG, "onDataChange: no snapchot");
                                                progressDialog.dismiss();
                                                Toast.makeText(activity, "Data doesn't exist!", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            progressDialog.dismiss();
                                            Stash.clearAll();
                                            Constants.auth().signOut();
                                            Log.d(TAG, "onCancelled: cancelled error");
                                            Toast.makeText(activity, error.toException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Log.d(TAG, "onComplete: failed");
                            Stash.clearAll();
                            Constants.auth().signOut();
                            progressDialog.dismiss();
                            toast(task.getException().getMessage());
                        }
                    }
                });

    }


    public boolean checkEditTextLogin() {
        if (b.emailEtLogin.getText().toString().isEmpty()) {
            toast("Please enter email!");
            return true;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(b.emailEtLogin.getText().toString()).matches()) {
            toast("Email is invalid!");
            return true;
        }

        if (b.passwordEtLogin.getText().toString().isEmpty()) {
            toast("Please enter password!");
            return true;
        }

        return false;
    }

    public boolean checkEditTextSignUp() {
        if (b.nameEtSignUp.getText().toString().isEmpty()) {
            toast("Please enter name!");
            return true;
        }
        if (b.genderDefaultText.getText().toString().toLowerCase(Locale.ROOT).equals(Constants.GENDER_MALE) ||
                b.genderDefaultText.getText().toString().toLowerCase(Locale.ROOT).equals(Constants.GENDER_FEMALE)) {
        } else {
            toast("Please select your gender!");
            return true;
        }
        if (b.emailEtSignUp.getText().toString().isEmpty()) {
            toast("Please enter email!");
            return true;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(b.emailEtSignUp.getText().toString()).matches()) {
            toast("Email is invalid!");
            return true;
        }

        if (b.passwordEtSignUp.getText().toString().isEmpty()) {
            toast("Please enter password!");
            return true;
        }

        return false;
    }

    public void registerUser(String emailStr, String passwordStr) {
        progressDialog.show();

        Constants.auth().createUserWithEmailAndPassword(emailStr, passwordStr)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            activity.model.name = b.nameEtSignUp.getText().toString();
                            activity.model.email = b.emailEtSignUp.getText().toString();
                            activity.model.gender = b.genderDefaultText.getText().toString();

                            activity.model.is_approved = false;

                            activity.model.uid = Constants.auth().getUid();
                            activity.model.number = "000000000";
                            if (activity.model.gender.equals(Constants.GENDER_FEMALE)) {
                                activity.model.bio = Constants.NULL;
                            }else {
                                activity.model.bio = Constants.DEFAULT_BIO;
                            }
                            activity.model.profile_url = Constants.DEFAULT_PROFILE_URL;
                            activity.model.followers_count = 0;
                            activity.model.following_count = 0;

                            Stash.put(Constants.CURRENT_USER_MODEL, activity.model);
                            uploadUserInfoToDatabase();
                        } else {
                            Stash.clearAll();
                            Constants.auth().signOut();
                            progressDialog.dismiss();
                            toast(task.getException().getMessage());
                        }
                    }
                });
    }

    public void uploadUserInfoToDatabase() {
        Constants.databaseReference().child(Constants.USERS)
                .child(Constants.auth().getUid())
                .setValue(activity.model)
                .addOnCompleteListener(activity.onCompleteListener());
    }
}

