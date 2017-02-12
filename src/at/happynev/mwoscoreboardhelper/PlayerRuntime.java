package at.happynev.mwoscoreboardhelper;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.effect.Bloom;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by Nev on 15.01.2017.
 */
public class PlayerRuntime {

    static final Insets DATA_INSETS = new Insets(2, 5, 2, 5);
    private static final Insets PLAYER_INSETS = new Insets(0, 10, 0, 10);
    private static Map<String, PlayerRuntime> playersByName = new HashMap<>();
    private static Map<Integer, PlayerRuntime> playersById = new HashMap<>();
    private final int id;
    private final SimpleStringProperty faction = new SimpleStringProperty("");
    private final SimpleStringProperty unit = new SimpleStringProperty("");
    private final SimpleStringProperty pilotname = new SimpleStringProperty("");
    private final SimpleObjectProperty<Color> guicolor_back = new SimpleObjectProperty(Color.web("#000000"));
    private final SimpleObjectProperty<Color> guicolor_front = new SimpleObjectProperty(Color.web("#FFFFFF"));
    private final SimpleStringProperty notes = new SimpleStringProperty("");
    private final SimpleStringProperty icon = new SimpleStringProperty("");
    private final SimpleStringProperty shortnote = new SimpleStringProperty("");
    private final ObservableList<PlayerMatchRecord> matchRecords = FXCollections.observableArrayList();
    private final Map<PlayerStat, StringExpression> calculatedValues = new TreeMap<>();
    private int playerNumber = -1;

    private PlayerRuntime(int id) {
        this.id = id;
        setupCalculatedValues();
        refreshDataFromDb();
        initBindings();
    }

    private PlayerRuntime() {
        this.id = -1;
        unit.set("[XXXX]");
        pilotname.set("Mechwarrior12345678901234567890");
        shortnote.set("this is not a real player");
        setupCalculatedValues();
        matchRecords.add(PlayerMatchRecord.getReferenceRecord(false));
    }

    public static PlayerRuntime getReferencePlayer() {
        return new PlayerRuntime();
    }

    public static PlayerRuntime getInstance(String playerName) {
        PlayerRuntime ret = playersByName.get(playerName);
        if (ret == null) {
            try {
                ret = createOrLoadPlayer(playerName);
            } catch (Exception e) {
                Logger.error(e);
                return null;
            }
            playersById.put(ret.getId(), ret);
            playersByName.put(ret.getPilotname(), ret);
        }
        return ret;
    }

    public static PlayerRuntime getInstance(int _id) {
        PlayerRuntime ret = playersById.get(_id);
        if (ret == null) {
            try {
                ret = new PlayerRuntime(_id);
            } catch (Exception e) {
                Logger.error(e);
                return null;
            }
            playersById.put(ret.getId(), ret);
            playersByName.put(ret.getPilotname(), ret);
        }
        return ret;
    }

    public static List<String> getAllPlayerNames() {
        List<String> ret = new ArrayList<>();
        try {
            PreparedStatement prep = DbHandler.getInstance().prepareStatement("select distinct pilotname from player_data");
            ResultSet rs = prep.executeQuery();
            while (rs.next()) {
                ret.add(rs.getString(1));
            }
            rs.close();
        } catch (SQLException e) {
            Logger.error(e);
        }
        Collections.sort(ret);
        return ret;
    }

    private static PlayerRuntime createOrLoadPlayer(String pilotName) throws Exception {
        PreparedStatement prepSelect = DbHandler.getInstance().prepareStatement("select id from player_data where pilotname=?");
        prepSelect.setString(1, pilotName);
        ResultSet rs = prepSelect.executeQuery();
        PlayerRuntime ret = null;
        int _id = -1;
        boolean firstTime = false;
        if (rs.next()) {
            _id = rs.getInt(1);
        } else {
            PreparedStatement prepInsert = DbHandler.getInstance().prepareStatement("insert into PLAYER_DATA(pilotname) values(?)", true);
            prepInsert.setString(1, pilotName);
            prepInsert.executeUpdate();
            ResultSet rsid = prepInsert.getGeneratedKeys();
            if (rsid.next()) {
                _id = rsid.getInt(1);
                if (_id == 0) {
                    throw new Exception("cannot create DB player row?");
                }
            }
            rsid.close();
            firstTime = true;
        }
        rs.close();
        ret = new PlayerRuntime(_id);
        if (firstTime && !ret.getPilotname().equals(SettingsTabController.getPlayername())) {
            SessionRuntime.playersNew.add(ret);
        }
        return ret;
    }

    private static String appendTopEntries(List<Map.Entry<String, Integer>> list, int max) {
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < Math.min(list.size(), max); i++) {
            if (ret.length() > 0) {
                ret.append(", ");
            }
            ret.append(list.get(i).getKey()).append("[").append(list.get(i).getValue()).append("]");
        }
        return ret.toString();
    }

    private static List<Map.Entry<String, Integer>> sortByValue(Map<String, Integer> map) {
        List<Map.Entry<String, Integer>> ret = new ArrayList<>(map.size());
        ret.addAll(map.entrySet());
        Collections.sort(ret, (o1, o2) -> {
            int c = o2.getValue().compareTo(o1.getValue());
            if (c == 0) {
                c = o2.getKey().compareTo(o1.getKey());
            }
            return c;
        });
        return ret;
    }

    private static void clickPlayer(MouseEvent event, PlayerRuntime pr) {
        if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
            PlayerTabController.getInstance().selectPlayer(pr);
        }
    }

    public Control applyPlayerFormat(Control node) {
        SimpleObjectProperty<Color> frontColor = new SimpleObjectProperty<>(Color.WHITE);
        SimpleObjectProperty<Color> backColor = new SimpleObjectProperty<>(Color.BLACK);
        frontColor.bind(guicolor_front);
        backColor.bind(guicolor_back);
        ObjectBinding<Background> backBinding = Bindings.createObjectBinding(() -> {
            BackgroundFill fill = new BackgroundFill(backColor.getValue(), CornerRadii.EMPTY, Insets.EMPTY);
            return new Background(fill);
        }, backColor);
        ObjectBinding<String> textBinding = Bindings.createObjectBinding(() -> "-fx-text-fill:" + Utils.getWebColor(frontColor.get()).replaceAll("0x", "#"), frontColor);
        node.backgroundProperty().bind(backBinding);
        GridPane.setFillWidth(node, true);
        node.setMaxWidth(Double.MAX_VALUE);
        node.setPadding(PLAYER_INSETS);
        if (node instanceof Labeled) {
            Labeled lnode = (Labeled) node;
            lnode.setFont(new Font(20));
            lnode.textFillProperty().bind(frontColor);
        }
        if (node instanceof TextInputControl) {
            TextInputControl tnode = (TextInputControl) node;
            tnode.styleProperty().bind(textBinding);
            tnode.setFont(new Font(18));
        }
        return node;
    }

    public void addDataToGrid(GridPane parent, int row, MatchRuntime currentMatch) {
        PlayerMatchRecord thisMatchRecord = currentMatch.getPlayerMatchRecord(this);
        Label labelUnit = new Label();
        Label labelName = new Label();
        TextField textShortNote = new TextField();
        applyPlayerFormat(labelUnit);
        applyPlayerFormat(labelName);
        applyPlayerFormat(textShortNote);
        labelName.effectProperty().bind(Bindings.when(labelName.hoverProperty()).then(new Bloom(0)).otherwise((Bloom) null));
        labelName.setTooltip(new Tooltip("Double-click to jump to player tab"));
        labelName.setOnMouseClicked(event -> clickPlayer(event, this));
        labelUnit.textProperty().bind(unit);
        labelName.textProperty().bind(pilotname);
        Tooltip noteTooltip = new Tooltip();
        noteTooltip.textProperty().bind(textShortNote.textProperty());
        textShortNote.textProperty().bindBidirectional(shortnote);
        textShortNote.setTooltip(noteTooltip);
        int col = 0;
        if (SettingsTabController.getInstance().getLayoutShowUnit()) {
            parent.add(labelUnit, col++, row);
        }
        if (SettingsTabController.getInstance().getLayoutShowName()) {
            ColumnConstraints tmp = GuiUtils.getColumnConstraint(labelName);
            ColumnConstraints cc = parent.getColumnConstraints().get(col);
            if (cc.getMinWidth() < tmp.getPrefWidth()) {
                cc.setMinWidth(tmp.getPrefWidth());
            }
            parent.add(labelName, col++, row);
        }
        if (SettingsTabController.getInstance().getLayoutShowNote()) {
            parent.add(textShortNote, col++, row);
        }

        for (Stat key : currentMatch.getStatsToDisplay()) {
            StringExpression value = null;
            if (key instanceof PlayerStat) {
                value = calculatedValues.get(key);
            } else if (thisMatchRecord != null && key instanceof MatchStat) {
                value = thisMatchRecord.getMatchValues().get(key);
            }
            if (value == null) {
                value = new SimpleStringProperty("?");
            }
            Label l = new Label();
            applyPlayerFormat(l);
            l.textProperty().bind(value);
            Tooltip tt = new Tooltip();
            tt.textProperty().bind(value);
            l.setTooltip(tt);
            ColumnConstraints tmp = GuiUtils.getColumnConstraint(l);
            ColumnConstraints cc = parent.getColumnConstraints().get(col);
            if (cc.getPrefWidth() < tmp.getPrefWidth()) {
                cc.setPrefWidth(tmp.getPrefWidth());
            }
            parent.add(l, col++, row);
        }
    }

    public int mergeInto(PlayerRuntime orig) {
        try {
            PreparedStatement prep = DbHandler.getInstance().prepareStatement("update player_matchdata set player_data_id=? where player_data_id=?");
            prep.setInt(1, orig.getId());
            prep.setInt(2, id);
            int ret = prep.executeUpdate();
            playersByName.remove(pilotname.get());
            playersById.remove(id);
            PreparedStatement prepDel = DbHandler.getInstance().prepareStatement("delete player_data where id=?");
            prepDel.setInt(1, id);
            prepDel.executeUpdate();
            orig.refreshDataFromDb();
            return ret;
        } catch (SQLException e) {
            Logger.error(e);
            return 0;
        }
    }

    public Color getGuicolor_back() {
        return guicolor_back.get();
    }

    public SimpleObjectProperty<Color> guicolor_backProperty() {
        return guicolor_back;
    }

    public Color getGuicolor_front() {
        return guicolor_front.get();
    }

    public SimpleObjectProperty<Color> guicolor_frontProperty() {
        return guicolor_front;
    }

    public int getPlayerNumber() {
        return playerNumber;
    }

    public void setPlayerNumber(int playerNumber) {
        this.playerNumber = playerNumber;
    }

    public Map<PlayerStat, StringExpression> getCalculatedValues() {
        return calculatedValues;
    }

    public ObservableList<PlayerMatchRecord> getMatchRecords() {
        return matchRecords;
    }

    public PlayerMatchRecord getMatchRecord(MatchRuntime match) {
        for (PlayerMatchRecord pmr : matchRecords) {
            if (pmr.getMatchId() == match.getId()) {
                return pmr;
            }
        }
        return null;
    }

    public void refreshDataFromDb() {
        try {
            PreparedStatement prepSelect = DbHandler.getInstance().prepareStatement("select pilotname,unit,guicolor_back,guicolor_front,notes,icon,shortnote from player_data where id=?");
            prepSelect.setInt(1, this.id);
            ResultSet rs = prepSelect.executeQuery();
            if (rs.next()) {
                pilotname.setValue(rs.getString(1));
                unit.setValue(rs.getString(2));
                if (unit.getValue() == null) unit.setValue("");
                String cback = rs.getString(3);
                String cfront = rs.getString(4);
                if (cback == null) {
                    cback = "#000000";
                }
                if (cfront == null) {
                    cfront = "#FFFFFF";
                }
                guicolor_back.setValue(Color.web(cback));
                guicolor_front.setValue(Color.web(cfront));
                notes.setValue(rs.getString(5));
                if (notes.getValue() == null) notes.setValue("");
                icon.setValue(rs.getString(6));
                if (icon.getValue() == null) icon.setValue("");
                shortnote.setValue(rs.getString(7));
                if (shortnote.getValue() == null) shortnote.setValue("");
            } else {
                throw new IllegalArgumentException("no player with id " + this.id);
            }
            rs.close();
            if (pilotname.get().equals(SettingsTabController.getPlayername())) {
                guicolor_back.bindBidirectional(SettingsTabController.getInstance().playerBackColorProperty());
                guicolor_front.bindBidirectional(SettingsTabController.getInstance().playerFrontColorProperty());
            } else {
                //in case of name change?
                guicolor_back.unbindBidirectional(SettingsTabController.getInstance().playerBackColorProperty());
                guicolor_front.unbindBidirectional(SettingsTabController.getInstance().playerFrontColorProperty());
            }
            PreparedStatement prepRecords = DbHandler.getInstance().prepareStatement("select match_data_id from player_matchdata where player_data_id=?");
            prepRecords.setInt(1, this.id);
            ResultSet rsRecords = prepRecords.executeQuery();
            ObservableList<PlayerMatchRecord> tmp = FXCollections.observableArrayList();
            while (rsRecords.next()) {
                int matchId = rsRecords.getInt(1);
                PlayerMatchRecord pmr = new PlayerMatchRecord(this.id, matchId);
                tmp.add(pmr);
            }
            matchRecords.setAll(tmp);
            rsRecords.close();
        } catch (Exception e) {
            Logger.error(e);
        }
    }

    public int getId() {
        return id;
    }

    public String getFaction() {
        return faction.get();
    }

    public SimpleStringProperty factionProperty() {
        return faction;
    }

    public String getUnit() {
        return unit.get();
    }

    public SimpleStringProperty unitProperty() {
        return unit;
    }

    public String getPilotname() {
        return pilotname.get();
    }

    public SimpleStringProperty pilotnameProperty() {
        return pilotname;
    }

    public String getNotes() {
        return notes.get();
    }

    public SimpleStringProperty notesProperty() {
        return notes;
    }

    public String getIcon() {
        return icon.get();
    }

    public SimpleStringProperty iconProperty() {
        return icon;
    }

    public String getShortnote() {
        return shortnote.get();
    }

    public SimpleStringProperty shortnoteProperty() {
        return shortnote;
    }

    private void initBindings() {
        faction.addListener((observable, oldValue, newValue) -> updatePlayerData("faction", newValue));
        unit.addListener((observable, oldValue, newValue) -> {
            //dont overwrite empty because of bad recognition
            if (newValue != null && !newValue.isEmpty()) updatePlayerData("unit", newValue);
        });
        guicolor_back.addListener((observable, oldValue, newValue) -> updatePlayerData("guicolor_back", Utils.getWebColor(newValue)));
        guicolor_front.addListener((observable, oldValue, newValue) -> updatePlayerData("guicolor_front", Utils.getWebColor(newValue)));
        notes.addListener((observable, oldValue, newValue) -> updatePlayerData("notes", newValue));
        icon.addListener((observable, oldValue, newValue) -> updatePlayerData("icon", newValue));
        shortnote.addListener((observable, oldValue, newValue) -> updatePlayerData("shortnote", newValue));
    }

    private void updatePlayerData(String field, String value) {
        try {
            //Utils.log("change " + field + " to " + value + "(" + id + " " + pilotname.get() + ")");
            PreparedStatement prep = DbHandler.getInstance().prepareStatement("update PLAYER_DATA set " + field + "=? where id=?");
            prep.setString(1, value);
            prep.setInt(2, id);
            prep.executeUpdate();
        } catch (SQLException e) {
            Logger.error(e);
        }
    }

    private void setupCalculatedValues() {
        final SimpleStringProperty favMechs = new SimpleStringProperty("");
        final SimpleStringProperty bestMechs = new SimpleStringProperty("");
        final SimpleStringProperty timesSeen = new SimpleStringProperty("");
        final SimpleStringProperty timesFinished = new SimpleStringProperty("");
        final SimpleStringProperty avgDamage = new SimpleStringProperty("");
        final SimpleStringProperty avgScore = new SimpleStringProperty("");
        final SimpleStringProperty avgKills = new SimpleStringProperty("");
        final SimpleStringProperty avgAssists = new SimpleStringProperty("");
        final SimpleStringProperty survivalRate = new SimpleStringProperty("");
        final SimpleStringProperty kdr = new SimpleStringProperty("");

        calculatedValues.clear();
        calculatedValues.put(PlayerStat.FAVMECHS, favMechs);
        calculatedValues.put(PlayerStat.BESTMECHS, bestMechs);
        calculatedValues.put(PlayerStat.TIMESSEEN, timesSeen);
        calculatedValues.put(PlayerStat.TIMESFINISHED, timesFinished);
        calculatedValues.put(PlayerStat.AVGDAMAGE, avgDamage);
        calculatedValues.put(PlayerStat.AVGSCORE, avgScore);
        calculatedValues.put(PlayerStat.AVGKILLS, avgKills);
        calculatedValues.put(PlayerStat.SURVIVAL, survivalRate);
        calculatedValues.put(PlayerStat.AVGASSISTS, avgAssists);
        calculatedValues.put(PlayerStat.KDR, kdr);

        matchRecords.addListener((ListChangeListener<? super PlayerMatchRecord>) c -> {
            //Utils.log("recalc values for " + pilotname.get());
            int totalDamage = 0;
            int totalScore = 0;
            int totalAlive = 0;
            int totalKills = 0;
            int totalAssists = 0;
            int totalValidMatches = 0;
            Map<String, Integer> mechsSeen = new TreeMap<>();
            Map<String, List<Integer>> mechScores = new HashMap<>();
            for (PlayerMatchRecord mr : matchRecords) {
                boolean isValidMatch = mr.getPing() > 0;
                if (isValidMatch) {
                    totalValidMatches++;
                    totalScore += mr.getMatchScore();
                    totalDamage += mr.getDamage();
                    totalAssists += mr.getAssists();
                    totalKills += mr.getKills();
                    if (mr.getStatus().equals("ALIVE")) totalAlive++;
                }
                if (mr.getMech() != null && !mr.getMech().isEmpty()) {
                    Integer seen = mechsSeen.get(mr.getMech());
                    List<Integer> mechScore = mechScores.get(mr.getMech());
                    if (seen == null) {
                        seen = 0;
                    }
                    if (mechScore == null) {
                        mechScore = new ArrayList<>();
                        mechScores.put(mr.getMech(), mechScore);
                    }
                    mechsSeen.put(mr.getMech(), seen + 1);
                    if (isValidMatch) {
                        mechScore.add(mr.getMatchScore());
                    }
                }
            }
            Map<String, Integer> mechAvgScores = new HashMap();
            for (Map.Entry<String, List<Integer>> e : mechScores.entrySet()) {
                double totalMechScore = 0;
                for (Integer s : e.getValue()) {
                    totalMechScore += s;
                }
                double davgScore = totalMechScore / (double) e.getValue().size();
                mechAvgScores.put(e.getKey(), (int) davgScore);
            }
            if (totalValidMatches > 0) {
                int iAvgScore = (int) ((double) totalScore / (double) totalValidMatches);
                int iTotalDamage = (int) ((double) totalDamage / (double) totalValidMatches);
                avgDamage.set("" + iTotalDamage);
                avgScore.set("" + iAvgScore);
                String assists = new BigDecimal((double) totalAssists / (double) totalValidMatches).setScale(1, BigDecimal.ROUND_HALF_UP).toPlainString();
                String kills = new BigDecimal((double) totalKills / (double) totalValidMatches).setScale(1, BigDecimal.ROUND_HALF_UP).toPlainString();
                int totalDead = totalValidMatches - totalAlive;
                if (totalDead > 0) {
                    String kdratio = new BigDecimal((double) totalKills / (double) totalDead).setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString();
                    kdr.set(kdratio);
                } else {
                    kdr.set(new BigDecimal(totalKills).setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString());
                }
                avgKills.set(kills);
                avgAssists.set(assists);
                String aliveRatio = new BigDecimal((double) totalAlive / (double) totalValidMatches).multiply(new BigDecimal(100)).setScale(0, BigDecimal.ROUND_HALF_UP).toPlainString();
                survivalRate.set(aliveRatio + "%");
                while (c.next()) {
                    for (PlayerMatchRecord pmr : c.getAddedSubList()) {
                        if (mechAvgScores.keySet().contains(pmr.getMech())) {
                            int mechavg = mechAvgScores.get(pmr.getMech());
                            if (mechavg > 0) {
                                String mechperf = new BigDecimal((double) pmr.getMatchScore() / (double) mechavg).multiply(new BigDecimal(100)).setScale(0, BigDecimal.ROUND_HALF_UP).toPlainString();
                                pmr.getMatchValues().put(MatchStat.MATCHMECHPERF, new SimpleStringProperty(mechperf + "%"));
                            } else {
                                pmr.getMatchValues().put(MatchStat.MATCHMECHPERF, new SimpleStringProperty("x0%"));
                            }
                        } else {
                            Logger.log("no avg for " + pmr.getMech());
                        }
                        if (iAvgScore > 0) {
                            String perf = new BigDecimal((double) pmr.getMatchScore() / (double) iAvgScore).multiply(new BigDecimal(100)).setScale(0, BigDecimal.ROUND_HALF_UP).toPlainString();
                            pmr.getMatchValues().put(MatchStat.MATCHPERF, new SimpleStringProperty(perf + "%"));
                        } else {
                            pmr.getMatchValues().put(MatchStat.MATCHPERF, new SimpleStringProperty("x%"));
                        }
                    }
                }
            }
            List<Map.Entry<String, Integer>> favMechSorted = sortByValue(mechsSeen);
            List<Map.Entry<String, Integer>> bestMechSorted = sortByValue(mechAvgScores);
            favMechs.set(appendTopEntries(favMechSorted, 5));
            bestMechs.set(appendTopEntries(bestMechSorted, 5));
            timesSeen.set("" + matchRecords.size());
            timesFinished.set("" + totalValidMatches);
        });
    }
}
