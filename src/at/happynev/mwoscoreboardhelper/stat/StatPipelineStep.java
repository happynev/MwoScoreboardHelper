package at.happynev.mwoscoreboardhelper.stat;

/**
 * Created by Nev on 06.08.2017.
 */
public interface StatPipelineStep {
    StatCalculationWorkingSet calculateStep(StatCalculationWorkingSet input);

    String getStepDescription();
}
