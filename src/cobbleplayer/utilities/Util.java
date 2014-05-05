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
package cobbleplayer.utilities;

import cobbleplayer.Main;
import cobbleplayer.Song;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
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
import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;
import org.farng.mp3.MP3File;
import org.farng.mp3.TagException;

/*
 * Just some utility methods 
 */
/**
 *
 * @author Jacob Moss
 */
public class Util {

    private Random r = new Random();
    private static Label notifArea;
    private static MP3File file;
    public static boolean DEBUG;
    private static PrintWriter log;
    //constants
    public final static String ERRORLOG_FILENAME = "errorlog.cob";
    public final static String CONFIG_FILENAME = "Config.cob";
    public final static String PLAYLIST_FILENAME = "Playlists.cob";
    public final static String PLAYLIST_CODE = "59y56x712x38y";
    public final static String END_CODE = "923728fj384539kf7";

    static {
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

    public static String getTitle(File input) throws TagException, IOException {
        if (!input.exists()) {
            log("!input.exists()" + input.getAbsolutePath());
        }
        file = new MP3File(input);
        if (file.hasID3v2Tag() && !file.getID3v2Tag().getSongTitle().isEmpty()) {
            return file.getID3v2Tag().getSongTitle().replaceAll("[^A-Za-z0-9'é ]", "");
        } else if (file.hasID3v1Tag() && !file.getID3v1Tag().getSongTitle().isEmpty()) {
            return file.getID3v1Tag().getSongTitle().replaceAll("[^A-Za-z0-9'é ]", "");
        } else {
            return input.getName().isEmpty() ? input.getName() : "-";
        }

    }

    public static String getArtist(File input) throws IOException, TagException {
        file = new MP3File(input);
        if (file.hasID3v2Tag() && !file.getID3v2Tag().getLeadArtist().isEmpty()) {
            return file.getID3v2Tag().getLeadArtist().replaceAll("[^A-Za-z0-9'é ]", "");
        } else if (file.hasID3v1Tag() && !file.getID3v1Tag().getLeadArtist().isEmpty()) {
            return file.getID3v1Tag().getLeadArtist().replaceAll("[^A-Za-z0-9'é ]", "");
        } else {
            return input.getName().isEmpty() ? input.getName() : "-";
        }

    }

    public static String getAlbum(File input) throws IOException, TagException {
        file = new MP3File(input);
        if (file.hasID3v2Tag() && !file.getID3v2Tag().getAlbumTitle().isEmpty()) {
            return file.getID3v2Tag().getAlbumTitle().replaceAll("[^A-Za-z0-9'é ]", "");
        } else if (file.hasID3v1Tag() && !file.getID3v1Tag().getAlbumTitle().isEmpty()) {
            return file.getID3v1Tag().getAlbumTitle().replaceAll("[^A-Za-z0-9'é ]", "");
        } else {
            return input.getName().isEmpty() ? input.getName() : "-";
        }

    }

    public static int getDuration(File file) {
        try {
            AudioFileFormat baseFileFormat = new MpegAudioFileReader().getAudioFileFormat(file);
            Map properties = baseFileFormat.properties();
            Long duration = (Long) properties.get("duration");
            int mili = (int) (duration / 1000);
            return mili / 1000;
        } catch (Exception ex) {
            err(ex.getMessage());
            ex.printStackTrace();
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

        } catch (Exception ex) {
            err(ex.getMessage());
            ex.printStackTrace();
            return "error";
        }
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

    /**
     * Creates new modal prompting textual input
     *
     * @param message prompt to display
     * @return the inputted string
     */
    public String getString(String message) {
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
    public List<Integer> randIntList(int high, int low) {
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
    public int randInt(int low, int high) {
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
        TableColumn artist = new TableColumn("Artist");
        artist.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Song, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Song, String> p) {
                return new ReadOnlyObjectWrapper(p.getValue().getArtist());
            }
        });
        artist.setMinWidth(80);
        TableColumn album = new TableColumn("Album");
        album.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Song, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Song, String> p) {
                return new ReadOnlyObjectWrapper(p.getValue().getAlbum());
            }
        });
        album.setMinWidth(60);
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
