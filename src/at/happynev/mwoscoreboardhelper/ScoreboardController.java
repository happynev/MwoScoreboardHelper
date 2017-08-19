package at.happynev.mwoscoreboardhelper;

import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class ScoreboardController {
    private static ScoreboardController instance;
    @FXML
    TabPane tabMainApplication;
    @FXML
    Tab tabPlayers;
    @FXML
    Tab tabWatcher;
    @FXML
    Tab tabMatches;
    @FXML
    Tab tabSettings;
    @FXML
    Tab tabMechs;
    @FXML
    Tab tabPreview;
    @FXML
    Tab tabGraphs;
    @FXML
    Tab tabPersonal;

    public ScoreboardController() {
        instance = this;
    }

    public static ScoreboardController getInstance() {
        if (instance == null) {
            instance = new ScoreboardController();
        }
        return instance;
    }

    @FXML
    private void initialize() {
        if (!Main.isDebug()) {
            tabMainApplication.getTabs().removeAll(tabGraphs, tabPersonal);
        }
        String activeTab = SettingsTabController.getActiveTab();
        for (Tab t : tabMainApplication.getTabs()) {
            if (t.getId().equals(activeTab)) {
                selectTab(t);
                break;
            }
        }
        tabMainApplication.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> SettingsTabController.saveActiveTab(newValue.getId()));
    }

    private void selectTab(Tab t) {
        tabMainApplication.getSelectionModel().select(t);
    }

    public void selectPlayerTab() {
        selectTab(tabPlayers);
    }
}
