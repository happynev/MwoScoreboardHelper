package at.happynev.mwoscoreboardhelper.stat.calculator;

import at.happynev.mwoscoreboardhelper.GuiUtils;
import at.happynev.mwoscoreboardhelper.PlayerMatchRecord;
import at.happynev.mwoscoreboardhelper.stat.StatExplanationStep;
import at.happynev.mwoscoreboardhelper.stat.StatTable;
import at.happynev.mwoscoreboardhelper.stat.StatType;
import at.happynev.mwoscoreboardhelper.tracer.ScreenshotType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by Nev on 29.07.2017.
 */
public class StatCalculatorRanking extends StatCalculator {

    private final StatType statType;
    private final boolean ascending;

    public StatCalculatorRanking(StatType statType, boolean ascending) {
        this.statType = statType;
        this.ascending = ascending;
    }

    @Override
    public String calculateCurrentValue(Collection<PlayerMatchRecord> records, PlayerMatchRecord currentRecord, List<String> previousValues) {
        Collection<PlayerMatchRecord> validRecords = StatCalculatorHelpers.filterValidRecords(records, statType);
        List<PlayerMatchRecord> sorted = new ArrayList<>(validRecords);
        StatCalculatorHelpers.sortByStat(sorted, statType);
        if (ascending) {
            Collections.reverse(sorted);
        }
        int currentRank = sorted.indexOf(currentRecord);
        if (currentRank == -1) {
            //not found
            return "?";
        }
        int rank = sorted.size() - currentRank;
        return rank + " of " + sorted.size();
    }

    @Override
    public StatExplanationStep getStepDescription() {
        return new StatExplanationStep(GuiUtils.DEFAULT_FRONT_COLOR, "ranked by " + statType.getDescription());
    }

    @Override
    public boolean canDisplay(ScreenshotType type, StatTable table) {
        return statType.canDisplay(type, table);
    }
}
