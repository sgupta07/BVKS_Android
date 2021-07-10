package com.iskcon.bvks.ui.signin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.messaging.FirebaseMessaging;
import com.iskcon.bvks.R;
import com.iskcon.bvks.listeners.LectureLoadListener;
import com.iskcon.bvks.listeners.NotificationSettingsListener;
import com.iskcon.bvks.manager.LectureManager;
import com.iskcon.bvks.manager.SettingsManager;
import com.iskcon.bvks.manager.HistoryManager;
import com.iskcon.bvks.model.Lecture;
import com.iskcon.bvks.model.SettingsModel;

import com.iskcon.bvks.ui.main.MainActivity;
import com.iskcon.bvks.util.Constants;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;
    private static final int RC_SIGN_IN = 9001;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private CallbackManager mCallbackManager;
    private EditText _emailText;
    private EditText _passwordText;
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        _emailText = findViewById(R.id.input_email);
        _passwordText = findViewById(R.id.input_password);
        findViewById(R.id.btn_login).setOnClickListener(this);
        findViewById(R.id.link_signup).setOnClickListener(this);
        findViewById(R.id.googleSignInButton).setOnClickListener(this);
        findViewById(R.id.forgot_password).setOnClickListener(this);
        findViewById(R.id.facebookSignInButton).setOnClickListener(this);

        getKeyHashDev();
    }

    @Override
    public void onStart() {
        super.onStart();

        // Check auth on Activity start
        if (mAuth.getCurrentUser() != null) {
            List<? extends UserInfo> infos = mAuth.getCurrentUser().getProviderData();
            for (UserInfo ui : infos) {
                if (ui.getProviderId().equals(GoogleAuthProvider.PROVIDER_ID)) {
                    onAuthSuccess(mAuth.getCurrentUser());
                    break;
                } else if (ui.getProviderId().equals(FacebookAuthProvider.PROVIDER_ID)) {
                    onAuthSuccess(mAuth.getCurrentUser());
                    break;
                } else {
                    if (mAuth.getCurrentUser().isEmailVerified()) {
                        onAuthSuccess(mAuth.getCurrentUser());
                        break;
                    }
                }
            }
        }
    }

    private void getKeyHashDev() {
        PackageInfo info;
        try {
            info = getPackageManager().getPackageInfo("com.iskcon.bvks", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md;
                md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String something = new String(Base64.encode(md.digest(), 0));
                //String something = new String(Base64.encodeBytes(md.digest()));
                Log.e("hash key", something);
            }
        } catch (PackageManager.NameNotFoundException e1) {
            Log.e("name not found", e1.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.e("no such an algorithm", e.toString());
        } catch (Exception e) {
            Log.e("exception", e.toString());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.link_signup:
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                break;
            case R.id.btn_login:
                login();
                break;
            case R.id.googleSignInButton:
                if (mGoogleSignInClient == null) {
                    // Configure Google Sign In
                    //TODO CHANGE THIS FOR PRODUCTION BUILD
                    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken("184985407900-avq07srgouv7j2cog7038r0j36k1r8rs.apps.googleusercontent.com")//prod
//                            .requestIdToken("519755993619-pl9qp56g9265851ue0kcem2slho5ofqa.apps.googleusercontent.com")//dev
                            .requestEmail()
                            .build();
                    mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
                }
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
                break;
            case R.id.forgot_password:
                String email = _emailText.getText().toString();
                if (email.isEmpty()) {
                    Toast.makeText(getApplication(), "Enter your registered email id", Toast.LENGTH_LONG).show();
                    return;
                }
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    _emailText.setError("enter a valid email address");
                    return;
                } else {
                    _emailText.setError(null);
                }
                mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Email sent.");
                                Toast.makeText(LoginActivity.this, "We have sent you instructions to reset your password!", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(LoginActivity.this, "Failed to send reset email!", Toast.LENGTH_LONG).show();
                            }
                        });
                break;
            case R.id.facebookSignInButton:
                if (mCallbackManager == null) {
                    // Initialize Facebook Login button
                    mCallbackManager = CallbackManager.Factory.create();
                    LoginManager.getInstance().registerCallback(
                            mCallbackManager, new FacebookCallback<LoginResult>() {
                                @Override
                                public void onSuccess(LoginResult loginResult) {
                                    // Handle success
                                    Log.d(TAG, "facebook:onSuccess:" + loginResult);
                                    handleFacebookAccessToken(loginResult.getAccessToken());
                                }

                                @Override
                                public void onCancel() {
                                    Log.d(TAG, "facebook:onCancel");
                                    hideProgressBar();
                                }

                                @Override
                                public void onError(FacebookException exception) {
                                    Log.d(TAG, "facebook:onError", exception);
                                    hideProgressBar();
                                }
                            }
                    );
                }
                LoginManager.getInstance().logInWithReadPermissions(
                        this, Arrays.asList("email", "public_profile"));
                break;
            default:
                break;
        }
    }

    public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed();
            return;
        }

        showProgressBar();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    Log.d(TAG, "signIn:onComplete:" + task.isSuccessful());

                    if (task.isSuccessful()) {
                        final FirebaseUser user = mAuth.getCurrentUser();
                        if (user.isEmailVerified()) {
                            onAuthSuccess(task.getResult().getUser());
                        } else {
                            Toast.makeText(LoginActivity.this,
                                    "Please verify email sent to " + user.getEmail(),
                                    Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Sign In Failed",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        // [START_EXCLUDE silent]
        showProgressBar();
        // [END_EXCLUDE]

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        onAuthSuccess(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(LoginActivity.this,
                                "An account already exists with the same email address but different sign-in credentials. Sign in using a provider associated with this email address.",
                                Toast.LENGTH_LONG).show();
                        hideProgressBar();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SIGNUP) {
        } else if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // [START_EXCLUDE]
                hideProgressBar();
                // [END_EXCLUDE]
            }
        } else {
            // Pass the activity result back to the Facebook SDK
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        showProgressBar();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");
                        onAuthSuccess(task.getResult().getUser());
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                        hideProgressBar();
                    }
                });
    }

    private void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();
    }

    private boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 6 || password.length() > 10) {
            _passwordText.setError("between 6 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }

    private void showProgressBar() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(LoginActivity.this,
                    R.style.AppTheme_Dark_Dialog);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Authenticating...");
        }
        progressDialog.show();
    }

    private void hideProgressBar() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    private void onAuthSuccess(FirebaseUser user) {
        String username = usernameFromEmail(user.getEmail());

        if (!LectureManager.getInstance().lectureLoadComplete()) {
            LectureManager.getInstance().forceLoadLectures(new LectureLoadListener() {
                @Override
                public void onLoadLecturesComplete(@NonNull List<Lecture> lectureList) {
                    hideProgressBar();
                    HistoryManager.INSTANCE.loadHistory();
                    loadNotificationSettings();
                    LectureManager.getInstance().forceLoadLecturesInformation();
                    // Go to MainActivity
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                }

                @Override
                public void onLoadLecturesFailed() {
                    hideProgressBar();
                    // Go to MainActivity
                    HistoryManager.INSTANCE.loadHistory();
                    loadNotificationSettings();
                    LectureManager.getInstance().forceLoadLecturesInformation();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                }
            });
        } else {
            //create urls
            for (Lecture lecture:LectureManager.getInstance().getLectures()){
                if (lecture.videoLink != null && !lecture.videoLink.isEmpty()) {
                   // LectureManager.getInstance().createHLSUrl(lecture);
                }
            }
            hideProgressBar();
            // Go to MainActivity
            HistoryManager.INSTANCE.loadHistory();
            loadNotificationSettings();
            LectureManager.getInstance().forceLoadLecturesInformation();
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();

        }
    }

    private void loadNotificationSettings() {
        SettingsManager.getInstance().getNotificationSettings(new NotificationSettingsListener() {
            @Override
            public void onLoadSettingsComplete(@Nullable SettingsModel settingsModel) {
               if (settingsModel!=null&&settingsModel.notification!=null){
                   if (settingsModel.notification.hindi) {
                       subscribeToTopic(Constants.PUSH_TOPIC_HINDI);
                   } else {
                       unsubscribeFromTopic(Constants.PUSH_TOPIC_HINDI);
                   }
                   if (settingsModel.notification.english) {
                       subscribeToTopic(Constants.PUSH_TOPIC_ENGLISH);
                   } else {
                       unsubscribeFromTopic(Constants.PUSH_TOPIC_ENGLISH);
                   }
                   if (settingsModel.notification.bengali) {
                       subscribeToTopic(Constants.PUSH_TOPIC_BENGALI);
                   } else {
                       unsubscribeFromTopic(Constants.PUSH_TOPIC_BENGALI);
                   }
               }else {
                   subscribeToTopic(Constants.PUSH_TOPIC_HINDI);
                   subscribeToTopic(Constants.PUSH_TOPIC_ENGLISH);
                   subscribeToTopic(Constants.PUSH_TOPIC_BENGALI);

                   SettingsModel sm = new SettingsModel(System.currentTimeMillis(), new SettingsModel.NotificationSettings(true, true, true));
                   SettingsManager.getInstance().saveNotificationSettings(sm);
               }
            }

            @Override
            public void onLoadSettingsFailed() {
            }

            @Override
            public void onDocumentDoesNotExist() {
                subscribeToTopic(Constants.PUSH_TOPIC_HINDI);
                subscribeToTopic(Constants.PUSH_TOPIC_ENGLISH);
                subscribeToTopic(Constants.PUSH_TOPIC_BENGALI);

                SettingsModel sm = new SettingsModel(System.currentTimeMillis(), new SettingsModel.NotificationSettings(true, true, true));
                SettingsManager.getInstance().saveNotificationSettings(sm);
            }
        });
    }


    private String usernameFromEmail(String email) {
        if (email.contains("@")) {
            return email.split("@")[0];
        } else {
            return email;
        }
    }
    private void subscribeToTopic(String topicName) {
        FirebaseMessaging.getInstance().subscribeToTopic(topicName);
    }

    private void unsubscribeFromTopic(String topicName) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topicName);
    }
}
