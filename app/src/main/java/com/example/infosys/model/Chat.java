package com.example.infosys.model;

import com.google.firebase.Timestamp;

import java.util.List;

public class Chat {
    private String id;
    private List<String> participants;
    private String lastMessage;
    private Timestamp lastUpdated;
    private boolean isGroupChat;
    private String groupName;
    private String groupChatImageUrl;

    public Chat() {
    }

    public Chat(String id, List<String> participants, boolean isGroupChat, Timestamp lastUpdated) {
        this.id = id;
        this.participants = participants;
        this.lastMessage = "";
        this.lastUpdated = lastUpdated;
        this.isGroupChat = false;
        this.groupName = "";
        this.groupChatImageUrl = "";
    }

    public String getId() {
        return id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public Timestamp getLastUpdated() {
        return lastUpdated;
    }

    public boolean isGroupChat() {
        return isGroupChat;
    }

    public void setGroupChat(boolean groupChat) {
        isGroupChat = groupChat;
    }

    public void addParticipant(String userId) {
        if (!participants.contains(userId)) {
            participants.add(userId);
        }
    }

    public void removeParticipant(String userId) {
        participants.remove(userId);
    }

    public void addParticipants(List<String> userIds) {
        for (String userId : userIds) {
            if (!participants.contains(userId)) {
                participants.add(userId);
            }
        }
    }

    public String getGroupChatImageUrl() {
        return groupChatImageUrl;
    }
}

