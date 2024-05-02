package com.example.lozachat.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.lozachat.activities.AddGroupActivity;
import com.example.lozachat.activities.ChatActivity;
import com.example.lozachat.activities.GroupChatActivity;
import com.example.lozachat.adapters.GroupsAdapter;
import com.example.lozachat.adapters.RecentConversationsAdapter;
import com.example.lozachat.databinding.FragmentMessageBinding;
import com.example.lozachat.listeners.ConversationListener;
import com.example.lozachat.listeners.GroupListener;
import com.example.lozachat.models.ChatMessage;
import com.example.lozachat.models.Group;
import com.example.lozachat.models.User;
import com.example.lozachat.utilities.Constants;
import com.example.lozachat.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class MessageFragment extends Fragment implements ConversationListener, GroupListener {
    private FragmentMessageBinding binding;
    private PreferenceManager preferenceManager;
    private List<ChatMessage> conversations;
    private List<Group> groups;
    private RecentConversationsAdapter conversationsAdapter;
    private GroupsAdapter groupsAdapter;
    private FirebaseFirestore database;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMessageBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        preferenceManager = new PreferenceManager(getContext());
        init();
        listenConversations();
        setListeners();
        return view;
    }

    private void init() {
        conversations = new ArrayList<>();
        groups = new ArrayList<>();
        conversationsAdapter = new RecentConversationsAdapter(conversations, this);
        groupsAdapter = new GroupsAdapter(groups, this);
        binding.conversationRecyclerView.setAdapter(conversationsAdapter);
        binding.groupRecyclerView.setAdapter(groupsAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void listenConversations() {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_GROUP)
                .whereArrayContains(Constants.KEY_MEMBERS, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener2);
    }

    private void setListeners() {
        binding.addGroupButton.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AddGroupActivity.class);
            startActivity(intent);
        });
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
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.chatId = documentChange.getDocument().getId();
                    Boolean flag = false;
                    for (int i = 0; i < conversations.size(); ++i) {
                        if (conversations.get(i).chatId.equals(chatMessage.chatId)) {
                            flag = true;
                            break;
                        }
                    }
                    if (flag) continue;
                    chatMessage.senderId = senderId;
                    chatMessage.receiverId = receiverId;
                    if (preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId)) {
                        chatMessage.conversationImage = documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE);
                        chatMessage.conversationName = documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME);
                        chatMessage.conversationId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    } else {
                        chatMessage.conversationImage = documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE);
                        chatMessage.conversationName = documentChange.getDocument().getString(Constants.KEY_SENDER_NAME);
                        chatMessage.conversationId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    }

                    if (!preferenceManager.getString(Constants.KEY_USER_ID).equals(documentChange.getDocument().getString(Constants.KEY_LAST_SENDER_ID))) {
                        chatMessage.seen = documentChange.getDocument().getBoolean(Constants.KEY_SEEN);
                    } else {
                        chatMessage.seen = true;
                    }
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                    chatMessage.dateObject =  documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    database.collection(Constants.KEY_MUTE_STATUS)
                                    .whereEqualTo(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID)).whereArrayContains(Constants.KEY_MUTED, chatMessage.conversationId).get()
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful() && task.getResult().isEmpty()) {
                                            chatMessage.muted = false;
                                        }
                                        else {
                                            chatMessage.muted = true;
                                        }
                                        conversations.add(chatMessage);
                                        conversations.sort(Collections.reverseOrder(Comparator.comparing(obj -> obj.dateObject)));
                                        conversationsAdapter.notifyDataSetChanged();
                                        binding.conversationRecyclerView.smoothScrollToPosition(0);
                                        binding.conversationRecyclerView.setVisibility(View.VISIBLE);
                                        binding.progressBar.setVisibility(View.GONE);
                                    });

                } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    for (int i = 0; i < conversations.size(); ++i) {
                        String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        if (conversations.get(i).senderId.equals(senderId) && conversations.get(i).receiverId.equals(receiverId)) {
                            conversations.get(i).message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                            conversations.get(i).dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                            if (!preferenceManager.getString(Constants.KEY_USER_ID).equals(documentChange.getDocument().getString(Constants.KEY_LAST_SENDER_ID))) {
                                conversations.get(i).seen = documentChange.getDocument().getBoolean(Constants.KEY_SEEN);
                            } else {
                                conversations.get(i).seen = true;
                            }
                            break;
                        }
                    }
                } else if (documentChange.getType() == DocumentChange.Type.REMOVED) {
                    for (int i = 0; i < conversations.size(); ++i) {
                        String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        if (conversations.get(i).senderId.equals(senderId) && conversations.get(i).receiverId.equals(receiverId)) {
                            conversations.remove(i);
                            conversationsAdapter.notifyItemRemoved(i);
                            break;
                        }
                    }
                }
            }
            conversations.sort(Collections.reverseOrder(Comparator.comparing(obj -> obj.dateObject)));
            conversationsAdapter.notifyDataSetChanged();
            binding.conversationRecyclerView.smoothScrollToPosition(0);
            binding.conversationRecyclerView.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
        }
    };
    private final EventListener<QuerySnapshot> eventListener2 = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange: value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    Group group = new Group();
                    group.id = documentChange.getDocument().getId();
                    Boolean flag = false;
                    for (int i = 0; i < groups.size(); ++i) {
                        if (groups.get(i).id.equals(group.id)) {
                            flag = true;
                            break;
                        }
                    }
                    if (flag) break;
                    group.image = documentChange.getDocument().getString(Constants.KEY_IMAGE);
                    group.name = documentChange.getDocument().getString(Constants.KEY_NAME);
                    group.members = (ArrayList<String>) documentChange.getDocument().get(Constants.KEY_MEMBERS);
                    group.lastMessage =  documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                    group.lastSenderId =  documentChange.getDocument().getString(Constants.KEY_LAST_SENDER_ID);
                    group.lastSenderName =  documentChange.getDocument().getString(Constants.KEY_LAST_SENDER_NAME);
                    if (preferenceManager.getString(Constants.KEY_USER_ID).equals(group.lastSenderId)) {
                        group.seen = true;
                    } else {
                        group.seen = documentChange.getDocument().getBoolean(Constants.KEY_SEEN);
                    }
                    group.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    groups.add(group);
                } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    for (int i = 0; i < groups.size(); ++i) {
                        if (groups.get(i).id.equals(documentChange.getDocument().getId())) {
                            groups.get(i).name = documentChange.getDocument().getString(Constants.KEY_NAME);
                            groups.get(i).lastMessage =  documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                            groups.get(i).lastSenderId =  documentChange.getDocument().getString(Constants.KEY_LAST_SENDER_ID);
                            groups.get(i).lastSenderName =  documentChange.getDocument().getString(Constants.KEY_LAST_SENDER_NAME);
                            groups.get(i).dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                            if (preferenceManager.getString(Constants.KEY_USER_ID).equals(groups.get(i).lastSenderId)) {
                                groups.get(i).seen = true;
                            } else {
                                groups.get(i).seen = documentChange.getDocument().getBoolean(Constants.KEY_SEEN);
                            }
                            break;
                        }
                    }
                }
            }
            groups.sort(Collections.reverseOrder(Comparator.comparing(obj -> obj.dateObject)));
            groupsAdapter.notifyDataSetChanged();
            binding.groupRecyclerView.smoothScrollToPosition(0);
            binding.groupRecyclerView.setVisibility(View.VISIBLE);
            binding.progressBar2.setVisibility(View.GONE);
        }
    };

    @Override
    public void OnConversationClicked(User user) {
        Intent intent = new Intent(getContext(), ChatActivity.class);
        User new_user = new User(user);
        new_user.image = "";
        intent.putExtra(Constants.KEY_USER, new_user);
        startActivity(intent);
    }

    @Override
    public void OnMuteClicked(User user) {
        database.collection(Constants.KEY_MUTE_STATUS)
                .whereEqualTo(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .get()
                .addOnCompleteListener(task -> {
                   if (task.isSuccessful()) {
                       if (task.getResult().isEmpty()) {
                           HashMap<String, Object> muted = new HashMap<>();
                           muted.put(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                           ArrayList<String> users = new ArrayList<>();
                           users.add(user.id);
                           muted.put(Constants.KEY_MUTED, users);
                           database.collection(Constants.KEY_MUTE_STATUS).add(muted);
                       } else {
                           DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                           DocumentReference documentReference = database.collection(Constants.KEY_MUTE_STATUS).document(documentSnapshot.getId());
                           documentReference.update(Constants.KEY_MUTED, FieldValue.arrayUnion(user.id));
                       }
                   }
                });

    }

    @Override
    public void onGroupClicked(Group group) {
        Intent intent = new Intent(getContext(), GroupChatActivity.class);
        Group new_group = new Group(group);
        new_group.image = "";
        intent.putExtra(Constants.KEY_GROUP, new_group);
        startActivity(intent);
    }
}