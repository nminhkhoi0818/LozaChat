package com.example.lozachat.models;

import java.io.Serializable;
import java.util.Date;

public class Summary implements Serializable {
    public String senderId, receiverId, summaryMessage, dateTime;
    public Date dateObject;
}
