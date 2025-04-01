package com.example.infosys.managers;

import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.infosys.activities.MainActivity;
import com.example.infosys.enums.Nav;

public class MainManager {
    private static final String TAG = "MainManager";
    public static MainManager instance;
    private MainActivity mainActivity;

    private MainManager() {
    }

    public static synchronized MainManager getInstance() {
        if (instance == null) {
            instance = new MainManager();
        }
        return instance;
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public FragmentManager getMainFragmentManager() {
        return mainActivity.getSupportFragmentManager();
    }

    public FragmentManager getNavFragmentManager(Nav fragmentType) {
        Fragment navFragment = mainActivity.getSupportFragmentManager().findFragmentByTag(fragmentType.toString());
        Log.d(TAG, "getFragmentManager: Getting fragment: " + fragmentType);
        if (navFragment != null) {
            return navFragment.getChildFragmentManager();
        }
        return null;
    }
}
