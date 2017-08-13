package at.happynev.mwoscoreboardhelper.stat.calculator;

import at.happynev.mwoscoreboardhelper.stat.StatType;

/**
 * Created by Nev on 06.08.2017.
 */
public enum AggregatedStatCalculatorType {
    RELATIVE,
    RATIO;

    public StatCalculator getInstance() {
        switch (this) {
            case RELATIVE:
                return new StatCalculatorRelativePercent();
            case RATIO:
                return new StatCalculatorRatio();
            default:
                throw new UnsupportedOperationException(this.toString() + " not implemented");
        }
    }
}
