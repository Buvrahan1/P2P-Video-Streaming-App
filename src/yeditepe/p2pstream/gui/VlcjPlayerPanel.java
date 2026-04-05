package yeditepe.p2pstream.gui;

import javax.swing.*;
import java.awt.*;

import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;

public class VlcjPlayerPanel extends JPanel {

    private final EmbeddedMediaPlayerComponent mediaPlayerComponent;

    public VlcjPlayerPanel() {
        super(new BorderLayout());
        mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
        this.add(mediaPlayerComponent, BorderLayout.CENTER);
    }

    public void playMedia(String path) {
        mediaPlayerComponent.mediaPlayer().media().play(path);
    }

    public void stop() {
        mediaPlayerComponent.mediaPlayer().controls().stop();
    }

    public void pause() {
        mediaPlayerComponent.mediaPlayer().controls().pause();
    }

    public void toggleFullScreen() {
        mediaPlayerComponent.mediaPlayer().fullScreen().toggle();
    }

    public EmbeddedMediaPlayerComponent getMediaPlayerComponent() {
        return mediaPlayerComponent;
    }
}
