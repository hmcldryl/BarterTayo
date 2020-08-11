package com.opustech.bartertayo.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.opustech.bartertayo.R;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private EditText user_fname, user_lname, user_dname, user_bdate;
    private TextView buttonText;
    private CardView continueBtn;
    private FirebaseAuth userAuth;
    private FirebaseFirestore db;
    private DocumentReference userRef;
    private CircleImageView user_dpic;
    ProgressBar progressBar;
    String currentUserID;
    final Calendar calendar = Calendar.getInstance();
    final static int PICK_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        userAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUserID = userAuth.getCurrentUser().getUid();
        userRef = db.document("users/" + currentUserID);

        user_dname = findViewById(R.id.regDisplayName);
        user_fname = findViewById(R.id.regFirstName);
        user_lname = findViewById(R.id.regLastName);
        user_bdate = findViewById(R.id.regBirthdate);

        buttonText = findViewById(R.id.buttonText);
        progressBar = findViewById(R.id.progressBar);

        user_dpic = findViewById(R.id.regUserProfilePhoto);

        user_dpic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_IMAGE);
            }
        });

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateBirthDate();
            }
        };

        user_bdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(SetupActivity.this, date, calendar
                        .get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH))
                        .show();
            }
        });

        continueBtn = findViewById(R.id.btnContinue);

        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonText.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);

                String dname = user_dname.getText().toString();
                String fname = user_fname.getText().toString();
                String lname = user_lname.getText().toString();
                String bdate = user_bdate.getText().toString();

                if (dname.isEmpty()) {
                    progressBar.setVisibility(View.GONE);
                    buttonText.setVisibility(View.VISIBLE);
                    user_dname.setError("Please provide a display name.");
                } else if (fname.isEmpty()) {
                    progressBar.setVisibility(View.GONE);
                    buttonText.setVisibility(View.VISIBLE);
                    user_fname.setError("Please enter a first name.");
                } else if (lname.isEmpty()) {
                    progressBar.setVisibility(View.GONE);
                    buttonText.setVisibility(View.VISIBLE);
                    user_lname.setError("Please enter a last name.");
                } else if (bdate.isEmpty()) {
                    progressBar.setVisibility(View.GONE);
                    buttonText.setVisibility(View.VISIBLE);
                    user_lname.setError("Please enter a valid date.");
                } else {
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("display_name", dname);
                    hashMap.put("first_name", fname);
                    hashMap.put("last_name", lname);
                    hashMap.put("birth_date", bdate);
                    hashMap.put("barter_score", "0");
                    hashMap.put("following", "0");
                    hashMap.put("followers", "0");
                    hashMap.put("bio", "Sana all barterist.");

                    userRef.update(hashMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    progressBar.setVisibility(View.GONE);
                                    buttonText.setVisibility(View.VISIBLE);
                                    Toast.makeText(SetupActivity.this, "You have successfully created your BarterTayo account.", Toast.LENGTH_SHORT).show();
                                    FirebaseUser user = userAuth.getCurrentUser();
                                    if (user != null) {
                                        startMainActivity();
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(SetupActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                    buttonText.setVisibility(View.VISIBLE);
                                }
                            });
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = userAuth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(SetupActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            if (userRef != null) {
                userRef.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (value != null && value.exists()) {
                            String image = value.getString("profile_image");
                            Picasso.get()
                                    .load(image)
                                    .into(user_dpic);
                        }
                    }
                });
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                assert result != null;
                Uri resultUri = result.getUri();
                FirebaseStorage.getInstance().getReference().child(currentUserID).child("profile_image")
                        .child(currentUserID + ".jpg")
                        .putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            Task<Uri> result = task.getResult().getMetadata().getReference().getDownloadUrl();
                            result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final String downloadUrl = uri.toString();
                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    hashMap.put("profile_image", downloadUrl);
                                    userRef.set(hashMap)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(SetupActivity.this, "Profile image upload success.", Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(SetupActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
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

    private void updateBirthDate() {
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