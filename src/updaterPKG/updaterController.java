package updaterPKG;

import StreamPiServer.Main;
import StreamPiServer.dashboardController;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;


public class updaterController implements Initializable {

    //fx elements
    @FXML
    private Label versionNum;
    @FXML
    private Label softName;
    @FXML
    private Label updateLabel;
    @FXML
    private Button updateButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if(dashboardController.actionConfigType == 1)
        {

        }
    }

    /*
    Takes a version number and separates it
    depends on every digit being 0-9
     */

    @FXML
    public void cancelButtonClicked()
    {
        Main.dc.newActionConfigDialog.close();
    }

}
