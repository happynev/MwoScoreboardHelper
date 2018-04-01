package at.happynev.mwoscoreboardhelper;

import at.happynev.mwoscoreboardhelper.isenleaderboard.IsenLeaderboard;
import at.happynev.mwoscoreboardhelper.isenleaderboard.IsenLeaderboardResult;
import at.happynev.mwoscoreboardhelper.isenleaderboard.IsenSeasonData;
import at.happynev.mwoscoreboardhelper.preloader.Preloadable;
import at.happynev.mwoscoreboardhelper.stat.StatType;
import at.happynev.mwoscoreboardhelper.tracer.PlayerInfoTracer;
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
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Nev on 20.01.2017.
 */
public class PlayerMatchRecord implements Preloadable {
    private final static ObservableMap<String, PlayerMatchRecord> allRecords = FXCollections.observableHashMap();
    private final static IntegerBinding totalRecords = Bindings.size(allRecords);
    private final int playerId;
    private final boolean isEnemy;
    private final boolean isWinner;
    private final boolean isLoser;
    private final long timestamp;
    private final Map<StatType, String> matchValues = new TreeMap<>();
    private int matchId;

    private PlayerMatchRecord(int playerId, int matchId, String mech, String status, int matchScore, int kills, int assists, int damage, int ping, boolean isEnemy, long timestamp, String matchResult, String gameMode, String map, Map<StatType, String> extraStats) {
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
        matchValues.put(StatType.GAMEMODE, gameMode);
        matchValues.put(StatType.MAP, map);
        matchValues.put(StatType.STATUS, status);
        if (extraStats != null) {
            matchValues.putAll(extraStats);
        }

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
        String status = "UNKNOWN";
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

    public static PlayerMatchRecord createFromTrace(PlayerRuntime player, PlayerInfoTracer info, MatchRuntime match, boolean isEnemy) {
        int playerId = player.getId();
        int matchId = match.getId();
        if (!info.getFinished()) {
            throw new IllegalArgumentException("PlayerInfoTracer is not ready");
        }
        String mech = MechRuntime.findMatchingMech(info.getMech());
        String tmpst = ValueHelpers.guessValue(info.getStatus().replaceAll(".*DEAD.*", "DEAD").replaceAll(".*ALIVE.*", "ALIVE"), Arrays.asList("DEAD", "ALIVE"));
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
        String gameMode = match.getGameMode();
        String map = match.getMap();
        Map<StatType, String> extraStats = new TreeMap<>();
        IsenLeaderboardResult leaderboardData = IsenLeaderboard.getInstance().getLeaderboardData(player.getPilotname());
        if (leaderboardData != null) {
            IsenSeasonData configuredSeason = leaderboardData.getConfiguredSeasonData();
            extraStats.put(StatType.ISEN_ADJSCORE, configuredSeason.getAdjScore().toPlainString());
            extraStats.put(StatType.ISEN_AVGSCORE, configuredSeason.getAvgScore().toPlainString());
            extraStats.put(StatType.ISEN_GAMESPLAYED, "" + configuredSeason.getGamesPlayed());
            extraStats.put(StatType.ISEN_KDRATIO, configuredSeason.getKdratio().toPlainString());
            extraStats.put(StatType.ISEN_PERCENTILE, "" + configuredSeason.getPercentile());
            extraStats.put(StatType.ISEN_RANK, "" + configuredSeason.getRank());
            extraStats.put(StatType.ISEN_SURVIVALRATE, "" + configuredSeason.getSurviveRate());
            extraStats.put(StatType.ISEN_WLRATIO, configuredSeason.getWinloss().toPlainString());
        }
        return new PlayerMatchRecord(playerId, matchId, mech, status, matchScore, kills, assists, damage, ping, isEnemy, timestamp, matchResult, gameMode, map, extraStats);
    }

    public static ObservableMap<String, PlayerMatchRecord> getAllRecords() {
        return allRecords;
    }

    public static PlayerMatchRecord getInstance(int playerId, int matchId) {
        String key = playerId + "_" + matchId;
        if (allRecords.containsKey(key)) {
            return allRecords.get(key);
        } else {
            throw new IllegalArgumentException("PlayerMatchRecord " + key + " doesn't exist");
        }
    }

    public static PlayerMatchRecord getReferenceRecord(boolean isEnemy, int matchId) {
        return new PlayerMatchRecord(isEnemy, -1, matchId);
    }

    //for player tab/mechlist
    public static PlayerMatchRecord getReferenceRecord(int playerId, String mechVariant) {
        PlayerMatchRecord pmr = new PlayerMatchRecord(false, playerId, -1);
        MechRuntime mr = MechRuntime.getMechByShortName(mechVariant);
        pmr.getMatchValues().putAll(mr.getDerivedValues());
        return pmr;
    }

    public static Preloadable getPreloaderInstance() {
        return getReferenceRecord(false, -1);
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
        PersonalMatchRecord pers = PersonalMatchRecord.getInstance(playerId, matchId);
        if (pers != null) {
            matchValues.putAll(pers.getMatchValues());
            return true;
        } else {
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

    public void delete() {
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

    @Override
    public ObservableIntegerValue loadedCountProperty() {
        return totalRecords;
    }

    @Override
    public ObservableIntegerValue totalCountProperty() {
        if (totalRecords.get() == 0) {
            int count = 0;
            try {
                PreparedStatement prep = DbHandler.getInstance().prepareStatement("select count(*) from player_matchdata");
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
                            "select pm.mech,pm.status,pm.score,pm.kills,pm.assists,pm.damage,pm.ping,pm.enemy," +
                                    "m.matchtime,m.matchresult,pm.player_data_id, pm.match_data_id, m.gamemode, m.map " +
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
                        String gameMode = rs.getString(13);
                        String map = rs.getString(14);
                        //saves itself to allRecords map
                        new PlayerMatchRecord(playerId, matchId, mech, status, matchScore, kills, assists, damage, ping, isEnemy, timestamp, matchResult, gameMode, map, null);
                        updateProgress(allRecords.size(), totalWork);
                        updateMessage("(" + allRecords.size() + "/" + totalWork + ")");
                    }
                    rs.close();
                    prep.close();
                } catch (Exception e) {
                    Logger.alertPopup("loading all matchrecords FAILED");
                    Logger.error(e);
                }
                return null;
            }
        };
    }

    @Override
    public String getPreloadCaption() {
        return "Match data Records";
    }
}
