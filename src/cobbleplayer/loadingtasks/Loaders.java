package cobbleplayer.loadingtasks;

import cobbleplayer.AnalysisController;
import cobbleplayer.GUIController;
import cobbleplayer.Song;
import cobbleplayer.util.FileImporter;
import cobbleplayer.util.Util;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;

/**
 *
 * @author jacob
 */
public class Loaders {

    public static class TableCreator extends Task<TableView> {
        private final GUIController master;
        public TableCreator(GUIController master) {
            this.master = master;
        }

        @Override
        protected TableView call() throws Exception {
            TableView musicTable = new TableView();
            musicTable.getColumns().addAll(Util.initColumns());
        musicTable.setItems(master.playlistData.get(0).getSongs());
        musicTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        musicTable.setOnMouseClicked((MouseEvent t) -> {
            if (t.getButton().equals(MouseButton.PRIMARY) && t.getClickCount() > 1 && musicTable.getItems().size() > 0 && musicTable.getSelectionModel().getSelectedItem() != null) {
                musicTable.setDisable(true);
                master.play((Song) musicTable.getSelectionModel().getSelectedItem());
            }
        });
        musicTable.setOnDragDetected((MouseEvent t) -> {
            Dragboard db = musicTable.startDragAndDrop(TransferMode.ANY);
//                util.write(t.getSource().toString());
            ClipboardContent content = new ClipboardContent();
            if (musicTable.getSelectionModel().getSelectedItem() != null) {
                content.putString("songs");
                db.setContent(content);
            }
            t.consume();
        });
        musicTable.setOnDragOver((DragEvent event) -> {
            Dragboard db = event.getDragboard();
            if (db.hasFiles()) {
                event.acceptTransferModes(TransferMode.LINK);
            } else {
                event.consume();
            }
        });
        musicTable.setOnKeyPressed((KeyEvent t) -> {
            if (t.getCode().equals(KeyCode.DELETE) && musicTable.getSelectionModel().getSelectedItem() != null) {
                Util.err("Removing " + musicTable.getSelectionModel().getSelectedItems().size() + "songs");
                int from = (int) musicTable.getSelectionModel().getSelectedIndices().get(0);
                int to = from + ((int) musicTable.getSelectionModel().getSelectedIndices().size());
                Util.err("From: " + from + " to: " + to);
                musicTable.getItems().remove(from, to);
                musicTable.getSelectionModel().clearSelection();
                master.updateItemCount();
            } else if (t.getCode().equals(KeyCode.SPACE)) {
                master.actionPause();
            }
        });

        musicTable.setOnDragDropped((DragEvent event) -> {
            final Dragboard db = event.getDragboard();
            if (db.hasFiles()) {
                master.getMultiuseProgressIndicator().setVisible(true);
                master.getVeil().setVisible(true);
                musicTable.setDisable(true);
                AnalysisController.active = false;
                FileImporter importer = new FileImporter(master.activeController, db.getFiles());
                new Thread(importer).start();
                master.getMultiuseProgressIndicator().progressProperty().bind(importer.progressProperty);
            }
            event.setDropCompleted(true);
            event.consume();
        });
        ContextMenu conMenu = new ContextMenu();
        MenuItem queue = new MenuItem("Queue song");
        queue.setOnAction((ActionEvent e) -> {
            if (musicTable.getSelectionModel().getSelectedItem() != null) {
                master.getTrackChange().addSong((Song) musicTable.getSelectionModel().getSelectedItem());
            }
        });
        conMenu.getItems().add(queue);
        musicTable.setContextMenu(conMenu);
            return null;
        }
    }

}
