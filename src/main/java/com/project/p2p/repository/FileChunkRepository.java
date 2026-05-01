package com.project.p2p.repository;

import com.project.p2p.model.FileChunk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileChunkRepository extends JpaRepository<FileChunk, Long> {

    List<FileChunk> findByFileIdOrderByChunkIndexAsc(Long fileId);

    void deleteByFileId(Long fileId);
}
