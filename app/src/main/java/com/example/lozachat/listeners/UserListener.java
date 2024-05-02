package com.example.lozachat.listeners;

import com.example.lozachat.models.User;

public interface UserListener {
    void onUserClicked(User user);
    void onUserLongClicked(User user);
}
