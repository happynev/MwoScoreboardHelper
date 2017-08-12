package at.happynev.mwoscoreboardhelper.stat.calculator;

import at.happynev.mwoscoreboardhelper.PlayerMatchRecord;
import at.happynev.mwoscoreboardhelper.Utils;
import at.happynev.mwoscoreboardhelper.stat.StatType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Nev on 29.07.2017.
 */
public class StatCalculatorHelpers {

    public static void sortByStat(List<PlayerMatchRecord> records, StatType statType) {
        Collections.sort(records, (o1, o2) -> {
            Comparator comp = Utils.getNumberComparator();
            String stat1 = o1.getMatchValues().get(statType);
            String stat2 = o2.getMatchValues().get(statType);
            int ret = comp.compare(stat1, stat2);
            if (ret == 0) {
                StatType tieBreaker = StatType.SCORE;
                if (statType == StatType.SCORE) {
                    tieBreaker = StatType.DAMAGE;
                }
                String altstat1 = o1.getMatchValues().get(tieBreaker);
                String altstat2 = o2.getMatchValues().get(tieBreaker);
                ret = comp.compare(altstat1, altstat2);
            }
            return ret;
        });
    }

    public static Map<String, Collection<PlayerMatchRecord>> splitByValue(Collection<PlayerMatchRecord> records, StatType stat) {
        Map<String, Collection<PlayerMatchRecord>> recordsPerKey = new HashMap<>();
        for (PlayerMatchRecord pmr : records) {
            String key = pmr.getMatchValues().get(stat);
            Collection<PlayerMatchRecord> data = recordsPerKey.get(key);
            if (data == null) {
                data = new HashSet<>();
                recordsPerKey.put(key, data);
            }
            data.add(pmr);
        }
        return recordsPerKey;
    }

    public static boolean isValidRecord(PlayerMatchRecord pmr, StatType statType) {
        boolean validRecord = false;
        if (pmr.getMatchValues().containsKey(StatType.PING) && pmr.getMatchValues().containsKey(statType)) {
            validRecord = 0 < Integer.parseInt(pmr.getMatchValues().get(StatType.PING));
        }
        return validRecord;
    }

    public static Collection<PlayerMatchRecord> filterValidRecords(Collection<PlayerMatchRecord> records, StatType statType) {
        return records.stream().filter(r -> isValidRecord(r, statType)).collect(Collectors.toSet());
    }
}
