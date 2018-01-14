package at.happynev.mwoscoreboardhelper.stat.calculator;

import at.happynev.mwoscoreboardhelper.PlayerMatchRecord;
import at.happynev.mwoscoreboardhelper.PlayerRuntime;
import at.happynev.mwoscoreboardhelper.isenleaderboard.IsenLeaderboard;
import at.happynev.mwoscoreboardhelper.isenleaderboard.IsenLeaderboardResult;
import at.happynev.mwoscoreboardhelper.stat.StatExplanationStep;
import at.happynev.mwoscoreboardhelper.stat.StatTable;
import at.happynev.mwoscoreboardhelper.tracer.ScreenshotType;
import javafx.scene.paint.Color;

import java.util.Collection;
import java.util.List;

/**
 * Created by Nev on 29.07.2017.
 */
public class StatCalculatorLeaderboardRank extends StatCalculator {

    public StatCalculatorLeaderboardRank() {

    }

    @Override
    public String calculateCurrentValue(Collection<PlayerMatchRecord> records, PlayerMatchRecord currentRecord, List<String> previousValues) {
        IsenLeaderboardResult data = IsenLeaderboard.getInstance().getLeaderboardData(PlayerRuntime.getInstance(currentRecord.getPlayerId()).getPilotname());

        if (data == null) {
            return "?";
        }
        return "" + data.getOverallData().getRank();
    }

    @Override
    public StatExplanationStep getStepDescription() {
        return new StatExplanationStep(Color.CORNFLOWERBLUE, "Overall Rank according to The Jarl's List");
    }

    @Override
    public boolean canDisplay(ScreenshotType type, StatTable table) {
        return true;
    }
}
