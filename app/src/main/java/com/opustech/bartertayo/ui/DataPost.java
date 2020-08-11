package com.opustech.bartertayo.ui;

import com.google.firebase.firestore.Exclude;

import java.util.List;

public class DataPost {
    private String documentId;
    private String userId;
    private String description;
    private List<String> imageUrl;
    private List<String> tags;

    public DataPost(String userId, String description, List<String> imageUrl, List<String> tags) {
        this.userId = userId;
        this.description = description;
        this.imageUrl = imageUrl;
        this.tags = tags;
    }

    @Exclude
    public String getDocumentId() {
        return documentId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public List<String> getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(List<String> imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
