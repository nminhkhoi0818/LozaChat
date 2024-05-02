package com.example.lozachat.utilities;

import java.util.HashMap;

public class Constants {
    public static final String KEY_COLLECTION_USERS = "users";
    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_PREFERENCE_NAME = "chatAppPreference";
    public static final String KEY_IS_SIGNED_IN = "isSignedIn";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_USER = "user";
    public static final String KEY_COLLECTION_CHAT = "chat";
    public static final String KEY_SENDER_ID = "senderId";
    public static final String KEY_RECEIVER_ID = "receiverId";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_COLLECTION_CONVERSATIONS = "conversations";
    public static final String KEY_SENDER_NAME = "senderName";
    public static final String KEY_RECEIVER_NAME = "receiverName";
    public static final String KEY_SENDER_IMAGE = "senderImage";
    public static final String KEY_RECEIVER_IMAGE = "receiverImage";
    public static final String KEY_LAST_MESSAGE = "lastMessage";
    public static final String KEY_AVAILABILITY = "availability";
    public static final String KEY_COLLECTION_FRIEND_REQUESTS = "friend_requests";
    public static final String KEY_SENDER_EMAIL = "senderEmail";
    public static final String KEY_FRIENDS_LIST = "friendsList";
    public static final String KEY_FCM_TOKEN = "fcmToken";
    public static final String KEY_COLLECTION_GROUP = "group";
    public static final String KEY_GROUPS_LIST = "groupsList";
    public static final String KEY_MEMBERS = "members";
    public static final String KEY_LAST_SENDER_ID = "lastSenderId";
    public static final String KEY_LAST_SENDER_NAME = "lastSenderName";
    public static final String KEY_GROUP = "group";
    public static final String KEY_TYPE = "type";
    public static final String KEY_COLLECTION_SUMMARY = "summary";
    public static final String KEY_SUMMARY_MESSAGE = "summaryMessage";
    public static final String KEY_LAST_MESSAGE_TYPE = "lastMessageType";
    public static final String KEY_SEEN = "seen";
    public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content-Type";
    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";
    public static HashMap<String, String> remoteMsgHeaders = null;
    public static HashMap<String, String> getRemoteMsgHeaders() {
        if (remoteMsgHeaders == null) {
            remoteMsgHeaders = new HashMap<>();
            remoteMsgHeaders.put(
                    REMOTE_MSG_AUTHORIZATION,
                    "key=AAAAy95xKKk:APA91bH_pdLMMgjIiUxKOl4cX2aKAeUQqPe-QwTHNe8y0auDsw8Ja__tLdShmiIvgW4UVT3XqDpKoA_OhSUDoLihhftxqPQG9shTUiNQVJpeSXE1Ra9cjhxW6mgh0EvH5417jKoHMUL4"
            );
            remoteMsgHeaders.put(
                    REMOTE_MSG_CONTENT_TYPE,
                    "application/json"
            );
        }
        return remoteMsgHeaders;
    }
    public static final String KEY_MUTE_STATUS = "mute_status";
    public static final String KEY_MUTED = "muted";
}
