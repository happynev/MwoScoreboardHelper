package at.happynev.mwoscoreboardhelper.stat.calculator;

import at.happynev.mwoscoreboardhelper.PlayerMatchRecord;
import at.happynev.mwoscoreboardhelper.stat.StatCalculationWorkingSet;
import at.happynev.mwoscoreboardhelper.stat.StatPipelineStep;

import java.util.Collection;
import java.util.List;

/**
 * Created by Nev on 29.07.2017.
 */
public abstract class StatCalculator implements StatPipelineStep {
    protected abstract String calculateCurrentValue(Collection<PlayerMatchRecord> records, PlayerMatchRecord currentRecord, List<String> previousValues);

    @Override
    public StatCalculationWorkingSet calculateStep(StatCalculationWorkingSet input) {
        StatCalculationWorkingSet ret = new StatCalculationWorkingSet(input);
        String result = calculateCurrentValue(input.getRecords(), input.getReference(), input.getResultValues());
        ret.getResultValues().add(result);
        ret.addStepExplanation(this.getStepDescription() + " --> " + result);
        return ret;
    }
}
