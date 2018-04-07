package at.happynev.mwoscoreboardhelper;

import at.happynev.mwoscoreboardhelper.isenleaderboard.IsenLeaderboard;
import at.happynev.mwoscoreboardhelper.isenleaderboard.IsenLeaderboardResult;
import at.happynev.mwoscoreboardhelper.preloader.Preloadable;
import at.happynev.mwoscoreboardhelper.tracer.PlayerInfoTracer;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableIntegerValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import javafx.concurrent.Task;
import javafx.scene.paint.Color;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Nev on 15.01.2017.
 */
public class PlayerRuntime implements Preloadable {
    private static ObservableMap<String, PlayerRuntime> playersByName = FXCollections.observableHashMap();
    private static ObservableMap<Integer, PlayerRuntime> playersById = FXCollections.observableHashMap();
    private final static IntegerBinding totalRecords = Bindings.size(playersById);
    private final int id;
    private final SimpleStringProperty faction = new SimpleStringProperty("");
    private final SimpleStringProperty unit = new SimpleStringProperty("");
    private final SimpleStringProperty pilotname = new SimpleStringProperty("");
    private final SimpleObjectProperty<Color> guicolor_back = new SimpleObjectProperty(Color.web("#000000"));
    private final SimpleObjectProperty<Color> guicolor_front = new SimpleObjectProperty(Color.web("#FFFFFF"));
    private final SimpleStringProperty notes = new SimpleStringProperty("");
    private final SimpleStringProperty icon = new SimpleStringProperty("");
    private final SimpleStringProperty shortnote = new SimpleStringProperty("");
    private final ObservableSet<PlayerMatchRecord> matchRecords = FXCollections.observableSet();
    private int playerNumber = -1;

    private PlayerRuntime() {
        this.id = -1;
        unit.set("[XXXX]");
        pilotname.set("Mechwarrior12345678901234567890");
        shortnote.set("this is not a real player");
        matchRecords.add(PlayerMatchRecord.getReferenceRecord(false, -1));
    }

    private PlayerRuntime(int id, String pilotname, String unit, String guicolor_back, String guicolor_front, String notes, String icon, String shortnote) {
        this.id = id;
        if (pilotname == null || pilotname.isEmpty()) {
            pilotname = "?? undefined";
        }
        this.pilotname.setValue(pilotname);
        if (unit == null) unit = "";
        if (guicolor_back == null) guicolor_back = "#000000";
        if (guicolor_front == null) guicolor_front = "#FFFFFF";
        if (notes == null) notes = "";
        if (icon == null) icon = "";
        if (shortnote == null) shortnote = "";
        this.unit.setValue(unit);
        this.guicolor_back.setValue(Color.web(guicolor_back));
        this.guicolor_front.setValue(Color.web(guicolor_front));
        this.notes.setValue(notes);
        this.icon.setValue(icon);
        this.shortnote.setValue(shortnote);
        addMatchRecordsFromDb();
        initBindings();
        playersById.put(id, this);
        playersByName.put(pilotname, this);
    }

    private static PlayerRuntime getInstance(String pilotName, String unitTag) {
        int tmpId = -1;
        try {
            PreparedStatement prep = DbHandler.getInstance().prepareStatement("insert into player_data(pilotname,unit) values(?,?)", true);
            prep.setString(1, pilotName);
            prep.setString(2, unitTag);
            prep.executeUpdate();
            ResultSet rsid = prep.getGeneratedKeys();
            if (rsid.next()) {
                tmpId = rsid.getInt(1);
            }
        } catch (SQLException e) {
            Logger.error(e);
            tmpId = -1;
        }
        return new PlayerRuntime(tmpId, pilotName, unitTag, null, null, "", "", "");
    }

    public static PlayerRuntime getUserInstance() {
        return SettingsTabController.getSelfPlayerInstance();
    }

    public static PlayerRuntime getReferencePlayer() {
        return new PlayerRuntime();
    }

    public static PlayerRuntime getInstance(String playerName) {
        PlayerRuntime ret = playersByName.get(playerName);
        return ret;
    }

    public static PlayerRuntime getInstance(int _id) {
        if (_id == -1) {
            return getReferencePlayer();
        }
        PlayerRuntime ret = playersById.get(_id);
        return ret;
    }

    public static Set<String> getAllPlayerNames() {
        return playersByName.keySet();
    }

    public static PlayerRuntime createOrLoadFromTrace(final PlayerInfoTracer pi) {
        String pilotName = pi.getPilotName();
        IsenLeaderboardResult leaderboard = IsenLeaderboard.getInstance().getLeaderboardData(pilotName);
        if (leaderboard != null) {
            pilotName = leaderboard.getPlayerName(); //go with name from leaderboard, better validation
        }
        boolean firstTime = false;
        boolean known = false;
        PlayerRuntime ret = getInstance(pilotName);
        if (ret == null) {
            firstTime = true;
            ret = getInstance(pilotName, pi.getUnitTag());
        } else {
            known = true;
            if (!ret.getUnit().equals(pi.getUnitTag())) {
                ret.unitProperty().setValue(pi.getUnitTag());
            }
        }
        if (firstTime && !ret.equals(getUserInstance())) {
            SessionRuntime.playersNew.add(ret);
        }
        if (known && !ret.equals(getUserInstance())) {
            SessionRuntime.playersKnown.add(ret);
        }
        return ret;
    }

    public static Preloadable getPreloaderInstance() {
        return getReferencePlayer();
    }

    public static Collection<PlayerRuntime> getAllPlayers() {
        return playersById.values();
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

    public ObservableSet<PlayerMatchRecord> getMatchRecords() {
        return matchRecords;
    }

    private void addMatchRecordsFromDb() {
        try {
            PreparedStatement prepRecords = DbHandler.getInstance().prepareStatement("select match_data_id from player_matchdata where player_data_id=?");
            prepRecords.setInt(1, this.id);
            ResultSet rsRecords = prepRecords.executeQuery();
            Set<PlayerMatchRecord> tmp = new HashSet<>();
            while (rsRecords.next()) {
                int matchId = rsRecords.getInt(1);
                PlayerMatchRecord pmr = PlayerMatchRecord.getInstance(this.id, matchId);
                tmp.add(pmr);
            }
            rsRecords.close();
            matchRecords.addAll(tmp);
        } catch (Exception e) {
            Logger.error(e);
        }
    }

    @Deprecated
    //not used
    private PlayerMatchRecord getMatchRecord(MatchRuntime match) {
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
        if (pilotname.get().equals(SettingsTabController.getSelfPlayerInstance().getPilotname())) {
            guicolor_back.bindBidirectional(SettingsTabController.getInstance().playerBackColorProperty());
            guicolor_front.bindBidirectional(SettingsTabController.getInstance().playerFrontColorProperty());
        } else {
            //in case of name change?
            guicolor_back.unbindBidirectional(SettingsTabController.getInstance().playerBackColorProperty());
            guicolor_front.unbindBidirectional(SettingsTabController.getInstance().playerFrontColorProperty());
        }
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

    @Override
    public ObservableIntegerValue loadedCountProperty() {
        return totalRecords;
    }

    @Override
    public ObservableIntegerValue totalCountProperty() {
        if (totalRecords.get() == 0) {
            int count = 0;
            try {
                PreparedStatement prep = DbHandler.getInstance().prepareStatement("select count(*) from player_data");
                ResultSet rs = prep.executeQuery();
                rs.next();
                count = rs.getInt(1);
                rs.close();
                prep.close();
            } catch (SQLException e) {
                Logger.error(e);
            }
            return new SimpleIntegerProperty(count);
        } else {
            return totalRecords;
        }
    }

    @Override
    public Task getLoaderTask() {
        final int totalWork = totalCountProperty().get();
        return new Task() {

            @Override
            protected Object call() {
                try {
                    PreparedStatement prep = DbHandler.getInstance().prepareStatement("select id,pilotname,unit,guicolor_back,guicolor_front,notes,icon,shortnote from player_data");
                    ResultSet rs = prep.executeQuery();
                    while (rs.next()) {
                        int id = rs.getInt(1);
                        String pilotname = rs.getString(2);
                        String unit = rs.getString(3);
                        String guicolor_back = rs.getString(4);
                        String guicolor_front = rs.getString(5);
                        String notes = rs.getString(6);
                        String icon = rs.getString(7);
                        String shortnote = rs.getString(8);
                        new PlayerRuntime(id, pilotname, unit, guicolor_back, guicolor_front, notes, icon, shortnote);
                        updateProgress(playersById.size(), totalWork);
                        updateMessage("(" + playersById.size() + "/" + totalWork + ")");
                    }
                    rs.close();
                    prep.close();
                } catch (Exception e) {
                    Logger.alertPopup("loading all player records FAILED");
                    Logger.error(e);
                }
                return null;
            }
        };
    }

    @Override
    public String getPreloadCaption() {
        return "Player data";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlayerRuntime that = (PlayerRuntime) o;

        return pilotname.getValue().equals(that.pilotname.getValue());
    }

    @Override
    public int hashCode() {
        return pilotname.getValue().hashCode();
    }
}
