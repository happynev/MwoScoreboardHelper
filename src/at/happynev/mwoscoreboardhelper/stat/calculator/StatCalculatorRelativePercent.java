package at.happynev.mwoscoreboardhelper.stat.calculator;

import at.happynev.mwoscoreboardhelper.PlayerMatchRecord;
import at.happynev.mwoscoreboardhelper.Utils;
import at.happynev.mwoscoreboardhelper.stat.StatTable;
import at.happynev.mwoscoreboardhelper.stat.StatType;
import at.happynev.mwoscoreboardhelper.tracer.ScreenshotType;

import java.util.Set;

/**
 * Created by Nev on 29.07.2017.
 */
public class StatCalculatorRelativePercent extends StatCalculator {
    private final StatType statType;

    public StatCalculatorRelativePercent(StatType statType) {
        this.statType = statType;
    }

    @Override
    public String calculateCurrentValue(Set<PlayerMatchRecord> records, PlayerMatchRecord currentRecord, String previousValue) {
        try {
            Double current = Double.parseDouble(currentRecord.getMatchValues().getOrDefault(statType, "0"));
            Double historic = Double.parseDouble(previousValue);
            if (current == 0.0d && historic == 0.0d) {
                return "0%"; //100% of 0 ist still 0%
            }
            return Utils.getPercentage(current, historic);
        } catch (NumberFormatException e) {
            return "NaN";
        }
    }

    @Override
    public String getStepDescription() {
        return "as base for current " + statType.getDescription();
    }

    @Override
    public boolean canDisplay(ScreenshotType type, StatTable table) {
        return statType.canDisplay(type, table);
    }
}
