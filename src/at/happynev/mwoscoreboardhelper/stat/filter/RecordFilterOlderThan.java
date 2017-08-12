package at.happynev.mwoscoreboardhelper.stat.filter;

import at.happynev.mwoscoreboardhelper.MatchRuntime;
import at.happynev.mwoscoreboardhelper.PlayerMatchRecord;

import java.util.Collection;

/**
 * Created by Nev on 29.07.2017.
 */
public class RecordFilterOlderThan extends RecordFilter {

    private static final long factorMillis = 24 * 60 * 60 * 1000;
    private final int days;

    public RecordFilterOlderThan(String[] parameters) {
        try {
            this.days = Integer.parseInt(parameters[0]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public boolean accept(Collection<PlayerMatchRecord> records, PlayerMatchRecord pmr, PlayerMatchRecord reference) {
        MatchRuntime match = MatchRuntime.getInstanceById(pmr.getMatchId());
        MatchRuntime refmatch = MatchRuntime.getInstanceById(reference.getMatchId());
        long diff = refmatch.getTimestamp() - match.getTimestamp();
        return diff > (days * factorMillis);
    }

    @Override
    public String getStepDescription() {
        return "filtered older than " + days + " days";
    }
}
