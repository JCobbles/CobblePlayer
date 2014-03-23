package cobbleplayer;

import static cobbleplayer.GUIController.samples;
import cobbleplayer.ca.AmplitudeAnalyser;
import cobbleplayer.ca.AmplitudeCollector;
import cobbleplayer.ca.CollectionListener;
import cobbleplayer.ca.FrequencyCollector;
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
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
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
    public static LineChart<Integer, Integer> ampChart;
    private Song cobbleSong;
    public static final XYChart.Series series = new XYChart.Series();
    Minim minim;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        tabPane.setOnMousePressed(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent t) {
//                tabPane.getSelectionModel().select(curTab);
            }
        });
        ((TableColumn) freqTable.getColumns().get(0)).setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Item, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Item, String> p) {
                return new ReadOnlyObjectWrapper(p.getValue().one);
            }
        });
        ((TableColumn) freqTable.getColumns().get(1)).setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Item, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Item, String> p) {
                return new ReadOnlyObjectWrapper(p.getValue().two);
            }
        });
        ((TableColumn) freqTable.getColumns().get(2)).setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Item, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Item, String> p) {
                return new ReadOnlyObjectWrapper(p.getValue().three);
            }
        });
        ((TableColumn) freqTable.getColumns().get(3)).setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Item, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Item, String> p) {
                return new ReadOnlyObjectWrapper(p.getValue().four);
            }
        });
        ((TableColumn) freqTable.getColumns().get(4)).setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Item, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Item, String> p) {
                return new ReadOnlyObjectWrapper(p.getValue().five);
            }
        });
        ((TableColumn) freqTable.getColumns().get(5)).setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Item, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Item, String> p) {
                return new ReadOnlyObjectWrapper(p.getValue().six);
            }
        });
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
                        if (item == null || empty) {
                        } else {
                            setText(item.toString() + ", " + item.getArtist());
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
        series.setName("Amplitude data");
        AmplitudeCollector col = new AmplitudeCollector(new File(cobbleSong.getFilepath()));
        col.
        new Thread(col).start();
    }

    public void analyse(boolean freq, boolean amp) {
        Util.err(cobbleSong.toString());
        GUIController.analyserModal.setTitle("Analyser :: " + cobbleSong.toString());
        reset();
        songBeingAnalysed.setText(cobbleSong.toString());
        minim = new Minim(this);
        final AudioPlayer song = minim.loadFile("song", 512);
        if (freq) {
            frequencyAnalysis(song);
        }
        if (amp) {
            amplitudeAnalysis();
        }

    }

    private void frequencyAnalysis(final AudioPlayer song) {
        FrequencyCollector col = new FrequencyCollector(song);
        col.setListener(this);
        new Thread(col).start();
    }

    public void finishFreqAnalysis(List<Float> one, List<Float> two, List<Float> three, List<Float> four, List<Float> five, List<Float> six) {
        for (int i = 0; i < one.size(); i++) {
            freqTable.getItems().add(new Item(one.get(i), two.get(i), three.get(i),
                    four.get(i), five.get(i), six.get(i)));
        }
        freqTable.getItems().add(new Item(Util.calculateAverage(one), Util.calculateAverage(two), Util.calculateAverage(three),
                Util.calculateAverage(four), Util.calculateAverage(five), Util.calculateAverage(six)));
        freqTable.getSelectionModel().clearAndSelect(samples);
        freqTable.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
//                freqTable.getSelectionModel().clearAndSelect(samples);
            }
        });
        freqTable.scrollTo(samples);
        tabPane.getSelectionModel().clearAndSelect(1);
    }

    public void give(Song song) {
        analyse.setDisable(true);
        this.cobbleSong = song;
    }

    public void reset() {

        series.getData().remove(0, series.getData().size());
        ampChart.getData().remove(series);

//        ObservableList<Item> dataaa = FXCollections.observableArrayList();
//        freqTable.setItems(dataaa);
        freqTable.getItems().remove(0, freqTable.getItems().size());

    }

    @Override
    public void freqCollectionFinished(List<Float> one, List<Float> two, List<Float> three, List<Float> four, List<Float> five, List<Float> six) {
        Util.err("YAY");
        finishFreqAnalysis(one, two, three, four, five, six);
    }

    @Override
    public void ampCollectionFinished(short[] amplitudes) {
        new Thread(new AmplitudeAnalyser(amplitudes)).start();
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

    public class Item {

        public float one, two, three, four, five, six;

        public Item(float one, float two, float three, float four, float five, float six) {
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
