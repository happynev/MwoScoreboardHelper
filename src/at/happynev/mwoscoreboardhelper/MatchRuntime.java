package at.happynev.mwoscoreboardhelper;

import at.happynev.mwoscoreboardhelper.tracer.*;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
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
    private final SimpleStringProperty matchName = new SimpleStringProperty("");
    private final SimpleStringProperty map = new SimpleStringProperty("");
    private final SimpleStringProperty server = new SimpleStringProperty("");
    private final SimpleStringProperty gameMode = new SimpleStringProperty("");
    private final SimpleStringProperty formattedTimestamp = new SimpleStringProperty("");
    private final SimpleStringProperty battleTime = new SimpleStringProperty("");
    private final SimpleStringProperty reward_cbills = new SimpleStringProperty("");
    private final SimpleStringProperty reward_xp = new SimpleStringProperty("");
    private final SimpleStringProperty matchResult = new SimpleStringProperty("");
    private final SimpleStringProperty mapTimeOfDay = new SimpleStringProperty("");
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final SimpleDateFormat sdfDir = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    private final ObservableList<PlayerRuntime> playersTeam = FXCollections.observableArrayList();
    private final ObservableList<PlayerRuntime> playersEnemy = FXCollections.observableArrayList();
    private final ObservableList<PlayerMatchRecord> playerRecords = FXCollections.observableArrayList();
    private final Map<Integer, SimpleStringProperty> preliminaryInfo = new HashMap<>(24);
    private final Set<String> gameModes = new HashSet<>(Arrays.asList("SKIRMISH", "DOMINATION", "ASSAULT", "CONQUEST", "INCURSION", "ESCORT"));
    private boolean matchFinished;
    private ScreenshotType type = null;
    private int id = 0;
    private Date timestamp = null;
    private boolean valid = false;

    public MatchRuntime(final File screenshot, final MatchRuntime previousData) {
        try {
            final long startProcessing = System.currentTimeMillis();
            timestamp = new Date(screenshot.lastModified());
            BufferedImage img = ImageIO.read(screenshot);
            type = ScreenshotType.identifyType(img);
            formattedTimestamp.setValue(sdf.format(timestamp));
            String filename = "";
            Offsets off = Offsets.getInstance(type, img);
            if (type == ScreenshotType.QP_1PREPARATION) {
                matchFinished = false;
            } else if (type == ScreenshotType.QP_3SUMMARY) {
                matchFinished = true;
            } else if (type == ScreenshotType.UNDEFINED) {
                throw new Exception("Screenshot cannot be identified");
            }
            final MatchInfoTracer tracer = new MatchInfoTracer(img, off);
            if (id == 0) {
                id = createMatchEntry();
            }
            SimpleBooleanProperty mapDataFinished = new SimpleBooleanProperty(false);
            initChangeListeners();
            filename = "Match-" + type + "." + screenshot.getName();
            for (int i = 0; i < 24; i++) {
                PlayerInfoTracer pi = new PlayerInfoTracer(img, i, off);
                final int p = i;
                preliminaryInfo.put(p, pi.progressProperty());
                BooleanBinding allFinished = BooleanBinding.booleanExpression(pi.finishedProperty()).and(mapDataFinished);
                startBindingWatchDog(allFinished, 300, TRACE_TIMEOUT);
                allFinished.addListener((observable, oldValue, newValue) -> {
                    if (newValue) {
                        try {
                            PlayerRuntime pr = PlayerRuntime.getInstance(pi.getPilotName());
                            pr.unitProperty().set(pi.getUnitTag());
                            pr.setPlayerNumber(p);
                            PlayerMatchRecord prec = null;
                            try {
                                prec = new PlayerMatchRecord(pr, pi, this);
                            } catch (Exception e) {
                                Logger.error(e);
                                Logger.log("using dummy match record for " + pi.getPilotName());
                                prec = PlayerMatchRecord.getDummyInstance();
                            }
                            pr.getMatchRecords().add(prec);
                            playerRecords.add(prec);
                            //TODO: just assuming result trace is done by now...
                            boolean isVictory = "VICTORY".equals(matchResult.get());
                            boolean isDefeat = "DEFEAT".equals(matchResult.get());
                            boolean isDraw = "DRAW".equals(matchResult.get());
                            //Logger.log("player trace finished, victory=" + isVictory + " defeat=" + isDefeat);
                            if (isVictory) {
                                if (p < 12) {
                                    playersTeam.add(pr);
                                } else {
                                    playersEnemy.add(pr);
                                }
                            } else if (isDefeat) {
                                if (p < 12) {
                                    playersEnemy.add(pr);
                                } else {
                                    playersTeam.add(pr);
                                }
                            } else if (isDraw) {
                                //just assuming, didn't have an example
                                if (p < 12) {
                                    playersTeam.add(pr);
                                } else {
                                    playersEnemy.add(pr);
                                }
                            } else {
                                //unknown result... just assign anywhere
                                if (p < 12) {
                                    playersTeam.add(pr);
                                } else {
                                    playersEnemy.add(pr);
                                }
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
                        String realResult = "";
                        realResult = tracer.getMatchResult();
                        if (!realResult.matches("VICTORY|DEFEAT|DRAW")) {
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
                        matchResult.set(realResult);
                        mapDataFinished.set(true);
                    }
                } catch (Exception e) {
                    Logger.error(e);
                }
            });
            //matchName.bind(Bindings.concat(formattedTimestamp, " ", map, " - ", gameMode));
            File archivedMatch = new File(SettingsTabController.getPostProcessedDirectory(), id + "-" + sdfDir.format(timestamp));

            archivedMatch.mkdirs();
            boolean deleteScreenshots = Boolean.parseBoolean(DbHandler.getInstance().loadSetting("deleteScreenshots", "true"));
            File arch = new File(archivedMatch, filename);
            if (deleteScreenshots) {
                if (arch.exists()) {
                    arch.delete();
                }
                boolean success = screenshot.renameTo(arch);
                if (!success) {
                    Logger.alertPopup("Failed to move " + screenshot.getName() + " to " + arch.toString());
                }
            } else {
                PreparedStatement prep = DbHandler.getInstance().prepareStatement("insert into processed(filename,processing_time) values(?,?)");
                prep.setString(1, screenshot.getName());
                prep.setTimestamp(2, new Timestamp(timestamp.getTime()));
                prep.executeUpdate();
                WatcherTabController.getInstance().getAlreadyProcessed().add(screenshot.getName());
                Utils.copyFile(screenshot, arch);
            }
            valid = true;
            try {
                //set up duplicate check for when tracing finishes
                BooleanBinding expr = Bindings.size(playerRecords).isEqualTo(24).and(mapDataFinished);
                startBindingWatchDog(expr, 300, TRACE_TIMEOUT);
                if (matchFinished && previousData != null && !previousData.isMatchFinished()) {
                    long maxMatchTime = 0;
                    long timeSinceLastData = timestamp.getTime() - previousData.getTimestamp().getTime();
                    if (type == ScreenshotType.QP_3SUMMARY) {
                        maxMatchTime = 15 * 60 * 1000;
                    }
                    maxMatchTime += 120000;//account for pre/post match time
                    if (timeSinceLastData < maxMatchTime) {
                        SimpleIntegerProperty duplicateLikelihood = new SimpleIntegerProperty(0);
                        //time matches. assign score for similar map/mode/players
                        //Utils.log("starting score check for possible duplicate");

                        expr.addListener((observable, oldValue, newValue) -> {
                            if (newValue) {
                                int similarityScore = calculateSimilarity(this, previousData);
                                Logger.log("similarity: " + similarityScore);
                                if (similarityScore > 20) {
                                    previousData.delete();
                                }
                            }
                        });
                        if (expr.get()) {
                            int similarityScore = calculateSimilarity(this, previousData);
                            Logger.log("similarity: " + similarityScore);
                            if (similarityScore > 20) {
                                previousData.delete();
                            }
                        }
                    } else {
                        Logger.log("time difference too large, not checking for duplicate");
                    }
                }
            } catch (Exception e) {
                Logger.error(e);
            }
        } catch (Exception e) {
            handleLoadError(e, screenshot);
        }
    }

    private MatchRuntime(int id) {
        try {
            PreparedStatement prep = DbHandler.getInstance().prepareStatement("select to_char(matchtime,'YYYY-MM-DD HH24:MI:SS),gamemode,map,matchResult,reward_cbill,reward_xp,matchname,battletime,maptimeofday from MATCH_DATA where id=?");
            prep.setInt(1, id);
            ResultSet rs = prep.executeQuery();
            if (rs.next()) {
                String _timestamp = rs.getString(1);
                String _gamemode = rs.getString(2);
                String _map = rs.getString(3);
                String _matchresult = rs.getString(4);
                String _reward_cbill = rs.getString(5);
                String _reward_xp = rs.getString(6);
                String _matchname = rs.getString(7);
                String _battletime = rs.getString(7);
                String _maptod = rs.getString(8);
                timestamp = sdf.parse(_timestamp);
                formattedTimestamp.setValue(_timestamp);
                this.matchName.set(_matchname);
                if (_gamemode != null) this.gameMode.set(_gamemode);
                if (_map != null) this.gameMode.set(_map);
                if (_matchresult != null) this.matchResult.set(_matchresult);
                if (_reward_cbill != null) this.reward_cbills.set(_reward_cbill);
                if (_reward_xp != null) this.reward_xp.set(_reward_xp);
                if (_battletime != null) this.battleTime.set(_battletime);
                if (_maptod != null) this.mapTimeOfDay.set(_maptod);
                initChangeListeners();
                //initPlayers(id);
            } else {
                throw new Exception("Match with id " + id + " not found in DB");
            }
            valid = true;
        } catch (Exception e) {
            handleLoadError(e, null);
        }
    }

    private static int calculateSimilarity(MatchRuntime match1, MatchRuntime match2) {
        int score = 0;
        if (match1.getGameMode().equals(match2.getGameMode())) score += 3;
        if (match1.getMap().equals(match2.getMap())) score += 3;
        for (PlayerRuntime pr : match1.getPlayersTeam()) {
            if (match2.getPlayersTeam().contains(pr)) score++;
        }
        for (PlayerRuntime pr : match1.getPlayersEnemy()) {
            if (match2.getPlayersEnemy().contains(pr)) score++;
        }
        return score;
    }

    public static MatchRuntime getInstanceFromDb(int id) {
        return new MatchRuntime(id);
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
        } catch (Exception e) {
            Logger.error(e);
        }
    }

    public Map<Integer, SimpleStringProperty> getPreliminaryInfo() {
        return preliminaryInfo;
    }

    public Date getTimestamp() {
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

    public void saveData() {
        try {
            if (id == 0) {
                id = createMatchEntry();
            }
            PreparedStatement prep = DbHandler.getInstance().prepareStatement("insert into match_data(gamemode,map,matchResult,reward_cbills,reward_xp,matchname,battletime,maptimeofday) values(?,?,?,?,?,?,?,?)");
            prep.setString(1, gameMode.getValue());
            prep.setString(2, map.getValue());
            prep.setString(3, matchResult.getValue());
            prep.setString(4, reward_cbills.getValue());
            prep.setString(5, reward_xp.getValue());
            prep.setString(6, matchName.getValue());
            prep.setString(7, battleTime.getValue());
            prep.setString(8, mapTimeOfDay.getValue());
            prep.executeUpdate();
        } catch (Exception e) {
            Logger.error(e);
        }
    }

    private void initChangeListeners() {
        gameMode.addListener((observable, oldValue, newValue) -> updateMatchData("gamemode", newValue));
        map.addListener((observable, oldValue, newValue) -> updateMatchData("map", newValue));
        matchResult.addListener((observable, oldValue, newValue) -> updateMatchData("matchResult", newValue));
        reward_cbills.addListener((observable, oldValue, newValue) -> updateMatchData("reward_cbill", newValue));
        reward_xp.addListener((observable, oldValue, newValue) -> updateMatchData("reward_xp", newValue));
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

    public String getReward_cbills() {
        return reward_cbills.get();
    }

    public SimpleStringProperty reward_cbillsProperty() {
        return reward_cbills;
    }

    public String getReward_xp() {
        return reward_xp.get();
    }

    public SimpleStringProperty reward_xpProperty() {
        return reward_xp;
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

    private int createMatchEntry() throws Exception {

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
        return _id;
    }

    public boolean isValid() {
        return valid;
    }
}
