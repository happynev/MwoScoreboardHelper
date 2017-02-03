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
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

/**
 * Created by Nev on 15.01.2017.
 */
public class WatcherTabController {
    public static final String colorBack = "#A0A0A0";
    public static final String colorTeam = "#0E8DFE";
    public static final String colorEnemy = "#D30000";
    public static final String colorNeutral = "#EDBE34";
    public static final String styleTeam = "-fx-text-fill: " + colorTeam;// + "; -fx-background-color: " + colorBack + ";";
    public static final String styleEnemy = "-fx-text-fill: " + colorEnemy;// + "; -fx-background-color: " + colorBack + ";";
    public static final String styleNeutral = "-fx-text-fill: " + colorNeutral;// + "; -fx-background-color: " + colorBack + ";";
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

    public static void prepareGrid(GridPane grid) {
        grid.getChildren().clear();
        grid.getColumnConstraints().clear();
        PlayerRuntime pr = PlayerRuntime.getReferencePlayer();
        int col = 0;
        Label labelUnit = applyHeaderFormat(new Label("Unit"));
        grid.getColumnConstraints().add(Utils.getColumnConstraint(labelUnit));
        grid.add(labelUnit, col++, 0);

        Label labelPilotname = applyHeaderFormat(new Label("Pilot Name"));
        grid.getColumnConstraints().add(Utils.getColumnConstraint(labelPilotname));
        grid.add(labelPilotname, col++, 0);

        Label labelShortnote = applyHeaderFormat(new Label("Short Note"));
        grid.getColumnConstraints().add(Utils.getColumnConstraint(labelShortnote));
        grid.add(labelShortnote, col++, 0);

        for (Stat key : pr.getCalculatedValues().keySet()) {
            Label label = applyHeaderFormat(new Label(key.toString()));
            grid.getColumnConstraints().add(Utils.getColumnConstraint(label));
            grid.add(label, col++, 0);
        }
    }

    private static Label applyHeaderFormat(Label node) {
        Font fontHeader = Font.font("System", FontWeight.BOLD, 22);
        node.setFont(fontHeader);
        node.setTextFill(Color.WHITE);
        node.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
        GridPane.setFillWidth(node, true);
        node.setMaxWidth(Double.MAX_VALUE);
        return node;
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
        boolean watcherActive = Boolean.parseBoolean(DbHandler.getInstance().loadSetting("autowatchEnabled", "false"));
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
                DbHandler.getInstance().saveSetting("autowatchEnabled", "" + newValue);
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
            prepareGrid(paneMyTeam);
            prepareGrid(paneEnemyTeam);
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
            pr.addPlayerDataToGrid(parent, row);
            playersFinished++;
        } catch (Exception e) {
            Logger.error(e);
            paneMyTeam.getChildren().add(new Label("Error adding player '" + pr.getPilotname() + "':" + e.toString()));
        }
        if (playersFinished == 24) {
            //all players traced. ok to procees with next screenshot
            isProcessing = false;
            Logger.log("Tracing finished");
            buildMatchAnalyticsGui();
        }
    }

    private void buildMatchAnalyticsGui() {
        Font fontHeader = Font.font("System", FontWeight.BOLD, 20);

        if (currentMatch.isMatchFinished()) {
            Label labelTeam = new Label("Your Team");
            labelTeam.setStyle(styleTeam);
            labelTeam.setFont(fontHeader);
            labelTeam.setPadding(PlayerRuntime.DATA_INSETS);
            labelTeam.setRotate(90);
            //
            Label labelEnemy = new Label("Your Enemy");
            labelEnemy.setStyle(styleEnemy);
            labelEnemy.setFont(fontHeader);
            labelEnemy.setPadding(PlayerRuntime.DATA_INSETS);
            labelEnemy.setRotate(90);
            //
            Label labelTotal = new Label("Total / Avg");
            labelTotal.setStyle(styleNeutral);
            labelTotal.setFont(fontHeader);
            labelTotal.setPadding(PlayerRuntime.DATA_INSETS);
            labelTotal.setRotate(90);
            //
            Label labelDummy = new Label("");
            labelDummy.setStyle(styleNeutral);
            labelDummy.setFont(fontHeader);
            labelDummy.setPadding(PlayerRuntime.DATA_INSETS);
            //
            VBox columnTitles = new VBox();
            VBox columnTeam = new VBox();
            VBox columnEnemy = new VBox();
            VBox columnTotal = new VBox();
            columnTitles.getChildren().add(labelDummy);
            columnTeam.getChildren().add(new Group(labelTeam));
            columnEnemy.getChildren().add(new Group(labelEnemy));
            columnTotal.getChildren().add(new Group(labelTotal));
            GridPane grid = new GridPane();
            int line = 0;
            grid.add(new Group(labelTeam), 1, 0);
            grid.add(new Group(labelEnemy), 2, 0);
            grid.add(new Group(labelTotal), 3, 0);
            line++;
            List<PlayerMatchRecord> pmrTeam = new ArrayList<>(12);
            List<PlayerMatchRecord> pmrEnemy = new ArrayList<>(12);
            for (PlayerRuntime pr : currentMatch.getPlayersTeam()) {
                PlayerMatchRecord pmr = pr.getMatchRecord(currentMatch);
                if (pmr != null) {
                    pmrTeam.add(pmr);
                }
            }
            for (PlayerRuntime pr : currentMatch.getPlayersEnemy()) {
                PlayerMatchRecord pmr = pr.getMatchRecord(currentMatch);
                if (pmr != null) {
                    pmrEnemy.add(pmr);
                }
            }
            MatchCalculatedValue damageDealt = new MatchCalculatedValue() {
                @Override
                public void calculate() {
                    int team = 0;
                    int enemy = 0;
                    for (PlayerMatchRecord pmr : pmrTeam) {
                        team += pmr.getDamage();
                    }
                    for (PlayerMatchRecord pmr : pmrEnemy) {
                        enemy += pmr.getDamage();
                    }
                    teamValue = "" + team;
                    enemyValue = "" + enemy;
                    totalValue = "" + (team + enemy);
                }
            };
            MatchCalculatedValue score = new MatchCalculatedValue() {
                @Override
                public void calculate() {
                    int team = 0;
                    int enemy = 0;
                    for (PlayerMatchRecord pmr : pmrTeam) {
                        team += pmr.getMatchScore();
                    }
                    for (PlayerMatchRecord pmr : pmrEnemy) {
                        enemy += pmr.getMatchScore();
                    }
                    teamValue = "" + team;
                    enemyValue = "" + enemy;
                    totalValue = "" + (team + enemy);
                }
            };
            buildDataLine(grid, line++, "Total Score", score);
            //buildDataLine(grid, line++, "Median Score", score);
            buildDataLine(grid, line++, "Total Damage", damageDealt);
            //buildDataLine(grid, line++, "Median Damage", score);
            //buildDataLine(grid, line++, "Net Weight", score);
            //buildDataLine(grid, line++, "Avg. Weight", score);
            //buildDataLine(grid, line++, "Score/Ton", score);

            paneMatchAnalytics.getChildren().add(grid);
        } else {

        }
    }

    private void buildDataLine(GridPane grid, int row, String title, MatchCalculatedValue mc) {
        Font fontData = Font.font("System", FontWeight.BOLD, 15);
        Label labelTitle = new Label(title);
        labelTitle.setFont(fontData);
        labelTitle.setStyle(styleNeutral);
        labelTitle.setPadding(PlayerRuntime.DATA_INSETS);
        //labelTitle.setRotate(45);
        //
        Label labelTeam = new Label(mc.teamValue);
        labelTeam.setFont(fontData);
        labelTeam.setStyle(styleTeam);
        labelTeam.setPadding(PlayerRuntime.DATA_INSETS);
        //
        Label labelEnemy = new Label(mc.enemyValue);
        labelEnemy.setFont(fontData);
        labelEnemy.setStyle(styleEnemy);
        labelEnemy.setPadding(PlayerRuntime.DATA_INSETS);
        //
        Label labelTotal = new Label(mc.totalValue);
        labelTotal.setFont(fontData);
        labelTotal.setStyle(styleNeutral);
        labelTotal.setPadding(PlayerRuntime.DATA_INSETS);
        //
        grid.add(labelTitle, 0, row);
        grid.add(labelTeam, 1, row);
        grid.add(labelEnemy, 2, row);
        grid.add(labelTotal, 3, row);
    }

    private void flashBackground(Color flashTo, int duration) {
        KeyFrame flashframe = new KeyFrame(new Duration(0), new KeyValue(backgroundColor, flashTo));
        KeyFrame endframe = new KeyFrame(new Duration(duration), new KeyValue(backgroundColor, defaultBackground));
        Timeline flash = new Timeline(flashframe, endframe);
        flash.setCycleCount(1);
        flash.play();
    }

    private abstract class MatchCalculatedValue {
        String teamValue = "";
        String enemyValue = "";
        String totalValue = "";

        public MatchCalculatedValue() {
            calculate();
        }

        public abstract void calculate();
    }
}
