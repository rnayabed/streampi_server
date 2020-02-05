package updaterPKG;

import StreamPiServer.Main;
import StreamPiServer.dashboardController;
import StreamPiServer.io;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.util.Duration;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;


public class updaterController implements Initializable {

    @FXML
    private Label headingLabel;

    @FXML
    private Label subHeadingLabel;

    @FXML
    private Label changelogLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if(Main.config.get("update_type").equals("server"))
        {
            headingLabel.setText("Server Update Available!");
            subHeadingLabel.setText("Version : "+Main.config.get("server_update_version"));
            changelogLabel.setText(Main.config.get("server_update_changelog"));
        }
        else if(Main.config.get("update_type").equals("client"))
        {
            headingLabel.setText("Client Update Available!");
            subHeadingLabel.setText("Version : "+Main.config.get("client_update_version"));
            changelogLabel.setText(Main.config.get("client_update_changelog"));
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

    @FXML
    public void updateButtonClicked()
    {
        new Thread(new Task<Void>() {
            @Override
            protected Void call() {
                try
                {
                    io.writeToFile(Main.config.get("server_update_download_url"),"updater/update_header");

                    Platform.runLater(()->Main.dc.showProgress("Downloading Update ..."));

                    if(Main.config.get("server_update_download_url").equals("unavailable"))
                    {
                        Platform.runLater(()->{
                            Main.dc.hideProgress();
                            Main.dc.showErrorAlert("Sorry!","The new update is not available for your system.\nContact devs for more info");
                        });
                    }
                    else
                    {
                        FileUtils.copyURLToFile(new URL(Main.config.get("server_update_download_url")),new File("update.zip"));
                        System.out.println("Downloaded!");

                        if(Main.config.get("update_type").equals("server"))
                        {
                            Process p = Runtime.getRuntime().exec("python3 updater.py");
                        }
                        else if(Main.config.get("update_type").equals("client"))
                        {
                            Main.dc.showErrorAlert("Later","Will be shown later!");
                        }
                    }
                }
                catch (Exception e)
                {
                    Platform.runLater(()->{
                        Main.dc.showErrorAlert("Uh Oh!","Unable to download! Check connection and try again");
                    });
                    e.printStackTrace();
                }
                finally {
                    Platform.runLater(()-> Main.dc.hideProgress());
                }
                return null;
            }
        }).start();
    }

}
