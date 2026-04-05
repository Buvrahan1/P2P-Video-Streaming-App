package yeditepe.p2pstream.core;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;

/**
 * UDP tabanlı peer discovery servisi.
 * Belirli aralıklarla broadcast HELLO mesajları gönderir,
 * gelen HELLO'ları dinler ve PeerManager'ı günceller.
 *
 * NOT: Burada "limited-scope flooding" mantığını,
 * LAN içi broadcast + periyodik HELLO ile sağlıyoruz.
 */
public class UdpDiscoveryService implements Runnable {

    private static final String MSG_TYPE_HELLO = "HELLO";
    // HELLO gönderme aralığı (ms)
    private static final long HELLO_INTERVAL_MS = 5000;
    
    private static final String MSG_TYPE_VIDEO = "VIDEO";
    
    private final PeerManager peerManager;
    private final AppConfig config;
    private final NetworkVideoManager networkVideoManager =
            NetworkVideoManager.getInstance();



    private volatile boolean running = false;
    private Thread thread;
    private DatagramSocket socket;

    private long lastHelloSentTime = 0;

    public UdpDiscoveryService(PeerManager peerManager) {
        this.peerManager = peerManager;
        this.config = AppConfig.getInstance();
    }

    public synchronized void start() throws SocketException {
        if (running) return;

        int port = config.getUdpDiscoveryPort();
        socket = new DatagramSocket(port);
        socket.setBroadcast(true);
        socket.setSoTimeout(1000); // receive için 1 sn timeout

        running = true;
        thread = new Thread(this, "UdpDiscoveryService");
        thread.start();
    }

    public synchronized void stop() {
        running = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    @Override
    public void run() {
        log("UDP discovery started on port " + config.getUdpDiscoveryPort());

        byte[] buf = new byte[1024];

        while (running) {
            long now = System.currentTimeMillis();

            // Periyodik HELLO gönder
            if (now - lastHelloSentTime >= HELLO_INTERVAL_MS) {
                sendHello();
                sendLocalVideoCatalog();
                lastHelloSentTime = now;
            }

            // Gelen paketleri dinle
            try {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet); // timeout varsa 1 sn sonra exception fırlatır

                handlePacket(packet);
            } catch (SocketTimeoutException e) {
                // Sadece tekrar döngüye gir
            } catch (SocketException e) {
                if (running) {
                    log("Socket exception in discovery: " + e.getMessage());
                }
            } catch (IOException e) {
                log("IO exception in discovery: " + e.getMessage());
            }
        }

        log("UDP discovery stopped.");
    }

    private void sendHello() {
        String peerId = config.getPeerId();
        int tcpPort = config.getTcpStreamingPort();

        // Basit mesaj formatı: HELLO|peerId|tcpPort
        String msg = MSG_TYPE_HELLO + "|" + peerId + "|" + tcpPort;
        byte[] data = msg.getBytes(StandardCharsets.UTF_8);

        try {
            DatagramPacket packet = new DatagramPacket(
                    data,
                    data.length,
                    InetAddress.getByName("255.255.255.255"),
                    config.getUdpDiscoveryPort()
            );
            socket.send(packet);
            log("Sent HELLO broadcast.");
        } catch (IOException e) {
            log("Failed to send HELLO: " + e.getMessage());
        }
    }
    
    private void sendLocalVideoCatalog() {
        try {
            var catalog = new LocalVideoCatalog();
            var config = AppConfig.getInstance();
            var root = config.getRootVideoFolder();
            if (root == null) return;

            var files = root.listFiles();
            if (files == null) return;

            for (var f : files) {
                if (!f.isFile()) continue;

                var entry = catalog.getCatalogEntryForFile(
                        f,
                        InetAddress.getLocalHost(),
                        config.getTcpStreamingPort()
                );

                String msg = MSG_TYPE_VIDEO + "|" +
                        entry.getFileName() + "|" +
                        entry.getFileHash() + "|" +
                        entry.getOwnerTcpPort();

                byte[] data = msg.getBytes(java.nio.charset.StandardCharsets.UTF_8);

                DatagramPacket packet = new DatagramPacket(
                        data,
                        data.length,
                        InetAddress.getByName("255.255.255.255"),
                        config.getUdpDiscoveryPort()
                );

                socket.send(packet);
            }

        } catch (Exception e) {
            log("VIDEO broadcast error: " + e.getMessage());
        }
    }


    private void handlePacket(DatagramPacket packet) {
        String received = new String(packet.getData(), packet.getOffset(),
                packet.getLength(), StandardCharsets.UTF_8).trim();

        String[] parts = received.split("\\|");
        if (parts.length < 3) {
            return; // geçersiz paket
        }

        String type = parts[0];
        if (MSG_TYPE_VIDEO.equals(type)) {
            String fileName = parts[1];
            String fileHash = parts[2];
            int tcpPort = Integer.parseInt(parts[3]);

            InetAddress addr = packet.getAddress();

            VideoCatalogEntry entry =
                    new VideoCatalogEntry(fileName, fileHash, addr, tcpPort);

            networkVideoManager.addEntry(entry);

            log("Discovered VIDEO from peer: " + entry);
            return;
        }


        if (!MSG_TYPE_HELLO.equals(type)) {
            return; // şu an sadece HELLO biliyoruz
        }

        String peerId = parts[1];
        int tcpPort;
        try {
            tcpPort = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            return;
        }

        // Kendimizden gelen HELLO'yu yok say
        if (peerId.equals(config.getPeerId())) {
            return;
        }

        InetAddress addr = packet.getAddress();
        long now = System.currentTimeMillis();

        PeerInfo peer = new PeerInfo(peerId, addr, tcpPort, now);
        peerManager.updatePeer(peer);

        log("Discovered/updated peer: " + peer);
    }

    private void log(String msg) {
        System.out.println("[UdpDiscovery] " + msg);
        // İLERİDE GUI'ye log aktarabiliriz
    }
}
