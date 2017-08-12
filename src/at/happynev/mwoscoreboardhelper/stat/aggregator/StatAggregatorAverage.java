package at.happynev.mwoscoreboardhelper.stat.aggregator;

import at.happynev.mwoscoreboardhelper.PlayerMatchRecord;
import at.happynev.mwoscoreboardhelper.stat.StatType;
import at.happynev.mwoscoreboardhelper.stat.calculator.StatCalculatorHelpers;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Created by Nev on 06.08.2017.
 */
public class StatAggregatorAverage extends StatAggregator {

    private final StatType statType;

    public StatAggregatorAverage(StatType statType) {
        this.statType = statType;
    }

    @Override
    public String aggregateValue(Set<PlayerMatchRecord> allRecords) {
        switch (statType) {
            case STATUS:
            case MECH_VARIANT:
            case MECH_CHASSIS:
            case MECH_FACTION:
            case MECH_CLASS:
                return "NaN";
        }
        int sum = 0;
        int valid = 0;
        for (PlayerMatchRecord pmr : allRecords) {
            if (StatCalculatorHelpers.isValidRecord(pmr, statType)) {
                sum += Integer.parseInt(pmr.getMatchValues().getOrDefault(statType, "0"));
                valid++;
            }
        }
        if (valid == 0) {
            return "0";
        }
        BigDecimal ret = new BigDecimal(sum).divide(new BigDecimal(valid), 2, BigDecimal.ROUND_HALF_UP);
        return ret.toPlainString();
    }

    @Override
    public String getStepDescription() {
        return "average " + statType.getDescription();
    }
}
