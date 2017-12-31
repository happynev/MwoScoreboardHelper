package at.happynev.mwoscoreboardhelper.stat.filter;

import at.happynev.mwoscoreboardhelper.GuiUtils;
import at.happynev.mwoscoreboardhelper.PlayerMatchRecord;
import at.happynev.mwoscoreboardhelper.PlayerRuntime;
import at.happynev.mwoscoreboardhelper.SettingsTabController;
import at.happynev.mwoscoreboardhelper.stat.StatExplanationStep;

import java.util.Collection;

/**
 * Created by Nev on 29.07.2017.
 */
public class RecordFilterExcludeSelf extends RecordFilterByPlayer {

    public RecordFilterExcludeSelf() {
        super("" + SettingsTabController.getSelfPlayerInstance().getId());
    }

    @Override
    public boolean accept(Collection<PlayerMatchRecord> records, PlayerMatchRecord pmr, PlayerMatchRecord reference) {
        return !super.accept(records, pmr, reference);
    }

    @Override
    public StatExplanationStep getStepDescription() {
        return new StatExplanationStep(GuiUtils.DEFAULT_FRONT_COLOR, "remove " + SettingsTabController.getSelfPlayerInstance().getPilotname() + "'s records");
    }
}
