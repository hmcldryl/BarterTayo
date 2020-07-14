package com.opustech.bartertayo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.opustech.bartertayo.ui.LoginActivity;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private EditText user_fname, user_lname, user_dname, user_bdate;
    private Button continueBtn;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private CircleImageView user_dpic;
    ProgressBar progressBar;
    String currentUserID;
    final Calendar calendar = Calendar.getInstance();
    final static int Gallery_Pick = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        storageReference = FirebaseStorage.getInstance().getReference().child("Profile Image");

        user_dname = findViewById(R.id.regDisplayName);
        user_fname = findViewById(R.id.regFirstName);
        user_lname = findViewById(R.id.regLastName);
        user_bdate = findViewById(R.id.regBirthdate);

        progressBar = findViewById(R.id.imageLoading);

        user_dpic = findViewById(R.id.regUserProfilePhoto);

        user_dpic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, Gallery_Pick);
            }
        });

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
                } else if (fname.isEmpty()) {
                    user_fname.setError("Please enter a first name.");
                } else if (lname.isEmpty()) {
                    user_lname.setError("Please enter a last name.");
                } else if (bdate.isEmpty()) {
                    user_lname.setError("Please enter a valid date.");
                } else {
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
                            } else {
                                Toast.makeText(SetupActivity.this, "Error: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }
        user_dpic.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (requestCode == RESULT_OK) {
                Uri uri = result.getUri();
                StorageReference filePath = storageReference.child(currentUserID + ".jpg");
                filePath.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            user_dpic.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(SetupActivity.this, "Profile image upload success.", Toast.LENGTH_SHORT).show();
                            final String downloadUrl = task.getResult().getMetadata().getReference().getDownloadUrl().toString();
                            databaseReference.child("Profile Image").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(SetupActivity.this, "Profile image upload success.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(SetupActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(SetupActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                Toast.makeText(SetupActivity.this, "Error: Image cannot be cropped.", Toast.LENGTH_SHORT).show();
            }
        }
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