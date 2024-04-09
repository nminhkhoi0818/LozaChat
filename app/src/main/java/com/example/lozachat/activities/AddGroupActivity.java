package com.example.lozachat.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lozachat.R;
import com.example.lozachat.adapters.AddGroupAdapter;
import com.example.lozachat.databinding.ActivityAddGroupBinding;
import com.example.lozachat.listeners.UserListener;
import com.example.lozachat.models.User;
import com.example.lozachat.utilities.Constants;
import com.example.lozachat.utilities.PreferenceManager;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class AddGroupActivity extends AppCompatActivity implements UserListener {
    ActivityAddGroupBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    ArrayList<User> selectedUsers = new ArrayList<>();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        init();
        getUsers();
        setListeners();
    }

    private void init() {
        database = FirebaseFirestore.getInstance();
    }

    private void getUsers() {
        loading(true);
        if (preferenceManager.getArrayList(Constants.KEY_FRIENDS_LIST).isEmpty()) {
            loading(false);
            return;
        }
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereIn(FieldPath.documentId(), preferenceManager.getArrayList(Constants.KEY_FRIENDS_LIST))
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);

                    if (task.isSuccessful() && task.getResult() != null) {
                        List<User> users = new ArrayList<>();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            if (currentUserId.equals(queryDocumentSnapshot.getId())) {
                                continue;
                            }
                            User user = new User();
                            user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                            user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                            user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                            user.id = queryDocumentSnapshot.getId();
                            users.add(user);
                        }
                        if (users.size() > 0) {
                            AddGroupAdapter contactUsersAdapter = new AddGroupAdapter(users, this);
                            binding.usersRecyclerView.setAdapter(contactUsersAdapter);
                            binding.usersRecyclerView.setVisibility(View.VISIBLE);
                        }
//                        else {
//                            showErrorMessage();
//                        }
                    }
                });
    }

    private void setListeners() {
        binding.backBtn.setOnClickListener(v -> {
            finish();
        });

        binding.addGroupButton.setOnClickListener(v -> {
            ArrayList<String> members = new ArrayList<>();
            members.add(preferenceManager.getString(Constants.KEY_USER_ID));
            for (int i = 0; i < selectedUsers.size(); i++) {
                members.add(selectedUsers.get(i).id);
            }
            HashMap<String, Object> group = new HashMap<>();
            group.put(Constants.KEY_NAME, binding.inputGroupName.getText().toString());
            group.put(Constants.KEY_MEMBERS, members);
            String encodedImage = getDefaultEncodedImage();
            group.put(Constants.KEY_IMAGE, encodedImage);
            group.put(Constants.KEY_LAST_MESSAGE, "");
            group.put(Constants.KEY_LAST_SENDER_ID, "");
            group.put(Constants.KEY_LAST_SENDER_NAME, "");
            group.put(Constants.KEY_TIMESTAMP, new Date());

            database.collection(Constants.KEY_COLLECTION_GROUP).add(group)
                    .addOnCompleteListener(e -> finish());
        });
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private String getDefaultEncodedImage() {
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.default_avatar);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos); // bm is the bitmap object
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    @Override
    public void onUserClicked(User user) {
        if (selectedUsers.contains(user)) {
            selectedUsers.remove(user);
        } else {
            selectedUsers.add(user);
        }
    }
}
