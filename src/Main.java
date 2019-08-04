import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.util.HashMap;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("dashboard.fxml"));
        primaryStage.setTitle("StreamPi Server");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        ps = primaryStage;
    }

    public static dashboardController dc;
    public static Stage ps;


    public static void main(String[] args) {
        launch(args);
    }
}
