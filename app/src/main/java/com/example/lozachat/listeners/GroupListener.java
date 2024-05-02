package com.example.lozachat.listeners;

import com.example.lozachat.models.Group;
import com.example.lozachat.models.User;

public interface GroupListener {
    void onGroupClicked(Group group);
    void OnGroupMuteClicked(Group group);
}
