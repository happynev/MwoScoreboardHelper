package at.happynev.mwoscoreboardhelper;

import at.happynev.mwoscoreboardhelper.stat.StatTable;
import at.happynev.mwoscoreboardhelper.tracer.ScreenshotType;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.SimpleObjectProperty;
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
    GridPane panePersonal;
    @FXML
    GridPane paneMyTeam;
    @FXML
    GridPane paneEnemyTeam;
    @FXML
    Label labelMap;
    @FXML
    Label labelGamemode;
    @FXML
    Label labelMatchResult;
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
    private boolean isProcessing = false;
    private Set<String> alreadyProcessed;

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
            SettingsTabController.setAutowatchEnabled(newValue);
            startWatcher(SettingsTabController.getInstance().getTextPollingInterval().getText());
            if (newValue) {
                toggleAutowatch.setText("Watcher Active");
            } else {
                toggleAutowatch.setText("Watcher Inactive");
            }
        });
        startWatcher(SettingsTabController.getInstance().getTextPollingInterval().getText());
        alreadyProcessed = getProcessedFiles();
        //set changelisteners
        SettingsTabController.getInstance().getTextPollingInterval().textProperty().addListener((observable, oldValue, newValue) -> startWatcher(newValue));
    }

    private void startWatcher(String newValue) {
        int interval = (int) Double.parseDouble(newValue);
        if (watcher != null) watcher.stop();
        if (toggleAutowatch.isSelected()) {
            if (SettingsTabController.isSafeToStart()) {
                watcher = new Timeline(new KeyFrame(Duration.millis(interval), ev -> watcherLoop()));
                watcher.setCycleCount(Animation.INDEFINITE);
                watcher.play();
            } else {
                Logger.alertPopup("Make sure to set Playername and Screenshot directory on the Settings tab and import Smurfy's Data on the Mechs Tab");
                toggleAutowatch.setSelected(false);
            }
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
        ScreenshotFileHandler sshandler = null;
        try {
            sshandler = new ScreenshotFileHandler(f);
        } catch (Exception e) {
            Logger.warning("Error identifying screenhot " + f.getName() + ": " + e.getMessage());
        }
        paneWatcherTab.setDisable(false);
        if (sshandler != null) {
            MatchRuntime results = new MatchRuntime(sshandler);
            preliminaryPlayerInfo.clear();
            paneMatchAnalytics.getChildren().clear();
            GuiUtils.prepareGrid(panePersonal, results, StatTable.WATCHER_PERSONAL);
            GuiUtils.prepareGrid(paneMyTeam, results, StatTable.WATCHER_TEAM);
            GuiUtils.prepareGrid(paneEnemyTeam, results, StatTable.WATCHER_ENEMY);
            labelMap.textProperty().bind(results.mapProperty());
            labelGamemode.textProperty().bind(results.gameModeProperty());
            labelMatchResult.textProperty().bind(results.matchResultProperty());
            labelTimestamp.textProperty().bind(results.formattedTimestampProperty());
            //textMatchName.textProperty().bind(results.matchNameProperty());
            if (results.getType() == ScreenshotType.QP_1PREPARATION || results.getType() == ScreenshotType.QP_4SUMMARY) {
                for (int i = 0; i < 24; i++) {
                    Label preliminaryInfo = new Label();
                    GuiUtils.applyPlayerFormat(preliminaryInfo, PlayerRuntime.getReferencePlayer());
                    preliminaryInfo.textProperty().bind(results.getPreliminaryInfo().get(i));
                    if (i < 12) {
                        paneMyTeam.add(preliminaryInfo, 0, 1 + i, GridPane.REMAINING, 1);
                    } else {
                        paneEnemyTeam.add(preliminaryInfo, 0, 1 + i % 12, GridPane.REMAINING, 1);
                    }
                }
                Label preliminaryInfo = new Label("waiting...");
                GuiUtils.applyPlayerFormat(preliminaryInfo, PlayerRuntime.getReferencePlayer());
                panePersonal.add(preliminaryInfo, 0, 1, GridPane.REMAINING, 1);
            }
            results.tracingFinishedProperty().addListener((observable, oldValue, newValue) -> {
                //all players traced. ok to proceed with next screenshot
                if (newValue) {
                    buildPlayerGui(SettingsTabController.getSelfPlayerInstance(), panePersonal, results, StatTable.WATCHER_PERSONAL);
                    for (PlayerRuntime p : results.getPlayersTeam()) {
                        buildPlayerGui(p, paneMyTeam, results, StatTable.WATCHER_TEAM);
                    }
                    for (PlayerRuntime p : results.getPlayersEnemy()) {
                        buildPlayerGui(p, paneEnemyTeam, results, StatTable.WATCHER_ENEMY);
                    }

                    isProcessing = false;
                    Logger.log("tracingfinished listener:" + newValue);
                    paneMatchAnalytics.getChildren().clear();
                    if (SettingsTabController.getInstance().getLayoutShowMatchStatSidebar()) {
                        paneMatchAnalytics.getChildren().add(results.getMatchStatSideBar());
                        Pane spacer = new Pane();
                        spacer.setMaxHeight(Double.MAX_VALUE);
                        VBox.setVgrow(spacer, Priority.ALWAYS);
                        paneMatchAnalytics.getChildren().add(spacer);
                        paneMatchAnalytics.getChildren().add(SessionRuntime.getSessionStatsPane());
                    }
                }
            });
            flashBackground(flashGreen, 1500);
        } else {
            flashBackground(flashRed, 2000);
            isProcessing = false;
        }
    }

    private void buildPlayerGui(PlayerRuntime pr, GridPane parent, MatchRuntime match, StatTable table) {
        try {
            Label preliminaryInfo = preliminaryPlayerInfo.get(pr.getPlayerNumber());
            preliminaryPlayerInfo.remove(pr.getPlayerNumber());
            parent.getChildren().remove(preliminaryInfo);
            int row = 0;
            if (table != StatTable.WATCHER_PERSONAL) {
                row = pr.getPlayerNumber() % 12;
            }
            row++;//account for header
            GuiUtils.addDataToGrid(parent, row, match, pr, table);
        } catch (Exception e) {
            Logger.error(e);
            parent.getChildren().add(new Label("Error adding player '" + pr.getPilotname() + "':" + e.toString()));
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
