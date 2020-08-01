package com.opustech.bartertayo.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.chip.Chip;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.opustech.bartertayo.R;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    private TextView profileDisplayName, profileFullname, profileFollowing, profileFollowers, profileBio;
    private ImageView profileImage;
    private Chip profileBarterScore;
    private TabLayout tabLayout;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private DocumentReference documentReference;
    private ListenerRegistration documentListener;
    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        documentReference = firebaseFirestore
                .collection("User")
                .document(currentUserID);

        profileImage = findViewById(R.id.userProfilePhoto);
        profileDisplayName = findViewById(R.id.userProfileName);
        profileFullname = findViewById(R.id.userProfileFullName);
        profileFollowing = findViewById(R.id.userProfileFollowing);
        profileFollowers = findViewById(R.id.userProfileFollowers);
        profileBarterScore = findViewById(R.id.userProfileBarterScore);
        profileBio = findViewById(R.id.userProfileBio);

        tabLayout = findViewById(R.id.profileTabs);
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            finish();
        } else {
            documentListener = documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                    if (value != null && value.exists()) {
                        String image = value.get("profile_image").toString();
                        String display_name = value.get("display_name").toString();
                        String first_name = value.get("first_name").toString();
                        String last_name = value.get("last_name").toString();
                        String full_name = first_name + " " + last_name;
                        int n = Integer.parseInt(value.get("followers").toString());
                        if (n <= 1) {
                            String followers = value.get("followers").toString() + " follower";
                            profileFollowers.setText(followers);
                        } else {
                            String followers = value.get("followers").toString() + " followers";
                            profileFollowers.setText(followers);
                        }
                        String following = value.get("following").toString() + " following";
                        String barter_score = value.get("barter_score").toString() + " BarterScore";
                        String bio = value.get("bio").toString();
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

    @Override
    protected void onStop() {
        super.onStop();
        documentListener.remove();
    }
}