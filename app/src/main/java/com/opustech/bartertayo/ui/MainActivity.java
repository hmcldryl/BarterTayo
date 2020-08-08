package com.opustech.bartertayo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.opustech.bartertayo.R;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private Toolbar topAppBar;
    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomAppBar;
    private NavigationView navigationView;
    private FirebaseAuth userAuth;
    private FirebaseFirestore db;
    private DocumentReference userRef;
    private CircleImageView userProfileImage;
    private TextView userProfileDisplayName, userProfileFollowers, userProfileFollowing;
    private Chip userProfileBarterScore;
    private String currentUserID;

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_topappbar, menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUserID = userAuth.getCurrentUser().getUid();
        userRef = db.collection("Users").document(currentUserID);

        drawerLayout = findViewById(R.id.main_container);
        bottomAppBar = findViewById(R.id.main_bottomAppBar);
        topAppBar = findViewById(R.id.main_topAppBar);
        setSupportActionBar(topAppBar);


        navigationView = findViewById(R.id.main_navigationdrawer);

        View navView = navigationView.inflateHeaderView(R.layout.nav_header);
        userProfileImage = navView.findViewById(R.id.userProfilePhotoDrawer);
        userProfileDisplayName = navView.findViewById(R.id.userDisplayNameDrawer);
        userProfileBarterScore = navView.findViewById(R.id.userBarterScoreDrawer);
        userProfileFollowers = navView.findViewById(R.id.userFollowerCountDrawer);
        userProfileFollowing = navView.findViewById(R.id.userFollowingCountDrawer);

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        bottomAppBar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                Fragment fragment;
                FragmentManager fragmentManager = getSupportFragmentManager();
                if (id == R.id.navigation_home) {
                    fragment = new HomeFragment();
                    fragmentManager.beginTransaction()
                            .replace(R.id.main_hostFragment, fragment)
                            .commit();
                }
                if (id == R.id.navigation_messages) {
                    fragment = new MessagesFragment();
                    fragmentManager.beginTransaction()
                            .replace(R.id.main_hostFragment, fragment)
                            .commit();
                }
                if (id == R.id.navigation_notifications) {
                    fragment = new NotificationsFragment();
                    fragmentManager.beginTransaction()
                            .replace(R.id.main_hostFragment, fragment)
                            .commit();
                }
                return true;
            }
        });

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                topAppBar,
                R.string.openNavDrawer,
                R.string.closeNavDrawer
        );

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                Fragment fragment;
                FragmentManager fragmentManager = getSupportFragmentManager();

                if (id == R.id.btn_logout) {
                    logoutUser();
                    drawerLayout.closeDrawers();
                }
                if (id == R.id.navigation_home) {
                    fragment = new HomeFragment();
                    fragmentManager.beginTransaction()
                            .replace(R.id.main_hostFragment, fragment)
                            .commit();
                    drawerLayout.closeDrawers();
                }
                if (id == R.id.navigation_profile) {
                    Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    drawerLayout.closeDrawers();
                }
                if (id == R.id.navigation_help) {
                    fragment = new HelpFragment();
                    fragmentManager.beginTransaction()
                            .replace(R.id.main_hostFragment, fragment)
                            .commit();
                    drawerLayout.closeDrawers();
                }
                if (id == R.id.navigation_about) {
                    fragment = new AboutFragment();
                    fragmentManager.beginTransaction()
                            .replace(R.id.main_hostFragment, fragment)
                            .commit();
                    drawerLayout.closeDrawers();
                }
                return true;
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = userAuth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            userRef.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                    if (value != null && value.exists()) {
                        String display_name = value.getString("display_name");
                        String followers = "Followers " + value.getString("followers");
                        String following = "Following " + value.getString("following");
                        String barter_score = value.getString("barter_score") + " BarterScore";
                        String image = value.getString("profile_image");
                        userProfileDisplayName.setText(display_name);
                        userProfileBarterScore.setText(barter_score);
                        userProfileFollowers.setText(followers);
                        userProfileFollowing.setText(following);
                        Picasso.get()
                                .load(image)
                                .into(userProfileImage);
                    }
                }
            });
        }
    }

    private void logoutUser() {
        userAuth.signOut();
        Toast.makeText(this, "Signing out...", Toast.LENGTH_SHORT).show();
        FirebaseUser currentUser = userAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Signed out.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }
}