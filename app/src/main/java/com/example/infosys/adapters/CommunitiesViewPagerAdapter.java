package com.example.infosys.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.infosys.fragments.communities.AllCommunitiesFragment;
import com.example.infosys.fragments.communities.YourCommunitiesFragment;

public class CommunitiesViewPagerAdapter extends FragmentStateAdapter {

    public CommunitiesViewPagerAdapter(Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new YourCommunitiesFragment();
            case 1:
                return new AllCommunitiesFragment();
            default:
                return new Fragment(); // Default fragment if none match
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
