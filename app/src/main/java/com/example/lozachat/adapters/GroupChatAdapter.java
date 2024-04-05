package com.example.lozachat.adapters;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lozachat.databinding.ItemContainerReceivedMessageBinding;
import com.example.lozachat.databinding.ItemContainerSentMessageBinding;
import com.example.lozachat.listeners.ChatListener;
import com.example.lozachat.models.ChatMessage;

import java.util.HashMap;
import java.util.List;

public class GroupChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<ChatMessage> chatMessages;
    private final HashMap<String, String> membersImage, membersName;
    private final String senderId;
    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;
    private ChatListener chatListener;

    public GroupChatAdapter(List<ChatMessage> chatMessages, HashMap<String, String> membersImage, HashMap<String, String> membersName, String senderId, ChatListener chatListener) {
        this.chatMessages = chatMessages;
        this.membersImage = membersImage;
        this.membersName = membersName;
        this.senderId = senderId;
        this.chatListener = chatListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            return new GroupChatAdapter.SentMessageViewHolder(
                    ItemContainerSentMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        } else {
            return new GroupChatAdapter.ReceivedMessageViewHolder(
                    ItemContainerReceivedMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_SENT) {
            ((GroupChatAdapter.SentMessageViewHolder) holder).setData(chatMessages.get(position));
        } else {
            ((GroupChatAdapter.ReceivedMessageViewHolder) holder).setData(
                    chatMessages.get(position),
                    membersName.get(chatMessages.get(position).senderId),
                    getUserImage(membersImage.get(chatMessages.get(position).senderId))
            );
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }
    public ChatMessage getItem(int position) {
        return chatMessages.get(position);
    }
    public String getMemberImage(int position) {
        return membersImage.get(chatMessages.get(position).senderId);
    }
    public String getMemberName(int position) {
        return membersName.get(chatMessages.get(position).senderId);
    }
    @Override
    public int getItemViewType(int position) {
        if (chatMessages.get(position).senderId.equals(senderId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerSentMessageBinding binding;

        SentMessageViewHolder(ItemContainerSentMessageBinding itemContainerSentMessageBinding) {
            super(itemContainerSentMessageBinding.getRoot());
            binding = itemContainerSentMessageBinding;
        }

        void setData(ChatMessage chatMessage) {
            binding.textMessage.setText(chatMessage.message);
            binding.textDateTime.setText(chatMessage.dateTime);
            binding.textMessage.setOnLongClickListener(v -> {
                new AlertDialog.Builder(itemView.getContext(), androidx.appcompat.R.style.Base_Theme_AppCompat_Light_Dialog_Alert)
                        .setTitle("Delete message")
                        .setMessage("Are you sure?")
                        .setPositiveButton("Confirm", (dialog, which) -> {
                            for (int i = 0; i < getItemCount(); ++i) {
                                if (chatMessages.get(i).chatId.equals(chatMessage.chatId)) {
                                    chatMessages.remove(i);
                                    chatListener.OnChatDelete(chatMessage);
                                    notifyItemRemoved(i);
                                    break;
                                }
                            }
                        }).setNegativeButton("Cancel", (dialog, which) -> {
                            // do nothing
                        }).setIcon(android.R.drawable.ic_dialog_alert).show();
                return false;
            });
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerReceivedMessageBinding binding;

        ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding itemContainerReceivedMessageBinding) {
            super(itemContainerReceivedMessageBinding.getRoot());
            binding = itemContainerReceivedMessageBinding;
        }

        void setData(ChatMessage chatMessage, String receivedName, Bitmap receivedProfileImage) {
            binding.textMessage.setText(chatMessage.message);
            binding.textDateTime.setText(chatMessage.dateTime);
            binding.textName.setText(receivedName);
            binding.textName.setVisibility(View.VISIBLE);
            binding.imageProfile.setImageBitmap(receivedProfileImage);
        }
    }
    private Bitmap getUserImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
