package com.project.p2p.repository;

import com.project.p2p.model.FileShare;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FileShareRepository extends JpaRepository<FileShare, Long> {
    boolean existsByFileIdAndSharedWithUserId(Long fileId, String sharedWithUserId);

    List<FileShare> findBySharedWithUserIdOrderBySharedAtDesc(String sharedWithUserId);

    List<FileShare> findByFileIdOrderBySharedAtDesc(Long fileId);

    Optional<FileShare> findByFileIdAndSharedWithUserId(Long fileId, String sharedWithUserId);

    void deleteByFileId(Long fileId);
}
