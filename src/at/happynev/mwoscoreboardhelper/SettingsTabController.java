package at.happynev.mwoscoreboardhelper;

import at.happynev.mwoscoreboardhelper.tracer.TraceHelpers;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * Created by Nev on 15.01.2017.
 */
public class SettingsTabController {
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
    CheckBox checkShowNote;
    @FXML
    Pane paneColumnSelection;
    @FXML
    GridPane paneColumnPreview;
    @FXML
    Pane paneMatchDataPreview;

    private SimpleObjectProperty<Color> playerBackColor = new SimpleObjectProperty<>(Color.web(DbHandler.getInstance().loadSetting("playerColorBack", "#000000")));
    private SimpleObjectProperty<Color> playerFrontColor = new SimpleObjectProperty<>(Color.web(DbHandler.getInstance().loadSetting("playerColorFront", "#FFFFFF")));

    public SettingsTabController() {
        instance = this;
    }

    public static SettingsTabController getInstance() {
        if (instance == null) {
            instance = new SettingsTabController();
        }
        return instance;
    }

    public static File getScreenshotDirectory() {
        String v = DbHandler.getInstance().loadSetting("screenshotDirectory", Utils.getHomeDir().getAbsolutePath());
        return new File(v).getAbsoluteFile();
    }

    public static File getPostProcessedDirectory() {
        String v = DbHandler.getInstance().loadSetting("postprocessingDirectory", Utils.getHomeDir().getAbsolutePath() + "/archived");
        File d = new File(v);
        if (!d.isDirectory()) {
            d.mkdirs();
        }
        return d.getAbsoluteFile();
    }

    public static File getErrorDirectory() {
        String v = DbHandler.getInstance().loadSetting("errorDirectory", Utils.getHomeDir().getAbsolutePath() + "/error");
        File d = new File(v);
        if (!d.isDirectory()) {
            d.mkdirs();
        }
        return d.getAbsoluteFile();
    }

    public static String getPlayername() {
        return DbHandler.getInstance().loadSetting("playerName", "");
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
        checkDeleteScreenshots.setSelected(Boolean.parseBoolean(DbHandler.getInstance().loadSetting("deleteScreenshots", "true")));
        checkAllowPopups.setSelected(Boolean.parseBoolean(DbHandler.getInstance().loadSetting("allowPopups", "true")));
        sliderPollingInterval.setValue(Double.parseDouble(DbHandler.getInstance().loadSetting("pollingInterval", "500")));
        togglePersistentDatabase.selectedProperty().setValue(DbHandler.getInstance().getWriteEnabled());
        pickerPlayerFront.valueProperty().bindBidirectional(playerFrontColor);
        pickerPlayerBack.valueProperty().bindBidirectional(playerBackColor);
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
        //set changelisteners
        textPlayerName.textProperty().addListener((observable, oldValue, newValue) -> DbHandler.getInstance().saveSetting("playerName", newValue));
        textScreenshotDirectory.textProperty().addListener((observable, oldValue, newValue) -> DbHandler.getInstance().saveSetting("screenshotDirectory", newValue));
        textPostProcessingDirectory.textProperty().addListener((observable, oldValue, newValue) -> DbHandler.getInstance().saveSetting("postprocessingDirectory", newValue));
        textErrorDirectory.textProperty().addListener((observable, oldValue, newValue) -> DbHandler.getInstance().saveSetting("errorDirectory", newValue));
        textPollingInterval.textProperty().addListener((observable, oldValue, newValue) -> DbHandler.getInstance().saveSetting("pollingInterval", newValue));
        checkDeleteScreenshots.selectedProperty().addListener((observable, oldValue, newValue) -> DbHandler.getInstance().saveSetting("deleteScreenshots", "" + newValue));
        checkAllowPopups.selectedProperty().addListener((observable, oldValue, newValue) -> DbHandler.getInstance().saveSetting("allowPopups", "" + newValue));
        togglePersistentDatabase.selectedProperty().bindBidirectional(DbHandler.getInstance().writeEnabledProperty());
        playerFrontColor.addListener((observable, oldValue, newValue) -> {
            DbHandler.getInstance().saveSetting("playerColorFront", Utils.getWebColor(newValue));
            PlayerRuntime.getInstance(getPlayername()).refreshDataFromDb();
        });
        playerBackColor.addListener((observable, oldValue, newValue) -> {
            DbHandler.getInstance().saveSetting("playerColorBack", Utils.getWebColor(newValue));
            PlayerRuntime.getInstance(getPlayername()).refreshDataFromDb();
        });
        WatcherTabController.getInstance().setSettingsLoaded(true);
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
        if (current.isDirectory()) {
            dc.setInitialDirectory(current);
        }
        File chosen = dc.showDialog(null);
        if (chosen != null && chosen.isDirectory()) {
            textfield.setText(chosen.toString());
        }
    }
}
