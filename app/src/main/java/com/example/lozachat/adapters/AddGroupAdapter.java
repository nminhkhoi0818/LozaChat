package com.example.lozachat.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lozachat.databinding.ItemContainerUserCheckboxBinding;
import com.example.lozachat.listeners.UserListener;
import com.example.lozachat.models.User;

import java.util.List;

public class AddGroupAdapter extends RecyclerView.Adapter<AddGroupAdapter.AddGroupViewHolder> {
    private final List<User> users;
    private UserListener userListener = null;
    public AddGroupAdapter (List<User> users) {
        this.users = users;
//        this.userListener = userListener;
    }

    @NonNull
    @Override
    public AddGroupAdapter.AddGroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerUserCheckboxBinding itemContainerUserCheckboxBinding = ItemContainerUserCheckboxBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new AddGroupAdapter.AddGroupViewHolder(itemContainerUserCheckboxBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull AddGroupAdapter.AddGroupViewHolder holder, int position) {
        holder.setUserData(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class AddGroupViewHolder extends RecyclerView.ViewHolder {
        ItemContainerUserCheckboxBinding binding;
        AddGroupViewHolder(ItemContainerUserCheckboxBinding itemContainerAddFriendBinding) {
            super(itemContainerAddFriendBinding.getRoot());
            binding = itemContainerAddFriendBinding;
        }
        void setUserData(User user) {
            binding.textName.setText(user.name);
            binding.textEmail.setText(user.email);
            binding.imageProfile.setImageBitmap(getUserImage(user.image));
        }
    }

    private Bitmap getUserImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
