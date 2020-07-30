package com.opustech.bartertayo.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.opustech.bartertayo.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class PostActivity extends AppCompatActivity {

    private EditText postCaption;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference userReference, postReference;
    private StorageReference postImage;
    private ExtendedFloatingActionButton btnAddBarter;
    private String currentUserID, currentDate, currentTime, fileName;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        btnAddBarter = findViewById(R.id.fab_addBarter);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
        userReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        postReference = FirebaseDatabase.getInstance().getReference().child("Posts");
        postImage = FirebaseStorage.getInstance().getReference();

        btnAddBarter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

    private void storeImage() {
        Calendar datetime = Calendar.getInstance();
        SimpleDateFormat sdfDate = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
        currentDate = sdfDate.format(datetime.getTime());
        currentTime = sdfTime.format(datetime.getTime());

        fileName = currentDate + currentTime;

        StorageReference path = postImage.child("post_images").child(imageUri.getLastPathSegment() + fileName + ".jpg");
    }

}