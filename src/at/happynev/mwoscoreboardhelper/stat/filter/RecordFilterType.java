package at.happynev.mwoscoreboardhelper.stat.filter;

import at.happynev.mwoscoreboardhelper.stat.StatType;

/**
 * Created by Nev on 06.08.2017.
 */
public enum RecordFilterType {
    MATCH,
    PLAYER,
    TEAM,
    MECHVARIANT,
    MECHCHASSIS,
    MECHTONS,
    MECHCLASS,
    MECHFACTION,
    MATCHRECENT;

    public RecordFilter getInstance() {
        switch (this) {
            case MATCH:
                return new RecordFilterByMatch();
            case PLAYER:
                return new RecordFilterByPlayer();
            case TEAM:
                return new RecordFilterByTeam();
            case MECHVARIANT:
                return new RecordFilterByStat(StatType.MECH_VARIANT);
            case MECHCHASSIS:
                return new RecordFilterByStat(StatType.MECH_CHASSIS);
            case MECHTONS:
                return new RecordFilterByStat(StatType.MECH_TONS);
            case MECHCLASS:
                return new RecordFilterByStat(StatType.MECH_CLASS);
            case MECHFACTION:
                return new RecordFilterByStat(StatType.MECH_FACTION);
            case MATCHRECENT:
                return new RecordFilterNewerThan(30);
            default:
                throw new UnsupportedOperationException(this.toString() + " not implemented");
        }
    }
}
