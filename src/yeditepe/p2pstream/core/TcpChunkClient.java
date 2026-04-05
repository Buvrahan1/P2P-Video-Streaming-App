package yeditepe.p2pstream.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Başka bir peer'dan tek bir chunk isteyen istemci.
 */
public class TcpChunkClient {

    public ChunkInfo requestChunk(InetAddress address, int tcpPort,
                                  String videoName, int chunkIndex) throws Exception {

        try (Socket socket = new Socket(address, tcpPort);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream())) {

            // İstek gönder
            out.writeUTF(videoName);
            out.writeInt(chunkIndex);
            out.flush();

            boolean found = in.readBoolean();
            if (!found) {
                System.out.println("[TcpChunkClient] Chunk not found on remote.");
                return null;
            }

            int idx = in.readInt();
            int len = in.readInt();
            byte[] data = new byte[len];
            in.readFully(data);

            System.out.println("[TcpChunkClient] Received chunk " + idx + " (" + len + " bytes)");
            return new ChunkInfo(idx, data, null); // hash şimdilik null; istersen hesaplayabilirsin
        }
    }
}
