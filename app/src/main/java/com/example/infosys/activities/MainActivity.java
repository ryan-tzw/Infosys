package com.example.infosys.activities;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.infosys.R;
import com.example.infosys.enums.Nav;
import com.example.infosys.fragments.communities.CommunityFragment;
import com.example.infosys.fragments.main.common.NavFragment;
import com.example.infosys.interfaces.ToolbarConfigurable;
import com.example.infosys.managers.MainManager;
import com.example.infosys.utils.AndroidUtil;
import com.example.infosys.utils.FirebaseUtil;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    MaterialToolbar topAppBar;
    private Fragment homeFragment, communitiesFragment, notificationsFragment, chatsFragment, profileFragment, activeFragment;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MainManager.getInstance().setMainActivity(this);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setItemIconTintList(null);
        topAppBar = findViewById(R.id.app_bar);

        Log.d(TAG, "onCreate: Retrieving and converting profile picture to drawable bitmap");
        StorageReference profilePictureReference = FirebaseStorage.getInstance().getReference().child("profile_pictures/").child(Objects.requireNonNull(FirebaseUtil.getCurrentUserUid()));
        profilePictureReference.getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    Glide.with(this)
                            .load(uri)
                            .circleCrop()
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .into(new CustomTarget<Drawable>() {
                                @Override
                                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                    Log.d(TAG, "onResourceReady: Resource ready");
                                    bottomNavigationView.getMenu().findItem(R.id.nav_profile).setIcon(resource);
                                }

                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {
                                    Log.d(TAG, "onLoadCleared: Load Cleared");
                                }
                            });
                });

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

    @Override
    protected void onResume() {
        super.onResume();

        MaterialToolbar toolbar = findViewById(R.id.app_bar);

        FragmentManager fm = getSelectedTabFM();
        Fragment visibleChild = getTopFragmentOf(fm);

        Log.d(TAG, "onResume: visibleChild: " + visibleChild);

        if (visibleChild instanceof ToolbarConfigurable) {
            Log.d(TAG, "onResume: ToolbarConfigurable" + visibleChild);
            ((ToolbarConfigurable) visibleChild).configureToolbar(toolbar);
        }
    }

    private FragmentManager getSelectedTabFM() {
        int selectedItemId = bottomNavigationView.getSelectedItemId();

        if (selectedItemId == R.id.nav_home)
            return MainManager.getInstance().getNavFragmentManager(Nav.HOME);
        if (selectedItemId == R.id.nav_communities)
            return MainManager.getInstance().getNavFragmentManager(Nav.COMMUNITIES);

        return null;
    }

    private Fragment getTopFragmentOf(FragmentManager fm) {
        if (fm != null) {
            List<Fragment> childFragments = fm.getFragments();
            for (int i = childFragments.size() - 1; i >= 0; i--) {
                Fragment child = childFragments.get(i);
                if (child != null && child.isVisible()) {
                    return child;
                }
            }
        }
        return null;
    }


    private void initialiseAppBar(MaterialToolbar appbar) {
        setSupportActionBar(appbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
    }

    private void initialiseFragments() {
        profileFragment = NavFragment.newInstance(Nav.PROFILE);
        homeFragment = NavFragment.newInstance(Nav.HOME);
        communitiesFragment = NavFragment.newInstance(Nav.COMMUNITIES);
        notificationsFragment = NavFragment.newInstance(Nav.NOTIFICATIONS);
        chatsFragment = NavFragment.newInstance(Nav.CHATS);
        activeFragment = homeFragment;

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container_view, profileFragment, "PROFILE").hide(profileFragment)
                .add(R.id.fragment_container_view, notificationsFragment, "NOTIFICATIONS").hide(notificationsFragment)
                .add(R.id.fragment_container_view, chatsFragment, "CHATS").hide(chatsFragment)
                .add(R.id.fragment_container_view, communitiesFragment, "COMMUNITIES").hide(communitiesFragment)
                .add(R.id.fragment_container_view, homeFragment, "HOME")
                .commit();

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                switchFragment(homeFragment, R.menu.home);
            } else if (item.getItemId() == R.id.nav_communities) {
                switchFragment(communitiesFragment, R.menu.communities);
            } else if (item.getItemId() == R.id.nav_notifications) {
                switchFragment(notificationsFragment, R.menu.notifications);
            } else if (item.getItemId() == R.id.nav_chats) {
                switchFragment(chatsFragment, R.menu.chats);
            } else if (item.getItemId() == R.id.nav_profile) {
                switchFragment(profileFragment, R.menu.profile);
            }
            return true;
        });
    }

    private void switchFragment(Fragment targetFragment, int menuResId) {
        if (activeFragment != targetFragment) {
            getSupportFragmentManager().beginTransaction()
                    .hide(activeFragment)
                    .show(targetFragment)
                    .commit();
            activeFragment = targetFragment;
        }

        inflateMenu(menuResId);
    }

    private void inflateMenu(int menuResId) {
        topAppBar.getMenu().clear();
        topAppBar.inflateMenu(R.menu.top_app_bar);
        topAppBar.inflateMenu(menuResId);
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
