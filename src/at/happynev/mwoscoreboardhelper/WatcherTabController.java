package at.happynev.mwoscoreboardhelper;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Nev on 15.01.2017.
 */
public class WatcherTabController {
    private final static Color defaultBackground = Color.valueOf("404040");
    private final static Color flashRed = Color.MAROON;
    private final static Color flashGreen = Color.GREENYELLOW;
    private static WatcherTabController instance;
    private final SimpleObjectProperty<Color> backgroundColor = new SimpleObjectProperty<>(defaultBackground);
    private final Map<Integer, Label> preliminaryPlayerInfo = new HashMap<>(24);
    @FXML
    ToggleButton toggleAutowatch;
    @FXML
    GridPane paneMyTeam;
    @FXML
    GridPane paneEnemyTeam;
    @FXML
    Label labelMap;
    @FXML
    Label labelGamemode;
    @FXML
    Label labelLastScreenshot;
    @FXML
    Label labelTimestamp;
    @FXML
    Label labelStatusInfo;
    @FXML
    Pane paneWatcherTab;
    @FXML
    Pane paneMatchAnalytics;
    @FXML
    Pane panePlayerdata;
    Timeline watcher = null;
    private int playersFinished = 0;
    private boolean isProcessing = false;
    private Set<String> alreadyProcessed;
    private MatchRuntime currentMatch = null;

    public WatcherTabController() {
        instance = this;
    }

    public static WatcherTabController getInstance() {
        if (instance == null) {
            instance = new WatcherTabController();
        }
        return instance;
    }

    public void stopWatching() {
        isProcessing = true;
    }

    public Set<String> getAlreadyProcessed() {
        return alreadyProcessed;
    }

    public void setSettingsLoaded(boolean settingsLoaded) {
        init();
    }

    private void init() {
        //load values
        boolean watcherActive = SettingsTabController.isAutowatchEnabled();
        toggleAutowatch.setSelected(watcherActive);
        if (watcherActive) {
            toggleAutowatch.setText("Watcher Active");
        } else {
            toggleAutowatch.setText("Watcher Inactive");
        }
        //set actions
        ObjectBinding<Background> backgroundBinding = Bindings.createObjectBinding(() -> {
            BackgroundFill fill = new BackgroundFill(backgroundColor.getValue(), CornerRadii.EMPTY, Insets.EMPTY);
            return new Background(fill);
        }, backgroundColor);
        paneWatcherTab.backgroundProperty().bind(backgroundBinding);
        paneMyTeam.backgroundProperty().bind(backgroundBinding);
        paneEnemyTeam.backgroundProperty().bind(backgroundBinding);
        toggleAutowatch.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (!SettingsTabController.getScreenshotDirectory().isDirectory() || SettingsTabController.getPlayername().isEmpty()) {
                Logger.alertPopup("Make sure to set Playername and Screenshot directory on the Settings tab");
            } else {
                SettingsTabController.setAutowatchEnabled(newValue);
                startWatcher(SettingsTabController.getInstance().getTextPollingInterval().getText());
                if (newValue) {
                    toggleAutowatch.setText("Watcher Active");
                } else {
                    toggleAutowatch.setText("Watcher Inactive");
                }
            }
        });
        startWatcher(SettingsTabController.getInstance().getTextPollingInterval().getText());
        alreadyProcessed = getProcessedFiles();
        //set changelisteners
        SettingsTabController.getInstance().getTextPollingInterval().textProperty().addListener((observable, oldValue, newValue) -> startWatcher(newValue));
        //paneMyTeam.maxWidthProperty().bind(panePlayerdata.widthProperty());
        //paneEnemyTeam.maxWidthProperty().bind(panePlayerdata.widthProperty());
    }

    private void startWatcher(String newValue) {
        int interval = (int) Double.parseDouble(newValue);
        if (watcher != null) watcher.stop();
        if (toggleAutowatch.isSelected()) {
            watcher = new Timeline(new KeyFrame(Duration.millis(interval), ev -> watcherLoop()));
            watcher.setCycleCount(Animation.INDEFINITE);
            watcher.play();
        }
    }

    public void setStatusInfo(String status) {
        labelStatusInfo.setText(status);
    }

    private File getNextScreenshot() {
        File f = null;
        for (File ls : SettingsTabController.getScreenshotDirectory().listFiles((dir, name) -> name.endsWith(".jpeg") || name.endsWith(".jpg") || name.endsWith(".png"))) {
            if (!alreadyProcessed.contains(ls.getName())) {
                return ls;
            }
        }
        return f;
    }

    private Set<String> getProcessedFiles() {
        Set<String> ret = new HashSet<>();
        try {
            PreparedStatement prep = DbHandler.getInstance().prepareStatement("select filename from processed");
            ResultSet rs = prep.executeQuery();
            while (rs.next()) {
                ret.add(rs.getString(1));
            }
            rs.close();
        } catch (Exception e) {
            Logger.error(e);
        }
        return ret;
    }

    private void watcherLoop() {
        if (!isProcessing) {
            File f = getNextScreenshot();
            if (f != null) {
                loadScreenshot(f);
            }
        }
    }

    private void loadScreenshot(File f) {
        isProcessing = true;
        paneWatcherTab.setDisable(true);
        labelLastScreenshot.setText(f.getName());
        MatchRuntime results = new MatchRuntime(f, currentMatch);
        paneWatcherTab.setDisable(false);
        if (results.isValid()) {
            playersFinished = 0;
            preliminaryPlayerInfo.clear();
            paneMatchAnalytics.getChildren().clear();
            GuiUtils.prepareGrid(paneMyTeam, results);
            GuiUtils.prepareGrid(paneEnemyTeam, results);
            this.currentMatch = results;
            labelMap.textProperty().bind(results.mapProperty());
            labelGamemode.textProperty().bind(results.gameModeProperty());
            labelTimestamp.textProperty().bind(results.formattedTimestampProperty());
            //textMatchName.textProperty().bind(results.matchNameProperty());
            for (int i = 0; i < 24; i++) {
                Label preliminaryInfo = new Label();
                PlayerRuntime.getReferencePlayer().applyPlayerFormat(preliminaryInfo);
                preliminaryInfo.textProperty().bind(results.getPreliminaryInfo().get(i));
                if (i < 12) {
                    paneMyTeam.add(preliminaryInfo, 0, 1 + i, GridPane.REMAINING, 1);
                } else {
                    paneEnemyTeam.add(preliminaryInfo, 0, 1 + i % 12, GridPane.REMAINING, 1);
                }
            }
            results.getPlayersTeam().addListener((ListChangeListener<? super PlayerRuntime>) c -> {
                c.next();
                c.getAddedSubList().forEach(o -> {
                    Logger.log("friend player " + o.getPilotname() + " finished tracing");
                    buildPlayerGui(o, paneMyTeam);
                });
            });
            results.getPlayersEnemy().addListener((ListChangeListener<? super PlayerRuntime>) c -> {
                c.next();
                c.getAddedSubList().forEach(o -> {
                    Logger.log("enemy  player " + o.getPilotname() + " finished tracing");
                    buildPlayerGui(o, paneEnemyTeam);
                });
            });
            flashBackground(flashGreen, 1500);
        } else {
            flashBackground(flashRed, 2000);
            isProcessing = false;
            //clean?
            //textMatchName.textProperty().unbind();
        }
    }

    private void buildPlayerGui(PlayerRuntime pr, GridPane parent) {
        try {
            Label preliminaryInfo = preliminaryPlayerInfo.get(pr.getPlayerNumber());
            preliminaryPlayerInfo.remove(pr.getPlayerNumber());
            parent.getChildren().remove(preliminaryInfo);
            int row = pr.getPlayerNumber() % 12;
            row++;//account for header
            pr.addDataToGrid(parent, row, currentMatch);
            playersFinished++;
        } catch (Exception e) {
            Logger.error(e);
            paneMyTeam.getChildren().add(new Label("Error adding player '" + pr.getPilotname() + "':" + e.toString()));
        }
        if (playersFinished == 24) {
            //all players traced. ok to proceed with next screenshot
            isProcessing = false;
            Logger.log("Tracing finished");
            paneMatchAnalytics.getChildren().clear();
            if (SettingsTabController.getInstance().getLayoutShowStatSummary()) {
                paneMatchAnalytics.getChildren().add(currentMatch.getMatchAnalyticsPane());
                Pane spacer = new Pane();
                spacer.setMaxHeight(Double.MAX_VALUE);
                VBox.setVgrow(spacer, Priority.ALWAYS);
                paneMatchAnalytics.getChildren().add(spacer);
                paneMatchAnalytics.getChildren().add(SessionRuntime.getSessionStatsPane());
            }
        }
    }

    private void flashBackground(Color flashTo, int duration) {
        KeyFrame flashframe = new KeyFrame(new Duration(0), new KeyValue(backgroundColor, flashTo));
        KeyFrame endframe = new KeyFrame(new Duration(duration), new KeyValue(backgroundColor, defaultBackground));
        Timeline flash = new Timeline(flashframe, endframe);
        flash.setCycleCount(1);
        flash.play();
    }
}
