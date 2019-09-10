import animatefx.animation.*;
import com.jfoenix.controls.*;
import com.jfoenix.controls.events.JFXDialogEvent;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.robot.Robot;
import javafx.scene.text.Font;
import javafx.util.Duration;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

import java.io.*;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Random;
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
    public JFXTextField serverIPField;
    @FXML
    public JFXTextField serverPortField;
    @FXML
    public VBox connectionErrorPane;
    @FXML
    public JFXButton retryButton;
    @FXML
    public VBox settingsPane;
    @FXML
    public JFXButton loginButtonTwitter;
    @FXML
    public StackPane loginTwitterStackPane;
    @FXML
    public JFXTextField twitterConsumerSecretField;
    @FXML
    public JFXTextField twitterConsumerKeyField;
    @FXML
    private StackPane progressStackPane;

    int currentSelectionMode = 0;
    /*
    Selection Modes:
    0 - Nothing (Normal)
    1 - Hotkey
    2 - Script
    3 - Tweet
     */
    static boolean isTwitterSetup = true;

    HashMap<String, String> config = new HashMap<>();
    boolean isServerStarted = false;
    boolean isConnectedToClient = false;
    boolean firstRun = true;
    final Paint WHITE_PAINT = Paint.valueOf("#ffffff");
    Image close_icon = new Image(getClass().getResourceAsStream("icons/icon_preview.png"));

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        readConfig();
        twitterSetup();

        try {
            serverIP = Inet4Address.getLocalHost().getHostAddress();
            server = new ServerSocket(Integer.parseInt(config.get("server_port")));
            server.setReuseAddress(true);
            server.setReceiveBufferSize(950000000);
            server.setSoTimeout(0);
            server.setReceiveBufferSize(370923);

            if (!isServerStarted) {
                startServer();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }



        serverIPField.setText(serverIP);
        serverPortField.setText(config.get("server_port"));
        twitterConsumerSecretField.setText(config.get("twitter_oauth_consumer_secret"));
        twitterConsumerKeyField.setText(config.get("twitter_oauth_consumer_key"));


        Thread t = new Thread(serverCommTask);
        t.setDaemon(true);
        t.start();

        Main.dc = this;
    }

    Twitter twitter;

    public void twitterSetup()
    {
        new Thread(new Task<Void>() {
            @Override
            protected Void call() {
                try
                {
                    if(config.get("twitter_oauth_consumer_key").equals("NULL") || config.get("twitter_oauth_consumer_secret").equals("NULL") || config.get("twitter_oauth_access_token").equals("NULL") || config.get("twitter_oauth_access_token_secret").equals("NULL"))
                    {
                        isTwitterSetup = false;
                    }
                    else
                    {
                        System.setProperty("twitter4j.http.useSSL", "true");
                        ConfigurationBuilder cb = new ConfigurationBuilder();
                        cb.setDebugEnabled(true)
                                .setOAuthConsumerKey(config.get("twitter_oauth_consumer_key"))
                                .setOAuthConsumerSecret(config.get("twitter_oauth_consumer_secret"))
                                .setOAuthAccessToken(config.get("twitter_oauth_access_token"))
                                .setOAuthAccessTokenSecret(config.get("twitter_oauth_access_token_secret"));

                        TwitterFactory tf = new TwitterFactory(cb.build());
                        twitter = tf.getInstance();
                        twitter.verifyCredentials();
                        isTwitterSetup = true;
                    }
                }
                catch (Exception e)
                {
                    showErrorAlert(":(","Unable to Verify Twitter Login. Check stacktrace, or perhaps try relogging in.");
                    isTwitterSetup = false;
                    e.printStackTrace();
                }
                System.out.println("GAYYY : "+isTwitterSetup);
                return null;
            }
        }).start();
    }

    Random r = new Random();
    public void createNewTweet(String txtMsg)
    {
        for(int i = 0;i<r.nextInt(150);i++)
        {
            txtMsg+="⠀"; // U+2800 Blank code
        }
        try
        {
            twitter.updateStatus(txtMsg);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    showErrorAlert("Error!","Something went wrong. Check Stacktrace for more info.");
                }
            });
        }
    }

    @FXML
    public void showConnectionErrorPane()
    {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                retryButton.setDisable(false);
                connectionErrorPane.toFront();
                ZoomIn x = new ZoomIn(connectionErrorPane);
                x.setSpeed(3.0);
                x.play();
                System.out.println("XDC");
            }
        });
    }

    @FXML
    public void aboutStreamPiButtonClicked()
    {
        showErrorAlert("About Us","Created By Debayan\nOrginally Thought of by CorporalSaturn\nBETA");
    }

    @FXML
    public void hideConnectionErrorPane()
    {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if(connectionErrorPane.getOpacity()>0)
                {
                    System.out.println("FADING");
                    ZoomOut x = new ZoomOut(connectionErrorPane);
                    x.setOnFinished(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            connectionErrorPane.toBack();
                        }
                    });
                    x.setSpeed(3);
                    x.play();
                }
            }
        });
    }

    @FXML
    public void showSettingsPane()
    {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                retryButton.setDisable(false);
                settingsPane.toFront();
                ZoomIn x = new ZoomIn(settingsPane);
                x.setSpeed(3.0);
                x.play();
                System.out.println("XDC");
            }
        });
    }

    @FXML
    public void hideSettingsPane()
    {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if(settingsPane.getOpacity()>0)
                {
                    System.out.println("FADING");
                    ZoomOut x = new ZoomOut(settingsPane);
                    x.setOnFinished(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            settingsPane.toBack();
                        }
                    });
                    x.setSpeed(2);
                    x.play();
                }
            }
        });
    }

    @FXML
    public void retryButtonClicked()
    {
        /*if(serverPortField.getText().length()==0)
        {
            showErrorAlert("Invalid Server Port Value","It cannot be left empty!");
        }
        else
        {
            try
            {
                Integer.parseInt(serverPortField.getText());

                retryButton.setDisable(true);
                updateConfig("server_port",serverPortField.getText());
                startServer();
            }
            catch (Exception e2)
            {
                showErrorAlert("Invalid Server Port Value","It must be a numeric value.");
            }
        }*/

        retryButton.setDisable(true);
        hideConnectionErrorPane();
        startServer();
        retryButton.setDisable(false);
    }

    @FXML
    public JFXButton applyButton;

    @FXML
    public void applySettings()
    {
        boolean error = false;

        String errs = "";
        if(serverPortField.getText().length()==0)
        {
            errs += "*Invalid Server Port Value, It cannot be left empty!\n";
            error = true;
        }
        else
        {
            try {
                Integer.parseInt(serverPortField.getText());
            }
            catch (Exception e)
            {
                errs += "*Invalid Server Port Value, Only Numbers are accepted!\n";
                error = true;
            }
        }

        if(twitterConsumerKeyField.getText().length() ==0)
        {
            errs += "*Invalid Twitter Consumer Key Field, It cannot be left empty!\n";
            error = true;
        }

        if(twitterConsumerSecretField.getText().length() == 0)
        {
            errs += "*Invalid Twitter Consumer Secret Key Field, It cannot be left empty!\n";
            error = true;
        }


        if(!error)
        {
            updateConfig("server_port", serverPortField.getText());
            updateConfig("twitter_oauth_consumer_key", twitterConsumerKeyField.getText());
            updateConfig("twitter_oauth_consumer_secret",twitterConsumerSecretField.getText());
            showErrorAlert("Done!","Restart to see changes ;)");
        }
        else
        {
            showErrorAlert("Alert!","Please resolve the following errors : \n"+errs);
        }
    }

    public void updateConfig(String keyName, String newValue)
    {
        config.put(keyName,newValue);
        io.writeToFile(config.get("server_port")+"::"+config.get("twitter_oauth_consumer_key")+"::"+config.get("twitter_oauth_consumer_secret")+"::"+config.get("twitter_oauth_access_token")+"::"+config.get("twitter_oauth_access_token_secret")+"::","config");
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
                    new ZoomIn(notConnectedPane).play();
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
                new ZoomOut(notConnectedPane).play();
                notConnectedPane.toBack();
            }
        });

    }

    @FXML
    private Accordion actionsAccordion;

    @FXML
    public void showDeviceConfigPane()
    {
        //retrieve actions...
        new Thread(new Task<Void>() {
            @Override
            protected Void call() {
                try
                {
                    hideConnectionErrorPane();
                    //Thread.sleep(3000);
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
        //System.out.println("txt  : "+txt);
        /*txt = txt + "<END>";
        currentlyWriting = true;
        String[] chunks = Iterables.toArray(Splitter.fixedLength(1000).split(txt),String.class);
        for(int i = 0;i<chunks.length;i++)
        {
            os.writeUTF(chunks[i]);
            Thread.sleep(100);

        }*/
        //currentlyWriting = true;
        os.writeUTF(txt);

        //os.write(txt.getBytes(StandardCharsets.UTF_8).length);
        //os.write(txt.getBytes(StandardCharsets.UTF_8));
        //currentlyWriting = false;
        os.flush();
        ////System.out.println("txt : "+txt);
    }

    public String readFromIS() throws Exception
    {
        String eachChunk = is.readUTF();
        return eachChunk;
        /*String finalResult = "";
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

        //System.out.println("txtrr : "+finalResult);
        return finalResult;*/
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
                    if(isConnectedToClient)
                    {
                        isConnectedToClient = false;
                        writeToOS("client_quit::");
                        Thread.sleep(400);
                        socket.close();
                    }

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            showNotConnectedPane();
                        }
                    });

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println("xcasd");
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
                    //Start Server Issues
                    try {
                        Thread.sleep(2500);
                    }
                    catch (Exception ex)
                    {}
                    showConnectionErrorPane();
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    stackTrace1 = sw.toString();
                }
                return null;
            }
        });
        t1.setDaemon(true);
        t1.start();
    }

    @FXML
    public void showStackTraceOfConnectionError()
    {
        showErrorAlert("Stack Trace",stackTrace1);
    }

    String stackTrace1 = "";

    static int selectedRow;
    static int selectedCol;
    static String selectedActionUniqueID;

    static HashMap<String, Image> icons = new HashMap<>();
    static String[][] actions;
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
                    if(noOfActions == 0)
                    {
                        //TODO : To be updated...
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

                                        if(ar[0].equals("allocatedaction"))
                                        {
                                            if(currentSelectionMode == 0)
                                            {
                                                System.out.println("GAY");
                                                System.out.println("SETTING");
                                                selectedActionUniqueID = ar[3]+"_"+ar[4];

                                                for (String[] eachAction : actions) {
                                                    if(eachAction[0].equals(selectedActionUniqueID))
                                                    {
                                                        if(eachAction[2].equals("hotkey"))
                                                            loadPopupFXML("hotkeyConfig.fxml", 2);
                                                        else if(eachAction[2].equals("script"))
                                                            loadPopupFXML("scriptConfig.fxml",2);
                                                        else if(eachAction[2].equals("tweet"))
                                                            loadPopupFXML("tweetConfig.fxml",2);
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                        else
                                        {
                                            if(currentSelectionMode == 1)
                                                loadPopupFXML("hotkeyConfig.fxml",1);
                                            else if(currentSelectionMode == 2)
                                                loadPopupFXML("scriptConfig.fxml",1);
                                            else if(currentSelectionMode == 3)
                                                loadPopupFXML("tweetConfig.fxml",1);
                                        }
                                    }
                                });
                                actionPane[k].getStyleClass().add("action_box");
                            }

                            rows[i].getChildren().addAll(actionPane);
                        }

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                controlVBox.getChildren().clear();
                                controlVBox.getChildren().addAll(rows);
                                //hideNotConnectedPane();
                                ZoomIn x = new ZoomIn(deviceConfigPane);
                                x.setSpeed(3.0);
                                x.play();
                                deviceConfigPane.toFront();
                            }
                        });
                    }
                    else
                    {
                        controlVBox.setAlignment(Pos.TOP_CENTER);
                        System.out.println("XDAasdsd@@!@");
                        Thread.sleep(1500);
                        writeToOS("client_actions_icons_get::");
                    }
                }
                else if(msgHeader.equals("client_quit"))
                {
                    isConnectedToClient = false;
                    socket.close();
                    startServer();
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

                                        if(ar[0].equals("allocatedaction"))
                                        {
                                            if(currentSelectionMode == 0)
                                            {
                                                System.out.println("GAY");
                                                System.out.println("SETTING");
                                                selectedActionUniqueID = ar[3]+"_"+ar[4];

                                                for (String[] eachAction : actions) {
                                                    if(eachAction[0].equals(selectedActionUniqueID))
                                                    {
                                                        if(eachAction[2].equals("hotkey"))
                                                            loadPopupFXML("hotkeyConfig.fxml", 2);
                                                        else if(eachAction[2].equals("script"))
                                                            loadPopupFXML("scriptConfig.fxml",2);
                                                        else if(eachAction[2].equals("tweet"))
                                                            loadPopupFXML("tweetConfig.fxml",2);
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                        else
                                        {
                                            if(currentSelectionMode == 1)
                                                loadPopupFXML("hotkeyConfig.fxml",1);
                                            else if(currentSelectionMode == 2)
                                                loadPopupFXML("scriptConfig.fxml",1);
                                            else if(currentSelectionMode == 3)
                                                loadPopupFXML("tweetConfig.fxml",1);
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

                            Pane aPane = (Pane) rows[Integer.parseInt(actions[i][5])].getChildren().get(Integer.parseInt(actions[i][6]));
                            aPane.getChildren().add(icon);
                            //Pane actionPane = new Pane(icon);
                            aPane.setPrefSize(90,90);
                            //actionPane.setStyle("-fx-effect: dropshadow(three-pass-box, "+actions[i][4]+", 5, 0, 0, 0);-fx-background-color:#212121");
                            aPane.setId("allocatedaction_"+actions[i][5]+"_"+actions[i][6]+"_"+actions[i][0]);

                            //rows[Integer.parseInt(actions[i][5])].getChildren().set(Integer.parseInt(actions[i][6]), actionPane);
                        }

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                controlVBox.getChildren().clear();
                                controlVBox.getChildren().addAll(rows);
                                //hideNotConnectedPane();
                                ZoomIn x = new ZoomIn(deviceConfigPane);
                                x.setSpeed(3.0);
                                x.play();
                                deviceConfigPane.toFront();
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
                    /*int[] keys = new int[keysRaw.length];

                    for(int i = 0;i<keysRaw.length; i++)
                    {
                        keys[i] = Integer.parseInt(keysRaw[i]);
                        System.out.println("SAXX : "+keys[i]);
                    }*/

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Robot robot = new Robot();

                                for(String eachKey : keysRaw)
                                {
                                    System.out.println(eachKey);
                                    robot.keyPress(KeyCode.valueOf(eachKey));
                                }
                                Thread.sleep(50);
                                for(String eachKey : keysRaw)
                                {
                                    robot.keyRelease(KeyCode.valueOf(eachKey));
                                }
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                    });
                }
                else if(msgHeader.equals("script"))
                {
                    String scriptRunn[] = msgArr[1].split("<>");
                    Runtime r = Runtime.getRuntime();
                    System.out.println("Running \""+scriptRunn[0]+"\" \""+scriptRunn[1]+"\"");
                    r.exec("\""+scriptRunn[0]+"\" \""+scriptRunn[1]+"\"");
                }
                else if(msgHeader.equals("tweet"))
                {
                    String data[] = msgArr[1].split("<>");
                    if(isTwitterSetup)
                        createNewTweet(data[0]);
                    else
                        showErrorAlert("Uh Oh!","It looks like Twitter is not setup on this computer. Go to settings to add your account.");
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

    static int actionConfigType;
    /*
    1 = New
    2 = Edit
     */

    public void loadPopupFXML(String fxmlFileName, int actionConfigTypeHere)
    {
        actionConfigType = actionConfigTypeHere;
        popupStackPane.toFront();
        try
        {
            JFXDialogLayout newActionDialogLayout = new JFXDialogLayout();
            newActionDialogLayout.getStyleClass().add("dialog_style");
            Node actionConfig = FXMLLoader.load(getClass().getResource(fxmlFileName));
            newActionDialogLayout.setBody(actionConfig);
            newActionConfigDialog = new JFXDialog(popupStackPane, newActionDialogLayout, JFXDialog.DialogTransition.CENTER);
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
    }

    public void readConfig()
    {
        String[] configArray = io.readFileArranged("config","::");
        config.put("server_port",configArray[0]);
        config.put("twitter_oauth_consumer_key",configArray[1]);
        config.put("twitter_oauth_consumer_secret",configArray[2]);
        config.put("twitter_oauth_access_token",configArray[3]);
        config.put("twitter_oauth_access_token_secret",configArray[4]);
    }

    @FXML
    public void newHotkeyAction()
    {
        showNewActionHint("Hotkey");
    }

    @FXML
    public void newScriptAction()
    {
        showNewActionHint("Script");
    }

    @FXML
    public void newTweetAction()
    {
        showNewActionHint("Tweet");
    }

    public void showNewActionHint(String actionName)
    {
        if(actionName.equals("Hotkey"))
            currentSelectionMode = 1;
        else if(actionName.equals("Script"))
            currentSelectionMode = 2;
        else if(actionName.equals("Tweet"))
            currentSelectionMode = 3;

        actionsAccordion.setDisable(true);
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
        actionsAccordion.setDisable(false);
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


        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                alertStackPane.toFront();
                alertDialog.show();
            }
        });

    }

    @FXML
    public void loginButtonTwitterClicked()
    {
        loginButtonTwitter.setDisable(true);
        new Thread(new Task<Void>() {
            @Override
            protected Void call() {
                try {
                    ConfigurationBuilder cb2 = new ConfigurationBuilder();
                    cb2.setDebugEnabled(true);
                    cb2.setOAuthConsumerKey(config.get("twitter_oauth_consumer_key"));
                    cb2.setOAuthConsumerSecret(config.get("twitter_oauth_consumer_secret"));

                    TwitterFactory tf2 = new TwitterFactory(cb2.build());
                    Twitter t2 = tf2.getInstance();

                    RequestToken rt = t2.getOAuthRequestToken();
                    System.out.println(rt.getAuthorizationURL());

                    JFXDialogLayout l = new JFXDialogLayout();
                    l.getStyleClass().add("dialog_style");
                    Label headingLabel = new Label("Twitter Login");
                    headingLabel.setTextFill(WHITE_PAINT);
                    headingLabel.setFont(Font.font("Roboto Regular",25));
                    l.setHeading(headingLabel);

                    Label contentLabel = new Label("Go to the following URL ...");
                    contentLabel.setFont(Font.font("Roboto Regular",15));
                    contentLabel.setTextFill(WHITE_PAINT);
                    contentLabel.setWrapText(true);


                    JFXTextField c2 = new JFXTextField(rt.getAuthenticationURL());
                    c2.setStyle("-fx-text-inner-color:white");
                    c2.setEditable(false);
                    c2.setFont(Font.font("Roboto Regular",15));

                    Label c3 = new Label("... Then Authorize StreamPi, and enter the shown PIN Below ...");
                    c3.setFont(Font.font("Roboto Regular",15));
                    c3.setTextFill(WHITE_PAINT);
                    c3.setWrapText(true);

                    JFXTextField pinField = new JFXTextField();
                    pinField.setStyle("-fx-text-inner-color:white");
                    pinField.setFont(Font.font("Roboto Regular",15));

                    JFXButton goButton = new JFXButton("Login!");
                    goButton.setFont(Font.font("Roboto Regular",15));
                    goButton.setTextFill(WHITE_PAINT);

                    VBox x = new VBox(contentLabel,c2,c3,pinField,goButton);
                    x.setSpacing(10);
                    x.setAlignment(Pos.CENTER);
                    l.setBody(x);

                    JFXDialog loginTwitterPopup = new JFXDialog(loginTwitterStackPane,l, JFXDialog.DialogTransition.CENTER);
                    loginTwitterPopup.setOverlayClose(false);
                    loginTwitterPopup.getStyleClass().add("dialog_box");

                    goButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent event) {
                            new Thread(new Task<Void>() {
                                @Override
                                protected Void call() {
                                    String pin = pinField.getText();
                                    try {
                                        if (pin.length() > 0) {
                                            AccessToken accessToken = t2.getOAuthAccessToken(rt,pin);
                                            Platform.runLater(new Runnable() {
                                                @Override
                                                public void run() {
                                                    loginTwitterPopup.close();
                                                    loginTwitterPopup.setOnDialogClosed(new EventHandler<JFXDialogEvent>() {
                                                        @Override
                                                        public void handle(JFXDialogEvent event) {
                                                            loginTwitterStackPane.toBack();
                                                        }
                                                    });
                                                    updateConfig("twitter_oauth_access_token",accessToken.getToken());
                                                    updateConfig("twitter_oauth_access_token_secret",accessToken.getTokenSecret());
                                                    twitterSetup();
                                                    showErrorAlert("Congratulations "+accessToken.getUserId(),"Twitter is now Setup!");
                                                    loginButtonTwitter.setDisable(false);
                                                }
                                            });
                                        }
                                        else
                                        {
                                            showErrorAlert("Uh Oh!","Please enter a valid PIN");
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        showErrorAlert(":(","Unable to Proceed! Check StackTrace!");
                                    }
                                    return null;
                                }
                            }).start();
                        }
                    });


                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            loginTwitterStackPane.toFront();
                            loginTwitterPopup.show();
                        }
                    });

                }
                catch (Exception e)
                {
                    showErrorAlert(":(","Unable to Proceed! Check StackTrace!");
                    loginButtonTwitter.setDisable(false);
                    e.printStackTrace();
                }
                return null;
            }
        }).start();
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