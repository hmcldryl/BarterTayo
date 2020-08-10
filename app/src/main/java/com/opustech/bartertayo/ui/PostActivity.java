package com.opustech.bartertayo.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.opustech.bartertayo.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class PostActivity extends AppCompatActivity {

    private Toolbar topAppBar;
    private EditText postCaption, postTag;
    private ChipGroup postTagCG;
    private ImageView btnAddImage;
    private ImageButton btnAddTag;
    private FirebaseAuth userAuth;
    private FirebaseFirestore db;
    private DocumentReference userRef;
    private StorageReference postImage;
    private String currentUserID;
    private Uri[] imageUri;
    final static int PICK_IMAGE = 1;

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_topappbar_post, menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        topAppBar = findViewById(R.id.header);
        setSupportActionBar(topAppBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        topAppBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        userAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUserID = userAuth.getCurrentUser().getUid();
        userRef = db.document("Users/" + currentUserID);
        postImage = FirebaseStorage.getInstance().getReference();

        btnAddTag = findViewById(R.id.btnAddTag);
        postTag = findViewById(R.id.postTag);
        postCaption = findViewById(R.id.postCaption);
        btnAddImage = findViewById(R.id.btnAddImage);
        postTagCG = findViewById(R.id.postTagCG);

        btnAddTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Chip chip = new Chip(PostActivity.this);
                String tag = postTag.getText().toString();
                chip.setChipBackgroundColorResource(R.color.colorPrimary);
                chip.setCloseIconVisible(true);
                chip.setChecked(true);
                chip.setText(tag);
                chip.setOnCloseIconClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        postTagCG.removeView(chip);
                    }
                });

                postTagCG.addView(chip);
                postTag.getText().clear();
            }
        });

        btnAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, PICK_IMAGE);
            }
        });

        topAppBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.btn_post) {
                    validateFields();
                }
                return true;
            }
        });

    }

    private void validateFields() {
        String[] tagArray = new String[postTagCG.getChildCount()];
        String post_caption = postCaption.getText().toString();
        String result = "";
        for (int i = 0; i < postTagCG.getChildCount(); i++) {
            Chip chip = (Chip) postTagCG.getChildAt(i);
            tagArray[i] = chip.getText().toString();
        }
        if (tagArray.length > 0) {
            StringBuilder stringBuilder = new StringBuilder();
            for (String tags : tagArray) {
                stringBuilder.append(tags).append(", ");
            }
            result = stringBuilder.deleteCharAt(stringBuilder.length() - 1).toString();
        }
        Toast.makeText(this, "Tags: " + result, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy", Locale.ENGLISH);
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH-mm", Locale.ENGLISH);
            String date = dateFormat.format(calendar.getTime());
            String time = timeFormat.format(calendar.getTime());
            int count = data.getClipData().getItemCount();
            imageUri = new Uri[count];
            for (int currentImageSelect = 0; currentImageSelect < count; currentImageSelect++) {
                Uri uri = data.getClipData().getItemAt(currentImageSelect).getUri();
                imageUri[currentImageSelect] = uri;
                postImage.child(currentUserID)
                        .child("uploaded_images")
                        .child(currentUserID + "_" + date + "_" + time + "_" + (currentImageSelect) + ".jpg")
                        .putFile(uri)
                        .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    Task<Uri> result = task.getResult().getMetadata().getReference().getDownloadUrl();
                                    result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            final String downloadUrl = uri.toString();
                                            HashMap<String, Object> hashMap = new HashMap<>();
                                            hashMap.put("url", downloadUrl);
                                            userRef.collection("posts")
                                                    .add(hashMap)
                                                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentReference> task) {
                                                            if (task.isSuccessful()) {
                                                                Toast.makeText(PostActivity.this, "Image upload success.", Toast.LENGTH_SHORT).show();
                                                            }
                                                            else {
                                                                Log.d("BarterTayo",task.getException().getMessage());
                                                                Toast.makeText(PostActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                        }
                                    });
                                } else {
                                    Log.d("BarterTayo",task.getException().getMessage());
                                    Toast.makeText(PostActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                currentImageSelect = currentImageSelect + 1;
            }
        }
    }

}