package com.example.lozachat.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lozachat.databinding.ItemContainerContactUserBinding;
import com.example.lozachat.databinding.ItemContainerFriendRequestBinding;
import com.example.lozachat.listeners.FriendRequestListener;
import com.example.lozachat.listeners.UserListener;
import com.example.lozachat.models.FriendRequest;
import com.example.lozachat.models.User;

import java.util.Comparator;
import java.util.List;

public class FriendRequestsAdapter extends RecyclerView.Adapter<FriendRequestsAdapter.FriendRequestViewHolder> {
    private final List<FriendRequest> requests;
    private FriendRequestListener friendRequestListener = null;
    public FriendRequestsAdapter(List<FriendRequest> requests, FriendRequestListener friendRequestListener) {
        this.requests = requests;
        this.friendRequestListener = friendRequestListener;
    }
    @NonNull
    @Override
    public FriendRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerFriendRequestBinding itemContainerFriendRequestBinding = ItemContainerFriendRequestBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new FriendRequestsAdapter.FriendRequestViewHolder(itemContainerFriendRequestBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendRequestViewHolder holder, int position) {
        holder.setRequestData(requests.get(position), position);
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    class FriendRequestViewHolder extends RecyclerView.ViewHolder {
        ItemContainerFriendRequestBinding binding;
        FriendRequestViewHolder(ItemContainerFriendRequestBinding itemContainerFriendRequestBinding) {
            super(itemContainerFriendRequestBinding.getRoot());
            binding = itemContainerFriendRequestBinding;
        }
        void setRequestData(FriendRequest request, int position) {
            binding.textName.setText(request.senderName);
            binding.textEmail.setText(request.senderEmail);
            binding.imageProfile.setImageBitmap(getUserImage(request.senderImage));

            binding.denyBtn.setOnClickListener(v -> {
                friendRequestListener.OnDeny(request);
                requests.remove(position);
                notifyItemRemoved(position);
            });
            binding.acceptBtn.setOnClickListener(v -> {
                friendRequestListener.OnAccept(request);
                requests.remove(position);
                notifyItemRemoved(position);
            });

        }
    }
    private Bitmap getUserImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
