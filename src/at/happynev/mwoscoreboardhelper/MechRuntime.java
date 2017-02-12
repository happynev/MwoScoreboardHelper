package at.happynev.mwoscoreboardhelper;

import at.happynev.mwoscoreboardhelper.tracer.TraceHelpers;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

/**
 * Created by Nev on 22.01.2017.
 */
public class MechRuntime {
    private static Set<String> knownShortNames = new HashSet<>();
    private static Map<String, MechRuntime> knownMechs = new HashMap<>();
    private static Map<String, Set<MechRuntime>> mechsPerChassis = new HashMap<>();
    private static Map<String, Set<MechRuntime>> mechsPerWeightClass = new HashMap<>();
    private static Map<String, Set<MechRuntime>> mechsPerFaction = new HashMap<>();
    private final String id;
    private final String internalName;
    private final String name;
    private final String shortName;
    private final String chassis;
    private final int tons;
    private final double speed;
    private final int armor;
    private final String faction;
    private final String specialtype;
    private final List<PlayerMatchRecord> matchRecords;
    private int avgDamage = 0;
    private int avgScore = 0;
    private BigDecimal popularityTotal = BigDecimal.ZERO;
    private BigDecimal popularityChassis = BigDecimal.ZERO;
    private BigDecimal popularityClass = BigDecimal.ZERO;
    private BigDecimal popularityFaction = BigDecimal.ZERO;

    private MechRuntime(String id, String internalName, String name, String shortName, String chassis, int tons, double speed, int armor, String faction, String specialtype, List<PlayerMatchRecord> records) {
        this.id = id;
        this.internalName = internalName;
        this.name = name;
        this.shortName = shortName;
        this.chassis = chassis;
        this.tons = tons;
        this.speed = speed;
        this.armor = armor;
        this.faction = faction;
        this.specialtype = specialtype;
        matchRecords = records;
    }

    public synchronized static Set<String> getKnownShortNames() {
        if (knownShortNames.isEmpty()) {
            bulkLoadFromDb();
        }
        return knownShortNames;
    }

    public synchronized static Map<String, MechRuntime> getKnownMechs() {
        if (knownMechs.isEmpty()) {
            bulkLoadFromDb();
        }
        return knownMechs;
    }

    public static MechRuntime getMechByShortName(String _short) { //short name might not be unique
        if (knownMechs.isEmpty()) {
            bulkLoadFromDb();
        }
        for (MechRuntime mr : knownMechs.values()) {
            if (_short.equals(mr.getShortName())) {
                return mr;
            }
        }
        return getReferenceMech();
    }

    private static MechRuntime getReferenceMech() {
        return new MechRuntime("-1", "Dummy mech", "Dummy mech", "XXX-1X", "Dummy", 0, 50.0, 0, "None", "", new ArrayList<>());
    }

    private static void bulkLoadFromDb() {
        try {
            PreparedStatement prep = DbHandler.getInstance().prepareStatement("select api_id,internal_name,name,short_name,chassis,tons,max_speed,max_armor,faction,specialtype from mech_data");
            PreparedStatement prepPmr = DbHandler.getInstance().prepareStatement("select player_data_id,match_data_id from player_matchdata where mech=?");
            ResultSet rs = prep.executeQuery();
            mechsPerFaction.clear();
            mechsPerChassis.clear();
            mechsPerWeightClass.clear();
            knownMechs.clear();
            knownShortNames.clear();
            while (rs.next()) {
                String _id = rs.getString("api_id");
                String _internal = rs.getString("internal_name");
                String _name = rs.getString("name");
                String _short = rs.getString("short_name");
                String _chassis = rs.getString("chassis");
                int _tons = rs.getInt("tons");
                double _speed = rs.getDouble("max_speed");
                int _armor = rs.getInt("max_armor");
                String _faction = rs.getString("faction");
                String _special = rs.getString("specialtype");
                List<PlayerMatchRecord> tmpPmr = new ArrayList<>();
                prepPmr.clearParameters();
                prepPmr.setString(1, _short);
                ResultSet rsPmr = prepPmr.executeQuery();
                while (rsPmr.next()) {
                    int pid = rsPmr.getInt(1);
                    int mid = rsPmr.getInt(2);
                    tmpPmr.add(new PlayerMatchRecord(pid, mid));
                }
                rsPmr.close();
                MechRuntime mr = new MechRuntime(_id, _internal, _name, _short, _chassis, _tons, _speed, _armor, _faction, _special, tmpPmr);
                knownMechs.put(mr.getId(), mr);
                knownShortNames.add(mr.getShortName());
                Set<MechRuntime> mechListFaction = mechsPerFaction.get(mr.getFaction());
                if (mechListFaction == null) {
                    mechListFaction = new HashSet<>();
                    mechsPerFaction.put(mr.getFaction(), mechListFaction);
                }
                mechListFaction.add(mr);
                Set<MechRuntime> mechListClass = mechsPerWeightClass.get(mr.getWeightClass());
                if (mechListClass == null) {
                    mechListClass = new HashSet<>();
                    mechsPerWeightClass.put(mr.getFaction(), mechListClass);
                }
                mechListClass.add(mr);
                Set<MechRuntime> mechListChassis = mechsPerChassis.get(mr.getChassis());
                if (mechListChassis == null) {
                    mechListChassis = new HashSet<>();
                    mechsPerChassis.put(mr.getFaction(), mechListChassis);
                }
                mechListChassis.add(mr);
            }
            rs.close();
            calculateStats();
        } catch (Exception e) {
            Logger.error(e);
        }
    }

    private static void calculateStats() {
        int totalSeenMechs = 0;
        Map<String, Integer> totalSeenPerChassis = new HashMap<>(mechsPerChassis.size());
        Map<String, Integer> totalSeenPerClass = new HashMap<>(mechsPerWeightClass.size());
        Map<String, Integer> totalSeenPerFaction = new HashMap<>(mechsPerFaction.size());
        for (MechRuntime variant : knownMechs.values()) {
            Integer seenChassis = totalSeenPerChassis.get(variant.getChassis());
            Integer seenClass = totalSeenPerClass.get(variant.getWeightClass());
            Integer seenFaction = totalSeenPerFaction.get(variant.getFaction());
            if (seenChassis == null) seenChassis = 0;
            if (seenClass == null) seenClass = 0;
            if (seenFaction == null) seenFaction = 0;
            seenChassis += variant.getMatchRecords().size();
            seenClass += variant.getMatchRecords().size();
            seenFaction += variant.getMatchRecords().size();
            totalSeenMechs += variant.getMatchRecords().size();
            totalSeenPerChassis.put(variant.getChassis(), seenChassis);
            totalSeenPerClass.put(variant.getWeightClass(), seenClass);
            totalSeenPerFaction.put(variant.getFaction(), seenFaction);
            int totalValid = 0;
            int totalDamage = 0;
            int totalScore = 0;
            for (PlayerMatchRecord pmr : variant.getMatchRecords()) {
                if (pmr.getPing() > 0) {
                    totalValid++;
                    totalDamage += pmr.getDamage();
                    totalScore += pmr.getMatchScore();
                }
            }
            if (totalValid > 0) {
                variant.avgDamage = totalDamage / totalValid;
                variant.avgScore = totalScore / totalValid;
            }
        }
        //loop again for totals
        for (MechRuntime variant : knownMechs.values()) {
            variant.popularityTotal = calculateFraction(variant.getMatchRecords().size(), totalSeenMechs);
            variant.popularityChassis = calculateFraction(variant.getMatchRecords().size(), totalSeenPerChassis.get(variant.getChassis()));
            variant.popularityClass = calculateFraction(variant.getMatchRecords().size(), totalSeenPerClass.get(variant.getWeightClass()));
            variant.popularityFaction = calculateFraction(variant.getMatchRecords().size(), totalSeenPerFaction.get(variant.getFaction()));
        }
    }

    private static BigDecimal calculateFraction(int _value, int _total) {
        if (_total == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal value = new BigDecimal(_value);
        BigDecimal total = new BigDecimal(_total);
        BigDecimal fraction = value.divide(total, 3, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(1);
        return fraction;
    }

    public static Map<String, Set<MechRuntime>> getMechsPerChassis() {
        return mechsPerChassis;
    }

    public static Map<String, Set<MechRuntime>> getMechsPerWeightClass() {
        return mechsPerWeightClass;
    }

    public static Map<String, Set<MechRuntime>> getMechsPerFaction() {
        return mechsPerFaction;
    }

    public static String findMatchingMech(String mech) {
        if (mech.isEmpty()) return "";
        String preparedMech = mech.replaceAll("\\(?[S5I8RPL]\\)", "").replaceAll("\\s*", ""); //(S)pecial, (S)team, (I)nvasion, (R)esistance, (P)hoenix mechs not in smurfy data
        //specially check loyalty variants, some are in smurfy data, some not
        Set<String> loyaltyvariants = new HashSet<>(getKnownShortNames());
        loyaltyvariants.removeIf(s -> !s.endsWith("(L)"));
        String firstguess = TraceHelpers.guessValue(preparedMech, loyaltyvariants);
        String guess = TraceHelpers.guessValue(firstguess, getKnownShortNames());
        if (!mech.equals(guess)) {
            Logger.log("changed mech: " + mech + "-->" + guess);
        }
        return guess;
    }

    public BigDecimal getPopularityFaction() {
        return popularityFaction;
    }

    public int getAvgDamage() {
        return avgDamage;
    }

    public int getAvgScore() {
        return avgScore;
    }

    public BigDecimal getPopularityTotal() {
        return popularityTotal;
    }

    public BigDecimal getPopularityChassis() {
        return popularityChassis;
    }

    public BigDecimal getPopularityClass() {
        return popularityClass;
    }

    public String getWeightClass() {
        if (tons >= 80) {
            return "Assault";
        }
        if (tons >= 60) {
            return "Heavy";
        }
        if (tons >= 40) {
            return "Medium";
        }
        if (tons >= 20) {
            return "Light";
        }
        return "None";
    }

    public String getId() {
        return id;
    }

    public String getInternalName() {
        return internalName;
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

    public String getChassis() {
        return chassis;
    }

    public int getTons() {
        return tons;
    }

    public double getSpeed() {
        return speed;
    }

    public int getArmor() {
        return armor;
    }

    public String getFaction() {
        return faction;
    }

    public String getSpecialtype() {
        return specialtype;
    }

    public List<PlayerMatchRecord> getMatchRecords() {
        return matchRecords;
    }

    public int getSeen() {
        return matchRecords.size();
    }

    public BigDecimal getDamagePerTon() {
        double ret = (double) avgDamage / (double) tons;
        return new BigDecimal(ret).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    public BigDecimal getScorePerTon() {
        double ret = (double) avgScore / (double) tons;
        return new BigDecimal(ret).setScale(2, BigDecimal.ROUND_HALF_UP);
    }
}
