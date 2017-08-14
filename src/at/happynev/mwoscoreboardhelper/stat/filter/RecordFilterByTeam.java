package at.happynev.mwoscoreboardhelper.stat.filter;

import at.happynev.mwoscoreboardhelper.GuiUtils;
import at.happynev.mwoscoreboardhelper.PlayerMatchRecord;
import at.happynev.mwoscoreboardhelper.stat.StatExplanationStep;

import java.util.Collection;

/**
 * Created by Nev on 29.07.2017.
 */
public class RecordFilterByTeam extends RecordFilter {

    @Override
    public boolean accept(Collection<PlayerMatchRecord> records, PlayerMatchRecord pmr, PlayerMatchRecord reference) {
        return pmr.isEnemy() == reference.isEnemy();
    }

    @Override
    public StatExplanationStep getStepDescription() {
        return new StatExplanationStep(GuiUtils.DEFAULT_FRONT_COLOR, "filtered by team");
    }
}
