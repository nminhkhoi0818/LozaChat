package com.example.lozachat.models;

import java.util.Date;

public class ChatMessage {
    public String chatId, senderId, receiverId, message, dateTime;
    public Date dateObject;
    public Boolean seen;
    public String conversationId, conversationName, conversationImage;
    public String type, image;
}
