import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class JavaAudioPlayer {
    private ArrayList<File> playlist;
    private int currentTrackIndex;
    private Clip clip;
    private FloatControl volumeControl;

    public JavaAudioPlayer() {
        playlist = new ArrayList<>();
        currentTrackIndex = -1;
    }

    public void addToPlaylist(File audioFile) {
        playlist.add(audioFile);
    }

    public void playTrack(int index) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        if (index >= 0 && index < playlist.size()) {
            if (clip != null && clip.isRunning()) {
                clip.stop();
            }

            File audioFile = playlist.get(index);
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(audioFile);
            clip = AudioSystem.getClip();
            clip.open(audioIn);
            volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            clip.start();
            currentTrackIndex = index;
        }
    }

    public void play() {
        if (clip != null && !clip.isRunning()) {
            clip.start();
        }
    }

    public void pause() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }

    public void stop() {
        if (clip != null) {
            clip.stop();
            clip.close();
        }
    }

    public void setVolume(float volume) {
        if (volumeControl != null) {
            volumeControl.setValue(volume);
        }
    }

    public FloatControl getVolumeControl() {
        return volumeControl;
    }

    public long getClipLength() {
        return clip != null ? clip.getMicrosecondLength() : 0;
    }

    public long getClipPosition() {
        return clip != null ? clip.getMicrosecondPosition() : 0;
    }

    public void setClipPosition(long microseconds) {
        if (clip != null) {
            clip.setMicrosecondPosition(microseconds);
        }
    }

    public String getCurrentTrackName() {
        if (currentTrackIndex >= 0 && currentTrackIndex < playlist.size()) {
            return playlist.get(currentTrackIndex).getName();
        }
        return "";
    }

    public String getCurrentTrackMetadata() {
        return getCurrentTrackName();
    }
}
