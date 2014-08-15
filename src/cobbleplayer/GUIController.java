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

import cobbleplayer.loadingtasks.Loaders;
import cobbleplayer.util.FileImporter;
import cobbleplayer.util.ImportListener;
import cobbleplayer.util.ModalDialog;
import cobbleplayer.util.Toast;
import cobbleplayer.util.TrackChange;
import cobbleplayer.util.Util;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.media.EqualizerBand;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.Duration;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * FXML Controller class
 *
 * @author Jacob Moss This also acts as main interface class
 */
public class GUIController implements Initializable, ImportListener {

    @FXML
    ComboBox moreInfo, presets;
    @FXML
    Button pauseToggleButton, repeatButton, shuffleButton, titleField, previousButton, nextButton, boredButton, resetButton;
    @FXML
    private SplitPane mainSplitPane; //musicSplitPane
    @FXML
    private TableView musicTable;
    @FXML
    ProgressBar seeker;
    @FXML
    Slider seekSlider, volSlider, rateSlider, balanceSlider, eq32, eq64, eq125, eq250, eq500, eq1k, eq2k, eq4k, eq8k, eq16k;
    @FXML
    ListView playlists;
    @FXML
    Label startTime, endTime, notifArea, items;
    @FXML
    TextField albumField, artistField, comboField;//encodingArea, channelsArea, durfield, pDescriptionField, pNameField
    @FXML
    TextField genreArea;
    @FXML
    MenuBar menuBar;
    @FXML
    MenuItem settings, analyser;
    @FXML
    AnchorPane leftPaneSplit;
    @FXML
    Region veil;
    @FXML
    ProgressIndicator multiuseProgressIndicator;

    public Region getVeil() {
        return veil;
    }

    public ProgressIndicator getMultiuseProgressIndicator() {
        return multiuseProgressIndicator;
    }

    public TrackChange getTrackChange() {
        return trackChange;
    }
    
    private final ObservableList<Song> data = FXCollections.observableArrayList();
    public final ObservableList<Playlist> playlistData = FXCollections.observableArrayList();
    private final Util util = new Util();
    private boolean setSeeker = true, repeat = false;
    private final ImageView iPlay = new ImageView("/resources/play.png"), iPause = new ImageView("/resources/pause.png");
    private String encoding, channels;
    public static boolean autoload = true, show = true, shuffle = false;
    private List<Playlist> importList = new ArrayList<>();
    public Playlist library;
    public final GUIController activeController = this;
    private final MusicController musicController = new MusicController(this);
    public static int samples;
    public static ModalDialog settingsModal;
    public static ModalDialog analyserModal;
    private final TrackChange trackChange = new TrackChange(this);

    private final Slider[] sliders = new Slider[10];
    public static ObservableList<SettingsController.ArtistFilterItem> filters = FXCollections.observableArrayList();
//    public static List<SettingsController.ArtistFilterItem> filters = new ArrayList<>();
    public BooleanProperty addPreviousSongs = new SimpleBooleanProperty(false);

    /**
     * Initialises the controller class.
     *
     * @param url the path to FXML doc?
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        util.give(notifArea);
//        util.write("Loading...");
        intiateGUIListeners();
        addPreviousSongs.addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            Util.err("Importing songs & playlists from previous session...");
            playlistData.remove(library);
            playlistData.addAll(importList);
            playlists.getSelectionModel().clearAndSelect(0);
            Main.sC.populateFilterDropdowns(musicTable);
        });
        library = new Playlist("Library", data);
        playlistData.add(library);

        initiateTable();
        initiatePlaylists();
        util.write("Loaded.");

        Util.initiateTimerTask();
    }

    private void intiateGUIListeners() {
        boredButton.setOnAction((ActionEvent event) -> {
            actionSettings(new ActionEvent());
            Main.sC.changeTab(SettingsController.FILTER_TAB);
        });
//        boredButton.setOnAction(new EventHandler<ActionEvent>() {
//            @Override
//            public void handle(ActionEvent t) {
//                actionSettings(new ActionEvent());
//                Main.sC.changeTab(SettingsController.FILTER_TAB);
//            }
//        });
        mainSplitPane.setDividerPosition(0, 0.18);
        mainSplitPane.getDividers().get(0).positionProperty().addListener((ObservableValue<? extends Number> ov, Number t, Number t1) -> {
            if (t1.doubleValue() > 0.22) {
                mainSplitPane.setDividerPosition(0, 0.22);
            } else if (t1.doubleValue() < 0.05) {
                mainSplitPane.setDividerPosition(0, 0.05);
            }
        });
        SplitPane.setResizableWithParent(leftPaneSplit, Boolean.FALSE);
        resetMainSplitPane();
        sliders[0] = eq32;
        sliders[1] = eq64;
        sliders[2] = eq125;
        sliders[3] = eq250;
        sliders[4] = eq500;
        sliders[5] = eq1k;
        sliders[6] = eq2k;
        sliders[7] = eq4k;
        sliders[8] = eq8k;
        sliders[9] = eq16k;
        for (int i = 0; i < 10; i++) {
            final int fi = i;
            sliders[i].setMin(EqualizerBand.MIN_GAIN);
            sliders[i].setMax(EqualizerBand.MAX_GAIN);
            sliders[i].setValue(0);
            sliders[i].setMinorTickCount(0);
            sliders[i].setMajorTickUnit(3);//(EqualizerBand.MAX_GAIN - EqualizerBand.MIN_GAIN) / 9
            sliders[i].valueProperty().addListener((ObservableValue<? extends Number> arg0, Number oldValue, Number newValue) -> {
                if (musicController.getCurrent() != null) {
                    musicController.getAudioEqualizer().getBands().get(fi).setGain(newValue.doubleValue());
                }
            });
        }
        ObservableList<String> infos = FXCollections.observableArrayList("Time", "Channels", "Encoding");
        moreInfo.setItems(infos);
        moreInfo.getSelectionModel().clearAndSelect(0);
        moreInfo.getSelectionModel().selectedIndexProperty().addListener((ObservableValue<? extends Number> ov, Number t, Number t1)
                -> setMoreInfoBox(t1.intValue()));
        ObservableList<String> presetList = FXCollections.observableArrayList("General", "R&B/Dance", "Piano", "Pop", "Rap", "Classical");
        presets.setItems(presetList);
        presets.getSelectionModel().selectedIndexProperty().addListener((ObservableValue<? extends Number> ov, Number t, Number t1)
                -> selectPreset(t1.intValue()));
        rateSlider.valueProperty().addListener((ObservableValue<? extends Number> ov, Number t, Number t1) -> {
            if (musicController.getCurrent() != null) {
                musicController.setRate(t1.doubleValue());
            }
        });
        balanceSlider.valueProperty().addListener((ObservableValue<? extends Number> ov, Number t, Number t1) -> {
            if (musicController.getCurrent() != null) {
                musicController.setBalance(t1.doubleValue());
            }
        });
        volSlider.setMax(1);
        volSlider.setValue(1);
        volSlider.valueProperty().addListener((ObservableValue<? extends Number> ov, Number t, Number t1) -> {
            if (musicController.getCurrent() != null) {
                musicController.setVolume(t1.doubleValue());

            }
        });
//        seek.setMax(1);
//        seeker.progressProperty().bind(seekSlider.valueProperty());
        seeker.progressProperty().addListener((ObservableValue<? extends Number> ov, Number t, Number time1) -> {
            if (setSeeker) {
                startTime.setText(musicController.getPositionAsString());
                seekSlider.setValue(musicController.getPosition());
            }
        });
//        seekSlider.setMin(0);
        seekSlider.valueProperty().addListener((ObservableValue<? extends Number> ov, Number old_val, Number new_val) -> {
            if (!setSeeker) {
                startTime.setText(Util.timeToString((new_val.floatValue())));
            }
        });
        shuffleButton.setDefaultButton(shuffle);

        multiuseProgressIndicator.setMaxSize(150, 150);
        veil.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4)");
    }

    private void initiateTable() {
        //
        multiuseProgressIndicator.setVisible(true);
        veil.setVisible(true);
        final Task<TableView> createTableTask = new Loaders.TableCreator(this);

        createTableTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                multiuseProgressIndicator.setVisible(true);
                veil.setVisible(true);
                musicTable = createTableTask.getValue();
            }
        });
        //

    }

    private void initiatePlaylists() {
        playlists.setItems(playlistData);
        ContextMenu conMenu = new ContextMenu();

        MenuItem delete = new MenuItem("Delete");
        delete.setOnAction((ActionEvent t) -> {
            final Playlist toDelete = ((Playlist) playlists.getSelectionModel().getSelectedItem());
            Button yes = new Button("Yes");
            yes.setOnAction((ActionEvent t1) -> {
                ModalDialog.exit();
                playlists.getItems().remove(toDelete);
            });
            Button no = new Button("No");
            no.setOnAction(Util.cancelEvent);
            List<Button> buttons = new ArrayList<>();
            buttons.add(yes);
            buttons.add(no);
            new ModalDialog("Confirm deletion", "Are you sure you want to delete this playlist '"
                    + toDelete.getName() + "'?", buttons, 350, 70);
        });
        final TextField nameF = new TextField();
        conMenu.getItems().add(delete);
        nameF.setStyle("-fx-background-color: black;");
        nameF.setOnKeyTyped((KeyEvent t) -> {
            Playlist p = ((Playlist) playlists.getSelectionModel().getSelectedItem());
            Util.err("Playlist " + p.getName() + " changing name to " + nameF.getText());
            playlistData.remove(p);
            p.setName(nameF.getText());
            playlistData.add(p);
            playlists.getSelectionModel().select(p);
        });
        CustomMenuItem name = new CustomMenuItem(nameF);
        nameF.setOnAction((ActionEvent t) -> {
            nameF.requestFocus();
        });
        name.setDisable(true);
        conMenu.setOnShowing((WindowEvent t) -> {
            Util.err("Showing");
            nameF.setText(((Playlist) playlists.getSelectionModel().getSelectedItem()).getName());
        });
        conMenu.setOnHidden((WindowEvent e) -> {
//            ObservableList<Playlist> temp = playlistData;
////            playlistData.clear();
//            playlistData.addAll(temp);
//            playlistData.remove(playlistBeingEdited);
//            playlistData.add(playlistBeingEdited);
        });
        conMenu.getItems().add(name);
        playlists.setContextMenu(conMenu);
        playlists.setOnKeyTyped((KeyEvent t) -> {
            if (t.getCode().equals(KeyCode.SPACE)) {
                Util.err("Space detected");//@TODO
                actionPause();
            }
        });
        playlists.setOnMouseClicked((MouseEvent t) -> {
            if (t.getClickCount() > 1 && musicTable.getItems().size() > 0) {
                if (musicTable.getItems().size() == 0) {
                    musicTable.setDisable(true);
                    play((Song) musicTable.getItems().get(Util.randInt(musicTable.getItems().size(), 0)));
                }
            }
        });
        playlists.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Playlist>() {
            @Override
            public void changed(ObservableValue<? extends Playlist> ov, Playlist old_p, Playlist new_p) {
                if (playlistData.size() > 0) {
                    musicTable.setItems(new_p.getSongs());
                    items.setText("" + new_p.getSongs().size());
                }
            }
        });

        playlists.setCellFactory(new Callback<ListView<Playlist>, ListCell<Playlist>>() {
            @Override
            public ListCell<Playlist> call(ListView<Playlist> l) {
                final ListCell<Playlist> pl = new ListCell<Playlist>() {
                    @Override
                    protected void updateItem(Playlist item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                        } else {
                            setText(item.getName());
//                            musicTable.setItems(null);
                        }
                    }
                };
                playlists.setEditable(true);

                pl.setOnDragOver((DragEvent t) -> {
                    if (t.getDragboard().getString() != null) {
                        if (t.getDragboard().getString().equalsIgnoreCase("songs")) {
                            t.acceptTransferModes(TransferMode.ANY);
                        }
                    }
                });
                pl.setOnDragDropped((DragEvent t) -> {
                    if (pl.getItem() != null) {
                        pl.getItem().addSong((Song) musicTable.getSelectionModel().getSelectedItem());
                    }
                });
                return pl;
            }
        });
        playlists.getSelectionModel().select(0);
        Util.initiateTimerTask();
    }

    /**
     * Should only be called by a music table listener
     *
     * @param song
     */
    public void play(Song song) {
        if (trackChange.hasPrevious()) {
            trackChange.deleteAllPast(trackChange.getPrevious());
            trackChange.incrementPointer();
        }
        musicController.play(song);
        trackChange.forceAddSong(song);
        trackChange.adjustPointer(song);
    }

    public void set(Song song) {
        Main.setTitle(Util.APP_TITLE + " :: " + song.toString());
        volSlider.setValue(musicController.getVolume());
        seekSlider.setMax(song.getSeconds());
//        seekSlider.setValue(0);
        titleField.setText(song.toString() + " - " + song.getArtist());
//        artistField.setText(song.getArtist());
        endTime.setText(song.getDuration());
        genreArea.setText(song.getGenre());
        pauseToggleButton.setGraphic(iPause);
        nextButton.setDisable(false);
        previousButton.setDisable(false);
        musicTable.setDisable(false);
        setMoreInfoBox(moreInfo.getSelectionModel().getSelectedIndex());
        try {
            AudioFormat f = musicController.getFormat();
            encoding = (f.getEncoding().toString());
            if (f.getChannels() > 0) {
                channels = ("Stereo");
            } else {
                channels = ("Mono");
            }
        } catch (UnsupportedAudioFileException | IOException e) {
            Util.log(e.getLocalizedMessage());
        }
    }

    private void selectPreset(int idx) {
        switch (idx) {
            case 0://general
                presets.setPromptText("General");
                eq32.setValue(-1);
                eq64.setValue(2);
                eq125.setValue(5);
                eq250.setValue(3);
                eq500.setValue(2);
                eq1k.setValue(1);
                eq2k.setValue(3);
                eq4k.setValue(5);
                eq8k.setValue(7);
                eq16k.setValue(4);
                break;
            case 1://R&B/Dance
                presets.setPromptText("R&B/Dance");
                eq32.setValue(9);
                eq64.setValue(6);
                eq125.setValue(3);
                eq250.setValue(3);
                eq500.setValue(2);
                eq1k.setValue(0);
                eq2k.setValue(-1);
                eq4k.setValue(0);
                eq8k.setValue(1);
                eq16k.setValue(3);
                break;
            case 4: //rap
                presets.setPromptText("Rap");
                break;
            case 5: //classical
                presets.setPromptText("Classical");
                break;
        }
    }

    private void setMoreInfoBox(int idx) {
        if (musicController.getCurrent() != null) {
            switch (idx) {
                case 0:
                    comboField.setText(musicController.getCurrent().getDuration());
                    break;
                case 1:
                    comboField.setText(channels);
                    break;
                case 2:
                    comboField.setText(encoding);
                    break;
            }
        }
    }

    public TableView getMusicTable() {
        return musicTable;
    }

    private void trackChange(boolean next) {
        if (musicController.getCurrent() == null) {
            play((Song) musicTable.getItems().get(Util.randInt(0, musicTable.getItems().size())));
        } else {
            Song toPlay;
            if (shuffle) {
                if (next) {
                    if (trackChange.hasNext()) {
                        toPlay = trackChange.getNext();
                        if (Util.filterArtistContains(filters, toPlay.getArtist())) {
                            trackChange(true);
                        }
                    } else {
                        int i = Util.randInt(0, musicTable.getItems().size());
                        toPlay = (Song) musicTable.getItems().get(i);
                    }
                } else { //previous
                    if (trackChange.hasPrevious()) {
                        toPlay = trackChange.getPrevious();
                        if (Util.filterArtistContains(filters, toPlay.getArtist())) {
                            trackChange(false);
                        }
                    } else {
                        int i = Util.randInt(0, musicTable.getItems().size());
                        while (trackChange.getsongs().contains((Song) musicTable.getItems().get(i))) {
                            i = Util.randInt(0, musicTable.getItems().size());
                        }
                        toPlay = (Song) musicTable.getItems().get(i);
                    }
                }
            } else { //not shuffle
                if (next) {
                    if (trackChange.hasNext()) {
                        toPlay = trackChange.getNext();
                        if (Util.filterArtistContains(filters, toPlay.getArtist())) {
                            trackChange(true);
                        }
                    } else {
                        int i = musicTable.getItems().indexOf(musicController.getCurrent()) + 1;
                        while (Util.filterArtistContains(filters, ((Song) musicTable.getItems().get(i)).getArtist())) {
                            i++;
                        }
                        toPlay = (Song) musicTable.getItems().get(i);
                    }
                } else { //previous
                    if (trackChange.hasPrevious()) {
                        toPlay = trackChange.getPrevious();
                        if (Util.filterArtistContains(filters, toPlay.getArtist())) {
                            trackChange(false);
                        }
                    } else {
                        int i = musicTable.getItems().indexOf(musicController.getCurrent()) - 1;
                        while (Util.filterArtistContains(filters, ((Song) musicTable.getItems().get(i)).getArtist())) {
                            i++;
                        }
                        toPlay = (Song) musicTable.getItems().get(i);
                    }
                }
            }
            musicController.play(toPlay);
            trackChange.addSong(toPlay);
            trackChange.adjustPointer(toPlay);
        }
    }

    @FXML
    private void actionPrev(ActionEvent event) {
        if (musicController.getPosition() > 2) {
            musicController.restart();
        } else {
//            previousButton.setDisable(true);
            trackChange(false);
        }
    }

    public void actionPause() {
        Toast.showToast("HI THERE", 5, Main.getStage());
        if (musicController.getCurrent() != null) {
            if (!musicController.playing) {
                musicController.resume();
                pauseToggleButton.setGraphic(iPause);
            } else {
                musicController.pause();
                pauseToggleButton.setGraphic(iPlay);
            }
        } else {
            if (musicTable.getItems().size() > 0) {
                play((Song) musicTable.getItems().get(Util.randInt(0, musicTable.getItems().size())));
            }
        }
    }

    @FXML
    public void actionPause(ActionEvent event) {
        actionPause();
    }

    @FXML
    private void actionNext(ActionEvent event) {
//        nextButton.setDisable(true);
        trackChange(true);
    }

    @FXML
    private void autoAnalyse(ActionEvent v) throws InterruptedException {
        if (musicController.getCurrent() == null) {
            new ModalDialog("Error", "Please choose a file (double click)", null, 200, 70);
        } else {
            Main.aC.give(musicController.getCurrent()); //give the controller the song

            EventHandler<WindowEvent> ev = (WindowEvent t) -> {
                Main.aC.reset();
            };
            analyserModal = new ModalDialog(Main.analyser, ev);
            Main.aC.analyse(true, true);
        }
    }

    @Override
    public void songImported(Song s) {
        if (s != null && s.getFilepath() != null) {
//            di.setMessage("Add: " + s.getFilepath());
            Platform.runLater(() -> {
                if ((playlists.getSelectionModel().getSelectedItem()) != null) {
                    ((Playlist) playlists.getSelectionModel().getSelectedItem()).addSong(s);
                } else {
                    library.addSong(s);
                }
            });
        }
    }

    @Override
    public void importFinished() {
        Platform.runLater(() -> {
            FadeTransition ft = new FadeTransition(Duration.millis(2000), veil);
            ft.setFromValue(1.0);
            ft.setToValue(0);
            ft.setOnFinished((ActionEvent t) -> {
                veil.setVisible(false);
            });
            FadeTransition ft2 = new FadeTransition(Duration.millis(2500), veil);
            ft2.setFromValue(1.0);
            ft2.setToValue(0);
            ft2.setOnFinished((ActionEvent t) -> {
                multiuseProgressIndicator.setVisible(false);
            });
            ParallelTransition pt = new ParallelTransition(ft, ft2);
            pt.play();
            musicTable.setDisable(false);
            updateItemCount();
            for (TableColumn col : (ObservableList<TableColumn>) musicTable.getColumns()) {
                col.setVisible(false);
                col.setVisible(true);
            }
            Main.sC.populateFilterDropdowns(musicTable);
            Main.aC.populateSongsDropdown(musicTable.getItems());
        });
        AnalysisController.active = true;
    }

    public void updateItemCount() {
        items.setText("" + musicTable.getItems().size());
    }

    public void updateSeeker(double value) {
        seeker.setProgress(value);
//        if (setSeeker) {
        seekSlider.setValue(musicController.getPosition());
//        }
        //t1.setText(currentSong.getPositionAsString());
    }

    @FXML
    private void actionClose(ActionEvent ev) {
        Main.close();
    }

    @FXML
    private void actionAutoload(ActionEvent ev) {
        autoload = !autoload;
    }

    @FXML
    private void actionAbout(ActionEvent ev) {
        List<Button> b = new ArrayList<>();
        Button bt = new Button("OK");
        bt.setOnAction(Util.cancelEvent);
        b.add(bt);

        new ModalDialog("About", "Music Player \n'Cobble Player' \n(C) 2014 Jacob Moss", b, 150, 90);
    }

    @FXML
    private void actionReportBug(ActionEvent ev) {
        String url = "http://cobbles.biz.ht/downloads.php?id=cobblePlayer#one";
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(URI.create(url));
            } catch (IOException e) {
                Util.log(e.getLocalizedMessage());
            }
        }
    }

    @FXML
    private void actionJumpToCurrent(ActionEvent e) {
        if (musicController.getCurrent() != null) {
            int idx = musicTable.getItems().indexOf(musicController.getCurrent());
            musicTable.getSelectionModel().clearAndSelect(idx);
            musicTable.scrollTo(idx);
        }
    }

    @FXML
    private void actionResetEqualizer(ActionEvent e) {
        for (Slider s : sliders) {
            s.setValue(0);
        }
    }

    @FXML
    private void actionResetRate(ActionEvent e) {
        rateSlider.setValue(1);
        trackChange.getsongs().stream().forEach((s)
                -> Util.err(s.toString()));
        Util.err("Pointer: " + trackChange.getPointer());
    }

    @FXML
    private void actionResetBalance(ActionEvent e) {
        balanceSlider.setValue(0);
    }

    @FXML
    private void actionQuickfix(ActionEvent e) {
        musicController.stop();
        musicTable.setDisable(false);
        nextButton.setDisable(false);
        previousButton.setDisable(false);
    }

    @FXML
    private void actionNewPlaylist(ActionEvent ev) {
        playlistData.add(new Playlist("New playlist", null));
    }

    boolean edit2 = false;

    @Deprecated
    private void actionDeletePlaylist(ActionEvent e) {
        final Playlist delete = (Playlist) playlists.getSelectionModel().getSelectedItem();
        if (delete != null) {
            Button yes = new Button("Confirm");
            yes.setOnAction((ActionEvent t) -> {
                playlistData.remove(delete);
                ModalDialog.exit();
            });
            Button cancel = new Button("Cancel");
            cancel.setOnAction(Util.cancelEvent);
            List<Button> buttons = new ArrayList<>();
            buttons.add(yes);
            buttons.add(cancel);
            new ModalDialog("Confirm", "Are you sure you want to delete \n"
                    + "the playlist '" + delete.getName() + "'?",
                    buttons, 200, 70);
        }

    }

    @FXML
    private void actionShuffleToggle() {
        shuffle = !shuffle;
        shuffleButton.setDefaultButton(shuffle);
    }

    @FXML
    private void actionRepeatToggle(ActionEvent ev) {
        repeat = !repeat;
        repeatButton.setDefaultButton(repeat);
    }

    @FXML
    private void actionSettings(ActionEvent ig) {
        Properties prop = Util.openProp(Util.CONFIG_FILENAME);
        String au = prop.getProperty("autoload");
        String sh = prop.getProperty("showQuitMsg");
        Main.sC.SAutoload.setSelected(au.equals("true"));
        Main.sC.SShow.setSelected(sh.equals("true"));
        Main.sC.samplesField.setText(samples + "");
        EventHandler<WindowEvent> ev = (WindowEvent t) -> {
            Main.saveConfig();
        };
        settingsModal = new ModalDialog(Main.settings, ev);
        settingsModal.setTitle("Settings");
    }

    @FXML
    private void actionAnalyser(ActionEvent ig) {
//        Main.aC.analyse.setDisable(false);
        Main.aC.populateSongsDropdown(musicTable.getItems());
        EventHandler<WindowEvent> ev = (WindowEvent t) -> {
            Main.aC.reset();
        };
        analyserModal = new ModalDialog(Main.analyser, ev);
        analyserModal.setTitle("Analyser");
    }

    @FXML
    private void sliderMouseEnter(MouseEvent ev) {
        setSeeker = false;
//        musicController.seekTask.pause();
//        seekSlider.valueProperty().unbind();
//        seek.valueProperty().unbind();
    }

    @FXML
    private void sliderMouseExit(MouseEvent ev) {
        musicController.seek(seekSlider.getValue());
        setSeeker = true;
    }

    public void resetMainSplitPane() {
        mainSplitPane.setDividerPosition(0, 0.22);
    }

    public void songEnded() {
        util.write("Song ended");
        if (repeat) {
            musicController.restart();
        } else {
            trackChange(true);
        }
    }

    @Deprecated
    public void updateTimes(final String cur) {
        Platform.runLater(() -> {
            startTime.setText(cur);
        });
    }

    public List<Playlist> getPlaylists() {
        return playlistData;
    }

    public void setPlaylists(List<Playlist> playlists) {
        importList = playlists;
    }
}
