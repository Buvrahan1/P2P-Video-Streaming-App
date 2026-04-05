package yeditepe.p2pstream.core;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Ağdaki peer'ları takip eder.
 */
public class PeerManager {

    // peerId -> PeerInfo
    private final Map<String, PeerInfo> peers = new HashMap<>();

    public synchronized void updatePeer(PeerInfo peer) {
        peers.put(peer.getPeerId(), peer);
    }

    public synchronized void removePeer(String peerId) {
        peers.remove(peerId);
    }

    public synchronized Collection<PeerInfo> getPeers() {
        return Collections.unmodifiableCollection(peers.values());
    }

    public synchronized int getPeerCount() {
        return peers.size();
    }
}
