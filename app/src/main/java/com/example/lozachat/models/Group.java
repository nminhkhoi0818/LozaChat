package com.example.lozachat.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class Group implements Serializable {
    public String id, name, image, lastMessage, lastSenderId, lastSenderName, dateTime;
    public ArrayList<String> members;
    public Date dateObject;
    public HashMap<String, String> membersImage;
}
