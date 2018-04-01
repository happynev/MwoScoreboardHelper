package at.happynev.mwoscoreboardhelper.stat.calculator;

import at.happynev.mwoscoreboardhelper.PlayerMatchRecord;
import at.happynev.mwoscoreboardhelper.PlayerRuntime;
import at.happynev.mwoscoreboardhelper.isenleaderboard.IsenLeaderboard;
import at.happynev.mwoscoreboardhelper.isenleaderboard.IsenLeaderboardResult;
import at.happynev.mwoscoreboardhelper.isenleaderboard.IsenSeasonData;
import at.happynev.mwoscoreboardhelper.stat.StatExplanationStep;
import at.happynev.mwoscoreboardhelper.stat.StatTable;
import at.happynev.mwoscoreboardhelper.tracer.ScreenshotType;
import javafx.scene.paint.Color;

import java.util.*;

/**
 * Created by Nev on 29.07.2017.
 */
public class StatCalculatorLeaderboardMechClass extends StatCalculator {

    public StatCalculatorLeaderboardMechClass() {

    }

    @Override
    public String calculateCurrentValue(Collection<PlayerMatchRecord> records, PlayerMatchRecord currentRecord, List<String> previousValues) {
        IsenLeaderboardResult data = IsenLeaderboard.getInstance().getLeaderboardData(PlayerRuntime.getInstance(currentRecord.getPlayerId()).getPilotname());
        if (data == null) {
            return "?";
        }
        IsenSeasonData leaderboardData = data.getConfiguredSeasonData();
        Map<String, Integer> classValues = new HashMap<>(4);
        if (leaderboardData.getLight() > 0) {
            classValues.put("Light", leaderboardData.getLight());
        }
        if (leaderboardData.getMedium() > 0) {
            classValues.put("Medium", leaderboardData.getMedium());
        }
        if (leaderboardData.getHeavy() > 0) {
            classValues.put("Heavy", leaderboardData.getHeavy());
        }
        if (leaderboardData.getAssault() > 0) {
            classValues.put("Assault", leaderboardData.getAssault());
        }
        Map<String, Integer> sortedValues = new TreeMap<>((o1, o2) -> -classValues.get(o1).compareTo(classValues.get(o2)));
        sortedValues.putAll(classValues);
        StringBuilder sb = new StringBuilder();
        for (String mechclass : sortedValues.keySet()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(mechclass).append(" ").append(sortedValues.get(mechclass)).append("%");
        }
        return sb.toString();
    }

    @Override
    public StatExplanationStep getStepDescription() {
        return new StatExplanationStep(Color.CORNFLOWERBLUE, "Mech class probability according to The Jarl's List for the previous season");
    }

    @Override
    public boolean canDisplay(ScreenshotType type, StatTable table) {
        return true;
    }
}
