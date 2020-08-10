package com.opustech.bartertayo.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
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
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private CollectionReference usersCollectionRef, postsCollectionRef;
    private DocumentReference postsDocumentRef;
    private StorageReference storage, postsStorageRef;
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

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance().getReference();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh-mm-ss", Locale.ENGLISH);
        String date = dateFormat.format(Calendar.getInstance().getTime());
        String time = timeFormat.format(Calendar.getInstance().getTime());

        currentUserID = auth.getCurrentUser().getUid();
        usersCollectionRef = db.collection("users");
        postsCollectionRef = db.collection("posts");
        postsDocumentRef = postsCollectionRef.document(currentUserID + "_" + date + "_" + time);
        postsStorageRef = storage.child(currentUserID)
                .child("uploaded_images")
                .child("posts");

        btnAddTag = findViewById(R.id.btnAddTag);
        postTag = findViewById(R.id.postTag);
        postCaption = findViewById(R.id.postCaption);
        btnAddImage = findViewById(R.id.btnAddImage);
        postTagCG = findViewById(R.id.postTagCG);

        btnAddTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!postTag.getText().toString().isEmpty()) {
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
                if (postTag.getText().toString().length() <= 2) {
                    postTag.setError("Please enter a valid keyword.");
                } else {
                    postTag.setError("Please enter at least one (1) keyword.");
                }
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
                    if (postTagCG.getChildCount() > 0 && !postCaption.getText().toString().isEmpty()) {
                        String[] tagArray = new String[postTagCG.getChildCount()];
                        String post_caption = postCaption.getText().toString();
                        for (int i = 0; i < postTagCG.getChildCount(); i++) {
                            Chip chip = (Chip) postTagCG.getChildAt(i);
                            tagArray[i] = chip.getText().toString();
                        }
                    }
                    if (postTagCG.getChildCount() == 0) {
                        Toast.makeText(PostActivity.this, "Please enter at least one (1) or more tags.", Toast.LENGTH_SHORT).show();
                    }
                    if (postCaption.getText().toString().isEmpty()) {
                        postCaption.setError("Please provide a description for your post.");
                    }
                }
                return true;
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            int count = data.getClipData().getItemCount();
            imageUri = new Uri[count];
            for (int i = 0; i < count; i++) {
                Uri uri = data.getClipData().getItemAt(i).getUri();
                imageUri[i] = uri;
                postsStorageRef.child(currentUserID + "_" + i + ".jpg")
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
                                            postsDocumentRef.update(hashMap)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                LinearLayout uploadedImage = findViewById(R.id.postImagesContainer);
                                                                LayoutInflater inflater = LayoutInflater.from(PostActivity.this);
                                                                View view = inflater.inflate(R.layout.upload_image_item_layout, uploadedImage, false);
                                                                ImageView postImage = view.findViewById(R.id.postImage);
                                                                uploadedImage.addView(postImage);
                                                                Picasso.get().load(downloadUrl).into(postImage);

                                                                Toast.makeText(PostActivity.this, "Image upload success.", Toast.LENGTH_SHORT).show();
                                                            } else {
                                                                Log.d("BarterTayo", task.getException().getMessage());
                                                                Toast.makeText(PostActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                        }
                                    });
                                } else {
                                    Log.d("BarterTayo", task.getException().getMessage());
                                    Toast.makeText(PostActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        }
    }

}