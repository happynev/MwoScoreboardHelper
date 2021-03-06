package at.happynev.mwoscoreboardhelper.stat.aggregator;

import at.happynev.mwoscoreboardhelper.GuiUtils;
import at.happynev.mwoscoreboardhelper.PlayerMatchRecord;
import at.happynev.mwoscoreboardhelper.stat.StatExplanationStep;
import at.happynev.mwoscoreboardhelper.stat.StatTable;
import at.happynev.mwoscoreboardhelper.stat.StatType;
import at.happynev.mwoscoreboardhelper.stat.calculator.StatCalculatorHelpers;
import at.happynev.mwoscoreboardhelper.tracer.ScreenshotType;

import java.util.Collection;

/**
 * Created by Nev on 06.08.2017.
 */
public class StatAggregatorSum extends StatAggregator {
    private final StatType statType;

    public StatAggregatorSum(StatType statType) {
        this.statType = statType;
    }

    @Override
    public String aggregateValue(Collection<PlayerMatchRecord> allRecords) {
        switch (statType) {
            case STATUS:
            case MECH_VARIANT:
            case MECH_CHASSIS:
            case MECH_FACTION:
            case MECH_CLASS:
                return "NaN";
        }
        int sum = 0;
        for (PlayerMatchRecord pmr : allRecords) {
            if (StatCalculatorHelpers.isValidRecord(pmr, statType)) {
                sum += Integer.parseInt(pmr.getMatchValues().getOrDefault(statType, "0"));
            }
        }
        return "" + sum;
    }

    @Override
    public boolean canDisplay(ScreenshotType type, StatTable table) {
        return true; //historic values
    }

    @Override
    public StatExplanationStep getStepDescription() {
        return new StatExplanationStep(GuiUtils.DEFAULT_FRONT_COLOR, "sum of " + statType.getDescription());
    }
}
