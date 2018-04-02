package at.happynev.mwoscoreboardhelper;

import at.happynev.mwoscoreboardhelper.preloader.Preloadable;
import at.happynev.mwoscoreboardhelper.stat.StatType;
import at.happynev.mwoscoreboardhelper.tracer.RewardInfoTracer;
import at.happynev.mwoscoreboardhelper.tracer.ValueHelpers;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableIntegerValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.concurrent.Task;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Nev on 20.01.2017.
 */
public class PersonalMatchRecord implements Preloadable {
    private final static ObservableMap<String, PersonalMatchRecord> allRecords = FXCollections.observableHashMap();
    private final static IntegerBinding totalRecords = Bindings.size(allRecords);
    private final int playerId;
    private final Map<StatType, String> matchValues = new TreeMap<>();
    private String id;
    private int matchId;

    private PersonalMatchRecord(int playerId, int matchId, String reward_cbills, String reward_xp, String solo_kills, String kmdds, String comps) {
        this.playerId = playerId;
        this.id = playerId + "_" + matchId;
        if (playerId == -1) {
            //no playerrecord exists? cannot do much now.
            return;
        }
        this.matchId = matchId;
        matchValues.put(StatType.REWARD_CBILLS, reward_cbills);
        matchValues.put(StatType.REWARD_XP, reward_xp);
        matchValues.put(StatType.SOLO_KILLS, solo_kills);
        matchValues.put(StatType.KMDDS, kmdds);
        matchValues.put(StatType.COMPONENT_DESTROYED, comps);
        if (playerId != -1 && matchId != -1) {
            allRecords.put(id, this);
        }
    }

    public static PersonalMatchRecord createFromTrace(PlayerRuntime player, RewardInfoTracer info, MatchRuntime match) {
        if (!info.getFinished()) {
            throw new IllegalArgumentException("RewardInfoTracer is not ready");
        }
        if (info == null) {
            throw new IllegalArgumentException("no reward info tracer");
        } else {
            int tmpSolo = 0;
            int tmpKmdd = 0;
            int tmpComp = 0;
            for (int i = 0; i < 9; i++) {
                String valuename = ValueHelpers.guessValue(info.getPerformanceName(i), ValueHelpers.ValueList.MATCHPERFORMANCE.getItems());
                if (valuename.equals("SOLO KILL")) {
                    tmpSolo = info.getPerformanceValue(i);
                } else if (valuename.equals("KILL MOST DAMAGE DEALT")) {
                    tmpKmdd = info.getPerformanceValue(i);
                } else if (valuename.equals("COMPONENT DESTROYED")) {
                    tmpComp = info.getPerformanceValue(i);
                }
            }
            PersonalMatchRecord newPmr = new PersonalMatchRecord(player.getId(), match.getId(), "" + info.getCbills(), "" + info.getXp(), "" + tmpSolo, "" + tmpKmdd, "" + tmpComp);
            SessionRuntime.sessionPersonalRecords.add(newPmr);
            return newPmr;
        }
    }

    public static PersonalMatchRecord getInstance(int playerId, int matchId) {
        String key = playerId + "_" + matchId;
        return allRecords.get(key);
    }

    public static PersonalMatchRecord getReferenceRecord(int playerId) {
        return new PersonalMatchRecord(playerId, -1, "50000", "1000", "1", "2", "3");
    }

    public static Preloadable getPreloaderInstance() {
        return getReferenceRecord(-1);
    }

    public Map<StatType, String> getMatchValues() {
        return matchValues;
    }

    public int getRewardsCbills() {
        return Integer.parseInt(matchValues.getOrDefault(StatType.REWARD_CBILLS, "0"));
    }

    public int getRewardsXp() {
        return Integer.parseInt(matchValues.getOrDefault(StatType.REWARD_XP, "0"));
    }

    public int getSoloKills() {
        return Integer.parseInt(matchValues.getOrDefault(StatType.SOLO_KILLS, "0"));
    }

    public int getKmdds() {
        return Integer.parseInt(matchValues.getOrDefault(StatType.KMDDS, "0"));
    }

    public int getComponentDestroyed() {
        return Integer.parseInt(matchValues.getOrDefault(StatType.COMPONENT_DESTROYED, "0"));
    }

    public void saveData(int matchId) throws SQLException {
        if (playerId == -1) {
            //not much we can do.
            return;
        }
        int oldId = matchId;
        this.matchId = matchId;
        allRecords.remove(id);//delete preliminary record
        id = playerId + "_" + matchId;
        allRecords.put(id, this);//update with new id
        PreparedStatement prepDel = DbHandler.getInstance().prepareStatement("delete personal_matchdata where player_data_id=? and match_data_id in (?,?)");
        prepDel.setInt(1, playerId);
        prepDel.setInt(2, matchId);
        prepDel.setInt(3, oldId);
        prepDel.executeUpdate();
        PreparedStatement prep = DbHandler.getInstance().prepareStatement("insert into personal_matchdata(player_data_id,match_data_id," +
                "reward_cbills,reward_xp,stat_solo,stat_kmdd,stat_comp) values(?,?,?,?,?,?,?)");
        prep.setInt(1, playerId);
        prep.setInt(2, matchId);
        prep.setInt(3, getRewardsCbills());
        prep.setInt(4, getRewardsXp());
        prep.setInt(5, getSoloKills());
        prep.setInt(6, getKmdds());
        prep.setInt(7, getComponentDestroyed());
        prep.executeUpdate();
    }

    private void updateRecord(String field, int value) {
        try {
            PreparedStatement prep = DbHandler.getInstance().prepareStatement("update PERSONAL_MATCHDATA set " + field + "=? where match_data_id=? and player_data_id=?");
            prep.setInt(1, value);
            prep.setInt(2, matchId);
            prep.setInt(3, playerId);
            prep.executeUpdate();
        } catch (SQLException e) {
            Logger.error(e);
        }
    }

    public void delete() {
        allRecords.remove(id);
        //PreparedStatement prep = DbHandler.getInstance().prepareStatement("delete from player_matchdata where player_data_id=? and match_data_id=?");
        //cascaded from match or player
    }

    public int getMatchId() {
        return matchId;
    }

    public int getPlayerId() {
        return playerId;
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
                PreparedStatement prep = DbHandler.getInstance().prepareStatement("select count(*) from personal_matchdata");
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
                    PreparedStatement prep = DbHandler.getInstance().prepareStatement(
                            "select reward_cbills,reward_xp,stat_solo,stat_kmdd,stat_comp,player_data_id,match_data_id from personal_matchdata");
                    ResultSet rs = prep.executeQuery();
                    while (rs.next()) {
                        String reward_cbills = rs.getString(1);
                        String reward_xp = rs.getString(2);
                        String solo_kills = rs.getString(3);
                        String kmdds = rs.getString(4);
                        String comps = rs.getString(5);
                        int player_id = rs.getInt(6);
                        int match_id = rs.getInt(7);
                        new PersonalMatchRecord(player_id, match_id, reward_cbills, reward_xp, solo_kills, kmdds, comps);
                        updateProgress(allRecords.size(), totalWork);
                        updateMessage("(" + allRecords.size() + "/" + totalWork + ")");
                    }
                    rs.close();
                    prep.close();
                } catch (Exception e) {
                    Logger.alertPopup("loading all personalrecords FAILED");
                    Logger.error(e);
                }
                return null;
            }
        };
    }

    @Override
    public String getPreloadCaption() {
        return "Personal data records";
    }
}
