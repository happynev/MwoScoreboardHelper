package at.happynev.mwoscoreboardhelper.stat.filter;

import at.happynev.mwoscoreboardhelper.stat.StatType;

/**
 * Created by Nev on 06.08.2017.
 */
public enum RecordFilterType {
    MATCH,
    PLAYER,
    TEAM,
    SELF,
    NOTSELF,
    MECHVARIANT,
    MECHCHASSIS,
    MECHTONS,
    MECHCLASS,
    MECHFACTION,
    MATCHRECENT,
    MATCHOLD,
    GAMEMODE,
    MAP,
    RESET;

    public RecordFilter getInstance(String... parameters) {
        switch (this) {
            case MATCH:
                return new RecordFilterByMatch();
            case PLAYER:
                return new RecordFilterByPlayer(parameters);
            case TEAM:
                return new RecordFilterByTeam();
            case SELF:
                return new RecordFilterBySelf();
            case NOTSELF:
                return new RecordFilterExcludeSelf();
            case MECHVARIANT:
                return new RecordFilterByStat(StatType.MECH_VARIANT, parameters);
            case MECHCHASSIS:
                return new RecordFilterByStat(StatType.MECH_CHASSIS, parameters);
            case MECHTONS:
                return new RecordFilterByStat(StatType.MECH_TONS, parameters);
            case MECHCLASS:
                return new RecordFilterByStat(StatType.MECH_CLASS, parameters);
            case MECHFACTION:
                return new RecordFilterByStat(StatType.MECH_FACTION, parameters);
            case MATCHRECENT:
                return new RecordFilterNewerThan(parameters);
            case MATCHOLD:
                return new RecordFilterOlderThan(parameters);
            case GAMEMODE:
                return new RecordFilterByStat(StatType.GAMEMODE, parameters);
            case MAP:
                return new RecordFilterByStat(StatType.MAP, parameters);
            case RESET:
                return new RecordFilterReset();
            default:
                throw new UnsupportedOperationException(this.toString() + " not implemented");
        }
    }
}
