package com.opustech.bartertayo.ui;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.opustech.bartertayo.R;
import com.squareup.picasso.Picasso;

public class ProfileEditActivity extends AppCompatActivity {
    private Toolbar topAppBar;
    private TextView profileDisplayName, profileFullname, profileFollowing, profileFollowers, profileBio;
    private ImageView profileImage;
    private Chip profileBarterScore;
    private FirebaseAuth userAuth;
    private FirebaseFirestore db;
    private DocumentReference userRef;
    private String currentUserID;

    private String KEY_DISPLAY_NAME = "display_name";
    private String KEY_FIRST_NAME = "first_name";
    private String KEY_LAST_NAME = "last_name";
    private String KEY_BIRTH_DATE = "birth_date";
    private String KEY_BARTER_SCORE = "barter_score";
    private String KEY_FOLLOWING = "following";
    private String KEY_FOLLOWERS = "followers";
    private String KEY_BIO = "bio";
    private String KEY_PROFILE_IMAGE = "profile_image";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        userAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUserID = userAuth.getCurrentUser().getUid();
        userRef = db.collection("users").document(currentUserID);
        currentUserID = userAuth.getCurrentUser().getUid();

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

        profileImage = findViewById(R.id.userProfilePhoto);
        profileDisplayName = findViewById(R.id.userProfileName);
        profileFullname = findViewById(R.id.userProfileFullName);
        profileFollowing = findViewById(R.id.userProfileFollowing);
        profileFollowers = findViewById(R.id.userProfileFollowers);
        profileBarterScore = findViewById(R.id.userProfileBarterScore);
        profileBio = findViewById(R.id.userProfileBio);
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = userAuth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(ProfileEditActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            userRef.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                    if (value != null && value.exists()) {
                        String image = value.getString(KEY_PROFILE_IMAGE);
                        String display_name = value.getString(KEY_DISPLAY_NAME);
                        String first_name = value.getString(KEY_FIRST_NAME);
                        String last_name = value.getString(KEY_LAST_NAME);
                        String full_name = first_name + " " + last_name;
                        int n = Integer.parseInt(value.getString(KEY_FOLLOWERS));
                        if (n <= 1) {
                            String followers = value.getString(KEY_FOLLOWERS) + " follower";
                            profileFollowers.setText(followers);
                        } else {
                            String followers = value.getString(KEY_FOLLOWERS) + " followers";
                            profileFollowers.setText(followers);
                        }
                        String following = value.getString(KEY_FOLLOWING) + " following";
                        String barter_score = value.getString(KEY_BARTER_SCORE) + " BarterScore";
                        String bio = value.getString(KEY_BIO);
                        Picasso.get()
                                .load(image)
                                .into(profileImage);
                        profileDisplayName.setText(display_name);
                        profileFullname.setText(full_name);
                        profileFollowing.setText(following);
                        profileBarterScore.setText(barter_score);
                        profileBio.setText(bio);
                    }
                }
            });
        }
    }
}