package at.happynev.mwoscoreboardhelper.stat.filter;

import at.happynev.mwoscoreboardhelper.PlayerMatchRecord;
import at.happynev.mwoscoreboardhelper.stat.StatCalculationWorkingSet;
import at.happynev.mwoscoreboardhelper.stat.StatPipelineStep;
import at.happynev.mwoscoreboardhelper.stat.StatTable;
import at.happynev.mwoscoreboardhelper.tracer.ScreenshotType;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Nev on 29.07.2017.
 */
public abstract class RecordFilter implements StatPipelineStep {

    protected abstract boolean accept(Set<PlayerMatchRecord> records, PlayerMatchRecord pmr, PlayerMatchRecord reference);

    @Override
    public StatCalculationWorkingSet calculateStep(StatCalculationWorkingSet input) {
        StatCalculationWorkingSet ret = new StatCalculationWorkingSet(input);
        Set<PlayerMatchRecord> filtered = input.getRecords().parallelStream().filter(playerMatchRecord -> accept(input.getRecords(), playerMatchRecord, input.getReference())).collect(Collectors.toSet());
        ret.setRecords(filtered);
        ret.addStepExplanation(this.getStepDescription() + " --> " + filtered.size() + " records");
        return ret;
    }

    @Override
    public boolean canDisplay(ScreenshotType type, StatTable table) {
        return true;
    }
}
