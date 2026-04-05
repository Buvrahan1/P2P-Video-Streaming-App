package yeditepe.p2pstream.core;

import java.io.File;
import java.util.UUID;


/**
 * Uygulama genel ayarlarını tutan basit singleton sınıf.
 * - Root Video Folder
 * - Buffer Folder
 * - UDP / TCP portları (ileride kullanacağız)
 */
public class AppConfig {

    private static AppConfig INSTANCE = new AppConfig();

    private File rootVideoFolder;
    private File bufferFolder;
    private String peerId = UUID.randomUUID().toString();


    // İleride discovery/streaming için kullanacağız:
    private int udpDiscoveryPort = 50000;
    private int tcpStreamingPort = 50010;

    private AppConfig() {
    }

    public static AppConfig getInstance() {
        return INSTANCE;
    }

    public File getRootVideoFolder() {
        return rootVideoFolder;
    }

    public void setRootVideoFolder(File rootVideoFolder) {
        this.rootVideoFolder = rootVideoFolder;
    }

    public File getBufferFolder() {
        return bufferFolder;
    }

    public void setBufferFolder(File bufferFolder) {
        this.bufferFolder = bufferFolder;
    }

    public int getUdpDiscoveryPort() {
        return udpDiscoveryPort;
    }

    public int getTcpStreamingPort() {
        return tcpStreamingPort;
    }
    
    public String getPeerId() {
        return peerId;
    }

}
