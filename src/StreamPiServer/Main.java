package StreamPiServer;

import com.jfoenix.controls.JFXDecorator;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.eclipse.jetty.util.resource.Resource;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        readConfig();
        ResourceBundle rb = ResourceBundle.getBundle("StreamPiServer.languageBundles.lang",new Locale(config.get("language")));
        Parent root = FXMLLoader.load(getClass().getResource("dashboard.fxml"),rb);
        primaryStage.getIcons().add(new Image("icons/app_icon.png"));
        primaryStage.setTitle("StreamPi Server");
        Scene x = new Scene(root);
        primaryStage.setMinWidth(1160);
        primaryStage.setMinHeight(650);
        primaryStage.setScene(x);
        primaryStage.show();
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                System.out.println("Quitting ...");
                System.exit(0);
            }
        });
        ps = primaryStage;
        xs = x;
    }

    public static dashboardController dc;
    public static Stage ps;
    public static Scene xs;

    public static HashMap<String,String> config = new HashMap<>();

    private void readConfig()
    {
        String[] configArray = io.readFileArranged("config","::");
        config.put("server_port",configArray[0]);
        config.put("twitter_oauth_consumer_key",configArray[1]);
        config.put("twitter_oauth_consumer_secret",configArray[2]);
        config.put("twitter_oauth_access_token",configArray[3]);
        config.put("twitter_oauth_access_token_secret",configArray[4]);
        config.put("is_obs_setup",configArray[5]);
        config.put("obs_websocket_address",configArray[6]);
        config.put("server_ip",configArray[7]);
        config.put("language",configArray[8]);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
