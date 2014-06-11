package cobbleplayer;

import cobbleplayer.utilities.ModalDialog;
import cobbleplayer.utilities.Toast;
import cobbleplayer.utilities.Util;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

/**
 *
 * @author Jacob Moss
 */
public class SettingsController implements Initializable {

    public static int FILTER_TAB = 2;
    private List<Song> songsAdded = new ArrayList<>();
    @FXML
    CheckBox SShow, SAutoload, enableRemoveButton;
    @FXML
    TableView filterArtistTable;
    @FXML
    ComboBox filterArtistDropdown; //<String>
    @FXML
    ComboBox filterArtistPeriodDropdown;
    @FXML
    TextField samplesField;
    @FXML
    Button filterArtistAdd, removeAll;
    @FXML
    TabPane settingsTabPane;

    public void populateFilterDropdowns(TableView musicTable) {
        songsAdded.clear();
        for (Song s : (ObservableList<Song>) musicTable.getItems()) {
            if (filterArtistDropdown == null) {
                Util.err("drop down null");
            }
            if (s == null) {
                Util.err("song null");
            }
            if (!filterArtistDropdown.getItems().contains(s.getArtist())) {
                filterArtistDropdown.getItems().add(s.getArtist());
                songsAdded.add(s);
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        filterArtistTable.setItems(GUIController.filters);
        ((TableColumn) filterArtistTable.getColumns().get(0)).setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ArtistFilterItem, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<ArtistFilterItem, String> p) {
                return new ReadOnlyObjectWrapper(p.getValue().artist);
            }
        });
        ((TableColumn) filterArtistTable.getColumns().get(1)).setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ArtistFilterItem, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<ArtistFilterItem, String> p) {
                return new ReadOnlyObjectWrapper(p.getValue().period);
            }
        });

        ContextMenu conMenu = new ContextMenu();
        MenuItem delete = new MenuItem("Remove");
        delete.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                final ArtistFilterItem toDelete = ((ArtistFilterItem) filterArtistTable.getSelectionModel().getSelectedItem());
                if (toDelete != null) {
                    Button yes = new Button("Yes");
                    yes.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent t) {
                            ModalDialog.exit();
                            filterArtistTable.getItems().remove(toDelete);
                        }
                    });
                    Button no = Util.buttonNo;
                    List<Button> buttons = new ArrayList<>();
                    buttons.add(yes);
                    buttons.add(no);
                    new ModalDialog("Confirm deletion", "Are you sure you want to remove this artist filter '"
                            + toDelete.artist + "'?", buttons, 350, 70);
                }
            }
        });
        conMenu.getItems().add(delete);
        filterArtistTable.setContextMenu(conMenu);
        filterArtistDropdown.getItems().clear();
        ObservableList<String> periods = FXCollections.observableArrayList("Indefinite", "10 days", "1 month", "2 months", "4 months", "6 months");
//        filterArtistDropdown.setCellFactory(new Callback<ListView<Song>, ListCell<Song>>() {
//            @Override
//            public ListCell<Song> call(ListView<Song> p) {
//                final ListCell<Song> sc = new ListCell<Song>() {
//                    @Override
//                    protected void updateItem(Song item, boolean empty) {
////                        if (!itemsAdded.contains(item)) {
//                        super.updateItem(item, empty);
//                        if (item == null || empty) {
//                        } else {
//                            Util.err(item.getArtist());
//                            setText(item.getArtist());
//                            itemsAdded.add(item);
//                        }
////                        }
//                    }
//                };
//                return sc;
//            }
//        });
        filterArtistPeriodDropdown.setItems(periods);

        filterArtistAdd.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                String chosenArtist = (String) filterArtistDropdown.getSelectionModel().getSelectedItem();
                int index = filterArtistDropdown.getSelectionModel().getSelectedIndex();
                Object chosenTime = filterArtistPeriodDropdown.getSelectionModel().getSelectedItem();
                if (chosenArtist == null || chosenTime == null) {
                    String error = (chosenArtist == null) ? "Please choose an artist!" : "Please choose a period!";
                    Toast.showPopupMessage(error, Main.getStage());
                } else {
                    Calendar cal = Calendar.getInstance();
                    long periodChosen = 0;
                    boolean indefinite = false;
                    long dayMillis = 86400000;
                    switch (chosenTime.toString()) {
                        case "Indefinite":
                            indefinite = true;
                            break;
                        case "10 days":
//                            periodChosen = 10 * dayMillis;
                            periodChosen = 60000;
                            break;
                        case "1 month":
                            periodChosen = 30 * dayMillis;
                            break;
                        case "2 months":
                            periodChosen = 60 * dayMillis;
                            break;
                        case "4 months":
                            periodChosen = 120 * dayMillis;
                            break;
                        case "6 months":
                            periodChosen = 180 * dayMillis;
                            break;
                    }
                    if (!indefinite) {
                        long periodInMillis = cal.getTimeInMillis() + periodChosen;
                        try {
                            BufferedWriter filtersWriter = Util.writeNewFile(Util.FILTERS_FILENAME);
                            filtersWriter.write(Util.NEW_BIT_CODE + chosenArtist + "PERIOD:" + periodInMillis);
                            filtersWriter.newLine();
                            filtersWriter.close();
                            ArtistFilterItem filter = new ArtistFilterItem(songsAdded.get(index), periodInMillis, chosenTime.toString());
//                            filterArtistTable.getItems().add(filter);
                            GUIController.filters.add(filter);
                        } catch (IOException ex) {
                            Util.err(ex.getLocalizedMessage());
                        }
                    } else {
                        try {
                            BufferedWriter filtersWriter = Util.writeNewFile(Util.FILTERS_FILENAME);
                            filtersWriter.write(Util.NEW_BIT_CODE + chosenArtist);
                            filtersWriter.newLine();
                            filtersWriter.close();
                            GUIController.filters.add(new ArtistFilterItem(songsAdded.get(index), -1, "Indefinite"));
                        } catch (IOException ex) {
                            Util.err(ex.getLocalizedMessage());
                        }
                    }
                }
            }
        });
        SShow.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                GUIController.show = SShow.isSelected();
            }
        });
        SAutoload.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                GUIController.autoload = SAutoload.isSelected();
            }
        });
        samplesField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                if (!t1.isEmpty()) {
                    try {
                        int samples = Integer.parseInt(t1);
                        if (samples > 0) {
                            GUIController.samples = samples;
                        }
                    } catch (NumberFormatException ignore) {
                    }
                }
            }
        });
    }

    public void changeTab(int tab) {
        settingsTabPane.getSelectionModel().clearAndSelect(tab);
    }

    @FXML
    private void enableRemoveButton(ActionEvent e) {
        removeAll.setDisable(!removeAll.isDisabled());

    }

    public static class ArtistFilterItem {

        public String filepath, artist;
        public long millis;
        public String period;

        public ArtistFilterItem(Song s, long millis, String period) {
            this.artist = s.getArtist();
            this.filepath = s.getFilepath();
            this.millis = millis;
            this.period = period;
        }
    }
}
