package yeditepe.p2pstream.core;

import java.io.*;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.io.RandomAccessFile;


public class ChunkUtils {

    public static final int CHUNK_SIZE = 256 * 1024; // 256 KB

    public static List<ChunkInfo> splitFileIntoChunks(File file) throws Exception {
        List<ChunkInfo> chunks = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[CHUNK_SIZE];
            int bytesRead;
            int index = 0;

            while ((bytesRead = fis.read(buffer)) != -1) {
                byte[] chunkData;

                if (bytesRead == CHUNK_SIZE) {
                    chunkData = buffer.clone();
                } else {
                    chunkData = new byte[bytesRead];
                    System.arraycopy(buffer, 0, chunkData, 0, bytesRead);
                }

                String hash = sha256(chunkData);
                chunks.add(new ChunkInfo(index, chunkData, hash));
                index++;
            }
        }

        return chunks;
    }

    private static String sha256(byte[] data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(data);

        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    
    public static ChunkInfo readSingleChunk(File file, int chunkIndex) throws Exception {
        long offset = (long) chunkIndex * CHUNK_SIZE;

        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            long fileLength = raf.length();
            if (offset >= fileLength) {
                return null; // böyle bir chunk yok
            }

            raf.seek(offset);
            int toRead = (int) Math.min(CHUNK_SIZE, fileLength - offset);
            byte[] buf = new byte[toRead];
            raf.readFully(buf);

            String hash = sha256(buf);
            return new ChunkInfo(chunkIndex, buf, hash);
        }
    }

}
