package at.happynev.mwoscoreboardhelper.isenleaderboard;

import java.math.BigDecimal;

public class IsenSeasonData {
    private String season;
    private int rank;
    private int percentile;
    private int gamesPlayed;
    private BigDecimal winloss;
    private int surviveRate;
    private BigDecimal kdratio;
    private BigDecimal avgScore;
    private BigDecimal adjScore;
    private int progress;
    private int light;
    private int medium;
    private int heavy;
    private int assault;

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = getSafeInt(rank);
    }

    @Override
    public String toString() {
        return "IsenSeasonData{" +
                "season=" + season +
                ", rank=" + rank +
                ", percentile=" + percentile +
                ", gamesPlayed=" + gamesPlayed +
                ", winloss=" + winloss +
                ", surviveRate=" + surviveRate +
                ", kdratio=" + kdratio +
                ", avgScore=" + avgScore +
                ", adjScore=" + adjScore +
                ", progress=" + progress +
                ", light=" + light +
                ", medium=" + medium +
                ", heavy=" + heavy +
                ", assault=" + assault +
                '}';
    }

    public int getPercentile() {
        return percentile;
    }

    public void setPercentile(String percentile) {
        this.percentile = getSafeInt(percentile);
    }

    private int getSafeInt(String input) {
        try {
            return Integer.parseInt(input.replaceAll("[^0-9-]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private BigDecimal getSafeBigDecimal(String input) {
        try {
            return new BigDecimal(input.replaceAll("[^0-9.-]", ""));
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(String gamesPlayed) {
        this.gamesPlayed = getSafeInt(gamesPlayed);
    }

    public BigDecimal getWinloss() {
        return winloss;
    }

    public void setWinloss(String winloss) {
        this.winloss = getSafeBigDecimal(winloss);
    }

    public int getSurviveRate() {
        return surviveRate;
    }

    public void setSurviveRate(String surviveRate) {
        this.surviveRate = getSafeInt(surviveRate);
    }

    public BigDecimal getKdratio() {
        return kdratio;
    }

    public void setKdratio(String kdratio) {
        this.kdratio = getSafeBigDecimal(kdratio);
    }

    public BigDecimal getAvgScore() {
        return avgScore;
    }

    public void setAvgScore(String avgScore) {
        this.avgScore = getSafeBigDecimal(avgScore);
    }

    public BigDecimal getAdjScore() {
        return adjScore;
    }

    public void setAdjScore(String adjScore) {
        this.adjScore = getSafeBigDecimal(adjScore);
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(String progress) {
        this.progress = getSafeInt(progress);
    }

    public int getLight() {
        return light;
    }

    public void setLight(String light) {
        this.light = getSafeInt(light);
    }

    public int getMedium() {
        return medium;
    }

    public void setMedium(String medium) {
        this.medium = getSafeInt(medium);
    }

    public int getHeavy() {
        return heavy;
    }

    public void setHeavy(String heavy) {
        this.heavy = getSafeInt(heavy);
    }

    public int getAssault() {
        return assault;
    }

    public void setAssault(String assault) {
        this.assault = getSafeInt(assault);
    }
}
