import animatefx.animation.*;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.jfoenix.controls.*;
import com.jfoenix.controls.events.JFXDialogEvent;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.Buffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.ResourceBundle;

public class dashboardController implements Initializable {
    @FXML
    public Label statusLabelNotConnectedPane;
    @FXML
    public Label serverStatsLabel;
    @FXML
    public AnchorPane deviceConfigPane;
    @FXML
    public VBox controlVBox;
    @FXML
    public VBox notConnectedPane;
    @FXML
    public Label addNewButtonHintLabel;
    @FXML
    public JFXButton cancelNewActionButton;
    @FXML
    public StackPane popupStackPane;
    @FXML
    public HBox newActionHintHBox;
    @FXML
    public StackPane alertStackPane;
    @FXML
    private StackPane progressStackPane;

    int currentSelectionMode = 0;
    /*
    Selection Modes:
    0 - Nothing (Normal)
    1 - Hotkey
     */

    HashMap<String, String> config = new HashMap<>();
    boolean isServerStarted = false;
    boolean isConnectedToClient = false;
    boolean firstRun = true;
    final Paint WHITE_PAINT = Paint.valueOf("#ffffff");
    Image close_icon = new Image(getClass().getResourceAsStream("icons/icon_preview.png"));

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            readConfig();
            serverIP = Inet4Address.getLocalHost().getHostAddress();
            server = new ServerSocket(Integer.parseInt(config.get("server_port")));
            server.setReceiveBufferSize(950000000);
            server.setSoTimeout(0);
            server.setReceiveBufferSize(370923);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!isServerStarted) {
            startServer();
        }



        Thread t = new Thread(serverCommTask);
        t.setDaemon(true);
        t.start();

        Main.dc = this;
    }

    @FXML
    public void showNotConnectedPane() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if(firstRun)
                {
                    notConnectedPane.toFront();
                    notConnectedPane.setOpacity(1.0);
                    firstRun = false;
                }
                else
                {
                    new FadeInUp(notConnectedPane).play();
                    if(newActionHintHBox.getOpacity()==1)
                        hideNewActionHint();
                    notConnectedPane.toFront();
                }
            }
        });

    }

    @FXML
    public void hideNotConnectedPane()
    {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                new FadeOutUp(notConnectedPane).play();
                notConnectedPane.toBack();
            }
        });

    }

    @FXML
    public void showDeviceConfigPane()
    {
        //retrieve actions...
        new Thread(new Task<Void>() {
            @Override
            protected Void call() {
                try
                {
                    Thread.sleep(3000);
                    System.out.println("xc");
                    if(isConnectedToClient)
                    {
                        System.out.println("xa11");
                        writeToOS("client_details::");
                        Thread.sleep(1000);
                        writeToOS("get_actions::");
                    }


                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                return null;
            }
        }).start();

    }

    public boolean currentlyWriting = false;
    public boolean currentlyReading = false;
    public void writeToOS(String txt) throws Exception
    {
        txt = txt + "<END>";
        currentlyWriting = true;
        String[] chunks = Iterables.toArray(Splitter.fixedLength(1000).split(txt),String.class);
        for(int i = 0;i<chunks.length;i++)
        {
            os.writeUTF(chunks[i]);
            Thread.sleep(100);

        }
        //os.writeUTF(txt);

        //os.write(txt.getBytes(StandardCharsets.UTF_8).length);
        //os.write(txt.getBytes(StandardCharsets.UTF_8));
        currentlyWriting = false;
        os.flush();
        //System.out.println("txt : "+txt);
    }

    public String readFromIS() throws Exception
    {
        String finalResult = "";
        while(true)
        {
            if(currentlyWriting)
            {
                Thread.sleep(500);
                continue;
            }
            String eachChunk = is.readUTF();
            if(!eachChunk.endsWith("<END>"))
            {
                finalResult += eachChunk;
            }
            else
            {
                finalResult += eachChunk.replace("<END>","");
                break;
            }
        }

        System.out.println("txtrr : "+finalResult);
        return finalResult;
    }

    ServerSocket server;
    Socket socket;
    DataInputStream is;
    DataOutputStream os;

    String serverIP;


    @FXML
    public void startServer()
    {
        Thread t1 = new Thread(new Task<Void>() {
            @Override
            protected Void call(){
                try
                {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            showNotConnectedPane();
                        }
                    });

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            statusLabelNotConnectedPane.setText("Listening for StreamPi");
                            serverStatsLabel.setText("Server Running on "+serverIP+", Port "+config.get("server_port"));
                        }
                    });

                    socket = server.accept();
                    System.out.println("Connected!");
                    FadeOutUp fou2 = new FadeOutUp(statusLabelNotConnectedPane);
                    FadeOutUp fou3 = new FadeOutUp(serverStatsLabel);
                    fou3.play();
                    fou2.play();
                    fou2.setOnFinished(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    statusLabelNotConnectedPane.setText("Connected to "+socket.getRemoteSocketAddress().toString().replace("/",""));
                                    serverStatsLabel.setText("Getting Things Ready...");
                                }
                            });
                            isConnectedToClient = true;
                            FadeInUp fiu3 = new FadeInUp(statusLabelNotConnectedPane);
                            FadeInUp fiu4 = new FadeInUp(serverStatsLabel);
                            fiu3.setDelay(Duration.millis(500));
                            fiu3.play();
                            fiu3.setOnFinished(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    fiu4.play();
                                    try
                                    {
                                        showDeviceConfigPane();

                                    }
                                    catch (Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    });
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                return null;
            }
        });
        t1.setDaemon(true);
        t1.start();
    }

    int selectedRow;
    int selectedCol;

    HashMap<String, Image> icons = new HashMap<>();
    String[][] actions;
    Task<Void> serverCommTask = new Task<Void>() {
        @Override
        protected Void call() {
            while(true) {
            try
            {
                if(!isConnectedToClient)
                {
                    Thread.sleep(100);
                    continue;
                }
                is = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                os = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));


                    if (currentlyReading)
                    {
                        Thread.sleep(500);
                        continue;
                    }
                    System.out.println("sdsdasdasdasdasd");
                    String message = readFromIS();
                    System.out.println(message);

                    String[] msgArr = message.split("::");
                    String msgHeader = msgArr[0];

                    System.out.println("'"+msgHeader+"'");
                    if(msgHeader.equals("client_actions"))
                    {
                        System.out.println("XDA@@!@");
                        int noOfActions = Integer.parseInt(msgArr[1]);
                        actions = new String[noOfActions][7];
                        int index = 2;
                        for(int i = 0; i<noOfActions;i++)
                        {
                            String[] actionChunk = msgArr[index].split("__");
                            actions[i][0] = actionChunk[0]; //Unique ID
                            actions[i][1] = actionChunk[1]; //Casual Name
                            actions[i][2] = actionChunk[2]; //Action Type
                            actions[i][3] = actionChunk[3]; //Action Content
                            //actions[i][3] = actionChunk[3]; //Picture in base 64
                            actions[i][4] = actionChunk[4]; //Picture file name
                            //actions[i][4] = actionChunk[4]; //Ambient Colour
                            actions[i][5] = actionChunk[5]; //Row No
                            actions[i][6] = actionChunk[6]; //Col No
                            index++;
                        }
                        System.out.println("XDAasdsd@@!@");
                        Thread.sleep(1500);
                        writeToOS("client_actions_icons_get::");
                    }
                    else if(msgHeader.equals("client_details"))
                    {
                        streamPIIP = msgArr[1];
                        streamPINickName = msgArr[2];
                        streamPIWidth = Integer.parseInt(msgArr[3]);
                        streamPIHeight = Integer.parseInt(msgArr[4]);
                        streamPIMaxActionsPerRow = Integer.parseInt(msgArr[5]);
                        streamPIMaxNoOfRows = Integer.parseInt(msgArr[6]);
                        System.out.println("NIGGER REGISTERED!");
                    }
                    else if(msgHeader.equals("action_icon"))
                    {
                        String iconName = msgArr[1];
                        System.out.println("ICON NAME : "+iconName);
                        //byte[] imageIcon = Base64.getDecoder().decode(msgArr[2]);
                        //Image newImage = new Image(new ByteArrayInputStream(imageIcon));
                        System.out.println("msg arr : "+msgArr[2]);

                        byte[] img_byte;
                        img_byte = Base64.getDecoder().decode(msgArr[2]);
                        //BufferedImage img = ImageIO.read(new ByteArrayInputStream(img_byte));
                        Image img = new Image(new ByteArrayInputStream(img_byte));
                        if(!icons.containsKey(msgArr[2]))
                            icons.put(iconName, img);


                        //check all icons present or not
                        boolean isPresent = true;
                        for(String[] eachAction : actions)
                        {
                            if(!icons.containsKey(eachAction[4]))
                            {
                                System.out.println(eachAction[4]+"XXS");
                                isPresent = false;
                                break;
                            }
                        }

                        if(isPresent && !isDrawn)
                        {
                            isDrawn = true;
                            System.out.println("PRESENT!!!");
                            HBox[] rows = new HBox[streamPIMaxNoOfRows];
                            for(int i = 0;i<streamPIMaxNoOfRows;i++)
                            {
                                rows[i] = new HBox();
                                rows[i].setSpacing(20);
                                rows[i].setAlignment(Pos.CENTER);

                                Pane[] actionPane = new Pane[streamPIMaxActionsPerRow];
                                for(int k = 0;k<streamPIMaxActionsPerRow;k++)
                                {
                                    actionPane[k] = new Pane();
                                    actionPane[k].setPrefSize(90,90);
                                    actionPane[k].setId("freeAction_"+i+"_"+k);
                                    actionPane[k].setOnMouseClicked(new EventHandler<MouseEvent>() {
                                        @Override
                                        public void handle(MouseEvent event) {
                                            Pane n = (Pane) event.getSource();

                                            String[] ar = n.getId().split("_");

                                            selectedRow = Integer.parseInt(ar[1]);
                                            selectedCol = Integer.parseInt(ar[2]);

                                            if(currentSelectionMode == 0)
                                            {
                                                System.out.println("now is normal");
                                            }
                                            else if(currentSelectionMode == 1)
                                            {
                                                popupStackPane.toFront();

                                                try
                                                {
                                                    JFXDialogLayout newHotkeyActionDialogLayout = new JFXDialogLayout();
                                                    newHotkeyActionDialogLayout.getStyleClass().add("dialog_style");
                                                    VBox newHotkey = FXMLLoader.load(getClass().getResource("newHotkeyPopup.fxml"));
                                                    newHotkeyActionDialogLayout.setBody(newHotkey);
                                                    newActionConfigDialog = new JFXDialog(popupStackPane, newHotkeyActionDialogLayout, JFXDialog.DialogTransition.CENTER);
                                                    newActionConfigDialog.setOverlayClose(false);
                                                    newActionConfigDialog.setOnDialogClosed(new EventHandler<JFXDialogEvent>() {
                                                        @Override
                                                        public void handle(JFXDialogEvent event) {
                                                            popupStackPane.toBack();
                                                        }
                                                    });
                                                    newActionConfigDialog.show();
                                                }
                                                catch (Exception e)
                                                {
                                                    e.printStackTrace();
                                                }


                                                System.out.println("now is hotkey");
                                            }
                                        }
                                    });
                                    actionPane[k].getStyleClass().add("action_box");
                                }

                                rows[i].getChildren().addAll(actionPane);
                            }

                            for(int i = 0;i<actions.length; i++)
                            {
                                System.out.println("XDA121d@@!@");
                                System.out.println("actions[i]XX : "+actions[i][3]);
                                ImageView icon = new ImageView();
                                icon.setImage(icons.get(actions[i][4]));
                                icon.setFitHeight(90);
                                icon.setFitWidth(90);

                                Pane actionPane = new Pane(icon);
                                actionPane.setPrefSize(90,90);
                                //actionPane.setStyle("-fx-effect: dropshadow(three-pass-box, "+actions[i][4]+", 5, 0, 0, 0);-fx-background-color:#212121");
                                actionPane.setId(actions[i][0]);
                                actionPane.setOnTouchPressed(new EventHandler<TouchEvent>() {
                                    @Override
                                    public void handle(TouchEvent event) {
                                        Node n = (Node) event.getSource();
                                        System.out.println(n.getId());
                                    }
                                });

                                rows[Integer.parseInt(actions[i][5])].getChildren().set(Integer.parseInt(actions[i][6]), actionPane);
                            }

                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    controlVBox.getChildren().clear();
                                    controlVBox.getChildren().addAll(rows);
                                    Platform.runLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            hideNotConnectedPane();
                                            new FadeInUp(deviceConfigPane).play();
                                            deviceConfigPane.toFront();
                                        }
                                    });
                                }
                            });
                        }
                        else
                        {
                            System.out.println("SORRY MADARCHOD!");
                        }
                    }
                    else if(msgHeader.equals("hotkey"))
                    {
                        String keysRaw[] = msgArr[1].split("<>");
                        int[] keys = new int[keysRaw.length];

                        for(int i = 0;i<keysRaw.length; i++)
                        {
                            if(keysRaw[i].equals("WIN"))
                            {
                                keys[i] = KeyEvent.VK_WINDOWS;
                            }
                            else if(keysRaw[i].equals("R"))
                            {
                                keys[i] = KeyEvent.VK_R;
                            }
                        }

                        Robot robot = new Robot();

                        for(int eachKey : keys)
                        {
                            robot.keyPress(eachKey);
                        }
                        Thread.sleep(50);
                        for(int eachKey : keys)
                        {
                            robot.keyRelease(eachKey);
                        }
                    }
                    else
                    {
                        System.out.println("'"+message+"'");
                    }
                }
            catch (Exception e)
            {

                startServer();
                isConnectedToClient = false;
                isDrawn = false;
                e.printStackTrace();
            }
            }
        }
    };
    JFXDialog newActionConfigDialog;
    String streamPIIP;
    String streamPINickName;
    int streamPIWidth;
    int streamPIHeight;
    int streamPIMaxActionsPerRow;
    int streamPIMaxNoOfRows;
    boolean isDrawn = false;


    public void readConfig()
    {
        String[] configArray = io.readFileArranged("config","::");
        config.put("server_port",configArray[0]);
    }

    @FXML
    public void newHotkeyAction()
    {
        currentSelectionMode = 1;
        showNewActionHint("Hotkey");
    }

    public void showNewActionHint(String actionName)
    {
        for(Node eachRowN : controlVBox.getChildren())
        {
            HBox eachRow = (HBox) eachRowN;
            for(Node eachActionN : eachRow.getChildren())
            {
                Pane eachAction = (Pane) eachActionN;
                if(eachAction.getId().contains("freeAction"))
                {
                    eachAction.getStyleClass().add("action_box_highlight");
                }
            }
        }

        addNewButtonHintLabel.setText("To add a new "+actionName+", click on the desired green Action Box");
        new FadeInUp(newActionHintHBox).play();
        cancelNewActionButton.setDisable(false);
    }

    @FXML
    public void hideNewActionHint()
    {
        for(Node eachRowN : controlVBox.getChildren())
        {
            HBox eachRow = (HBox) eachRowN;
            for(Node eachActionN : eachRow.getChildren())
            {
                Pane eachAction = (Pane) eachActionN;
                if(eachAction.getId().contains("freeAction"))
                {
                    eachAction.getStyleClass().remove("action_box_highlight");
                }
            }
        }
        currentSelectionMode = 0;
        cancelNewActionButton.setDisable(true);
        new FadeOutDown(newActionHintHBox).play();
    }

    //TODO : Call System.gc() every 30 secs to clear memory.

    public void showErrorAlert(String heading, String content)
    {
        JFXDialogLayout l = new JFXDialogLayout();
        l.getStyleClass().add("dialog_style");
        Label headingLabel = new Label(heading);
        headingLabel.setTextFill(WHITE_PAINT);
        headingLabel.setFont(Font.font("Roboto Regular",25));
        l.setHeading(headingLabel);
        Label contentLabel = new Label(content);
        contentLabel.setFont(Font.font("Roboto Regular",15));
        contentLabel.setTextFill(WHITE_PAINT);
        contentLabel.setWrapText(true);
        l.setBody(contentLabel);
        JFXButton okButton = new JFXButton("OK");
        okButton.setTextFill(WHITE_PAINT);
        l.setActions(okButton);

        JFXDialog alertDialog = new JFXDialog(alertStackPane,l, JFXDialog.DialogTransition.CENTER);
        alertDialog.setOverlayClose(false);
        alertDialog.getStyleClass().add("dialog_box");
        okButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                alertDialog.close();
                alertDialog.setOnDialogClosed(new EventHandler<JFXDialogEvent>() {
                    @Override
                    public void handle(JFXDialogEvent event) {
                        alertStackPane.toBack();
                    }
                });
            }
        });

        alertStackPane.toFront();
        alertDialog.show();
    }

    public void showProgress(String heading, String text)
    {
        JFXDialogLayout l = new JFXDialogLayout();
        l.getStyleClass().add("dialog_style");
        Label headingLabel = new Label(heading);
        headingLabel.setTextFill(WHITE_PAINT);
        headingLabel.setFont(Font.font("Roboto Regular",25));
        l.setHeading(headingLabel);
        Label textLabel = new Label(text);
        textLabel.setFont(Font.font("Roboto Regular",15));
        textLabel.setTextFill(WHITE_PAINT);
        textLabel.setWrapText(true);

        //TODO : Add Transparent Indeterminate Progress loading gif

        HBox content = new HBox(textLabel);
        l.setBody(content);

        progressDialog = new JFXDialog(progressStackPane,l, JFXDialog.DialogTransition.CENTER);
        progressDialog.setOverlayClose(false);
        progressDialog.getStyleClass().add("dialog_box");

        progressStackPane.toFront();
        progressDialog.show();
    }

    JFXDialog progressDialog;

    public void hideProgress()
    {
        progressDialog.close();
        progressDialog.setOnDialogClosed(new EventHandler<JFXDialogEvent>() {
            @Override
            public void handle(JFXDialogEvent event) {
                progressStackPane.toBack();
            }
        });
    }

    public void hideNewActionConfigDialog()
    {
        newActionConfigDialog.close();
        newActionConfigDialog.setOnDialogClosed(new EventHandler<JFXDialogEvent>() {
            @Override
            public void handle(JFXDialogEvent event) {
                popupStackPane.toBack();
            }
        });
    }
}