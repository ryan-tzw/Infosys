package com.example.infosys.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class StatusActivity extends AppCompatActivity {

    private DocumentReference documentReference;
    private FirebaseFirestore firestore;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firestore = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (userId != null) {
            documentReference = firestore.collection("users").document(userId);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (documentReference != null) {
            documentReference.update("availability", 0)
                    .addOnFailureListener(e ->
                            System.out.println("Error updating status: " + e.getMessage()));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (documentReference != null) {
            documentReference.update("availability", 1)
                    .addOnFailureListener(e ->
                            System.out.println("Error updating status: " + e.getMessage()));
        }
    }
}
