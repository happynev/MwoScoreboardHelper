package at.happynev.mwoscoreboardhelper.stat.calculator;

import at.happynev.mwoscoreboardhelper.GuiUtils;
import at.happynev.mwoscoreboardhelper.Logger;
import at.happynev.mwoscoreboardhelper.PlayerMatchRecord;
import at.happynev.mwoscoreboardhelper.stat.StatExplanationStep;
import at.happynev.mwoscoreboardhelper.stat.StatTable;
import at.happynev.mwoscoreboardhelper.tracer.ScreenshotType;

import java.util.Collection;
import java.util.List;

/**
 * Created by Nev on 29.07.2017.
 */
public class StatCalculatorNumericAverage extends StatCalculator {

    @Override
    public String calculateCurrentValue(Collection<PlayerMatchRecord> records, PlayerMatchRecord currentRecord, List<String> previousValues) {
        try {
            if (previousValues.size() == 0) {
                return "?";
            }
            long sum = 0;
            for (String value : previousValues) {
                Logger.log("calculating average: " + sum + " +" + value+" for "+currentRecord.getPlayerId());
                int v = Integer.parseInt(value.replaceAll("\\D", ""));
                sum += v;
            }
            long result = sum / previousValues.size();
            return "" + result;
        } catch (NumberFormatException e) {
            return "NaN";
        }
    }

    @Override
    public StatExplanationStep getStepDescription() {
        return new StatExplanationStep(GuiUtils.DEFAULT_FRONT_COLOR, "Average of previous values");
    }

    @Override
    public boolean canDisplay(ScreenshotType type, StatTable table) {
        return true;
    }
}
