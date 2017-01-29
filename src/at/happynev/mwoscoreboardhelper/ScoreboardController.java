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

    }

    public void selectPlayerTab() {
        tabMainApplication.getSelectionModel().select(tabPlayers);
    }
}
