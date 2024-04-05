package com.example.lozachat.listeners;

import com.example.lozachat.models.ChatMessage;
import com.example.lozachat.models.User;

public interface ChatListener {
    void OnChatDelete(ChatMessage chatMessage);
}
