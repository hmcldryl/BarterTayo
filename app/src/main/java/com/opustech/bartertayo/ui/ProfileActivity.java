package com.opustech.bartertayo.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.chip.Chip;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opustech.bartertayo.R;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    private TextView profileDisplayName, profileFullname, profileFollowing, profileFollowers, profileBio;
    private ImageView profileImage;
    private Chip profileBarterScore;
    private TabLayout tabLayout;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference userRef;
    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        profileImage = findViewById(R.id.userProfilePhoto);
        profileDisplayName = findViewById(R.id.userProfileName);
        profileFullname = findViewById(R.id.userProfileFullName);
        profileFollowing = findViewById(R.id.userProfileFollowing);
        profileFollowers = findViewById(R.id.userProfileFollowers);
        profileBarterScore = findViewById(R.id.userProfileBarterScore);
        profileBio = findViewById(R.id.userProfileBio);

        tabLayout = findViewById(R.id.profileTabs);


        userRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.hasChild("profile_image")) {
                        String image = snapshot.child("profile_image").getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.placeholder_userpicture).into(profileImage);
                    }
                    if (snapshot.hasChild("display_name")) {
                        String display_name = snapshot.child("display_name").getValue().toString();
                        profileDisplayName.setText(display_name);
                    }
                    if (snapshot.hasChild("first_name") && snapshot.hasChild("last_name")) {
                        String first_name = snapshot.child("first_name").getValue().toString();
                        String last_name = snapshot.child("last_name").getValue().toString();
                        String full_name = first_name + " " + last_name;
                        profileFullname.setText(full_name);
                    }
                    if (snapshot.hasChild("followers")) {
                        int n = Integer.parseInt(snapshot.child("followers").getValue().toString());
                        if (n <= 1) {
                            String display_name = snapshot.child("followers").getValue().toString() + " follower";
                            profileFollowers.setText(display_name);
                        } else {
                            String display_name = snapshot.child("followers").getValue().toString() + " followers";
                            profileFollowers.setText(display_name);
                        }
                    }
                    if (snapshot.hasChild("following")) {
                        String display_name = snapshot.child("following").getValue().toString() + " following";
                        profileFollowing.setText(display_name);
                    }
                    if (snapshot.hasChild("barter_score")) {
                        String display_name = snapshot.child("barter_score").getValue().toString() + " BarterScore";
                        profileBarterScore.setText(display_name);
                    }
                    if (snapshot.hasChild("bio")) {
                        String display_name = snapshot.child("bio").getValue().toString();
                        profileBio.setText(display_name);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}