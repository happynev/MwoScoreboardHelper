package at.happynev.mwoscoreboardhelper.stat.aggregator;

import at.happynev.mwoscoreboardhelper.PlayerMatchRecord;
import at.happynev.mwoscoreboardhelper.stat.StatTable;
import at.happynev.mwoscoreboardhelper.stat.StatType;
import at.happynev.mwoscoreboardhelper.stat.calculator.StatCalculatorHelpers;
import at.happynev.mwoscoreboardhelper.tracer.ScreenshotType;

import java.util.Set;

/**
 * Created by Nev on 06.08.2017.
 */
public class StatAggregatorCountValid extends StatAggregator {
    private final StatType statType;

    public StatAggregatorCountValid(StatType statType) {
        this.statType = statType;
    }

    @Override
    public String aggregateValue(Set<PlayerMatchRecord> allRecords) {
        int count = 0;
        for (PlayerMatchRecord pmr : allRecords) {
            if (StatCalculatorHelpers.isValidRecord(pmr, statType)) {
                count++;
            }
        }
        return "" + count;
    }

    @Override
    public boolean canDisplay(ScreenshotType type, StatTable table) {
        return true; //historic values
    }

    @Override
    public String getStepDescription() {
        return "number of records with " + statType.getDescription();
    }
}
