package com.opustech.bartertayo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.opustech.bartertayo.MainActivity;
import com.opustech.bartertayo.R;

public class LoginActivity extends AppCompatActivity {

    EditText loginUser, loginPassword;
    Button loginBtn;
    TextView registerBtn;
    ProgressBar loginLoading;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();

        loginUser = findViewById(R.id.loginUsername);
        loginPassword = findViewById(R.id.loginPassword);
        loginBtn = findViewById(R.id.btnLogin);
        registerBtn = findViewById(R.id.registerBtn);
        loginLoading = findViewById(R.id.loginLoading);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginLoading.setVisibility(View.VISIBLE);
                registerBtn.setVisibility(View.GONE);

                String user_email = loginUser.getText().toString().trim();
                String user_password = loginPassword.getText().toString().trim();

                if (user_email.isEmpty()) {
                    loginUser.setError("Please enter username.");
                    loginLoading.setVisibility(View.GONE);
                    registerBtn.setVisibility(View.VISIBLE);
                }
                else if (user_password.isEmpty()) {
                    loginPassword.setError("Please enter password.");
                    loginLoading.setVisibility(View.GONE);
                    registerBtn.setVisibility(View.VISIBLE);
                }
                else {
                    loginUser(user_email, user_password);
                }
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent home = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(home);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            Intent home = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(home);
            finish();
        }
    }

    private void loginUser(String user_email, String user_password) {
        firebaseAuth.signInWithEmailAndPassword(user_email, user_password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Success.", Toast.LENGTH_SHORT).show();
                            loginLoading.setVisibility(View.GONE);
                            registerBtn.setVisibility(View.VISIBLE);
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            if (user != null) {
                                startMainActivity();
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "Error: " + task.getException(), Toast.LENGTH_SHORT).show();
                            loginLoading.setVisibility(View.GONE);
                            registerBtn.setVisibility(View.VISIBLE);

                        }
                    }
                });
    }

    private void startMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}
