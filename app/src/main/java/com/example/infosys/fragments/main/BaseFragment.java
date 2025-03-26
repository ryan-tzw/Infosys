package com.example.infosys.fragments.main;

import androidx.fragment.app.Fragment;

import com.example.infosys.R;
import com.google.android.material.appbar.MaterialToolbar;

/*
  BaseFragment class to be extended by all top-level fragments (fragments in MainActivity)
 */
public abstract class BaseFragment extends Fragment {


    @Override
    public void onResume() {
        super.onResume();
        resetToolbar();
    }

    protected void resetToolbar() {
        MaterialToolbar toolbar = requireActivity().findViewById(R.id.app_bar);
        toolbar.setNavigationIcon(R.drawable.ic_menu);
        toolbar.setNavigationOnClickListener(v -> {
            // TODO: nav drawer
        });
    }
}
