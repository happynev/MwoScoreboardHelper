package at.happynev.mwoscoreboardhelper;

import at.happynev.mwoscoreboardhelper.stat.StatType;
import at.happynev.mwoscoreboardhelper.tracer.TraceHelpers;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.apache.commons.lang3.time.FastDateFormat;

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
    Label labelSeenInfo;
    @FXML
    ListView<PlayerRuntime> listPossibleDuplicates;
    @FXML
    Button buttonMergeDuplicate;
    @FXML
    ChoiceBox<String> choiceIcon;
    @FXML
    TableView<String> tableMechs;
    @FXML
    Pane panePlayername;
    @FXML
    Button buttonJumpToDuplicate;
    @FXML
    Button buttonJumpToMatch;
    @FXML
    Pane panePlayerstats;
    @FXML
    Button buttonJumpToSelf;
    @FXML
    TextField textPlayerFilter;
    @FXML
    Button buttonClearPlayerFilter;

    FastDateFormat fdfSeen = FastDateFormat.getInstance("yyyy-MM-dd HH:mm");

    public PlayerTabController() {
        instance = this;
    }

    public static PlayerTabController getInstance() {
        if (instance == null) {
            instance = new PlayerTabController();
        }
        return instance;
    }

    private static long findFirstSeen(PlayerRuntime pr) {
        long first = Long.MAX_VALUE;
        for (PlayerMatchRecord pmr : pr.getMatchRecords()) {
            if (pmr.getTimestamp() < first) {
                first = pmr.getTimestamp();
            }
        }
        return first;
    }

    private static long findLastSeen(PlayerRuntime pr) {
        long last = 0;
        for (PlayerMatchRecord pmr : pr.getMatchRecords()) {
            if (pmr.getTimestamp() > last) {
                last = pmr.getTimestamp();
            }
        }
        return last;
    }

    @FXML
    private void initialize() {
        //tablePlayerMatches.setItems(FXCollections.observableArrayList());
        //tableMechs.setItems(FXCollections.observableArrayList());
        tablePlayerMatches.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableMechs.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
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
            selectPlayerFromList(listPossibleDuplicates.getSelectionModel().getSelectedItem());
            listPossibleDuplicates.getSelectionModel().select(old);
        });
        panePlayername.backgroundProperty().bind(backBinding);
        buttonClearPlayerFilter.disableProperty().bind(textPlayerFilter.textProperty().isEmpty());
        buttonClearPlayerFilter.setOnAction(event1 -> textPlayerFilter.setText(""));
        textPlayerFilter.textProperty().addListener((observable, oldValue, newValue) -> refreshData());
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
        buttonJumpToMatch.setDisable(true);//TODO after match tab is implemented
        buttonJumpToSelf.setOnAction(event -> selectSelf());
        //mech stat columns
        buildMechTable();
        buildMatchTable();
        refreshData();
    }

    private void buildMechTable() {
        /*
        RecordFilterType.PLAYER.getInstance()
        TableColumn<CustomizableStatRuntime, String> colMech = new TableColumn<>("Mech");
        colMech.setCellValueFactory(param -> {
            String value = param.getValue().getMech().getShortName();
            return new ReadOnlyStringWrapper("test");
        });
        tableMechs.getColumns().add(colMech);
        for (CustomizableStatTemplate stat : StatBuilder.getDefaultPlayerTabMechStats()) {
            TableColumn<CustomizableStatRuntime, String> col = new TableColumn<>();
            col.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue()));
            Label columnHeader = new Label(stat.toString());
            columnHeader.setTooltip(new Tooltip(stat.getDescription()));
            col.setGraphic(columnHeader);
            GuiUtils.getColumnConstraint(columnHeader);
            col.setPrefWidth(GuiUtils.getColumnConstraint(columnHeader).getPrefWidth());
            col.setComparator(Utils.getNumberComparator());
            tableMechs.getColumns().add(col);
        }*/
    }

    private void buildMatchTable() {
        TableColumn<PlayerMatchRecord, String> colTime = new TableColumn<>("Match Time");
        colTime.setCellValueFactory(param -> {
            MatchRuntime match = MatchRuntime.getInstanceById(param.getValue().getMatchId());
            String value = match.getFormattedTimestamp();
            return new SimpleStringProperty(value);
        });
        TableColumn<PlayerMatchRecord, String> colMap = new TableColumn<>("Map");
        colMap.setCellValueFactory(param -> {
            MatchRuntime match = MatchRuntime.getInstanceById(param.getValue().getMatchId());
            String value = match.getMap();
            return new SimpleStringProperty(value);
        });
        TableColumn<PlayerMatchRecord, String> colGameMode = new TableColumn<>("GameMode");
        colGameMode.setCellValueFactory(param -> {
            MatchRuntime match = MatchRuntime.getInstanceById(param.getValue().getMatchId());
            String value = match.getGameMode();
            return new SimpleStringProperty(value);
        });
        TableColumn<PlayerMatchRecord, String> colMatchResult = new TableColumn<>("Result");
        colMatchResult.setCellValueFactory(param -> {
            MatchRuntime match = MatchRuntime.getInstanceById(param.getValue().getMatchId());
            String value = match.getMatchResult();
            return new SimpleStringProperty(value);
        });
        tablePlayerMatches.getColumns().addAll(colTime, colMap, colGameMode, colMatchResult);
        for (StatType stat : StatType.values()) {
            if (stat == StatType.MECH_TONS) {
                continue;
            }
            TableColumn<PlayerMatchRecord, String> col = new TableColumn<>();
            Label columnHeader = new Label(stat.toString());
            columnHeader.setTooltip(new Tooltip(stat.getDescription()));
            col.setGraphic(columnHeader);
            col.setPrefWidth(GuiUtils.getColumnConstraint(columnHeader).getPrefWidth());
            col.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getMatchValues().get(stat)));
            col.setComparator(Utils.getNumberComparator());
            tablePlayerMatches.getColumns().add(col);
        }
    }

    private void buildPlayerTable() {
        TableColumn<PlayerRuntime, String> colPlayerName = new TableColumn<>("Pilot Name");
        TableColumn<PlayerRuntime, String> colPlayerUnit = new TableColumn<>("Unit");
        TableColumn<PlayerRuntime, Number> colPlayerSeen = new TableColumn<>("# Seen");
        colPlayerName.setCellValueFactory(param -> {
            String value = param.getValue().getPilotname();
            return new SimpleStringProperty(value);
        });
        colPlayerUnit.setCellValueFactory(param -> {
            String value = param.getValue().getUnit();
            return new SimpleStringProperty(value);
        });
        colPlayerSeen.setCellValueFactory(param -> Bindings.size(param.getValue().getMatchRecords()));
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
        labelSeenInfo.textProperty().set("");
        labelPilotname.textProperty().set("");
        textShortNote.textProperty().set("");
        textNotes.textProperty().set("");
        pickerBack.valueProperty().set(Color.BLACK);
        pickerFront.valueProperty().set(Color.WHITE);
        listPossibleDuplicates.getItems().clear();
        panePlayerstats.getChildren().clear();
        tablePlayerMatches.getItems().clear();
        tableMechs.getItems().clear();
        if (newPlayer != null) {
            labelUnit.textProperty().bindBidirectional(newPlayer.unitProperty());
            labelPilotname.textProperty().bindBidirectional(newPlayer.pilotnameProperty());
            textShortNote.textProperty().bindBidirectional(newPlayer.shortnoteProperty());
            textNotes.textProperty().bindBidirectional(newPlayer.notesProperty());
            pickerBack.valueProperty().bindBidirectional(newPlayer.guicolor_backProperty());
            pickerFront.valueProperty().bindBidirectional(newPlayer.guicolor_frontProperty());
            listPossibleDuplicates.getItems().addAll(findDuplicates(newPlayer));
            labelSeenInfo.setText("Seen " + newPlayer.getMatchRecords().size() + " times between " + fdfSeen.format(findFirstSeen(newPlayer)) + " and " + fdfSeen.format(findLastSeen(newPlayer)));
            panePlayerstats.getChildren().add(new Label("Overall Stats:"));
           /* for (PlayerStat stat : PlayerStat.values()) {
                Label statdesc = new Label(stat.getDescription() + ":");
                statdesc.setBackground(new Background(new BackgroundFill(DisplayableStat.COLOR_PLAYERDATA, null, null)));
                Label statvalue = new Label(newPlayer.getCalculatedValues().get(stat));
                HBox hbox = new HBox(statdesc, statvalue);
                hbox.setSpacing(10);
                panePlayerstats.getChildren().add(hbox);
            }*/
            tablePlayerMatches.getItems().addAll(newPlayer.getMatchRecords());
            tablePlayerMatches.sort();
            //tableMechs.getItems().addAll(newPlayer.getMechStats().values());
            tableMechs.sort();
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
        tablePlayers.getItems().addAll(PlayerRuntime.getAllPlayers());
        tablePlayers.sort();
        if (selection != null && tablePlayers.getItems().contains(selection)) {
            selectPlayerFromList(selection);
        } else if (tablePlayers.getItems().size() > 0) {
            selectPlayerFromList(tablePlayers.getItems().get(0));
        } else {
            selectPlayer(null, null);
        }
    }

    public void selectPlayerFromList(PlayerRuntime pr) {
        ScoreboardController.getInstance().selectPlayerTab();
        tablePlayers.getSelectionModel().select(pr);
        tablePlayers.scrollTo(pr);
    }

    public void selectSelf() {
        buttonClearPlayerFilter.fire();
        selectPlayerFromList(PlayerRuntime.getInstance(SettingsTabController.getPlayername()));
    }
}
