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
import androidx.fragment.app.FragmentManager;

import com.example.infosys.R;
import com.example.infosys.enums.Nav;
import com.example.infosys.fragments.communities.CommunityFragment;
import com.example.infosys.fragments.main.FriendsFragment;
import com.example.infosys.fragments.main.HomeFragment;
import com.example.infosys.fragments.main.NotificationsFragment;
import com.example.infosys.fragments.main.ProfileFragment;
import com.example.infosys.fragments.main.nav.NavCommunitiesFragment;
import com.example.infosys.interfaces.ToolbarConfigurable;
import com.example.infosys.managers.MainManager;
import com.example.infosys.utils.AndroidUtil;
import com.example.infosys.utils.FirebaseUtil;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private Fragment homeFragment, communitiesFragment, notificationsFragment, friendsFragment, profileFragment, activeFragment;
    private BottomNavigationView bottomNavigationView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MainManager.getInstance().setMainActivity(this);

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
        profileFragment = new ProfileFragment();
        homeFragment = new HomeFragment();
        communitiesFragment = new NavCommunitiesFragment();
        notificationsFragment = new NotificationsFragment();
        friendsFragment = new FriendsFragment();
        activeFragment = homeFragment;

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container_view, profileFragment, "PROFILE").hide(profileFragment)
                .add(R.id.fragment_container_view, notificationsFragment, "NOTIFICATIONS").hide(notificationsFragment)
                .add(R.id.fragment_container_view, friendsFragment, "FRIENDS").hide(friendsFragment)
                .add(R.id.fragment_container_view, communitiesFragment, "COMMUNITIES").hide(communitiesFragment)
                .add(R.id.fragment_container_view, homeFragment, "HOME")
                .commit();

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                switchFragment(homeFragment);
            } else if (item.getItemId() == R.id.nav_communities) {
                switchFragment(communitiesFragment);
            } else if (item.getItemId() == R.id.nav_notifications) {
                switchFragment(notificationsFragment);
            } else if (item.getItemId() == R.id.nav_chats) {
                switchFragment(friendsFragment);
            } else if (item.getItemId() == R.id.nav_profile) {
                switchFragment(profileFragment);
            }
            return true;
        });
    }

    private void switchFragment(Fragment targetFragment) {
        if (activeFragment != targetFragment) {
            getSupportFragmentManager().beginTransaction()
                    .hide(activeFragment)
                    .show(targetFragment)
                    .commit();
            activeFragment = targetFragment;
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

}
