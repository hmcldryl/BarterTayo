package com.opustech.bartertayo.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.opustech.bartertayo.R;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import cc.cloudist.acplibrary.ACProgressConstant;
import cc.cloudist.acplibrary.ACProgressFlower;

public class PostActivity extends AppCompatActivity {

    private Toolbar topAppBar;
    private EditText postDescription, postTag;
    private ChipGroup postTagCG;
    private ImageView btnAddImage;
    private ImageButton btnAddTag;
    private LinearLayout uploadedImage;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private CollectionReference usersCollectionRef, postsCollectionRef;
    private DocumentReference postsDocumentRef;
    private StorageReference storage, postsStorageRef;
    private String currentUserID;
    private ArrayList<Uri> imageUriList = new ArrayList<>();
    private ArrayList<String> imageUrlList = new ArrayList<>();
    private Uri imageUri;
    final static int PICK_IMAGE = 1;
    private String TAG = "OPUSTECH_DEBUG";

    @Override
    public void onBackPressed() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage("Save post as draft?");
        alertDialog.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (imageUrlList.size() > 0) {
                    for (int i = 0; i < imageUrlList.size(); i++) {
                        FirebaseStorage.getInstance().getReferenceFromUrl(imageUrlList.get(i))
                                .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                            }
                        });
                    }
                    imageUrlList.clear();
                    clearImage();
                }
                dialog.dismiss();
                finish();
            }
        });
        alertDialog.show();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_topappbar_post, menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        uploadedImage = findViewById(R.id.postImagesContainer);

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
        postsStorageRef = storage.child(currentUserID);

        btnAddTag = findViewById(R.id.btnAddTag);
        postTag = findViewById(R.id.postTag);
        postDescription = findViewById(R.id.postDescription);
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
            }
        });

        btnAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(intent, PICK_IMAGE);
            }
        });

        topAppBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.btn_post) {
                    uploadPost();
                }
                return true;
            }
        });

    }

    private void uploadPost() {
        if (postTagCG.getChildCount() > 0 && !postDescription.getText().toString().isEmpty() && imageUrlList.size() == imageUriList.size()) {
            final ACProgressFlower dialog = new ACProgressFlower.Builder(PostActivity.this)
                    .direction(ACProgressConstant.DIRECT_CLOCKWISE)
                    .themeColor(getResources().getColor(R.color.colorPrimary))
                    .text("Uploading...")
                    .fadeColor(Color.DKGRAY).build();
            dialog.show();

            String[] tagArray = new String[postTagCG.getChildCount()];
            String description = postDescription.getText().toString();

            for (int i = 0; i < postTagCG.getChildCount(); i++) {
                Chip chip = (Chip) postTagCG.getChildAt(i);
                tagArray[i] = chip.getText().toString();
            }

            List<String> tags = Arrays.asList(tagArray);

            DataPost dataPost = new DataPost(currentUserID, description, imageUrlList, tags);

            postsDocumentRef.set(dataPost)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (!task.isSuccessful()) {
                                Log.d(TAG, task.getException().getMessage());
                            } else {
                                dialog.dismiss();
                                finish();
                            }
                        }
                    });
        }
        if (postTagCG.getChildCount() == 0) {
            Toast.makeText(PostActivity.this, "Enter at least one (1) or more tags.", Toast.LENGTH_SHORT).show();
        }
        if (postDescription.getText().toString().isEmpty()) {
            postDescription.setError("Provide a description to your post.");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    if (data.getClipData() != null) {
                        int countClipData = (int) data.getClipData().getItemCount();
                        int currentImageSelect = 0;

                        while (currentImageSelect < countClipData) {
                            imageUri = data.getClipData().getItemAt(currentImageSelect).getUri();
                            imageUriList.add(imageUri);
                            uploadImage(imageUri);
                            currentImageSelect = currentImageSelect + 1;
                        }
                        Toast.makeText(this, "You uploaded " + imageUriList.size() + " images.", Toast.LENGTH_SHORT).show();
                    } else {
                        imageUri = data.getData();
                        imageUriList.add(imageUri);
                        uploadImage(imageUri);
                        Toast.makeText(this, "You uploaded 1 image.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

        }
    }

    private void uploadImage(Uri imageUri) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy_hh-mm-ss", Locale.ENGLISH);
        String dateTime = dateFormat.format(Calendar.getInstance().getTime());
        postsStorageRef.child(dateTime + "_" + imageUriList.size() + ".jpg")
                .putFile(imageUri)
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            Task<Uri> result = task.getResult().getMetadata().getReference().getDownloadUrl();
                            result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final String imageUrl = uri.toString();
                                    imageUrlList.add(imageUrl);
                                    loadImage(imageUrl);
                                }
                            });
                        } else {
                            Log.d(TAG, task.getException().getMessage());
                        }
                    }
                });
    }

    private void loadImage(String imageUrl) {
        LayoutInflater inflater = LayoutInflater.from(PostActivity.this);
        View view = inflater.inflate(R.layout.upload_image_item_layout, uploadedImage, false);
        final ImageView postImage = view.findViewById(R.id.postImage);
        if (postImage.getParent() != null) {
            ((ViewGroup) postImage.getParent()).removeView(postImage);
        }
        uploadedImage.addView(postImage);
        Picasso.get().load(imageUrl).into(postImage);
        postImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadedImage.removeView(postImage);
            }
        });
    }

    private void clearImage() {
        uploadedImage.removeAllViews();
    }

}