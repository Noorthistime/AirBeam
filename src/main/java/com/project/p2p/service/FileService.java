package com.project.p2p.service;

import com.project.p2p.config.StorageConfig;
import com.project.p2p.model.FileAccessDto;
import com.project.p2p.model.FileChunk;
import com.project.p2p.model.FileMetadata;
import com.project.p2p.model.FileShare;
import com.project.p2p.model.SharedUserDto;
import com.project.p2p.model.UserAccount;
import com.project.p2p.repository.FileChunkRepository;
import com.project.p2p.repository.FileMetadataRepository;
import com.project.p2p.repository.FileShareRepository;
import com.project.p2p.repository.UserAccountRepository;
import com.project.p2p.util.FileChunkUtil;
import com.project.p2p.util.HashUtil;
import org.springframework.beans.factory.annotation.Autowired;
import com.project.p2p.model.UploadResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service
public class FileService {

    @Autowired
    private FileMetadataRepository metadataRepo;

    @Autowired
    private FileChunkRepository chunkRepo;

    @Autowired
    private FileShareRepository shareRepo;

    @Autowired
    private UserAccountRepository userRepo;

    @Autowired
    private StorageConfig storageConfig;

    private final String[] peers = { "peer1", "peer2", "peer3" };

    public UserAccount login(String requestedUserId, String password) {
        String normalizedUserId = normalizeUserId(requestedUserId);
        if (normalizedUserId == null) {
            throw new IllegalArgumentException("A user ID is required to login.");
        }
        UserAccount user = userRepo.findById(normalizedUserId)
                .orElseThrow(() -> new IllegalArgumentException("User ID not found. Please sign up first."));

        if (user.getPassword() != null && !user.getPassword().equals(password)) {
            throw new IllegalArgumentException("Incorrect password.");
        }
        return user;
    }

    public synchronized UserAccount signIn(String requestedUserId, String displayName, String password) {
        String normalizedUserId = normalizeUserId(requestedUserId);
        if (normalizedUserId == null) {
            normalizedUserId = generateNextUserId();
        }

        String finalUserId = normalizedUserId;
        UserAccount user = userRepo.findById(finalUserId).orElseGet(() -> {
            UserAccount newUser = new UserAccount();
            newUser.setUserId(finalUserId);
            newUser.setCreatedAt(LocalDateTime.now());
            if (userRepo.count() == 0) {
                newUser.setAdmin(true);
            }
            return newUser;
        });

        String normalizedName = normalizeDisplayName(displayName, finalUserId);
        user.setDisplayName(normalizedName);
        user.setPassword(password);
        return userRepo.save(user);
    }

    public void changePassword(String userId, String currentPassword, String newPassword) {
        UserAccount user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        if (user.getPassword() != null && !user.getPassword().equals(currentPassword)) {
            throw new IllegalArgumentException("Incorrect current password.");
        }

        user.setPassword(newPassword);
        userRepo.save(user);
    }

    @Transactional
    public void deleteAccount(String userId) {
        String normalizedUserId = requireUserId(userId);
        UserAccount user = userRepo.findById(normalizedUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        if ("ID-1".equalsIgnoreCase(normalizedUserId)) {
            throw new IllegalArgumentException("Admin account cannot be deleted.");
        }

        List<FileMetadata> userFiles = metadataRepo.findByOwnerUserIdOrderByUploadTimeDesc(normalizedUserId);
        for (FileMetadata file : userFiles) {
            deleteOwnedFile(file.getId(), normalizedUserId);
        }

        userRepo.delete(user);
    }

    public boolean userExists(String userId) {
        String normalizedUserId = normalizeUserId(userId);
        if (normalizedUserId == null) return false;
        return userRepo.existsById(normalizedUserId);
    }

    public boolean toggleReceiving(String userId, boolean enabled) {
        String normalizedUserId = requireUserId(userId);
        UserAccount user = userRepo.findById(normalizedUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        user.setReceivingEnabled(enabled);
        userRepo.save(user);
        return enabled;
    }

    public UploadResponseDto uploadFile(MultipartFile file, String userId) throws Exception {
        String normalizedUserId = requireUserId(userId);
        ensureUserExists(normalizedUserId);

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "No file was uploaded. Please provide a file with the form field name 'file'.");
        }

        String originalFilename = file.getOriginalFilename();

        long fileSize = file.getSize();
        int chunkSize = 1024 * 1024; // 1MB
        int totalChunks = fileSize == 0 ? 0 : (int) Math.ceil((double) fileSize / chunkSize);

        FileMetadata metadata = new FileMetadata();
        metadata.setFileName(originalFilename != null ? originalFilename : "uploaded_file");
        metadata.setFileSize(fileSize);
        metadata.setTotalChunks(totalChunks);
        metadata.setUploadTime(LocalDateTime.now());
        metadata.setOwnerUserId(normalizedUserId);

        metadata = metadataRepo.save(metadata);

        String baseStoragePath = storageConfig.getStoragePath();
        if (baseStoragePath == null || baseStoragePath.isBlank()) {
            baseStoragePath = "storage" + File.separator;
        }
        if (!baseStoragePath.endsWith(File.separator)) {
            baseStoragePath += File.separator;
        }

        File baseDir = new File(baseStoragePath);
        if (!baseDir.exists() && !baseDir.mkdirs()) {
            throw new IllegalStateException("Could not create storage base directory: " + baseStoragePath);
        }

        Random random = new Random();
        try (java.io.InputStream is = file.getInputStream()) {
            byte[] buffer = new byte[chunkSize];
            int bytesRead;
            int chunkIndex = 0;

            while ((bytesRead = is.read(buffer)) != -1) {
                byte[] chunkData;
                if (bytesRead == chunkSize) {
                    chunkData = buffer;
                } else {
                    chunkData = new byte[bytesRead];
                    System.arraycopy(buffer, 0, chunkData, 0, bytesRead);
                }

                String peer = peers[random.nextInt(peers.length)];
                File peerDir = new File(baseDir, peer);
                if (!peerDir.exists() && !peerDir.mkdirs()) {
                    throw new IllegalStateException(
                            "Could not create peer storage directory: " + peerDir.getAbsolutePath());
                }

                File chunkFile = new File(peerDir, "chunk_" + metadata.getId() + "_" + chunkIndex);
                try (FileOutputStream fos = new FileOutputStream(chunkFile)) {
                    fos.write(chunkData);
                }

                FileChunk chunk = new FileChunk();
                chunk.setFileId(metadata.getId());
                chunk.setChunkIndex(chunkIndex);
                chunk.setChunkPath(chunkFile.getAbsolutePath());
                chunk.setHash(HashUtil.generateHash(chunkData));

                chunkRepo.save(chunk);
                chunkIndex++;
            }
        }

        String downloadUrl = "/file/download/" + metadata.getId();
        return new UploadResponseDto(metadata.getId(), metadata.getFileName(), metadata.getFileSize(), downloadUrl,
                "File uploaded successfully");
    }

    public FileShare shareFile(Long fileId, String ownerUserId, String sharedWithUserId) {
        String normalizedOwnerId = requireUserId(ownerUserId);
        String normalizedSharedWithId = requireUserId(sharedWithUserId);

        if (normalizedOwnerId.equals(normalizedSharedWithId)) {
            throw new IllegalArgumentException(
                    "You already own this file, so there is no need to share it with yourself.");
        }

        ensureUserExists(normalizedOwnerId);
        
        UserAccount sharedWithUser = userRepo.findById(normalizedSharedWithId)
                .orElseThrow(() -> new IllegalArgumentException("User ID does not exist: " + normalizedSharedWithId));

        if (!sharedWithUser.isReceivingEnabled()) {
            throw new IllegalArgumentException("This user has turned off receiving shared files.");
        }

        FileMetadata metadata = metadataRepo.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found for ID: " + fileId));

        if (!normalizedOwnerId.equals(metadata.getOwnerUserId())) {
            throw new IllegalArgumentException("Only the file owner can share this file.");
        }

        Optional<FileShare> existingShare = shareRepo.findByFileIdAndSharedWithUserId(fileId, normalizedSharedWithId);
        if (existingShare.isPresent()) {
            return existingShare.get();
        }

        FileShare share = new FileShare();
        share.setFileId(fileId);
        share.setOwnerUserId(normalizedOwnerId);
        share.setSharedWithUserId(normalizedSharedWithId);
        share.setSharedAt(LocalDateTime.now());
        return shareRepo.save(share);
    }

    @Transactional
    public void revokeShare(Long fileId, String ownerUserId, String sharedWithUserId) {
        String normalizedOwnerId = requireUserId(ownerUserId);
        String normalizedSharedWithId = requireUserId(sharedWithUserId);

        FileMetadata metadata = metadataRepo.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found for ID: " + fileId));

        if (!normalizedOwnerId.equals(metadata.getOwnerUserId())) {
            throw new IllegalArgumentException("Only the file owner can remove shared access.");
        }

        FileShare share = shareRepo.findByFileIdAndSharedWithUserId(fileId, normalizedSharedWithId)
                .orElseThrow(() -> new IllegalArgumentException("This user does not have access to this file."));
        shareRepo.delete(share);
    }

    @Transactional
    public void revokeAllShares(Long fileId, String ownerUserId) {
        String normalizedOwnerId = requireUserId(ownerUserId);

        FileMetadata metadata = metadataRepo.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found for ID: " + fileId));

        if (!normalizedOwnerId.equals(metadata.getOwnerUserId())) {
            throw new IllegalArgumentException("Only the file owner can remove shared access.");
        }

        shareRepo.deleteByFileId(fileId);
    }

    @Transactional
    public void deleteOwnedFile(Long fileId, String ownerUserId) {
        String normalizedOwnerId = requireUserId(ownerUserId);

        FileMetadata metadata = metadataRepo.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found for ID: " + fileId));

        if (!normalizedOwnerId.equals(metadata.getOwnerUserId())) {
            throw new IllegalArgumentException("Only the file owner can remove this file.");
        }

        List<FileChunk> chunks = chunkRepo.findByFileIdOrderByChunkIndexAsc(fileId);
        for (FileChunk chunk : chunks) {
            File chunkFile = new File(chunk.getChunkPath());
            if (chunkFile.exists() && !chunkFile.delete()) {
                throw new IllegalStateException("Could not delete file chunk: " + chunkFile.getAbsolutePath());
            }
        }

        shareRepo.deleteByFileId(fileId);
        chunkRepo.deleteByFileId(fileId);
        metadataRepo.delete(metadata);
    }

    public List<FileAccessDto> listAccessibleFiles(String userId) {
        String normalizedUserId = requireUserId(userId);
        ensureUserExists(normalizedUserId);

        Map<Long, FileAccessDto> files = new LinkedHashMap<>();
        for (FileMetadata metadata : metadataRepo.findByOwnerUserIdOrderByUploadTimeDesc(normalizedUserId)) {
            files.put(metadata.getId(), toFileAccessDto(metadata, normalizedUserId));
        }

        for (FileShare share : shareRepo.findBySharedWithUserIdOrderBySharedAtDesc(normalizedUserId)) {
            metadataRepo.findById(share.getFileId())
                    .ifPresent(metadata -> files.putIfAbsent(metadata.getId(),
                            toFileAccessDto(metadata, normalizedUserId)));
        }

        return new ArrayList<>(files.values());
    }

    public record DownloadResult(File file, String fileName, String contentType) {
    }

    public DownloadResult downloadFile(Long fileId, String userId) throws Exception {
        String normalizedUserId = requireUserId(userId);
        ensureUserExists(normalizedUserId);

        FileMetadata metadata = metadataRepo.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found for ID: " + fileId));

        if (!canAccess(metadata, normalizedUserId)) {
            throw new IllegalArgumentException("This file is not shared with user ID: " + normalizedUserId);
        }

        List<FileChunk> chunks = chunkRepo.findByFileIdOrderByChunkIndexAsc(fileId);

        String baseStoragePath = storageConfig.getStoragePath();
        if (baseStoragePath == null || baseStoragePath.isBlank()) {
            baseStoragePath = "storage" + File.separator;
        }
        if (!baseStoragePath.endsWith(File.separator)) {
            baseStoragePath += File.separator;
        }

        File mergedDir = new File(baseStoragePath, "merged");
        if (!mergedDir.exists() && !mergedDir.mkdirs()) {
            throw new IllegalStateException(
                    "Could not create merged storage directory: " + mergedDir.getAbsolutePath());
        }

        String safeFileName = metadata.getFileName() != null ? metadata.getFileName().replaceAll("[^a-zA-Z0-9._-]", "_")
                : "file_" + fileId;
        File outputFile = new File(mergedDir, fileId + "_" + safeFileName);

        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            for (FileChunk chunk : chunks) {
                File chunkFile = new File(chunk.getChunkPath());
                byte[] data = java.nio.file.Files.readAllBytes(chunkFile.toPath());
                fos.write(data);
            }
        }

        String contentType = Optional.ofNullable(URLConnection.guessContentTypeFromName(metadata.getFileName()))
                .orElse("application/octet-stream");

        return new DownloadResult(outputFile, metadata.getFileName(), contentType);
    }

    private FileAccessDto toFileAccessDto(FileMetadata metadata, String userId) {
        UserAccount ownerAccount = userRepo.findById(metadata.getOwnerUserId()).orElse(null);
        String ownerName = ownerAccount != null ? ownerAccount.getDisplayName() : metadata.getOwnerUserId();
        List<SharedUserDto> sharedUsers = shareRepo.findByFileIdOrderBySharedAtDesc(metadata.getId()).stream()
                .map(share -> {
                    UserAccount sharedUser = userRepo.findById(share.getSharedWithUserId()).orElse(null);
                    String displayName = sharedUser != null ? sharedUser.getDisplayName() : share.getSharedWithUserId();
                    return new SharedUserDto(share.getSharedWithUserId(), displayName);
                })
                .toList();

        return new FileAccessDto(
                metadata.getId(),
                metadata.getFileName(),
                metadata.getFileSize(),
                metadata.getOwnerUserId(),
                ownerName,
                userId.equals(metadata.getOwnerUserId()),
                "/file/download/" + metadata.getId() + "?userId=" + userId,
                sharedUsers);
    }

    private boolean canAccess(FileMetadata metadata, String userId) {
        return userId.equals(metadata.getOwnerUserId())
                || shareRepo.existsByFileIdAndSharedWithUserId(metadata.getId(), userId);
    }

    private void ensureUserExists(String userId) {
        if (!userRepo.existsById(userId)) {
            throw new IllegalArgumentException("User ID does not exist: " + userId);
        }
    }

    private String requireUserId(String userId) {
        String normalizedUserId = normalizeUserId(userId);
        if (normalizedUserId == null) {
            throw new IllegalArgumentException("A user ID is required.");
        }
        return normalizedUserId;
    }

    private String normalizeUserId(String userId) {
        if (userId == null) {
            return null;
        }
        String trimmed = userId.trim();
        if (trimmed.matches("(?i)^id-\\d+$")) {
            return "ID-" + trimmed.substring(trimmed.indexOf("-") + 1);
        }
        String normalized = trimmed.toLowerCase().replaceAll("[^a-z0-9._-]", "-");
        normalized = normalized.replaceAll("-+", "-");
        if (normalized.isBlank()) {
            return null;
        }
        return normalized.length() > 64 ? normalized.substring(0, 64) : normalized;
    }

    private String generateNextUserId() {
        java.util.List<Integer> existingIds = userRepo.findAll().stream()
                .map(UserAccount::getUserId)
                .filter(userId -> userId != null && userId.matches("(?i)^id-\\d+$"))
                .map(userId -> Integer.parseInt(userId.substring(userId.indexOf("-") + 1)))
                .sorted()
                .toList();

        int nextId = 1;
        for (int id : existingIds) {
            if (id == nextId) {
                nextId++;
            } else if (id > nextId) {
                break;
            }
        }
        
        return "ID-" + nextId;
    }

    private String normalizeDisplayName(String displayName, String userId) {
        if (displayName == null || displayName.isBlank()) {
            return userId;
        }
        return displayName.trim();
    }

    public List<UserAccount> getAllUsers(String adminId) {
        UserAccount admin = userRepo.findById(normalizeUserId(adminId))
                .orElseThrow(() -> new IllegalArgumentException("Admin user not found."));
        if (!admin.isAdmin()) {
            throw new IllegalArgumentException("Unauthorized access.");
        }
        return userRepo.findAll();
    }
}
