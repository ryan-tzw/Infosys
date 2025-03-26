package com.example.infosys.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.infosys.R;
import com.example.infosys.fragments.communities.CommunityFragment;
import com.example.infosys.fragments.main.CommunitiesFragment;
import com.example.infosys.fragments.main.FriendsFragment;
import com.example.infosys.fragments.main.HomeFragment;
import com.example.infosys.fragments.main.NotificationsFragment;
import com.example.infosys.fragments.main.ProfileFragment;
import com.example.infosys.utils.AndroidUtil;
import com.example.infosys.utils.FirebaseUtil;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private Fragment homeFragment, communitiesFragment, notificationsFragment, friendsFragment, profileFragment, activeFragment;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        MaterialToolbar topAppBar = findViewById(R.id.app_bar);

        overrideBackButton();

        initialiseAppBar(topAppBar);
        initialiseFragments();

        String communityId = getIntent().getStringExtra("communityId");
        if (communityId != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_communities);
            CommunityFragment fragment = CommunityFragment.newInstance(communityId);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container_view, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void initialiseAppBar(MaterialToolbar appbar) {
        setSupportActionBar(appbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
    }

    private void initialiseFragments() {
        profileFragment = new ProfileFragment();
        homeFragment = new HomeFragment();
        communitiesFragment = new CommunitiesFragment();
        notificationsFragment = new NotificationsFragment();
        friendsFragment = new FriendsFragment();
        activeFragment = homeFragment;

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container_view, profileFragment, "5").hide(profileFragment)
                .add(R.id.fragment_container_view, notificationsFragment, "4").hide(notificationsFragment)
                .add(R.id.fragment_container_view, friendsFragment, "3").hide(friendsFragment)
                .add(R.id.fragment_container_view, communitiesFragment, "2").hide(communitiesFragment)
                .add(R.id.fragment_container_view, homeFragment, "1")
                .commit();

        bottomNavigationView.setOnItemSelectedListener(item -> {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
            }

            if (item.getItemId() == R.id.nav_home) {
                transaction.hide(activeFragment).show(homeFragment);
                activeFragment = homeFragment;
            } else if (item.getItemId() == R.id.nav_communities) {
                transaction.hide(activeFragment).show(communitiesFragment);
                activeFragment = communitiesFragment;
            } else if (item.getItemId() == R.id.nav_notifications) {
                transaction.hide(activeFragment).show(notificationsFragment);
                activeFragment = notificationsFragment;
            } else if (item.getItemId() == R.id.nav_chats) {
                transaction.hide(activeFragment).show(friendsFragment);
                activeFragment = friendsFragment;
            } else if (item.getItemId() == R.id.nav_profile) {
                transaction.hide(activeFragment).show(profileFragment);
                activeFragment = profileFragment;
            }

            transaction.commit();
            return true;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_app_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Log.d(TAG, "Menu item selected: " + item.getItemId());
        if (item.getItemId() == R.id.logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        Log.d(TAG, "Logging out user");
        FirebaseUtil.logoutUser();
        AndroidUtil.navigateTo(MainActivity.this, LoginActivity.class);
    }

    private void overrideBackButton() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!FirebaseUtil.isUserLoggedIn()) {
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        };

        getOnBackPressedDispatcher().addCallback(this, callback);
    }

}
