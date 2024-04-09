package com.example.lozachat.adapters;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lozachat.R;
import com.example.lozachat.databinding.ItemContainerReceivedImageBinding;
import com.example.lozachat.databinding.ItemContainerReceivedMessageBinding;
import com.example.lozachat.databinding.ItemContainerSentImageBinding;
import com.example.lozachat.databinding.ItemContainerSentMessageBinding;
import com.example.lozachat.listeners.ChatListener;
import com.example.lozachat.models.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<ChatMessage> chatMessages;
    private Bitmap receiverProfileImage;
    private final String senderId;
    public static final int VIEW_TYPE_SENT_TEXT = 1;
    public static final int VIEW_TYPE_RECEIVED_TEXT = 2;
    public static final int VIEW_TYPE_SENT_IMAGE = 3;
    public static final int VIEW_TYPE_RECEIVED_IMAGE = 4;
    private ChatListener chatListener;
    public void setReceiverProfileImage(Bitmap bitmap) {
        receiverProfileImage = bitmap;
    }
    public ChatAdapter(List<ChatMessage> chatMessages, Bitmap receiverProfileImage, String senderId, ChatListener chatListener) {
        this.chatMessages = chatMessages;
        this.receiverProfileImage = receiverProfileImage;
        this.senderId = senderId;
        this.chatListener = chatListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT_TEXT) {
            return new SentMessageViewHolder(
                    ItemContainerSentMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        } else if (viewType == VIEW_TYPE_SENT_IMAGE) {
            return new SentImageViewHolder(
                    ItemContainerSentImageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        } else if (viewType == VIEW_TYPE_RECEIVED_IMAGE) {
            return new ReceivedImageViewHolder(
                    ItemContainerReceivedImageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        } else  {
            return new ReceivedMessageViewHolder(
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
        if (getItemViewType(position) == VIEW_TYPE_SENT_TEXT) {
            ((SentMessageViewHolder) holder).setData(chatMessages.get(position));
        } else if (getItemViewType(position) == VIEW_TYPE_SENT_IMAGE) {
            ((SentImageViewHolder) holder).setData(chatMessages.get(position));
        } else if (getItemViewType(position) == VIEW_TYPE_RECEIVED_IMAGE) {
            ((ReceivedImageViewHolder) holder).setData(chatMessages.get(position), receiverProfileImage);
        } else {
            ((ReceivedMessageViewHolder) holder).setData(chatMessages.get(position), receiverProfileImage);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }
    public ChatMessage getItem(int position) {
        return chatMessages.get(position);
    }
    @Override
    public int getItemViewType(int position) {
        if (chatMessages.get(position).senderId.equals(senderId)) {
            if (chatMessages.get(position).type.equals("image"))
                return VIEW_TYPE_SENT_IMAGE;
            return VIEW_TYPE_SENT_TEXT;
        } else {
            if (chatMessages.get(position).type.equals("image"))
                return VIEW_TYPE_RECEIVED_IMAGE;
            return VIEW_TYPE_RECEIVED_TEXT;
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

        void setData(ChatMessage chatMessage, Bitmap receiverProfileImage) {
            binding.textMessage.setText(chatMessage.message);
            binding.textDateTime.setText(chatMessage.dateTime);
            if (receiverProfileImage != null) {
                binding.imageProfile.setImageBitmap(receiverProfileImage);
            }

        }
    }

    class SentImageViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerSentImageBinding binding;

        SentImageViewHolder(ItemContainerSentImageBinding itemContainerSentImageBinding) {
            super(itemContainerSentImageBinding.getRoot());
            binding = itemContainerSentImageBinding;
        }

        void setData(ChatMessage chatMessage) {
            binding.imageMessage.setImageBitmap(decodeImage(chatMessage.message));
            binding.textDateTime.setText(chatMessage.dateTime);
            binding.imageMessage.setOnLongClickListener(v -> {
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
        private Bitmap decodeImage(String encodedImage) {
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }
    }
    static class ReceivedImageViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerReceivedImageBinding binding;

        ReceivedImageViewHolder(ItemContainerReceivedImageBinding itemContainerReceivedImageBinding) {
            super(itemContainerReceivedImageBinding.getRoot());
            binding = itemContainerReceivedImageBinding;
        }

        void setData(ChatMessage chatMessage, Bitmap receiverProfileImage) {
            binding.imageMessage.setImageBitmap(decodeImage(chatMessage.message));
            binding.textDateTime.setText(chatMessage.dateTime);
            if (receiverProfileImage != null) {
                binding.imageProfile.setImageBitmap(receiverProfileImage);
            }

        }
        private Bitmap decodeImage(String encodedImage) {
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }
    }
}
