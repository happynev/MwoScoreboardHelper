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
        //TODO match tab (matches table, match details, link to SS, retrace?
        //TODO player tab: stats, sorting
        //TODO automerge(alias table)
        //TODO team details
        //TODO playerstat win/loss ratio
        //TODO reward screen (xp,cbills,kmdd)
        //TODO match notes
        //TODO CSV matchdata
        //TODO CSV db export
        //TODO merge SS folders (prep, reward, summary)
        //TODO SQL tab table list
        //TODO memory monitor
        //TODO watchertab: column ordering
        //TODO open ss folder
        //TODO wipe all stats
        //TODO reimport matches from archive
        //TODO quick styles
        //TODO team stat: median damage
        //TODO team stat: median score
        //TODO team stat: total tonnage
        //TODO team stat: assists per kill
        //TODO team stat: damage per ton
        //TODO team stat: mech classes
        //TODO match result (12-3)
        //TODO settings: customize font size
        //TODO hangar screenshot: reset match/earned cbills,xp?
        //TODO server persistieren
        //TODO match duplicate detection logic
        //TODO player tab: manual unit/name edit
        //TODO fix AS7-K(L) detection
        //TODO faction play??
        //TODO refactor match calculated stats
        //TODO SS ident first, then new matchruntime
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
