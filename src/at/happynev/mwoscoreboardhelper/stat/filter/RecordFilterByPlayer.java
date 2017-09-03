package at.happynev.mwoscoreboardhelper.stat.filter;

import at.happynev.mwoscoreboardhelper.PlayerMatchRecord;
import at.happynev.mwoscoreboardhelper.stat.StatExplanationStep;
import javafx.scene.paint.Color;

import java.util.Collection;

/**
 * Created by Nev on 29.07.2017.
 */
public class RecordFilterByPlayer extends RecordFilter {
    private final int playerId;

    public RecordFilterByPlayer(String... parameters) {
        if (parameters != null && parameters.length > 0) {
            playerId = Integer.valueOf(parameters[0]);
        } else {
            playerId = -1;
        }
    }

    @Override
    public boolean accept(Collection<PlayerMatchRecord> records, PlayerMatchRecord pmr, PlayerMatchRecord reference) {
        int refid = reference.getPlayerId();
        if (playerId >= 0) {
            refid = playerId;
        }
        return pmr.getPlayerId() == refid;
    }

    @Override
    public StatExplanationStep getStepDescription() {
        return new StatExplanationStep(Color.CORNFLOWERBLUE, "filtered by player");
    }
}
