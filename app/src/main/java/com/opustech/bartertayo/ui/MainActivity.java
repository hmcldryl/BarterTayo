package com.opustech.bartertayo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opustech.bartertayo.R;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private BottomAppBar bottomAppBar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference userRef;
    private CircleImageView userProfileImage;
    private TextView userProfileDisplayName, userProfileBarterScore;
    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        drawerLayout = findViewById(R.id.main_container);
        bottomAppBar = findViewById(R.id.main_bottomAppBar);
        setSupportActionBar(bottomAppBar);
        navigationView = findViewById(R.id.main_navigationdrawer);

        View navView = navigationView.inflateHeaderView(R.layout.nav_header);
        userProfileImage = navView.findViewById(R.id.userProfilePhotoDrawer);
        userProfileDisplayName = navView.findViewById(R.id.userDisplayNameDrawer);
        userProfileBarterScore = navView.findViewById(R.id.userBarterScoreDrawer);

        userRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.hasChild("display_name")) {
                        String display_name = snapshot.child("display_name").getValue().toString();
                        userProfileDisplayName.setText(display_name);
                    }
                    if (snapshot.hasChild("profile_image")) {
                        String image = snapshot.child("profile_image").getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.placeholder_userpicture).into(userProfileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                bottomAppBar,
                R.string.openNavDrawer,
                R.string.closeNavDrawer
        );

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        } else {
            updateUI();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_bottomappbar, menu);
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Fragment fragment;
        FragmentManager fragmentManager = getSupportFragmentManager();

        if (id == R.id.btn_logout) {
            logoutUser();
        } else if (id == R.id.btn_home) {
            fragment = new HomeFragment();
            fragmentManager.beginTransaction()
                    .replace(R.id.main_hostFragment, fragment)
                    .commit();
        } else if (id == R.id.btn_profile) {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        } else if (id == R.id.btn_messages) {
            fragment = new HomeFragment();
            fragmentManager.beginTransaction()
                    .replace(R.id.main_hostFragment, fragment)
                    .commit();
        } else if (id == R.id.btn_notifications) {
            fragment = new NotificationsFragment();
            fragmentManager.beginTransaction()
                    .replace(R.id.main_hostFragment, fragment)
                    .commit();
        } else if (id == R.id.btn_help) {
            fragment = new HomeFragment();
            fragmentManager.beginTransaction()
                    .replace(R.id.main_hostFragment, fragment)
                    .commit();
        }
        return true;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private void updateUI() {

    }

    private void logoutUser() {
        firebaseAuth.signOut();
        Toast.makeText(this, "Signing out...", Toast.LENGTH_SHORT).show();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Signed out.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }
    }
}