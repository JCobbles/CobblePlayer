package cobbleplayer.utilities;

import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPaneBuilder;
import javafx.scene.shape.Rectangle;
import javafx.stage.Popup;
import javafx.stage.Stage;
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
        label.setOnMouseReleased(new EventHandler<MouseEvent>() {
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

    public static void showPopupMessage(final String message, final Stage stage) {
        final Popup popup = createPopup(message);
        popup.setOnShown(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent e) {
                popup.setX(stage.getX() + stage.getWidth() / 2 - popup.getWidth() / 2);
                popup.setY(stage.getY() + stage.getHeight() / 2 - popup.getHeight() / 2);
            }
        });
        Rectangle rect = new Rectangle(popup.getWidth(), popup.getHeight());
        popup.getContent().add(rect);
        popup.show(stage);
        FadeTransition ft = new FadeTransition(Duration.seconds(4), rect);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.setCycleCount(1);
        ft.setAutoReverse(false);
        ft.play();
        ft.setOnFinished(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                popup.hide();
                Util.err("hiding");
            }
        });

    }
}
