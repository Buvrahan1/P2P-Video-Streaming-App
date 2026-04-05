package yeditepe.p2pstream.core;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Yerel (local) video klasörünü tarar ve VideoDescriptor listesi üretir.
 * Şimdilik sadece root klasördeki dosyalara bakıyoruz.
 */
public class LocalVideoCatalog {

    // Basit video uzantıları filtresi
    private static final String[] VIDEO_EXTENSIONS = {
            ".mp4", ".mkv", ".avi", ".mov", ".flv"
    };

    public List<VideoDescriptor> scanRootFolder() {
        AppConfig config = AppConfig.getInstance();
        File root = config.getRootVideoFolder();

        if (root == null || !root.isDirectory()) {
            return Collections.emptyList();
        }

        File[] files = root.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                String lower = name.toLowerCase();
                for (String ext : VIDEO_EXTENSIONS) {
                    if (lower.endsWith(ext)) {
                        return true;
                    }
                }
                return false;
            }
        });

        List<VideoDescriptor> result = new ArrayList<>();
        if (files != null) {
            for (File f : files) {
                result.add(new VideoDescriptor(f));
            }
        }
        return result;
    }
    
    public File findFileByName(String fileName) {
        AppConfig config = AppConfig.getInstance();
        File root = config.getRootVideoFolder();

        if (root == null || !root.isDirectory()) {
            return null;
        }

        File candidate = new File(root, fileName);
        if (candidate.isFile()) {
            return candidate;
        }
        return null;
    }
    
    public VideoCatalogEntry getCatalogEntryForFile(File file,
            java.net.InetAddress ownerAddr,
            int ownerTcpPort) throws Exception {
		
		byte[] data = java.nio.file.Files.readAllBytes(file.toPath());
		java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
		byte[] hashBytes = digest.digest(data);
		
		StringBuilder sb = new StringBuilder();
		for (byte b : hashBytes) {
			sb.append(String.format("%02x", b));
		}
		
		return new VideoCatalogEntry(
			file.getName(),
			sb.toString(),
			ownerAddr,
			ownerTcpPort
		);
    }

    
}
