package at.happynev.mwoscoreboardhelper;

import at.happynev.mwoscoreboardhelper.tracer.TraceHelpers;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nev on 15.01.2017.
 */
public class PlayerTabController {
    private static PlayerTabController instance;
    @FXML
    Button buttonRefreshData;
    @FXML
    TableView<PlayerRuntime> tablePlayers;
    @FXML
    TableView<PlayerMatchRecord> tablePlayerMatches;
    @FXML
    TextField textShortNote;
    @FXML
    TextArea textNotes;
    @FXML
    ColorPicker pickerFront;
    @FXML
    ColorPicker pickerBack;
    @FXML
    Label labelUnit;
    @FXML
    Label labelPilotname;
    @FXML
    ListView<PlayerRuntime> listPossibleDuplicates;
    @FXML
    Button buttonMergeDuplicate;
    @FXML
    ChoiceBox<String> choiceIcon;
    @FXML
    TableView<String> tableMechs;
    @FXML
    TableView<String> tableStatistics;
    @FXML
    Pane panePlayername;
    @FXML
    Button buttonJumpToDuplicate;

    public PlayerTabController() {
        instance = this;
    }

    public static PlayerTabController getInstance() {
        if (instance == null) {
            instance = new PlayerTabController();
        }
        return instance;
    }

    @FXML
    private void initialize() {
        buttonRefreshData.setOnAction(event -> refreshData());
        buildPlayerTable();
        BooleanBinding duplicateSelected = listPossibleDuplicates.getSelectionModel().selectedItemProperty().isNotNull();
        buttonMergeDuplicate.disableProperty().bind(duplicateSelected.not());
        buttonMergeDuplicate.setOnAction(event -> mergePlayers(tablePlayers.getSelectionModel().getSelectedItem(), listPossibleDuplicates.getSelectionModel().getSelectedItem()));
        ObjectBinding<Background> backBinding = Bindings.createObjectBinding(() -> {
            BackgroundFill fill = new BackgroundFill(pickerBack.getValue(), CornerRadii.EMPTY, Insets.EMPTY);
            return new Background(fill);
        }, pickerBack.valueProperty());
        buttonJumpToDuplicate.disableProperty().bind(duplicateSelected.not());
        buttonJumpToDuplicate.setOnAction(event -> {
            PlayerRuntime old = tablePlayers.getSelectionModel().getSelectedItem();
            selectPlayer(listPossibleDuplicates.getSelectionModel().getSelectedItem());
            listPossibleDuplicates.getSelectionModel().select(old);
        });
        panePlayername.backgroundProperty().bind(backBinding);
        labelUnit.textFillProperty().bind(pickerFront.valueProperty());
        labelPilotname.textFillProperty().bind(pickerFront.valueProperty());
        listPossibleDuplicates.setCellFactory(param -> {
            return new ListCell<PlayerRuntime>() {
                @Override
                protected void updateItem(PlayerRuntime item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null) {
                        this.setGraphic(new Label(item.getFaction() + " " + item.getPilotname() + "(" + item.getMatchRecords().size() + " Matches)"));
                    } else {
                        this.setGraphic(null);
                    }
                }
            };
        });
        refreshData();
    }

    private void buildPlayerTable() {
        TableColumn<PlayerRuntime, String> colPlayerName = new TableColumn<>("Pilot Name");
        TableColumn<PlayerRuntime, String> colPlayerUnit = new TableColumn<>("Unit");
        TableColumn<PlayerRuntime, String> colPlayerSeen = new TableColumn<>("# Seen");
        colPlayerName.setCellValueFactory(param -> {
            String value = param.getValue().getPilotname();
            return new SimpleStringProperty(value);
        });
        colPlayerUnit.setCellValueFactory(param -> {
            String value = param.getValue().getUnit();
            return new SimpleStringProperty(value);
        });
        colPlayerSeen.setCellValueFactory(param -> {
            return param.getValue().getCalculatedValues().timesSeenProperty();
        });
        colPlayerUnit.prefWidthProperty().bind(tablePlayers.widthProperty().multiply(0.2));
        colPlayerName.prefWidthProperty().bind(tablePlayers.widthProperty().multiply(0.50));
        colPlayerSeen.prefWidthProperty().bind(tablePlayers.widthProperty().multiply(0.25));
        colPlayerUnit.setResizable(false);
        colPlayerName.setResizable(false);
        colPlayerSeen.setResizable(false);
        tablePlayers.getColumns().setAll(colPlayerUnit, colPlayerName, colPlayerSeen);
        tablePlayers.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> selectPlayer(oldValue, newValue));
    }

    private void selectPlayer(PlayerRuntime oldPlayer, PlayerRuntime newPlayer) {
        if (oldPlayer != null) {
            oldPlayer.unitProperty().unbindBidirectional(labelUnit.textProperty());
            oldPlayer.pilotnameProperty().unbindBidirectional(labelPilotname.textProperty());
            oldPlayer.shortnoteProperty().unbindBidirectional(textShortNote.textProperty());
            oldPlayer.notesProperty().unbindBidirectional(textNotes.textProperty());
            oldPlayer.guicolor_backProperty().unbindBidirectional(pickerBack.valueProperty());
            oldPlayer.guicolor_frontProperty().unbindBidirectional(pickerFront.valueProperty());
        }
        labelUnit.textProperty().set("");
        labelPilotname.textProperty().set("");
        textShortNote.textProperty().set("");
        textNotes.textProperty().set("");
        pickerBack.valueProperty().set(Color.BLACK);
        pickerFront.valueProperty().set(Color.WHITE);
        listPossibleDuplicates.getItems().clear();
        tableMechs.setItems(FXCollections.emptyObservableList());
        tableStatistics.setItems(FXCollections.emptyObservableList());
        tablePlayerMatches.setItems(FXCollections.emptyObservableList());
        if (newPlayer != null) {
            labelUnit.textProperty().bindBidirectional(newPlayer.unitProperty());
            labelPilotname.textProperty().bindBidirectional(newPlayer.pilotnameProperty());
            textShortNote.textProperty().bindBidirectional(newPlayer.shortnoteProperty());
            textNotes.textProperty().bindBidirectional(newPlayer.notesProperty());
            pickerBack.valueProperty().bindBidirectional(newPlayer.guicolor_backProperty());
            pickerFront.valueProperty().bindBidirectional(newPlayer.guicolor_frontProperty());
            tablePlayerMatches.setItems(newPlayer.getMatchRecords());
            listPossibleDuplicates.getItems().addAll(findDuplicates(newPlayer));
        }
    }

    private List<PlayerRuntime> findDuplicates(PlayerRuntime orig) {
        List<PlayerRuntime> ret = new ArrayList<>();
        List<String> possibleNames = TraceHelpers.findSimilarLookingStrings(orig.getPilotname(), PlayerRuntime.getAllPlayerNames());//visual
        possibleNames.addAll(TraceHelpers.findSimilarStrings(orig.getPilotname(), PlayerRuntime.getAllPlayerNames(), 3));//levenshtein
        for (String name : possibleNames) {
            if (!name.equals(orig.getPilotname())) {
                PlayerRuntime pr = PlayerRuntime.getInstance(name);
                if (!ret.contains(pr)) ret.add(pr);
            }
        }
        return ret;
    }

    private void mergePlayers(PlayerRuntime orig, PlayerRuntime dup) {
        boolean doIt = Utils.confirmationDialog("Merge Player data", "Really merge " + dup.getPilotname() + " into " + orig.getPilotname() + "?");
        if (doIt) {
            dup.mergeInto(orig);
            refreshData();
        }
    }

    private void refreshData() {
        PlayerRuntime selection = tablePlayers.getSelectionModel().getSelectedItem();
        tablePlayers.getSelectionModel().clearSelection();
        try {
            PreparedStatement prepListPlayers = DbHandler.getInstance().prepareStatement("select distinct pilotname from player_data");
            ResultSet rs = prepListPlayers.executeQuery();
            ObservableList<PlayerRuntime> tmp = FXCollections.observableArrayList();
            while (rs.next()) {
                PlayerRuntime pr = PlayerRuntime.getInstance(rs.getString(1));
                tmp.add(pr);
            }
            rs.close();
            //tablePlayers.setItems(tmp);
            tablePlayers.getItems().clear();
            tablePlayers.getItems().addAll(tmp);
        } catch (Exception e) {
            Logger.error(e);
        }
        tablePlayers.sort();
        if (selection != null) {
            tablePlayers.getSelectionModel().select(selection);
        } else if (tablePlayers.getItems().size() > 0) {
            tablePlayers.getSelectionModel().selectFirst();
        }
    }

    public void selectPlayer(PlayerRuntime pr) {
        ScoreboardController.getInstance().selectPlayerTab();
        tablePlayers.getSelectionModel().select(pr);
    }
}
