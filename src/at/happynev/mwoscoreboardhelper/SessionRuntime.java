package at.happynev.mwoscoreboardhelper;

import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Nev on 11.02.2017.
 */
public abstract class SessionRuntime {
    public static final List<PlayerMatchRecord> sessionMatchRecords = new ArrayList<>();
    public static final List<PersonalMatchRecord> sessionPersonalRecords = new ArrayList<>();
    public static final Set<PlayerRuntime> playersNew = new HashSet<>();
    public static final Set<PlayerRuntime> playersKnown = new HashSet<>();
    private static final long started = System.currentTimeMillis();
    public static int wins = 0;
    public static int losses = 0;
    public static int totalMatches = 0;
    private static long battleTimeMillis = 0;

    public static Pane getSessionStatsPane() {
        Font fontHeader = Font.font("System", FontWeight.BOLD, 20);
        GridPane grid = new GridPane();
        Label labelHeader = new Label("Session Stats");
        labelHeader.setFont(fontHeader);
        labelHeader.setStyle(GuiUtils.styleNeutral);
        int row = 0;
        grid.add(labelHeader, 0, row++, GridPane.REMAINING, 1);
        BigDecimal winloss = new BigDecimal(wins);
        if (losses > 0) {
            winloss = winloss.divide(new BigDecimal(losses), 2, BigDecimal.ROUND_HALF_UP);
        }
        int avgdamage = 0;
        int avgscore = 0;
        int kills = 0;
        int deaths = 0;
        int totaldamage = 0;
        int totalscore = 0;
        int validmatches = 0;
        int compDestruction = 0;
        int kmdds = 0;
        int solokills = 0;
        int totalCbills = 0;
        int totalXp = 0;

        for (PlayerMatchRecord pmr : sessionMatchRecords) {
            if (pmr.getStatus().equals("DEAD")) deaths++;
            kills += pmr.getKills();
            totaldamage += pmr.getDamage();
            totalscore += pmr.getMatchScore();
            if (pmr.getPing() > 0) validmatches++;
        }
        for (PersonalMatchRecord pmr : sessionPersonalRecords) {
            compDestruction += pmr.getComponentDestroyed();
            kmdds += pmr.getKmdds();
            solokills += pmr.getSoloKills();
            totalCbills += pmr.getRewardsCbills();
            totalXp += pmr.getRewardsXp();
        }
        BigDecimal killdeath = new BigDecimal(kills);
        if (validmatches > 0) {
            avgscore = (int) ((double) totalscore / (double) validmatches);
            avgdamage = (int) ((double) totaldamage / (double) validmatches);
        }
        if (deaths > 0) {
            killdeath = killdeath.divide(new BigDecimal(deaths), 2, BigDecimal.ROUND_HALF_UP);
        }
        long totalRuntime = System.currentTimeMillis() - started;
        BigDecimal hourFactor = new BigDecimal(totalRuntime).divide(new BigDecimal(60 * 60 * 1000), 5, BigDecimal.ROUND_HALF_UP);
        BigDecimal matchPerHour = BigDecimal.ZERO;
        if (totalMatches > 0) {
            matchPerHour = new BigDecimal(totalMatches).divide(hourFactor, 1, BigDecimal.ROUND_HALF_UP);
        }
        buildSessionDataLine(grid, row++, "New Players", "" + playersNew.size());
        buildSessionDataLine(grid, row++, "Known Players", "" + playersKnown.size());
        buildSessionDataLine(grid, row++, "Matches played", "" + totalMatches);
        buildSessionDataLine(grid, row++, "Win/Loss", "" + winloss.toPlainString());
        buildSessionDataLine(grid, row++, "Kills/Deaths", "" + killdeath.toPlainString());
        buildSessionDataLine(grid, row++, "Avg. Score", "" + avgscore);
        buildSessionDataLine(grid, row++, "Comp. Destructions", "" + compDestruction);
        buildSessionDataLine(grid, row++, "Kills", "" + kills);
        buildSessionDataLine(grid, row++, "KMDDs", "" + kmdds);
        buildSessionDataLine(grid, row++, "Solo Kills", "" + solokills);
        buildSessionDataLine(grid, row++, "Cbills earned", "" + totalCbills);
        buildSessionDataLine(grid, row++, "Xp earned", "" + totalXp);
        buildSessionDataLine(grid, row++, "Matches per Hour", "" + matchPerHour.toPlainString());
        buildSessionDataLine(grid, row++, "Battle Time %", "" + getWaitVsBattle());
        return grid;
    }

    private static String getWaitVsBattle() {
        long totalMillis = System.currentTimeMillis() - started;
        return Utils.getPercentage(battleTimeMillis, totalMillis);
    }

    private static void buildSessionDataLine(GridPane grid, int row, String title, String value) {
        Font fontData = Font.font("System", FontWeight.BOLD, 15);
        Label labelTitle = new Label(title);
        labelTitle.setFont(fontData);
        labelTitle.setStyle(GuiUtils.styleNeutral);
        labelTitle.setPadding(GuiUtils.DATA_INSETS);
        //labelTitle.setRotate(45);
        //
        Label labelTeam = new Label(value);
        labelTeam.setFont(fontData);
        labelTeam.setStyle(GuiUtils.styleTeam);
        labelTeam.setPadding(GuiUtils.DATA_INSETS);
        //
        grid.add(labelTitle, 0, row);
        grid.add(labelTeam, 1, row);
    }

    public static void addBattleTime(String battleTime) {
        if (battleTime != null && battleTime.matches("\\d+:\\d+")) {
            String[] time = battleTime.split(":");
            battleTimeMillis += Integer.parseInt(time[0]) * 60 * 1000;
            battleTimeMillis += Integer.parseInt(time[1]) * 1000;
        } else {
            Logger.warning("invalid battletime: " + battleTime);
        }
    }
}
