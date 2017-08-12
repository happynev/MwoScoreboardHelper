package at.happynev.mwoscoreboardhelper.stat.calculator;

import at.happynev.mwoscoreboardhelper.PlayerMatchRecord;
import at.happynev.mwoscoreboardhelper.stat.StatType;

import java.util.Set;

/**
 * Created by Nev on 29.07.2017.
 */
public class StatCalculatorRawValue extends StatCalculator {
    private final StatType statType;

    public StatCalculatorRawValue(StatType statType) {
        this.statType = statType;
    }

    @Override
    public String calculateCurrentValue(Set<PlayerMatchRecord> records, PlayerMatchRecord currentRecord, String previousValue) {
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
}
