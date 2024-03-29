package at.happynev.mwoscoreboardhelper.stat.filter;

import at.happynev.mwoscoreboardhelper.GuiUtils;
import at.happynev.mwoscoreboardhelper.PlayerMatchRecord;
import at.happynev.mwoscoreboardhelper.stat.StatCalculationWorkingSet;
import at.happynev.mwoscoreboardhelper.stat.StatExplanationStep;

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
        input.getRecords().addAll(PlayerMatchRecord.getAllRecords().values());
        input.addStepExplanation(new StatExplanationStep(this.getStepDescription(), input.getRecords().size() + " records"));
        return input;
    }

    @Override
    public StatExplanationStep getStepDescription() {
        return new StatExplanationStep(GuiUtils.DEFAULT_FRONT_COLOR, "reset filters");
    }
}
