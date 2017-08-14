package at.happynev.mwoscoreboardhelper.stat;

import at.happynev.mwoscoreboardhelper.tracer.ScreenshotType;

/**
 * Created by Nev on 06.08.2017.
 */
public interface StatPipelineStep {
    StatCalculationWorkingSet calculateStep(StatCalculationWorkingSet input);

    StatExplanationStep getStepDescription();

    boolean canDisplay(ScreenshotType type, StatTable table);
}
