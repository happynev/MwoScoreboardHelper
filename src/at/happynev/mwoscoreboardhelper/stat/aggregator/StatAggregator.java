package at.happynev.mwoscoreboardhelper.stat.aggregator;

import at.happynev.mwoscoreboardhelper.PlayerMatchRecord;
import at.happynev.mwoscoreboardhelper.stat.StatCalculationWorkingSet;
import at.happynev.mwoscoreboardhelper.stat.StatPipelineStep;

import java.util.Set;

/**
 * Created by Nev on 06.08.2017.
 */
public abstract class StatAggregator implements StatPipelineStep {
    public abstract String aggregateValue(Set<PlayerMatchRecord> allRecords);

    @Override
    public StatCalculationWorkingSet calculateStep(StatCalculationWorkingSet input) {
        StatCalculationWorkingSet ret = new StatCalculationWorkingSet(input);
        String value = aggregateValue(input.getRecords());
        ret.setCurrentValue(value);
        ret.addStepExplanation(this.getStepDescription() + " --> " + value);
        return ret;
    }
}