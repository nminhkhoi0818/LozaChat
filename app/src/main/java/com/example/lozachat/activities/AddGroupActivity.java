package com.example.lozachat.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lozachat.adapters.AddGroupAdapter;
import com.example.lozachat.databinding.ActivityAddGroupBinding;
import com.example.lozachat.databinding.ActivitySearchUserBinding;
import com.example.lozachat.models.User;
import com.example.lozachat.utilities.Constants;
import com.example.lozachat.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class AddGroupActivity extends AppCompatActivity {
    ActivityAddGroupBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        init();
        getUsers();
        setListeners();
    }

    private void init() {
        database = FirebaseFirestore.getInstance();
    }

    private void getUsers() {
        loading(true);
        if (preferenceManager.getArrayList(Constants.KEY_FRIENDS_LIST).isEmpty()) {
            loading(false);
            return;
        }
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereIn(FieldPath.documentId(), preferenceManager.getArrayList(Constants.KEY_FRIENDS_LIST))
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);

                    if (task.isSuccessful() && task.getResult() != null) {
                        List<User> users = new ArrayList<>();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            if (currentUserId.equals(queryDocumentSnapshot.getId())) {
                                continue;
                            }
                            User user = new User();
                            user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                            user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                            user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                            user.id = queryDocumentSnapshot.getId();
                            // Handle logout using token tutorial 5
                            // user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            users.add(user);
                        }
                        Log.d("zzz", users.toString());
                        if (users.size() > 0) {
                            AddGroupAdapter contactUsersAdapter = new AddGroupAdapter(users);
                            binding.usersRecyclerView.setAdapter(contactUsersAdapter);
                            binding.usersRecyclerView.setVisibility(View.VISIBLE);
                        }
//                        else {
//                            showErrorMessage();
//                        }
                    }
                });
    }

    private void setListeners() {
        binding.backBtn.setOnClickListener(v -> {
            finish();
        });
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }
}
