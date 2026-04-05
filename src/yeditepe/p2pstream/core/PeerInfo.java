package yeditepe.p2pstream.core;

import java.net.InetAddress;

public class PeerInfo {

    private final String peerId;
    private final InetAddress address;
    private final int tcpPort;

    private long lastSeen;

    public PeerInfo(String peerId, InetAddress address, int tcpPort, long lastSeen) {
        this.peerId = peerId;
        this.address = address;
        this.tcpPort = tcpPort;
        this.lastSeen = lastSeen;
    }

    public String getPeerId() {
        return peerId;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getTcpPort() {
        return tcpPort;
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }

    @Override
    public String toString() {
        return "Peer{" +
                "id=" + peerId +
                ", addr=" + address.getHostAddress() +
                ", tcpPort=" + tcpPort +
                ", lastSeen=" + lastSeen +
                '}';
    }
}
