package at.happynev.mwoscoreboardhelper;

/**
 * Created by Nev on 02.02.2017.
 */
public enum Stat {
    FAVMECHS,
    BESTMECHS,
    TIMESSEEN,
    TIMESFINISHED,
    AVGSCORE,
    AVGDAMAGE,
    SURVIVAL,
    AVGKILLS,
    AVGASSISTS;

    @Override
    public String toString() {
        switch (this) {
            case FAVMECHS:
                return "Fav. Mechs[#]";
            case BESTMECHS:
                return "Best Mechs";
            case TIMESSEEN:
                return "# Seen";
            case TIMESFINISHED:
                return "# Stats";
            case AVGSCORE:
                return "Avg. Score";
            case AVGDAMAGE:
                return "Avg. Damage";
            case SURVIVAL:
                return "Survival Rate";
            case AVGKILLS:
                return "Avg. Kills";
            case AVGASSISTS:
                return "Avg. Assists";
        }
        return "undefined";
    }
}
