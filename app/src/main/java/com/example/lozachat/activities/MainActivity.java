package com.example.lozachat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lozachat.R;
import com.example.lozachat.adapters.RecentConversationsAdapter;
import com.example.lozachat.databinding.ActivityMainBinding;
import com.example.lozachat.listeners.ConversationListener;
import com.example.lozachat.models.ChatMessage;
import com.example.lozachat.models.User;
import com.example.lozachat.utilities.Constants;
import com.example.lozachat.utilities.PreferenceManager;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
//    private PreferenceManager preferenceManager;
    private MessageFragment messageFragment = new MessageFragment();
    private ContactsFragment contactsFragment = new ContactsFragment();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
//        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
//        getSupportFragmentManager().beginTransaction().add(R.id.main_frame_layout, messageFragment, null).commit();
    }

    private void setListeners() {
        binding.bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId()==R.id.menu_message) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, messageFragment).commit();
                } else if (item.getItemId()==R.id.menu_contacts) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, contactsFragment).commit();
                }
                return true;
            }
        });
        binding.bottomNavigation.setSelectedItemId(R.id.menu_message);
    }
}
