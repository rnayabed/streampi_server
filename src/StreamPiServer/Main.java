package StreamPiServer;

import com.jfoenix.controls.JFXDecorator;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("dashboard.fxml"));
        primaryStage.setTitle("StreamPi Server");
        //JFXDecorator decorator = new JFXDecorator(primaryStage, root);
        //decorator.setCustomMaximize(true);
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

    public static void main(String[] args) {
        launch(args);
    }
}
