package at.happynev.mwoscoreboardhelper.stat.filter;

import at.happynev.mwoscoreboardhelper.PlayerMatchRecord;

import java.util.Collection;
import java.util.Set;

/**
 * Created by Nev on 29.07.2017.
 */
public class RecordFilterByPlayer extends RecordFilter {

    @Override
    public boolean accept(Collection<PlayerMatchRecord> records, PlayerMatchRecord pmr, PlayerMatchRecord reference) {
        return pmr.getPlayerId() == reference.getPlayerId();
    }

    @Override
    public String getStepDescription() {
        return "filtered by player";
    }
}
