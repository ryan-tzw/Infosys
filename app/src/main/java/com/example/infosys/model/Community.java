package com.example.infosys.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Community {
    private String id;
    private String name;
    private String description;
    private int memberCount;
    private List<String> tags;
    private boolean isPrivate;
    private List<String> adminIds;
    private String ownerId;

    public Community() {
        // Required empty public constructor
    }

    public Community(String name, String description) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.description = description;
        this.memberCount = 0;
        this.tags = null;
        this.isPrivate = false;
        this.adminIds = new ArrayList<>();
        this.ownerId = null;
    }

    // This was just for debugging/testing purposes
    public Community(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.memberCount = 0;
        this.tags = null;
        this.isPrivate = false;
        this.adminIds = new ArrayList<>();
        this.ownerId = null;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void incrementMemberCount() {
        memberCount++;
    }

    public void decrementMemberCount() {
        memberCount--;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public List<String> getAdminIds() {
        return adminIds;
    }

    public void addAdminId(String adminId) {
        adminIds.add(adminId);
    }

    public void removeAdminId(String adminId) {
        adminIds.remove(adminId);
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
}
