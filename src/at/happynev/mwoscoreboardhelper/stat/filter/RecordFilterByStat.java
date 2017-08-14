package at.happynev.mwoscoreboardhelper.stat.filter;

import at.happynev.mwoscoreboardhelper.GuiUtils;
import at.happynev.mwoscoreboardhelper.PlayerMatchRecord;
import at.happynev.mwoscoreboardhelper.stat.StatExplanationStep;
import at.happynev.mwoscoreboardhelper.stat.StatType;

import java.util.Collection;

/**
 * Created by Nev on 29.07.2017.
 */
public class RecordFilterByStat extends RecordFilter {

    private final StatType stat;
    private final String refValue;

    public RecordFilterByStat(StatType stat, String... parameters) {
        this.stat = stat;
        if (parameters.length > 0) {
            refValue = parameters[0];
        } else {
            refValue = null;
        }
    }

    @Override
    public boolean accept(Collection<PlayerMatchRecord> records, PlayerMatchRecord pmr, PlayerMatchRecord reference) {
        String value = pmr.getMatchValues().get(stat);
        String ref = reference.getMatchValues().get(stat);
        if (refValue != null) {
            ref = refValue;
        }
        if (value == null) {
            return false;
        }
        return value.equals(ref);
    }

    @Override
    public StatExplanationStep getStepDescription() {
        String text = "filtered by " + stat.getDescription();
        if (refValue != null) {
            text += " (" + refValue + ")";
        }
        return new StatExplanationStep(GuiUtils.DEFAULT_FRONT_COLOR, text);
    }
}
