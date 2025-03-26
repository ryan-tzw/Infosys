package com.example.infosys.adapters;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.infosys.enums.SortType;
import com.example.infosys.fragments.communities.posts.PostListFragment;

public class PostsViewPagerAdapter extends FragmentStateAdapter {
    private final static String TAG = "PostsViewPagerAdapter";
    private final String communityId, communityName;

    public PostsViewPagerAdapter(Fragment fragment, String communityId, String communityName) {
        super(fragment);
        Log.d(TAG, "PostsViewPagerAdapter: Initialising PostsViewPagerAdapter with community ID: " + communityId + ", community name: " + communityName);
        this.communityId = communityId;
        this.communityName = communityName;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return PostListFragment.newInstance(communityId, communityName, SortType.RECENT);
            case 1:
                return PostListFragment.newInstance(communityId, communityName, SortType.POPULAR);
            default:
                return new Fragment(); // Default fragment if none match
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
