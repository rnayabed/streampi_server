import com.jfoenix.controls.JFXDecorator;
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
        JFXDecorator decorator = new JFXDecorator(primaryStage, root);
        decorator.setCustomMaximize(true);
        Scene x = new Scene(decorator);
        primaryStage.setScene(x);
        primaryStage.show();
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
