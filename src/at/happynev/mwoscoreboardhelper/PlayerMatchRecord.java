package at.happynev.mwoscoreboardhelper;

import at.happynev.mwoscoreboardhelper.tracer.PlayerInfoTracer;
import at.happynev.mwoscoreboardhelper.tracer.TraceHelpers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * Created by Nev on 20.01.2017.
 */
public class PlayerMatchRecord {
    private final int playerId;
    private final int matchId;

    private final String mech;
    private final String status;
    private final int matchScore;
    private final int kills;
    private final int assists;
    private final int damage;
    private final int ping;
    private final boolean isEnemy;
    private final long timestamp;

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
            mech = rs.getString(1);
            status = rs.getString(2);
            matchScore = rs.getInt(3);
            kills = rs.getInt(4);
            assists = rs.getInt(5);
            damage = rs.getInt(6);
            ping = rs.getInt(7);
            isEnemy = rs.getBoolean(8);
            timestamp = rs.getTimestamp(9).getTime();
        } else {
            throw new Exception("Match Record for " + playerId + "/" + matchId + " not found");
        }
        rs.close();
    }

    private PlayerMatchRecord(boolean isEnemy) {
        playerId = -1;
        matchId = -1;
        mech = "XXX-1X";
        status = "DEAD";
        matchScore = 1000;
        kills = 10;
        assists = 10;
        damage = 1000;
        ping = 100;
        this.isEnemy = isEnemy;
        timestamp = 0;
    }

    public PlayerMatchRecord(PlayerRuntime player, PlayerInfoTracer info, MatchRuntime match, boolean isEnemy) throws IllegalArgumentException, SQLException {
        playerId = player.getId();
        matchId = match.getId();
        if (!info.getFinished()) {
            throw new IllegalArgumentException("PlayerInfoTracer is not ready");
        }
        mech = MechRuntime.findMatchingMech(info.getMech());
        String tmpst = TraceHelpers.guessValue(info.getStatus().replaceAll(".*DEAD.*", "DEAD").replaceAll(".*ALIVE.*", "ALIVE"), Arrays.asList("DEAD", "ALIVE"));
        if (tmpst.matches("DEAD|ALIVE")) {
            status = tmpst;
        } else {
            status = "DEAD";//alive is white and traces very well
        }
        matchScore = info.getMatchScore();
        kills = info.getKills();
        assists = info.getAssists();
        damage = info.getDamage();
        ping = info.getPing();
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
    }

    public static PlayerMatchRecord getDummyInstance(boolean isEnemy) {
        return new PlayerMatchRecord(isEnemy);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isEnemy() {
        return isEnemy;
    }

    public String getMech() {
        return mech;
    }

    public int getMatchScore() {
        return matchScore;
    }

    public int getKills() {
        return kills;
    }

    public int getAssists() {
        return assists;
    }

    public int getDamage() {
        return damage;
    }

    public String getStatus() {
        return status;
    }

    public int getPing() {
        return ping;
    }

    public void delete() throws SQLException {
        //PreparedStatement prep = DbHandler.getInstance().prepareStatement("delete from player_matchdata where player_data_id=? and match_data_id=?");
        //cascaded from match or player
    }

    public int getMatchId() {
        return matchId;
    }
}
