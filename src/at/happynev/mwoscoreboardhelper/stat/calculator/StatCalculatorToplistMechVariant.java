package at.happynev.mwoscoreboardhelper.stat.calculator;

import at.happynev.mwoscoreboardhelper.PlayerMatchRecord;
import at.happynev.mwoscoreboardhelper.stat.StatTable;
import at.happynev.mwoscoreboardhelper.stat.StatType;
import at.happynev.mwoscoreboardhelper.stat.aggregator.StatAggregatorType;
import at.happynev.mwoscoreboardhelper.tracer.ScreenshotType;

import java.util.*;

/**
 * Created by Nev on 29.07.2017.
 */
public class StatCalculatorToplistMechVariant extends StatCalculator {
    private final StatType statType;

    public StatCalculatorToplistMechVariant(StatType statType) {
        this.statType = statType;
    }

    @Override
    public String calculateCurrentValue(Collection<PlayerMatchRecord> records, PlayerMatchRecord currentRecord, String previousValue) {
        Collection<PlayerMatchRecord> validRecords = StatCalculatorHelpers.filterValidRecords(records, StatType.MECH_VARIANT);
        Map<String, Collection<PlayerMatchRecord>> recordsPerKey = StatCalculatorHelpers.splitByValue(records, StatType.MECH_VARIANT);
        Map<String, Integer> scorePerKey = new HashMap<>();
        Map<String, Integer> sorted = new TreeMap<>((o1, o2) -> -scorePerKey.get(o1).compareTo(scorePerKey.get(o2)));
        for (String key : recordsPerKey.keySet()) {
            int value = Integer.parseInt(StatAggregatorType.AVERAGE.getInstance(statType).aggregateValue(recordsPerKey.get(key)).replaceAll("\\..*", ""));
            scorePerKey.put(key, value);
        }
        sorted.putAll(scorePerKey);
        StringBuilder sb = new StringBuilder();
        int c = 0;
        for (String key : sorted.keySet()) {
            if (c == 4) {
                break;
            }
            int value = sorted.get(key);
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(key).append("[").append(value).append("]");
            c++;
        }
        return sb.toString();
    }

    @Override
    public String getStepDescription() {
        return "best mech variants by " + statType.getDescription();
    }

    @Override
    public boolean canDisplay(ScreenshotType type, StatTable table) {
        return true;//historic values only
    }
}
