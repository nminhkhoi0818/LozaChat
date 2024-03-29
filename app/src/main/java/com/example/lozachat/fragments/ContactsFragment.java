package com.example.lozachat.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.lozachat.activities.ChatActivity;
import com.example.lozachat.adapters.ContactUsersAdapter;
import com.example.lozachat.adapters.FriendRequestsAdapter;
import com.example.lozachat.adapters.RecentConversationsAdapter;
import com.example.lozachat.databinding.FragmentContactsBinding;
import com.example.lozachat.listeners.UserListener;
import com.example.lozachat.models.ChatMessage;
import com.example.lozachat.models.FriendRequest;
import com.example.lozachat.models.User;
import com.example.lozachat.utilities.Constants;
import com.example.lozachat.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ContactsFragment extends Fragment implements UserListener {
    FragmentContactsBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private List<FriendRequest> friendRequests;
    private FriendRequestsAdapter friendRequestsAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentContactsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        preferenceManager = new PreferenceManager(getContext());
        init();
        getUsers();
        listenFriendRequests();
        return view;
    }
    private void init() {
        friendRequests = new ArrayList<>();
        friendRequestsAdapter = new FriendRequestsAdapter(friendRequests);
        binding.friendRequestsRecyclerView.setAdapter(friendRequestsAdapter);
        database = FirebaseFirestore.getInstance();
    }
    private void getUsers() {
        loading(true);
        database.collection(Constants.KEY_COLLECTION_USERS)
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
                        if (users.size() > 0) {
                            ContactUsersAdapter contactUsersAdapter = new ContactUsersAdapter(users, this);
                            binding.usersRecyclerView.setAdapter(contactUsersAdapter);
                            binding.usersRecyclerView.setVisibility(View.VISIBLE);
                        } else {
                            showErrorMessage();
                        }
                    } else {
                        showErrorMessage();
                    }
                });
    }

    private void listenFriendRequests() {
        database.collection(Constants.KEY_COLLECTION_FRIEND_REQUESTS)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange: value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    FriendRequest friendRequest = new FriendRequest();
                    friendRequest.senderId = senderId;
                    friendRequest.receiverId = receiverId;
                    if (preferenceManager.getString(Constants.KEY_USER_ID).equals(receiverId)) {
                        friendRequest.senderImage = documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE);
                        friendRequest.senderName = documentChange.getDocument().getString(Constants.KEY_SENDER_NAME);
                        friendRequest.senderEmail = documentChange.getDocument().getString(Constants.KEY_SENDER_EMAIL);
                        friendRequests.add(friendRequest);
                    }
                }
            }
//            friendRequests.sort(Comparator.comparing(obj -> obj.dateObject));
            friendRequestsAdapter.notifyDataSetChanged();
            binding.friendRequestsRecyclerView.smoothScrollToPosition(0);
            binding.friendRequestsRecyclerView.setVisibility(View.VISIBLE);
//            binding.progressBar.setVisibility(View.GONE);
        }
    };

    private void showErrorMessage() {
        binding.textErrorMessage.setText(String.format("%s", "No user available"));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
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
        Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
    }
}
