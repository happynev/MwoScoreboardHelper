package at.happynev.mwoscoreboardhelper.isenleaderboard;

import at.happynev.mwoscoreboardhelper.Logger;
import org.apache.http.Header;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IsenLeaderboard {
    private static IsenLeaderboard instance;
    private final CloseableHttpClient httpClient;
    private final int COL_SEASON = 0;
    private final int COL_RANK = 1;
    private final int COL_PERCENTILE = 2;
    private final int COL_GAMESPLAYED = 3;
    private final int COL_WLRATIO = 4;
    private final int COL_SURVIVERATE = 5;
    private final int COL_KDRATIO = 6;
    private final int COL_AVGSCORE = 7;
    private final int COL_ADJSCORE = 8;
    private final int COL_PROGRESS = 9;
    private final int COL_LIGHT = 10;
    private final int COL_MEDIUM = 11;
    private final int COL_HEAVY = 12;
    private final int COL_ASSAULT = 13;

    private IsenLeaderboard() {
        RequestConfig cfg = RequestConfig.custom().setConnectTimeout(10000).build();
        //TODO: proxy settings
        httpClient = HttpClients.custom()
                .disableCookieManagement()
                .setDefaultRequestConfig(cfg)
                .build();
    }

    public static IsenLeaderboard getInstance() {
        if (instance == null) {
            instance = new IsenLeaderboard();
        }
        return instance;
    }

    private String getWebsiteData(String playerName) {
        String url = null;
        try {
            url = "https://leaderboard.isengrim.org/search.php?u=" + URLEncoder.encode(playerName, "UTF-8");
            long start = System.currentTimeMillis();
            HttpGet request = new HttpGet(url);
            CloseableHttpResponse response = httpClient.execute(request);
            int status = response.getStatusLine().getStatusCode();
            if (status == 200) {
                for (Header h : response.getAllHeaders()) {
                    //Logger.log("header= " + h.getName() + ": " + h.getValue());
                }
                long strstart = System.currentTimeMillis();
                BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                String line = null;
                StringBuilder sb = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                br.close();
                String data = sb.toString();
                long end = System.currentTimeMillis();
                Logger.log("requested ISEN Leaderboard data for " + playerName + ", took:" + (end - start) + "ms");
                return sb.toString();
            } else {
                Logger.warning("ISEN Leaderboard request NOK data for " + playerName + ": " + response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
            }
            response.close();
        } catch (Exception e) {
            Logger.error(e);
        }
        return null;
    }

    public IsenLeaderboardResult getLeaderboardData(String playerName) {
        IsenLeaderboardResult ret = new IsenLeaderboardResult();
        String html = getWebsiteData(playerName);
        long start = System.currentTimeMillis();
        Pattern row = Pattern.compile("(?:<tr>)?(?:\\s*<td[^>]+>([^<]*)<[^>]+>)(?:\\s*<td[^>]+>[^<]*<[^>]+>){13}\\s*</tr>", Pattern.DOTALL);
        Pattern field = Pattern.compile("<td[^>]+>([^<]*)<[^>]+>", Pattern.DOTALL);
        //int startOffset = html.indexOf("<tr class=\"overallrow\">");
        Matcher rowMatcher = row.matcher(html);
        while (rowMatcher.find()) {
            String tableRow = rowMatcher.group();
            String season = rowMatcher.group(1);
            Matcher fieldMatcher = field.matcher(tableRow);
            int column = 0;
            IsenSeasonData seasonData = new IsenSeasonData();
            while (fieldMatcher.find()) {
                String value = fieldMatcher.group(1);
                switch (column) {
                    case COL_SEASON:
                        seasonData.setSeason(value);
                        break;
                    case COL_RANK:
                        if (value.equalsIgnoreCase("not found")) {
                            return null;
                        } else if (value.equalsIgnoreCase("retired")) {
                            value = "999999";
                        }
                        seasonData.setRank(value);
                        break;
                    case COL_PERCENTILE:
                        seasonData.setPercentile(value);
                        break;
                    case COL_GAMESPLAYED:
                        seasonData.setGamesPlayed(value);
                        break;
                    case COL_WLRATIO:
                        seasonData.setWinloss(value);
                        break;
                    case COL_SURVIVERATE:
                        seasonData.setSurviveRate(value);
                        break;
                    case COL_KDRATIO:
                        seasonData.setKdratio(value);
                        break;
                    case COL_AVGSCORE:
                        seasonData.setAvgScore(value);
                        break;
                    case COL_ADJSCORE:
                        seasonData.setAdjScore(value);
                        break;
                    case COL_PROGRESS:
                        seasonData.setProgress(value);
                        break;
                    case COL_LIGHT:
                        seasonData.setLight(value);
                        break;
                    case COL_MEDIUM:
                        seasonData.setMedium(value);
                        break;
                    case COL_HEAVY:
                        seasonData.setHeavy(value);
                        break;
                    case COL_ASSAULT:
                        seasonData.setAssault(value);
                        break;
                }
                column++;
            }
            ret.addSeason(season, seasonData);
        }
        long end = System.currentTimeMillis();
        //Logger.log("parsing took:" + (end - start) + "ms");
        return ret;
    }
}
