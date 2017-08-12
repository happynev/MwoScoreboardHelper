package at.happynev.mwoscoreboardhelper.stat.filter;

import at.happynev.mwoscoreboardhelper.PlayerMatchRecord;
import at.happynev.mwoscoreboardhelper.PlayerRuntime;
import at.happynev.mwoscoreboardhelper.SettingsTabController;

import java.util.Collection;

/**
 * Created by Nev on 29.07.2017.
 */
public class RecordFilterBySelf extends RecordFilterByPlayer {

    private final PlayerRuntime self = PlayerRuntime.getInstance(SettingsTabController.getPlayername());

    @Override
    public boolean accept(Collection<PlayerMatchRecord> records, PlayerMatchRecord pmr, PlayerMatchRecord reference) {
        return pmr.getPlayerId() == self.getId();
    }

    @Override
    public String getStepDescription() {
        return "filtered by player " + self.getPilotname();
    }
}
