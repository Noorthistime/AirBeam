package com.project.p2p.service;

import com.project.p2p.config.StorageConfig;
import com.project.p2p.model.FileChunk;
import com.project.p2p.model.FileMetadata;
import com.project.p2p.repository.FileChunkRepository;
import com.project.p2p.repository.FileMetadataRepository;
import com.project.p2p.util.FileChunkUtil;
import com.project.p2p.util.HashUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class FileService {

    @Autowired
    private FileMetadataRepository metadataRepo;

    @Autowired
    private FileChunkRepository chunkRepo;

    @Autowired
    private StorageConfig storageConfig;

    private final String[] peers = {"peer1", "peer2", "peer3"};

    public String uploadFile(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No file was uploaded. Please provide a file with the form field name 'file'.");
        }

        String originalFilename = file.getOriginalFilename();
        String suffix = ".tmp";
        if (originalFilename != null && originalFilename.contains(".")) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf('.'));
            if (suffix.length() < 3) {
                suffix = ".tmp";
            }
        }

        File tempFile = File.createTempFile("upload_", suffix);
        file.transferTo(tempFile);

        List<byte[]> chunks = FileChunkUtil.splitFile(tempFile);

        FileMetadata metadata = new FileMetadata();
        metadata.setFileName(originalFilename != null ? originalFilename : tempFile.getName());
        metadata.setFileSize(file.getSize());
        metadata.setTotalChunks(chunks.size());
        metadata.setUploadTime(LocalDateTime.now());

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
        for (int i = 0; i < chunks.size(); i++) {
            byte[] chunkData = chunks.get(i);

            String peer = peers[random.nextInt(peers.length)];
            File peerDir = new File(baseDir, peer);
            if (!peerDir.exists() && !peerDir.mkdirs()) {
                throw new IllegalStateException("Could not create peer storage directory: " + peerDir.getAbsolutePath());
            }

            File chunkFile = new File(peerDir, "chunk_" + metadata.getId() + "_" + i);
            try (FileOutputStream fos = new FileOutputStream(chunkFile)) {
                fos.write(chunkData);
            }

            FileChunk chunk = new FileChunk();
            chunk.setFileId(metadata.getId());
            chunk.setChunkIndex(i);
            chunk.setChunkPath(chunkFile.getAbsolutePath());
            chunk.setHash(HashUtil.generateHash(chunkData));

            chunkRepo.save(chunk);
        }

        tempFile.delete();
        return "File uploaded successfully";
    }

    public File downloadFile(Long fileId) throws Exception {
        List<FileChunk> chunks = chunkRepo.findByFileIdOrderByChunkIndexAsc(fileId);

        String baseStoragePath = storageConfig.getStoragePath();
        if (!baseStoragePath.endsWith(File.separator)) {
            baseStoragePath += File.separator;
        }
        File mergedDir = new File(baseStoragePath, "merged");
        if (!mergedDir.exists() && !mergedDir.mkdirs()) {
            throw new IllegalStateException("Could not create merged storage directory: " + mergedDir.getAbsolutePath());
        }

        String outputPath = new File(mergedDir, "file_" + fileId).getAbsolutePath();
        try (FileOutputStream fos = new FileOutputStream(outputPath)) {
            for (FileChunk chunk : chunks) {
                File file = new File(chunk.getChunkPath());
                byte[] data = java.nio.file.Files.readAllBytes(file.toPath());
                fos.write(data);
            }
        }

        return new File(outputPath);
    }
}