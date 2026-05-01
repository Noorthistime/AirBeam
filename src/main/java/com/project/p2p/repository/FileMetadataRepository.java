package com.project.p2p.repository;

import com.project.p2p.model.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
    List<FileMetadata> findByOwnerUserIdOrderByUploadTimeDesc(String ownerUserId);
}
