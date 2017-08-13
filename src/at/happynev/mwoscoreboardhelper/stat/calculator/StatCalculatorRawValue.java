package at.happynev.mwoscoreboardhelper.stat.calculator;

import at.happynev.mwoscoreboardhelper.PlayerMatchRecord;
import at.happynev.mwoscoreboardhelper.stat.StatTable;
import at.happynev.mwoscoreboardhelper.stat.StatType;
import at.happynev.mwoscoreboardhelper.tracer.ScreenshotType;

import java.util.Collection;
import java.util.List;

/**
 * Created by Nev on 29.07.2017.
 */
public class StatCalculatorRawValue extends StatCalculator {
    private final StatType statType;

    public StatCalculatorRawValue(StatType statType) {
        this.statType = statType;
    }

    @Override
    public String calculateCurrentValue(Collection<PlayerMatchRecord> records, PlayerMatchRecord currentRecord, List<String> previousValues) {
        String val = currentRecord.getMatchValues().get(statType);
        if (val == null || val.isEmpty()) {
            val = "?";
        }
        return val;
    }

    @Override
    public String getStepDescription() {
        return "traced " + statType.getDescription();
    }

    @Override
    public boolean canDisplay(ScreenshotType type, StatTable table) {
        return statType.canDisplay(type, table);
    }
}
