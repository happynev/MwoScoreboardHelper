package at.happynev.mwoscoreboardhelper;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.h2.tools.Server;

import java.net.URL;
import java.util.Arrays;

public class Main extends Application {
    private static boolean debug = false;

    static {
        System.setProperty("logback.configurationFile", Main.class.getResource("logback.xml").toString());
        Logger.log("############################# Application STARTED ###########################");
    }

    private Server dbserver;

    public static boolean isDebug() {
        return debug;
    }

    public static int getDbVersion() {
        return 4;
    }

    public static String getVersion() {
        return "0.41 High Roller";
    }

    public static void main(String[] args) {
        if (Arrays.asList(args).contains("debug")) {
            debug = true;
        }
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        dbserver = Server.createTcpServer("-tcpPort", "9124", "-tcpAllowOthers", "-baseDir", Utils.getHomeDir().toString()).start();
        Logger.log("db server: " + dbserver.getURL());
        DbHandler.getInstance();//pre-init
        URL loc = this.getClass().getResource("ScoreboardHelper.fxml");
        try {
            Parent root = FXMLLoader.load(loc);
            Scene scene = new Scene(root);
            primaryStage.setTitle("ScoreboardHelper");
            primaryStage.setScene(scene);
            SettingsTabController.restoreWindowPos(primaryStage);
        } catch (Exception e) {
            Logger.error(e);
            Logger.alertPopup("error loading GUI. exiting:");
            shutdown();
        }

        primaryStage.setOnCloseRequest(
                event -> {
                    WatcherTabController.getInstance().stopWatching();
                    if (dbserver != null) {
                        SettingsTabController.saveWindowPos(primaryStage);
                        shutdown();
                    }
                }

        );
        primaryStage.show();
    }

    private void shutdown() {
        if (dbserver != null) {
            dbserver.stop();
        }
        dbserver = null;
        Logger.log("######## Application closing ########");
    }
}
