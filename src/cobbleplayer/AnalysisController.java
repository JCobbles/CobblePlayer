/**
 * Copyright (C) 2016 Jacob Moss
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

import static cobbleplayer.GUIController.samples;
import cobbleplayer.ca.AmplitudeCollector;
import cobbleplayer.ca.CollectionListener;
import cobbleplayer.ca.FrequencyCollector;
import cobbleplayer.utilities.Toast;
import cobbleplayer.utilities.Util;
import ddf.minim.AudioPlayer;
import ddf.minim.Minim;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 *
 * @author Jacob
 */
public class AnalysisController implements Initializable, CollectionListener {

    @FXML
    public static TextField position, songBeingAnalysed; //20-1000, 1000-3000, 3000-10000, -30000, -60000, -100000
    @FXML
    public TableView freqTable;
    @FXML
    ComboBox<Song> songChooser;
    @FXML
    Button analyse;
    @FXML
    TabPane tabPane;
    @FXML
    CheckBox checkFreq, checkRhythm;
    @FXML
    LineChart<Integer, Integer> ampChart;
    public static boolean active = true;
    private Song cobbleSong;
    public XYChart.Series series = new XYChart.Series();
    Minim minim;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        series.setName("Amplitude data");
        ((TableColumn) freqTable.getColumns().get(0)).setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Item, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Item, String> p) {
                return new ReadOnlyObjectWrapper(p.getValue().sample);
            }
        });
        ((TableColumn) freqTable.getColumns().get(1)).setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Item, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Item, String> p) {
                return new ReadOnlyObjectWrapper(p.getValue().one);
            }
        });
        ((TableColumn) freqTable.getColumns().get(2)).setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Item, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Item, String> p) {
                return new ReadOnlyObjectWrapper(p.getValue().two);
            }
        });
        ((TableColumn) freqTable.getColumns().get(3)).setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Item, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Item, String> p) {
                return new ReadOnlyObjectWrapper(p.getValue().three);
            }
        });
        ((TableColumn) freqTable.getColumns().get(4)).setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Item, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Item, String> p) {
                return new ReadOnlyObjectWrapper(p.getValue().four);
            }
        });
        ((TableColumn) freqTable.getColumns().get(5)).setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Item, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Item, String> p) {
                return new ReadOnlyObjectWrapper(p.getValue().five);
            }
        });
        ((TableColumn) freqTable.getColumns().get(6)).setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Item, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Item, String> p) {
                return new ReadOnlyObjectWrapper(p.getValue().six);
            }
        });
//        TableColumn<Item, Boolean> actionCol = new TableColumn<>("Action");
//        actionCol.setSortable(false);
//        actionCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Item, Boolean>, ObservableValue<Boolean>>() {
//            @Override
//            public ObservableValue<Boolean> call(TableColumn.CellDataFeatures<Item, Boolean> features) {
//                return new SimpleBooleanProperty(features.getValue() != null);
//            }
//        });
//        actionCol.setCellFactory(new Callback<TableColumn<Item, Boolean>, TableCell<Item, Boolean>>() {
//            @Override
//            public TableCell<Item, Boolean> call(TableColumn<Item, Boolean> personBooleanTableColumn) {
//                return new AddPersonCell(Main.getStage(), freqTable);
//            }
//        });
//        freqTable.getColumns().add(actionCol);

        if (songChooser == null) {
            Util.err("SONGCHOOSER IS NULL");
        }
        songChooser.setCellFactory(new Callback<ListView<Song>, ListCell<Song>>() {
            @Override
            public ListCell<Song> call(ListView<Song> p) {
                final ListCell<Song> sc = new ListCell<Song>() {
                    @Override
                    protected void updateItem(Song item, boolean empty) {
                        super.updateItem(item, empty);
                        if (active) {
                            if (item == null || empty) {
                            } else {
                                setText(item.toString() + ", " + item.getArtist());
                            }
                        }

                    }
                };
                return sc;
            }
        });
        songChooser.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                cobbleSong = (Song) songChooser.getSelectionModel().getSelectedItem();
            }
        });
        for (Object c : freqTable.getColumns()) {
            ((TableColumn) c).setSortable(false);
        }
        analyse.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                analyse(checkFreq.isSelected(), checkRhythm.isSelected());
            }
        });
    }

    private void amplitudeAnalysis() {

        AmplitudeCollector col = new AmplitudeCollector(new File(cobbleSong.getFilepath()));
        col.setListener(this);
        Thread colT = new Thread(col);
        colT.setName("Amplitude-collector");
        colT.start();
    }

    public void analyse(boolean freq, boolean amp) {
        if (cobbleSong != null) {
            Util.err(cobbleSong.toString());
            GUIController.analyserModal.setTitle("Analyser :: " + cobbleSong.toString());
//            reset();
            songBeingAnalysed.setText(cobbleSong.toString());
            minim = new Minim(this);
            final AudioPlayer song = minim.loadFile("song", 512);
            Util.err(song.bufferSize());
            if (freq) {
                frequencyAnalysis(song);
            }
            if (amp) {
                amplitudeAnalysis();
            }

        }

    }

    private void frequencyAnalysis(AudioPlayer minim) {
//        FrequencyCollector col = new FrequencyCollector(new File(cobbleSong.getFilepath()), minim);
        FrequencyCollector col = new FrequencyCollector(minim);
        col.setListener(this);
        Thread colT = new Thread(col);
        colT.setName("Frequency-collector");
        colT.start();
    }

    public void give(Song song) {
        analyse.setDisable(true);
        this.cobbleSong = song;
    }

    public void reset() {
        if (ampChart.getData().size() > 0) {
            ampChart.getData().remove(0);
        }
        if (freqTable.getItems().size() > 0) {
            freqTable.getItems().remove(0, freqTable.getItems().size());
        }
    }

    @Override
    public void freqCollectionFinished(List<Float> one, List<Float> two, List<Float> three, List<Float> four, List<Float> five, List<Float> six) {
        if (freqTable.getItems().size() > 0) {
            freqTable.getItems().remove(0, freqTable.getItems().size());
        }
        for (int i = 0; i < one.size(); i++) {
            freqTable.getItems().add(new Item(i + 1 + "", one.get(i), two.get(i), three.get(i),
                    four.get(i), five.get(i), six.get(i)));
        }
        freqTable.getItems().add(new Item("Average:", Util.calculateAverage(one), Util.calculateAverage(two), Util.calculateAverage(three),
                Util.calculateAverage(four), Util.calculateAverage(five), Util.calculateAverage(six)));

        freqTable.getSelectionModel().clearAndSelect(samples);

//        freqTable.scrollTo(samples);
//        tabPane.getSelectionModel().clearAndSelect(1);
    }

    @Override
    public void ampCollectionFinished(short[] amplitudes, XYChart.Series s) {
        Util.err("Finished amplitude collection");
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Toast.showPopupMessage("Collection has finished, proceeding with analysis", Main.getStage());
            }
        });

        if (ampChart.getData().size() > 0) {
            ampChart.getData().remove(0);
        }

        series = s;
//        Platform.runLater(new Runnable() {
//            @Override
//            public void run() {
        ampChart.getData().add(series);
        ampChart.getXAxis().setLabel("Time / seconds");
        ampChart.getYAxis().setLabel("Amplitude / arbitrary units");
        ampChart.getYAxis().setStyle("-fx-text-fill: #4682b4;");
//            }
//        });
//        new Thread(new AmplitudeAnalyser(amplitudes)).start();
    }

    public String sketchPath(String filename) { //required by minim
        return "penguin";
    }

    public InputStream createInput(String filename) { //needed by minim
        try {
            return new FileInputStream(new File(cobbleSong.getFilepath()));
        } catch (FileNotFoundException ex) {
            return null;

        }
    }

//    private class AddPersonCell extends TableCell<Item, Boolean> {
//
//        // a button for adding a new person.
//        final Button addButton = new Button("BUTTON_TITLE");
//        // pads and centers the add button in the cell.
//        final StackPane paddedButton = new StackPane();
//        // records the y pos of the last button press so that the add person dialog can be shown next to the cell.
//        final DoubleProperty buttonY = new SimpleDoubleProperty();
//
//        /**
//         * AddPersonCell constructor
//         *
//         * @param table the table to which a new person can be added.
//         */
//        AddPersonCell(final TableView table) {
//            paddedButton.setPadding(new Insets(3));
//            paddedButton.getChildren().add(addButton);
//            addButton.setOnMousePressed(new EventHandler<MouseEvent>() {
//                @Override
//                public void handle(MouseEvent mouseEvent) {
//                    buttonY.set(mouseEvent.getScreenY());
//                }
//            });
//            addButton.setOnAction(new EventHandler<ActionEvent>() {
//                @Override
//                public void handle(ActionEvent actionEvent) {
//                    //PERFORM ACTION HERE
//                    table.getSelectionModel().select(getTableRow().getIndex());
//                }
//            });
//        }
//
//        /**
//         * places an add button in the row only if the row is not empty.
//         */
//        @Override
//        protected void updateItem(Boolean item, boolean empty) {
//            super.updateItem(item, empty);
//            if (!empty) {
//                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
//                setGraphic(paddedButton);
//            }
//        }
//    }

    public class Item {

        public float one, two, three, four, five, six;
        public String sample;

        public Item(String sample, float one, float two, float three, float four, float five, float six) {
            this.sample = sample;
            this.one = one;
            this.two = two;
            this.three = three;
            this.four = four;
            this.five = five;
            this.six = six;
        }
    }
}

//while (din.read(temp, 0, 4) != -1) {
//                            if (decodedFormat.getChannels() == 2) {
//                                leftDos.writeShort(temp[1] * 256 + temp[0]);
//                                rightDos.writeShort(temp[3] * 256 + temp[2]);
//                                i++;
//                            }
//                        }
//                int id = 0;
//
//new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    File file = new File(cobbleSong.getFilepath());
//                    if (file.exists()) {
//
//                        AudioInputStream in = AudioSystem.getAudioInputStream(file);
//                        AudioInputStream din = null;
//                        AudioFormat baseFormat = in.getFormat();
//                        AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
//                                baseFormat.getSampleRate(), //sample rate
//                                16, //sample size (bits)
//                                baseFormat.getChannels(), //channels
//                                baseFormat.getChannels() * 2,//frame size
//                                baseFormat.getSampleRate(), //frame rate
//                                false);                     //bigendian?
//                        din = AudioSystem.getAudioInputStream(decodedFormat, in);
//                        AudioInputStream count = din;
//                        Util.err(din.available());
//                        int i = 0;
//                        Util.err("Starting amplitude analysis...");
//                        while (count.read() != -1) {
//                            i++;
//                        }
//                        i = i / 4;
//                        short[] m = new short[i];
//                        int index = 0;
//                        Random r = new Random();
//                        Util.err("Step 2 skip size:");
//                        int len = 0;
//                        byte[] temp = new byte[4];
//                        int offset = 0, skip = i / (int) Main.analyser.getWidth();
//                        Util.err(skip);
//                        while (1 > 0) {
//                            System.err.println(din.available());
//                            while (offset < skip) {
//                                offset += din.skip(skip - offset);
//                                Util.err(offset + "\nskip:" + skip);
//                            }
//                            len += skip;
//                            offset = 0;
//                            System.err.println("Escaped");
//                            if (din.read(temp, 0, 4) == -1) {
//                                Util.err(len + "\n" + index);
//                                break;
//                            }
//
//                            final int templen = index;
//                            final short s = (short) (((temp[1] * 256 + temp[0]) + (temp[3] * 256 + temp[2])) / 2);
//                            Platform.runLater(new Runnable() {
//                                @Override
//                                public void run() {
//                                    Util.err(s);
//                                    series.getData().add(new XYChart.Data(templen, s));
//                                }
//                            });
//
//                            index++;
//                        }
//
//                        Platform.runLater(new Runnable() {
//                            @Override
//                            public void run() {
//                                ampChart.getData().add(series);
//                                ampChart.getXAxis().setLabel("Time / arbitrary units");
//                                ampChart.getYAxis().setLabel("Amplitude / arbitrary units");
//                                Util.err("Finished");
//                                Button ok = new Button("OK");
//                                ok.setOnAction(new EventHandler<ActionEvent>() {
//                                    @Override
//                                    public void handle(ActionEvent paramT) {
//                                        ModalDialog.exit();
//                                    }
//                                });
//                                List<Button> buttons = new ArrayList<>();
//                                buttons.add(ok);
//                                new ModalDialog("Finished", "Analysis has finished completely.", buttons, 210, 75);
//                            }
//                        });
//
//                    }
//                } catch (IOException | UnsupportedAudioFileException e) {
//                    Util.err("Error." + e.getLocalizedMessage());
//                }
//            }
//        }
//        );
