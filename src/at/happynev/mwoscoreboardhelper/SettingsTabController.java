package at.happynev.mwoscoreboardhelper;

import at.happynev.mwoscoreboardhelper.tracer.ScreenshotType;
import at.happynev.mwoscoreboardhelper.tracer.TraceHelpers;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Nev on 15.01.2017.
 */
public class SettingsTabController {
    public final static String EMPTY = "~empty~";
    private static SettingsTabController instance;
    @FXML
    TextField textPlayerName;
    @FXML
    TextField textScreenshotDirectory;
    @FXML
    TextField textPostProcessingDirectory;
    @FXML
    TextField textErrorDirectory;
    @FXML
    Button buttonSelectScreenshotDir;
    @FXML
    Button buttonSelectPostProcessingDir;
    @FXML
    Button buttonSelectErrorDir;
    @FXML
    Button buttonFixOldData;
    @FXML
    CheckBox checkDeleteScreenshots;
    @FXML
    Slider sliderPollingInterval;
    @FXML
    Slider sliderFontSize;
    @FXML
    TextField textPollingInterval;
    @FXML
    ToggleButton togglePersistentDatabase;
    @FXML
    ColorPicker pickerPlayerBack;
    @FXML
    ColorPicker pickerPlayerFront;
    @FXML
    CheckBox checkAllowPopups;
    @FXML
    CheckBox checkShowUnit;
    @FXML
    CheckBox checkShowName;
    @FXML
    CheckBox checkShowNote;
    @FXML
    Pane paneColumnSelectionPrepPlayer;
    @FXML
    Pane paneColumnSelectionSummaryMatch;
    @FXML
    GridPane paneColumnPreviewPrep;
    @FXML
    GridPane paneColumnPreviewSummary;
    @FXML
    Pane paneMatchDataPrepPreview;
    @FXML
    Pane paneMatchDataPreview;
    @FXML
    CheckBox checkShowStatSummary;
    @FXML
    Label labelColorPlayerData;
    @FXML
    Label labelColorMatchData;
    @FXML
    Label labelColorPlayerMatchData;

    private SimpleObjectProperty<Color> playerBackColor = new SimpleObjectProperty<>(Color.web(loadSetting("playerColorBack", "#000000")));
    private SimpleObjectProperty<Color> playerFrontColor = new SimpleObjectProperty<>(Color.web(loadSetting("playerColorFront", "#FFFFFF")));

    public SettingsTabController() {
        instance = this;
    }

    public static SettingsTabController getInstance() {
        if (instance == null) {
            instance = new SettingsTabController();
        }
        return instance;
    }

    private static void saveSetting(String key, String value) {
        if (value == null || value.isEmpty()) {
            value = EMPTY;
        }
        Logger.log("save setting " + key + "=" + value);
        try {
            PreparedStatement clean = DbHandler.getInstance().prepareStatement("delete from SETTINGS where propKey=?");
            clean.setString(1, key);
            clean.executeUpdate();
            PreparedStatement insert = DbHandler.getInstance().prepareStatement("insert into SETTINGS(propValue,propKey) values(?,?)");
            insert.setString(1, value);
            insert.setString(2, key);
            insert.executeUpdate();
        } catch (SQLException e) {
            Logger.error(e);
        }
    }

    private static String loadSetting(String key, String defaultValue) {
        String ret = defaultValue;
        try {
            PreparedStatement prep = DbHandler.getInstance().prepareStatement("select propValue from SETTINGS where propKey=?");
            prep.setString(1, key);
            ResultSet rs = prep.executeQuery();

            if (rs.next()) {
                ret = rs.getString(1);
                if (ret.equals(EMPTY)) {
                    ret = "";
                }
            }
            rs.close();
        } catch (SQLException e) {
            Logger.dberror(e);
        }
        return ret;
    }

    public static File getScreenshotDirectory() {
        String v = loadSetting("screenshotDirectory", Utils.getHomeDir().getAbsolutePath());
        return new File(v).getAbsoluteFile();
    }

    public static File getPostProcessedDirectory() {
        String v = loadSetting("postprocessingDirectory", Utils.getHomeDir().getAbsolutePath() + "/archived");
        File d = new File(v);
        if (!d.isDirectory()) {
            d.mkdirs();
        }
        return d.getAbsoluteFile();
    }

    public static File getErrorDirectory() {
        String v = loadSetting("errorDirectory", Utils.getHomeDir().getAbsolutePath() + "/error");
        File d = new File(v);
        if (!d.isDirectory()) {
            d.mkdirs();
        }
        return d.getAbsoluteFile();
    }

    public static String getPlayername() {
        return loadSetting("playerName", "");
    }

    public static boolean isDbWriteEnabled() {
        return Boolean.parseBoolean(loadSetting("dbWriteEnabled", "true"));
    }

    public static void setDbWriteEnabled(boolean enabled) {
        saveSetting("dbWriteEnabled", "" + enabled);
    }

    public static int getVersion() {
        return Integer.parseInt(loadSetting("version", "0").replaceAll("\\..*", ""));
    }

    public static void setVersion(int version) {
        saveSetting("version", "" + version);
    }

    public static boolean isAutowatchEnabled() {
        return Boolean.parseBoolean(loadSetting("autowatchEnabled", "false"));
    }

    public static void setAutowatchEnabled(boolean enabled) {
        saveSetting("autowatchEnabled", "" + enabled);
    }

    public static boolean isDeleteScreenshots() {
        return Boolean.parseBoolean(loadSetting("deleteScreenshots", "true"));
    }

    public static void restoreWindowPos(Stage primaryStage) {
        double sceneX = Double.parseDouble(loadSetting("sceneX", "20"));
        double sceneY = Double.parseDouble(loadSetting("sceneY", "20"));
        double sceneWidth = Double.parseDouble(loadSetting("sceneWidth", "1500"));
        double sceneHeight = Double.parseDouble(loadSetting("sceneHeight", "900"));
        boolean sceneMaximized = Boolean.parseBoolean(loadSetting("sceneMaximized", "false"));
        primaryStage.setX(sceneX);
        primaryStage.setY(sceneY);
        primaryStage.setWidth(sceneWidth);
        primaryStage.setHeight(sceneHeight);
        primaryStage.setMaximized(sceneMaximized);
    }

    public static void saveWindowPos(Stage primaryStage) {
        saveSetting("sceneX", "" + primaryStage.getX());
        saveSetting("sceneY", "" + primaryStage.getY());
        saveSetting("sceneWidth", "" + primaryStage.getWidth());
        saveSetting("sceneHeight", "" + primaryStage.getHeight());
        saveSetting("sceneMaximized", "" + primaryStage.isMaximized());
    }

    public TextField getTextPlayerName() {
        return textPlayerName;
    }

    public CheckBox getCheckDeleteScreenshots() {
        return checkDeleteScreenshots;
    }

    public TextField getTextPollingInterval() {
        return textPollingInterval;
    }

    public Color getPlayerBackColor() {
        return playerBackColor.get();
    }

    public SimpleObjectProperty<Color> playerBackColorProperty() {
        return playerBackColor;
    }

    public Color getPlayerFrontColor() {
        return playerFrontColor.get();
    }

    public SimpleObjectProperty<Color> playerFrontColorProperty() {
        return playerFrontColor;
    }

    @FXML
    private void initialize() {
        //load values
        textPlayerName.setText(getPlayername());
        textScreenshotDirectory.setText(getScreenshotDirectory().toString());
        textPostProcessingDirectory.setText(getPostProcessedDirectory().toString());
        textErrorDirectory.setText(getErrorDirectory().toString());
        checkDeleteScreenshots.setSelected(Boolean.parseBoolean(loadSetting("deleteScreenshots", "true")));
        checkAllowPopups.setSelected(Boolean.parseBoolean(loadSetting("allowPopups", "true")));
        sliderPollingInterval.setValue(Double.parseDouble(loadSetting("pollingInterval", "500")));
        togglePersistentDatabase.selectedProperty().setValue(DbHandler.getInstance().getWriteEnabled());
        pickerPlayerFront.valueProperty().bindBidirectional(playerFrontColor);
        pickerPlayerBack.valueProperty().bindBidirectional(playerBackColor);
        checkShowUnit.setSelected(Boolean.parseBoolean(loadSetting("layoutShowUnit", "true")));
        checkShowName.setSelected(Boolean.parseBoolean(loadSetting("layoutShowName", "true")));
        checkShowNote.setSelected(Boolean.parseBoolean(loadSetting("layoutShowNote", "true")));
        checkShowStatSummary.setSelected(Boolean.parseBoolean(loadSetting("layoutShowStatSummary", "true")));
        //set actions
        textPollingInterval.textProperty().bind(StringExpression.stringExpression(sliderPollingInterval.valueProperty()));
        buttonSelectScreenshotDir.setOnAction(event -> selectDirectory(textScreenshotDirectory));
        buttonSelectPostProcessingDir.setOnAction(event -> selectDirectory(textPostProcessingDirectory));
        buttonSelectErrorDir.setOnAction(event -> selectDirectory(textErrorDirectory));
        buttonFixOldData.setOnAction(event -> {
            int mechs = fixMechReferences();
            int status = fixPlayerStatus();
            Logger.infoPopup("Fixed " + mechs + " Mech references and " + status + " Player Status records");
        });
        sliderFontSize.setValue(Double.parseDouble(loadSetting("fontSize", "18")));
        //set changelisteners
        textPlayerName.textProperty().addListener((observable, oldValue, newValue) -> saveSetting("playerName", newValue));
        textScreenshotDirectory.textProperty().addListener((observable, oldValue, newValue) -> saveSetting("screenshotDirectory", newValue));
        textPostProcessingDirectory.textProperty().addListener((observable, oldValue, newValue) -> saveSetting("postprocessingDirectory", newValue));
        textErrorDirectory.textProperty().addListener((observable, oldValue, newValue) -> saveSetting("errorDirectory", newValue));
        textPollingInterval.textProperty().addListener((observable, oldValue, newValue) -> saveSetting("pollingInterval", newValue));
        checkDeleteScreenshots.selectedProperty().addListener((observable, oldValue, newValue) -> saveSetting("deleteScreenshots", "" + newValue));
        checkAllowPopups.selectedProperty().addListener((observable, oldValue, newValue) -> saveSetting("allowPopups", "" + newValue));
        sliderFontSize.valueProperty().addListener((observable, oldValue, newValue) -> {
            saveSetting("fontSize", "" + newValue.intValue());
            refreshPreviews();
        });
        checkShowUnit.selectedProperty().addListener((observable, oldValue, newValue) -> {
            saveSetting("layoutShowUnit", "" + newValue);
            refreshPreviews();
        });
        checkShowName.selectedProperty().addListener((observable, oldValue, newValue) -> {
            saveSetting("layoutShowName", "" + newValue);
            refreshPreviews();
        });
        checkShowNote.selectedProperty().addListener((observable, oldValue, newValue) -> {
            saveSetting("layoutShowNote", "" + newValue);
            refreshPreviews();
        });
        checkShowStatSummary.selectedProperty().addListener((observable, oldValue, newValue) -> {
            saveSetting("layoutShowStatSummary", "" + newValue);
            refreshPreviews();
        });
        togglePersistentDatabase.selectedProperty().bindBidirectional(DbHandler.getInstance().writeEnabledProperty());
        playerFrontColor.addListener((observable, oldValue, newValue) -> {
            saveSetting("playerColorFront", Utils.getWebColor(newValue));
            PlayerRuntime.getInstance(getPlayername()).guicolor_frontProperty().set(newValue);
        });
        playerBackColor.addListener((observable, oldValue, newValue) -> {
            saveSetting("playerColorBack", Utils.getWebColor(newValue));
            PlayerRuntime.getInstance(getPlayername()).guicolor_backProperty().set(newValue);
        });
        //build dynamic part
        labelColorMatchData.setBackground(new Background(new BackgroundFill(DisplayableStat.COLOR_MATCHDATA, null, null)));
        labelColorPlayerData.setBackground(new Background(new BackgroundFill(DisplayableStat.COLOR_PLAYERDATA, null, null)));
        labelColorPlayerMatchData.setBackground(new Background(new BackgroundFill(DisplayableStat.COLOR_PLAYERMATCHDATA, null, null)));
        List<DisplayableStat> allStats = new ArrayList<>();
        allStats.addAll(Arrays.asList(MatchStat.class.getEnumConstants()));
        allStats.addAll(Arrays.asList(PlayerStat.class.getEnumConstants()));
        addStatSettings(allStats, ScreenshotType.QP_1PREPARATION, paneColumnSelectionPrepPlayer);
        addStatSettings(allStats, ScreenshotType.QP_4SUMMARY, paneColumnSelectionSummaryMatch);
        refreshPreviews();
        WatcherTabController.getInstance().setSettingsLoaded(true);
    }

    private void addStatSettings(List<DisplayableStat> statsForPrep, ScreenshotType type, Pane pane) {
        for (DisplayableStat stat : statsForPrep) {
            if (stat.canDisplay(type)) {
                String desc = stat.getDescription();
                String name = stat.toString();
                String checktitle = name;
                String checkid = "layout" + type + "-" + name;
                if (!name.equals(desc)) {
                    checktitle = desc + " ('" + name + "')";
                }
                CheckBox check = new CheckBox(checktitle);
                check.setBackground(new Background(new BackgroundFill(stat.getColor(), null, null)));
                check.setSelected(Boolean.parseBoolean(loadSetting(checkid, "false")));
                check.selectedProperty().addListener((observable, oldValue, newValue) -> {
                    saveSetting(checkid, "" + newValue);
                    refreshPreviews();
                });
                pane.getChildren().add(check);
            }
        }
    }

    private void refreshPreviews() {
        MatchRuntime matchPrep = MatchRuntime.getReferenceMatch(ScreenshotType.QP_1PREPARATION);
        MatchRuntime matchSummary = MatchRuntime.getReferenceMatch(ScreenshotType.QP_4SUMMARY);
        PlayerRuntime player = PlayerRuntime.getReferencePlayer();
        GuiUtils.prepareGrid(paneColumnPreviewPrep, matchPrep);
        GuiUtils.prepareGrid(paneColumnPreviewSummary, matchSummary);
        player.addDataToGrid(paneColumnPreviewPrep, 1, matchPrep);
        player.addDataToGrid(paneColumnPreviewSummary, 1, matchSummary);
        paneMatchDataPrepPreview.getChildren().clear();
        paneMatchDataPreview.getChildren().clear();
        if (getLayoutShowStatSummary()) {
            paneMatchDataPrepPreview.getChildren().add(matchPrep.getMatchAnalyticsPane());
            paneMatchDataPreview.getChildren().add(matchSummary.getMatchAnalyticsPane());
        }
    }

    public boolean getLayoutShowStatSummary() {
        return checkShowStatSummary.isSelected();
    }

    public boolean getLayoutShowName() {
        return checkShowName.isSelected();
    }

    public boolean getLayoutShowUnit() {
        return checkShowUnit.isSelected();
    }

    public boolean getLayoutShowNote() {
        return checkShowNote.isSelected();
    }

    public List<DisplayableStat> getStatsToDisplay(ScreenshotType type) {
        List<DisplayableStat> allStats = new ArrayList<>();
        allStats.addAll(Arrays.asList(MatchStat.class.getEnumConstants()));
        allStats.addAll(Arrays.asList(PlayerStat.class.getEnumConstants()));
        List<DisplayableStat> ret = new ArrayList<>();
        for (DisplayableStat stat : allStats) {
            String checkid = "layout" + type + "-" + stat;
            boolean showStat = Boolean.parseBoolean(loadSetting(checkid, "false"));
            if (showStat) {
                ret.add(stat);
            }
        }
        return ret;
    }

    private int fixMechReferences() {
        int fixed = 0;
        try {
            PreparedStatement prepFind = DbHandler.getInstance().prepareStatement("select distinct mech from player_matchdata where mech not in (select short_name from mech_data)");
            PreparedStatement prepFix = DbHandler.getInstance().prepareStatement("update player_matchdata set mech=? where mech=?");
            ResultSet rs = prepFind.executeQuery();
            while (rs.next()) {
                String mech = rs.getString(1);
                if (mech != null && !mech.isEmpty()) {
                    String fixedMech = MechRuntime.findMatchingMech(mech);
                    if (!mech.equals(fixedMech)) {
                        prepFix.clearParameters();
                        prepFix.setString(1, fixedMech);
                        prepFix.setString(2, mech);
                        prepFix.addBatch();
                        Logger.log(mech + "-->" + fixedMech);
                    }
                }
            }
            int[] parts = prepFix.executeBatch();
            for (int i : parts) fixed += i;
            rs.close();
        } catch (SQLException e) {
            Logger.error(e);
        }
        return fixed;
    }

    public boolean popupsAllowed() {
        if (checkAllowPopups == null) return true;
        return checkAllowPopups.isSelected();
    }

    private int fixPlayerStatus() {
        int fixed = 0;
        try {
            PreparedStatement prepFind = DbHandler.getInstance().prepareStatement("select distinct status from player_matchdata where status not in ('ALIVE','DEAD')");
            PreparedStatement prepFix = DbHandler.getInstance().prepareStatement("update player_matchdata set status=? where status=?");
            ResultSet rs = prepFind.executeQuery();
            while (rs.next()) {
                String status = rs.getString(1);
                if (status != null && !status.isEmpty()) {
                    String fixedStatus = TraceHelpers.guessValue(status.replaceAll(".*DEAD.*", "DEAD").replaceAll(".*ALIVE.*", "ALIVE"), Arrays.asList("DEAD", "ALIVE"));
                    if (!status.equals(fixedStatus)) {
                        prepFix.clearParameters();
                        prepFix.setString(1, fixedStatus);
                        prepFix.setString(2, status);
                        prepFix.addBatch();
                        Logger.log(status + "-->" + fixedStatus);
                    }
                }
            }
            int[] parts = prepFix.executeBatch();
            for (int i : parts) fixed += i;
            rs.close();
        } catch (SQLException e) {
            Logger.error(e);
        }
        return fixed;
    }

    private void selectDirectory(TextField textfield) {
        DirectoryChooser dc = new DirectoryChooser();
        File current = new File(textfield.getText());
        if (textfield.getText().isEmpty()) {
            current = Utils.getInstallDir();
        }
        if (current.isDirectory()) {
            dc.setInitialDirectory(current);
        }
        File chosen = dc.showDialog(null);
        if (chosen != null && chosen.isDirectory()) {
            textfield.setText(chosen.toString());
        }
    }

    public int getFontSize() {
        return (int) sliderFontSize.getValue();
    }
}
