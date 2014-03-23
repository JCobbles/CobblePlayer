package cobbleplayer;

import cobbleplayer.utilities.Util;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.util.Duration;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.tritonus.share.sampled.TAudioFormat;
import org.tritonus.share.sampled.file.TAudioFileFormat;

/**
 *
 * @author Jacob
 */
public class MusicController {

    private GUIController con;
    private Song song;
    private MediaPlayer player;
    private SeekThread seekThread;
    public boolean playing = false;
    private double volume = 1, rate = 1, balance = 0;
    private MusicController thisController = this;

    public MusicController(GUIController con) {
        this.con = con;
    }

    public void play(Song song) {
        if (player != null) {
            stop();
        }
        this.song = song;
        try {
            Media audioFile = new Media(new File(song.getFilepath()).toURI().toString());
            player = new MediaPlayer(audioFile);
            set();
            player.play();
        } catch (Exception e) {
            Util.print(e.getMessage());
        }
    }

    private void set() {
        con.set(song);
        setVolume(volume);
        setRate(rate);
        setBalance(balance);
        player.statusProperty().addListener(new ChangeListener<Status>() {
            @Override
            public void changed(ObservableValue<? extends Status> ov, Status t, Status t1) {
                switch (t1) {
                    case PLAYING:
                        Util.print("Playing");
                        playing = true;
                        break;
                    case PAUSED:
                        playing = false;
                        break;
                    case STOPPED:
                        playing = false;
                        break;
                }
            }
        });
        player.setOnPlaying(new Runnable() {
            @Override
            public void run() {
                if (seekThread != null) {
                    seekThread.end();
                }
                seekThread = new SeekThread(thisController);
                Thread seekerThread = new Thread(seekThread);
                seekerThread.setName("Seeker");
                seekerThread.start();
            }

        });
        player.setOnEndOfMedia(new Runnable() {
            @Override
            public void run() {
                Util.err("End of song");
                con.songEnded();
            }
        });
    }

    @Deprecated
    public Object getMeta(String key) throws UnsupportedAudioFileException, IOException {
        File file = new File(song.getFilepath());
        AudioFileFormat baseFileFormat = null;
        baseFileFormat = AudioSystem.getAudioFileFormat(file);
        AudioFormat baseFormat = baseFileFormat.getFormat();
// TAudioFileFormat properties
        if (baseFileFormat instanceof TAudioFileFormat) {
            System.err.println("Yes");
            Map properties = ((TAudioFileFormat) baseFileFormat).properties();
            return properties.get(key);
//            key = "mp3.id3tag.v2";
//            InputStream tag = (InputStream) properties.get(key);
        } else {
            return null;
        }
    }
    public AudioFormat getFormat() throws UnsupportedAudioFileException, IOException {
        File file = new File(song.getFilepath());
        AudioInputStream in= AudioSystem.getAudioInputStream(file);
        return in.getFormat();
    }

    public void stop() {
        player.stop();
        player.dispose();
        player = null;
    }

    public void restart() {
        player.stop();
        player.play();
    }

    public void setVolume(double vol) {
        player.setVolume(vol);
        volume = vol;
    }

    public double getVolume() {
        return player.getVolume();
    }

    public void pause() {
        player.pause();
    }

    public void resume() {
        if (player == null) {
            if (con.musicTable.getItems().size() > 0) {
                play((Song) con.musicTable.getItems().get(0));
            }
        } else {
            player.play();
        }
//        con.pauseToggleButton.setGraphic(iPause); TODO
    }

    public void update() {
        con.updateSeeker((double) getPosition() / song.getSeconds());
        con.updateTimes(getPositionAsString());
    }

    /**
     * sets the balance of volume; Limit: -1 to 1
     *
     * @param value
     */
    public void setBalance(double value) {
        balance = value;
        player.setBalance(value);
    }

    public void seek(double value) {
        player.seek(Duration.seconds(value));
    }

    /**
     * Sets the rate of playback; the limit is 0-8
     *
     * @param value the new rate
     */
    public void setRate(double value) {
        player.setRate(value);
        rate = value;
    }

    /**
     * @return seconds
     */
    public int getPosition() {
        try {
            return (int) player.getCurrentTime().toSeconds();
        } catch (Exception e) {
            return 0;
        }
    }

    public void songEnded() {
        con.songEnded();
    }

    /**
     * @return the current position in the form eg 4:06
     */
    public String getPositionAsString() {
        int sec = getPosition() % 60;
        int min = getPosition() / 60;
        if (sec < 10) {
            return (min + ":0" + sec);
        } else {
            return (min + ":" + sec);
        }
    }

    public Song getCurrent() {
        return song;
    }

    public boolean compareSong(Song compareSong) {
        return song.equals(compareSong);
    }
}
