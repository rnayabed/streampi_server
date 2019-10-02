/*
dashboardController Class
Originally Written By Debayan Sutradhar
Contributors :

 */

import animatefx.animation.*;
import com.jfoenix.controls.*;
import com.jfoenix.controls.events.JFXDialogEvent;
import javafx.application.Platform;
import javafx.concurrent.Task;
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
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.robot.Robot;
import javafx.scene.text.Font;
import javafx.util.Duration;
import net.twasi.obsremotejava.Callback;
import net.twasi.obsremotejava.OBSRemoteController;
import net.twasi.obsremotejava.requests.ResponseBase;
import twitter4j.Twitter;
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

    // Importing all neccessary nodes from dashboard.fxml via respective FXML IDs
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
    public StackPane progressStackPane;
    @FXML
    public JFXButton applyButton;
    @FXML
    public JFXTextField eachActionSizeField;
    @FXML
    public JFXTextField eachActionPaddingField;
    @FXML
    public Accordion actionsAccordion;
    @FXML
    public HBox unableToConnectOBSHBox;
    @FXML
    public JFXButton retryConnectOBSButton;
    @FXML
    public JFXToggleButton obsToggleButton;

    //currentSelectionMode is used to distinguish between the type of action user wants to add...
    private int currentSelectionMode = 0;
    /*
    Selection Modes:
    0 - Nothing (Normal)
    1 - Hotkey
    2 - Script
    3 - Tweet
    4 - Folder
    5 - OBS Studio - Set Scene
    This isn't final and will go on increasing in the future.
     */

    //Global Boolean to ensure whether Twitter Dependencies are Setup...
    static boolean isTwitterSetup = false;

    static OBSRemoteController obsController;
    private boolean isOBSSetup = false;

    //Global Hashmap where config will be stored (taken from the config file)
    private HashMap<String, String> config = new HashMap<>();
    //Global Variable to store whether Server is connected to the client
    private boolean isConnectedToClient = false;
    //First Run variable, used especially to avoid init server animations on startup
    private boolean firstRun = true;
    //Global Paint Constant for white font in Alert Boxes (They are generated from code, and not hardcoded FXML)
    private final Paint WHITE_PAINT = Paint.valueOf("#ffffff");

    //Initialize method, runs when the application first starts
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        readConfig();
        twitterSetup();
        obsSetup();

        try {
            //Global variable to store the Computer's (Server) IP of the local network
            serverIP = Inet4Address.getLocalHost().getHostAddress();
            //Global Socket Variable, which is mainly used here to just open and close comms
            server = new ServerSocket(Integer.parseInt(config.get("server_port")));
            //Reuse address, if the previous thing goes haywire
            server.setReuseAddress(true);
            //Set Buffersize to 9.5 X 10^8 Bytes to accommodate for the
            server.setReceiveBufferSize(950000000);
            server.setSoTimeout(0);
            server.setReceiveBufferSize(370923);

            if (!isConnectedToClient) {
                //Server not started? Then Start it
                startServer();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        //In settings, set server IP field as the Host IP address (for the local network)
        serverIPField.setText(serverIP);
        //Set the Port Filed as the port no written in config
        serverPortField.setText(config.get("server_port"));
        //Set Twitter Api Keys
        twitterConsumerSecretField.setText(config.get("twitter_oauth_consumer_secret"));
        twitterConsumerKeyField.setText(config.get("twitter_oauth_consumer_key"));

        //Start new background thread to handle server connections
        Thread t = new Thread(serverCommTask);
        t.setDaemon(true);
        t.start();

        //Set the instance of dc in main as this class, so that other classes can access its nodes
        Main.dc = this;


    }

    @FXML
    private void obsSetup()
    {
        Task<Void> obsTask = new Task<Void>() {
            @Override
            protected Void call()
            {
                Platform.runLater(() -> retryConnectOBSButton.setDisable(true));
                try
                {
                    if(config.get("is_obs_setup").equals("0"))
                    {
                        obsToggleButton.setSelected(false);
                        isOBSSetup = false;
                        unableToConnectOBSHBox.setVisible(false);
                    }
                    else
                    {
                        obsToggleButton.setSelected(true);
                        obsController = new OBSRemoteController("ws://localhost:4444",false);
                        if(obsController.isFailed())
                        {
                            unableToConnectOBSHBox.setVisible(true);
                            isOBSSetup = false;
                        }
                        obsController.registerDisconnectCallback(new Callback() {
                            @Override
                            public void run(ResponseBase responseBase) {
                                isOBSSetup = false;
                                showErrorAlert("Uh Oh!","OBS is no more running!");
                                unableToConnectOBSHBox.setVisible(true);
                            }
                        });
                        obsController.registerConnectCallback(new Callback() {
                            @Override
                            public void run(ResponseBase responseBase) {
                                isOBSSetup = true;
                                unableToConnectOBSHBox.setVisible(false);
                                System.out.println("gay");
                            }
                        });
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                Platform.runLater(() -> retryConnectOBSButton.setDisable(false));
                return null;
            }
        };

        OBSThread= new Thread(obsTask);
        OBSThread.start();
    }

    Thread OBSThread;
    private Twitter twitter;

    //Checks whether Twitter is Setup or not, and takes the necessary steps
    private void twitterSetup()
    {
        //Runs in Thread to avoid UI Freezing
        new Thread(new Task<Void>() {
            @Override
            protected Void call() {
                try
                {
                    //Checks whether Twitter OAuth Keys are present or not ...
                    if(config.get("twitter_oauth_consumer_key").equals("NULL") || config.get("twitter_oauth_consumer_secret").equals("NULL") || config.get("twitter_oauth_access_token").equals("NULL") || config.get("twitter_oauth_access_token_secret").equals("NULL"))
                    {
                        isTwitterSetup = false;
                    }
                    else
                    {
                        isTwitterSetup = true;
                        //If present, then starts using the Twitter4j library

                        //Sets mode of connection via SSL as Twitter API uses only SSL
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
                    showErrorAlert(":(","Unable to Verify Twitter Login. Check stacktrace, or perhaps try relogging in.\nIt might be a network connection error as well");
                    isTwitterSetup = false;
                    e.printStackTrace();
                }
                return null;
            }
        }).start();
    }

    private Random r = new Random();
    private void createNewTweet(String txtMsg) throws Exception
    {
        //IMPORTANT : Twitter does not allow same tweet to be sent over and over again so here is workaround.
        //This trick adds few blank characters after the original text, so that twitter thinks it to be a new text tweet, and goes on to publish it!

        txtMsg = txtMsg + ("⠀".repeat(Math.max(0, r.nextInt(150)))) // U+2800 Blank code to avoid twitter
        ;

        //Uses the Twitter4J Instance to finally send the tweet
        twitter.updateStatus(txtMsg);
    }

    //Shows that Server was unable to start
    @FXML
    private void showConnectionErrorPane()
    {
        Platform.runLater(() -> {
            retryButton.setDisable(false);
            connectionErrorPane.toFront();
            ZoomIn x = new ZoomIn(connectionErrorPane);
            x.setSpeed(3.0);
            x.play();
        });
    }

    //Shows About
    @FXML
    public void aboutStreamPiButtonClicked()
    {
        showErrorAlert("About Us","Programmed By Debayan. Originally Thought of of Corporal Saturn\n\nAlpha - No Version");
    }

    //Hides that server was unable to start
    @FXML
    private void hideConnectionErrorPane()
    {
        Platform.runLater(() -> {
            if(connectionErrorPane.getOpacity()>0)
            {
                ZoomOut x = new ZoomOut(connectionErrorPane);
                x.setOnFinished(event -> connectionErrorPane.toBack());
                x.setSpeed(3);
                x.play();
            }
        });
    }

    //Shows the Settings Pane
    @FXML
    public void showSettingsPane()
    {
        Platform.runLater(() -> {
            if(config.get("is_obs_setup").equals("0"))
            {
                obsToggleButton.setSelected(false);
            }
            else if(config.get("is_obs_setup").equals("1"))
            {
                obsToggleButton.setSelected(true);
            }
            retryButton.setDisable(false);
            settingsPane.toFront();
            ZoomIn x = new ZoomIn(settingsPane);
            x.setSpeed(3.0);
            x.play();
        });
    }

    //Hides the Settings Pane
    @FXML
    public void hideSettingsPane()
    {
        Platform.runLater(() -> {
            if(settingsPane.getOpacity()>0)
            {
                ZoomOut x = new ZoomOut(settingsPane);
                x.setOnFinished(event -> settingsPane.toBack());
                x.setSpeed(2);
                x.play();
            }
        });
    }

    //Called when the Retry Button for "unable to start server" is clicked
    @FXML
    public void retryButtonClicked()
    {
        retryButton.setDisable(true);
        hideConnectionErrorPane();
        startServer();
        retryButton.setDisable(false);
    }

    //Called when the "Apply" Button in Settings is clicked
    @FXML
    private void applySettings()
    {
        boolean error = false;

        boolean isRestartableSettingChanged = false;
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
                if(!(serverPortField.getText()+"").equals(config.get("server_port")))
                    isRestartableSettingChanged = true;
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


        String paddingTextFieldText = eachActionPaddingField.getText();
        String sizeTextFieldText = eachActionSizeField.getText();
        try
        {
            int padding = Integer.parseInt(paddingTextFieldText);
            int size = Integer.parseInt(sizeTextFieldText);

            if(padding==0)
            {
                errs += "*Invalid Action Padding, It cannot be left empty!\n";
                error = true;
            }

            if(size==0)
            {
                errs += "*Invalid Action Size. It cannot be left empty\n";
                error = true;
            }
        }
        catch (Exception e)
        {
            errs += "*Invalid Action Size. Needs to be integer\n";
            error = true;
        }

        if(!error)
        {
            if(obsToggleButton.isSelected())
            {
                updateConfig("is_obs_setup","1");
                if(!isOBSSetup)
                {
                    obsSetup();
                }
            }
            else
            {
                updateConfig("is_obs_setup","0");
                isOBSSetup = false;
                unableToConnectOBSHBox.setVisible(false);
            }

            updateConfig("server_port", serverPortField.getText());
            updateConfig("twitter_oauth_consumer_key", twitterConsumerKeyField.getText());
            updateConfig("twitter_oauth_consumer_secret",twitterConsumerSecretField.getText());
            try {
                if(!sizeTextFieldText.equals(eachActionSize+"") || !paddingTextFieldText.equals(eachActionPadding))
                {
                    eachActionSize = Integer.parseInt(sizeTextFieldText);
                    eachActionPadding = Integer.parseInt(paddingTextFieldText);

                    streamPIMaxActionsPerRow = (int) Math.floor(streamPIWidth / (eachActionSize + eachActionPadding + eachActionPadding));
                    streamPIMaxNoOfRows = (int) Math.floor(streamPIHeight / (eachActionSize +eachActionPadding + eachActionPadding));

                    drawLayer(0);
                    writeToOS("client_action_size_padding_update::"+eachActionSize+"::"+eachActionPadding+"::");
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            if(isRestartableSettingChanged)
            {
                showErrorAlert("Done!","Restart to see changes ;)");
            }
            else
            {
                showErrorAlert("Done!","Settings have been applied");
            }
        }
        else
        {
            showErrorAlert("Alert!","Please resolve the following errors : \n"+errs);
        }
    }

    //Used to update the "config" file
    private void updateConfig(String keyName, String newValue)
    {
        config.put(keyName,newValue);
        io.writeToFile(config.get("server_port")+"::"+config.get("twitter_oauth_consumer_key")+"::"+config.get("twitter_oauth_consumer_secret")+"::"+config.get("twitter_oauth_access_token")+"::"+config.get("twitter_oauth_access_token_secret")+"::"+config.get("is_obs_setup")+"::","config");
    }

    //Shows "Listening For StreamPi" pane, indicating user that no Pi is connected to the server
    @FXML
    private void showNotConnectedPane() {
        Platform.runLater(() -> {
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
        });
    }

    //Hides "Listening For StreamPi" pane, indicating user that Pi is successfully connected to the server
    @FXML
    public void hideNotConnectedPane()
    {
        Platform.runLater(() -> {
            new ZoomOut(notConnectedPane).play();
            notConnectedPane.toBack();
        });
    }

    @FXML
    private void showDeviceConfigPane()
    {
        //Retrieve actions from the pi, in a background Thread to avoid UI Freeze
        new Thread(new Task<Void>() {
            @Override
            protected Void call() {
                try
                {
                    hideConnectionErrorPane();
                    if(isConnectedToClient)
                    {
                        writeToOS("client_details::");
                        Thread.sleep(300);
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

    private boolean currentlyReading = false;

    //Writes to the Output Stream of the Socket connection between pi and pc
    public void writeToOS(String txt) throws Exception
    {
        os.writeUTF(txt);
        os.flush();
    }

    //Writes from the Input Stream of the Socket connection between pi and pc
    public String readFromIS()
    {
        try {
            return is.readUTF();
        }
        catch (Exception e)
        {
            return null;
        }
    }

    //Necessary Variables for Socket Handling
    private ServerSocket server;
    private Socket socket;
    private DataInputStream is;
    private DataOutputStream os;

    //Stores the Server IP (Host's IP)
    private String serverIP;

    //Starts the Server, listening for the Pi on the Host
    @FXML
    private void startServer()
    {
        //Run in background Thread to avoid UI Freeze
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

                    Platform.runLater(() -> showNotConnectedPane());

                    Platform.runLater(() -> {
                        statusLabelNotConnectedPane.setText("Listening for StreamPi");
                        serverStatsLabel.setText("Server Running on "+serverIP+", Port "+config.get("server_port"));
                    });

                    socket = server.accept();
                    System.out.println("Connected!");
                    FadeOutUp fou2 = new FadeOutUp(statusLabelNotConnectedPane);
                    FadeOutUp fou3 = new FadeOutUp(serverStatsLabel);
                    fou3.play();
                    fou2.play();
                    fou2.setOnFinished(event -> {
                        Platform.runLater(() -> {
                            statusLabelNotConnectedPane.setText("Connected to "+socket.getRemoteSocketAddress().toString().replace("/",""));
                            serverStatsLabel.setText("Getting Things Ready...");
                        });
                        isConnectedToClient = true;
                        FadeInUp fiu3 = new FadeInUp(statusLabelNotConnectedPane);
                        FadeInUp fiu4 = new FadeInUp(serverStatsLabel);
                        fiu3.setDelay(Duration.millis(500));
                        fiu3.play();
                        fiu3.setOnFinished(event1 -> {
                            fiu4.play();
                            try
                            {
                                showDeviceConfigPane();
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        });
                    });
                }
                catch (Exception e)
                {
                    //Start Server Issues
                    try {
                        Thread.sleep(2500);
                    }
                    catch (Exception ex)
                    {
                        e.printStackTrace();
                    }
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
        //Start the thread.
        t1.start();
    }

    @FXML
    private void showStackTraceOfConnectionError()
    {
        showErrorAlert("Stack Trace",stackTrace1);
    }

    private String stackTrace1 = "";

    static int selectedRow;
    static int selectedCol;
    static String selectedActionUniqueID;

    static int maxLayers = 0;
    static int currentLayer = 0;
    static HashMap<String, Image> icons = new HashMap<>();
    static String[][] actions;
    static int eachActionSize;
    private int eachActionPadding;

    //listens to any replies or queries from the pi. Runs in a background Thread
    private Task<Void> serverCommTask = new Task<>() {
        @Override
        protected Void call() {
            while (true) try {
                if (!isConnectedToClient) {
                    Thread.sleep(100);
                    continue;
                }
                is = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                os = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                if (currentlyReading) {
                    Thread.sleep(500);
                    continue;
                }
                String message = readFromIS();
                System.out.println(message);

                String[] msgArr = message.split("::");
                String msgHeader = msgArr[0];

                System.out.println("'" + msgHeader + "'");
                if (msgHeader.equals("client_actions")) {
                    int noOfActions = Integer.parseInt(msgArr[1]);
                    actions = new String[noOfActions][8];
                    int index = 2;
                    for (int i = 0; i < noOfActions; i++) {
                        String[] actionChunk = msgArr[index].split("__");
                        actions[i][0] = actionChunk[0]; //Unique ID
                        actions[i][1] = actionChunk[1]; //Casual Name
                        actions[i][2] = actionChunk[2]; //Action Type
                        actions[i][3] = actionChunk[3]; //Action Content
                        actions[i][4] = actionChunk[4]; //Picture file name
                        actions[i][5] = actionChunk[5]; //Row No
                        actions[i][6] = actionChunk[6]; //Col No
                        actions[i][7] = actionChunk[7]; //Layer Index
                        index++;
                    }

                    maxLayers = Integer.parseInt(msgArr[index]);

                    if (noOfActions == 0) {
                        drawLayer(-1);
                    } else {
                        controlVBox.setAlignment(Pos.TOP_CENTER);
                        Thread.sleep(1000);
                        writeToOS("client_actions_icons_get::");
                    }
                } else if (msgHeader.equals("obs_set_scene")) {
                    if(isOBSSetup)
                    {
                        try {
                            obsController.setCurrentScene(msgArr[1], new Callback() {
                                @Override
                                public void run(ResponseBase responseBase) {
                                    if(responseBase.getStatus().equals("error"))
                                    {
                                        showErrorAlert("Uh Oh!","Unable to change to Scene '"+msgArr[1]+"'. Check whether it actually exists in the current profile...");
                                        sendSuccessResponse(msgArr[2],false);
                                    }
                                    else
                                    {
                                        sendSuccessResponse(msgArr[2],true);
                                    }
                                }
                            });
                        }
                        catch (Exception e)
                        {
                            showErrorAlert("Uh Oh!","Check whether OBS is setup in settings, or if OBS Studio is running with websocket plugin installed");
                            sendSuccessResponse(msgArr[2],false);
                        }
                    }
                    else
                    {
                        showErrorAlert("Uh Oh!","Check whether OBS is setup in settings, or if OBS Studio is running with websocket plugin installed");
                        sendSuccessResponse(msgArr[2],false);
                    }
                } else if (msgHeader.equals("client_quit")) {
                    isConnectedToClient = false;
                    socket.close();
                    startServer();
                } else if (msgHeader.equals("client_details")) {
                    streamPIIP = msgArr[1];
                    streamPINickName = msgArr[2];
                    streamPIWidth = Integer.parseInt(msgArr[3]);
                    streamPIHeight = Integer.parseInt(msgArr[4]);
                    streamPIMaxActionsPerRow = Integer.parseInt(msgArr[5]);
                    streamPIMaxNoOfRows = Integer.parseInt(msgArr[6]);
                    eachActionSize = Integer.parseInt(msgArr[7]);
                    eachActionPadding = Integer.parseInt(msgArr[8]);

                    Platform.runLater(()-> {
                        eachActionSizeField.setText(eachActionSize+"");
                        eachActionPaddingField.setText(eachActionPadding+"");
                    });

                    controlVBox.setSpacing(eachActionPadding);
                } else if (msgHeader.equals("action_icon")) {
                    String iconName = msgArr[1];

                    byte[] img_byte;
                    img_byte = Base64.getDecoder().decode(msgArr[2]);
                    Image img = new Image(new ByteArrayInputStream(img_byte));
                    if (!icons.containsKey(msgArr[2]))
                        icons.put(iconName, img);

                    boolean isPresent = true;
                    for (String[] eachAction : actions) {
                        if (!icons.containsKey(eachAction[4])) {
                            System.out.println(eachAction[4] + "XXS");
                            isPresent = false;
                            break;
                        }
                    }

                    if (isPresent && !isDrawn) {
                        drawLayer(currentLayer);

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                ZoomIn x = new ZoomIn(deviceConfigPane);
                                x.setSpeed(3.0);
                                x.play();
                                deviceConfigPane.toFront();
                            }
                        });
                    }
                } else if (msgHeader.equals("hotkey")) {
                    String keysRaw[] = msgArr[1].split("<>");
                    sendSuccessResponse(msgArr[2],true);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Robot robot = new Robot();

                                for (String eachKey : keysRaw) {
                                    System.out.println(eachKey);
                                    robot.keyPress(KeyCode.valueOf(eachKey));
                                }
                                Thread.sleep(50);
                                for (String eachKey : keysRaw) {
                                    robot.keyRelease(KeyCode.valueOf(eachKey));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } else if (msgHeader.equals("script")) {
                    sendSuccessResponse(msgArr[2],true);
                    String[] scriptRunn = msgArr[1].split("<>");
                    Runtime r = Runtime.getRuntime();
                    System.out.println("Running \"" + scriptRunn[0] + "\" \"" + scriptRunn[1] + "\"");
                    if (scriptRunn[0].length() == 0) {
                        r.exec("\"" + scriptRunn[1] + "\"");
                    } else {
                        r.exec("\"" + scriptRunn[0] + "\" \"" + scriptRunn[1] + "\"");
                    }
                } else if (msgHeader.equals("tweet")) {
                    String[] data = msgArr[1].split("<>");
                    if (isTwitterSetup)
                    {
                        try
                        {
                            sendSuccessResponse(msgArr[2],true);
                            createNewTweet(data[0]);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                            sendSuccessResponse(msgArr[2],false);
                            Platform.runLater(() -> showErrorAlert("Error!","Something went wrong. Check Stacktrace for more info."));
                        }
                    }
                    else
                    {
                        showErrorAlert("Uh Oh!", "It looks like Twitter is not setup on this computer. Go to settings to add your account.");
                        sendSuccessResponse(msgArr[2],false);
                    }
                } else {
                    System.out.println("'" + message + "'");
                }
            } catch (Exception e) {

                startServer();
                isConnectedToClient = false;
                isDrawn = false;
                e.printStackTrace();
            }
        }
    };

    private void sendSuccessResponse(String uniqueID, boolean isSuccess)
    {
        try
        {
            if(isSuccess)
                writeToOS("action_success_response::"+uniqueID+"::1::");
            else
                writeToOS("action_success_response::"+uniqueID+"::0::");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void drawLayer(int layerIndex)
    {
        isDrawn = true;
        System.out.println("PRESENT!!!");
        HBox[] rows = new HBox[streamPIMaxNoOfRows];
        for(int i = 0;i<streamPIMaxNoOfRows;i++)
        {
            rows[i] = new HBox();
            rows[i].setSpacing(eachActionPadding);
            rows[i].setAlignment(Pos.CENTER);

            Pane[] actionPane = new Pane[streamPIMaxActionsPerRow];
            for(int k = 0;k<streamPIMaxActionsPerRow;k++)
            {
                actionPane[k] = new Pane();
                actionPane[k].setPrefSize(eachActionSize,eachActionSize);
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
                                        else if(eachAction[2].equals("folder"))
                                            {
                                                if(event.getButton() == MouseButton.SECONDARY)
                                                {
                                                    loadPopupFXML("folderConfig.fxml",2);
                                                }
                                                else if(event.getButton() == MouseButton.PRIMARY)
                                                {
                                                    drawLayer(Integer.parseInt(eachAction[3]));
                                                }
                                            }
                                        else if(eachAction[2].equals("obs_set_scene"))
                                        {
                                            loadPopupFXML("OBSSetSceneConfig.fxml",2);
                                        }
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
                            else if(currentSelectionMode == 4)
                                loadPopupFXML("folderConfig.fxml",1);
                            else if(currentSelectionMode == 5)
                                loadPopupFXML("OBSSetSceneConfig.fxml",1);
                        }
                    }
                });
                actionPane[k].getStyleClass().add("action_box");
            }

            rows[i].getChildren().addAll(actionPane);
        }

        try {
            if(layerIndex > -1)
            {
                for(int i = 0;i<actions.length; i++)
                {
                    System.out.println(Integer.parseInt(actions[i][7])+","+layerIndex);
                    if(Integer.parseInt(actions[i][7]) != layerIndex)
                        continue;
                    ImageView icon = new ImageView();
                    icon.setImage(icons.get(actions[i][4]));
                    icon.setPreserveRatio(false);
                    icon.setFitHeight(eachActionSize);
                    icon.setFitWidth(eachActionSize);

                    Pane aPane = (Pane) rows[Integer.parseInt(actions[i][5])].getChildren().get(Integer.parseInt(actions[i][6]));
                    aPane.getChildren().add(icon);
                    aPane.setPrefSize(eachActionSize,eachActionSize);
                    aPane.setId("allocatedaction_"+actions[i][5]+"_"+actions[i][6]+"_"+actions[i][0]);
                }
            }
        } catch (IndexOutOfBoundsException e)
        {
            //TODO :: Show error that some action(s) couldnt be added due to different screen size
        }


        Platform.runLater(()-> {
            if(layerIndex == -1)
            {
                currentLayer = 0;
                ZoomIn x = new ZoomIn(deviceConfigPane);
                x.setSpeed(3.0);
                x.play();
                deviceConfigPane.toFront();
            }

            controlVBox.getChildren().clear();
            controlVBox.getChildren().addAll(rows);

            if(layerIndex != -1)
                currentLayer = layerIndex;
        });
    }

    @FXML
    public void returnToParentLayerButtonClicked()
    {
        for(String[] eachAction : actions)
        {
            if(eachAction[2].equals("folder") && eachAction[3].equals(currentLayer+""))
            {
                drawLayer(Integer.parseInt(eachAction[7]));
                break;
            }
        }
    }

    JFXDialog newActionConfigDialog;
    private String streamPIIP;
    private String streamPINickName;
    private int streamPIWidth;
    private int streamPIHeight;
    private int streamPIMaxActionsPerRow;
    private int streamPIMaxNoOfRows;
    private boolean isDrawn = false;

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
            newActionConfigDialog.setOnDialogClosed(event -> popupStackPane.toBack());
            newActionConfigDialog.show();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void readConfig()
    {
        String[] configArray = io.readFileArranged("config","::");
        config.put("server_port",configArray[0]);
        config.put("twitter_oauth_consumer_key",configArray[1]);
        config.put("twitter_oauth_consumer_secret",configArray[2]);
        config.put("twitter_oauth_access_token",configArray[3]);
        config.put("twitter_oauth_access_token_secret",configArray[4]);
        config.put("is_obs_setup",configArray[5]);
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
    public void newFolderAction()
    {
        showNewActionHint("Folder");
    }

    @FXML
    public void newOBSStudioSetSceneAction() {
        if(!isOBSSetup)
        {
            showErrorAlert("Uh Oh!","Make sure OBS Studio is setup in Settings\nIf yes, then check whether OBS Studio is running, with OBS Studio Websocket installed ...");
        }
        else
        {
            showNewActionHint("OBS Studio (Set Scene)");
        }
    }

    @FXML
    public void newTweetAction()
    {
        showNewActionHint("Tweet");
    }

    @FXML
    public JFXButton returnToParentLayerButton;

    private void showNewActionHint(String actionName)
    {
        switch (actionName) {
            case "Hotkey":
                currentSelectionMode = 1;
                break;
            case "Script":
                currentSelectionMode = 2;
                break;
            case "Tweet":
                currentSelectionMode = 3;
                break;
            case "Folder":
                currentSelectionMode = 4;
                break;
            case "OBS Studio (Set Scene)":
                currentSelectionMode = 5;
                break;
        }

        actionsAccordion.setDisable(true);
        returnToParentLayerButton.setDisable(true);
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
        returnToParentLayerButton.setDisable(false);
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

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                alertStackPane.getChildren().clear();
            }
        });
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

                    JFXButton closeButton = new JFXButton("Close");
                    closeButton.setFont(Font.font("Roboto Regular",15));
                    closeButton.setTextFill(WHITE_PAINT);

                    VBox x = new VBox(contentLabel,c2,c3,pinField,goButton,closeButton);
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

                    closeButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent event) {
                            loginTwitterPopup.close();
                            loginTwitterStackPane.toBack();
                            loginButtonTwitter.setDisable(false);
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