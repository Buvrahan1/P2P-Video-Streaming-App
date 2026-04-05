package yeditepe.p2pstream.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

import yeditepe.p2pstream.core.AppConfig;
import yeditepe.p2pstream.core.LocalVideoCatalog;
import yeditepe.p2pstream.core.VideoDescriptor;
import java.io.File;
import java.util.List;
import yeditepe.p2pstream.core.PeerManager;
import yeditepe.p2pstream.core.UdpDiscoveryService;
import java.net.SocketException;
import yeditepe.p2pstream.core.TcpChunkServer;
import yeditepe.p2pstream.gui.ActiveStreamInfo;
import yeditepe.p2pstream.core.ChunkAssembler;
import yeditepe.p2pstream.core.MultiSourceChunkDownloader;
import yeditepe.p2pstream.core.NetworkVideoManager;
import yeditepe.p2pstream.gui.VlcjPlayerPanel;



public class MainWindow extends JFrame {

	// Available videos'ın arkasındaki gerçek nesneleri tutmak için
	private java.util.List<VideoDescriptor> availableVideos = new java.util.ArrayList<>();
	
	private PeerManager peerManager;
	private UdpDiscoveryService discoveryService;
	private boolean connectedToOverlay = false;
	private TcpChunkServer tcpChunkServer;
	private java.util.List<ActiveStreamInfo> activeStreams = new java.util.ArrayList<>();
	
	private NetworkVideoManager networkVideoManager = NetworkVideoManager.getInstance();

	
    // Üst kısım: arama
    private JTextField searchField;
    private JButton searchButton;

    // Sol kısım: listeler
    private JList<String> availableVideosList;
    private DefaultListModel<String> availableVideosModel;

    private JList<String> activeStreamsList;
    private DefaultListModel<String> activeStreamsModel;

    // Alt kısım: log ve buffer
    private JTextArea eventLogArea;
    private JLabel globalBufferStatusLabel;

    // Sağ kısım: video panel placeholder
 // private JPanel videoPanel;
    private VlcjPlayerPanel videoPlayer;


    public MainWindow() {
        super("P2P Video Streaming Application");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        createMenuBar();
        createMainLayout();

        logEvent("Application started.");
        
        peerManager = new PeerManager();

    }
    
    private void onLocalVideoSelected(VideoDescriptor vd) {

        if (!connectedToOverlay) {
            logEvent("Cannot start stream. Not connected to overlay network.");
            return;
        }

        String videoHash = networkVideoManager.findHashByName(vd.getDisplayName());
        if (videoHash == null) {
            logEvent("ERROR: Video not found in network catalog.");
            return;
        }

        int totalChunks = (int) ((vd.getFile().length() +
                yeditepe.p2pstream.core.ChunkUtils.CHUNK_SIZE - 1)
                / yeditepe.p2pstream.core.ChunkUtils.CHUNK_SIZE);

        logEvent("Starting MULTI-SOURCE stream for: " + vd.getDisplayName());

        ActiveStreamInfo streamInfo =
                new ActiveStreamInfo(vd.getDisplayName(), "MULTI-SOURCE", totalChunks);

        activeStreams.add(streamInfo);
        refreshActiveStreamsList();

        new Thread(() -> {
            try {
                ChunkAssembler assembler = new ChunkAssembler(totalChunks);

                MultiSourceChunkDownloader downloader =
                        new MultiSourceChunkDownloader();

                downloader.download(
                        videoHash,
                        totalChunks,
                        assembler,
                        networkVideoManager,
                        () -> {
                            streamInfo.incrementReceived();
                            SwingUtilities.invokeLater(this::refreshActiveStreamsList);
                        }
                );

                // Chunk'lar tamamlanana kadar BEKLE
                while (!assembler.isComplete()) {
                    Thread.sleep(100);
                }

                var output = new File(
                        yeditepe.p2pstream.core.AppConfig.getInstance().getBufferFolder(),
                        "multi_stream_" + vd.getDisplayName()
                );

                assembler.assembleToFile(output);
                logEvent("MULTI-SOURCE video saved to: " + output.getAbsolutePath());

                // VLCj ile oynat
                if (videoPlayer != null) {
                    videoPlayer.playMedia(output.getAbsolutePath());
                    logEvent("Started playback in embedded VLC player.");
                }

                streamInfo.setStatus("COMPLETED");


            } catch (Exception ex) {
                streamInfo.setStatus("ERROR");
                ex.printStackTrace();
            } finally {
                SwingUtilities.invokeLater(this::refreshActiveStreamsList);
            }

        }).start();
    }






    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // STREAM MENÜSÜ
        JMenu streamMenu = new JMenu("Stream");

        JMenuItem connectItem = new JMenuItem(new AbstractAction("Connect") {
            @Override
            public void actionPerformed(ActionEvent e) {
                onConnectClicked();
            }
        });


        JMenuItem disconnectItem = new JMenuItem(new AbstractAction("Disconnect") {
            @Override
            public void actionPerformed(ActionEvent e) {
                onDisconnectClicked();
            }
        });


        JMenuItem setRootFolderItem = new JMenuItem(new AbstractAction("Set Root Video Folder...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                chooseRootFolder();
            }
        });

        JMenuItem setBufferFolderItem = new JMenuItem(new AbstractAction("Set Buffer Folder...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                chooseBufferFolder();
            }
        });

        JMenuItem exitItem = new JMenuItem(new AbstractAction("Exit") {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        streamMenu.add(connectItem);
        streamMenu.add(disconnectItem);
        streamMenu.addSeparator();
        streamMenu.add(setRootFolderItem);
        streamMenu.add(setBufferFolderItem);
        streamMenu.addSeparator();
        streamMenu.add(exitItem);

        // HELP MENÜSÜ
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem(new AbstractAction("About") {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(
                        MainWindow.this,
                        "P2P Video Streaming Application\nDeveloper: [Senin İsmin]",
                        "About",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        });
        helpMenu.add(aboutItem);

        menuBar.add(streamMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }
    
    private void onConnectClicked() {
        if (connectedToOverlay) {
            logEvent("Already connected to overlay network.");
            return;
        }

        // UDP discovery
        discoveryService = new UdpDiscoveryService(peerManager);
        try {
            discoveryService.start();
            logEvent("Connected to overlay network (UDP discovery started).");
        } catch (SocketException ex) {
            logEvent("Failed to start UDP discovery: " + ex.getMessage());
            discoveryService = null;
        }

        // TCP chunk server
        tcpChunkServer = new TcpChunkServer();
        try {
            tcpChunkServer.start();
            logEvent("TCP chunk server started on port " +
                    yeditepe.p2pstream.core.AppConfig.getInstance().getTcpStreamingPort());
        } catch (Exception ex) {
            logEvent("Failed to start TCP chunk server: " + ex.getMessage());
            tcpChunkServer = null;
        }

        connectedToOverlay = true;
    }


    private void onDisconnectClicked() {
        if (!connectedToOverlay) {
            logEvent("Not connected to overlay network.");
            return;
        }

        if (discoveryService != null) {
            discoveryService.stop();
            discoveryService = null;
        }

        if (tcpChunkServer != null) {
            tcpChunkServer.stop();
            tcpChunkServer = null;
        }

        connectedToOverlay = false;

        logEvent("Disconnected from overlay network.");
    }


    
    
    private void createMainLayout() {
        // ÜST: Search alanı
        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        searchField = new JTextField();
        searchButton = new JButton("Search");

        searchButton.addActionListener(e -> {
            String query = searchField.getText().trim().toLowerCase();

            availableVideosModel.clear();
            for (VideoDescriptor vd : availableVideos) {
                if (query.isEmpty() || vd.getDisplayName().toLowerCase().contains(query)) {
                    availableVideosModel.addElement(vd.getDisplayName());
                }
            }

            logEvent("Search clicked for query: " + query + ". Showing "
                    + availableVideosModel.size() + " result(s).");
        });


        searchPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        searchPanel.add(new JLabel("Search Videos:"), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);

        add(searchPanel, BorderLayout.NORTH);

        // SOL: Available Videos + Active Streams
        availableVideosModel = new DefaultListModel<>();
        availableVideosList = new JList<>(availableVideosModel);
        availableVideosList.setBorder(
                BorderFactory.createTitledBorder("Available Videos on Network")
        );


        // Çift tıklama için MouseListener:
        availableVideosList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    try {
                        int index = availableVideosList.locationToIndex(e.getPoint());
                        if (index >= 0 && index < availableVideosModel.getSize()) {

                            String videoName = availableVideosModel.getElementAt(index);
                            onVideoSelectedByName(videoName);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        logEvent("ERROR on double-click: " + ex.getMessage());
                        javax.swing.JOptionPane.showMessageDialog(
                                MainWindow.this,
                                "An error occurred starting the stream:\n" + ex.getMessage(),
                                "Streaming Error",
                                javax.swing.JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
            }
        });



        activeStreamsModel = new DefaultListModel<>();
        activeStreamsList = new JList<>(activeStreamsModel);
        activeStreamsList.setBorder(
                BorderFactory.createTitledBorder("Active Streams")
        );

        JPanel listsPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        listsPanel.add(new JScrollPane(availableVideosList));
        listsPanel.add(new JScrollPane(activeStreamsList));

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        leftPanel.add(listsPanel, BorderLayout.CENTER);

        // SAĞ: Video panel placeholder
        videoPlayer = new VlcjPlayerPanel();
        videoPlayer.setBorder(BorderFactory.createTitledBorder("Video Player"));

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 5));
        rightPanel.add(videoPlayer, BorderLayout.CENTER);

        // Video alanına çift tıklayınca full-screen toggle
        videoPlayer.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    videoPlayer.toggleFullScreen();
                }
            }
        });


        JSplitPane centerSplit = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                leftPanel,
                rightPanel
        );
        centerSplit.setResizeWeight(0.35); // soldaki listeler %35 yer kaplasın

        add(centerSplit, BorderLayout.CENTER);

        // ALT: Global Buffer Status + Event Log
        globalBufferStatusLabel = new JLabel("Global Buffer Status: 0% (no active stream)");
        globalBufferStatusLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        eventLogArea = new JTextArea(5, 20);
        eventLogArea.setEditable(false);
        JScrollPane logScroll = new JScrollPane(eventLogArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("Event Log"));

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(globalBufferStatusLabel, BorderLayout.NORTH);
        bottomPanel.add(logScroll, BorderLayout.CENTER);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void chooseRootFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selected = chooser.getSelectedFile();

            // Config'e yaz
            AppConfig.getInstance().setRootVideoFolder(selected);
            logEvent("Root Video Folder set to: " + selected.getAbsolutePath());

            refreshAvailableVideosFromLocal();

        }
    }


    private void chooseBufferFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selected = chooser.getSelectedFile();

            AppConfig.getInstance().setBufferFolder(selected);
            logEvent("Buffer Folder set to: " + selected.getAbsolutePath());
        }
    }


    public void logEvent(String message) {
        eventLogArea.append(message + "\n");
        eventLogArea.setCaretPosition(eventLogArea.getDocument().getLength());
    }

    public void setGlobalBufferStatus(String statusText) {
        globalBufferStatusLabel.setText("Global Buffer Status: " + statusText);
    }
    
    private void refreshAvailableVideosFromNetwork() {
        availableVideosModel.clear();

        for (String name : networkVideoManager.getAllVideoNames()) {
            availableVideosModel.addElement(name);
        }

        logEvent("Network video catalog refreshed. Found "
                + availableVideosModel.size() + " video(s).");
    }
    
    private void refreshAvailableVideosFromLocal() {
        LocalVideoCatalog catalog = new LocalVideoCatalog();
        java.util.List<VideoDescriptor> list = catalog.scanRootFolder();

        availableVideos.clear();
        availableVideos.addAll(list);

        availableVideosModel.clear();
        for (VideoDescriptor vd : availableVideos) {
            availableVideosModel.addElement(vd.getDisplayName());
        }

        logEvent("Local video catalog refreshed. Found "
                + availableVideos.size() + " video(s).");
    }

    
    private void refreshActiveStreamsList() {
        activeStreamsModel.clear();
        for (ActiveStreamInfo s : activeStreams) {
            activeStreamsModel.addElement(s.toString());
        }

        // Global Buffer Status: Şimdilik basit – aktif stream'lerin ortalama progress'i
        if (activeStreams.isEmpty()) {
            setGlobalBufferStatus("0% (no active stream)");
        } else {
            int sum = 0;
            for (ActiveStreamInfo s : activeStreams) {
                sum += s.getProgressPercent();
            }
            int avg = sum / activeStreams.size();
            setGlobalBufferStatus(avg + "% (" + activeStreams.size() + " active stream(s))");
        }
    }
    
    private void onVideoSelectedByName(String videoName) {
        // Local listeden karşılık gelen VideoDescriptor'u bul
        VideoDescriptor vd = null;
        for (VideoDescriptor v : availableVideos) {
            if (v.getDisplayName().equals(videoName)) {
                vd = v;
                break;
            }
        }

        if (vd == null) {
            logEvent("Could not find local descriptor for video: " + videoName);
            return;
        }

        onLocalVideoSelected(vd); // zaten multi-source chunk download yapan metot
    }

}
