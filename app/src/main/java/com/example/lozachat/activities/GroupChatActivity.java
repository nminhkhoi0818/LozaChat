package com.example.lozachat.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.example.lozachat.adapters.ChatAdapter;
import com.example.lozachat.adapters.GroupChatAdapter;
import com.example.lozachat.databinding.ActivityChatBinding;
import com.example.lozachat.listeners.ChatListener;
import com.example.lozachat.models.ChatMessage;
import com.example.lozachat.models.Group;
import com.example.lozachat.models.User;
import com.example.lozachat.utilities.Constants;
import com.example.lozachat.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class GroupChatActivity extends BaseActivity implements ChatListener {
    private ActivityChatBinding binding;
    private Group group;
    private List<ChatMessage> chatMessages;
    private GroupChatAdapter groupChatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private Boolean isReceiverAvailable = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        init();
    }
    private void init() {
        group = (Group) getIntent().getSerializableExtra(Constants.KEY_GROUP);
        binding.textName.setText(group.name);
        group.membersImage = new HashMap<>();
        chatMessages = new ArrayList<>();
        preferenceManager = new PreferenceManager(getApplicationContext());
        database = FirebaseFirestore.getInstance();
        retrieveMembersImage();
    }

    private void retrieveMembersImage() {
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereIn(FieldPath.documentId(), group.members)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                        HashMap<String, String> membersImages = new HashMap<>(), membersName = new HashMap<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {;
                            membersImages.put(document.getId(), document.getString(Constants.KEY_IMAGE));
                            membersName.put(document.getId(), document.getString(Constants.KEY_NAME));
                        }
                        group.membersImage = membersImages;
                        group.membersName = membersName;
                        groupChatAdapter = new GroupChatAdapter(
                                chatMessages,
                                group.membersImage,
                                group.membersName,
                                preferenceManager.getString(Constants.KEY_USER_ID),
                                this
                        );
                        binding.chatRecycleView.setAdapter(groupChatAdapter);
                        listenMessages();
                    } else { }
                });
    }

    private void sendMessage() {
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID, group.id);
        message.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString());
        message.put(Constants.KEY_TIMESTAMP, new Date());
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_GROUP).document(group.id);
        documentReference.update(
                Constants.KEY_LAST_MESSAGE, binding.inputMessage.getText().toString(),
                Constants.KEY_LAST_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID),
                Constants.KEY_LAST_SENDER_NAME, preferenceManager.getString(Constants.KEY_NAME),
                Constants.KEY_TIMESTAMP, new Date()
        );
        binding.inputMessage.setText(null);
    }
//    private void listenAvailabilityOfReceiver() {
//        database.collection(Constants.KEY_COLLECTION_USERS).document(
//                receiverUser.id
//        ).addSnapshotListener(GroupChatActivity.this, (value, error) -> {
//            if (error != null) {
//                return;
//            }
//            if (value != null) {
//                if (value.getLong(Constants.KEY_AVAILABILITY) != null) {
//                    int availability = Objects.requireNonNull(
//                            value.getLong(Constants.KEY_AVAILABILITY)
//                    ).intValue();
//                    isReceiverAvailable = availability == 1;
//                }
//            }
//            if (isReceiverAvailable) {
//                binding.textAvailability.setVisibility(View.VISIBLE);
//            } else {
//                binding.textAvailability.setVisibility(View.GONE);
//            }
//        });
//    }
    private void listenMessages() {
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, group.id)
                .addSnapshotListener(eventListener);
    }
    @SuppressLint("NotifyDataSetChanged")
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            int count = chatMessages.size();
            for (DocumentChange documentChange: value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.chatId = documentChange.getDocument().getId();
                    chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatMessage.receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.dateTime = getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    chatMessages.add(chatMessage);
                } else if (documentChange.getType() == DocumentChange.Type.REMOVED) {
                    for (int i = 0; i < chatMessages.size(); ++i) {
                        if (chatMessages.get(i).chatId.equals(documentChange.getDocument().getId())) {
                            chatMessages.remove(i);
                            groupChatAdapter.notifyItemRemoved(i);
                            return;
                        }
                    }
                    return;
                }
            }
            chatMessages.sort(Comparator.comparing(obj -> obj.dateObject));
            if (count == 0) {
                groupChatAdapter.notifyDataSetChanged();
            } else {
                groupChatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
                binding.chatRecycleView.smoothScrollToPosition(chatMessages.size() - 1);
            }
            binding.chatRecycleView.setVisibility(View.VISIBLE);
        }
        binding.progressBar.setVisibility(View.GONE);
    };
    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> finish());
        binding.layoutSend.setOnClickListener(v -> sendMessage());
    }
    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }
    @Override
    protected void onResume() {
        super.onResume();
//        listenAvailabilityOfReceiver();
    }

    @Override
    public void OnChatDelete(ChatMessage chatMessage) {
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_CHAT).document(chatMessage.chatId);
        documentReference.delete();
        DocumentReference groupReference =
                database.collection(Constants.KEY_COLLECTION_GROUP).document(group.id);
        if (groupChatAdapter.getItemCount() > 0) {
            ChatMessage prevChatMessage = groupChatAdapter.getItem(groupChatAdapter.getItemCount() - 1);
            groupReference.update(
                    Constants.KEY_LAST_MESSAGE, prevChatMessage.message,
                    Constants.KEY_LAST_SENDER_ID, prevChatMessage.senderId,
                    Constants.KEY_LAST_SENDER_NAME, groupChatAdapter.getMemberName(groupChatAdapter.getItemCount() - 1),
                    Constants.KEY_TIMESTAMP, prevChatMessage.dateObject
            );
        } else {
            groupReference.update(
                    Constants.KEY_LAST_MESSAGE, "",
                    Constants.KEY_LAST_SENDER_ID, "",
                    Constants.KEY_LAST_SENDER_NAME, ""
            );
        }
    }
}
