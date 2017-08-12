package at.happynev.mwoscoreboardhelper.stat;

/**
 * Created by Nev on 29.07.2017.
 */
public enum StatTable {
    WATCHER_PERSONAL,
    WATCHER_TEAM,
    WATCHER_ENEMY;

    @Override
    public String toString() {
        switch (this) {
            case WATCHER_PERSONAL:
                return "Watcher tab: Personal";
            case WATCHER_TEAM:
                return "Watcher tab: Your Team";
            case WATCHER_ENEMY:
                return "Watcher tab: Your Enemy";
        }
        return "undefined";
    }
}
