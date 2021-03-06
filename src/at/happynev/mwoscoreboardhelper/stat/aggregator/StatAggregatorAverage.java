package at.happynev.mwoscoreboardhelper.stat.aggregator;

import at.happynev.mwoscoreboardhelper.GuiUtils;
import at.happynev.mwoscoreboardhelper.PlayerMatchRecord;
import at.happynev.mwoscoreboardhelper.stat.StatExplanationStep;
import at.happynev.mwoscoreboardhelper.stat.StatTable;
import at.happynev.mwoscoreboardhelper.stat.StatType;
import at.happynev.mwoscoreboardhelper.stat.calculator.StatCalculatorHelpers;
import at.happynev.mwoscoreboardhelper.tracer.ScreenshotType;

import java.math.BigDecimal;
import java.util.Collection;

/**
 * Created by Nev on 06.08.2017.
 */
public class StatAggregatorAverage extends StatAggregator {

    private final StatType statType;

    public StatAggregatorAverage(StatType statType) {
        this.statType = statType;
    }

    @Override
    public String aggregateValue(Collection<PlayerMatchRecord> allRecords) {
        boolean allow999999 = true;
        switch (statType) {
            case STATUS:
            case MECH_VARIANT:
            case MECH_CHASSIS:
            case MECH_FACTION:
            case MECH_CLASS:
                return "NaN";
            case ISEN_RANK:
                allow999999 = false;//hack to prevent "retired" rank from affecting average
                break;
        }
        BigDecimal sum = BigDecimal.ZERO;
        int valid = 0;
        for (PlayerMatchRecord pmr : allRecords) {
            if (StatCalculatorHelpers.isValidRecord(pmr, statType)) {
                BigDecimal value = new BigDecimal(pmr.getMatchValues().getOrDefault(statType, "0"));
                if (allow999999 || value.intValue() != 999999) {
                    sum = sum.add(value);
                    valid++;
                }
            }
        }
        if (valid == 0) {
            return "0";
        }
        BigDecimal ret = sum.divide(new BigDecimal(valid), 5, BigDecimal.ROUND_HALF_UP);
        int precision = 2;
        if (ret.abs().doubleValue() > 100 || ret.signum() == 0) {
            precision = 0;
        } else if (ret.abs().doubleValue() > 10) {
            precision = 1;
        } else if (ret.abs().doubleValue() < 1) {
            precision = 3;
        }
        return ret.setScale(precision, BigDecimal.ROUND_HALF_UP).toPlainString();
    }

    @Override
    public StatExplanationStep getStepDescription() {
        return new StatExplanationStep(GuiUtils.DEFAULT_FRONT_COLOR, "average " + statType.getDescription());
    }

    @Override
    public boolean canDisplay(ScreenshotType type, StatTable table) {
        return true;//historic
    }
}
