package at.happynev.mwoscoreboardhelper;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.h2.tools.Server;

import java.net.URL;

public class Main extends Application {
    static {
        System.setProperty("logback.configurationFile", Main.class.getResource("logback.xml").toString());
        Logger.log("############################# Application STARTED ###########################");
    }

    public static int getDbVersion() {
        return 1;
    }

    public static String getVersion() {
        return "0.2 Blackjack";
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Server dbserver = Server.createTcpServer("-tcpPort", "9124", "-tcpAllowOthers", "-baseDir", Utils.getHomeDir().toString()).start();
        Logger.log("db server: " + dbserver.getURL());
        DbHandler.getInstance();//pre-init
        URL loc = this.getClass().getResource("ScoreboardHelper.fxml");
        Parent root = FXMLLoader.load(loc);
        Scene scene = new Scene(root);
        primaryStage.setTitle("ScoreboardHelper");
        primaryStage.setScene(scene);
        SettingsTabController.restoreWindowPos(primaryStage);

        primaryStage.setOnCloseRequest(
                event -> {
                    try {
                        WatcherTabController.getInstance().stopWatching();
                        SettingsTabController.saveWindowPos(primaryStage);
                    } catch (Exception e) {
                        Logger.error(e);
                    }
                    dbserver.stop();
                    Logger.log("######## Application closing ########");
                });
        primaryStage.show();
    }
}
