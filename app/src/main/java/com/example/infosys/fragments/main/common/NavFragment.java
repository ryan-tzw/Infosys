package com.example.infosys.fragments.main.common;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.infosys.R;
import com.example.infosys.enums.Nav;
import com.example.infosys.fragments.main.ChatsFragment;
import com.example.infosys.fragments.main.CommunitiesFragment;
import com.example.infosys.fragments.main.HomeFragment;
import com.example.infosys.fragments.main.NotificationsFragment;
import com.example.infosys.fragments.main.ProfileFragment;

import java.util.Objects;

public class NavFragment extends Fragment {
    private static final String TAG = NavFragment.class.getSimpleName();
    private Fragment fragment;

    public NavFragment() {
        // Required empty public constructor
    }

    public static NavFragment newInstance(Nav fragmentType) {
        Bundle args = new Bundle();
        NavFragment navFragment = new NavFragment();
        args.putString("fragmentType", fragmentType.name());
        navFragment.setArguments(args);
        return navFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String fragmentType = getArguments().getString("fragmentType");
            switch (Objects.requireNonNull(fragmentType)) {
                case "COMMUNITIES":
                    fragment = new CommunitiesFragment();
                    break;
                case "NOTIFICATIONS":
                    fragment = new NotificationsFragment();
                    break;
                case "CHATS":
                    fragment = new ChatsFragment();
                    break;
                case "PROFILE":
                    fragment = new ProfileFragment();
                    break;
                case "HOME":
                    fragment = new HomeFragment();
                    break;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_nav, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState == null) {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.nav_container, fragment)
                    .commit();
        }
    }
}