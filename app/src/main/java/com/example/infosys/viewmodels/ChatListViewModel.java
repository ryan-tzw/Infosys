package com.example.infosys.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatListViewModel extends ViewModel {

    private final Map<String, Boolean> userAvailabilityMap = new HashMap<>();
    private final MutableLiveData<Map<String, Boolean>> availabilityLiveData = new MutableLiveData<>();
    private final Map<String, ListenerRegistration> listeners = new HashMap<>();

    public LiveData<Map<String, Boolean>> getAvailabilityLiveData() {
        return availabilityLiveData;
    }

    public void observeAvailability(List<String> userIds) {
        for (String userId : userIds) {
            if (listeners.containsKey(userId)) continue;

            ListenerRegistration reg = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .addSnapshotListener((snapshot, e) -> {
                        if (e != null || snapshot == null || !snapshot.exists()) return;

                        Boolean available = snapshot.getBoolean("available");
                        userAvailabilityMap.put(userId, available != null && available);
                        availabilityLiveData.postValue(new HashMap<>(userAvailabilityMap));
                    });

            listeners.put(userId, reg);
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        for (ListenerRegistration reg : listeners.values()) {
            reg.remove();
        }
        listeners.clear();
    }
}
