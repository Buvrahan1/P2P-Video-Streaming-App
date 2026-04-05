package yeditepe.p2pstream.core;

import java.net.InetAddress;

public class VideoCatalogEntry {

    private final String fileName;
    private final String fileHash;
    private final InetAddress ownerAddress;
    private final int ownerTcpPort;

    public VideoCatalogEntry(String fileName, String fileHash,
                             InetAddress ownerAddress, int ownerTcpPort) {
        this.fileName = fileName;
        this.fileHash = fileHash;
        this.ownerAddress = ownerAddress;
        this.ownerTcpPort = ownerTcpPort;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileHash() {
        return fileHash;
    }

    public InetAddress getOwnerAddress() {
        return ownerAddress;
    }

    public int getOwnerTcpPort() {
        return ownerTcpPort;
    }

    @Override
    public String toString() {
        return fileName + " @ " +
                ownerAddress.getHostAddress() + ":" + ownerTcpPort;
    }
}
