package yeditepe.p2pstream.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Elindeki videolar için chunk isteği alan TCP sunucu.
 * Basit protokol:
 *   Client -> UTF videoName, int chunkIndex
 *   Server -> boolean found
 *             [found == true ise] int chunkIndex, int length, byte[] data
 */
public class TcpChunkServer implements Runnable {

    private final AppConfig config;
    private final LocalVideoCatalog catalog;

    private volatile boolean running = false;
    private Thread thread;
    private ServerSocket serverSocket;

    public TcpChunkServer() {
        this.config = AppConfig.getInstance();
        this.catalog = new LocalVideoCatalog();
    }

    public synchronized void start() throws IOException {
        if (running) return;

        int port = config.getTcpStreamingPort();
        serverSocket = new ServerSocket(port);
        running = true;

        thread = new Thread(this, "TcpChunkServer");
        thread.start();

        log("TCP chunk server started on port " + port);
    }

    public synchronized void stop() {
        running = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException ignored) {
            }
        }
    }

    @Override
    public void run() {
        while (running) {
            try {
                Socket client = serverSocket.accept();
                new Thread(() -> handleClient(client), "TcpChunkClientHandler").start();
            } catch (IOException e) {
                if (running) {
                    log("Error accepting connection: " + e.getMessage());
                }
            }
        }
        log("TCP chunk server stopped.");
    }

    private void handleClient(Socket socket) {
        try (Socket s = socket;
             DataInputStream in = new DataInputStream(s.getInputStream());
             DataOutputStream out = new DataOutputStream(s.getOutputStream())) {

            String videoName = in.readUTF();
            int chunkIndex = in.readInt();

            log("Request for video=" + videoName + " chunk=" + chunkIndex);

            File file = catalog.findFileByName(videoName);
            if (file == null) {
                log("File not found: " + videoName);
                out.writeBoolean(false);
                out.flush();
                return;
            }

            ChunkInfo chunk = ChunkUtils.readSingleChunk(file, chunkIndex);
            if (chunk == null) {
                log("Chunk out of range for file: " + videoName);
                out.writeBoolean(false);
                out.flush();
                return;
            }

            out.writeBoolean(true);
            out.writeInt(chunk.getChunkIndex());
            out.writeInt(chunk.getData().length);
            out.write(chunk.getData());
            out.flush();

            log("Sent chunk " + chunk.getChunkIndex() + " (" + chunk.getData().length + " bytes)");

        } catch (IOException e) {
            log("Error handling client: " + e.getMessage());
        } catch (Exception e) {
            log("Error reading chunk: " + e.getMessage());
        }
    }

    private void log(String msg) {
        System.out.println("[TcpChunkServer] " + msg);
    }
}
