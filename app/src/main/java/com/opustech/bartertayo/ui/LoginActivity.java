package com.opustech.bartertayo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
                String user_email = loginUser.getText().toString().trim();
                String user_password = loginPassword.getText().toString().trim();

                if (user_email.isEmpty()) {
                    loginUser.setError("Please enter username.");
                }
                else if (user_password.isEmpty()) {
                    loginPassword.setError("Please enter password.");
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
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        //updateUI(currentUser);
    }

}
