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
    //private final boolean isEnemy;

    public PlayerMatchRecord(int playerId, int matchId) throws Exception {
        this.playerId = playerId;
        this.matchId = matchId;
        PreparedStatement prep = DbHandler.getInstance().prepareStatement("select mech,status,score,kills,assists,damage,ping from player_matchdata where player_data_id=? and match_data_id=?");
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
        } else {
            throw new Exception("Match Record for " + playerId + "/" + matchId + " not found");
        }
        rs.close();
    }

    private PlayerMatchRecord() {
        playerId = -1;
        matchId = -1;
        mech = "";
        status = "";
        matchScore = 0;
        kills = 0;
        assists = 0;
        damage = 0;
        ping = 0;
    }

    public PlayerMatchRecord(PlayerRuntime player, PlayerInfoTracer info, MatchRuntime match) throws IllegalArgumentException, SQLException {
        playerId = player.getId();
        matchId = match.getId();
        if (!info.getFinished()) {
            throw new IllegalArgumentException("PlayerInfoTracer is not ready");
        }
        mech = MechRuntime.findMatchingMech(info.getMech());
        status = TraceHelpers.guessValue(info.getStatus().replaceAll(".*DEAD.*", "DEAD").replaceAll(".*ALIVE.*", "ALIVE"), Arrays.asList("DEAD", "ALIVE"));
        matchScore = info.getMatchScore();
        kills = info.getKills();
        assists = info.getAssists();
        damage = info.getDamage();
        ping = info.getPing();
        PreparedStatement prep = DbHandler.getInstance().prepareStatement("insert into player_matchdata(player_data_id,match_data_id,mech,status,score,kills,assists,damage,ping) values(?,?,?,?,?,?,?,?,?)");
        prep.setInt(1, playerId);
        prep.setInt(2, matchId);
        prep.setString(3, mech);
        prep.setString(4, status);
        prep.setInt(5, matchScore);
        prep.setInt(6, kills);
        prep.setInt(7, assists);
        prep.setInt(8, damage);
        prep.setInt(9, ping);
        prep.executeUpdate();
    }

    public static PlayerMatchRecord getDummyInstance() {
        return new PlayerMatchRecord();
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
