package at.happynev.mwoscoreboardhelper;

import at.happynev.mwoscoreboardhelper.stat.CustomizableStatTemplate;
import at.happynev.mwoscoreboardhelper.stat.StatBuilder;
import at.happynev.mwoscoreboardhelper.stat.StatTable;
import at.happynev.mwoscoreboardhelper.tracer.ScreenshotType;
import at.happynev.mwoscoreboardhelper.tracer.TraceHelpers;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nev on 15.01.2017.
 */
public class SettingsTabController {
    public final static String EMPTY = "~empty~";
    private static final Map<String, Boolean> statsToDisplay = new HashMap<>();
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
    CheckBox checkShowStatSummary;
    @FXML
    Pane paneStatColumnSelection;
    @FXML
    Pane paneStatColumnPreview;

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

    public static void saveActiveTab(String tab) {
        saveSetting("activeTab", tab);
    }

    public static String getActiveTab() {
        return loadSetting("activeTab", "tabSettings");
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
        sliderFontSize.setValue(Double.parseDouble(loadSetting("fontSize", "16")));
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
        GridPane grid = new GridPane();
        int textColumnOffset = StatTable.values().length;
        int col = 0;
        int row = 0;
        for (StatTable table : StatTable.values()) {
            Label labelTable = new Label(table.toString());
            labelTable.setRotate(90);
            grid.add(new Group(labelTable), col++, row);
        }
        row++;
        for (ScreenshotType sstype : Arrays.asList(ScreenshotType.QP_1PREPARATION, ScreenshotType.QP_4SUMMARY)) {
            Label labelSsType = new Label("On " + sstype.toString() + ":");
            grid.add(labelSsType, 0, row++, GridPane.REMAINING, 1);
            for (CustomizableStatTemplate stat : StatBuilder.getDefaultStats()) {
                col = 0;
                for (StatTable table : StatTable.values()) {
                    CheckBox check = new CheckBox();
                    boolean canDisplay = stat.canDisplay(sstype, table);
                    check.setDisable(!canDisplay);
                    if (canDisplay) {
                        boolean selected = getShouldDisplay(sstype, stat, table);
                        check.setSelected(selected);
                    } else {
                        check.setSelected(false);
                    }
                    check.setTooltip(new Tooltip(stat.getLongName() + " on " + table.toString()));
                    check.selectedProperty().addListener((observable, oldValue, newValue) -> changeStatDisplay(sstype, stat, table, newValue));
                    grid.add(check, col++, row);
                }
                Label statTitle = new Label(stat.getShortName());
                grid.add(statTitle, col++, row);
                Label statDesc = new Label(stat.getLongName());
                grid.add(statDesc, col++, row);
                row++;
            }
        }
        paneStatColumnSelection.getChildren().add(grid);
        refreshPreviews();
        WatcherTabController.getInstance().setSettingsLoaded(true);
    }

    private void changeStatDisplay(ScreenshotType sstype, CustomizableStatTemplate stat, StatTable table, boolean newValue) {
        String key = sstype.toString() + stat.getShortName() + table.toString();
        statsToDisplay.put(key, newValue);
        saveSetting("statdisplay_" + key, "" + newValue);
        refreshPreviews();
    }

    public boolean getShouldDisplay(ScreenshotType sstype, CustomizableStatTemplate stat, StatTable table) {
        String key = sstype.toString() + stat.getShortName() + table.toString();
        if (statsToDisplay.containsKey(key)) {
            return statsToDisplay.get(key);
        } else {
            boolean val = Boolean.parseBoolean(loadSetting("statdisplay_" + key, "false"));
            statsToDisplay.put(key, val);
            return val;
        }
    }

    private void refreshPreviews() {
        PlayerRuntime player = PlayerRuntime.getReferencePlayer();
        paneStatColumnPreview.getChildren().clear();
        for (ScreenshotType sstype : Arrays.asList(ScreenshotType.QP_1PREPARATION, ScreenshotType.QP_4SUMMARY)) {
            MatchRuntime match = MatchRuntime.getReferenceMatch(sstype);
            Label labelSsType = new Label("Preview for " + sstype.toString());
            paneStatColumnPreview.getChildren().add(labelSsType);
            for (StatTable table : StatTable.values()) {
                GridPane pane = new GridPane();
                Label labelTable = new Label("    Table " + table.toString());
                paneStatColumnPreview.getChildren().add(labelTable);
                GuiUtils.prepareGrid(pane, match, table);
                GuiUtils.addDataToGrid(pane, 1, match, player, table);
                paneStatColumnPreview.getChildren().add(pane);
                paneStatColumnPreview.getChildren().add(new Label(" "));//buffer
            }
        }

        if (getLayoutShowStatSummary()) {
            //TODO
            //paneMatchDataPrepPreview.getChildren().add(matchPrep.getMatchAnalyticsPane());
            //paneMatchDataPreview.getChildren().add(matchSummary.getMatchAnalyticsPane());
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
