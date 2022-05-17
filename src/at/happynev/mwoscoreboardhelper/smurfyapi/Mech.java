package at.happynev.mwoscoreboardhelper.smurfyapi;

import java.math.BigDecimal;

/**
 * Created by Nev on 22.01.2017.
 */
public class Mech {
    String id;
    String name;
    String faction;
    String mech_type;
    String family;
    String chassis_translated;
    String translated_name;
    String translated_short_name;
    Details details = new Details();
    String type;
    int tons;
    double top_speed;
    int max_armor;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFaction() {
        return faction;
    }

    public void setFaction(String faction) {
        this.faction = faction;
    }

    public String getMech_type() {
        return mech_type;
    }

    public void setMech_type(String mech_type) {
        this.mech_type = mech_type;
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public String getChassis_translated() {
        return chassis_translated;
    }

    public void setChassis_translated(String chassis_translated) {
        this.chassis_translated = chassis_translated;
    }

    public String getTranslated_name() {
        return translated_name;
    }

    public void setTranslated_name(String translated_name) {
        this.translated_name = translated_name;
    }

    public String getTranslated_short_name() {
        return translated_short_name;
    }

    public void setTranslated_short_name(String translated_short_name) {
        this.translated_short_name = translated_short_name;
    }

    public Details getDetails() {
        return details;
    }

    public void setDetails(Details details) {
        this.details = details;
    }

    public int getMax_armor() {
        if (max_armor == 0) {
            max_armor = details.max_armor;
        }
        return max_armor;
    }

    public String getType() {
        if (type == null) {
            type = details.type;
        }
        return type;
    }

    public int getTons() {
        if (tons == 0) {
            tons = details.tons;
        }
        return tons;
    }

    public String getTop_speed() {
        if (top_speed == 0d) {
            top_speed = details.top_speed;
        }
        BigDecimal bd = new BigDecimal(top_speed);
        return bd.setScale(1, BigDecimal.ROUND_HALF_UP).toPlainString();
    }

    public static class Details {
        String type;
        int tons;
        double top_speed;
        int max_armor;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public int getTons() {
            return tons;
        }

        public void setTons(int tons) {
            this.tons = tons;
        }

        public double getTop_speed() {
            return top_speed;
        }

        public void setTop_speed(double top_speed) {
            this.top_speed = top_speed;
        }

        public void setMax_armor(int max_armor) {
            this.max_armor = max_armor;
        }
    }
}
