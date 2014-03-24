/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cobbleplayer;

import cobbleplayer.utilities.ModalDialog;
import cobbleplayer.utilities.PlaylistHolder;
import cobbleplayer.utilities.Util;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
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
    private static URL analysisResource;

    @Override
    public void start(Stage primaryStage) {
        analysisResource = getClass().getResource("/resources/analyser.fxml");
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(final WindowEvent e) {
                e.consume();
                close();
            }
        });

        this.stage = primaryStage;

        boolean sQM;
        int samples = 15;
        try {
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
                                Util.err("Line: " + line);
                                if (line.contains(Util.PLAYLIST_CODE)) {
                                    line = line.substring(Util.PLAYLIST_CODE.length());
                                    if (curp == null) {
                                        curp = new Playlist(line, null);
                                    } else {
                                        playlists.add(curp);
                                        curp = new Playlist(line, null);
//                                        System.err.println(line) ;
                                    }
                                } else {
                                    try {
                                        curp.addSongUnimported(line);
                                    } catch (Exception e) {
                                        Util.err("BROAD EXCEPTION\n" + e.getMessage());
                                    }
                                }
                            } else if (line.equals(Util.END_CODE)) {
                                playlists.add(curp);
                            }
                        }
                        br.close();
                        System.err.println("Adding " + playlists.size() + " playlists...");
                        GUIController.setPlaylists(playlists);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            } //end autoload null check 
            sQM = prop.getProperty("showQuitMsg") != null ? prop.getProperty("showQuitMsg").equalsIgnoreCase("true") : true;
            samples = prop.getProperty("samples") != null ? Integer.parseInt(prop.getProperty("samples")) : 15;
        } catch (IOException ex) {
            Util.err(ex.getMessage());
            sQM = true;
        }
        try {
            //gui
            FXMLLoader load = new FXMLLoader(getClass().getResource("/resources/GUI.fxml"));
            load.setController(new GUIController());
            //settings
            FXMLLoader load2 = new FXMLLoader(getClass().getResource("/resources/settings.fxml"));
            sC = new SettingsController();
            load2.setController(sC);
            settings = new Scene((AnchorPane) load2.load());
            //analyser
            FXMLLoader load3 = new FXMLLoader(analysisResource);
            aC = new AnalysisController();
            load3.setController(aC);
            analyser = new Scene((AnchorPane) load3.load());
            GUIController.show = sQM;
            GUIController.samples = samples;
//            System.err.println(GUIController.getPlaylists().get(0).getName());
//            
//            GUIController.importFiles(songList);
            scene = new Scene((AnchorPane) load.load());
            primaryStage.setScene(scene);
            Screen screen = Screen.getPrimary();
            Rectangle2D bounds = screen.getVisualBounds();
            primaryStage.setX(bounds.getMinX());
            primaryStage.setY(bounds.getMinY());
            primaryStage.setWidth(bounds.getWidth());
            primaryStage.setHeight(bounds.getHeight());

            primaryStage.setTitle(APP_TITLE);
            primaryStage.show();

            Util.DEBUG = true;

        } catch (IOException ex) {
            //No point in continuing if scene is not found/loaded
            ex.printStackTrace();
        }
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
                    endClose();
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
            endClose();
        }

    }

    public static void setTitle(String title) {
        APP_TITLE = title;
        stage.setTitle(APP_TITLE);
    }

    public static Stage getStage() {
        return stage;
    }

    public static void endClose() {
        saveConfig();

        System.exit(0);
    }

    public static void saveConfig() {
        Properties prop = new Properties();
        try {
            if (GUIController.autoload) {
                PlaylistHolder hold = new PlaylistHolder(GUIController.getPlaylists());
                Util.err("Saving\n" + GUIController.getPlaylists().size() + " playlist(s)");
                prop.setProperty("autoload", "true");
            } else {
                prop.setProperty("autoload", "false");
            }
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
