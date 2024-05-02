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

import com.example.lozachat.databinding.ItemContainerContactUserBinding;
import com.example.lozachat.databinding.ItemContainerUserBinding;
import com.example.lozachat.listeners.UserListener;
import com.example.lozachat.models.User;

import java.util.Comparator;
import java.util.List;


public class ContactUsersAdapter extends RecyclerView.Adapter<ContactUsersAdapter.UserViewHolder> {
    private final List<User> users;
    private UserListener userListener = null;
    public ContactUsersAdapter(List<User> users, UserListener userListener) {
        this.users = users;
        this.users.sort(Comparator.comparing(obj -> obj.name.toLowerCase()));
        this.userListener = userListener;
    }

    public ContactUsersAdapter(List<User> users) {
        this.users = users;
        this.users.sort(Comparator.comparing(obj -> obj.name.toLowerCase()));
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerContactUserBinding itemContainerUserBinding = ItemContainerContactUserBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new UserViewHolder(itemContainerUserBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setUserData(users.get(position), position);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        ItemContainerContactUserBinding binding;
        UserViewHolder(ItemContainerContactUserBinding ItemContainerContactUserBinding) {
            super(ItemContainerContactUserBinding.getRoot());
            binding = ItemContainerContactUserBinding;
        }
        void setUserData(User user, int position) {
            binding.textName.setText(user.name);
            binding.textEmail.setText(user.email);
            binding.imageProfile.setImageBitmap(getUserImage(user.image));

            binding.header.setText(user.name.substring(0, 1).toUpperCase());
            if (position > 0 && users.get(position - 1).name.substring(0, 1).equals(user.name.substring(0, 1))) {
                binding.header.setVisibility(View.GONE);
            } else {
                binding.header.setVisibility(View.VISIBLE);
            }
            binding.getRoot().setOnClickListener(v -> userListener.onUserClicked(user));
            binding.getRoot().setOnLongClickListener(v -> {
                new AlertDialog.Builder(itemView.getContext(), androidx.appcompat.R.style.Base_Theme_AppCompat_Light_Dialog_Alert)
                        .setTitle("Unfriend")
                        .setMessage("Are you sure?")
                        .setPositiveButton("Confirm", (dialog, which) -> {
                            for (int i = 0; i < getItemCount(); ++i) {
                                if (users.get(i).id.equals(user.id)) {
                                    users.remove(i);
                                    userListener.onUserLongClicked(user);
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
    private Bitmap getUserImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
