package cobbleplayer;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

/**
 *
 * @author Jacob Moss
 */
public class SettingsController implements Initializable {

    @FXML
    CheckBox SShow, SAutoload;
    @FXML
    TextField samplesField;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
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
}
