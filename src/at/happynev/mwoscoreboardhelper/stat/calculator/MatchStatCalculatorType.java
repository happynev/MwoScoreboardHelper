package at.happynev.mwoscoreboardhelper.stat.calculator;

import at.happynev.mwoscoreboardhelper.stat.StatType;

/**
 * Created by Nev on 06.08.2017.
 */
public enum MatchStatCalculatorType {
    RANKING,
    RAWVALUE;

    public StatCalculator getInstance(StatType statType) {
        switch (this) {
            case RANKING:
                return new StatCalculatorRanking(statType);
            case RAWVALUE:
                return new StatCalculatorRawValue(statType);
            default:
                throw new UnsupportedOperationException(this.toString() + " not implemented");
        }
    }
}
