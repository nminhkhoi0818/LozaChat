package com.example.lozachat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lozachat.R;
import com.example.lozachat.databinding.ActivityMainBinding;
import com.example.lozachat.fragments.ContactsFragment;
import com.example.lozachat.fragments.MessageFragment;

import com.example.lozachat.fragments.SettingsFragment;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
//    private PreferenceManager preferenceManager;
    private MessageFragment messageFragment = new MessageFragment();
    private ContactsFragment contactsFragment = new ContactsFragment();
    private SettingsFragment settingsFragment = new SettingsFragment();
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
                } else if (item.getItemId()==R.id.menu_settings) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, settingsFragment).commit();
                }
                return true;
            }
        });
        binding.bottomNavigation.setSelectedItemId(R.id.menu_message);
    }
}
