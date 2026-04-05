package yeditepe.p2pstream.core;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class NetworkVideoManager {

    private static final NetworkVideoManager INSTANCE = new NetworkVideoManager();

    // fileHash -> List<VideoCatalogEntry>
    private final Map<String, List<VideoCatalogEntry>> videoMap = new ConcurrentHashMap<>();

    private NetworkVideoManager() {
    }

    public static NetworkVideoManager getInstance() {
        return INSTANCE;
    }

    public synchronized void addEntry(VideoCatalogEntry entry) {
        videoMap
            .computeIfAbsent(entry.getFileHash(), k -> new ArrayList<>())
            .add(entry);
    }

    public synchronized Set<String> getAllVideoNames() {
        Set<String> names = new HashSet<>();
        for (List<VideoCatalogEntry> list : videoMap.values()) {
            for (VideoCatalogEntry e : list) {
                names.add(e.getFileName());
            }
        }
        return names;
    }

    public synchronized List<VideoCatalogEntry> getSourcesForHash(String hash) {
        return videoMap.getOrDefault(hash, Collections.emptyList());
    }

    public synchronized String findHashByName(String fileName) {
        for (var entry : videoMap.entrySet()) {
            for (var v : entry.getValue()) {
                if (v.getFileName().equals(fileName)) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }
}
