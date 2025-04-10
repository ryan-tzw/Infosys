package com.example.infosys.fragments.main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.infosys.R;
import com.example.infosys.fragments.main.common.BaseFragment;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;


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

        testButton(view);

        return view;
    }

    private void testButton(View view) {
        // test button on click listener
        Button testButton = view.findViewById(R.id.test_button);
        testButton.setOnClickListener(v -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("users").get()
                    .addOnSuccessListener(querySnapshot -> {
                        WriteBatch batch = db.batch();

                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            String username = doc.getString("username");

                            if (username != null) {
                                DocumentReference docRef = doc.getReference();
                                batch.update(docRef, "usernameLowercase", username.toLowerCase());
                            }
                        }

                        // Commit the batch
                        batch.commit()
                                .addOnSuccessListener(aVoid -> Log.d("UpdateUsers", "All usernames lowercased successfully"))
                                .addOnFailureListener(e -> Log.e("UpdateUsers", "Failed to update usernames", e));
                    })
                    .addOnFailureListener(e -> Log.e("UpdateUsers", "Failed to fetch users", e));
        });
    }
}

