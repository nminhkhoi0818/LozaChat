package com.example.lozachat.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class Group implements Serializable {
    public String id, name, image, lastMessage, lastSenderId, lastSenderName, dateTime;
    public ArrayList<String> members;
    public Date dateObject;
    public HashMap<String, String> membersImage, membersName;
    public Group() {}
    public Group(Group group) {
        this.id = group.id;
        this.name = group.name;
        this.image = group.image;
        this.lastMessage = group.lastMessage;
        this.lastSenderId = group.lastSenderId;
        this.lastSenderName = group.lastSenderName;
        this.dateTime = group.dateTime;
        this.members = group.members;
        this.dateObject = group.dateObject;
        this.membersImage = group.membersImage;
        this.membersName = group.membersName;
    }
}
