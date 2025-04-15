package com.example.infosys.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuProvider;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;

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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    getAndSaveFcmToken();
                } else {
                    Log.d(TAG, "Notification permission denied");
                    new MaterialAlertDialogBuilder(this)
                            .setTitle("Notification Permission Denied")
                            .setMessage("Notification permission denied. You will not receive push notifications, but will still be able to view in-app notifications.")
                            .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                            .show();
                }
            });
    MaterialToolbar topAppBar;
    private int lastSelectedMenu = R.menu.home;
    private Fragment homeFragment, communitiesFragment, notificationsFragment, chatsFragment, profileFragment, activeFragment;
    private BottomNavigationView bottomNavigationView;
    private MenuProvider currentMenuProvider;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.dark_blue));


        WindowInsetsControllerCompat insetsController =
                new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        insetsController.setAppearanceLightStatusBars(false);  // Use true for dark icons on light bg



        Log.d(TAG, "onCreate called");

        requestNotificationPermissionIfNeeded();

        MainManager.getInstance().setMainActivity(this);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setItemIconTintList(null);
        topAppBar = findViewById(R.id.app_bar);

        AndroidUtil.setToolbarPadding(topAppBar);

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
        handleIntent(getIntent());

        // Initially hide the top app bar if the active fragment is Home
        if (activeFragment == homeFragment) {
            topAppBar.setVisibility(View.GONE);  // Hide the toolbar when Home is active
        }
    }



    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        Log.d(TAG, "onNewIntent: New Intent: " + intent);
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent == null) return;

        // When the app is opened from a notification, handle the notification
        if (intent.hasExtra("notification_type")) {
            String type = intent.getStringExtra("notification_type");

            Log.d(TAG, "handleIntent: App opened from notification, type: " + type);

            if ("message".equals(type)) {
                String chatId = intent.getStringExtra("chatId");
                openChat(chatId);
            } else if ("comment".equals(type)) {
                String postId = intent.getStringExtra("postId");
                String communityId = intent.getStringExtra("communityId");
                String communityName = intent.getStringExtra("communityName");
                openPostFromNotification(postId, communityId, communityName);
            }
        }

        // When a new community is created, navigate to the community fragment
        if (intent.hasExtra("newCommunity")) {
            boolean newCommunity = intent.getBooleanExtra("newCommunity", false);
            if (newCommunity) {
                Log.d(TAG, "handleIntent: New community created, navigating to it");
                String communityId = intent.getStringExtra("communityId");

                Log.d(TAG, "handleIntent: Navigating to newly created community with id: " + communityId);

                bottomNavigationView.setSelectedItemId(R.id.nav_communities);
                findViewById(R.id.bottom_navigation).setVisibility(bottomNavigationView.GONE);
                CommunityFragment fragment = CommunityFragment.newInstance(communityId);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container_view, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        }
    }

    private void openChat(String chatId) {
        Intent chatIntent = new Intent(this, ChatActivity.class);
        chatIntent.putExtra("chatId", chatId);
        startActivity(chatIntent);
    }

    private void openPostFromNotification(String postId, String communityId, String communityName) {
        Intent postIntent = new Intent(this, PostActivity.class);
        postIntent.putExtra("postId", postId);
        postIntent.putExtra("communityId", communityId);
        postIntent.putExtra("communityName", communityName);
        startActivity(postIntent);
    }


    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED) {
                getAndSaveFcmToken();
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                Log.d(TAG, "Permission denied before.");
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            getAndSaveFcmToken();
        }
    }


    private void getAndSaveFcmToken() {
        Log.d(TAG, "getAndSaveFcmToken: Saving user token");
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token -> {
                    Log.d(TAG, "onCreate: User token: " + token);
                    FirebaseUtil.updateFcmToken(token);
                })
                .addOnFailureListener(e -> Log.e(TAG, "onCreate: Failed to get FCM token. ", e));
    }


    @Override
    protected void onResume() {
        super.onResume();
        MaterialToolbar toolbar = findViewById(R.id.app_bar);
        FragmentManager fm = getSelectedTabFM();
        Fragment visibleChild = getTopFragmentOf(fm);

        Log.d(TAG, "onResume: visibleChild: " + visibleChild);

        if (visibleChild instanceof ToolbarConfigurable) {
            Log.d(TAG, "onResume: ToolbarConfigurable: " + visibleChild);
            ((ToolbarConfigurable) visibleChild).configureToolbar(toolbar);
        }

        // Check if a new community was created and navigate to it
        if (getIntent() != null && getIntent().hasExtra("newCommunity")) {
            boolean newCommunity = getIntent().getBooleanExtra("newCommunity", false);
            if (newCommunity) {
                String communityId = getIntent().getStringExtra("communityId");

                Log.d(TAG, "onResume: New community created, navigating to it");

                // Navigate to the new community's fragment
                bottomNavigationView.setSelectedItemId(R.id.nav_communities);
                CommunityFragment fragment = CommunityFragment.newInstance(communityId);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container_view, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        }
    }



    private FragmentManager getSelectedTabFM() {
        int selectedItemId = bottomNavigationView.getSelectedItemId();

        if (selectedItemId == R.id.nav_home)
            return MainManager.getInstance().getNavFragmentManager(Nav.HOME);
        if (selectedItemId == R.id.nav_communities)
            return MainManager.getInstance().getNavFragmentManager(Nav.COMMUNITIES);
        if (selectedItemId == R.id.nav_notifications)
            return MainManager.getInstance().getNavFragmentManager(Nav.NOTIFICATIONS);
        if (selectedItemId == R.id.nav_chats)
            return MainManager.getInstance().getNavFragmentManager(Nav.CHATS);
        if (selectedItemId == R.id.nav_profile)
            return MainManager.getInstance().getNavFragmentManager(Nav.PROFILE);

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
            // Hide the toolbar for the home fragment
            if ((targetFragment == homeFragment || targetFragment == chatsFragment || targetFragment == notificationsFragment)){
                topAppBar.setVisibility(View.GONE);  // Hide toolbar
            }else {
                topAppBar.setVisibility(View.VISIBLE);  // Show toolbar for other fragments
            }

            getSupportFragmentManager().beginTransaction()
                    .hide(activeFragment)
                    .show(targetFragment)
                    .runOnCommit(() -> updateToolbarMenuForFragment(targetFragment))
                    .commit();
            activeFragment = targetFragment;
        }
        lastSelectedMenu = menuResId;
    }


    private void updateToolbarMenuForFragment(Fragment navFragment) {
        topAppBar.getMenu().clear();
        topAppBar.inflateMenu(R.menu.top_app_bar);

        // Remove old provider if exists
        if (currentMenuProvider != null) {
            removeMenuProvider(currentMenuProvider);
            currentMenuProvider = null;
        }

        // Check for visible child fragment inside the NavFragment
        FragmentManager fm = navFragment.getChildFragmentManager();
        Fragment innerFragment = getTopFragmentOf(fm);
        if (innerFragment instanceof MenuProvider) {
            currentMenuProvider = (MenuProvider) innerFragment;
            addMenuProvider(currentMenuProvider, this, Lifecycle.State.RESUMED);
            getMenuInflater().inflate(lastSelectedMenu, topAppBar.getMenu());
        }
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

    public void navigateBackToCommunities() {
        // This already exists in your code
        switchFragment(communitiesFragment, R.menu.communities);
        findViewById(R.id.bottom_navigation).setVisibility(bottomNavigationView.VISIBLE);

        // Clear any back stack entries related to community fragments
        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        // Ensure bottom nav is selected
        bottomNavigationView.setSelectedItemId(R.id.nav_communities);
    }
}