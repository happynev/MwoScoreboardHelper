package at.happynev.mwoscoreboardhelper.isenleaderboard;

import at.happynev.mwoscoreboardhelper.Logger;

import java.util.HashMap;
import java.util.Map;

public class IsenLeaderboardResult {
    private IsenSeasonData overallData;
    private Map<String, IsenSeasonData> seasonData = new HashMap<>();

    public IsenSeasonData getOverallData() {
        return overallData;
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
