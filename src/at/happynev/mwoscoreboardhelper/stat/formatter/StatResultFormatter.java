package at.happynev.mwoscoreboardhelper.stat.formatter;

import at.happynev.mwoscoreboardhelper.stat.StatCalculationWorkingSet;
import at.happynev.mwoscoreboardhelper.stat.StatExplanationStep;
import at.happynev.mwoscoreboardhelper.stat.StatPipelineStep;
import at.happynev.mwoscoreboardhelper.stat.StatTable;
import at.happynev.mwoscoreboardhelper.tracer.ScreenshotType;

/**
 * Created by Nev on 14.08.2017.
 */
public abstract class StatResultFormatter implements StatPipelineStep {
    @Override
    public abstract StatCalculationWorkingSet calculateStep(StatCalculationWorkingSet input);

    @Override
    public abstract StatExplanationStep getStepDescription();

    @Override
    public boolean canDisplay(ScreenshotType type, StatTable table) {
        return true;
    }
}
