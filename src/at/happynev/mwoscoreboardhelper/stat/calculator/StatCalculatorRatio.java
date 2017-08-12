package at.happynev.mwoscoreboardhelper.stat.calculator;

import at.happynev.mwoscoreboardhelper.PlayerMatchRecord;
import at.happynev.mwoscoreboardhelper.Utils;
import at.happynev.mwoscoreboardhelper.stat.StatTable;
import at.happynev.mwoscoreboardhelper.stat.StatType;
import at.happynev.mwoscoreboardhelper.tracer.ScreenshotType;

import java.util.Collection;

/**
 * Created by Nev on 29.07.2017.
 */
public class StatCalculatorRatio extends StatCalculator {
    private final StatType statType;

    public StatCalculatorRatio(StatType statType) {
        this.statType = statType;
    }

    @Override
    public String calculateCurrentValue(Collection<PlayerMatchRecord> records, PlayerMatchRecord currentRecord, String previousValue) {
        try {
            Double current = Double.parseDouble(currentRecord.getMatchValues().getOrDefault(statType, "0"));
            Double historic = Double.parseDouble(previousValue);
            if (current == 0 && historic == 0) {
                return "0.00"; //100% of 0 ist still 0%
            }
            return Utils.getRatio(current, historic);
        } catch (NumberFormatException e) {
            return "NaN";
        }
    }

    @Override
    public String getStepDescription() {
        return "divided by current current " + statType.getDescription();
    }

    @Override
    public boolean canDisplay(ScreenshotType type, StatTable table) {
        return statType.canDisplay(type, table);
    }
}
