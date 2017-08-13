package at.happynev.mwoscoreboardhelper.stat.aggregator;

import at.happynev.mwoscoreboardhelper.PlayerMatchRecord;
import at.happynev.mwoscoreboardhelper.stat.StatTable;
import at.happynev.mwoscoreboardhelper.stat.StatType;
import at.happynev.mwoscoreboardhelper.stat.calculator.StatCalculatorHelpers;
import at.happynev.mwoscoreboardhelper.tracer.ScreenshotType;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Nev on 29.07.2017.
 */
public class StatAggregatorToplistMechClass extends StatAggregator {
    private final StatType statType;

    public StatAggregatorToplistMechClass(StatType statType) {
        this.statType = statType;
    }

    @Override
    public String aggregateValue(Collection<PlayerMatchRecord> records) {
        Collection<PlayerMatchRecord> validRecords = StatCalculatorHelpers.filterValidRecords(records, StatType.MECH_CLASS);
        Map<String, Collection<PlayerMatchRecord>> recordsPerKey = StatCalculatorHelpers.splitByValue(records, StatType.MECH_CLASS);
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
        return true; //historic values only
    }
}
