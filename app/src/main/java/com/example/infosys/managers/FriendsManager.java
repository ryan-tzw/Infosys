//package com.example.infosys.managers;
//
//import android.util.Log;
//
//import com.example.infosys.model.Chat;
//import com.example.infosys.model.User;
//import com.example.infosys.utils.FirebaseUtil;
//import com.google.firebase.Timestamp;
//import com.google.firebase.firestore.DocumentSnapshot;
//import com.google.firebase.firestore.FieldValue;
//import com.google.firebase.firestore.FirebaseFirestore;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class FriendsManager {
//    private static final String TAG = "FriendsManager";
//
//    private static FriendsManager instance;
//    private final FirebaseFirestore db;
//
//    private FriendsManager() {
//        db = FirebaseFirestore.getInstance();
//    }
//
//    public static synchronized FriendsManager getInstance() {
//        if (instance == null) {
//            instance = new FriendsManager();
//        }
//        return instance;
//    }
//
//    public void addFriend(String currentUserId, String friendUserId, final FriendsCallback callback) {
//        db.collection("users").document(currentUserId)
//                .update("friends", FieldValue.arrayUnion(friendUserId))
//                .addOnSuccessListener(aVoid -> {
//                    // Add the current user to the friend's friend list
//                    db.collection("users").document(friendUserId)
//                            .update("friends", FieldValue.arrayUnion(currentUserId))
//                            .addOnSuccessListener(aVoid2 -> callback.onSuccess())
//                            .addOnFailureListener(callback::onError);
//                })
//                .addOnFailureListener(callback::onError);
//    }
//
//    public void removeFriend(String currentUserId, String friendUserId, final FriendsCallback callback) {
//        db.collection("users").document(currentUserId)
//                .update("friends", FieldValue.arrayRemove(friendUserId))
//                .addOnSuccessListener(aVoid -> {
//                    // Remove the current user from the friend's friend list
//                    db.collection("users").document(friendUserId)
//                            .update("friends", FieldValue.arrayRemove(currentUserId))
//                            .addOnSuccessListener(aVoid2 -> callback.onSuccess())
//                            .addOnFailureListener(callback::onError);
//                })
//                .addOnFailureListener(callback::onError);
//    }
//
//    public void getConversationList(String currentUserId, final ConversationCallback callback) {
//        getFriendsList(currentUserId, new FriendsListCallback() {
//            @Override
//            public void onFriendsListReceived(List<User> friendsList) {
//                fetchConversationDetails(friendsList, callback);
//            }
//
//            @Override
//            public void onError(Exception e) {
//                callback.onError(e);
//            }
//        });
//    }
//
//    private void fetchConversationDetails(List<User> friendsList, final ConversationCallback callback) {
//        List<Chat> chats = new ArrayList<>();
//        String currentUserId = FirebaseUtil.getCurrentUserUid();
//
//        for (User friend : friendsList) {
//            assert currentUserId != null;
//            String conversationId = ChatManager.generateChatId(currentUserId, friend.getUid());
//
//            db.collection("chats").document(conversationId)
//                    .get()
//                    .addOnSuccessListener(documentSnapshot -> {
//                        if (documentSnapshot.exists()) {
//                            String lastMessage = documentSnapshot.getString("last_message");
//                            Timestamp lastUpdated = documentSnapshot.getTimestamp("last_updated");
//                            chats.add(new Chat(friend, lastMessage, lastUpdated));
//                        } else {
//                            chats.add(new Chat(friend, "Say hi!", Timestamp.now()));
//                        }
//                        if (chats.size() == friendsList.size()) {
//                            chats.sort((c1, c2) -> {
//                                // Compare by seconds first, and if they're equal, compare by nanoseconds
//                                int secondsComparison = Long.compare(c2.getLastUpdated().getSeconds(), c1.getLastUpdated().getSeconds());
//                                if (secondsComparison != 0) {
//                                    return secondsComparison;
//                                }
//                                // If the seconds are the same, compare nanoseconds
//                                return Integer.compare(c2.getLastUpdated().getNanoseconds(), c1.getLastUpdated().getNanoseconds());
//
//                            });
//                            callback.onConversationsReceived(chats);
//                        }
//                    })
//                    .addOnFailureListener(callback::onError);
//        }
//    }
//
//    public void getFriendsList(String currentUserId, final FriendsListCallback callback) {
//        db.collection("users").document(currentUserId)
//                .get()
//                .addOnSuccessListener(documentSnapshot -> {
//                    if (documentSnapshot.exists()) {
//                        Log.d(TAG, "getFriendsList: " + documentSnapshot.getData());
//                        List<String> friendsList = (List<String>) documentSnapshot.get("friends");
//                        if (friendsList != null && !friendsList.isEmpty()) {
//                            fetchFriendDetails(friendsList, callback);
//                        } else {
//                            callback.onError(new Exception("No friends found."));
//                        }
//                    } else {
//                        callback.onError(new Exception("No friends found."));
//                    }
//                })
//                .addOnFailureListener(callback::onError);
//    }
//
//    private void fetchFriendDetails(List<String> friendsList, final FriendsListCallback callback) {
//        Log.d(TAG, "fetchFriendDetails: Start" + friendsList.size());
//        db.collection("users").whereIn("uid", friendsList)
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    Log.d(TAG, "fetchFriendDetails: onSuccess: " + queryDocumentSnapshots.size());
//                    List<User> friends = new ArrayList<>();
//                    for (DocumentSnapshot document : queryDocumentSnapshots) {
//                        String userId = document.getId();
//                        String username = document.getString("username");
//                        String profilePic = document.getString("profilePic");
//                        friends.add(new User(userId, username, profilePic));
//                    }
//                    callback.onFriendsListReceived(friends);
//                })
//                .addOnFailureListener(callback::onError);
//    }
//
//    public interface ConversationCallback {
//        void onConversationsReceived(List<Chat> chats);
//
//        void onError(Exception e);
//    }
//
//    // Callback for friend operations
//    public interface FriendsCallback {
//        void onSuccess();
//
//        void onError(Exception e);
//    }
//
//    // Callback for fetching friends list
//    public interface FriendsListCallback {
//        void onFriendsListReceived(List<User> friends);
//
//        void onError(Exception e);
//    }
//}
