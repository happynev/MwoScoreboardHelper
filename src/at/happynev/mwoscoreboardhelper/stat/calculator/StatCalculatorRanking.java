package at.happynev.mwoscoreboardhelper.stat.calculator;

import at.happynev.mwoscoreboardhelper.PlayerMatchRecord;
import at.happynev.mwoscoreboardhelper.stat.StatTable;
import at.happynev.mwoscoreboardhelper.stat.StatType;
import at.happynev.mwoscoreboardhelper.tracer.ScreenshotType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created by Nev on 29.07.2017.
 */
public class StatCalculatorRanking extends StatCalculator {

    private final StatType statType;

    public StatCalculatorRanking(StatType statType) {
        this.statType = statType;
    }

    @Override
    public String calculateCurrentValue(Collection<PlayerMatchRecord> records, PlayerMatchRecord currentRecord, String previousValue) {
        Collection<PlayerMatchRecord> validRecords = StatCalculatorHelpers.filterValidRecords(records, statType);
        if (validRecords.size() != records.size()) {
            //some records are invalid, ranking makes no sense
            return "?";
        }
        List<PlayerMatchRecord> sorted = new ArrayList<>(validRecords);
        StatCalculatorHelpers.sortByStat(sorted, statType);
        int currentRank = sorted.indexOf(currentRecord);
        int rank = sorted.size() - currentRank;
        return rank + " of " + sorted.size();
    }

    @Override
    public String getStepDescription() {
        return "ranked by " + statType.getDescription();
    }

    @Override
    public boolean canDisplay(ScreenshotType type, StatTable table) {
        return statType.canDisplay(type, table);
    }
}
