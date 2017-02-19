package at.happynev.mwoscoreboardhelper;

import at.happynev.mwoscoreboardhelper.tracer.*;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Nev on 15.01.2017.
 */
public class MatchRuntime {

    private static final int TRACE_TIMEOUT = 1000 * 30;
    private static final long maxMatchTimeDifference = 1000 * 60 * (15 + 4);//15 minutes matchtime+4 to account for pre/post match;
    private final SimpleStringProperty matchName = new SimpleStringProperty("");
    private final SimpleStringProperty map = new SimpleStringProperty("");
    private final SimpleStringProperty server = new SimpleStringProperty("");
    private final SimpleStringProperty gameMode = new SimpleStringProperty("");
    private final SimpleStringProperty formattedTimestamp = new SimpleStringProperty("");
    private final SimpleStringProperty battleTime = new SimpleStringProperty("");
    private final SimpleStringProperty matchResult = new SimpleStringProperty("");
    private final SimpleStringProperty mapTimeOfDay = new SimpleStringProperty("");
    private final SimpleDateFormat sdfDb = new SimpleDateFormat("yyyMMddHHmmss");
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final SimpleDateFormat sdfDir = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    private final ObservableList<PlayerRuntime> playersTeam = FXCollections.observableArrayList();
    private final ObservableList<PlayerRuntime> playersEnemy = FXCollections.observableArrayList();
    private final ObservableList<PlayerMatchRecord> playerRecords = FXCollections.observableArrayList();
    private final Map<Integer, SimpleStringProperty> preliminaryInfo = new HashMap<>(24);
    private final Set<String> gameModes = new HashSet<>(Arrays.asList("SKIRMISH", "DOMINATION", "ASSAULT", "CONQUEST", "INCURSION", "INVASION", "ESCORT"));
    private final Map<ScreenshotType, ScreenshotFileHandler> matchScreenshots = new HashMap<>();
    private final SimpleBooleanProperty tracingFinished = new SimpleBooleanProperty(false);
    private PersonalMatchRecord personalRecord = null;
    private ScreenshotType type;
    private boolean matchFinished;
    private int id = 0;
    private long timestamp = 0;

    public MatchRuntime(ScreenshotType _type) {
        type = _type;
        id = -1;
        map.set("SAMPLE MAP");
        server.set("welcome to the test server");
        gameMode.set("SETTINGS ASSAULT");
        timestamp = System.currentTimeMillis();
        formattedTimestamp.set(sdf.format(timestamp));
        battleTime.set("00:00");
        matchResult.set("VICTORY");
        matchFinished = ScreenshotType.QP_4SUMMARY == type;
        PlayerRuntime pr = PlayerRuntime.getReferencePlayer();
        playersTeam.add(pr);
        playerRecords.addAll(pr.getMatchRecords());
    }

    public MatchRuntime(final ScreenshotFileHandler screenshot) {
        id = -1;//until after duplicate check
        timestamp = screenshot.getTimestamp();
        formattedTimestamp.setValue(sdf.format(timestamp));
        type = screenshot.getType();
        matchFinished = ScreenshotType.QP_4SUMMARY == type;
        if (matchScreenshots.containsKey(type)) {
            Logger.warning("replacing " + type.toString());
        }
        matchScreenshots.put(type, screenshot);

        if (type == ScreenshotType.QP_1PREPARATION || type == ScreenshotType.QP_4SUMMARY) {
            setupMatchFinishListeners(screenshot);
        } else if (type == ScreenshotType.QP_3REWARDS) {
            setupRewardFinishListeners(screenshot);
        }
    }

    private MatchRuntime(int id) {
        try {
            PreparedStatement prep = DbHandler.getInstance().prepareStatement("select matchtime,gamemode,map,matchResult,matchname,battletime,maptimeofday from MATCH_DATA where id=?");
            prep.setInt(1, id);
            ResultSet rs = prep.executeQuery();
            if (rs.next()) {
                long _timestamp = rs.getTimestamp(1).getTime();
                String _gamemode = rs.getString(2);
                String _map = rs.getString(3);
                String _matchresult = rs.getString(4);
                String _matchname = rs.getString(5);
                String _battletime = rs.getString(6);
                String _maptod = rs.getString(7);
                timestamp = _timestamp;
                formattedTimestamp.setValue(sdf.format(new Date(_timestamp)));
                this.matchName.set(_matchname);
                if (_gamemode != null) this.gameMode.set(_gamemode);
                if (_map != null) this.map.set(_map);
                if (_matchresult != null) this.matchResult.set(_matchresult);
                if (_battletime != null) this.battleTime.set(_battletime);
                if (_maptod != null) this.mapTimeOfDay.set(_maptod);
                //initChangeListeners();
            } else {
                throw new Exception("Match with id " + id + " not found in DB");
            }
            rs.close();
            this.id = id;
            PreparedStatement prepPlayers = DbHandler.getInstance().prepareStatement("select player_data_id from PLAYER_MATCHDATA where match_data_id=?");
            prepPlayers.setInt(1, id);
            ResultSet rsPlayers = prepPlayers.executeQuery();
            while (rsPlayers.next()) {
                int player_id = rsPlayers.getInt(1);
                PlayerMatchRecord pmr = new PlayerMatchRecord(player_id, id);
                playerRecords.add(pmr);
                if (pmr.isEnemy()) {
                    playersEnemy.add(PlayerRuntime.getInstance(player_id));
                } else {
                    playersTeam.add(PlayerRuntime.getInstance(player_id));
                }
            }
            rsPlayers.close();
            int playerid = PlayerRuntime.getInstance(SettingsTabController.getPlayername()).getId();
            personalRecord = new PersonalMatchRecord(playerid, id);
            //TODO: check screenshot archive
        } catch (Exception e) {
            handleLoadError(e, null);
        }
    }

    public static MatchRuntime getReferenceMatch(ScreenshotType type) {
        return new MatchRuntime(type);
    }

    private static int calculateSimilarity(MatchRuntime oldMatch, MatchRuntime newMatch) {
        int matchScore = 0;
        int timeScore = (int) (maxMatchTimeDifference - Math.abs(newMatch.getTimestamp() - oldMatch.getTimestamp())) / (1000 * 60);
        if (oldMatch.getGameMode().equals(newMatch.getGameMode())) {
            matchScore += 8;
        } else {
            //gamemode is parsed with valuelist. match is either exact or wrong.
            matchScore -= 15;
        }
        if (oldMatch.getMap().equals(newMatch.getMap())) matchScore += 8;
        if (oldMatch.getBattleTime().equals(newMatch.getBattleTime())) matchScore += 10;
        //if (oldMatch.getMatchResult().equals(newMatch.getMatchResult())) matchScore += 3;
        //if (oldMatch.getMapTimeOfDay().equals(newMatch.getMapTimeOfDay())) matchScore += 3;
        int playerScore = 0;
        //Logger.log("time matchScore: " + timeScore);
        Set<String> newTeam = new HashSet<>(12);
        Set<String> newEnemy = new HashSet<>(12);
        newMatch.getPlayersTeam().forEach(playerRuntime -> newTeam.add(playerRuntime.getPilotname()));
        newMatch.getPlayersEnemy().forEach(playerRuntime -> newEnemy.add(playerRuntime.getPilotname()));
        for (PlayerRuntime pr : oldMatch.getPlayersTeam()) {
            if (newMatch.getPlayersTeam().contains(pr)) {
                playerScore++;
            } else {
                String bestMatch = TraceHelpers.guessValue(pr.getPilotname(), newTeam);
                //player matching is not 100%, try fuzzy
                int diff = StringUtils.getLevenshteinDistance(bestMatch, pr.getPilotname());
                if (diff > 0 && diff < 3) {
                    playerScore++;
                    Logger.log("possible player merge: '" + pr.getPilotname() + "' and '" + bestMatch + "'");
                }
            }
        }
        for (PlayerRuntime pr : oldMatch.getPlayersEnemy()) {
            if (newMatch.getPlayersEnemy().contains(pr)) {
                playerScore++;
            } else {
                String bestMatch = TraceHelpers.guessValue(pr.getPilotname(), newEnemy);
                //player matching is not 100%, try fuzzy
                int diff = StringUtils.getLevenshteinDistance(bestMatch, pr.getPilotname());
                if (diff > 0 && diff < 3) {
                    playerScore++;
                    Logger.log("possible player merge: '" + pr.getPilotname() + "' and '" + bestMatch + "'");
                }
            }
        }
        Logger.log("Similarity: match:" + matchScore + " players:" + playerScore + " time:" + (timeScore / 4));
        return matchScore + playerScore + timeScore / 4;
    }

    private static MatchRuntime getInstanceFromDb(int id) {
        return new MatchRuntime(id);
    }

    public static void buildMatchDataLine(GridPane grid, int row, String title, MatchCalculatedValue mc, boolean matchFinished) {
        Font fontData = Font.font("System", FontWeight.BOLD, 15);
        Label labelTitle = new Label(title);
        labelTitle.setFont(fontData);
        labelTitle.setStyle(GuiUtils.styleNeutral);
        labelTitle.setPadding(PlayerRuntime.DATA_INSETS);
        //labelTitle.setRotate(45);
        //
        Label labelTeam = new Label(mc.teamValue);
        labelTeam.setFont(fontData);
        labelTeam.setStyle(GuiUtils.styleTeam);
        labelTeam.setPadding(PlayerRuntime.DATA_INSETS);
        //
        grid.add(labelTitle, 0, row);
        grid.add(labelTeam, 1, row);

        if (matchFinished) {
            Label labelEnemy = new Label(mc.enemyValue);
            labelEnemy.setFont(fontData);
            labelEnemy.setStyle(GuiUtils.styleEnemy);
            labelEnemy.setPadding(PlayerRuntime.DATA_INSETS);
            //
            Label labelTotal = new Label(mc.totalValue);
            labelTotal.setFont(fontData);
            labelTotal.setStyle(GuiUtils.styleNeutral);
            labelTotal.setPadding(PlayerRuntime.DATA_INSETS);
            //
            grid.add(labelEnemy, 2, row);
            grid.add(labelTotal, 3, row);
        }
    }

    public boolean getTracingFinished() {
        return tracingFinished.get();
    }

    public SimpleBooleanProperty tracingFinishedProperty() {
        return tracingFinished;
    }

    private void setupRewardFinishListeners(ScreenshotFileHandler screenshot) {
        RewardInfoTracer tracer = screenshot.getRewardInfoTracer();
        tracingFinished.bind(tracer.finishedProperty());
        tracer.finishedProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (newValue) {
                    map.set(tracer.getMap());
                    gameMode.set(TraceHelpers.guessValue(tracer.getGameMode().replaceAll(".*:", ""), gameModes));
                    battleTime.set(tracer.getBattleTime());
                    PlayerRuntime player = PlayerRuntime.getInstance(SettingsTabController.getPlayername());
                    personalRecord = new PersonalMatchRecord(player, tracer, this);
                    saveOrUpdateMatch();
                    screenshot.archiveFile(id);
                }
            } catch (Exception e) {
                Logger.error(e);
            }
        });
    }

    private void setupMatchFinishListeners(ScreenshotFileHandler screenshot) {
        SimpleBooleanProperty mapDataFinished = new SimpleBooleanProperty(false);
        MatchInfoTracer tracer = screenshot.getMatchTracer();
        for (int i = 0; i < 24; i++) {
            PlayerInfoTracer pi = screenshot.getPlayerInfoTracer(i);
            final int p = i;
            preliminaryInfo.put(p, pi.progressProperty());
            BooleanBinding mapAndPlayerFinished = BooleanBinding.booleanExpression(pi.finishedProperty()).and(mapDataFinished);
            startBindingWatchDog(mapAndPlayerFinished, 300, TRACE_TIMEOUT);
            mapAndPlayerFinished.addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    try {
                        boolean isVictory = matchResult.get().startsWith("VICTORY");
                        boolean isDefeat = matchResult.get().startsWith("DEFEAT");
                        boolean isDraw = matchResult.get().startsWith("TIE");
                        boolean isEnemy = false;
                        //Logger.log("player trace finished for " + pi.getPilotName() + " mech:" + pi.getMech());
                        if (isVictory) {
                            isEnemy = p >= 12;
                        } else if (isDefeat) {
                            isEnemy = p < 12;
                        } else if (isDraw) {
                            //just assuming, didn't have an example
                            isEnemy = p < 12;
                        } else {
                            //unknown result... just assign anywhere
                            isEnemy = p >= 12;
                        }

                        PlayerRuntime pr = PlayerRuntime.getInstance(pi.getPilotName());
                        pr.unitProperty().set(pi.getUnitTag());
                        if (!pr.getPilotname().equals(SettingsTabController.getPlayername()) && !SessionRuntime.playersNew.contains(pr)) {
                            //don't count players that were seen firsttime in this session as known
                            SessionRuntime.playersKnown.add(pr);
                        }

                        pr.setPlayerNumber(p);
                        PlayerMatchRecord prec = null;
                        try {
                            prec = new PlayerMatchRecord(pr, pi, this, isEnemy);
                        } catch (Exception e) {
                            Logger.error(e);
                            Logger.log("using dummy match record for " + pi.getPilotName());
                            prec = PlayerMatchRecord.getReferenceRecord(isEnemy);
                        }
                        pr.getMatchRecords().add(prec);
                        playerRecords.add(prec);
                        if (pr.getPilotname().equals(SettingsTabController.getPlayername())) {
                            SessionRuntime.sessionRecords.add(prec);
                        }
                        //last because other triggers depend on it
                        if (isEnemy) {
                            playersEnemy.add(pr);
                        } else {
                            playersTeam.add(pr);
                        }
                    } catch (Exception e) {
                        Logger.log("Player info tracer finish trigger, " + pi.getPilotName());
                        Logger.error(e);
                    }
                }
            });
        }
        tracer.finishedProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (newValue) {
                    Logger.log("Match info trace finished");
                    map.set(tracer.getMap());
                    gameMode.set(TraceHelpers.guessValue(tracer.getGameMode().replaceAll(".*:", ""), gameModes));
                    battleTime.set(tracer.getBattleTime());
                    server.set(tracer.getServer());
                    if (type == ScreenshotType.QP_4SUMMARY) {
                        String realResult = "";
                        realResult = tracer.getMatchResult();
                        if (!realResult.matches(".*(?:VICTORY|DEFEAT|TIE).*")) {
                            String winner = tracer.getWinningTeam().toLowerCase();
                            String loser = tracer.getLosingTeam().toLowerCase();
                            if (winner.contains("team")) {
                                realResult = "VICTORY";
                            } else if (winner.contains("enemy")) {
                                realResult = "DEFEAT";
                            } else if (loser.contains("team")) {
                                realResult = "DEFEAT";
                            } else if (loser.contains("enemy")) {
                                realResult = "VICTORY";
                            }
                        }
                        if (realResult.startsWith("VICTORY")) {
                            SessionRuntime.wins++;
                        } else if (realResult.startsWith("DEFEAT")) {
                            SessionRuntime.losses++;
                        }
                        SessionRuntime.totalMatches++;
                        matchResult.set(realResult);
                    }
                    mapDataFinished.set(true);
                }
            } catch (Exception e) {
                Logger.error(e);
            }
        });
        //set up duplicate check for when tracing finishes
        tracingFinished.bind(Bindings.size(playersTeam).isEqualTo(12).and(Bindings.size(playersEnemy).isEqualTo(12)).and(mapDataFinished));
        startBindingWatchDog(tracingFinished, 300, TRACE_TIMEOUT);
        tracingFinished.addListener((observable, oldValue, newValue) -> {
            Logger.log("tracingfinished:" + newValue);
            if (newValue) {
                saveOrUpdateMatch();
                screenshot.archiveFile(id);
            }
        });
    }

    private void saveOrUpdateMatch() {
        MatchRuntime oldRuntime = findSavedMatch();
        int newId;
        if (oldRuntime != null) {
            newId = oldRuntime.getId();
            Logger.log("Screenshot identified as existing match " + newId);
            WatcherTabController.getInstance().labelStatusInfo.setText("Assigned to previously saved Match: " + oldRuntime.toString());
            //delete incomplete/outdated records, possibly from prep screenshot
            //only if better records are available
            if (type == ScreenshotType.QP_4SUMMARY) {
                cleanPreviousMatchRecords(newId);
            }
            if (type != ScreenshotType.QP_3REWARDS) {
                //load previously saved personal record
                try {
                    personalRecord = new PersonalMatchRecord(PlayerRuntime.getInstance(SettingsTabController.getPlayername()).getId(), newId);
                } catch (Exception e) {
                    //ok, it might not exist. leave as null
                }
            }
        } else {
            newId = createMatchEntry();
            Logger.log("Screenshot is a new match " + newId);
            WatcherTabController.getInstance().labelStatusInfo.setText("New match: " + newId);
        }
        id = newId;
        if (type == ScreenshotType.QP_1PREPARATION) {
            //save initial map/game data
            saveInitialMatchData();
            if (personalRecord == null) { //dont overwrite
                personalRecord = PersonalMatchRecord.getReferenceRecord(PlayerRuntime.getInstance(SettingsTabController.getPlayername()).getId());
            }
            savePersonalMatchRecord();
            //remember seen players
            savePlayerMatchRecords();
        } else if (type == ScreenshotType.QP_3REWARDS) {
            savePersonalMatchRecord();
            //add missing matchdata
            saveCompleteMatchData();
        } else if (type == ScreenshotType.QP_4SUMMARY) {
            if (personalRecord == null) { //dont overwrite
                personalRecord = PersonalMatchRecord.getReferenceRecord(PlayerRuntime.getInstance(SettingsTabController.getPlayername()).getId());
            }
            savePlayerMatchRecords();
            //save player match records
            savePersonalMatchRecord();
            //add missing matchdata
            saveCompleteMatchData();
        }
        //initChangeListeners();
    }

    private void savePersonalMatchRecord() {
        try {
            personalRecord.saveData(id);
        } catch (SQLException e) {
            Logger.error(e);
        }
    }

    private void saveCompleteMatchData() {
        saveInitialMatchData();//"if empty"
        updateIfEmpty("matchresult", this.matchResult.get());
        updateIfEmpty("battletime", this.battleTime.get());
    }

    private boolean updateIfEmpty(String field, String value) {
        boolean ret = false;
        try {
            PreparedStatement prep = DbHandler.getInstance().prepareStatement("update match_data set " + field + "=? where id=? and " + field + " is null or " + field + "=''");
            prep.setString(1, value);
            prep.setInt(2, id);
            ret = prep.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error(e);
            return false;
        }
        return ret;
    }

    private void saveInitialMatchData() {
        updateIfEmpty("gamemode", this.gameMode.get());
        updateIfEmpty("map", this.map.get());
        updateIfEmpty("maptimeofday", this.mapTimeOfDay.get());
    }

    private void cleanPreviousMatchRecords(int oldMatchId) {
        try {
            PreparedStatement prep = DbHandler.getInstance().prepareStatement("delete from player_matchdata where match_data_id=?");
            prep.setInt(1, oldMatchId);
            int del = prep.executeUpdate();
            Logger.log("cleaned " + del + " old player records");
            List<PlayerRuntime> players = new ArrayList<>(playersTeam);
            players.addAll(playersEnemy);
            for (PlayerRuntime pr : players) {
                pr.removeMatchRecord(oldMatchId);
            }
        } catch (SQLException e) {
            Logger.error(e);
        }
    }

    private void savePlayerMatchRecords() {
        playerRecords.forEach(playerMatchRecord -> {
            try {
                playerMatchRecord.saveData(id);
            } catch (SQLException e) {
                Logger.error(e);
            }
        });
    }

    private MatchRuntime findSavedMatch() {
        try {
            PreparedStatement prep = DbHandler.getInstance().prepareStatement("select id from match_data where matchtime between ? and ? order by matchtime desc");
            prep.setTimestamp(1, new Timestamp(timestamp - maxMatchTimeDifference));
            prep.setTimestamp(2, new Timestamp(timestamp + maxMatchTimeDifference));
            ResultSet rs = prep.executeQuery();
            MatchRuntime bestCandidate = null;
            Logger.log("Finding match for " + this.toString());
            int bestScore = 15;//minimum requirement
            while (rs.next()) {
                int id = rs.getInt(1);
                MatchRuntime candidate = new MatchRuntime(id);
                if (candidate.getTimestamp() == timestamp) {
                    bestCandidate = candidate;
                    Logger.warning("tried to import duplicate screenshot, same timestamp " + sdf.format(new Date(timestamp)));
                    break;
                } else {
                    int score = calculateSimilarity(candidate, this);
                    Logger.log(candidate.toString() + " has similarity score " + score);
                    if (score > bestScore) {
                        bestCandidate = candidate;
                        bestScore = score;
                    }
                }
            }
            rs.close();
            return bestCandidate;
        } catch (Exception e) {
            Logger.error(e);
            return null;
        }
    }

    @Override
    public String toString() {
        return id + "-" + gameMode.get() + "-" + map.get() + "-" + formattedTimestamp.get();
    }

    public List<Stat> getStatsToDisplay() {
        return SettingsTabController.getInstance().getStatsToDisplay(type);
    }

    private void startBindingWatchDog(ObservableValue<Boolean> binding, int interval, int timeout) {
        new Thread(() -> {
            try {
                int elapsed = 0;
                while (!binding.getValue() && elapsed < timeout) {//necessary to get the lazy changelistener to fire
                    Thread.sleep(interval);
                    elapsed += interval;
                }
            } catch (InterruptedException e) {
                Logger.error(e);
            }
        }).start();
    }

    private void delete() {
        try {
            PreparedStatement prep = DbHandler.getInstance().prepareStatement("delete MATCH_DATA where id=?");
            prep.setInt(1, id);
            prep.executeUpdate();
            for (PlayerRuntime pr : playersTeam) {
                pr.getMatchRecords().removeAll(playerRecords);//forces recalc of values
            }
            for (PlayerRuntime pr : playersEnemy) {
                pr.getMatchRecords().removeAll(playerRecords);//forces recalc of values
            }
            for (PlayerMatchRecord pmr : playerRecords) {
                pmr.delete();//does nothing for now, cascaded from match delete
            }
            personalRecord.delete();//does nothing for now, cascaded from match delete
        } catch (Exception e) {
            Logger.error(e);
        }
    }

    public Map<Integer, SimpleStringProperty> getPreliminaryInfo() {
        return preliminaryInfo;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getMapTimeOfDay() {
        return mapTimeOfDay.get();
    }

    public SimpleStringProperty mapTimeOfDayProperty() {
        return mapTimeOfDay;
    }

    public boolean isMatchFinished() {
        return matchFinished;
    }

    public String getServer() {
        return server.get();
    }

    public SimpleStringProperty serverProperty() {
        return server;
    }

    public int getId() {
        return id;
    }

    public ScreenshotType getType() {
        return type;
    }

    private void initChangeListeners() {
        gameMode.addListener((observable, oldValue, newValue) -> updateMatchData("gamemode", newValue));
        map.addListener((observable, oldValue, newValue) -> updateMatchData("map", newValue));
        matchResult.addListener((observable, oldValue, newValue) -> updateMatchData("matchResult", newValue));
        matchName.addListener((observable, oldValue, newValue) -> updateMatchData("matchname", newValue));
        battleTime.addListener((observable, oldValue, newValue) -> updateMatchData("battletime", newValue));
        mapTimeOfDay.addListener((observable, oldValue, newValue) -> updateMatchData("maptimeofday", newValue));
    }

    private void updateMatchData(String field, String value) {
        if (id == 0) return;
        try {
            //Utils.log("save match " + field + "=" + value);
            PreparedStatement prep = DbHandler.getInstance().prepareStatement("update MATCH_DATA set " + field + "=? where id=?");
            prep.setString(1, value);
            prep.setInt(2, id);
            prep.executeUpdate();
        } catch (SQLException e) {
            Logger.error(e);
        }
    }

    public String getMatchResult() {
        return matchResult.get();
    }

    public SimpleStringProperty matchResultProperty() {
        return matchResult;
    }

    public String getMatchName() {
        return matchName.get();
    }

    public SimpleStringProperty matchNameProperty() {
        return matchName;
    }

    public String getMap() {
        return map.get();
    }

    public SimpleStringProperty mapProperty() {
        return map;
    }

    public String getGameMode() {
        return gameMode.get();
    }

    public SimpleStringProperty gameModeProperty() {
        return gameMode;
    }

    public String getFormattedTimestamp() {
        return formattedTimestamp.get();
    }

    public SimpleStringProperty formattedTimestampProperty() {
        return formattedTimestamp;
    }

    public String getBattleTime() {
        return battleTime.get();
    }

    public SimpleStringProperty battleTimeProperty() {
        return battleTime;
    }

    public ObservableList<PlayerRuntime> getPlayersTeam() {
        return playersTeam;
    }

    public ObservableList<PlayerRuntime> getPlayersEnemy() {
        return playersEnemy;
    }

    private void handleLoadError(Exception e, File screenshot) {
        if (screenshot == null) {
            e.printStackTrace();
            return;
        }
        WatcherTabController.getInstance().setStatusInfo("import failed: " + e.getMessage());
        e.printStackTrace();
        File errordir = new File(SettingsTabController.getErrorDirectory(), sdfDir.format(new Date()));
        errordir.mkdir();
        screenshot.renameTo(new File(errordir, screenshot.getName()));
        File desc = new File(errordir, "error.txt");
        try {
            FileOutputStream fos = new FileOutputStream(desc);
            PrintStream ps = new PrintStream(fos);
            e.printStackTrace(ps);
            ps.close();
            fos.close();
        } catch (Exception e1) {
            WatcherTabController.getInstance().setStatusInfo("can't even write stacktrace" + e1.toString());
            e1.printStackTrace();
        }
    }

    private int createMatchEntry() {
        try {
            PreparedStatement prep = DbHandler.getInstance().prepareStatement("insert into MATCH_DATA(matchtime,matchname) values(?,?)", true);
            prep.setString(1, sdf.format(timestamp));
            prep.setString(2, "undefined name");
            prep.executeUpdate();
            ResultSet rsid = prep.getGeneratedKeys();
            int _id = 0;
            if (rsid.next()) {
                _id = rsid.getInt(1);
            }
            if (_id == 0) {
                throw new Exception("cannot create DB match row?");
            }
            rsid.close();
            //Logger.log("new match id: " + _id);
            return _id;
        } catch (Exception e) {
            Logger.error(e);
            return -1;
        }
    }

    public Pane getMatchAnalyticsPane() {
        Font fontHeader = Font.font("System", FontWeight.BOLD, 20);

        Label labelTeam = new Label("Your Team");
        labelTeam.setStyle(GuiUtils.styleTeam);
        labelTeam.setFont(fontHeader);
        labelTeam.setPadding(PlayerRuntime.DATA_INSETS);
        labelTeam.setRotate(90);
        //
        VBox columnTitles = new VBox();
        VBox columnTeam = new VBox();
        columnTeam.getChildren().add(new Group(labelTeam));
        GridPane grid = new GridPane();
        int line = 0;
        grid.add(new Group(labelTeam), 1, 0);
        if (matchFinished) {
            VBox columnEnemy = new VBox();
            VBox columnTotal = new VBox();
            Label labelEnemy = new Label("Your Enemy");
            labelEnemy.setStyle(GuiUtils.styleEnemy);
            labelEnemy.setFont(fontHeader);
            labelEnemy.setPadding(PlayerRuntime.DATA_INSETS);
            labelEnemy.setRotate(90);
            //
            Label labelTotal = new Label("Total / Avg");
            labelTotal.setStyle(GuiUtils.styleNeutral);
            labelTotal.setFont(fontHeader);
            labelTotal.setPadding(PlayerRuntime.DATA_INSETS);
            labelTotal.setRotate(90);
            //
            Label labelDummy = new Label("");
            labelDummy.setStyle(GuiUtils.styleNeutral);
            labelDummy.setFont(fontHeader);
            labelDummy.setPadding(PlayerRuntime.DATA_INSETS);
            columnTitles.getChildren().add(labelDummy);

            columnEnemy.getChildren().add(new Group(labelEnemy));
            columnTotal.getChildren().add(new Group(labelTotal));
            grid.add(new Group(labelEnemy), 2, 0);
            grid.add(new Group(labelTotal), 3, 0);
        }
        line++;
        List<PlayerMatchRecord> pmrTeam = new ArrayList<>(12);
        List<PlayerMatchRecord> pmrEnemy = new ArrayList<>(12);
        for (PlayerRuntime pr : playersTeam) {
            PlayerMatchRecord pmr = pr.getMatchRecord(this);
            if (pmr != null) {
                pmrTeam.add(pmr);
            } else {
                Logger.warning("cannot find player in match: " + pr.getPilotname());
            }
        }
        for (PlayerRuntime pr : playersEnemy) {
            PlayerMatchRecord pmr = pr.getMatchRecord(this);
            if (pmr != null) {
                pmrEnemy.add(pmr);
            } else {
                Logger.warning("cannot find player in match: " + pr.getPilotname());
            }
        }
        MatchCalculatedValue damageDealt = new MatchCalculatedValue() {
            @Override
            public void calculate() {
                int team = 0;
                int enemy = 0;
                for (PlayerMatchRecord pmr : pmrTeam) {
                    team += pmr.getDamage();
                }
                for (PlayerMatchRecord pmr : pmrEnemy) {
                    enemy += pmr.getDamage();
                }
                teamValue = "" + team;
                enemyValue = "" + enemy;
                totalValue = "" + (team + enemy);
            }
        };
        MatchCalculatedValue medianDamage = new MatchCalculatedValue() {
            @Override
            public void calculate() {
                BigDecimal[] damageTeam = new BigDecimal[12];
                BigDecimal[] damageEnemy = new BigDecimal[12];
                BigDecimal[] damageTotal = new BigDecimal[24];
                int i = 0;
                for (PlayerMatchRecord pmr : pmrTeam) {
                    BigDecimal dam = new BigDecimal(pmr.getDamage());
                    damageTeam[i] = dam;
                    damageTotal[i] = dam;
                    i++;
                }
                i = 0;
                for (PlayerMatchRecord pmr : pmrEnemy) {
                    BigDecimal dam = new BigDecimal(pmr.getDamage());
                    damageEnemy[i] = dam;
                    damageTotal[i + 12] = dam;
                    i++;
                }
                teamValue = "" + Utils.getMedianValue(damageTeam).toPlainString();
                enemyValue = "" + Utils.getMedianValue(damageEnemy).toPlainString();
                totalValue = "" + Utils.getMedianValue(damageTotal).toPlainString();
            }
        };
        MatchCalculatedValue score = new MatchCalculatedValue() {
            @Override
            public void calculate() {
                int team = 0;
                int enemy = 0;
                for (PlayerMatchRecord pmr : pmrTeam) {
                    team += pmr.getMatchScore();
                }
                for (PlayerMatchRecord pmr : pmrEnemy) {
                    enemy += pmr.getMatchScore();
                }
                teamValue = "" + team;
                enemyValue = "" + enemy;
                totalValue = "" + (team + enemy);
            }
        };
        MatchCalculatedValue medianScore = new MatchCalculatedValue() {
            @Override
            public void calculate() {
                BigDecimal[] scoreTeam = new BigDecimal[12];
                BigDecimal[] scoreEnemy = new BigDecimal[12];
                BigDecimal[] scoreTotal = new BigDecimal[24];
                int i = 0;
                for (PlayerMatchRecord pmr : pmrTeam) {
                    BigDecimal dam = new BigDecimal(pmr.getMatchScore());
                    scoreTeam[i] = dam;
                    scoreTotal[i] = dam;
                    i++;
                }
                i = 0;
                for (PlayerMatchRecord pmr : pmrEnemy) {
                    BigDecimal dam = new BigDecimal(pmr.getMatchScore());
                    scoreEnemy[i] = dam;
                    scoreTotal[i + 12] = dam;
                    i++;
                }
                teamValue = "" + Utils.getMedianValue(scoreTeam).toPlainString();
                enemyValue = "" + Utils.getMedianValue(scoreEnemy).toPlainString();
                totalValue = "" + Utils.getMedianValue(scoreTotal).toPlainString();
            }
        };
        MatchCalculatedValue weight = new MatchCalculatedValue() {
            @Override
            public void calculate() {
                int team = 0;
                int enemy = 0;
                for (PlayerMatchRecord pmr : pmrTeam) {
                    MechRuntime mech = MechRuntime.getMechByShortName(pmr.getMech());
                    team += mech.getTons();
                }
                for (PlayerMatchRecord pmr : pmrEnemy) {
                    enemy += MechRuntime.getMechByShortName(pmr.getMech()).getTons();
                }
                teamValue = team + "";
                enemyValue = enemy + "";
                totalValue = (team + enemy) + "";
            }
        };
        MatchCalculatedValue scorePerTon = new MatchCalculatedValue() {
            @Override
            public void calculate() {
                try {
                    teamValue = new BigDecimal(score.teamValue).divide(new BigDecimal(weight.teamValue), 2, BigDecimal.ROUND_HALF_UP).toPlainString();
                    enemyValue = new BigDecimal(score.enemyValue).divide(new BigDecimal(weight.enemyValue), 2, BigDecimal.ROUND_HALF_UP).toPlainString();
                    totalValue = new BigDecimal(score.totalValue).divide(new BigDecimal(weight.totalValue), 2, BigDecimal.ROUND_HALF_UP).toPlainString();
                } catch (Exception e) {
                    teamValue = "0";
                    enemyValue = "0";
                    totalValue = "0";
                }
            }
        };
        MatchCalculatedValue numLights = new MatchCalculatedValue() {
            @Override
            public void calculate() {
                int team = 0;
                int enemy = 0;
                for (PlayerMatchRecord pmr : pmrTeam) {
                    if ("Light".equals(MechRuntime.getMechByShortName(pmr.getMech()).getWeightClass())) team++;
                }
                for (PlayerMatchRecord pmr : pmrEnemy) {
                    if ("Light".equals(MechRuntime.getMechByShortName(pmr.getMech()).getWeightClass())) enemy++;
                }
                teamValue = team + "";
                enemyValue = enemy + "";
                totalValue = (team + enemy) + "";
            }
        };
        MatchCalculatedValue numMediums = new MatchCalculatedValue() {
            @Override
            public void calculate() {
                int team = 0;
                int enemy = 0;
                for (PlayerMatchRecord pmr : pmrTeam) {
                    if ("Medium".equals(MechRuntime.getMechByShortName(pmr.getMech()).getWeightClass())) team++;
                }
                for (PlayerMatchRecord pmr : pmrEnemy) {
                    if ("Medium".equals(MechRuntime.getMechByShortName(pmr.getMech()).getWeightClass())) enemy++;
                }
                teamValue = team + "";
                enemyValue = enemy + "";
                totalValue = (team + enemy) + "";
            }
        };
        MatchCalculatedValue numHeavies = new MatchCalculatedValue() {
            @Override
            public void calculate() {
                int team = 0;
                int enemy = 0;
                for (PlayerMatchRecord pmr : pmrTeam) {
                    if ("Heavy".equals(MechRuntime.getMechByShortName(pmr.getMech()).getWeightClass())) team++;
                }
                for (PlayerMatchRecord pmr : pmrEnemy) {
                    if ("Heavy".equals(MechRuntime.getMechByShortName(pmr.getMech()).getWeightClass())) enemy++;
                }
                teamValue = team + "";
                enemyValue = enemy + "";
                totalValue = (team + enemy) + "";
            }
        };
        MatchCalculatedValue numAssaults = new MatchCalculatedValue() {
            @Override
            public void calculate() {
                int team = 0;
                int enemy = 0;
                for (PlayerMatchRecord pmr : pmrTeam) {
                    if ("Assault".equals(MechRuntime.getMechByShortName(pmr.getMech()).getWeightClass())) team++;
                }
                for (PlayerMatchRecord pmr : pmrEnemy) {
                    if ("Assault".equals(MechRuntime.getMechByShortName(pmr.getMech()).getWeightClass())) enemy++;
                }
                teamValue = team + "";
                enemyValue = enemy + "";
                totalValue = (team + enemy) + "";
            }
        };
        MatchCalculatedValue numMissing = new MatchCalculatedValue() {
            @Override
            public void calculate() {
                int team = 0;
                int enemy = 0;
                for (PlayerMatchRecord pmr : pmrTeam) {
                    MechRuntime mr = MechRuntime.getMechByShortName(pmr.getMech());
                    if ("None".equals(mr.getWeightClass())) team++;
                }
                for (PlayerMatchRecord pmr : pmrEnemy) {
                    MechRuntime mr = MechRuntime.getMechByShortName(pmr.getMech());
                    if ("None".equals(mr.getWeightClass())) enemy++;
                }
                teamValue = team + "";
                enemyValue = enemy + "";
                totalValue = (team + enemy) + "";
            }
        };
        if (matchFinished) {
            buildMatchDataLine(grid, line++, "Total Score", score, matchFinished);
            buildMatchDataLine(grid, line++, "Median Score", medianScore, matchFinished);
            buildMatchDataLine(grid, line++, "Total Damage", damageDealt, matchFinished);
            buildMatchDataLine(grid, line++, "Median Damage", medianDamage, matchFinished);
            buildMatchDataLine(grid, line++, "Score/Ton", scorePerTon, matchFinished);
        }
        buildMatchDataLine(grid, line++, "Total Weight", weight, matchFinished);
        buildMatchDataLine(grid, line++, "Light Mechs", numLights, matchFinished);
        buildMatchDataLine(grid, line++, "Medium Mechs", numMediums, matchFinished);
        buildMatchDataLine(grid, line++, "Heavy Mechs", numHeavies, matchFinished);
        buildMatchDataLine(grid, line++, "Assault Mechs", numAssaults, matchFinished);
        if (!numMissing.totalValue.equals("0")) {
            buildMatchDataLine(grid, line++, "Missing Mechs", numMissing, matchFinished);
        }

        return grid;
    }

    public PlayerMatchRecord getPlayerMatchRecord(PlayerRuntime playerRuntime) {
        for (PlayerMatchRecord pmr : playerRecords) {
            if (pmr.getPlayerId() == playerRuntime.getId()) {
                return pmr;
            }
        }
        return null;
    }

    public static abstract class MatchCalculatedValue {
        String teamValue = "";
        String enemyValue = "";
        String totalValue = "";

        public MatchCalculatedValue() {
            calculate();
        }

        public abstract void calculate();
    }
}
