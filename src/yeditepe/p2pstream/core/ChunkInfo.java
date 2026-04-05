package yeditepe.p2pstream.core;

public class ChunkInfo {

    private final int chunkIndex;
    private final byte[] data;
    private final String hash;

    public ChunkInfo(int chunkIndex, byte[] data, String hash) {
        this.chunkIndex = chunkIndex;
        this.data = data;
        this.hash = hash;
    }

    public int getChunkIndex() {
        return chunkIndex;
    }

    public byte[] getData() {
        return data;
    }

    public String getHash() {
        return hash;
    }
}
