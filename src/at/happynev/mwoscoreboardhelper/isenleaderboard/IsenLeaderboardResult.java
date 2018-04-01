package at.happynev.mwoscoreboardhelper.isenleaderboard;

import at.happynev.mwoscoreboardhelper.Logger;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class IsenLeaderboardResult {
    private Date lastEditDate;
    private IsenSeasonData overallData;
    private Map<String, IsenSeasonData> seasonData = new HashMap<>();

    public Date getLastEditDate() {
        return lastEditDate;
    }

    public void setLastEditDate(Date lastEditDate) {
        this.lastEditDate = lastEditDate;
    }

    public IsenSeasonData getOverallData() {
        return overallData;
    }
    public Map<String, IsenSeasonData> getSeasonData() {
        return seasonData;
    }

    public void addSeason(String season, IsenSeasonData data) {
        season = season.replaceAll("\\s*:.*", "");
        //Logger.log("add season " + season);
        if (season.equalsIgnoreCase("Overall")) {
            overallData = data;
        } else {
            seasonData.put(season, data);
        }
    }
}
