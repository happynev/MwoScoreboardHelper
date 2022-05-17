package at.happynev.mwoscoreboardhelper;

import at.happynev.mwoscoreboardhelper.smurfyapi.Mech;
import at.happynev.mwoscoreboardhelper.stat.StatType;
import at.happynev.mwoscoreboardhelper.tracer.ValueHelpers;
import org.apache.commons.lang3.StringUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Nev on 22.01.2017.
 */
public class MechRuntime {
    private static final Set<String> knownShortNames = new HashSet<>();
    private static final Map<String, MechRuntime> knownMechs = new HashMap<>();
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

    private MechRuntime(String id, String internalName, String name, String shortName, String chassis, int tons, double speed, int armor, String faction, String specialtype) {
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
    }

    public static Set<String> getKnownShortNames() {
        if (knownShortNames.isEmpty()) {
            getKnownMechs();
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
            getKnownMechs();
        }
        for (MechRuntime mr : knownMechs.values()) {
            if (_short.equals(mr.getShortName())) {
                return mr;
            }
        }
        if (!"XXX-1X".equals(_short) && !_short.isEmpty()) {
            Logger.warning("No Mech instance for '" + _short + "'");
        }
        return getReferenceMech();
    }

    public static MechRuntime getReferenceMech() {
        return new MechRuntime("-1", "Dummy mech", "Dummy mech", "XXX-1X", "Dummy", 0, 50.0, 0, "None", "");
    }

    private static void bulkLoadFromDb() {
        int i = 0;
        try {
            PreparedStatement prep = DbHandler.getInstance().prepareStatement("select api_id,internal_name,name,short_name,chassis,tons,max_speed,max_armor,faction,specialtype from mech_data");
            ResultSet rs = prep.executeQuery();
            knownMechs.clear();
            knownShortNames.clear();

            while (rs.next()) {
                i++;
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
                MechRuntime mr = new MechRuntime(_id, _internal, _name, _short, _chassis, _tons, _speed, _armor, _faction, _special);
                knownMechs.put(mr.getId(), mr);
                knownShortNames.add(mr.getShortName());
            }
            rs.close();
            prep.close();
        } catch (Exception e) {
            Logger.log("mech:" + i);
            Logger.error(e);
        }
    }

    private static void calculateStats() {

    }

    public static String findMatchingMech(String mech) {
        if (mech.isEmpty()) return "";
        Set<String> specialvariants = new HashSet<>(getKnownShortNames());
        specialvariants.removeIf(s -> !s.matches(".*\\([^)]+\\)$"));
        String specialguess = ValueHelpers.guessValue(mech, specialvariants);
        String noPostfixMech = mech.replaceAll("\\(?[SIRPLG]\\)", "").replaceAll("\\s*", ""); //(S)pecial, (S)team, (I)nvasion, (R)esistance, (P)hoenix mechs not in smurfy data, some (L)oyalty. and stupid (G)old mechs
        String normalguess = ValueHelpers.guessValue(noPostfixMech, getKnownShortNames());
        String guess = "";
        int distSpecial = StringUtils.getLevenshteinDistance(specialguess, mech);
        int distNormal = StringUtils.getLevenshteinDistance(normalguess, noPostfixMech);
        if (distSpecial <= distNormal) {
            guess = specialguess;
        } else {
            guess = normalguess;
        }
        if (!mech.equals(guess)) {
            Logger.log("changed mech: " + mech + "-->" + guess);
        }
        return guess;
    }

    public Mech getDataObject() {
        Mech mech = new Mech();
        mech.setId(id);
        mech.setMech_type(getWeightClass());
        mech.setFaction(faction);
        mech.setChassis_translated(chassis);
        mech.setName(internalName);
        mech.setTranslated_name(name);
        mech.setTranslated_short_name(shortName);

        Mech.Details details = new Mech.Details();
        details.setTons(tons);
        details.setMax_armor(armor);
        details.setTop_speed(speed);
        details.setType(specialtype);
        mech.setDetails(details);
        mech.getMax_armor();
        mech.getTons();
        mech.getTop_speed();
        mech.getType();
        mech.setDetails(null);
        return mech;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MechRuntime that = (MechRuntime) o;

        return shortName.equals(that.shortName);
    }

    @Override
    public int hashCode() {
        return shortName.hashCode();
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

    public Map<StatType, String> getDerivedValues() {
        Map<StatType, String> mechStats = new HashMap<>(5);
        mechStats.put(StatType.MECH_CHASSIS, getChassis());
        mechStats.put(StatType.MECH_CLASS, getWeightClass());
        mechStats.put(StatType.MECH_FACTION, getFaction());
        mechStats.put(StatType.MECH_TONS, "" + getTons());
        mechStats.put(StatType.MECH_VARIANT, getShortName());
        return mechStats;
    }
}
