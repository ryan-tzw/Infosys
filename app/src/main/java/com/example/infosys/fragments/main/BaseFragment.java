package com.example.infosys.fragments.main;

import android.util.Log;

import androidx.fragment.app.Fragment;

import com.example.infosys.R;
import com.example.infosys.enums.Nav;
import com.example.infosys.interfaces.ToolbarConfigurable;
import com.example.infosys.managers.MainManager;
import com.google.android.material.appbar.MaterialToolbar;

/*
  BaseFragment class to be extended by all top-level fragments (fragments in MainActivity)
 */
public abstract class BaseFragment extends Fragment implements ToolbarConfigurable {
    private static final String TAG = "NavFragment";


    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        resetToolbar();
    }

    protected void resetToolbar() {
        Log.d(TAG, "resetToolbar: ");
        MaterialToolbar toolbar = requireActivity().findViewById(R.id.app_bar);
        configureToolbar(toolbar);
    }

    public void configureToolbar(MaterialToolbar toolbar) {
        Log.d(TAG, "configureToolbar: ");
        if (MainManager.getInstance().getNavFragmentManager(Nav.COMMUNITIES).getBackStackEntryCount() == 0) {
            toolbar.setNavigationIcon(R.drawable.ic_menu);
            toolbar.setNavigationOnClickListener(v -> {
                // TODO: nav drawer

//                DrawerLayout drawer = requireActivity().findViewById(R.id.drawer_layout);
//                drawer.openDrawer(GravityCompat.START);
            });
        }
    }

}
