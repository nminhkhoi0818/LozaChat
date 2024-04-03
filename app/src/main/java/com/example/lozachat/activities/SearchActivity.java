package com.example.lozachat.activities;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lozachat.adapters.AddFriendAdapter;
import com.example.lozachat.adapters.ContactUsersAdapter;
import com.example.lozachat.databinding.ActivitySearchUserBinding;
import com.example.lozachat.listeners.UserListener;
import com.example.lozachat.models.User;
import com.example.lozachat.utilities.Constants;
import com.example.lozachat.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements UserListener {
    ActivitySearchUserBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private AddFriendAdapter addFriendAdapter;
    private List<User> users;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        init();
        setListeners();
        binding.searchText.requestFocus();
    }

    private void init() {
        database = FirebaseFirestore.getInstance();
        users = new ArrayList<>();
        addFriendAdapter = new AddFriendAdapter(users, SearchActivity.this);
        binding.searchRecyclerView.setAdapter(addFriendAdapter);
    }

    private void setListeners() {
        binding.searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                database.collection(Constants.KEY_COLLECTION_USERS)
                        .whereEqualTo(Constants.KEY_EMAIL, s.toString())
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                loading(false);
                                String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                                if (task.isSuccessful()) {
                                    users.clear();
                                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                        if (currentUserId.equals(queryDocumentSnapshot.getId())) {
                                            continue;
                                        }
                                        User user = new User();
                                        user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                                        user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                                        user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                                        user.id = queryDocumentSnapshot.getId();
                                        ArrayList<String> currentFriends = preferenceManager.getArrayList(Constants.KEY_FRIENDS_LIST);
                                        user.is_friend = currentFriends.contains(user.id);
                                        // Handle logout using token tutorial 5
                                        // user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                                        users.add(user);
                                    }
                                    addFriendAdapter.notifyDataSetChanged();
                                    binding.searchRecyclerView.setVisibility(View.VISIBLE);
                                    binding.searchRecyclerView.smoothScrollToPosition(0);
                                } else { }
                            }
                        });
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
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

    @Override
    public void onUserClicked(User user) {
        HashMap<String, Object> friendRequest = new HashMap<>();
        friendRequest.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        friendRequest.put(Constants.KEY_RECEIVER_ID, user.id);
        friendRequest.put(Constants.KEY_SENDER_EMAIL, preferenceManager.getString(Constants.KEY_EMAIL));
        friendRequest.put(Constants.KEY_SENDER_NAME, preferenceManager.getString(Constants.KEY_NAME));
        friendRequest.put(Constants.KEY_SENDER_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
        friendRequest.put(Constants.KEY_TIMESTAMP, new Date());
        database.collection(Constants.KEY_COLLECTION_FRIEND_REQUESTS).add(friendRequest);
    }
}
