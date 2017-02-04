package at.happynev.mwoscoreboardhelper;

import at.happynev.mwoscoreboardhelper.tracer.PlayerInfoTracer;
import at.happynev.mwoscoreboardhelper.tracer.TraceHelpers;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.SimpleStringProperty;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Nev on 20.01.2017.
 */
public class PlayerMatchRecord {
    private final int playerId;
    private final int matchId;

    private final boolean isEnemy;
    private final long timestamp;
    private final Map<MatchStat, StringExpression> matchValues = new TreeMap<>();

    public PlayerMatchRecord(int playerId, int matchId) throws Exception {
        this.playerId = playerId;
        this.matchId = matchId;
        PreparedStatement prep = DbHandler.getInstance().prepareStatement(
                "select pm.mech,pm.status,pm.score,pm.kills,pm.assists,pm.damage,pm.ping,pm.enemy,m.matchtime " +
                        "from player_matchdata pm, match_data m " +
                        "where pm.player_data_id=? and pm.match_data_id=? and pm.match_data_id=m.id");
        prep.setInt(1, playerId);
        prep.setInt(2, matchId);
        ResultSet rs = prep.executeQuery();
        if (rs.next()) {
            String mech = rs.getString(1);
            String status = rs.getString(2);
            int matchScore = rs.getInt(3);
            int kills = rs.getInt(4);
            int assists = rs.getInt(5);
            int damage = rs.getInt(6);
            int ping = rs.getInt(7);
            isEnemy = rs.getBoolean(8);
            timestamp = rs.getTimestamp(9).getTime();
            matchValues.put(MatchStat.MATCHMECH, new SimpleStringProperty(mech));
            matchValues.put(MatchStat.MATCHASSISTS, new SimpleStringProperty("" + assists));
            matchValues.put(MatchStat.MATCHDAMAGE, new SimpleStringProperty("" + damage));
            matchValues.put(MatchStat.MATCHKILLS, new SimpleStringProperty("" + kills));
            matchValues.put(MatchStat.MATCHPING, new SimpleStringProperty("" + ping));
            matchValues.put(MatchStat.MATCHSCORE, new SimpleStringProperty("" + matchScore));
            matchValues.put(MatchStat.MATCHSTATUS, new SimpleStringProperty(status));
        } else {
            throw new Exception("Match Record for " + playerId + "/" + matchId + " not found");
        }
        rs.close();
    }

    private PlayerMatchRecord(boolean isEnemy) {
        playerId = -1;
        matchId = -1;
        String mech = "XXX-1X";
        String status = "DEAD";
        int matchScore = 1000;
        int kills = 10;
        int assists = 10;
        int damage = 1000;
        int ping = 100;
        matchValues.put(MatchStat.MATCHMECH, new SimpleStringProperty(mech));
        matchValues.put(MatchStat.MATCHASSISTS, new SimpleStringProperty("" + assists));
        matchValues.put(MatchStat.MATCHDAMAGE, new SimpleStringProperty("" + damage));
        matchValues.put(MatchStat.MATCHKILLS, new SimpleStringProperty("" + kills));
        matchValues.put(MatchStat.MATCHPING, new SimpleStringProperty("" + ping));
        matchValues.put(MatchStat.MATCHSCORE, new SimpleStringProperty("" + matchScore));
        matchValues.put(MatchStat.MATCHSTATUS, new SimpleStringProperty(status));
        this.isEnemy = isEnemy;
        timestamp = 0;
    }

    public PlayerMatchRecord(PlayerRuntime player, PlayerInfoTracer info, MatchRuntime match, boolean isEnemy) throws IllegalArgumentException, SQLException {
        playerId = player.getId();
        matchId = match.getId();
        if (!info.getFinished()) {
            throw new IllegalArgumentException("PlayerInfoTracer is not ready");
        }
        String mech = MechRuntime.findMatchingMech(info.getMech());
        String tmpst = TraceHelpers.guessValue(info.getStatus().replaceAll(".*DEAD.*", "DEAD").replaceAll(".*ALIVE.*", "ALIVE"), Arrays.asList("DEAD", "ALIVE"));
        String status = "DEAD";
        if (tmpst.matches("DEAD|ALIVE")) {
            status = tmpst;
        }
        int matchScore = info.getMatchScore();
        int kills = info.getKills();
        int assists = info.getAssists();
        int damage = info.getDamage();
        int ping = info.getPing();
        timestamp = match.getTimestamp();
        this.isEnemy = isEnemy;
        PreparedStatement prep = DbHandler.getInstance().prepareStatement("insert into player_matchdata(player_data_id,match_data_id,mech,status,score,kills,assists,damage,ping,enemy) values(?,?,?,?,?,?,?,?,?,?)");
        prep.setInt(1, playerId);
        prep.setInt(2, matchId);
        prep.setString(3, mech);
        prep.setString(4, status);
        prep.setInt(5, matchScore);
        prep.setInt(6, kills);
        prep.setInt(7, assists);
        prep.setInt(8, damage);
        prep.setInt(9, ping);
        prep.setBoolean(10, this.isEnemy);
        prep.executeUpdate();
        matchValues.put(MatchStat.MATCHMECH, new SimpleStringProperty(mech));
        matchValues.put(MatchStat.MATCHASSISTS, new SimpleStringProperty("" + assists));
        matchValues.put(MatchStat.MATCHDAMAGE, new SimpleStringProperty("" + damage));
        matchValues.put(MatchStat.MATCHKILLS, new SimpleStringProperty("" + kills));
        matchValues.put(MatchStat.MATCHPING, new SimpleStringProperty("" + ping));
        matchValues.put(MatchStat.MATCHSCORE, new SimpleStringProperty("" + matchScore));
        matchValues.put(MatchStat.MATCHSTATUS, new SimpleStringProperty(status));
        MechRuntime mr = MechRuntime.getMechByShortName(mech);
        if (mr != null) {
            matchValues.put(MatchStat.MATCHTONS, new SimpleStringProperty("" + mr.getTons()));
        }
    }

    public static PlayerMatchRecord getReferenceRecord(boolean isEnemy) {
        return new PlayerMatchRecord(isEnemy);
    }

    public Map<MatchStat, StringExpression> getMatchValues() {
        return matchValues;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isEnemy() {
        return isEnemy;
    }

    public String getMech() {
        return matchValues.get(MatchStat.MATCHMECH).get();
    }

    public int getMatchScore() {
        return Integer.parseInt(matchValues.get(MatchStat.MATCHSCORE).get());
    }

    public int getKills() {
        return Integer.parseInt(matchValues.get(MatchStat.MATCHKILLS).get());
    }

    public int getAssists() {
        return Integer.parseInt(matchValues.get(MatchStat.MATCHASSISTS).get());
    }

    public int getDamage() {
        return Integer.parseInt(matchValues.get(MatchStat.MATCHDAMAGE).get());
    }

    public String getStatus() {
        return matchValues.get(MatchStat.MATCHSTATUS).get();
    }

    public int getPing() {
        return Integer.parseInt(matchValues.get(MatchStat.MATCHPING).get());
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
