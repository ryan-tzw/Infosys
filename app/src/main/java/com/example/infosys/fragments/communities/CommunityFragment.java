package com.example.infosys.fragments.communities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.widget.ViewPager2;

import com.example.infosys.R;
import com.example.infosys.activities.CreatePostActivity;
import com.example.infosys.activities.MainActivity;
import com.example.infosys.activities.PostActivity;
import com.example.infosys.adapters.PostsViewPagerAdapter;
import com.example.infosys.enums.Nav;
import com.example.infosys.fragments.communities.posts.PostListFragment;
import com.example.infosys.fragments.main.common.NavFragment;
import com.example.infosys.interfaces.ToolbarConfigurable;
import com.example.infosys.managers.CommunityManager;
import com.example.infosys.managers.MainManager;
import com.example.infosys.model.Community;
import com.example.infosys.utils.AndroidUtil;
import com.example.infosys.utils.FirebaseUtil;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;


/*
 This fragment displays individual communities
 */
public class CommunityFragment extends Fragment implements ToolbarConfigurable, MenuProvider {
    private static final String TAG = "CommunityFragment";
    private static final String ARG_COMMUNITY_ID = "communityId";
    CircularProgressIndicator progressIndicator;
    CoordinatorLayout rootContainer;
    private int memberCount = 0;
    private String communityId, communityName;
    private ImageView imgCommunityImage;
    private TextView txtCommunityName, txtCommunityDesc, txtCommunityMembers;
    private MaterialButton joinButton;
    private FloatingActionButton floatingActionButton;
    private ActivityResultLauncher<Intent> createPostLauncher;
    private ViewPager2 viewPager;

    public CommunityFragment() {
        // Required empty public constructor
    }

    public static CommunityFragment newInstance(String communityId) {
        CommunityFragment fragment = new CommunityFragment();
        Bundle args = new Bundle();
        args.putString(ARG_COMMUNITY_ID, communityId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            communityId = getArguments().getString(ARG_COMMUNITY_ID);
        }


        createPostLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            String postId = data.getStringExtra("postId");

                            // Clear the cached posts so they reload
                            FragmentManager fm = getChildFragmentManager();
                            for (Fragment fragment : fm.getFragments()) {
                                if (fragment instanceof PostListFragment) {
                                    ((PostListFragment) fragment).refreshPosts();
                                }
                            }

                            openPostInCommunity(postId);
                        }
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_community, container, false);

        instantiateViews(view);
        hideScreen();
        retrieveCommunityData(view);

        floatingActionButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CreatePostActivity.class);
            intent.putExtra("communityId", communityId);
            createPostLauncher.launch(intent);
        });

        viewPager.setUserInputEnabled(false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(this, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        // Hide NavFragment if it exists in the parent
        if (getParentFragment() instanceof NavFragment) {
            ((ViewGroup) getParentFragment().requireView()).setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        CommunityManager.getInstance().isUserAdminOfCommunity(communityId)
                .addOnSuccessListener(isAdmin -> {
                    Log.d(TAG, "onCreateMenu: isAdmin: " + isAdmin);
                    if (isAdmin) {
                        menuInflater.inflate(R.menu.community_admin, menu);
                    }
                });
    }

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.manage_button) {
            Log.d(TAG, "onMenuItemSelected: Manage users");
            MainManager.getInstance().getNavFragmentManager(Nav.COMMUNITIES).beginTransaction()
                    .replace(R.id.nav_container, ManageUsersFragment.newInstance(communityId))
                    .addToBackStack(null)
                    .commit();
            return true;
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Community Fragment");

        MaterialToolbar toolbar = requireActivity().findViewById(R.id.app_bar);
        configureToolbar(toolbar);
    }

    @Override
    public void configureToolbar(MaterialToolbar toolbar) {
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(v -> {
            // Safely handle the back navigation
            if (getActivity() instanceof MainActivity) {
                MainActivity activity = (MainActivity) getActivity();

                if (getActivity().getIntent() != null) {
                    getActivity().getIntent().removeExtra("newCommunity");
                }

                // Post to ensure UI operations happen on the main thread
                toolbar.post(activity::navigateBackToCommunities);
            }
        });
    }

    private void openPostInCommunity(String postId) {
        PostActivity.start(getContext(), postId, communityId, communityName);
    }

    private void getProfilePicture(String communityId) {
        CommunityManager.getInstance().getProfilePicture(communityId)
                .addOnSuccessListener(uri -> {
                    Log.d(TAG, "getProfilePicture: Community profile: " + uri);
                    AndroidUtil.loadProfilePicture(requireActivity(), uri, imgCommunityImage);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "getProfilePicture: Failed to get community profile", e);
                });
    }

    private void joinCommunity() {
        Task<Void> task = CommunityManager.getInstance().joinCommunity(communityId);
        task.addOnSuccessListener(aVoid -> AndroidUtil.showToast(requireContext(), "Joined " + communityName));
        memberCount++;
        updateMemberCountDisplay();
        setJoinButton(true, false);
    }

    private void leaveCommunity() {
        Task<Void> task = CommunityManager.getInstance().leaveCommunity(communityId, FirebaseUtil.getCurrentUserUid());
        task.addOnSuccessListener(aVoid -> AndroidUtil.showToast(requireContext(), "Left " + communityName));
        memberCount--;
        updateMemberCountDisplay();
        setJoinButton(false, false);
    }

    private void showScreen() {
        progressIndicator.setVisibility(View.GONE);
        rootContainer.setVisibility(View.VISIBLE);
    }

    private void hideScreen() {
        progressIndicator.setVisibility(View.VISIBLE);
        rootContainer.setVisibility(View.GONE);
    }

    private void updateMemberCountDisplay() {
        String members = memberCount + " Members";
        txtCommunityMembers.setText(members);
    }

    private void retrieveCommunityData(View view) {
        CommunityManager.getInstance().getCommunity(communityId, community -> {
            if (community == null) {
                Log.e(TAG, "onCreateView: Community is null");
                return;
            }

            populateData(community);
            setupViewPager(view);

            Task<Boolean> isMemberTask = CommunityManager.getInstance().isUserMember(communityId, FirebaseUtil.getCurrentUserUid());
            Task<Boolean> isBannedTask = CommunityManager.getInstance().isUserBanned(communityId, FirebaseUtil.getCurrentUserUid());

            Tasks.whenAllSuccess(isMemberTask, isBannedTask)
                    .addOnSuccessListener(tasks -> {
                        boolean isMember = (Boolean) tasks.get(0);
                        boolean isBanned = (Boolean) tasks.get(1);
                        setJoinButton(isMember, isBanned);
                        showScreen();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "retrieveCommunityData: Failed to check membership status", e);
                        showScreen();
                    });
        });
    }

    private void populateData(Community community) {
        memberCount = community.getMemberCount();
        updateMemberCountDisplay();
        communityName = community.getName();
        txtCommunityName.setText(communityName);
        txtCommunityDesc.setText(community.getDescription());
        getProfilePicture(communityId);
    }

    private void setupViewPager(View view) {
        TabLayout tabLayout = view.findViewById(R.id.sorting_layout);
        ViewPager2 viewPager = view.findViewById(R.id.view_pager);
        PostsViewPagerAdapter adapter = new PostsViewPagerAdapter(this, communityId, communityName);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Recent");
                    break;
                case 1:
                    tab.setText("Popular");
                    break;
            }
        }).attach();
    }

    private void instantiateViews(View view) {
        imgCommunityImage = view.findViewById(R.id.community_image);
        txtCommunityName = view.findViewById(R.id.community_name);
        txtCommunityDesc = view.findViewById(R.id.community_description);
        txtCommunityMembers = view.findViewById(R.id.community_members);
        joinButton = view.findViewById(R.id.join_button);
        progressIndicator = view.findViewById(R.id.progress_indicator);
        rootContainer = view.findViewById(R.id.root_container);
        floatingActionButton = view.findViewById(R.id.fab_create_post);
        viewPager = view.findViewById(R.id.view_pager);
    }

    private void setJoinButton(boolean isMember, boolean isBanned) {
        int color = ContextCompat.getColor(requireContext(), R.color.primaryColor);
        if (isMember) {
            joinButton.setOnClickListener(v -> leaveCommunity());
            joinButton.setText(R.string.joined);
            joinButton.setTextColor(color);
            joinButton.setIcon(null);
            joinButton.setBackgroundColor(Color.TRANSPARENT);
            joinButton.setStrokeColorResource(R.color.primaryColor);
            joinButton.setStrokeWidth(2);
        } else {
            joinButton.setOnClickListener(v -> joinCommunity());
            joinButton.setText(R.string.join);
            joinButton.setTextColor(Color.WHITE);
            Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_person_add, null);
            joinButton.setIcon(drawable);
            joinButton.setBackgroundColor(color);
            joinButton.setStrokeColor(null);
        }

        if (isBanned) {
            joinButton.setEnabled(false);
            joinButton.setText("BANNED");
            joinButton.setTextColor(Color.RED);
            joinButton.setIcon(null);
            joinButton.setBackgroundColor(Color.TRANSPARENT);
            joinButton.setStrokeColorResource(R.color.red);
            joinButton.setStrokeWidth(2);
        }
    }
}