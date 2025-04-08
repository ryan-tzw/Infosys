package com.example.infosys.fragments.main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.infosys.R;
import com.example.infosys.fragments.main.common.BaseFragment;
import com.example.infosys.model.Notification;
import com.example.infosys.utils.FirebaseUtil;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


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
            String currentUserId = FirebaseUtil.getCurrentUserUid();

            assert currentUserId != null;
            CollectionReference notificationRef = db.collection("users").document(currentUserId).collection("notifications");

            List<Notification> testNotifications = new ArrayList<>();
            testNotifications.add(new Notification(
                    UUID.randomUUID().toString(),
                    "Alex",
                    "New post in Community",
                    "Check out Alex's latest post on UI design trends!",
                    "https://i.pravatar.cc/150?img=7",
                    "post",
                    "post123",
                    Timestamp.now()
            ));
            testNotifications.add(new Notification(
                    UUID.randomUUID().toString(),
                    "Jamie",
                    "New message from Jamie",
                    "Hey, are you free to review my PR today?",
                    "https://i.pravatar.cc/150?img=8",
                    "message",
                    "chat456",
                    Timestamp.now()
            ));
            testNotifications.add(new Notification(
                    UUID.randomUUID().toString(),
                    "Casey",
                    "New post in Community",
                    "Casey just shared a tutorial on Android RecyclerViews!",
                    "https://i.pravatar.cc/150?img=9",
                    "post",
                    "post789",
                    Timestamp.now()
            ));

            for (Notification n : testNotifications) {
                notificationRef.document(n.getId()).set(n)
                        .addOnSuccessListener(aVoid -> Log.d("TEST_NOTIF", "✅ Added notification: " + n.getTitle()))
                        .addOnFailureListener(e -> Log.e("TEST_NOTIF", "❌ Failed to add notification", e));
            }

        });
    }
}

