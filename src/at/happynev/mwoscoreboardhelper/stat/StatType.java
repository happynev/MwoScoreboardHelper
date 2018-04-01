package at.happynev.mwoscoreboardhelper.stat;

import at.happynev.mwoscoreboardhelper.tracer.ScreenshotType;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by Nev on 02.02.2017.
 */
public enum StatType {
    SCORE,
    DAMAGE,
    STATUS,
    KILLS,
    ASSISTS,
    MECH_VARIANT,
    MECH_TONS,
    MECH_FACTION,
    MECH_CHASSIS,
    MECH_CLASS,
    PING,
    KMDDS,
    SOLO_KILLS,
    COMPONENT_DESTROYED,
    REWARD_CBILLS,
    REWARD_XP,
    WINS,
    LOSSES,
    MATCHES,
    GAMEMODE,
    MAP,
    ISEN_RANK,
    ISEN_PERCENTILE,
    ISEN_GAMESPLAYED,
    ISEN_WLRATIO,
    ISEN_SURVIVALRATE,
    ISEN_KDRATIO,
    ISEN_AVGSCORE,
    ISEN_ADJSCORE;

    private static final Collection<StatType> prepTeamVisible = Arrays.asList(
            MECH_VARIANT,
            MECH_TONS,
            MECH_FACTION,
            MECH_CHASSIS,
            MECH_CLASS,
            PING,
            GAMEMODE,
            MAP,
            ISEN_RANK,
            ISEN_PERCENTILE,
            ISEN_GAMESPLAYED,
            ISEN_WLRATIO,
            ISEN_SURVIVALRATE,
            ISEN_KDRATIO,
            ISEN_AVGSCORE,
            ISEN_ADJSCORE);
    private static final Collection<StatType> prepEnemyVisible = Arrays.asList(PING,
            GAMEMODE,
            MAP,
            ISEN_RANK,
            ISEN_PERCENTILE,
            ISEN_GAMESPLAYED,
            ISEN_WLRATIO,
            ISEN_SURVIVALRATE,
            ISEN_KDRATIO,
            ISEN_AVGSCORE,
            ISEN_ADJSCORE);
    private static final Collection<StatType> personal = Arrays.asList(KMDDS, SOLO_KILLS, COMPONENT_DESTROYED, REWARD_CBILLS, REWARD_XP);

    public boolean canDisplay(ScreenshotType type, StatTable table) {
        switch (type) {
            case QP_1PREPARATION:
                switch (table) {
                    case WATCHER_PERSONAL:
                        return prepTeamVisible.contains(this);
                    case WATCHER_TEAM:
                        return prepTeamVisible.contains(this);
                    case WATCHER_ENEMY:
                        return prepEnemyVisible.contains(this);
                    case WATCHER_SIDEBAR:
                        return prepTeamVisible.contains(this);
                }
            case QP_3REWARDS:
                switch (table) {
                    case WATCHER_PERSONAL:
                        return personal.contains(this);
                    default:
                        return false;
                }
            case QP_4SUMMARY:
                return true;
            case UNDEFINED:
                return false;
        }
        return false;
    }

    @Override
    public String toString() {
        switch (this) {
            case SCORE:
                return "Score";
            case DAMAGE:
                return "Dmg";
            case STATUS:
                return "Status";
            case KILLS:
                return "K";
            case ASSISTS:
                return "A";
            case MECH_VARIANT:
                return "Mech";
            case MECH_FACTION:
                return "Faction";
            case MECH_CHASSIS:
                return "Chassis";
            case MECH_CLASS:
                return "Class";
            case MECH_TONS:
                return "tons";
            case PING:
                return "P";
            case KMDDS:
                return "KMDD";
            case SOLO_KILLS:
                return "Solo";
            case COMPONENT_DESTROYED:
                return "Comp";
            case REWARD_CBILLS:
                return "CB";
            case REWARD_XP:
                return "XP";
            case WINS:
                return "Win";
            case LOSSES:
                return "Loss";
            case MATCHES:
                return "Match";
            case GAMEMODE:
                return "Mode";
            case MAP:
                return "Map";
            case ISEN_RANK:
                return "JLRank";
            case ISEN_PERCENTILE:
                return "JL%";
            case ISEN_GAMESPLAYED:
                return "JL#";
            case ISEN_WLRATIO:
                return "JLWLR";
            case ISEN_SURVIVALRATE:
                return "JLSrv";
            case ISEN_KDRATIO:
                return "JLKDR";
            case ISEN_AVGSCORE:
                return "JLAvg";
            case ISEN_ADJSCORE:
                return "JLAdj";
        }
        return "undefined";
    }

    public String getDescription() {
        switch (this) {
            case SCORE:
                return "Matchscore";
            case DAMAGE:
                return "Damage";
            case STATUS:
                return "Status";
            case KILLS:
                return "Kills";
            case ASSISTS:
                return "Assists";
            case MECH_VARIANT:
                return "Mech";
            case MECH_FACTION:
                return "Faction";
            case MECH_CHASSIS:
                return "Mech chassis";
            case MECH_CLASS:
                return "Mech weightclass";
            case MECH_TONS:
                return "tons";
            case PING:
                return "Ping";
            case KMDDS:
                return "Kills most damage dealt";
            case SOLO_KILLS:
                return "Solo kills";
            case COMPONENT_DESTROYED:
                return "Components destroyed";
            case REWARD_CBILLS:
                return "C-Bills earned";
            case REWARD_XP:
                return "XP earned";
            case WINS:
                return "Match won";
            case LOSSES:
                return "Match lost";
            case MATCHES:
                return "Match finished";
            case GAMEMODE:
                return "Game Mode";
            case MAP:
                return "Map";
            case ISEN_RANK:
                return "Rank on the Jarl's List";
            case ISEN_PERCENTILE:
                return "Percentile on the Jarl's List";
            case ISEN_GAMESPLAYED:
                return "Games played on the Jarl's List";
            case ISEN_WLRATIO:
                return "Win/Loss ratio on the Jarl's List";
            case ISEN_SURVIVALRATE:
                return "Survive rate on the Jarl's List";
            case ISEN_KDRATIO:
                return "Kill/Death ratio on the Jarl's List";
            case ISEN_AVGSCORE:
                return "Average Score on the Jarl's List";
            case ISEN_ADJSCORE:
                return "Adjusted Score on the Jarl's List";
        }
        return "undefined";
    }
}
