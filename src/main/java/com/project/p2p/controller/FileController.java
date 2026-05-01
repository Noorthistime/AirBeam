package com.project.p2p.controller;

import com.project.p2p.model.FileAccessDto;
import com.project.p2p.model.ShareRequest;
import com.project.p2p.model.UploadResponseDto;
import com.project.p2p.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.FileInputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/file")
@CrossOrigin(origins = "*")
public class FileController {

    @Autowired
    private FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<UploadResponseDto> upload(@RequestParam("file") MultipartFile file,
                                                     @RequestParam("userId") String userId) {
        try {
            UploadResponseDto response = fileService.uploadFile(file, userId);
            String downloadUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/file/download/")
                    .path(String.valueOf(response.getFileId()))
                    .queryParam("userId", userId)
                    .toUriString();
            response.setDownloadUrl(downloadUrl);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new UploadResponseDto(null, null, 0, null, ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new UploadResponseDto(null, null, 0, null, "Upload failed: " + ex.getMessage()));
        }
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<InputStreamResource> download(@PathVariable Long id,
                                                        @RequestParam("userId") String userId) throws Exception {
        var result = fileService.downloadFile(id, userId);

        InputStreamResource resource = new InputStreamResource(new FileInputStream(result.file()));
        String encodedFileName = URLEncoder.encode(result.fileName(), StandardCharsets.UTF_8).replaceAll("\\+", "%20");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodedFileName + "; filename=\"" + result.fileName().replaceAll("[\\\\\"\\\n\\\r]", "_") + "\"")
                .contentType(MediaType.parseMediaType(result.contentType()))
                .contentLength(result.file().length())
                .body(resource);
    }

    @GetMapping
    public ResponseEntity<List<FileAccessDto>> listFiles(@RequestParam("userId") String userId) {
        return ResponseEntity.ok(fileService.listAccessibleFiles(userId));
    }

    @PostMapping("/share")
    public ResponseEntity<Map<String, String>> share(@RequestBody ShareRequest request) {
        try {
            fileService.shareFile(request.getFileId(), request.getOwnerUserId(), request.getSharedWithUserId());
            return ResponseEntity.ok(Map.of("message", "File shared successfully."));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteFile(@PathVariable Long id,
                                                          @RequestParam("userId") String userId) {
        try {
            fileService.deleteOwnedFile(id, userId);
            return ResponseEntity.ok(Map.of("message", "File removed successfully."));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}/share")
    public ResponseEntity<Map<String, String>> revokeShare(@PathVariable Long id,
                                                           @RequestParam("ownerUserId") String ownerUserId,
                                                           @RequestParam("sharedWithUserId") String sharedWithUserId) {
        try {
            fileService.revokeShare(id, ownerUserId, sharedWithUserId);
            return ResponseEntity.ok(Map.of("message", "Access removed successfully."));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}/shares")
    public ResponseEntity<Map<String, String>> revokeAllShares(@PathVariable Long id,
                                                               @RequestParam("ownerUserId") String ownerUserId) {
        try {
            fileService.revokeAllShares(id, ownerUserId);
            return ResponseEntity.ok(Map.of("message", "All shared access removed. Your uploaded file is still in your account."));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
    }
}
