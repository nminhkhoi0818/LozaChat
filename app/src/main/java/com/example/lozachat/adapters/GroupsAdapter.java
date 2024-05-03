package com.example.lozachat.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lozachat.databinding.ItemContainerGroupBinding;
import com.example.lozachat.databinding.ItemContainerUserBinding;
import com.example.lozachat.listeners.GroupListener;
import com.example.lozachat.listeners.UserListener;
import com.example.lozachat.models.Group;
import com.example.lozachat.models.User;

import java.util.List;

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.GroupViewHolder> {
    private final List<Group> groups;
    private GroupListener groupListener = null;
    public GroupsAdapter(List<Group> groups, GroupListener groupListener) {
        this.groups = groups;
        this.groupListener = groupListener;
    }

    public GroupsAdapter(List<Group> groups) {
        this.groups = groups;
    }

    @NonNull
    @Override
    public GroupsAdapter.GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerGroupBinding itemContainerGroupBinding = ItemContainerGroupBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new GroupsAdapter.GroupViewHolder(itemContainerGroupBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupsAdapter.GroupViewHolder holder, int position) {
        holder.setUserData(groups.get(position));
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    class GroupViewHolder extends RecyclerView.ViewHolder {
        ItemContainerGroupBinding binding;
        GroupViewHolder(ItemContainerGroupBinding itemContainerGroupBinding) {
            super(itemContainerGroupBinding.getRoot());
            binding = itemContainerGroupBinding;
        }
        void setUserData(Group group) {
            binding.groupName.setText(group.name);
            if (!group.lastSenderName.isEmpty() && !group.lastSenderId.isEmpty()) {
                binding.textRecentMessage.setText(String.format("%s: %s", group.lastSenderName, group.lastMessage));
            } else {
                binding.textRecentMessage.setText("");
            }
            if (!group.seen) {
                binding.textRecentMessage.setTypeface(null, Typeface.BOLD);
            } else {
                binding.textRecentMessage.setTypeface(null, Typeface.NORMAL);
            }
            if (group.muted) {
                binding.notificationButton.setColorFilter(Color.GREEN);
            } else {
                binding.notificationButton.setColorFilter(Color.RED);
            }
            binding.imageProfile.setImageBitmap(getUserImage(group.image));
            binding.notificationButton.setOnClickListener(v -> {
                groupListener.OnGroupMuteClicked(group, group.muted);
            });
            binding.getRoot().setOnClickListener(v -> groupListener.onGroupClicked(group));
        }
    }
    private Bitmap getUserImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
