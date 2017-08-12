package at.happynev.mwoscoreboardhelper.stat.aggregator;

import at.happynev.mwoscoreboardhelper.stat.StatType;

/**
 * Created by Nev on 06.08.2017.
 */
public enum StatAggregatorType {
    AVERAGE,
    COUNT,
    SUM;

    public StatAggregator getInstance(StatType statType) {
        switch (this) {
            case AVERAGE:
                return new StatAggregatorAverage(statType);
            case COUNT:
                return new StatAggregatorCountValid(statType);
            case SUM:
                return new StatAggregatorSum(statType);
            default:
                throw new UnsupportedOperationException(this.toString() + " not implemented");
        }
    }
}