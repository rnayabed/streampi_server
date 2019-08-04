import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.TouchEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Base64;
import java.util.Random;
import java.util.ResourceBundle;

public class newHotkeyPopupController implements Initializable {
    @FXML
    private JFXTextField actionCasualNameField;

    @FXML
    private JFXTextField hotkeyCodeField;

    @FXML
    private JFXTextField iconPathField;

    @FXML
    private JFXButton browseButton;

    @FXML
    private ImageView iconPreviewImg;

    @FXML
    private JFXButton addButton;

    @FXML
    private JFXButton cancelButton;

    Image previewImageDefault = new Image(getClass().getResourceAsStream("icons/icon_preview.png"));

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML
    public void cancelButtonClicked()
    {
        Main.dc.newActionConfigDialog.close();
    }

    @FXML
    public void addButtonClicked()
    {
        String actionCasualName = actionCasualNameField.getText();
        String hotkeyCode = hotkeyCodeField.getText();
        String iconPath = iconPathField.getText();

        StringBuilder errors = new StringBuilder("Please correct and resolve the following errors :\n");
        boolean isError = false;

        if(actionCasualName.length() == 0)
        {
            errors.append("Invalid Action Name Entered\n");
            isError = true;
        }

        if(hotkeyCode.length() == 0)
        {
            errors.append("Invalid HotKey Entered\n");
            isError = true;
        }

        if(!isImageFileOK)
        {
            errors.append("Invalid Action Icon\n");
            isError = true;
        }

        if(isError)
        {
            Main.dc.showErrorAlert("Error!",errors.toString());
            return;
        }

        //Good to go!

        new Thread(new Task<Void>() {
            @Override
            protected Void call() {
                try
                {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            Main.dc.showProgress("Updating StreamPi Client","Updating actions... ");
                        }
                    });

                    //send icon to client ...
                    FileInputStream fs = new FileInputStream(selectedIconFile.getAbsolutePath());
                    byte[] imageB = fs.readAllBytes();
                    String base64EncryptedIcon = Base64.getEncoder().encodeToString(imageB);

                    String iconName = selectedIconFile.getName();

                    Main.dc.writeToOS("update_icon::"+iconName+"::"+base64EncryptedIcon+"::");

                    //first update local actions....
                    String[][] oldActions = new String[Main.dc.actions.length+1][7];
                    int i;
                    for(i = 0;i<Main.dc.actions.length;i++)
                    {
                        oldActions[i] = Main.dc.actions[i];
                    }

                    oldActions[i][0] = generateRandomID();
                    oldActions[i][1] = actionCasualName;
                    oldActions[i][2] = "hotkey";
                    oldActions[i][3] = hotkeyCodeField.getText();
                    oldActions[i][4] = selectedIconFile.getName();
                    oldActions[i][5] = Main.dc.selectedRow+"";
                    oldActions[i][6] = Main.dc.selectedCol+"";

                    Main.dc.actions = oldActions;

                    Main.dc.icons.put(oldActions[i][4],previewIcon);


                    ImageView icon = new ImageView();
                    icon.setImage(Main.dc.icons.get(Main.dc.actions[i][4]));
                    icon.setFitHeight(90);
                    icon.setFitWidth(90);

                    Pane actionPane = new Pane(icon);
                    actionPane.setPrefSize(90,90);
                    //actionPane.setStyle("-fx-effect: dropshadow(three-pass-box, "+actions[i][4]+", 5, 0, 0, 0);-fx-background-color:#212121");
                    actionPane.setId(Main.dc.actions[i][0]);
                    actionPane.setOnTouchPressed(new EventHandler<TouchEvent>() {
                        @Override
                        public void handle(TouchEvent event) {
                            Node n = (Node) event.getSource();
                            System.out.println(n.getId());
                        }
                    });

                    HBox row = (HBox) Main.dc.controlVBox.getChildren().get(Main.dc.selectedRow);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            row.getChildren().set(Main.dc.selectedCol, actionPane);
                        }
                    });

                    String towrite = "actions_update::"+Main.dc.actions.length+"::";

                    for(String[] eachAction : Main.dc.actions)
                    {
                        //FileInputStream fs = new FileInputStream("actions/icons/"+eachAction[3]);
                        //byte[] imageB = fs.readAllBytes();
                        //fs.close();
                        //String base64Image = Base64.getEncoder().encodeToString(imageB);
                        towrite+=eachAction[0]+"__"+eachAction[1]+"__"+eachAction[2]+"__"+eachAction[3]+"__"+eachAction[4]+"__"+eachAction[5]+"__"+eachAction[6]+"::";
                    }
                    Main.dc.writeToOS(towrite);

                    Thread.sleep(50);

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            Main.dc.hideProgress();
                            Main.dc.hideNewActionConfigDialog();
                        }
                    });
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                return null;
            }
        }).start();

    }

    boolean isImageFileOK = false;
    File selectedIconFile;
    Image previewIcon;

    @FXML
    public void browseButtonClicked()
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG","*.png"));
        selectedIconFile = fileChooser.showOpenDialog(Main.ps);

        if(selectedIconFile == null)
        {
            isImageFileOK = false;
            iconPathField.setText("");
            iconPreviewImg.setImage(previewImageDefault);
        }

        try
        {
            previewIcon = new Image(selectedIconFile.toURI().toString());
            if(selectedIconFile.length() <= 32000)
            {
                iconPreviewImg.setImage(previewIcon);
                iconPathField.setText(selectedIconFile.getAbsolutePath());
                isImageFileOK = true;
            }
            else
            {
                System.out.println(previewIcon.getHeight() + ", "+previewIcon.getWidth()+", "+selectedIconFile.length());
                Main.dc.showErrorAlert("Uh Oh!","The Icon you provided doesnt meet the StreamPi Criteria. Max size : 32 KB");
                isImageFileOK = false;
                iconPreviewImg.setImage(previewImageDefault);
                iconPathField.setText("");
            }
        }
        catch (Exception e)
        {
            isImageFileOK = false;
            e.printStackTrace();
            Main.dc.showErrorAlert("Uh Oh!","It seems that the Icon you selected is invalid!");
            iconPreviewImg.setImage(previewImageDefault);
            iconPathField.setText("");
        }
    }


    public String generateRandomID() {
        Random r = new Random();
        return "action_"+r.nextInt((15000 - 1) + 1) + 1;
    }
}
