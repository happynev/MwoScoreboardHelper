package at.happynev.mwoscoreboardhelper;

import at.happynev.mwoscoreboardhelper.stat.StatType;
import at.happynev.mwoscoreboardhelper.tracer.PlayerInfoTracer;
import at.happynev.mwoscoreboardhelper.tracer.TraceHelpers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by Nev on 20.01.2017.
 */
public class PlayerMatchRecord {
    private final static Map<String, PlayerMatchRecord> allRecords = new HashMap<>();
    private final int playerId;
    private final boolean isEnemy;
    private final boolean isWinner;
    private final boolean isLoser;
    private final long timestamp;
    private final Map<StatType, String> matchValues = new TreeMap<>();
    private int matchId;

    private PlayerMatchRecord(int playerId, int matchId, String mech, String status, int matchScore, int kills, int assists, int damage, int ping, boolean isEnemy, long timestamp, String matchResult) {
        this.playerId = playerId;
        this.matchId = matchId;
        this.timestamp = timestamp;
        this.isEnemy = isEnemy;
        matchValues.putAll(MechRuntime.getMechByShortName(mech).getDerivedValues());
        matchValues.put(StatType.ASSISTS, "" + assists);
        matchValues.put(StatType.DAMAGE, "" + damage);
        matchValues.put(StatType.KILLS, "" + kills);
        matchValues.put(StatType.PING, "" + ping);
        matchValues.put(StatType.SCORE, "" + matchScore);
        matchValues.put(StatType.STATUS, status);
        if ("DEFEAT".equals(matchResult)) {
            isWinner = isEnemy;
            isLoser = !isWinner;
        } else if ("VICTORY".equals(matchResult)) {
            isWinner = !isEnemy;
            isLoser = !isWinner;
        } else {
            //TIE
            isWinner = false;
            isLoser = false;
        }
        if (isWinner) {
            matchValues.put(StatType.WINS, "1");
        } else if (isLoser) {
            matchValues.put(StatType.LOSSES, "1");
        }
        matchValues.put(StatType.MATCHES, "1");
        mergePersonalStats();
        allRecords.put(playerId + "_" + matchId, this);
    }

    private PlayerMatchRecord(boolean isEnemy, int playerId, int matchId) {
        this.playerId = playerId;
        this.matchId = matchId;
        String status = "DEAD";
        int matchScore = 1000;
        int kills = 10;
        int assists = 10;
        int damage = 1000;
        int ping = 100;
        matchValues.putAll(MechRuntime.getReferenceMech().getDerivedValues());
        matchValues.put(StatType.ASSISTS, "" + assists);
        matchValues.put(StatType.DAMAGE, "" + damage);
        matchValues.put(StatType.KILLS, "" + kills);
        matchValues.put(StatType.PING, "" + ping);
        matchValues.put(StatType.SCORE, "" + matchScore);
        matchValues.put(StatType.STATUS, status);
        this.isEnemy = isEnemy;
        this.isWinner = true;
        this.isLoser = false;
        if (isWinner) {
            matchValues.put(StatType.WINS, "1");
        } else if (isLoser) {
            matchValues.put(StatType.LOSSES, "1");
        }
        matchValues.put(StatType.MATCHES, "1");
        timestamp = 0;
        try {
            mergePersonalStats();
        } catch (Exception e) {
            Logger.log("failed to merge personal stats");
            Logger.error(e);
        }
    }

    public static PlayerMatchRecord createInstance(PlayerRuntime player, PlayerInfoTracer info, MatchRuntime match, boolean isEnemy) {
        int playerId = player.getId();
        int matchId = match.getId();
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
        long timestamp = match.getTimestamp();
        String matchResult = match.getMatchResult();
        return new PlayerMatchRecord(playerId, matchId, mech, status, matchScore, kills, assists, damage, ping, isEnemy, timestamp, matchResult);
    }

    public static synchronized Collection<PlayerMatchRecord> getAllRecords() {
        if (allRecords.isEmpty()) {
            Logger.log("loading all matchrecords");
            try {
                PreparedStatement prep = DbHandler.getInstance().prepareStatement(
                        "select pm.mech,pm.status,pm.score,pm.kills,pm.assists,pm.damage,pm.ping,pm.enemy,m.matchtime,m.matchresult,pm.player_data_id, pm.match_data_id " +
                                "from player_matchdata pm, match_data m where pm.match_data_id=m.id");
                ResultSet rs = prep.executeQuery();
                while (rs.next()) {
                    String mech = rs.getString(1);
                    String status = rs.getString(2);
                    int matchScore = rs.getInt(3);
                    int kills = rs.getInt(4);
                    int assists = rs.getInt(5);
                    int damage = rs.getInt(6);
                    int ping = rs.getInt(7);
                    boolean isEnemy = rs.getBoolean(8);
                    long timestamp = rs.getTimestamp(9).getTime();
                    String matchResult = rs.getString(10);
                    int playerId = rs.getInt(11);
                    int matchId = rs.getInt(12);
                    //saves itself to allRecords map
                    new PlayerMatchRecord(playerId, matchId, mech, status, matchScore, kills, assists, damage, ping, isEnemy, timestamp, matchResult);
                }
                rs.close();
                prep.close();
                Logger.log("loading all " + allRecords.size() + " matchrecords finished");
            } catch (Exception e) {
                Logger.alertPopup("loading all matchrecords FAILED");
                Logger.error(e);
            }
        }
        return allRecords.values();
    }

    public static PlayerMatchRecord getInstance(int playerId, int matchId) {
        getAllRecords();
        return allRecords.get(playerId + "_" + matchId);
    }

    public static PlayerMatchRecord getReferenceRecord(boolean isEnemy) {
        return new PlayerMatchRecord(isEnemy, -1, -1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlayerMatchRecord that = (PlayerMatchRecord) o;

        if (playerId != that.playerId) return false;
        return matchId == that.matchId;
    }

    @Override
    public int hashCode() {
        int result = playerId;
        result = 31 * result + matchId;
        return result;
    }

    public void saveData(int matchId) throws IllegalArgumentException, SQLException {
        //matchId provided externally because it may have changed because of duplicate match detection
        this.matchId = matchId;
        try {
            PreparedStatement prep = DbHandler.getInstance().prepareStatement("insert into player_matchdata(player_data_id,match_data_id,mech,status,score,kills,assists,damage,ping,enemy) values(?,?,?,?,?,?,?,?,?,?)");
            prep.setInt(1, playerId);
            prep.setInt(2, matchId);
            prep.setString(3, matchValues.get(StatType.MECH_VARIANT));
            prep.setString(4, matchValues.get(StatType.STATUS));
            prep.setInt(5, Integer.parseInt(matchValues.get(StatType.SCORE)));
            prep.setInt(6, Integer.parseInt(matchValues.get(StatType.KILLS)));
            prep.setInt(7, Integer.parseInt(matchValues.get(StatType.ASSISTS)));
            prep.setInt(8, Integer.parseInt(matchValues.get(StatType.DAMAGE)));
            prep.setInt(9, Integer.parseInt(matchValues.get(StatType.PING)));
            prep.setBoolean(10, this.isEnemy);
            prep.executeUpdate();
        } catch (SQLException e) {
            if (e.getMessage().contains("PRIMARY_KEY")) {
                //ok, don't overwrite anything
            } else {
                throw e;
            }
        }
        //reload personal stats
        mergePersonalStats();
    }

    public boolean mergePersonalStats() {
        try {
            PersonalMatchRecord pers = new PersonalMatchRecord(playerId, matchId);
            matchValues.putAll(pers.getMatchValues());
            return true;
        } catch (IllegalArgumentException | SQLException e) {
            return false;
        }
    }

    public Map<StatType, String> getMatchValues() {
        return matchValues;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isEnemy() {
        return isEnemy;
    }

    public MechRuntime getMech() {
        return MechRuntime.getMechByShortName(matchValues.get(StatType.MECH_VARIANT));
    }

    public int getMatchScore() {
        return Integer.parseInt(matchValues.get(StatType.SCORE));
    }

    public int getKills() {
        return Integer.parseInt(matchValues.get(StatType.KILLS));
    }

    public int getAssists() {
        return Integer.parseInt(matchValues.get(StatType.ASSISTS));
    }

    public int getDamage() {
        return Integer.parseInt(matchValues.get(StatType.DAMAGE));
    }

    public String getStatus() {
        return matchValues.get(StatType.STATUS);
    }

    public int getPing() {
        return Integer.parseInt(matchValues.get(StatType.PING));
    }

    public void delete() throws SQLException {
        allRecords.remove(playerId + "_" + matchId);
        //PreparedStatement prep = DbHandler.getInstance().prepareStatement("delete from player_matchdata where player_data_id=? and match_data_id=?");
        //cascaded from match or player
    }

    public int getMatchId() {
        return matchId;
    }

    public int getPlayerId() {
        return playerId;
    }

    public boolean isWinner() {
        return isWinner;
    }

    public boolean isLoser() {
        return isLoser;
    }
}
