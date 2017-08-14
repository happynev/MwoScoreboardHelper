package at.happynev.mwoscoreboardhelper.stat.formatter;

/**
 * Created by Nev on 06.08.2017.
 */
public enum StatResultFormatterType {
    RANKING,
    STATUS,
    PERCENT;

    public StatResultFormatter getInstance(String... parameters) {
        switch (this) {
            case RANKING:
                return new StatResultRankingFormatter(parameters);
            case STATUS:
                return new StatResultStatusFormatter(parameters);
            case PERCENT:
                return new StatResultPercentageFormatter(parameters);
            default:
                throw new UnsupportedOperationException(this.toString() + " not implemented");
        }
    }
}
