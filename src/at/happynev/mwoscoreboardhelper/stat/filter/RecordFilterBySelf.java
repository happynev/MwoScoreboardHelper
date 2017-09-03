package at.happynev.mwoscoreboardhelper.stat.filter;

import at.happynev.mwoscoreboardhelper.PlayerRuntime;
import at.happynev.mwoscoreboardhelper.SettingsTabController;

/**
 * Created by Nev on 29.07.2017.
 */
public class RecordFilterBySelf extends RecordFilterByPlayer {
    public RecordFilterBySelf() {
        super("" + PlayerRuntime.getInstance(SettingsTabController.getPlayername()).getId());
    }
}
