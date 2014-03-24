package cobbleplayer.utilities;

import cobbleplayer.Main;
import cobbleplayer.Song;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
    //constants
    public final static String CONFIG_FILENAME = "Config.cob";
    public final static String PLAYLIST_FILENAME = "Playlists.cob";
    public final static String PLAYLIST_CODE = "59y56x712x38y";
    public final static String END_CODE = "923728fj384539kf7";

    public Util() {
    }

    public static void log(Exception e) {
        
    }

    public static void print(String message) {
        notifArea.setText(message);
    }

    public static void err(Object message) {
        if(DEBUG) {
            System.err.println(message);
        }
        
    }

    public static String getTitle(File input) throws TagException, IOException {

        file = new MP3File(input);
        if (file.hasID3v2Tag()) {
            return file.getID3v2Tag().getSongTitle();
        } else if (file.hasID3v1Tag()) {
            return file.getID3v1Tag().getSongTitle();
        } else {
            return input.getName().isEmpty() ? input.getName() : "Error";
        }

    }

    public static String getArtist(File input) throws IOException, TagException {
        file = new MP3File(input);
        if (file.hasID3v2Tag()) {
            return file.getID3v2Tag().getLeadArtist();
        } else if (file.hasID3v1Tag()) {
            return file.getID3v1Tag().getLeadArtist();
        } else {
            return input.getName().isEmpty() ? input.getName() : "Error";
        }

    }

    public static String getAlbum(File input) throws IOException, TagException {
        file = new MP3File(input);
        if (file.hasID3v2Tag()) {
            return file.getID3v2Tag().getAlbumTitle();
        } else if (file.hasID3v1Tag()) {
            return file.getID3v1Tag().getAlbumTitle();
        } else {
            return input.getName().isEmpty() ? input.getName() : "Error";
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
            int sec = (mili / 1000) % 60;
            int min = (mili / 1000) / 60;
            if (String.valueOf(sec).length() == 1) {
                String time = (min + ":0" + sec);
                return time;
            } else {
                String time = (min + ":" + sec);
                return time;
            }

        } catch (Exception ex) {
            err(ex.getMessage());
            ex.printStackTrace();
            return "error";
        }
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
    public List<Integer> generateRand(int high, int low) {
        int idx = 0;
        List<Integer> s = new ArrayList<Integer>();
        while (high != idx) {
            s.add(r.nextInt(high - low) + low);
            idx++;
        }
        return s;
    }

    public int rand(int high, int low) {
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
