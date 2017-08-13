package at.happynev.mwoscoreboardhelper.stat.filter;

import at.happynev.mwoscoreboardhelper.PlayerMatchRecord;
import at.happynev.mwoscoreboardhelper.stat.StatCalculationWorkingSet;

import java.util.Collection;

/**
 * Created by Nev on 13.08.2017.
 */
public class RecordFilterReset extends RecordFilter {
    @Override
    protected boolean accept(Collection<PlayerMatchRecord> records, PlayerMatchRecord pmr, PlayerMatchRecord reference) {
        //dummy, never called
        return false;
    }

    @Override
    public StatCalculationWorkingSet calculateStep(StatCalculationWorkingSet input) {
        input.getRecords().clear();
        input.getRecords().addAll(PlayerMatchRecord.getAllRecords());
        input.addStepExplanation(this.getStepDescription() + " --> " + input.getRecords().size() + " records");
        return input;
    }

    @Override
    public String getStepDescription() {
        return "reset filters";
    }
}
