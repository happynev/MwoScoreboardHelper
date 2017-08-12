package at.happynev.mwoscoreboardhelper;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by Nev on 15.01.2017.
 */
public class PlayerRuntime {

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
    private int playerNumber = -1;

    private PlayerRuntime(int id) {
        this.id = id;
        refreshDataFromDb();
        initBindings();
    }

    private PlayerRuntime() {
        this.id = -1;
        unit.set("[XXXX]");
        pilotname.set("Mechwarrior12345678901234567890");
        shortnote.set("this is not a real player");
        matchRecords.add(PlayerMatchRecord.getReferenceRecord(false,-1));
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
        if (_id == -1) {
            return getReferencePlayer();
        }
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
            prepInsert.close();
            firstTime = true;
        }
        rs.close();
        prepSelect.close();
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

    public ObservableList<PlayerMatchRecord> getMatchRecords() {
        if (matchRecords.size() == 0) {
            //lazy load
            reloadMatchRecordsFromDb();
        }
        return matchRecords;
    }

    private synchronized void reloadMatchRecordsFromDb() {
        try {
            PreparedStatement prepRecords = DbHandler.getInstance().prepareStatement("select match_data_id from player_matchdata where player_data_id=?");
            prepRecords.setInt(1, this.id);
            ResultSet rsRecords = prepRecords.executeQuery();
            ObservableList<PlayerMatchRecord> tmp = FXCollections.observableArrayList();
            while (rsRecords.next()) {
                int matchId = rsRecords.getInt(1);
                PlayerMatchRecord pmr = PlayerMatchRecord.getInstance(this.id, matchId);
                tmp.add(pmr);
            }
            rsRecords.close();
            matchRecords.setAll(tmp);
        } catch (Exception e) {
            Logger.error(e);
        }
    }

    public PlayerMatchRecord getMatchRecord(MatchRuntime match) {
        for (PlayerMatchRecord pmr : getMatchRecords()) {
            if (pmr.getMatchId() == match.getId()) {
                return pmr;
            }
        }
        return null;
    }

    private void refreshDataFromDb() {
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

    public void removeMatchRecord(int oldMatchId) {
        matchRecords.removeIf(pmr -> pmr.getMatchId() == oldMatchId);
    }
}
