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
    MECHVARIANT,
    MECHCHASSIS,
    MECHTONS,
    MECHCLASS,
    MECHFACTION,
    MATCHRECENT,
    MATCHOLD,
    GAMEMODE,
    MAP;

    public RecordFilter getInstance(String... parameters) {
        switch (this) {
            case MATCH:
                return new RecordFilterByMatch();
            case PLAYER:
                return new RecordFilterByPlayer();
            case TEAM:
                return new RecordFilterByTeam();
            case SELF:
                return new RecordFilterBySelf();
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
            default:
                throw new UnsupportedOperationException(this.toString() + " not implemented");
        }
    }
}
