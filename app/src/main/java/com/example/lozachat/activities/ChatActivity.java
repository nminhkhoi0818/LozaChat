package com.example.lozachat.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.lozachat.adapters.ChatAdapter;
import com.example.lozachat.adapters.ContactUsersAdapter;
import com.example.lozachat.databinding.ActivityChatBinding;
import com.example.lozachat.listeners.ChatListener;
import com.example.lozachat.models.ChatMessage;
import com.example.lozachat.models.Summary;
import com.example.lozachat.models.User;
import com.example.lozachat.network.ApiClient;
import com.example.lozachat.network.ApiService;
import com.example.lozachat.utilities.Constants;
import com.example.lozachat.utilities.GptChatbot;
import com.example.lozachat.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends BaseActivity implements ChatListener, GptChatbot.OnMessageReceivedListener {
    private ActivityChatBinding binding;
    private User receiverUser;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private String conversationId = null;
    private Boolean isReceiverAvailable = false;
    private final int numberOfRecentMessage = 10;
    private boolean isSummarizing = false;
    ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                // Callback is invoked after the user selects a media item or closes the
                // photo picker.
                if (uri != null) {
//                    Glide.with(getApplicationContext()).load(uri).into(binding.imageProfile);
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        String bitmapString = bitMapToString(bitmap);
                        HashMap<String, Object> message = new HashMap<>();
                        message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                        message.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
                        message.put(Constants.KEY_MESSAGE, bitmapString);
                        message.put(Constants.KEY_TIMESTAMP, new Date());
                        message.put(Constants.KEY_TYPE, "image");
                        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
                        if (conversationId != null) {
                            updateConversation("Sent an image");
                        } else {
                            HashMap<String, Object> conversation = new HashMap<>();
                            conversation.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                            conversation.put(Constants.KEY_LAST_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                            conversation.put(Constants.KEY_SEEN, false);
                            conversation.put(Constants.KEY_SENDER_NAME, preferenceManager.getString(Constants.KEY_NAME));
                            conversation.put(Constants.KEY_SENDER_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
                            conversation.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
                            conversation.put(Constants.KEY_RECEIVER_NAME, receiverUser.name);
                            conversation.put(Constants.KEY_RECEIVER_IMAGE, receiverUser.image);
                            conversation.put(Constants.KEY_LAST_MESSAGE, "Sent an image");
                            conversation.put(Constants.KEY_TIMESTAMP, new Date());
                            addConversation(conversation);
                        }
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadReceiverDetails();
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());
        getUserImage();
    }
    private void getUserImage() {
        DocumentReference docRef = database.collection(Constants.KEY_COLLECTION_USERS).document(receiverUser.id);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                receiverUser.image = document.getString(Constants.KEY_IMAGE);
                init();
                listenMessages();
                setListeners();
            }
        });
    }
    private void init() {
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(
                chatMessages,
                getBitmapFromEncodedString(receiverUser.image),
                preferenceManager.getString(Constants.KEY_USER_ID),
                this
        );
        binding.chatRecycleView.setAdapter(chatAdapter);
    }

    private void sendMessage() {
        String messageToSend = binding.inputMessage.getText().toString().trim();
        if (messageToSend.isEmpty()) {
            return;
        }
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
        message.put(Constants.KEY_MESSAGE, messageToSend);
        message.put(Constants.KEY_TIMESTAMP, new Date());
        message.put(Constants.KEY_TYPE, "text");
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
        if (conversationId != null) {
            updateConversation(binding.inputMessage.getText().toString());
            if (!isReceiverAvailable) {
                database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                        .whereEqualTo(FieldPath.documentId(), conversationId)
                        .whereArrayContains(Constants.KEY_MUTED, receiverUser.id)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && task.getResult().isEmpty()) {
                                try {
                                    JSONArray tokens = new JSONArray();
                                    tokens.put(receiverUser.token);

                                    JSONObject data = new JSONObject();
                                    data.put(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                                    data.put(Constants.KEY_NAME, preferenceManager.getString(Constants.KEY_NAME));
                                    data.put(Constants.KEY_FCM_TOKEN, preferenceManager.getString(Constants.KEY_FCM_TOKEN));
                                    data.put(Constants.KEY_MESSAGE, messageToSend);

                                    JSONObject body = new JSONObject();
                                    body.put(Constants.REMOTE_MSG_DATA, data);
                                    body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

                                    sendNotification(body.toString());
                                } catch (Exception exception) {
//                                showToast(exception.getMessage());
                                }
                            }
                        });
            }
        } else {
            HashMap<String, Object> conversation = new HashMap<>();
            conversation.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
            conversation.put(Constants.KEY_LAST_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
            conversation.put(Constants.KEY_SEEN, false);
            conversation.put(Constants.KEY_SENDER_NAME, preferenceManager.getString(Constants.KEY_NAME));
            conversation.put(Constants.KEY_SENDER_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
            conversation.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
            conversation.put(Constants.KEY_RECEIVER_NAME, receiverUser.name);
            conversation.put(Constants.KEY_RECEIVER_IMAGE, receiverUser.image);
            conversation.put(Constants.KEY_LAST_MESSAGE, messageToSend);
            conversation.put(Constants.KEY_TIMESTAMP, new Date());
            addConversation(conversation);
        }

        binding.inputMessage.setText(null);
    }
    private void listenAvailabilityOfReceiver() {
        database.collection(Constants.KEY_COLLECTION_USERS).document(
                receiverUser.id
        ).addSnapshotListener(ChatActivity.this, (value, error) -> {
           if (error != null) {
               return;
           }
           if (value != null) {
               if (value.getLong(Constants.KEY_AVAILABILITY) != null) {
                   int availability = Objects.requireNonNull(
                           value.getLong(Constants.KEY_AVAILABILITY)
                   ).intValue();
                   isReceiverAvailable = availability == 1;
                   receiverUser.token = value.getString(Constants.KEY_FCM_TOKEN);
                   if (receiverUser.image == null) {
                       receiverUser.image = value.getString(Constants.KEY_IMAGE);
                       chatAdapter.setReceiverProfileImage(getBitmapFromEncodedString(receiverUser.image));
                       chatAdapter.notifyItemRangeChanged(0, chatMessages.size());
                   }
               }
           }
           if (isReceiverAvailable) {
               binding.textAvailability.setVisibility(View.VISIBLE);
           } else {
               binding.textAvailability.setVisibility(View.GONE);
           }
        });
    }
    private void listenMessages() {
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverUser.id)
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, receiverUser.id)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    private void sendNotification(String messageBody) {
        ApiClient.getClient().create(ApiService.class).sendMessage(
                Constants.getRemoteMsgHeaders(),
                messageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    try {
                        if (response.body() != null) {
                            JSONObject responseJSON = new JSONObject(response.body());
                            JSONArray results = responseJSON.getJSONArray("results");
                            if (responseJSON.getInt("failure") == 1) {
                                JSONObject error = (JSONObject) results.get(0);
//                                showToast(error.getString("error"));
                                return;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
//                    showToast("Notification sent successfully");
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
//                showToast(t.getMessage());
            }
        });
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
                    chatMessage.type = documentChange.getDocument().getString(Constants.KEY_TYPE);
                    chatMessages.add(chatMessage);
                } else if (documentChange.getType() == DocumentChange.Type.REMOVED) {
                    for (int i = 0; i < chatMessages.size(); ++i) {
                        if (chatMessages.get(i).chatId.equals(documentChange.getDocument().getId())) {
                            chatMessages.remove(i);
                            chatAdapter.notifyItemRemoved(i);
                            return;
                        }
                    }
                    return;
                }
            }
            chatMessages.sort(Comparator.comparing(obj -> obj.dateObject));
            if (count == 0) {
                chatAdapter.notifyDataSetChanged();
            } else {
                chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
                binding.chatRecycleView.smoothScrollToPosition(chatMessages.size() - 1);
            }
            binding.chatRecycleView.setVisibility(View.VISIBLE);
        }
        binding.progressBar.setVisibility(View.GONE);
        if (conversationId == null) {
            checkForConversation();
        }
    };
    private Bitmap getBitmapFromEncodedString(String encodedImage) {
        if (encodedImage != null) {
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } else {
            return null;
        }
    }
    private void loadReceiverDetails() {
        receiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.textName.setText(receiverUser.name);
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> finish());
        binding.layoutSend.setOnClickListener(v -> sendMessage());
        binding.layoutImage.setOnClickListener(v -> sendImage());
        binding.imageInfo.setOnClickListener(v -> {
            if (!isSummarizing) {
                isSummarizing = true;
                summarize();
            }
        });
    }
    private void sendImage() {
        pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }
    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("dd MMMM yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    private void addConversation(HashMap<String, Object> conversation) {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .add(conversation)
                .addOnSuccessListener(documentReference -> conversationId = documentReference.getId());
    }
    private void updateConversation(String message) {
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(conversationId);
        documentReference.update(
                Constants.KEY_LAST_MESSAGE, message,
                Constants.KEY_SEEN, false,
                Constants.KEY_LAST_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID),
                Constants.KEY_TIMESTAMP, new Date()
        );
    }
    private void checkForConversation() {
        if (chatMessages.size() != 0) {
            checkForConversationRemotely(
                    preferenceManager.getString(Constants.KEY_USER_ID),
                    receiverUser.id
            );
            checkForConversationRemotely(
                    receiverUser.id,
                    preferenceManager.getString(Constants.KEY_USER_ID)
            );
        }
    }
    private void checkForConversationRemotely(String senderId, String receiverId) {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
                .get()
                .addOnCompleteListener(conversationOnCompleteListener);
    }
    private final OnCompleteListener<QuerySnapshot> conversationOnCompleteListener = task -> {
        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversationId = documentSnapshot.getId();
            if (!documentSnapshot.getString(Constants.KEY_LAST_SENDER_ID).equals(preferenceManager.getString(Constants.KEY_USER_ID))) {
                DocumentReference documentReference =
                        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(conversationId);
                documentReference.update(
                        Constants.KEY_SEEN, true
                );
            }
        }
    };
    public String bitMapToString(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100, baos);
        byte [] b = baos.toByteArray();
        String temp = Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }
    @Override
    protected void onResume() {
        super.onResume();
        listenAvailabilityOfReceiver();
    }

    @Override
    public void OnChatDelete(ChatMessage chatMessage) {
        DocumentReference chatReference =
                database.collection(Constants.KEY_COLLECTION_CHAT).document(chatMessage.chatId);
        chatReference.delete();
        if (conversationId != null) {
            DocumentReference conversationReference =
                database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(conversationId);
            if (chatAdapter.getItemCount() > 0) {
                ChatMessage prevChatMessage = chatAdapter.getItem(chatAdapter.getItemCount() - 1);
                if (!prevChatMessage.type.equals("image")) {
                    conversationReference.update(
                            Constants.KEY_LAST_MESSAGE, prevChatMessage.message,
                            Constants.KEY_LAST_SENDER_ID, prevChatMessage.senderId,
                            Constants.KEY_TIMESTAMP, prevChatMessage.dateObject
                    );
                }
                else {
                    conversationReference.update(
                            Constants.KEY_LAST_MESSAGE, "Sent an image",
                            Constants.KEY_LAST_SENDER_ID, prevChatMessage.senderId,
                            Constants.KEY_TIMESTAMP, prevChatMessage.dateObject
                    );
                }
            } else {
                conversationReference.delete();
            }
        }
    }

    private void summarize() {
        new AlertDialog.Builder(ChatActivity.this, androidx.appcompat.R.style.Base_Theme_AppCompat_Light_Dialog_Alert)
                .setTitle("Summarize messages")
                .setMessage("Are you sure?")
                .setPositiveButton("Confirm", (dialog, which) -> {
                    summarizeWithAI();
                }).setNegativeButton("Cancel", (dialog, which) -> {
                    Intent intent = new Intent(getApplicationContext(), SummarizeActivity.class);
                    intent.putExtra(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                    intent.putExtra(Constants.KEY_RECEIVER_ID, receiverUser.id);
                    startActivity(intent);
                    isSummarizing = false;
                }).setIcon(android.R.drawable.ic_dialog_info).show();
    }
    private void summarizeWithAI() {
        GptChatbot gptChatbot = new GptChatbot();
        String prompt = "I have a conversation needed to be summarized, here are the messages:\n";
        int count = 0;
        ArrayList<String> chats = new ArrayList<>();
        String receiverName = receiverUser.name;
        String selfName = preferenceManager.getString(Constants.KEY_NAME);
        for (int i = chatMessages.size() - 1; i >= 0; --i) {
            if (chatMessages.get(i).type.equals("text") && chatMessages.get(i).message.length() < 51) {
                if (chatMessages.get(i).senderId.equals(receiverUser.id)) {
                    chats.add(receiverName + ": " + chatMessages.get(i).message);
                } else {
                    chats.add(selfName + ": " + chatMessages.get(i).message);
                }
                count++;
            }
            if (count >= numberOfRecentMessage) break;
        }
        Collections.reverse(chats);
        prompt += String.join("\n", chats);
        prompt += "\n\nSummarize the conversation above in one sentence, capturing the main topics and any important conclusions or agreements made.";
        gptChatbot.sendMessage(prompt, this);
    }
    @Override
    public void onMessageReceived(String message) {
        HashMap<String, Object> summary = new HashMap<>();
        summary.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        summary.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
        summary.put(Constants.KEY_SUMMARY_MESSAGE, message);
        summary.put(Constants.KEY_TIMESTAMP, new Date());

        database.collection(Constants.KEY_COLLECTION_SUMMARY).add(summary).addOnCompleteListener(task -> {
            Intent intent = new Intent(getApplicationContext(), SummarizeActivity.class);
            intent.putExtra(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
            intent.putExtra(Constants.KEY_RECEIVER_ID, receiverUser.id);
            startActivity(intent);
            isSummarizing = false;
        });


    }
}
