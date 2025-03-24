package com.example.infosys.fragments;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.example.infosys.R;
import com.example.infosys.managers.CommunityManager;
import com.example.infosys.utils.AndroidUtil;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class CommunityFragment extends Fragment {
    private static final String TAG = "CommunityFragment";
    private static final String ARG_PARAM1 = "communityId";
    CircularProgressIndicator progressIndicator;
    ConstraintLayout rootContainer;
    private int memberCount = 0;
    private String communityId;
    private ImageView communityImage;
    private TextView communityName, communityDescription, communityMembers;
    private MaterialButton joinButton;

    public CommunityFragment() {
        // Required empty public constructor
    }

    public static CommunityFragment newInstance(String param1) {
        CommunityFragment fragment = new CommunityFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            communityId = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_community, container, false);

        instantiateViews(view);
        populateData();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        MaterialToolbar toolbar = requireActivity().findViewById(R.id.app_bar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });
    }

    private void getProfilePicture(String communityId) {
        String path = String.format("communities/%s/profile", communityId);
        StorageReference ref = FirebaseStorage.getInstance().getReference().child(path);
        ref.getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    Log.d(TAG, "getProfilePicture: Community profile: " + uri);
                    AndroidUtil.loadProfilePicture(requireActivity(), uri, communityImage);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "getProfilePicture: Failed to get community profile", e);
                });
    }

    private void instantiateViews(View view) {
        communityImage = view.findViewById(R.id.community_image);
        communityName = view.findViewById(R.id.community_name);
        communityDescription = view.findViewById(R.id.community_description);
        communityMembers = view.findViewById(R.id.community_members);
        joinButton = view.findViewById(R.id.join_button);
        progressIndicator = view.findViewById(R.id.progress_indicator);
        rootContainer = view.findViewById(R.id.root_container);
    }

    private void populateData() {
        CommunityManager.getInstance().getCommunityDetails(communityId, community -> {
            if (community == null) {
                Log.e(TAG, "onCreateView: Community is null");
                return;
            }

            memberCount = community.getMemberCount();
            updateMemberCountDisplay();
            communityName.setText(community.getName());
            communityDescription.setText(community.getDescription());
            getProfilePicture(communityId);

            CommunityManager.getInstance().isUserMemberOfCommunity(communityId, isMember -> {
                setJoinButton(isMember);
                showScreen();
            });
        });
    }

    private void joinCommunity() {
        CommunityManager.getInstance().joinCommunity(communityId);
        memberCount++;
        updateMemberCountDisplay();
        setJoinButton(true);
    }

    private void leaveCommunity() {
        CommunityManager.getInstance().leaveCommunity(communityId);
        memberCount--;
        updateMemberCountDisplay();
        setJoinButton(false);
    }

    private void setJoinButton(boolean isMember) {
        int color = ContextCompat.getColor(requireContext(), R.color.primaryColor);
        if (isMember) {
            joinButton.setOnClickListener(v -> leaveCommunity());
            joinButton.setText("Joined");
            joinButton.setTextColor(color);
            joinButton.setIcon(null);
            joinButton.setBackgroundColor(Color.TRANSPARENT);
            joinButton.setStrokeColorResource(R.color.primaryColor);
            joinButton.setStrokeWidth(2);
        } else {
            joinButton.setOnClickListener(v -> joinCommunity());

            joinButton.setText("Join");
            joinButton.setTextColor(Color.WHITE);
            Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_person_add, null);
            joinButton.setIcon(drawable);
            joinButton.setBackgroundColor(color);
            joinButton.setStrokeColor(null);
        }
    }

    private void showScreen() {
        progressIndicator.setVisibility(View.GONE);
        rootContainer.setVisibility(View.VISIBLE);
    }

    private void updateMemberCountDisplay() {
        String members = memberCount + " Members";
        communityMembers.setText(members);
    }
}