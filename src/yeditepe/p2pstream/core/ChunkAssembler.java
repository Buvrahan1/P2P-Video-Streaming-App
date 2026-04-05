package yeditepe.p2pstream.core;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkAssembler {

    private final Map<Integer, ChunkInfo> receivedChunks = new ConcurrentHashMap<>();
    private final int totalChunks;

    public ChunkAssembler(int totalChunks) {
        this.totalChunks = totalChunks;
    }

    // Gelen chunk'ı ekle
    public void addChunk(ChunkInfo chunk) {
        receivedChunks.putIfAbsent(chunk.getChunkIndex(), chunk);
    }

    // Hepsi geldi mi?
    public boolean isComplete() {
        return receivedChunks.size() == totalChunks;
    }

    // Dosyayı birleştir
    public File assembleToFile(File outputFile) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            for (int i = 0; i < totalChunks; i++) {
                ChunkInfo chunk = receivedChunks.get(i);
                if (chunk == null) {
                    throw new IllegalStateException("Missing chunk: " + i);
                }
                fos.write(chunk.getData());
            }
        }
        return outputFile;
    }

    public int getReceivedChunkCount() {
        return receivedChunks.size();
    }
}
