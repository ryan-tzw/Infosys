package com.example.infosys.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.infosys.model.Post;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class PostListViewModel extends ViewModel {
    private final MutableLiveData<List<Post>> posts = new MutableLiveData<>(new ArrayList<>());
    private DocumentSnapshot lastVisibleSnapshot = null;
    private boolean isLastPage = false;

    public LiveData<List<Post>> getPosts() {
        return posts;
    }

    public void appendPosts(List<Post> newPosts) {
        List<Post> current = new ArrayList<>(posts.getValue());
        current.addAll(newPosts);
        posts.setValue(current);
    }

    public void clearPosts() {
        posts.setValue(new ArrayList<>());
    }

    public DocumentSnapshot getLastVisibleSnapshot() {
        return lastVisibleSnapshot;
    }

    public void setLastVisibleSnapshot(DocumentSnapshot snapshot) {
        this.lastVisibleSnapshot = snapshot;
    }

    public void setIsLastPage(boolean isLastPage) {
        this.isLastPage = isLastPage;
    }

    public boolean isLastPage() {
        return isLastPage;
    }
}

