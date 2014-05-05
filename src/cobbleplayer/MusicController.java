/**
 * Copyright (C) 2014 Jacob Moss
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>
 *
 */
package cobbleplayer;

import cobbleplayer.utilities.Util;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.util.Duration;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.tritonus.share.sampled.file.TAudioFileFormat;

/**
 *
 * @author Jacob
 */
public class MusicController {

    private final GUIController con;
    private Song song;
    private MediaPlayer player;
//    public SeekThread seekThread;
    public boolean playing = false;
    private double volume = 1, rate = 1, balance = 0;
    private final MusicController thisController = this;
    public final SeekTask seekTask = new SeekTask(this);

    public MusicController(GUIController con) {
        this.con = con;

    }

    boolean onetimeuse = true;

    /**
     * Plays a song, stopping the previous one (if any). Only to be called from
     * GUIController.
     *
     * @param song
     */
    public void play(Song song) {
        if (onetimeuse) {
            Util.err("Binding seekslider once");
            con.seeker.progressProperty().bind(seekTask.progressProperty());
            Thread seekerThread = new Thread(seekTask);
            seekerThread.setName("Seeker");
            seekerThread.start();
            onetimeuse = false;
        }
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
//                if (seekThread != null) {
//                    seekThread.end();
//                }
//                seekThread = new SeekThread(thisController);
//                Thread seekerThread = new Thread(seekThread);

            }

        });
        player.setOnEndOfMedia(new Runnable() {
            @Override
            public void run() {
                Util.err("End of song");
                con.songEnded();
            }
        });
        seekTask.resume();
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
        AudioInputStream in = AudioSystem.getAudioInputStream(file);
        return in.getFormat();
    }

    /**
     * Stops the JavaFX MediaPlayer and disposes it.
     */
    public void stop() {
        player.stop();
        player.dispose();
        player = null;
    }

    /**
     * Restarts the current song
     */
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

    @Deprecated
    public void update(boolean paused) {
        if (paused) {
            con.updateTimes(getPositionAsString());
        } else {
            con.updateTimes(getPositionAsString());
            con.updateSeeker((double) getPosition() / song.getSeconds());
        }

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
        seekTask.adjustTimerSetting((long) ((long) 1000/ value));
        rate = value;
    }

    /**
     * Returns how many seconds in to the song we are in
     *
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
        Util.err("End of song from seektask");
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

    /**
     * Checks whether the two songs are the same.
     *
     * @param compareSong
     * @return true if compareSong is equal to the current song
     */
    public boolean compareSong(Song compareSong) {
        return song.equals(compareSong);
    }
}
