package at.happynev.mwoscoreboardhelper.stat.calculator;

import at.happynev.mwoscoreboardhelper.stat.StatType;

/**
 * Created by Nev on 06.08.2017.
 */
public enum MatchStatCalculatorType {
    RANKING_DESC,
    RANKING_ASC,
    RAWVALUE;

    public StatCalculator getInstance(StatType statType) {
        switch (this) {
            case RANKING_DESC:
                return new StatCalculatorRanking(statType, false);
            case RANKING_ASC:
                return new StatCalculatorRanking(statType, true);
            case RAWVALUE:
                return new StatCalculatorRawValue(statType);
            default:
                throw new UnsupportedOperationException(this.toString() + " not implemented");
        }
    }
}
