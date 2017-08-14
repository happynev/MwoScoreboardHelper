package at.happynev.mwoscoreboardhelper.stat.filter;

import at.happynev.mwoscoreboardhelper.GuiUtils;
import at.happynev.mwoscoreboardhelper.PlayerMatchRecord;
import at.happynev.mwoscoreboardhelper.stat.StatExplanationStep;
import javafx.scene.paint.Color;

import java.util.Collection;

/**
 * Created by Nev on 29.07.2017.
 */
public class RecordFilterByPlayer extends RecordFilter {

    @Override
    public boolean accept(Collection<PlayerMatchRecord> records, PlayerMatchRecord pmr, PlayerMatchRecord reference) {
        return pmr.getPlayerId() == reference.getPlayerId();
    }

    @Override
    public StatExplanationStep getStepDescription() {
        return new StatExplanationStep(Color.CORNFLOWERBLUE, "filtered by player");
    }
}
