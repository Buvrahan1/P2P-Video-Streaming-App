package yeditepe.p2pstream.gui;

public class ActiveStreamInfo {

    private final String videoName;
    private final String source; // örn: "127.0.0.1:50010"
    private final int totalChunks;
    private int receivedChunks;
    private String status; // "DOWNLOADING", "COMPLETED", "ERROR"

    public ActiveStreamInfo(String videoName, String source, int totalChunks) {
        this.videoName = videoName;
        this.source = source;
        this.totalChunks = totalChunks;
        this.status = "DOWNLOADING";
        this.receivedChunks = 0;
    }

    public void incrementReceived() {
        this.receivedChunks++;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getTotalChunks() {
        return totalChunks;
    }

    public int getReceivedChunks() {
        return receivedChunks;
    }

    public String getVideoName() {
        return videoName;
    }

    public String getSource() {
        return source;
    }

    public String getStatus() {
        return status;
    }

    public int getProgressPercent() {
        if (totalChunks == 0) return 0;
        return (int) Math.round(receivedChunks * 100.0 / totalChunks);
    }

    @Override
    public String toString() {
        // Listede görünecek metin
        return source + " | " + videoName +
                " | " + getProgressPercent() + "% (" +
                receivedChunks + "/" + totalChunks + ") | " + status;
    }
}
