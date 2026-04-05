package yeditepe.p2pstream;

import javax.swing.SwingUtilities;

import com.sun.jna.NativeLibrary;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;
import yeditepe.p2pstream.gui.MainWindow;

public class Main {

    public static void main(String[] args) {

        NativeLibrary.addSearchPath("libvlc", "C:\\Program Files\\VideoLAN\\VLC");

        boolean found = new NativeDiscovery().discover();
        System.out.println("VLC native library found = " + found);

        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            window.setVisible(true);
        });
    }
}
