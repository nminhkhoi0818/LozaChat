package com.example.lozachat.listeners;

import com.example.lozachat.models.User;

public interface ConversationListener {
    void OnConversationClicked(User user);
    void OnMuteClicked(User user);
}
