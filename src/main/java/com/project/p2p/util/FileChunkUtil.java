package com.project.p2p.util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileChunkUtil {

    private static final int CHUNK_SIZE = 1024 * 1024; // 1MB

    public static List<byte[]> splitFile(File file) throws IOException {
        List<byte[]> chunks = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[CHUNK_SIZE];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                byte[] chunk = new byte[bytesRead];
                System.arraycopy(buffer, 0, chunk, 0, bytesRead);
                chunks.add(chunk);
            }
        }
        return chunks;
    }
}