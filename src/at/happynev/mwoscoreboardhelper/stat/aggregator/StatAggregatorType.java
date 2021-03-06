package at.happynev.mwoscoreboardhelper.stat.aggregator;

import at.happynev.mwoscoreboardhelper.stat.StatType;

/**
 * Created by Nev on 06.08.2017.
 */
public enum StatAggregatorType {
    AVERAGE,
    MEDIAN,
    COUNT,
    SUM,
    TOPLISTMECH,
    TOPLISTCLASS;

    public StatAggregator getInstance(StatType statType) {
        switch (this) {
            case AVERAGE:
                return new StatAggregatorAverage(statType);
            case MEDIAN:
                return new StatAggregatorMedian(statType);
            case COUNT:
                return new StatAggregatorCountValid(statType);
            case SUM:
                return new StatAggregatorSum(statType);
            case TOPLISTMECH:
                return new StatAggregatorToplistMechVariant(statType);
            case TOPLISTCLASS:
                return new StatAggregatorToplistMechClass(statType);
            default:
                throw new UnsupportedOperationException(this.toString() + " not implemented");
        }
    }
}