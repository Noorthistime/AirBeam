package com.project.p2p.model;

import jakarta.persistence.*;

@Entity
public class FileChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long fileId;
    private int chunkIndex;
    private String chunkPath;
    private String hash;

    public Long getId() { return id; }

    public Long getFileId() { return fileId; }
    public void setFileId(Long fileId) { this.fileId = fileId; }

    public int getChunkIndex() { return chunkIndex; }
    public void setChunkIndex(int chunkIndex) { this.chunkIndex = chunkIndex; }

    public String getChunkPath() { return chunkPath; }
    public void setChunkPath(String chunkPath) { this.chunkPath = chunkPath; }

    public String getHash() { return hash; }
    public void setHash(String hash) { this.hash = hash; }
}