package at.happynev.mwoscoreboardhelper;

import at.happynev.mwoscoreboardhelper.isenleaderboard.IsenLeaderboard;
import at.happynev.mwoscoreboardhelper.isenleaderboard.IsenLeaderboardResult;
import at.happynev.mwoscoreboardhelper.stat.CustomizableStatRuntime;
import at.happynev.mwoscoreboardhelper.stat.CustomizableStatTemplate;
import at.happynev.mwoscoreboardhelper.stat.StatBuilder;
import at.happynev.mwoscoreboardhelper.stat.StatType;
import at.happynev.mwoscoreboardhelper.tracer.ValueHelpers;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.apache.commons.lang3.time.FastDateFormat;

import java.util.*;
import java.util.stream.Collectors;

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
    @FXML
    Label labelNumPlayers;
    @FXML
    Label labelLeaderboardInfo;
    @FXML
    Button buttonUpdateLeaderboardInfo;
    MechdataAggregation selectedAggregation = MechdataAggregation.VARIANT;

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

    private enum MechdataAggregation {
        FACTION,
        CLASS,
        CHASSIS,
        VARIANT,
        TONNAGE,
        MAP,
        GAMEMODE;

        @Override
        public String toString() {
            switch (this) {
                case FACTION:
                    return "Faction";
                case CLASS:
                    return "Class";
                case CHASSIS:
                    return "Chassis";
                case VARIANT:
                    return "Variant";
                case TONNAGE:
                    return "Tonnage";
                case MAP:
                    return "Map";
                case GAMEMODE:
                    return "Gamemode";
                default:
                    return "xxx";
            }
        }
    }

    @FXML
    private void initialize() {
        //tablePlayerMatches.setItems(FXCollections.observableArrayList());
        //tableMechs.setItems(FXCollections.observableArrayList());
        tablePlayerMatches.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
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
        labelNumPlayers.textProperty().bind(Bindings.concat(Bindings.size(tablePlayers.getItems())));
        panePlayername.backgroundProperty().bind(backBinding);
        buttonClearPlayerFilter.disableProperty().bind(textPlayerFilter.textProperty().isEmpty());
        buttonClearPlayerFilter.setOnAction(event1 -> textPlayerFilter.setText(""));
        textPlayerFilter.textProperty().addListener((observable, oldValue, newValue) -> refreshData());
        labelUnit.textFillProperty().bind(pickerFront.valueProperty());
        labelPilotname.textFillProperty().bind(pickerFront.valueProperty());
        listPossibleDuplicates.setCellFactory(param -> new ListCell<PlayerRuntime>() {
            @Override
            protected void updateItem(PlayerRuntime item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    this.setGraphic(new Label(item.getFaction() + " " + item.getPilotname() + "(" + item.getMatchRecords().size() + " Matches)"));
                } else {
                    this.setGraphic(null);
                }
            }
        });
        buttonJumpToSelf.setOnAction(event -> selectSelf());
        buttonUpdateLeaderboardInfo.setOnAction(event -> {
            IsenLeaderboardResult result = IsenLeaderboard.getInstance().getLeaderboardData(tablePlayers.getSelectionModel().getSelectedItem().getPilotname());
            if (result != null) {
                labelLeaderboardInfo.setText("Leaderboard rank: " + result.getOverallData().getRank());
            } else {
                labelLeaderboardInfo.setText("Player not found. most likely an OCR error");
            }
        });
        //mech stat columns
        buildMechTable();
        buildMatchTable();
        refreshData();
    }

    private void buildMechTable() {

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
        tablePlayerMatches.getColumns().add(colTime);
        for (StatType stat : StatType.values()) {
            if (stat == StatType.MECH_TONS || stat.getDescription().toLowerCase().contains("jarl")) {
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
        tablePlayerMatches.getItems().clear();
        labelLeaderboardInfo.textProperty().setValue("");
        panePlayerstats.getChildren().clear();
        if (newPlayer != null) {
            labelUnit.textProperty().bindBidirectional(newPlayer.unitProperty());
            labelPilotname.textProperty().bindBidirectional(newPlayer.pilotnameProperty());
            textShortNote.textProperty().bindBidirectional(newPlayer.shortnoteProperty());
            textNotes.textProperty().bindBidirectional(newPlayer.notesProperty());
            pickerBack.valueProperty().bindBidirectional(newPlayer.guicolor_backProperty());
            pickerFront.valueProperty().bindBidirectional(newPlayer.guicolor_frontProperty());
            listPossibleDuplicates.getItems().addAll(findDuplicates(newPlayer));
            labelSeenInfo.setText("Seen " + newPlayer.getMatchRecords().size() + " times between " + fdfSeen.format(findFirstSeen(newPlayer)) + " and " + fdfSeen.format(findLastSeen(newPlayer)));
            tablePlayerMatches.getItems().addAll(newPlayer.getMatchRecords());
            tablePlayerMatches.sort();
            buildMechTable(newPlayer);
        }
    }

    private static class AggregateMatchData {
        private final Map<String, String> stats = new HashMap<>();
        private final String listKey;

        public AggregateMatchData(String listKey, PlayerRuntime pr, MechdataAggregation agg) {
            this.listKey = listKey;
            PlayerMatchRecord refRecord = PlayerMatchRecord.getReferenceRecord(pr.getId());
            List<PlayerMatchRecord> matches = new ArrayList<>();
            if (agg == MechdataAggregation.VARIANT) {
                matches.addAll(pr.getMatchRecords().parallelStream().filter(pmr -> pmr.getMech().getShortName().equals(listKey)).collect(Collectors.toList()));
            } else if (agg == MechdataAggregation.CHASSIS) {
                matches.addAll(pr.getMatchRecords().parallelStream().filter(pmr -> pmr.getMech().getChassis().equals(listKey)).collect(Collectors.toList()));
            } else if (agg == MechdataAggregation.CLASS) {
                matches.addAll(pr.getMatchRecords().parallelStream().filter(pmr -> pmr.getMech().getWeightClass().equals(listKey)).collect(Collectors.toList()));
            } else if (agg == MechdataAggregation.FACTION) {
                matches.addAll(pr.getMatchRecords().parallelStream().filter(pmr -> pmr.getMech().getFaction().equals(listKey)).collect(Collectors.toList()));
            } else if (agg == MechdataAggregation.TONNAGE) {
                matches.addAll(pr.getMatchRecords().parallelStream().filter(pmr -> listKey.equals("" + pmr.getMech().getTons())).collect(Collectors.toList()));
            } else if (agg == MechdataAggregation.MAP) {
                matches.addAll(pr.getMatchRecords().parallelStream().filter(pmr -> pmr.getMatchValues().get(StatType.MAP).equals(listKey)).collect(Collectors.toList()));
            } else if (agg == MechdataAggregation.GAMEMODE) {
                matches.addAll(pr.getMatchRecords().parallelStream().filter(pmr -> pmr.getMatchValues().get(StatType.GAMEMODE).equals(listKey)).collect(Collectors.toList()));
            }
            for (CustomizableStatTemplate template : StatBuilder.getDefaultPlayerTabMechStats()) {
                CustomizableStatRuntime stat = template.getRuntimeInstance(refRecord);
                String value = stat.getValue(matches);
                stats.put(template.getShortName(), value);
            }
        }

    }

    private void buildMechTable(PlayerRuntime newPlayer) {
        if (newPlayer == null) {
            return;
        }
        ChoiceBox<MechdataAggregation> choiceAggregateMechStats = new ChoiceBox<>();
        choiceAggregateMechStats.getItems().addAll(MechdataAggregation.values());
        choiceAggregateMechStats.getSelectionModel().select(selectedAggregation);
        choiceAggregateMechStats.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            panePlayerstats.getChildren().clear();
            selectedAggregation = newValue;
            buildMechTable(newPlayer);
        });

        TableView<AggregateMatchData> tableMechdata = new TableView<>();
        List<String> listKeys = new ArrayList<>();
        if (selectedAggregation == MechdataAggregation.VARIANT) {
            listKeys.addAll(newPlayer.getMatchRecords().parallelStream().map(pmr -> pmr.getMech().getShortName()).collect(Collectors.toSet()));
            TableColumn<AggregateMatchData, String> keyCol = new TableColumn<>("Mech Variant");
            keyCol.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().listKey));
            tableMechdata.getColumns().add(keyCol);
        } else if (selectedAggregation == MechdataAggregation.CHASSIS) {
            listKeys.addAll(newPlayer.getMatchRecords().parallelStream().map(pmr -> pmr.getMech().getChassis()).collect(Collectors.toSet()));
            TableColumn<AggregateMatchData, String> keyCol = new TableColumn<>("Mech Chassis");
            keyCol.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().listKey));
            tableMechdata.getColumns().add(keyCol);
        } else if (selectedAggregation == MechdataAggregation.CLASS) {
            listKeys.addAll(newPlayer.getMatchRecords().parallelStream().map(pmr -> pmr.getMech().getWeightClass()).collect(Collectors.toSet()));
            TableColumn<AggregateMatchData, String> keyCol = new TableColumn<>("Class");
            keyCol.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().listKey));
            tableMechdata.getColumns().add(keyCol);
        } else if (selectedAggregation == MechdataAggregation.FACTION) {
            listKeys.addAll(newPlayer.getMatchRecords().parallelStream().map(pmr -> pmr.getMech().getFaction()).collect(Collectors.toSet()));
            TableColumn<AggregateMatchData, String> keyCol = new TableColumn<>("Faction");
            keyCol.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().listKey));
            tableMechdata.getColumns().add(keyCol);
        } else if (selectedAggregation == MechdataAggregation.TONNAGE) {
            listKeys.addAll(newPlayer.getMatchRecords().parallelStream().map(pmr -> pmr.getMech().getTons() + "").collect(Collectors.toSet()));
            TableColumn<AggregateMatchData, String> keyCol = new TableColumn<>("Tons");
            keyCol.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().listKey));
            tableMechdata.getColumns().add(keyCol);
        } else if (selectedAggregation == MechdataAggregation.MAP) {
            listKeys.addAll(newPlayer.getMatchRecords().parallelStream().map(pmr -> pmr.getMatchValues().get(StatType.MAP)).collect(Collectors.toSet()));
            TableColumn<AggregateMatchData, String> keyCol = new TableColumn<>("Map");
            keyCol.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().listKey));
            tableMechdata.getColumns().add(keyCol);
        } else if (selectedAggregation == MechdataAggregation.GAMEMODE) {
            listKeys.addAll(newPlayer.getMatchRecords().parallelStream().map(pmr -> pmr.getMatchValues().get(StatType.GAMEMODE)).collect(Collectors.toSet()));
            TableColumn<AggregateMatchData, String> keyCol = new TableColumn<>("GameMode");
            keyCol.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().listKey));
            tableMechdata.getColumns().add(keyCol);
        }
        for (String key : listKeys) {
            AggregateMatchData agg = new AggregateMatchData(key, newPlayer, selectedAggregation);
            tableMechdata.getItems().add(agg);
            if (tableMechdata.getColumns().size() == 1) {
                for (String stat : agg.stats.keySet()) {
                    TableColumn<AggregateMatchData, String> col = new TableColumn<>(stat);
                    col.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().stats.get(stat)));
                    col.setComparator(new Utils.NumericStringComparator());
                    tableMechdata.getColumns().add(col);
                }
            }
        }

        panePlayerstats.getChildren().setAll(new HBox(5, new Label("Aggregate by"), choiceAggregateMechStats),
                tableMechdata
        );
    }

    private List<PlayerRuntime> findDuplicates(PlayerRuntime orig) {
        List<PlayerRuntime> ret = new ArrayList<>();
        List<String> possibleNames = ValueHelpers.findSimilarLookingStrings(orig.getPilotname(), PlayerRuntime.getAllPlayerNames());//visual
        possibleNames.addAll(ValueHelpers.findSimilarStrings(orig.getPilotname(), PlayerRuntime.getAllPlayerNames(), 3));//levenshtein
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
        tablePlayers.getItems().clear();
        //tablePlayers.getItems().addAll(PlayerRuntime.getAllPlayers());
        Collection<PlayerRuntime> unfiltered = PlayerRuntime.getAllPlayers();
        unfiltered.stream().filter(playerRuntime -> {
            return textPlayerFilter.getText().isEmpty() || playerRuntime.getPilotname().toLowerCase().contains(textPlayerFilter.getText().toLowerCase());
        }).forEach(playerRuntime -> tablePlayers.getItems().add(playerRuntime));
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
        selectPlayerFromList(SettingsTabController.getSelfPlayerInstance());
    }
}
