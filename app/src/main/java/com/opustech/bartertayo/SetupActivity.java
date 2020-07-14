package com.opustech.bartertayo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.opustech.bartertayo.ui.LoginActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class SetupActivity extends AppCompatActivity {

    private EditText user_fname, user_lname, user_dname, user_bdate;
    private Button continueBtn;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    String currentUserID;
    final Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);

        user_dname = findViewById(R.id.regDisplayName);
        user_fname = findViewById(R.id.regFirstName);
        user_lname = findViewById(R.id.regLastName);
        user_bdate = findViewById(R.id.regBirthdate);

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateBirthdate();
            }
        };

        user_bdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(SetupActivity.this, date, calendar
                        .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        continueBtn = findViewById(R.id.btnContinue);

        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dname = user_dname.getText().toString();
                String fname = user_fname.getText().toString();
                String lname = user_lname.getText().toString();
                String bdate = user_bdate.getText().toString();

                if (dname.isEmpty()) {
                    user_dname.setError("Please provide a display name.");
                }
                else if (fname.isEmpty()) {
                    user_fname.setError("Please enter a first name.");
                }
                else if (lname.isEmpty()) {
                    user_lname.setError("Please enter a last name.");
                }
                else if (bdate.isEmpty()) {
                    user_lname.setError("Please enter a valid date.");
                }
                else {
                    HashMap hashMap = new HashMap();
                    hashMap.put("display_name", dname);
                    hashMap.put("first_name", fname);
                    hashMap.put("last_name", lname);
                    hashMap.put("birth_date", bdate);
                    hashMap.put("barter_post", "Barter");
                    databaseReference.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(SetupActivity.this, "You have successfully created your BarterTayo account.", Toast.LENGTH_SHORT).show();
                                FirebaseUser user = firebaseAuth.getCurrentUser();
                                if (user != null) {
                                    startMainActivity();
                                }
                            }
                            else {
                                Toast.makeText(SetupActivity.this, "Error: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    private void updateBirthdate() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH);
        user_bdate.setText(simpleDateFormat.format(calendar.getTime()));
    }

    private void startMainActivity() {
        Intent intent = new Intent(SetupActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}