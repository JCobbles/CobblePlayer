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

import cobbleplayer.utilities.ModalDialog;
import cobbleplayer.utilities.PlaylistHolder;
import cobbleplayer.utilities.UpdateController;
import cobbleplayer.utilities.Util;
import com.sun.javafx.perf.PerformanceTracker;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.application.Preloader.ProgressNotification;
import javafx.application.Preloader.StateChangeNotification;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

/**
 *
 * @author Jacob
 */
public class Main extends Application {

    private static String APP_TITLE = "CobblePlayer";
    private static Scene scene;
    private static Stage stage;
    public static Scene settings;
    public static Scene analyser;
    public static SettingsController sC;
    public static AnalysisController aC;
    public static GUIController guiController;
    private static URL analysisResource, settingsResource, guiResource;
    public static PerformanceTracker perfTracker;
    BooleanProperty ready = new SimpleBooleanProperty(false);
    private final static String versionURL = "http://cobbles.biz.ht/updates/cobblePlayer/version.html";
    private final static String historyURL = "http://cobbles.biz.ht/updates/cobblePlayer/history.html";
    public static Stage dialog;

    public String getLatestVersion() throws IOException {
        String data = getData(versionURL);
        return data.substring(data.indexOf("[version]") + 9, data.indexOf("[/version]"));
    }

    public String getChangelog() throws IOException {
        String data = getData(historyURL);
        return data.substring(data.indexOf("[history]") + 9, data.indexOf("[/history]"));
    }

    private String getData(String address) throws MalformedURLException, IOException {
        URL url = new URL(address);
        InputStream html = url.openStream();
        int c = 0;
        StringBuffer buffer = new StringBuffer("");

        while (c != -1) {
            c = html.read();
            buffer.append((char) c);
        }
        return buffer.toString();
    }

    public void update(boolean update) {
        if (update) {
            System.err.println("Updating...");
            notifyPreloader(new Preloader.ErrorNotification("UPDATE", "UPDATE", new Throwable()));
            dialog.close();
//            System.exit(0);
        } else {
//            task.notify();
        }

    }
    static boolean pause;
    Task task;
    Main main = this;

    private void startup() {
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                int max = 4;
                String address = "";
                boolean sQM = true;
                int samples = 15;
                boolean shuffle = false;
                for (int i = 1; i <= max; i++) {
                    if (i == 1) { //check for updates @TODO
                        Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
                        Util.DEBUG = true;
                        if (Integer.parseInt(getLatestVersion()) > Util.CURRENT_VERSION) {
                            Util.err("Outdated version...");
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        dialog = new Stage();
                                        dialog.initStyle(StageStyle.UTILITY);
                                        dialog.initModality(Modality.APPLICATION_MODAL);
                                        final FXMLLoader load = new FXMLLoader(getClass().getResource("/resources/update.fxml"));
                                        UpdateController upC = new UpdateController(getChangelog(), main);
                                        load.setController(upC);
                                        Scene scene = new Scene((AnchorPane) load.load());
                                        dialog.setScene(scene);
                                        dialog.show();
                                    } catch (IOException ex) {
                                        Util.err(ex.getLocalizedMessage());
                                    }
                                }
                            });
                        } else {
                            Util.err("Up to date version");
                        }

                    } else if (i == 2) { //
                        Util.err("i == 2, loading analysis resource");

                        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                            @Override
                            public void handle(final WindowEvent e) {
                                e.consume();
                                close();
                            }
                        });
                        notifyPreloader(new Preloader.ErrorNotification("CHANGE_MESSAGE", "Loading settings...", null));
                    } else if (i == 3) { //load settings
                        Util.err("i == 3, loading previous session");
                        try {
                            Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
                            Properties prop = new Properties();
                            prop.load(new FileInputStream(Util.CONFIG_FILENAME));
                            if (prop.getProperty("autoload") != null) {
                                if (prop.getProperty("autoload").equalsIgnoreCase("true")) {
                                    try {
                                        List<Playlist> playlists = new ArrayList<>();
                                        BufferedReader br = new BufferedReader(new FileReader(Util.PLAYLIST_FILENAME));
                                        String line;
                                        Playlist curp = null;
                                        while ((line = br.readLine()) != null) {

                                            if (!line.isEmpty() && !line.equals("") && !line.equals(Util.END_CODE)) {
//                                                Util.err("Line: " + line);
                                                if (line.contains(Util.NEW_BIT_CODE)) {
                                                    line = line.substring(Util.NEW_BIT_CODE.length());
                                                    if (curp == null) {
                                                        curp = new Playlist(line, null);
                                                    } else {
                                                        playlists.add(curp);
                                                        curp = new Playlist(line, null);
                                                    }
                                                } else {
                                                    try {
                                                        curp.addSongUnimported(line);
                                                    } catch (IOException e) {
//                                                        Util.err(e.getLocalizedMessage());
                                                    }
                                                }
                                            } else if (line.equals(Util.END_CODE)) {
                                                playlists.add(curp);
                                            }
                                        }
                                        br.close();
                                        System.err.println("Adding " + playlists.size() + " playlists...");
                                        addPlaylists(playlists);
                                    } catch (IOException e) {
                                        Util.err(e.getLocalizedMessage());
                                    }
                                }

                            } //end autoload null check 
                            sQM = prop.getProperty("showQuitMsg") != null ? prop.getProperty("showQuitMsg").equalsIgnoreCase("true") : true;//if not null, else use default
                            samples = prop.getProperty("samples") != null ? Integer.parseInt(prop.getProperty("samples")) : 15;
                            shuffle = prop.getProperty("shuffle") != null ? prop.getProperty("shuffle").equals("true") : false;
                            System.err.println("Loaded settings");
                        } catch (IOException ex) {
                            Util.err(ex.getMessage());
                            sQM = true;
                        }
                    } else if (i == 4) {
                        System.err.println("i == 4, loading GUI");
//                        notifyPreloader(new Preloader.ErrorNotification("CHANGE_MESSAGE", "Loading past session...", null));
                        try {

                            GUIController.show = sQM;
                            GUIController.samples = samples;
                            GUIController.shuffle = shuffle;
//            GUIController.importFiles(songList);
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {

                                }
                            });
                            Screen screen = Screen.getPrimary();
                            Rectangle2D bounds = screen.getVisualBounds();
                            stage.setX(bounds.getMinX());
                            stage.setY(bounds.getMinY());
                            stage.setWidth(bounds.getWidth() - 50);
                            stage.setHeight(bounds.getHeight() - 50);
                            stage.getIcons().add(new Image(Main.class.getResourceAsStream("/resources/favicon.png")));
                            stage.setTitle(APP_TITLE);
//                            stage.show();
//                            perfTracker = PerformanceTracker.getSceneTracker(scene);

                            System.err.println("Loaded GUI");
                        } catch (NullPointerException ex) {
                            //No point in continuing if scene is not found/loaded
                            Util.err(ex.getLocalizedMessage());
                            ex.printStackTrace();
                        }
                    }
                    // Send progress to preloader
                    notifyPreloader(new ProgressNotification(((double) i) / max));
                }
                // After init is ready, the app is ready to be shown
                // Do this before hiding the preloader stage to prevent the 
                // app from exiting prematurely
                ready.setValue(Boolean.TRUE);

                notifyPreloader(new StateChangeNotification(
                        StateChangeNotification.Type.BEFORE_START));

                return null;
            }
        };
        new Thread(task).start();
    }
    private List<Playlist> playlists;

    @Override
    public void start(Stage primaryStage) {

        this.stage = primaryStage;
        analysisResource = getClass().getResource("/resources/analyser.fxml");
        settingsResource = getClass().getResource("/resources/settings.fxml");
        guiResource = getClass().getResource("/resources/GUI2.fxml");
        ready.addListener((ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
            if (Boolean.TRUE.equals(t1)) {
                Platform.runLater(() -> {
                    try {
                        //analyser
                        final FXMLLoader analysisLoader = new FXMLLoader(analysisResource);
                        aC = new AnalysisController();
                        analysisLoader.setController(aC);
                        //settings
                        final FXMLLoader settingsLoader = new FXMLLoader(settingsResource);
                        sC = new SettingsController();
                        settingsLoader.setController(sC);
                        //gui
                        final FXMLLoader load = new FXMLLoader(guiResource);
                        guiController = new GUIController();
                        load.setController(guiController);
                        analyser = new Scene((AnchorPane) analysisLoader.load());
                        settings = new Scene((AnchorPane) settingsLoader.load());
                        scene = new Scene((AnchorPane) load.load());
                        stage.setScene(scene);
                        scene.widthProperty().addListener((ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) -> {
                            guiController.resetMainSplitPane();
                        });
                        if (playlists != null) {
                            guiController.setPlaylists(playlists);
                            guiController.addPreviousSongs.set(true);
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        Util.err(ex.getLocalizedMessage());
                    }
                    stage.show();
                });
            }
        });
//            final FXMLLoader load = new FXMLLoader(getClass().getResource("/resources/GUI2.fxml"));
//          
//            final GUIController guiController = new GUIController();
//            load.setController(guiController);
//            scene = new Scene((AnchorPane) load.load());
//            stage.setScene(scene);
        startup();

    }

    private void addPlaylists(List<Playlist> playlists) {
        this.playlists = playlists;
    }
//    public static void reloadAC() {
//        try {
//            FXMLLoader l = new FXMLLoader(analysisResource);
//            l.setController(aC);
//            analyser = new Scene((AnchorPane) l.load());
//        } catch (IOException ex) {
//            Util.log(ex);
//        }
//    }

    public static void close() {
        if (GUIController.show) {
            Button yes = new Button("Yes");
            yes.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent paramT) {
                    closeWithoutDialog();
                }
            });

            Button no = new Button("No");
            no.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent paramT) {
                    ModalDialog.exit();
                }
            });
            List<Button> buttons = new ArrayList<>();
            buttons.add(yes);
            buttons.add(no);
            new ModalDialog("Close", "Are you sure you want to close?", buttons, 250, 75);
        } else {
            closeWithoutDialog();
        }

    }

    public static void setTitle(String title) {
        APP_TITLE = title;
        stage.setTitle(APP_TITLE);
    }

    public static Stage getStage() {
        return stage;
    }

    public static void closeWithoutDialog() {
        saveConfig();
        Util.closeLog(); //force unprinted bytes to be logged and close the stream
        System.exit(0);
    }

    public static void saveConfig() {
        Properties prop = new Properties();
        try {
            if (GUIController.autoload) {
                PlaylistHolder hold = new PlaylistHolder(guiController.getPlaylists());
                Util.err("Saving...\n" + guiController.getPlaylists().size() + " playlist(s) saved");
                prop.setProperty("autoload", "true");
            } else {
                prop.setProperty("autoload", "false");
            }
            prop.setProperty("shuffle", GUIController.shuffle ? "true" : "false");
            prop.setProperty("showQuitMsg", GUIController.show ? "true" : "false");
            prop.setProperty("samples", "" + GUIController.samples);
            prop.store(new FileOutputStream(Util.CONFIG_FILENAME, false), null); //root folder
        } catch (IOException ex) {
            Util.err(ex.getMessage());
        }
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
