package at.happynev.mwoscoreboardhelper.stat.calculator;

import at.happynev.mwoscoreboardhelper.GuiUtils;
import at.happynev.mwoscoreboardhelper.PlayerMatchRecord;
import at.happynev.mwoscoreboardhelper.Utils;
import at.happynev.mwoscoreboardhelper.stat.StatExplanationStep;
import at.happynev.mwoscoreboardhelper.stat.StatTable;
import at.happynev.mwoscoreboardhelper.tracer.ScreenshotType;

import java.util.Collection;
import java.util.List;

/**
 * Created by Nev on 29.07.2017.
 */
public class StatCalculatorRatio extends StatCalculator {
    @Override
    public String calculateCurrentValue(Collection<PlayerMatchRecord> records, PlayerMatchRecord currentRecord, List<String> previousValues) {
        try {
            if (previousValues.size() < 2) {
                return "?";
            }
            Double current = Double.parseDouble(previousValues.get(previousValues.size() - 1));
            Double historic = Double.parseDouble(previousValues.get(previousValues.size() - 2));
            if (current == 0 && historic == 0) {
                return "0.00"; //100% of 0 ist still 0%
            }
            return Utils.getRatio(current, historic);
        } catch (NumberFormatException e) {
            return "NaN";
        }
    }

    @Override
    public StatExplanationStep getStepDescription() {
        return new StatExplanationStep(GuiUtils.DEFAULT_FRONT_COLOR, "ratio between last 2 results");
    }

    @Override
    public boolean canDisplay(ScreenshotType type, StatTable table) {
        return true;
    }
}
