package at.happynev.mwoscoreboardhelper.stat.filter;

import at.happynev.mwoscoreboardhelper.MatchRuntime;
import at.happynev.mwoscoreboardhelper.PlayerMatchRecord;

import java.util.Calendar;
import java.util.Set;

/**
 * Created by Nev on 29.07.2017.
 */
public class RecordFilterNewerThan extends RecordFilter {

    private final int days;

    public RecordFilterNewerThan(int days) {
        this.days = days;
    }

    @Override
    public boolean accept(Set<PlayerMatchRecord> records, PlayerMatchRecord pmr, PlayerMatchRecord reference) {
        MatchRuntime match = MatchRuntime.getInstanceById(pmr.getMatchId());
        MatchRuntime refmatch = MatchRuntime.getInstanceById(pmr.getMatchId());
        Calendar check = Calendar.getInstance();
        check.setTimeInMillis(match.getTimestamp());
        check.add(Calendar.DAY_OF_YEAR, days);
        return check.getTimeInMillis() > match.getTimestamp();
    }

    @Override
    public String getStepDescription() {
        return "filtered newer than " + days + " days";
    }
}
