package at.happynev.mwoscoreboardhelper;

import at.happynev.mwoscoreboardhelper.tracer.RewardInfoTracer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Nev on 20.01.2017.
 */
public class PersonalMatchRecord {
    private final int playerId;
    private final boolean hasRewards;
    private final long timestamp;
    private final int rewardsCbills;
    private final int rewardsXp;
    private final int soloKills;
    private final int kmdds;
    private final int componentDestroyed;
    private int ratingTeam;
    private int ratingEnemy;
    private int ratingMatch;
    private int matchId;

    public PersonalMatchRecord(int playerId, int matchId) throws Exception {
        this.playerId = playerId;
        this.matchId = matchId;
        PreparedStatement prep = DbHandler.getInstance().prepareStatement(
                "select pm.has_rewards,pm.reward_cbills,pm.reward_xp,pm.stat_solo,pm.stat_kmdd,pm.stat_comp,pm.rating_team,pm.rating_enemy,pm.rating_match,m.matchtime " +
                        "from personal_matchdata pm, match_data m " +
                        "where pm.player_data_id=? and pm.match_data_id=? and pm.match_data_id=m.id");
        prep.setInt(1, playerId);
        prep.setInt(2, matchId);
        ResultSet rs = prep.executeQuery();
        if (rs.next()) {
            hasRewards = rs.getBoolean(1);
            rewardsCbills = rs.getInt(2);
            rewardsXp = rs.getInt(3);
            soloKills = rs.getInt(4);
            kmdds = rs.getInt(5);
            componentDestroyed = rs.getInt(6);
            ratingTeam = rs.getInt(7);
            ratingEnemy = rs.getInt(8);
            ratingMatch = rs.getInt(9);
            timestamp = rs.getTimestamp(10).getTime();
        } else {
            throw new Exception("Personal Record for " + playerId + "/" + matchId + " not found");
        }
        rs.close();
    }

    private PersonalMatchRecord(int playerId) {
        this.playerId = playerId;
        matchId = -1;
        timestamp = 0;
        hasRewards = false;
        rewardsCbills = 0;
        rewardsXp = 0;
        soloKills = 0;
        kmdds = 0;
        componentDestroyed = 0;
        ratingTeam = 0;
        ratingEnemy = 0;
        ratingMatch = 0;
    }

    public PersonalMatchRecord(PlayerRuntime player, MatchRuntime match) throws IllegalArgumentException, SQLException {
        this(player, null, match);
    }

    public PersonalMatchRecord(PlayerRuntime player, RewardInfoTracer info, MatchRuntime match) throws IllegalArgumentException, SQLException {
        playerId = player.getId();
        if (!info.getFinished()) {
            throw new IllegalArgumentException("RewardInfoTracer is not ready");
        }//select count(*),sum(reward_cbills),sum(reward_xp),sum(stat_solo),sum(stat_kmdd),sum(stat_comp) from personal_matchdata where has_rewards=true
        timestamp = match.getTimestamp();
        ratingTeam = 0;
        ratingEnemy = 0;
        ratingMatch = 0;
        if (info == null) {
            hasRewards = false;
            rewardsCbills = 0;
            rewardsXp = 0;
            soloKills = 0;
            kmdds = 0;
            componentDestroyed = 0;
        } else {
            hasRewards = true;
            rewardsCbills = info.getCbills();
            rewardsXp = info.getXp();
            int tmpSolo = 0;
            int tmpKmdd = 0;
            int tmpComp = 0;
            for (int i = 0; i < 9; i++) {
                String valuename = info.getPerformanceName(i);
                if (valuename.equals("SOLO KILL")) {
                    tmpSolo = info.getPerformanceValue(i);
                } else if (valuename.equals("KILL MOST DAMAGE DEALT")) {
                    tmpKmdd = info.getPerformanceValue(i);
                } else if (valuename.equals("COMPONENT DESTROYED")) {
                    tmpComp = info.getPerformanceValue(i);
                }
            }
            soloKills = tmpSolo;
            kmdds = tmpKmdd;
            componentDestroyed = tmpComp;
        }
    }

    public static PersonalMatchRecord getReferenceRecord(int playerId) {
        return new PersonalMatchRecord(playerId);
    }

    public boolean isHasRewards() {
        return hasRewards;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getRewardsCbills() {
        return rewardsCbills;
    }

    public int getRewardsXp() {
        return rewardsXp;
    }

    public int getSoloKills() {
        return soloKills;
    }

    public int getKmdds() {
        return kmdds;
    }

    public int getComponentDestroyed() {
        return componentDestroyed;
    }

    public int getRatingTeam() {
        return ratingTeam;
    }

    public void setRatingTeam(int ratingTeam) {
        this.ratingTeam = ratingTeam;
        updateRecord("rating_team", ratingTeam);
    }

    public int getRatingEnemy() {
        return ratingEnemy;
    }

    public void setRatingEnemy(int ratingEnemy) {
        this.ratingEnemy = ratingEnemy;
        updateRecord("rating_enemy", ratingEnemy);
    }

    public int getRatingMatch() {
        return ratingMatch;
    }

    public void setRatingMatch(int ratingMatch) {
        this.ratingMatch = ratingMatch;
        updateRecord("rating_match", ratingMatch);
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
                "has_rewards,reward_cbills,reward_xp,stat_solo,stat_kmdd,stat_comp,rating_team,rating_enemy,rating_match) values(?,?,?,?,?,?,?,?,?,?,?)");
        prep.setInt(1, playerId);
        prep.setInt(2, matchId);
        prep.setBoolean(3, hasRewards);
        prep.setInt(4, rewardsCbills);
        prep.setInt(5, rewardsXp);
        prep.setInt(6, soloKills);
        prep.setInt(7, kmdds);
        prep.setInt(8, componentDestroyed);
        prep.setInt(9, ratingTeam);
        prep.setInt(10, ratingEnemy);
        prep.setInt(11, ratingMatch);
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
