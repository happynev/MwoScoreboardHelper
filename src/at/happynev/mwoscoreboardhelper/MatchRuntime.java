package at.happynev.mwoscoreboardhelper;

import at.happynev.mwoscoreboardhelper.tracer.MatchInfoTracer;
import at.happynev.mwoscoreboardhelper.tracer.PlayerInfoTracer;
import at.happynev.mwoscoreboardhelper.tracer.ScreenshotType;
import at.happynev.mwoscoreboardhelper.tracer.TraceHelpers;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
    private final Set<String> gameModes = new HashSet<>(Arrays.asList("SKIRMISH", "DOMINATION", "ASSAULT", "CONQUEST", "INCURSION", "INVASION", "ESCORT"));
    private ScreenshotType type;
    private boolean matchFinished;
    private int id = 0;
    private long timestamp = 0;
    private boolean valid = false;

    public MatchRuntime(ScreenshotType _type) {
        type = _type;
        id = -1;
        map.set("SAMPLE MAP");
        server.set("welcome to the test server");
        gameMode.set("SETTINGS ASSAULT");
        timestamp = System.currentTimeMillis();
        formattedTimestamp.set(sdf.format(timestamp));
        battleTime.set("00:00");
        reward_cbills.set("500000");
        reward_xp.set("5000");
        matchResult.set("VICTORY");
        matchFinished = ScreenshotType.QP_3SUMMARY == type;
        valid = true;
        PlayerRuntime pr = PlayerRuntime.getReferencePlayer();
        playersTeam.add(pr);
        playerRecords.addAll(pr.getMatchRecords());
    }

    public MatchRuntime(final ScreenshotFileHandler screenshot) {
        try {
            timestamp = screenshot.getTimestamp();
            formattedTimestamp.setValue(sdf.format(timestamp));
            type = screenshot.getType();
            if (type == ScreenshotType.QP_1PREPARATION) {
                matchFinished = false;
            } else if (type == ScreenshotType.QP_3SUMMARY) {
                matchFinished = true;
                SessionRuntime.totalMatches++;
            }
            final MatchInfoTracer tracer = screenshot.getMatchTracer();
            id = createOrLoadMatchEntry();
            SimpleBooleanProperty mapDataFinished = new SimpleBooleanProperty(false);
            initChangeListeners();

            for (int i = 0; i < 24; i++) {
                PlayerInfoTracer pi = screenshot.getPlayerInfoTracer(i);
                final int p = i;
                preliminaryInfo.put(p, pi.progressProperty());
                BooleanBinding allFinished = BooleanBinding.booleanExpression(pi.finishedProperty()).and(mapDataFinished);
                startBindingWatchDog(allFinished, 300, TRACE_TIMEOUT);
                allFinished.addListener((observable, oldValue, newValue) -> {
                    if (newValue) {
                        try {
                            boolean isVictory = "VICTORY".equals(matchResult.get());
                            boolean isDefeat = "DEFEAT".equals(matchResult.get());
                            boolean isDraw = "TIE".equals(matchResult.get());
                            boolean isEnemy = false;
                            //Logger.log("player trace finished, victory=" + isVictory + " defeat=" + isDefeat);
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
                            if (isEnemy) {
                                playersEnemy.add(pr);
                            } else {
                                playersTeam.add(pr);
                            }
                            if (pr.getPilotname().equals(SettingsTabController.getPlayername())) {
                                SessionRuntime.sessionRecords.add(prec);
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
                        if (realResult.equals("VICTORY")) {
                            SessionRuntime.wins++;
                        } else if (realResult.equals("DEFEAT")) {
                            SessionRuntime.losses++;
                        }
                        matchResult.set(realResult);
                        mapDataFinished.set(true);
                    }
                } catch (Exception e) {
                    Logger.error(e);
                }
            });
            //matchName.bind(Bindings.concat(formattedTimestamp, " ", map, " - ", gameMode));
            screenshot.postProcessFile(id);
            valid = true;
            /*
            try {
                //set up duplicate check for when tracing finishes
                BooleanBinding expr = Bindings.size(playerRecords).isEqualTo(24).and(mapDataFinished);
                startBindingWatchDog(expr, 300, TRACE_TIMEOUT);
                if (matchFinished && previousData != null && !previousData.isMatchFinished()) {
                    long maxMatchTime = 0;
                    long timeSinceLastData = timestamp - previousData.getTimestamp();
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
            }*/
        } catch (Exception e) {
            //handleLoadError(e, screenshot.get);
            Logger.error(e);
        }
    }

    private MatchRuntime(int id) {
        try {
            PreparedStatement prep = DbHandler.getInstance().prepareStatement("select matchtime,gamemode,map,matchResult,reward_cbill,reward_xp,matchname,battletime,maptimeofday from MATCH_DATA where id=?");
            prep.setInt(1, id);
            ResultSet rs = prep.executeQuery();
            if (rs.next()) {
                long _timestamp = rs.getTimestamp(1).getTime();
                String _gamemode = rs.getString(2);
                String _map = rs.getString(3);
                String _matchresult = rs.getString(4);
                String _reward_cbill = rs.getString(5);
                String _reward_xp = rs.getString(6);
                String _matchname = rs.getString(7);
                String _battletime = rs.getString(7);
                String _maptod = rs.getString(8);
                timestamp = _timestamp;
                formattedTimestamp.setValue(sdf.format(new Date(_timestamp)));
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

    public static MatchRuntime getReferenceMatch(ScreenshotType type) {
        return new MatchRuntime(type);
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

    public void saveData() {
        try {
            if (id == 0) {
                id = createOrLoadMatchEntry();
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

    private int createOrLoadMatchEntry() throws Exception {

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
            }
        }
        for (PlayerRuntime pr : playersEnemy) {
            PlayerMatchRecord pmr = pr.getMatchRecord(this);
            if (pmr != null) {
                pmrEnemy.add(pmr);
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
                    if ("None".equals(MechRuntime.getMechByShortName(pmr.getMech()).getWeightClass())) team++;
                }
                for (PlayerMatchRecord pmr : pmrEnemy) {
                    if ("None".equals(MechRuntime.getMechByShortName(pmr.getMech()).getWeightClass())) enemy++;
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
