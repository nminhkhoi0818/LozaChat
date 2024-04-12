package com.example.lozachat.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.lozachat.activities.WelcomeActivity;
import com.example.lozachat.databinding.FragmentSettingsBinding;
import com.example.lozachat.utilities.Constants;
import com.example.lozachat.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;


public class SettingsFragment extends Fragment {
    FragmentSettingsBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private ActivityResultLauncher<PickVisualMediaRequest> launcher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        preferenceManager = new PreferenceManager(getContext());
        init();
        setListeners();


        launcher = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri == null)
                return;
            Glide.with(getContext()).load(uri).into(binding.imageProfile);
            handleImageSelection(uri);
        });
    }

    private void handleImageSelection(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
            Bitmap resizedBitmap = getResizedBitmap(bitmap, 1024); // Resize to max 1024px
            String bitmapString = bitMapToString(resizedBitmap);
            preferenceManager.putString(Constants.KEY_IMAGE, bitmapString);
            DocumentReference documentReference =
                    database.collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID));
            documentReference.update(
                    Constants.KEY_IMAGE, bitmapString
            );
            database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                    .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(doc.getId()).update(
                                        Constants.KEY_SENDER_IMAGE, bitmapString
                                );
                            }
                        }
                    });

            database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                    .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(doc.getId()).update(
                                        Constants.KEY_RECEIVER_IMAGE, bitmapString
                                );
                            }
                        }
                    });
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    private void init() {
        binding.userName.setText(preferenceManager.getString(Constants.KEY_NAME));
        binding.userEmail.setText(preferenceManager.getString(Constants.KEY_EMAIL));
        binding.imageProfile.setImageBitmap(getUserImage(preferenceManager.getString(Constants.KEY_IMAGE)));

        database = FirebaseFirestore.getInstance();
    }

    private void setListeners() {
        binding.buttonLogout.setOnClickListener(v -> {
            signOut();
        });
        binding.imageProfile.setOnClickListener(v -> {
            launcher.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });
    }

    public String bitMapToString(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,80, baos);
        byte [] b = baos.toByteArray();
        String temp = Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    private Bitmap getUserImage(String encodedImage) {
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private void signOut() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    preferenceManager.clear();
                    startActivity(new Intent(getContext(), WelcomeActivity.class));
                    getActivity().finish();
                });

    }
}
