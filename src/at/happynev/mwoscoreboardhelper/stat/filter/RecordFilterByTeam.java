package at.happynev.mwoscoreboardhelper.stat.filter;

import at.happynev.mwoscoreboardhelper.PlayerMatchRecord;

import java.util.Set;

/**
 * Created by Nev on 29.07.2017.
 */
public class RecordFilterByTeam extends RecordFilter {

    @Override
    public boolean accept(Set<PlayerMatchRecord> records, PlayerMatchRecord pmr, PlayerMatchRecord reference) {
        return pmr.isEnemy() == reference.isEnemy();
    }

    @Override
    public String getStepDescription() {
        return "filtered by team";
    }
}
