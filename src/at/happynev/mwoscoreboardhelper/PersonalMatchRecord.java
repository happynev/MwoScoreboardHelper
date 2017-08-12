package at.happynev.mwoscoreboardhelper;

import at.happynev.mwoscoreboardhelper.stat.StatType;
import at.happynev.mwoscoreboardhelper.tracer.RewardInfoTracer;
import at.happynev.mwoscoreboardhelper.tracer.TraceHelpers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Nev on 20.01.2017.
 */
public class PersonalMatchRecord{
    private final int playerId;
    private final long timestamp;
    private final Map<StatType, String> matchValues = new TreeMap<>();
    private int matchId;

    public PersonalMatchRecord(int playerId, int matchId) throws IllegalArgumentException, SQLException {
        this.playerId = playerId;
        this.matchId = matchId;
        PreparedStatement prep = DbHandler.getInstance().prepareStatement(
                "select pm.reward_cbills,pm.reward_xp,pm.stat_solo,pm.stat_kmdd,pm.stat_comp,m.matchtime " +
                        "from personal_matchdata pm, match_data m " +
                        "where pm.player_data_id=? and pm.match_data_id=? and pm.match_data_id=m.id");
        prep.setInt(1, playerId);
        prep.setInt(2, matchId);
        ResultSet rs = prep.executeQuery();
        try {
            if (rs.next()) {
                matchValues.put(StatType.REWARD_CBILLS, rs.getString(1));
                matchValues.put(StatType.REWARD_XP, rs.getString(2));
                matchValues.put(StatType.SOLO_KILLS, rs.getString(3));
                matchValues.put(StatType.KMDDS, rs.getString(4));
                matchValues.put(StatType.COMPONENT_DESTROYED, rs.getString(5));
                timestamp = rs.getTimestamp(6).getTime();
            } else {
                throw new IllegalArgumentException("Personal Record for " + playerId + "/" + matchId + " not found");
            }
        } finally {
            rs.close();
        }
    }

    private PersonalMatchRecord(int playerId) {
        this.playerId = playerId;
        matchId = -1;
        timestamp = 0;
        matchValues.put(StatType.REWARD_CBILLS, "0");
        matchValues.put(StatType.REWARD_XP, "0");
        matchValues.put(StatType.SOLO_KILLS, "0");
        matchValues.put(StatType.KMDDS, "0");
        matchValues.put(StatType.COMPONENT_DESTROYED, "0");
    }

    public PersonalMatchRecord(PlayerRuntime player, RewardInfoTracer info, MatchRuntime match) throws IllegalArgumentException, SQLException {
        playerId = player.getId();
        if (!info.getFinished()) {
            throw new IllegalArgumentException("RewardInfoTracer is not ready");
        }//select count(*),sum(reward_cbills),sum(reward_xp),sum(stat_solo),sum(stat_kmdd),sum(stat_comp) from personal_matchdata where has_rewards=true
        timestamp = match.getTimestamp();
        if (info == null) {
            throw new IllegalArgumentException("no reward info tracer");
        } else {
            int tmpSolo = 0;
            int tmpKmdd = 0;
            int tmpComp = 0;
            for (int i = 0; i < 9; i++) {
                String valuename = TraceHelpers.guessValue(info.getPerformanceName(i), TraceHelpers.ValueList.MATCHPERFORMANCE.getItems());
                if (valuename.equals("SOLO KILL")) {
                    tmpSolo = info.getPerformanceValue(i);
                } else if (valuename.equals("KILL MOST DAMAGE DEALT")) {
                    tmpKmdd = info.getPerformanceValue(i);
                } else if (valuename.equals("COMPONENT DESTROYED")) {
                    tmpComp = info.getPerformanceValue(i);
                }
            }
            matchValues.put(StatType.REWARD_CBILLS, "" + info.getCbills());
            matchValues.put(StatType.REWARD_XP, "" + info.getXp());
            matchValues.put(StatType.SOLO_KILLS, "" + tmpSolo);
            matchValues.put(StatType.KMDDS, "" + tmpKmdd);
            matchValues.put(StatType.COMPONENT_DESTROYED, "" + tmpComp);
            SessionRuntime.sessionPersonalRecords.add(this);
        }
    }

    public static PersonalMatchRecord getReferenceRecord(int playerId) {
        return new PersonalMatchRecord(playerId);
    }

    public Map<StatType, String> getMatchValues() {
        return matchValues;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getRewardsCbills() {
        return Integer.parseInt(matchValues.get(StatType.REWARD_CBILLS));
    }

    public int getRewardsXp() {
        return Integer.parseInt(matchValues.get(StatType.REWARD_XP));
    }

    public int getSoloKills() {
        return Integer.parseInt(matchValues.get(StatType.SOLO_KILLS));
    }

    public int getKmdds() {
        return Integer.parseInt(matchValues.get(StatType.KMDDS));
    }

    public int getComponentDestroyed() {
        return Integer.parseInt(matchValues.get(StatType.COMPONENT_DESTROYED));
    }

    public void saveData(int matchId) throws SQLException {
        int oldId = matchId;
        this.matchId = matchId;
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

    public void delete() throws SQLException {
        //PreparedStatement prep = DbHandler.getInstance().prepareStatement("delete from player_matchdata where player_data_id=? and match_data_id=?");
        //cascaded from match or player
    }

    public int getMatchId() {
        return matchId;
    }

    public int getPlayerId() {
        return playerId;
    }
}
