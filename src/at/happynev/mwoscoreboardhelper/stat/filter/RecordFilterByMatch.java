package at.happynev.mwoscoreboardhelper.stat.filter;

import at.happynev.mwoscoreboardhelper.PlayerMatchRecord;

import java.util.Set;

/**
 * Created by Nev on 29.07.2017.
 */
public class RecordFilterByMatch extends RecordFilter {
    @Override
    public boolean accept(Set<PlayerMatchRecord> records, PlayerMatchRecord pmr, PlayerMatchRecord reference) {
        return pmr.getMatchId() == reference.getMatchId();
    }

    @Override
    public String getStepDescription() {
        return "filtered by match";
    }
}
