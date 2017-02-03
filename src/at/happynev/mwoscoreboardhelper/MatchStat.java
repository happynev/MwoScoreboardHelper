package at.happynev.mwoscoreboardhelper;

/**
 * Created by Nev on 02.02.2017.
 */
public enum MatchStat implements Stat {
    MATCHSCORE,
    MATCHDAMAGE,
    MATCHSTATUS,
    MATCHKILLS,
    MATCHASSISTS,
    MATCHMECH,
    MATCHTONS,
    MATCHPING;

   /* @Override
    public String toString() {
        switch (this) {

        }
        return "" + this;
    }*/

    @Override
    public String getDescription() {
        return "desc:" + this.toString();
    }
}
