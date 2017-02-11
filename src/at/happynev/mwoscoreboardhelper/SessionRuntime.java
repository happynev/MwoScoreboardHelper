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
    public static final List<PlayerMatchRecord> sessionRecords = new ArrayList<>();
    public static final Set<PlayerRuntime> playersNew = new HashSet<>();
    public static final Set<PlayerRuntime> playersKnown = new HashSet<>();
    private static final long started = System.currentTimeMillis();
    public static int wins = 0;
    public static int losses = 0;
    public static int totalMatches = 0;

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
        for (PlayerMatchRecord pmr : sessionRecords) {
            if (pmr.getStatus().equals("DEAD")) deaths++;
            kills += pmr.getKills();
            totaldamage += pmr.getDamage();
            totalscore += pmr.getMatchScore();
            if (pmr.getPing() > 0) validmatches++;
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
        BigDecimal matchPerHour = new BigDecimal(totalMatches).divide(hourFactor, 1, BigDecimal.ROUND_HALF_UP);
        buildSessionDataLine(grid, row++, "New Players", "" + playersNew.size());
        buildSessionDataLine(grid, row++, "Known Players", "" + playersKnown.size());
        buildSessionDataLine(grid, row++, "Matches played", "" + totalMatches);
        buildSessionDataLine(grid, row++, "Win/Loss", "" + winloss.toPlainString());
        buildSessionDataLine(grid, row++, "Kills/Deaths", "" + killdeath.toPlainString());
        buildSessionDataLine(grid, row++, "Avg. Score", "" + avgscore);
        buildSessionDataLine(grid, row++, "Total Kills", "" + kills);
        buildSessionDataLine(grid, row++, "Matches per Hour", "" + matchPerHour.toPlainString());
        return grid;
    }

    private static void buildSessionDataLine(GridPane grid, int row, String title, String value) {
        Font fontData = Font.font("System", FontWeight.BOLD, 15);
        Label labelTitle = new Label(title);
        labelTitle.setFont(fontData);
        labelTitle.setStyle(GuiUtils.styleNeutral);
        labelTitle.setPadding(PlayerRuntime.DATA_INSETS);
        //labelTitle.setRotate(45);
        //
        Label labelTeam = new Label(value);
        labelTeam.setFont(fontData);
        labelTeam.setStyle(GuiUtils.styleTeam);
        labelTeam.setPadding(PlayerRuntime.DATA_INSETS);
        //
        grid.add(labelTitle, 0, row);
        grid.add(labelTeam, 1, row);
    }
}