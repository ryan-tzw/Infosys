package com.example.infosys.fragments.main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.infosys.R;
import com.example.infosys.fragments.main.common.BaseFragment;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;


public class HomeFragment extends BaseFragment {

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // test button on click listener
        Button testButton = view.findViewById(R.id.test_button);
        testButton.setOnClickListener(v -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("communities").get().addOnSuccessListener(communitiesSnapshot -> {
                for (QueryDocumentSnapshot communityDoc : communitiesSnapshot) {
                    String communityId = communityDoc.getId();

                    db.collection("communities")
                            .document(communityId)
                            .collection("posts")
                            .get()
                            .addOnSuccessListener(postsSnapshot -> {
                                for (DocumentSnapshot postDoc : postsSnapshot) {
                                    postDoc.getReference().update("communityId", communityId)
                                            .addOnSuccessListener(aVoid -> {
                                                Log.d("UpdatePosts", "✅ Updated post " + postDoc.getId());
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("UpdatePosts", "❌ Failed to update post " + postDoc.getId(), e);
                                            });
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e("UpdatePosts", "❌ Failed to get posts for community " + communityId, e);
                            });
                }
            }).addOnFailureListener(e -> {
                Log.e("UpdatePosts", "❌ Failed to get communities", e);
            });


        });

        return view;
    }
}