package at.happynev.mwoscoreboardhelper.tracer;

import at.happynev.mwoscoreboardhelper.Logger;

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
        pilotName = new TraceableImage(extractWhite(Offsets.getSubImage(screenshot, off.getPlayerElementLocation(ScreenPlayerElement.PILOTNAME, player))), OcrConfig.DEFAULT);
        unitTag = new TraceableImage(extractWhite(Offsets.getSubImage(screenshot, off.getPlayerElementLocation(ScreenPlayerElement.UNIT, player))), OcrConfig.UNIT);
        BufferedImage mechImg = Offsets.getSubImage(screenshot, off.getPlayerElementLocation(ScreenPlayerElement.MECH, player));
        if (mechImg != null) {
            //enemy team in preparation screen
            mech = new TraceableImage(mechImg, OcrConfig.MECHS);//s -> s.replaceAll(".*?(READY|NOT READY|Disconnected|CONNECTING)?.*", "$1").replaceAll("^\\s*$", "DEAD")
        } else {
            mech = null;
        }
        if (off.getType() == ScreenshotType.QP_4SUMMARY) {
            matchScore = new TraceableImage(extractWhite(Offsets.getSubImage(screenshot, off.getPlayerElementLocation(ScreenPlayerElement.MATCHSCORE, player))), OcrConfig.NUMERIC);
            kills = new TraceableImage(extractWhite(Offsets.getSubImage(screenshot, off.getPlayerElementLocation(ScreenPlayerElement.KILLS, player))), OcrConfig.NUMERIC);
            assists = new TraceableImage(extractWhite(Offsets.getSubImage(screenshot, off.getPlayerElementLocation(ScreenPlayerElement.ASSISTS, player))), OcrConfig.NUMERIC);
            damage = new TraceableImage(extractWhite(Offsets.getSubImage(screenshot, off.getPlayerElementLocation(ScreenPlayerElement.DAMAGE, player))), OcrConfig.NUMERIC);
            status = new TraceableImage(extractWhite(Offsets.getSubImage(screenshot, off.getPlayerElementLocation(ScreenPlayerElement.STATUS, player))), OcrConfig.STATUS);
            ping = new TraceableImage(extractWhite(Offsets.getSubImage(screenshot, off.getPlayerElementLocation(ScreenPlayerElement.PING, player))), OcrConfig.NUMERIC);
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
        if (s.isEmpty()) {
            return "DEAD";
        }
        return s;
    }

    private String findMostLikelyMech(String s) {
        return s;
    }

    private BufferedImage extractWhite(BufferedImage input) {
        return TraceHelpers.threshold(input, new int[]{100, 100, 100});
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
        try {
            return Integer.parseInt(matchScore.getValue());
        } catch (NumberFormatException e) {
            Logger.warning(e.toString());
            return 0;
        }
    }

    public TraceableImage getMatchScoreImage() {
        return matchScore;
    }

    public int getKills() {
        if (kills == null || kills.getValue().isEmpty()) return 0;
        try {
            return Integer.parseInt(kills.getValue());
        } catch (NumberFormatException e) {
            Logger.warning(e.toString());
            return 0;
        }
    }

    public TraceableImage getKillsImage() {
        return kills;
    }

    public int getAssists() {
        if (assists == null || assists.getValue().isEmpty()) return 0;
        try {
            return Integer.parseInt(assists.getValue());
        } catch (NumberFormatException e) {
            Logger.warning(e.toString());
            return 0;
        }
    }

    public TraceableImage getAssistsImage() {
        return assists;
    }

    public int getDamage() {
        if (damage == null || damage.getValue().isEmpty()) return 0;
        try {
            return Integer.parseInt(damage.getValue());
        } catch (NumberFormatException e) {
            Logger.warning(e.toString());
            return 0;
        }
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
        try {
            return Integer.parseInt(ping.getValue());
        } catch (NumberFormatException e) {
            Logger.warning(e.toString());
            return 0;
        }
    }

    public TraceableImage getPingImage() {
        return ping;
    }
}
