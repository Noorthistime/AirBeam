package com.project.p2p.model;

public class ShareRequest {
    private Long fileId;
    private String ownerUserId;
    private String sharedWithUserId;

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public String getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(String ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public String getSharedWithUserId() {
        return sharedWithUserId;
    }

    public void setSharedWithUserId(String sharedWithUserId) {
        this.sharedWithUserId = sharedWithUserId;
    }
}
