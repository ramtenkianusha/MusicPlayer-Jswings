import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.sound.sampled.*;

public class AdvancedMusicPlayer extends JFrame implements ActionListener {
    private JTextField filePathField;
    private JButton playButton;
    private JButton pauseButton;
    private JButton stopButton;
    private JButton chooseButton;
    private JButton loopButton;
    private JButton addToPlaylistButton;
    private JSlider volumeSlider;
    private JSlider seekSlider;
    private JLabel currentTimeLabel;
    private JLabel totalTimeLabel;
    private JLabel trackInfoLabel;
    private boolean isPaused;
    private boolean isLooping;
    private JFileChooser fileChooser;
    private JavaAudioPlayer audioPlayer;
    private Timer timer;

    public AdvancedMusicPlayer() {
        super("Advanced Music Player");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(4, 1));

        filePathField = new JTextField(20);
        playButton = new JButton("Play");
        pauseButton = new JButton("Pause");
        stopButton = new JButton("Stop");
        chooseButton = new JButton("Choose File");
        loopButton = new JButton("Loop");
        addToPlaylistButton = new JButton("Add to Playlist");
        volumeSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
        seekSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
        currentTimeLabel = new JLabel("00:00");
        totalTimeLabel = new JLabel("/ 00:00");
        trackInfoLabel = new JLabel("Track Info: None");

        playButton.addActionListener(this);
        pauseButton.addActionListener(this);
        stopButton.addActionListener(this);
        chooseButton.addActionListener(this);
        loopButton.addActionListener(this);
        addToPlaylistButton.addActionListener(this);
        volumeSlider.addChangeListener(e -> adjustVolume());
        seekSlider.addChangeListener(e -> seekTrack());

        JPanel filePanel = new JPanel();
        filePanel.add(filePathField);
        filePanel.add(chooseButton);
        filePanel.add(addToPlaylistButton);

        JPanel controlPanel = new JPanel();
        controlPanel.add(playButton);
        controlPanel.add(pauseButton);
        controlPanel.add(stopButton);
        controlPanel.add(loopButton);
        controlPanel.add(new JLabel("Volume:"));
        controlPanel.add(volumeSlider);

        JPanel timePanel = new JPanel();
        timePanel.add(currentTimeLabel);
        timePanel.add(seekSlider);
        timePanel.add(totalTimeLabel);

        JPanel infoPanel = new JPanel();
        infoPanel.add(trackInfoLabel);

        add(filePanel);
        add(controlPanel);
        add(timePanel);
        add(infoPanel);

        fileChooser = new JFileChooser(".");
        fileChooser.setFileFilter(new FileNameExtensionFilter("WAV Files", "wav"));

        audioPlayer = new JavaAudioPlayer();

        setSize(600, 200);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == playButton) {
            playMusic();
        } else if (event.getSource() == pauseButton) {
            pauseMusic();
        } else if (event.getSource() == stopButton) {
            stopMusic();
        } else if (event.getSource() == chooseButton) {
            chooseFile();
        } else if (event.getSource() == addToPlaylistButton) {
            addToPlaylist();
        } else if (event.getSource() == loopButton) {
            toggleLoop();
        }
    }

    private void playMusic() {
        if (filePathField.getText().isEmpty() && audioPlayer.getCurrentTrackName().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please choose a file or add to the playlist first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            if (filePathField.getText().isEmpty()) {
                audioPlayer.play();
            } else {
                audioPlayer.addToPlaylist(new File(filePathField.getText()));
                audioPlayer.playTrack(audioPlayer.getCurrentTrackName().isEmpty() ? 0 : audioPlayer.getCurrentTrackName().length() - 1);
            }
            isPaused = false;
            pauseButton.setText("Pause");
            updateTrackInfo();
            updateTotalTimeLabel();
            startTimer();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error playing file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void pauseMusic() {
        if (isPaused) {
            audioPlayer.play();
            pauseButton.setText("Pause");
            isPaused = false;
        } else {
            audioPlayer.pause();
            pauseButton.setText("Resume");
            isPaused = true;
        }
    }

    private void stopMusic() {
        audioPlayer.stop();
        if (timer != null) {
            timer.stop();
        }
        seekSlider.setValue(0);
        currentTimeLabel.setText("00:00");
        pauseButton.setText("Pause");
        isPaused = false;
    }

    private void chooseFile() {
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            filePathField.setText(selectedFile.getAbsolutePath());
        }
    }

    private void addToPlaylist() {
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            audioPlayer.addToPlaylist(selectedFile);
            JOptionPane.showMessageDialog(this, "File added to playlist.", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void toggleLoop() {
        isLooping = !isLooping;
        loopButton.setText(isLooping ? "Stop Loop" : "Loop");
        if (isLooping && !audioPlayer.getCurrentTrackName().isEmpty()) {
            try {
                audioPlayer.playTrack(audioPlayer.getCurrentTrackName().length() - 1);
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                JOptionPane.showMessageDialog(this, "Error playing track in loop: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void adjustVolume() {
        float volume = volumeSlider.getValue() / 100.0f * (audioPlayer.getVolumeControl().getMaximum() - audioPlayer.getVolumeControl().getMinimum()) + audioPlayer.getVolumeControl().getMinimum();
        audioPlayer.setVolume(volume);
    }

    private void seekTrack() {
        long position = seekSlider.getValue() * audioPlayer.getClipLength() / 100;
        audioPlayer.setClipPosition(position);
    }

    private void updateTotalTimeLabel() {
        long microseconds = audioPlayer.getClipLength();
        int seconds = (int) (microseconds / 1_000_000);
        int minutes = seconds / 60;
        seconds %= 60;
        totalTimeLabel.setText(String.format("/ %02d:%02d", minutes, seconds));
    }

    private void startTimer() {
        timer = new Timer(1000, e -> updateCurrentTimeLabel());
        timer.start();
    }

    private void updateCurrentTimeLabel() {
        long microseconds = audioPlayer.getClipPosition();
        int seconds = (int) (microseconds / 1_000_000);
        int minutes = seconds / 60;
        seconds %= 60;
        currentTimeLabel.setText(String.format("%02d:%02d", minutes, seconds));
        seekSlider.setValue((int) (microseconds * 100 / audioPlayer.getClipLength()));
    }

    private void updateTrackInfo() {
        String trackInfo = audioPlayer.getCurrentTrackMetadata();
        trackInfoLabel.setText("Track Info: " + trackInfo);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AdvancedMusicPlayer::new);
    }
}
