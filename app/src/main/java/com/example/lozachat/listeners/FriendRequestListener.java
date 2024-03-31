package com.example.lozachat.listeners;

import com.example.lozachat.models.FriendRequest;

public interface FriendRequestListener {
    void OnAccept(FriendRequest friendRequest);
    void OnDeny(FriendRequest friendRequest);
}
