package com.iskcon.bvks.ui.signin;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.iskcon.bvks.R;

public class SignUpActivity extends AppCompatActivity {
    private static final String TAG = "SignupActivity";

    private FirebaseAuth mAuth;
    private EditText _emailText, _passwordText;
    private Button _signupButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();

        _emailText = findViewById(R.id.input_email);
        _passwordText = findViewById(R.id.input_password);
        _signupButton = findViewById(R.id.btn_signup);
        _signupButton.setOnClickListener(v -> signup());
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    public void signup() {
        Log.d(TAG, "Signup");

        if (!validate()) {
            onSignupFailed();
            return;
        }

        _signupButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(SignUpActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    Log.d(TAG, "createUser:onComplete:" + task.isSuccessful());
                    progressDialog.dismiss();

                    if (task.isSuccessful()) {
                        final FirebaseUser user = mAuth.getCurrentUser();
                        user.sendEmailVerification()
                                .addOnCompleteListener(this, task1 -> {
                                    if (task1.isSuccessful()) {
                                        Toast.makeText(SignUpActivity.this,
                                                "Verification email sent to " + user.getEmail(),
                                                Toast.LENGTH_LONG).show();
                                        // On complete call either onSignupSuccess or onSignupFailed
                                        // depending on success
                                        onSignupSuccess();
                                    } else {
                                        Log.e(TAG, "sendEmailVerification", task1.getException());
                                        Toast.makeText(SignUpActivity.this,
                                                "Failed to send verification email.",
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                    } else {
                        Toast.makeText(SignUpActivity.this, "Sign Up Failed",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void onSignupSuccess() {
        _signupButton.setEnabled(true);
        setResult(RESULT_OK, null);
        finish();
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        _signupButton.setEnabled(true);
    }

    public boolean validate() {
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
}