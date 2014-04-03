/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cobbleplayer.utilities;

/**
 *
 * @author Jacob
 */
import cobbleplayer.Main;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class ModalDialog extends Stage {

    Stage owner;
    static Stage stage;
    BorderPane root;
    private String message;
    private List<Button> buttons;
    int width = 150, height = 80;
    Group group;

    public ModalDialog(String title, String message, List<Button> buttons) {
        width = message.length() * 20;
        init(title, message, buttons);
    }

    /**
     * @param s the scene to display
     * @param closeEvent the action to be performed when scene closed
     */
    public ModalDialog(Scene s, EventHandler<WindowEvent> closeEvent) {
        owner = Main.getStage();
        initModality(Modality.APPLICATION_MODAL);
        initOwner(owner);
        initStyle(StageStyle.UTILITY);
        setScene(s);
        stage = this;
        stage.setOnCloseRequest(closeEvent);
        stage.show();
    }

    public ModalDialog(String title, String message, List<Button> buttons, int width, int height) {
        this.width = width;
        this.height = height;
        init(title, message, buttons);
    }

    private void init(String title, String message, List<Button> buttons) {
        root = new BorderPane();
        owner = Main.getStage();
        stage = this;
        initModality(Modality.APPLICATION_MODAL);
        initOwner(owner);
        initStyle(StageStyle.UTILITY);
        setTitle(title);
        this.message = message;
        this.buttons = buttons;
        setContents();
    }

    private void setContents() {
        Scene scene = new Scene(root, width, height);
        setScene(scene);
        group = new Group();
        group.getChildren().add(new Label(message));
        root.setCenter(group);
        if (buttons != null) {
            HBox buttonPane = new HBox();
            buttonPane.setSpacing(10);

            root.setBottom(buttonPane);
            for (Button but : buttons) {
                buttonPane.getChildren().add(but);
            }
        }

        stage.show();
    }

    public void setMessage(final String message) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                ((Label) group.getChildren().get(0)).setText(message);
            }
        });
    }

    public static void exit() {
        stage.close();
    }
}
