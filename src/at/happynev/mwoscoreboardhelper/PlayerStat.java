package at.happynev.mwoscoreboardhelper;

/**
 * Created by Nev on 02.02.2017.
 */
public enum PlayerStat implements Stat {
    TIMESSEEN,
    TIMESFINISHED,
    AVGSCORE,
    AVGDAMAGE,
    SURVIVAL,
    AVGKILLS,
    AVGASSISTS,
    KDR,
    FAVMECHS,
    BESTMECHS;

    @Override
    public String toString() {
        switch (this) {
            case FAVMECHS:
                return "Fav. Mechs";
            case BESTMECHS:
                return "Best Mechs";
            case TIMESSEEN:
                return "#Seen";
            case TIMESFINISHED:
                return "#Stats";
            case AVGSCORE:
                return "~Score";
            case AVGDAMAGE:
                return "~Dmg";
            case SURVIVAL:
                return "Survival";
            case AVGKILLS:
                return "~K";
            case AVGASSISTS:
                return "~A";
            case KDR:
                return "K/D";
        }
        return "undefined";
    }

    @Override
    public String getDescription() {
        switch (this) {
            case FAVMECHS:
                return "Most used Mechs";
            case BESTMECHS:
                return "Most effective Mechs";
            case TIMESSEEN:
                return "Times encountered";
            case TIMESFINISHED:
                return "Number of matches recorded";
            case AVGSCORE:
                return "Average Score per Match";
            case AVGDAMAGE:
                return "Average Damage per Match";
            case SURVIVAL:
                return "Survival Rate";
            case AVGKILLS:
                return "Average Kills per Match";
            case AVGASSISTS:
                return "Average Assists per Match";
            case KDR:
                return "Kill/Death Ratio";
        }
        return "undefined";
    }

}
