package com.example.infosys.fragments.main.common;

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
    private static final String TAG = "BaseFragment";

    @Override
    public void onResume() {
        super.onResume();
        resetToolbar();
    }

    protected void resetToolbar() {
        MaterialToolbar toolbar = requireActivity().findViewById(R.id.app_bar);
        configureToolbar(toolbar);
    }

    @Override
    public void configureToolbar(MaterialToolbar toolbar) {
        if (MainManager.getInstance().getNavFragmentManager(Nav.COMMUNITIES).getBackStackEntryCount() == 0) {
            toolbar.setNavigationIcon(R.drawable.ic_search);
        }
    }

}
