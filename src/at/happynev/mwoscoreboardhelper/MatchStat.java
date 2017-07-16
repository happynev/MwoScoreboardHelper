package at.happynev.mwoscoreboardhelper;

import at.happynev.mwoscoreboardhelper.tracer.ScreenshotType;
import javafx.scene.paint.Color;

/**
 * Created by Nev on 02.02.2017.
 */
public enum MatchStat implements DisplayableStat {
    MATCHSCORE,
    MATCHDAMAGE,
    MATCHPERF,
    MATCHMECHPERF,
    MATCHSTATUS,
    MATCHKILLS,
    MATCHASSISTS,
    MATCHMECH,
    MATCHTONS,
    MATCHPING;

    @Override
    public boolean canDisplay(ScreenshotType type) {
        switch (type) {
            case QP_1PREPARATION:
                switch (this) {
                    case MATCHMECH:
                    case MATCHTONS:
                    case MATCHPING:
                        return true;
                    default:
                        return false;
                }
            case QP_4SUMMARY:
                return true;
        }
        return false;
    }

    @Override
    public Color getColor() {
        switch (this) {
            case MATCHSCORE:
            case MATCHDAMAGE:
            case MATCHSTATUS:
            case MATCHKILLS:
            case MATCHASSISTS:
            case MATCHMECH:
            case MATCHTONS:
            case MATCHPING:
                return COLOR_MATCHDATA;
            default:
                return COLOR_PLAYERMATCHDATA;
        }
    }

    @Override
    public String toString() {
        switch (this) {
            case MATCHSCORE:
                return "Score";
            case MATCHDAMAGE:
                return "Dmg";
            case MATCHSTATUS:
                return "Status";
            case MATCHKILLS:
                return "K";
            case MATCHASSISTS:
                return "A";
            case MATCHMECH:
                return "Mech";
            case MATCHTONS:
                return "tons";
            case MATCHPING:
                return "P";
            case MATCHPERF:
                return "Score%";
            case MATCHMECHPERF:
                return "Mech%";
        }
        return "undefined";
    }

    @Override
    public String getDescription() {
        switch (this) {
            case MATCHSCORE:
                return "Matchscore";
            case MATCHDAMAGE:
                return "Damage";
            case MATCHSTATUS:
                return "Status";
            case MATCHKILLS:
                return "Kills";
            case MATCHASSISTS:
                return "Assists";
            case MATCHMECH:
                return "Mech";
            case MATCHTONS:
                return "tons";
            case MATCHPING:
                return "Ping";
            case MATCHPERF:
                return "Relative score";
            case MATCHMECHPERF:
                return "Mech relative Score";
        }
        return "undefined";
    }
}
