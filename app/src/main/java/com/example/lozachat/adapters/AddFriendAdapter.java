package com.example.lozachat.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lozachat.databinding.ItemContainerAddFriendBinding;
import com.example.lozachat.listeners.UserListener;
import com.example.lozachat.models.User;
import com.example.lozachat.utilities.PreferenceManager;

import java.util.List;

public class AddFriendAdapter extends RecyclerView.Adapter<AddFriendAdapter.AddFriendViewHolder> {
    private final List<User> users;
    private UserListener userListener = null;
    private PreferenceManager preferenceManager;
    public AddFriendAdapter (List<User> users, UserListener userListener) {
        this.users = users;
        this.userListener = userListener;
    }

    @NonNull
    @Override
    public AddFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerAddFriendBinding itemContainerAddFriendBinding = ItemContainerAddFriendBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new AddFriendViewHolder(itemContainerAddFriendBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull AddFriendViewHolder holder, int position) {
        holder.setUserData(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class AddFriendViewHolder extends RecyclerView.ViewHolder {
        ItemContainerAddFriendBinding binding;
        AddFriendViewHolder(ItemContainerAddFriendBinding itemContainerAddFriendBinding) {
            super(itemContainerAddFriendBinding.getRoot());
            binding = itemContainerAddFriendBinding;
        }
        void setUserData(User user) {
            binding.textName.setText(user.name);
            binding.textEmail.setText(user.email);
            binding.imageProfile.setImageBitmap(getUserImage(user.image));
            if (user.is_friend) {
                binding.addFriendBtn.setEnabled(false);
            } else {
                binding.addFriendBtn.setEnabled(true);
                binding.addFriendBtn.setOnClickListener(v -> {
                    userListener.onUserClicked(user);
                    binding.addFriendBtn.setEnabled(false);
                });
            }
        }
    }

    private Bitmap getUserImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
