package com.example.infosys.fragments.main;

import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.infosys.R;
import com.example.infosys.activities.ChatActivity;
import com.example.infosys.activities.PostActivity;
import com.example.infosys.adapters.NotificationsAdapter;
import com.example.infosys.fragments.main.common.BaseFragment;
import com.example.infosys.managers.NotificationsManager;
import com.example.infosys.model.Notification;
import com.example.infosys.utils.FirebaseUtil;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;


public class NotificationsFragment extends BaseFragment {
    private static final String TAG = "NotificationsFragment";
    private RecyclerView recyclerView;
    private NotificationsAdapter adapter;
    private List<Notification> notifications = new ArrayList<>();
    private FirebaseFirestore db;
    private String currentUserId;
    private int unreadCount = 0;
    private BottomNavigationView bottomNavigationView;

    public NotificationsFragment() {
        // Required empty public constructor
    }

    public static NotificationsFragment newInstance() {
        NotificationsFragment fragment = new NotificationsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        recyclerView = view.findViewById(R.id.notifications_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new NotificationsAdapter(notifications, getContext(), notification -> {
            handleNotificationClick(notification);
        });

        recyclerView.setAdapter(adapter);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();
        loadNotifications();

        setupSwipeToDelete();

        bottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation);

        ImageButton btnMarkAllAsRead = view.findViewById(R.id.mark_all_as_read_button);
        btnMarkAllAsRead.setOnClickListener(v -> markAllAsRead());

        return view;
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                Notification swipedNotification = notifications.get(position);
                String userId = FirebaseUtil.getCurrentUserUid();
                assert userId != null;

                if (direction == ItemTouchHelper.LEFT) {
                    Log.d(TAG, "onSwiped: Mark notification as read");
                    // Swipe left: Mark as read
                    NotificationsManager.getInstance().markNotificationReadStatus(userId, swipedNotification.getId(), !swipedNotification.isRead())
                            .addOnSuccessListener(a -> {
                                Log.d(TAG, "onSwiped: Marked notification as read");
                                swipedNotification.setRead(!swipedNotification.isRead());
                                adapter.notifyItemChanged(position);
                            });
                }

                if (direction == ItemTouchHelper.RIGHT) {
                    Log.d(TAG, "onSwiped: Delete notification");
                    db.collection("users")
                            .document(currentUserId)
                            .collection("notifications")
                            .document(swipedNotification.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Deleted notification");

                                Snackbar.make(recyclerView, "Notification deleted", Snackbar.LENGTH_LONG)
                                        .setAction("Undo", v -> {
                                            notifications.add(position, swipedNotification);
                                            adapter.notifyItemInserted(position);
                                            db.collection("users")
                                                    .document(currentUserId)
                                                    .collection("notifications")
                                                    .document(swipedNotification.getId())
                                                    .set(swipedNotification);
                                        }).show();
                            })
                            .addOnFailureListener(e -> Log.e(TAG, "Error deleting", e));
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {
                int position = viewHolder.getBindingAdapterPosition();
                Notification swipedNotification = notifications.get(position);

                RecyclerViewSwipeDecorator.Builder builder = new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                if (dX < 0) {
                    builder.addSwipeLeftLabel("Mark as " + (swipedNotification.isRead() ? "unread" : "read"));
                } else {
                    builder.addSwipeRightLabel("Delete");
                }

                if (swipedNotification.isRead()) {
                    builder
                            .addSwipeLeftBackgroundColor(ContextCompat.getColor(requireContext(), R.color.tertiaryColor))
                            .addSwipeLeftActionIcon(R.drawable.ic_mark_unread);
                } else {
                    builder
                            .addSwipeLeftBackgroundColor(ContextCompat.getColor(requireContext(), R.color.secondaryColor))
                            .addSwipeLeftActionIcon(R.drawable.ic_mark_read);
                }

                builder
                        .addSwipeRightBackgroundColor(ContextCompat.getColor(requireContext(), R.color.red))
                        .addSwipeRightActionIcon(R.drawable.ic_delete)
                        .create().decorate();

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);

    }

    private void loadNotifications() {
        db.collection("users")
                .document(currentUserId)
                .collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;

                    notifications.clear();
                    unreadCount = 0;
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Notification notification = doc.toObject(Notification.class);
                        if (notification != null) {
                            Log.d(TAG, "loadNotifications: notif read:" + notification.isRead());
                            notifications.add(notification);
                            if (!notification.isRead()) unreadCount++;
                        }
                    }
                    adapter.notifyDataSetChanged();

                    // Update badge
                    BadgeDrawable badge = bottomNavigationView.getOrCreateBadge(R.id.nav_notifications);
                    badge.setVisible(unreadCount > 0);
                    badge.setNumber(unreadCount);
                });


    }


    private void markAllAsRead() {
        WriteBatch batch = db.batch();

        for (Notification notification : notifications) {
            if (!notification.isRead()) {
                DocumentReference ref = db.collection("users")
                        .document(currentUserId)
                        .collection("notifications")
                        .document(notification.getId());
                batch.update(ref, "read", true);
            }
        }

        batch.commit().addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Marked all as read", Toast.LENGTH_SHORT).show();
                    adapter.notifyDataSetChanged();
                }
        );
    }


    private void handleNotificationClick(Notification notification) {
        if (notification.getType().equals("post")) {
            openChat(notification.getChatId());
        } else if (notification.getType().equals("message")) {
            openPostFromNotification(notification.getPostId(), notification.getCommunityId(), notification.getCommunityName());
        }

        // Mark as read
        db.collection("users")
                .document(currentUserId)
                .collection("notifications")
                .document(notification.getId())
                .update("read", true);
    }

    private void openChat(String chatId) {
        Intent chatIntent = new Intent(requireContext(), ChatActivity.class);
        chatIntent.putExtra("chatId", chatId);
        startActivity(chatIntent);
    }

    private void openPostFromNotification(String postId, String communityId, String communityName) {
        Intent postIntent = new Intent(requireContext(), PostActivity.class);
        postIntent.putExtra("postId", postId);
        postIntent.putExtra("communityId", communityId);
        postIntent.putExtra("communityName", communityName);
        startActivity(postIntent);
    }
}