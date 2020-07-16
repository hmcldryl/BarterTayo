package com.opustech.bartertayo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.opustech.bartertayo.R;

public class RegisterActivity extends AppCompatActivity {

    private CardView registerBtn;
    private EditText regPassword, regEmail, regPasswordConfirm;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();

        registerBtn = findViewById(R.id.btnRegister);
        regPassword = findViewById(R.id.regPassword);
        regPasswordConfirm = findViewById(R.id.regConfirmPassword);
        regEmail = findViewById(R.id.regEmail);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user_email = regEmail.getText().toString().trim();
                String user_password = regPassword.getText().toString().trim();
                String user_passwordConfirm = regPasswordConfirm.getText().toString().trim();

                if (user_email.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Please enter a valid e-mail address (someone@sample.com).", Toast.LENGTH_SHORT).show();
                    regEmail.setError("Please enter a valid e-mail address (someone@sample.com).");
                } else if (user_password.isEmpty() && user_passwordConfirm.isEmpty()) {
                    regPassword.setError("Please enter and confirm your password.");
                    regPasswordConfirm.setError("Please enter and confirm your password.");
                } else if (!user_password.equals(user_passwordConfirm)) {
                    Toast.makeText(RegisterActivity.this, "Passwords do not match. Try again.", Toast.LENGTH_SHORT).show();
                } else {
                    createAccount(user_email, user_password);
                }

            }
        });
    }

    private void createAccount(String user_email, String user_password) {
        firebaseAuth.createUserWithEmailAndPassword(user_email, user_password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "Success.", Toast.LENGTH_SHORT).show();
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            if (user != null) {
                            startSetupActivity();
                            }
                        } else {
                            Toast.makeText(RegisterActivity.this, "Error: " + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void startSetupActivity() {
        Intent intent = new Intent(RegisterActivity.this, SetupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

}
