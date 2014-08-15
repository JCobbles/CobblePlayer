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
package cobbleplayer.util;

import cobbleplayer.GUIController;
import cobbleplayer.Main;
import cobbleplayer.SettingsController;
import cobbleplayer.Song;
import cobbleplayer.UpdateTask;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.TimerTask;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Callback;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.UnsupportedAudioFileException;
import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

/*
 * utility methods 
 *
 * @author Jacob Moss
 */
public class Util {

    private static Random r = new Random();
    private static Label notifArea;
    public static boolean DEBUG;
    private static PrintWriter log;
    public static final Button buttonNo;
    public static final Button buttonCancel;
    public static final EventHandler<ActionEvent> cancelEvent;
    //constants
    public final static String ERRORLOG_FILENAME = "errorlog.cob";
    public final static String CONFIG_FILENAME = "Config.cob";
    public final static String APP_TITLE = "CobblePlayer";
    public final static String PLAYLIST_FILENAME = "Playlists.cob";
    public final static String NEW_BIT_CODE = "59y56x712x38y";
    public final static String END_CODE = "923728fj384539kf7";
    public final static String FILTERS_FILENAME = "Filters.cob";
    public final static int CURRENT_VERSION = 2;

    static {
        cancelEvent = (ActionEvent t) -> {
            ModalDialog.exit();
        };
        buttonNo = new Button("No");
        buttonNo.setOnAction(cancelEvent);
        buttonCancel = new Button("No");
        buttonCancel.setOnAction(cancelEvent);
        try {
            log = new PrintWriter(new BufferedWriter(new FileWriter(Util.ERRORLOG_FILENAME, true)));
        } catch (IOException ex) {
            Util.err("Error creating printwriter");
        }
    }

    public Util() {
    }

    public static void closeLog() {
        if (log != null) {
            log.close();
        }
    }

    /**
     * Prints the message to error file
     *
     * @param s the string to print
     */
    public static void log(String s) {
        log.println(s);
    }

    public static void print(String message) {
        notifArea.setText(message);
    }

    public static void err(Object message) {
        if (DEBUG) {
            System.err.println(message);
        }

    }

    public static void initiateTimerTask() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Util.checkSongFiltersAreUpdated(GUIController.filters);
            }
        };
        UpdateTask updater = new UpdateTask(task);
        Thread updateThread = new Thread(updater);
        updateThread.setName("updateThread");
        updateThread.start();
    }

    public static void checkArtistFiltersAreUpdated(List<SettingsController.ArtistFilterItem> list) {
        Calendar cal = Calendar.getInstance();
        List<SettingsController.ArtistFilterItem> deletionList = new ArrayList<>();
        for (SettingsController.ArtistFilterItem filter : list) {
            if (filter.millis < cal.getTimeInMillis()) {
                Util.err("removing a filter from the list\n" + cal.getTimeInMillis() + " filter: " + filter.millis);
                deletionList.add(filter);
            }
        }
        for (SettingsController.ArtistFilterItem item : deletionList) {
            GUIController.filters.remove(item);
        }
    }

    public static void checkSongFiltersAreUpdated(List<SettingsController.ArtistFilterItem> list) {
        for (SettingsController.ArtistFilterItem filter : list) {

        }
    }

    public static boolean filterArtistContains(List<SettingsController.ArtistFilterItem> list, String artist) {
        for (SettingsController.ArtistFilterItem filter : list) {
            if (filter.artist.equals(artist)) {
                err(filter.artist);
                return true;
            }
        }
        return false;
    }

    public static String getTitle(File input) {
        try {
            if (!input.exists()) {
                log("!input.exists()" + input.getAbsolutePath());
            }
            AudioFile f = AudioFileIO.read(input);
            Tag tag = f.getTag();

            if (tag.hasField(FieldKey.TITLE)) {
                return tag.getFirst(FieldKey.TITLE);
            } else {
                if (input.getName().isEmpty()) {
                    return "-";
                } else {
                    return input.getName();
                }
            }
//        if (file.hasID3v2Tag() && !file.getID3v2Tag().getSongTitle().isEmpty()) {
//            return file.getID3v2Tag().getSongTitle().replaceAll("[^A-Za-z0-9'\\p{M}\\- ]", "");
//        } else if (file.hasID3v1Tag() && !file.getID3v1Tag().getSongTitle().isEmpty()) {
//            return file.getID3v1Tag().getSongTitle().replaceAll("[^A-Za-z0-9'\\p{M}\\- ]", "");
//        } else {
//            return input.getName().isEmpty() ? input.getName() : "-";
//        }
        } catch (CannotReadException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException ex) {
//          ex.printStackTrace();
            return "-";
        }

    }

    public static String getArtist(File input) {
        try {
            if (!input.exists()) {
                log("!input.exists()" + input.getAbsolutePath());
            }
            AudioFile f = AudioFileIO.read(input);
            Tag tag = f.getTag();

            if (tag.hasField(FieldKey.ARTIST)) {
                String all = "";
                if (tag.getAll(FieldKey.ARTIST).size() > 1) {
                    for (String s : tag.getAll(FieldKey.ARTIST)) {
                        all += s + ";";
                    }
                }
                all = tag.getFirst(FieldKey.ARTIST);
                return all;
            } else {
                return "-";
            }
//        if (file.hasID3v2Tag() && !file.getID3v2Tag().getSongTitle().isEmpty()) {
//            return file.getID3v2Tag().getSongTitle().replaceAll("[^A-Za-z0-9'\\p{M}\\- ]", "");
//        } else if (file.hasID3v1Tag() && !file.getID3v1Tag().getSongTitle().isEmpty()) {
//            return file.getID3v1Tag().getSongTitle().replaceAll("[^A-Za-z0-9'\\p{M}\\- ]", "");
//        } else {
//            return input.getName().isEmpty() ? input.getName() : "-";
//        }
        } catch (CannotReadException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException ex) {
//          ex.printStackTrace();
            return "-";
        }
//        file = new MP3File(input);
//        if (file.hasID3v2Tag() && !file.getID3v2Tag().getLeadArtist().isEmpty()) {
//            return file.getID3v2Tag().getLeadArtist().replaceAll("[^A-Za-z0-9'\\p{M}\\- ]", "");
//        } else if (file.hasID3v1Tag() && !file.getID3v1Tag().getLeadArtist().isEmpty()) {
//            return file.getID3v1Tag().getLeadArtist().replaceAll("[^A-Za-z0-9'\\p{M}\\- ]", "");
//        } else {
//            return input.getName().isEmpty() ? input.getName() : "-";
//        }

    }

    public static String getAlbum(File input) throws IOException {
        try {
            if (!input.exists()) {
                log("!input.exists()" + input.getAbsolutePath());
            }
            AudioFile f = AudioFileIO.read(input);
            Tag tag = f.getTag();
            if (tag.hasField(FieldKey.ALBUM)) {
                return tag.getFirst(FieldKey.ALBUM);

            } else {
                return "-";
            }
//        if (file.hasID3v2Tag() && !file.getID3v2Tag().getSongTitle().isEmpty()) {
//            return file.getID3v2Tag().getSongTitle().replaceAll("[^A-Za-z0-9'\\p{M}\\- ]", "");
//        } else if (file.hasID3v1Tag() && !file.getID3v1Tag().getSongTitle().isEmpty()) {
//            return file.getID3v1Tag().getSongTitle().replaceAll("[^A-Za-z0-9'\\p{M}\\- ]", "");
//        } else {
//            return input.getName().isEmpty() ? input.getName() : "-";
//        }
        } catch (CannotReadException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException ex) {
//          ex.printStackTrace();
            return "-";
        }
    }

    public static int getDuration(File file) {
        try {
            AudioFileFormat baseFileFormat = new MpegAudioFileReader().getAudioFileFormat(file);
            Map properties = baseFileFormat.properties();
            Long duration = (Long) properties.get("duration");
            int mili = (int) (duration / 1000);
            return mili / 1000;
        } catch (IOException | UnsupportedAudioFileException ex) {
            err(ex.getMessage());
//            ex.printStackTrace();
            return 0;
        }
    }

    public static String getDurationAsString(File file) {
        try {
            AudioFileFormat baseFileFormat = new MpegAudioFileReader().getAudioFileFormat(file);
            Map properties = baseFileFormat.properties();
            Long duration = (Long) properties.get("duration");
            int mili = (int) (duration / 1000);
            return timeToString(mili / 1000);

        } catch (IOException | UnsupportedAudioFileException ex) {
            e(ex.getMessage() + "\n" + file.getPath(), "getDurationAsString");
            return "error";
        }
    }

    public static void e(Object message, String origin) {
        err("---" + origin + "---\n" + message + "\n---\\/---");
    }

    public static String timeToString(float seconds) {
        int sec = (int) (seconds % 60);
        int min = (int) (seconds / 60);
        if (sec > 9) {
            String time = (min + ":" + sec);
            return time;
        } else {
            String time = (min + ":0" + sec);
            return time;
        }
    }

    public static String timeToStringWithoutColon(float seconds) {
        int sec = (int) (seconds % 60);
        int min = (int) (seconds / 60);
        return min + "" + sec;
    }

    public void give(Label notifArea) {
        this.notifArea = notifArea;
    }

    public static BufferedWriter writeNewFile(String filename) throws IOException {
        return new BufferedWriter(new FileWriter(filename));
    }

    /**
     * Creates new modal prompting textual input
     *
     * @param message prompt to display
     * @return the inputted string
     */
    public static String getString(String message) {
        final Stage dialog = new Stage();
        Window owner = Main.getStage().getOwner();
        dialog.setTitle(message);
        dialog.initOwner(owner);
        dialog.initStyle(StageStyle.UTILITY);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setX(owner.getX());
        dialog.setY(owner.getY());

        final TextField textField = new TextField();
        final Button submitButton = new Button("Submit");
        submitButton.setDefaultButton(true);
        submitButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                dialog.close();
            }
        });
        textField.setMinHeight(TextField.USE_PREF_SIZE);

        final VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER_RIGHT);
        layout.setStyle("-fx-background-color: azure; -fx-padding: 10;");
        layout.getChildren().setAll(
                textField,
                submitButton
        );

        dialog.setScene(new Scene(layout));
        dialog.showAndWait();

        return textField.getText();
    }

    /**
     * @param text the text to display in the notification area
     */
    public void write(Object text) {
        notifArea.setText(text + "");
    }

    public static Properties openProp(String f) {
        try {
            Properties prop = new Properties();
            prop.load(new FileInputStream(f));
            return prop;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @param high the upper boundary in which to generate a random number
     * @param low the lower boundary
     * @return a List<Integer> of random numbers within the boundaries
     */
    public static List<Integer> randIntList(int high, int low) {
        int idx = 0;
        List<Integer> s = new ArrayList<Integer>();
        while (high != idx) {
            s.add(r.nextInt(high - low) + low);
            idx++;
        }
        return s;
    }

    /**
     * A random number between low and high
     *
     * @param low the lower boundary (inclusive)
     * @param high the upper boundary (exclusive)
     * @return integer
     */
    public static int randInt(int low, int high) {
        return r.nextInt(high - low) + low;
    }

    /**
     * @param f the list to find the average of elements within
     * @return the average
     */
    public static float calculateAverage(List<Float> f) {
        Iterator it = f.iterator();
        float sum = 0f;
        while (it.hasNext()) {
            sum += (float) it.next();
        }
        return sum / f.size();
    }

    /**
     * one time use
     */
    public List<TableColumn> initColumns() {
        TableColumn title = new TableColumn("Song");
        title.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Song, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Song, String> p) {
                return new ReadOnlyObjectWrapper(p.getValue().toString());
            }
        });
        title.setMinWidth(120);
        title.setMaxWidth(200);
        TableColumn artist = new TableColumn("Artist");
        artist.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Song, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Song, String> p) {
                return new ReadOnlyObjectWrapper(p.getValue().getArtist());
            }
        });
        artist.setMinWidth(80);
        artist.setMaxWidth(150);
        TableColumn album = new TableColumn("Album");
        album.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Song, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Song, String> p) {
                return new ReadOnlyObjectWrapper(p.getValue().getAlbum());
            }
        });
        album.setMinWidth(60);
        album.setMaxWidth(100);
        TableColumn duration = new TableColumn("Duration");
        duration.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Song, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Song, String> p) {
                return new ReadOnlyObjectWrapper(p.getValue().getDuration());
            }
        });
        duration.setMinWidth(30);
        List<TableColumn> columns = new ArrayList<>();
        columns.add(title);
        columns.add(artist);
        columns.add(album);
        columns.add(duration);
        return columns;
    }
}
