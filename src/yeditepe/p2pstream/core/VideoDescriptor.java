package yeditepe.p2pstream.core;

import java.io.File;

/**
 * Tek bir video dosyasını temsil eder.
 * İleride:
 *  - Hash (dosyanın kimliği)
 *  - Chunk sayısı
 *  - Dosya boyutu vb.
 * ekleyeceğiz.
 */
public class VideoDescriptor {

    private final File file;
    private final String displayName;

    public VideoDescriptor(File file) {
        this.file = file;
        this.displayName = file.getName();
    }

    public File getFile() {
        return file;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
