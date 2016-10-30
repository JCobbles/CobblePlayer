package cobbleplayer.utilities;

import cobbleplayer.Main;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;

/**
 *
 * @author Jacob
 */
public class UpdateController implements Initializable {

    @FXML
    TextArea changelog;
    String log;
    Main main;

    public UpdateController(String log, Main main) {
        this.log = log;
        this.main = main;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        changelog.setText(log);
    }

    @FXML
    public void actionUpdate(ActionEvent evt) {
        main.update(true);
    }

    @FXML
    public void actionCancel(ActionEvent evt) {
        main.update(false);
        Main.dialog.close();

    }

}
