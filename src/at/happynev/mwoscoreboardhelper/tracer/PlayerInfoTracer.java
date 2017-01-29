package at.happynev.mwoscoreboardhelper.tracer;

import net.sourceforge.tess4j.util.ImageHelper;

import java.awt.image.BufferedImage;
import java.util.Arrays;

/**
 * Created by Nev on 15.01.2017.
 */
public class PlayerInfoTracer extends AsyncTracer {
    private final TraceableImage unitTag;
    private final TraceableImage pilotName;
    private final TraceableImage mech;
    private final TraceableImage matchScore;
    private final TraceableImage kills;
    private final TraceableImage assists;
    private final TraceableImage damage;
    private final TraceableImage status;
    private final TraceableImage ping;

    public PlayerInfoTracer(BufferedImage screenshot, int player, Offsets off) {
        pilotName = new TraceableImage(Offsets.getSubImage(screenshot, off.playerPilotName(player)), OcrConfig.DEFAULT);
        unitTag = new TraceableImage(Offsets.getSubImage(screenshot, off.playerUnit(player)), OcrConfig.UNIT);
        BufferedImage mechImg = Offsets.getSubImage(screenshot, off.playerMech(player));
        if (mechImg != null) {
            //enemy team in preparation screen
            mech = new TraceableImage(mechImg, OcrConfig.MECHS);//s -> s.replaceAll(".*?(READY|NOT READY|Disconnected|CONNECTING)?.*", "$1").replaceAll("^\\s*$", "DEAD")
        } else {
            mech = null;
        }
        if (off.getType() == ScreenshotType.QP_3SUMMARY) {
            matchScore = new TraceableImage(Offsets.getSubImage(screenshot, off.playerMatchScore(player)), OcrConfig.NUMERIC);
            kills = new TraceableImage(Offsets.getSubImage(screenshot, off.playerKills(player)), OcrConfig.NUMERIC);
            assists = new TraceableImage(Offsets.getSubImage(screenshot, off.playerAssists(player)), OcrConfig.NUMERIC);
            damage = new TraceableImage(Offsets.getSubImage(screenshot, off.playerDamage(player)), OcrConfig.NUMERIC);
            status = new TraceableImage(ImageHelper.convertImageToGrayscale(Offsets.getSubImage(screenshot, off.playerStatus(player))), OcrConfig.STATUS);
            ping = new TraceableImage(Offsets.getSubImage(screenshot, off.playerPing(player)), OcrConfig.NUMERIC);
        } else {
            matchScore = null;
            kills = null;
            assists = null;
            damage = null;
            status = null;
            ping = null;
        }
        traceAllAsync(Arrays.asList(pilotName, unitTag, mech, matchScore, kills, assists, damage, status, ping));
    }

    private String fixUnitTag(String s) {
        return s.replaceAll("^[L1I\\|]", "[").replaceAll("[1IJ\\|]$", "]").replaceAll(" ", "").replaceAll(".*?(\\[[()\\w+-]{1,4}\\])?.*", "$1");
    }

    private String findMostLikelyStatus(String s) {
        return s;
    }

    private String findMostLikelyMech(String s) {
        return s;
    }

    public String getUnitTag() {
        if (unitTag == null) return "";
        return fixUnitTag(unitTag.getValue());
    }

    public TraceableImage getUnitTagImage() {
        return unitTag;
    }

    public String getPilotName() {
        if (pilotName == null) return "";
        return pilotName.getValue();
    }

    public TraceableImage getPilotNameImage() {
        return pilotName;
    }

    public String getMech() {
        if (mech == null) return "";
        return findMostLikelyMech(mech.getValue());
    }

    public TraceableImage getMechImage() {
        return mech;
    }

    public int getMatchScore() {
        if (matchScore == null || matchScore.getValue().isEmpty()) return 0;
        return Integer.parseInt(matchScore.getValue());
    }

    public TraceableImage getMatchScoreImage() {
        return matchScore;
    }

    public int getKills() {
        if (kills == null || kills.getValue().isEmpty()) return 0;
        return Integer.parseInt(kills.getValue());
    }

    public TraceableImage getKillsImage() {
        return kills;
    }

    public int getAssists() {
        if (assists == null || assists.getValue().isEmpty()) return 0;
        return Integer.parseInt(assists.getValue());
    }

    public TraceableImage getAssistsImage() {
        return assists;
    }

    public int getDamage() {
        if (damage == null || damage.getValue().isEmpty()) return 0;
        return Integer.parseInt(damage.getValue());
    }

    public TraceableImage getDamageImage() {
        return damage;
    }

    public String getStatus() {
        if (status == null) return "";
        return findMostLikelyStatus(status.getValue());
    }

    public TraceableImage getStatusImage() {
        return status;
    }

    public int getPing() {
        if (ping == null || ping.getValue().isEmpty()) return 0;
        return Integer.parseInt(ping.getValue());
    }

    public TraceableImage getPingImage() {
        return ping;
    }
}
