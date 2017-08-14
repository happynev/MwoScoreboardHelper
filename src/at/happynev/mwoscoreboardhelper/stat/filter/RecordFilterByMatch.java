package at.happynev.mwoscoreboardhelper.stat.filter;

import at.happynev.mwoscoreboardhelper.PlayerMatchRecord;
import at.happynev.mwoscoreboardhelper.stat.StatExplanationStep;
import javafx.scene.paint.Color;

import java.util.Collection;

/**
 * Created by Nev on 29.07.2017.
 */
public class RecordFilterByMatch extends RecordFilter {
    @Override
    public boolean accept(Collection<PlayerMatchRecord> records, PlayerMatchRecord pmr, PlayerMatchRecord reference) {
        return pmr.getMatchId() == reference.getMatchId();
    }

    @Override
    public StatExplanationStep getStepDescription() {
        return new StatExplanationStep(Color.YELLOW, "filtered by match");
    }
}
