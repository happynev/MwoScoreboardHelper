package at.happynev.mwoscoreboardhelper.stat.calculator;

import at.happynev.mwoscoreboardhelper.stat.StatType;

/**
 * Created by Nev on 06.08.2017.
 */
public enum StatCalculatorType {
    RANKING,
    RAWVALUE,
    RELATIVE,
    RATIO;

    public StatCalculator getInstance(StatType statType) {
        switch (this) {
            case RANKING:
                return new StatCalculatorRanking(statType);
            case RAWVALUE:
                return new StatCalculatorRawValue(statType);
            case RELATIVE:
                return new StatCalculatorRelativePercent(statType);
            case RATIO:
                return new StatCalculatorRatio(statType);
            default:
                throw new UnsupportedOperationException(this.toString() + " not implemented");
        }
    }
}
