package cobbleplayer.util;

import java.awt.Transparency;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.StackPaneBuilder;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

/**
 *
 * @author Jacob
 */
public class Toast {

    private static Popup createPopup(final String message) {
        final Popup popup = new Popup();
        popup.setAutoFix(true);
        popup.setAutoHide(true);
        popup.setHideOnEscape(true);
        Label label = new Label(message);
        label.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                popup.hide();
            }
        });
        label.getStylesheets().add("/resources/cobble.css");
        label.getStyleClass().add("popup");
        popup.getContent().add(label);
        return popup;
    }

    /**
     * Displays a popup
     *
     * @param message the message to display on the popup
     * @param seconds the time before the popup fades out, in seconds
     */
    public static void showToast(String message, int seconds, Stage stage) {
        final Stage dialog = new Stage();
        dialog.initModality(Modality.NONE);
        dialog.initStyle(StageStyle.TRANSPARENT);
        dialog.initOwner(stage);
        HBox dialogVbox = new HBox(0);
        dialogVbox.setAlignment(Pos.CENTER);
        dialogVbox.getChildren().add(new Text(message));
        Scene dialogScene = new Scene(dialogVbox, message.length() + 50, 20);
        dialogScene.setFill(null);
        dialog.setScene(dialogScene);
        FadeTransition ft = new FadeTransition(Duration.seconds(seconds), dialogVbox);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.setAutoReverse(false);
        ft.setOnFinished((ActionEvent t) -> {
            dialog.hide();
        });
        dialog.show();
        ft.play();
    }
}
