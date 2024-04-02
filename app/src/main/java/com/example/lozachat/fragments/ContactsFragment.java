package com.example.lozachat.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.lozachat.activities.ChatActivity;
import com.example.lozachat.activities.MainActivity;
import com.example.lozachat.activities.SearchActivity;
import com.example.lozachat.adapters.ContactUsersAdapter;
import com.example.lozachat.adapters.FriendRequestsAdapter;
import com.example.lozachat.adapters.RecentConversationsAdapter;
import com.example.lozachat.databinding.FragmentContactsBinding;
import com.example.lozachat.listeners.FriendRequestListener;
import com.example.lozachat.listeners.UserListener;
import com.example.lozachat.models.ChatMessage;
import com.example.lozachat.models.FriendRequest;
import com.example.lozachat.models.User;
import com.example.lozachat.utilities.Constants;
import com.example.lozachat.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class ContactsFragment extends Fragment implements UserListener, FriendRequestListener {
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
        setListeners();
        return view;
    }
    private void init() {
        friendRequests = new ArrayList<>();
        friendRequestsAdapter = new FriendRequestsAdapter(friendRequests, this);
        binding.friendRequestsRecyclerView.setAdapter(friendRequestsAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void setListeners() {
        binding.searchButton.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), SearchActivity.class);
            startActivity(intent);
        });
    }

    private void getUsers() {
        if (preferenceManager.getArrayList(Constants.KEY_FRIENDS_LIST).isEmpty()) {
            loading(false);
            return;
        }
        loading(true);
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
                    friendRequest.id = documentChange.getDocument().getId();
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

    @Override
    public void OnAccept(FriendRequest friendRequest) {
        DocumentReference senderReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(friendRequest.senderId);
        DocumentReference receiverReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(friendRequest.receiverId);
        senderReference.update(Constants.KEY_FRIENDS_LIST, FieldValue.arrayUnion(friendRequest.receiverId));
        receiverReference.update(Constants.KEY_FRIENDS_LIST, FieldValue.arrayUnion(friendRequest.senderId));
        database.collection(Constants.KEY_COLLECTION_FRIEND_REQUESTS).document(friendRequest.id).delete();

    }

    @Override
    public void OnDeny(FriendRequest friendRequest) {
        database.collection(Constants.KEY_COLLECTION_FRIEND_REQUESTS).document(friendRequest.id).delete();
    }
}
