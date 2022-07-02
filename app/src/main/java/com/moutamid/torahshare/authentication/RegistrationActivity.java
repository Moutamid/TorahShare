package com.moutamid.torahshare.authentication;

import static com.moutamid.torahshare.utils.Stash.toast;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moutamid.torahshare.R;
import com.moutamid.torahshare.activity.HomeActivity;
import com.moutamid.torahshare.databinding.ActivitySignUpBinding;
import com.moutamid.torahshare.model.UserModel;
import com.moutamid.torahshare.utils.Constants;
import com.moutamid.torahshare.utils.Stash;

public class RegistrationActivity extends AppCompatActivity {

    private ActivitySignUpBinding b;

    private RegistrationController controller;
    private static final String TAG = "RegistrationActivity";

    private static final String USER_INFO = "userinfo";
    private static final int RC_SIGN_IN = 9001;

    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        controller = new RegistrationController(this, b);

        b.genderLayout.setOnClickListener(view -> {
            if (b.genderMaleTextview.getVisibility() == View.GONE) {

                b.genderArrow.setRotation(180);

                b.genderMaleTextview.setVisibility(View.VISIBLE);
                b.genderFemaleTextview.setVisibility(View.VISIBLE);
            } else {
                b.genderArrow.setRotation(0);

                b.genderMaleTextview.setVisibility(View.GONE);
                b.genderFemaleTextview.setVisibility(View.GONE);
            }
        });

        b.genderMaleTextview.setOnClickListener(view -> {
            b.genderDefaultText.setText(Constants.GENDER_MALE);

            b.genderArrow.setRotation(0);

            b.genderMaleTextview.setVisibility(View.GONE);
            b.genderFemaleTextview.setVisibility(View.GONE);
        });
        b.genderFemaleTextview.setOnClickListener(view -> {
            b.genderDefaultText.setText(Constants.GENDER_FEMALE);

            b.genderArrow.setRotation(0);

            b.genderMaleTextview.setVisibility(View.GONE);
            b.genderFemaleTextview.setVisibility(View.GONE);
        });


        b.passwordEtSignUp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Stash.put(Constants.MY_PASSWORD, charSequence.toString());
                if (charSequence.toString().isEmpty()) {
                    b.passwordStrengthLayout.setVisibility(View.GONE);
                } else {
                    b.passwordStrengthLayout.setVisibility(View.VISIBLE);

                    if (charSequence.toString().length() < 5) {
                        controller.weak();
                    } else {

                        controller.medium1();
                        if (charSequence.toString().matches(".*\\d.*")) {
                            // contains a number

                            controller.medium2();
                            if (charSequence.toString().length() > 15) {
                                controller.strong();
                            }
                        }

                    }

                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        b.alreadyHaveAnAccount.setOnClickListener(view -> {
            b.cardSignUp.setVisibility(View.GONE);
            b.cardLogin.setVisibility(View.VISIBLE);
        });

        b.dontHaveAnAccount.setOnClickListener(view -> {
            b.cardSignUp.setVisibility(View.VISIBLE);
            b.cardLogin.setVisibility(View.GONE);
        });

        b.loginBtn.setOnClickListener(view -> {
            if (controller.checkEditTextLogin())
                return;

            String emailStr = b.emailEtLogin.getText().toString();
            String passwordStr = b.passwordEtLogin.getText().toString();

            controller.loginUser(emailStr, passwordStr);
        });

        b.signUpBtn.setOnClickListener(view -> {
            if (controller.checkEditTextSignUp())
                return;

            String emailStr = b.emailEtSignUp.getText().toString();
            String passwordStr = b.passwordEtSignUp.getText().toString();

            controller.registerUser(emailStr, passwordStr);
        });

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        // [END config_signin]

        b.googleLoginBtn.setOnClickListener(view -> {
            controller.progressDialog.show();
            signIn();
        });

        b.forgotPasswordBtn.setOnClickListener(view -> {
            b.backBtn.setVisibility(View.VISIBLE);
            b.cardLogin.setVisibility(View.GONE);
            b.cardSignUp.setVisibility(View.GONE);
            b.cardForgotPassword.setVisibility(View.VISIBLE);
        });

        b.backBtn.setOnClickListener(view -> {
            b.backBtn.setVisibility(View.GONE);
            b.cardLogin.setVisibility(View.VISIBLE);
            b.cardSignUp.setVisibility(View.GONE);
            b.cardForgotPassword.setVisibility(View.GONE);
        });

        b.codeBtn.setOnClickListener(view -> {
            if (b.emailEtForgotPassword.getText().toString().isEmpty()) {
                toast("Please enter your email!");
                return;
            }

            Constants.auth().sendPasswordResetEmail(b.emailEtForgotPassword.getText().toString())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            b.emailEtForgotPassword.setText("");
                            toast("Please check your inbox!");
                        }
                    });

        });

    }

    @Override
    public void onBackPressed() {
        if (b.backBtn.getVisibility() == View.VISIBLE) {
            b.backBtn.setVisibility(View.GONE);
            b.cardLogin.setVisibility(View.VISIBLE);
            b.cardSignUp.setVisibility(View.GONE);
            b.cardForgotPassword.setVisibility(View.GONE);
        } else
            super.onBackPressed();
    }

    public OnCompleteListener<Void> onCompleteListener() {
        return new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                controller.progressDialog.dismiss();

                if (task.isSuccessful()) {
                    onCompleteMethod();
                } else {
                    toast(task.getException().getMessage());
                    Log.d(TAG, "onComplete: error " + task.getException().getMessage());

                }
            }
        };
    }

    public void onCompleteMethod() {
        Stash.put(Constants.IS_LOGGED_IN, true);

        Intent intent = new Intent(RegistrationActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        finish();
        startActivity(intent);
    }

    // [START onactivityresult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                controller.progressDialog.dismiss();
                Toast.makeText(this, "Google sign in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Google sign in failed", e);
            }
        }
    }
    // [END onactivityresult]

    // [START auth_with_google]
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        Constants.auth().signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = Constants.auth().getCurrentUser();
                            updateUI(user);
                        } else {
                            controller.progressDialog.dismiss();
                            Toast.makeText(RegistrationActivity.this, "Google sign in failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            updateUI(null);
                        }
                    }
                });
    }
    // [END auth_with_google]

    // [START signin]
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // [END signin]
    public UserModel model = new UserModel();

    private void updateUI(FirebaseUser user) {
        if (user != null) {

            if (user.getDisplayName().isEmpty() || user.getDisplayName() == null) {
                model.name = user.getEmail();
            } else
                model.name = user.getDisplayName();

            model.email = user.getEmail();
            model.gender = Constants.GENDER_MALE;
            model.isApproved = false;

            model.uid = user.getUid();
            model.number = "000000000";
            model.bio = Constants.NULL;
            model.profile_url = Constants.DEFAULT_PROFILE_URL;
            model.followers_count = 0;
            model.following_count = 0;

            Constants.databaseReference().child(Constants.USERS)
                    .child(Constants.auth().getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (!snapshot.exists()) {
                                controller.uploadUserInfoToDatabase();
                            } else {
                                UserModel userModel = snapshot.getValue(UserModel.class);
                                Stash.put(Constants.CURRENT_USER_MODEL, userModel);
                                controller.progressDialog.dismiss();
                                onCompleteMethod();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
    }

}