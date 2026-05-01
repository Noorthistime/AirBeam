package com.project.p2p.model;

public class FileAccessDto {
    private Long fileId;
    private String fileName;
    private long fileSize;
    private String ownerUserId;
    private String ownerDisplayName;
    private boolean owner;
    private String downloadUrl;
    private java.util.List<SharedUserDto> sharedWithUsers;

    public FileAccessDto(Long fileId, String fileName, long fileSize, String ownerUserId, String ownerDisplayName, boolean owner, String downloadUrl, java.util.List<SharedUserDto> sharedWithUsers) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.ownerUserId = ownerUserId;
        this.ownerDisplayName = ownerDisplayName;
        this.owner = owner;
        this.downloadUrl = downloadUrl;
        this.sharedWithUsers = sharedWithUsers;
    }

    public Long getFileId() {
        return fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getOwnerUserId() {
        return ownerUserId;
    }

    public String getOwnerDisplayName() {
        return ownerDisplayName;
    }

    public boolean isOwner() {
        return owner;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public java.util.List<SharedUserDto> getSharedWithUsers() {
        return sharedWithUsers;
    }
}
