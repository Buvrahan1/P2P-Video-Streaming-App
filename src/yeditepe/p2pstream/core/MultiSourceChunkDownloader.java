package yeditepe.p2pstream.core;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiSourceChunkDownloader {

    private final ExecutorService executor =
            Executors.newFixedThreadPool(4); // 4 paralel peer

    public void download(
            String videoHash,
            int totalChunks,
            ChunkAssembler assembler,
            NetworkVideoManager networkVideoManager,
            Runnable onProgress
    ) {

        List<VideoCatalogEntry> sources =
                networkVideoManager.getSourcesForHash(videoHash);

        for (int i = 0; i < totalChunks; i++) {
            final int chunkIndex = i;
            final VideoCatalogEntry source =
                    sources.get(i % sources.size()); // round-robin

            executor.submit(() -> {
                try {
                    TcpChunkClient client = new TcpChunkClient();

                    ChunkInfo chunk = client.requestChunk(
                            source.getOwnerAddress(),
                            source.getOwnerTcpPort(),
                            source.getFileName(),
                            chunkIndex
                    );

                    if (chunk != null) {
                        assembler.addChunk(chunk);
                        onProgress.run();   // GUI update tetikler
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public void shutdown() {
        executor.shutdown();
    }
}
