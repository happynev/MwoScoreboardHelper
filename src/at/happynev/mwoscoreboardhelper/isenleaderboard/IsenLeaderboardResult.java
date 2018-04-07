package at.happynev.mwoscoreboardhelper.isenleaderboard;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class IsenLeaderboardResult {
    private final String playerName;
    private Date lastEditDate;
    private IsenSeasonData overallData;
    private Map<String, IsenSeasonData> seasonData = new HashMap<>();

    public IsenLeaderboardResult(String playerName) {
        this.playerName = playerName;
    }

    public Date getLastEditDate() {
        return lastEditDate;
    }

    public void setLastEditDate(Date lastEditDate) {
        this.lastEditDate = lastEditDate;
    }

    public String getPlayerName() {
        return playerName;
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

    public IsenSeasonData getConfiguredSeasonData() {
        //TODO: check overall/last/last x
        return overallData;
    }
}
