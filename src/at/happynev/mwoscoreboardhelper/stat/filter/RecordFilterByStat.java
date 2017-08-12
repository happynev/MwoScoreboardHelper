package at.happynev.mwoscoreboardhelper.stat.filter;

import at.happynev.mwoscoreboardhelper.PlayerMatchRecord;
import at.happynev.mwoscoreboardhelper.stat.StatType;

import java.util.Set;

/**
 * Created by Nev on 29.07.2017.
 */
public class RecordFilterByStat extends RecordFilter {

    private final StatType stat;

    public RecordFilterByStat(StatType stat) {
        this.stat = stat;
    }

    @Override
    public boolean accept(Set<PlayerMatchRecord> records, PlayerMatchRecord pmr, PlayerMatchRecord reference) {
        String value = pmr.getMatchValues().get(stat);
        String refvalue = reference.getMatchValues().get(stat);
        if (value == null) {
            return false;
        }
        return value.equals(refvalue);
    }

    @Override
    public String getStepDescription() {
        return "filtered by " + stat.getDescription();
    }
}
