package at.happynev.mwoscoreboardhelper.tracer;

import net.sourceforge.tess4j.util.ImageHelper;

import java.awt.image.BufferedImage;
import java.util.Arrays;

/**
 * Created by Nev on 15.01.2017.
 */
public class MatchInfoTracer extends AsyncTracer {
    private final TraceableImage map;
    private final TraceableImage gameMode;
    private final TraceableImage server;
    private final TraceableImage battleTime;
    private final TraceableImage winningTeam;
    private final TraceableImage losingTeam;
    private final TraceableImage matchResult;

    public MatchInfoTracer(BufferedImage screenshot, Offsets off) {
        map = new TraceableImage(ImageHelper.convertImageToGrayscale(Offsets.getSubImage(screenshot, off.map())), OcrConfig.DEFAULT);
        gameMode = new TraceableImage(ImageHelper.convertImageToGrayscale(Offsets.getSubImage(screenshot, off.gameMode())), OcrConfig.DEFAULT);
        if (off.getType() == ScreenshotType.QP_1PREPARATION) {
            server = new TraceableImage(Offsets.getSubImage(screenshot, off.server()), OcrConfig.DEFAULT);
            battleTime = null;
            winningTeam = null;
            losingTeam = null;
            matchResult = null;
        } else if (off.getType() == ScreenshotType.QP_3SUMMARY) {
            battleTime = new TraceableImage(Offsets.getSubImage(screenshot, off.battleTime()), OcrConfig.TIME);
            winningTeam = new TraceableImage(Offsets.getSubImage(screenshot, off.winningTeam()), OcrConfig.TEAMS);
            losingTeam = new TraceableImage(Offsets.getSubImage(screenshot, off.losingTeam()), OcrConfig.TEAMS);
            matchResult = new TraceableImage(Offsets.getSubImage(screenshot, off.matchResult()), OcrConfig.MATCHRESULT);
            server = null;
        } else {
            battleTime = null;
            server = null;
            winningTeam = null;
            losingTeam = null;
            matchResult = null;
        }
        traceAllAsync(Arrays.asList(map, gameMode, server, battleTime, winningTeam, losingTeam, matchResult));
    }

    public TraceableImage getWinningTeamImage() {
        return winningTeam;
    }

    public String getWinningTeam() {
        if (winningTeam == null) return "";
        return winningTeam.getValue();
    }

    public TraceableImage getLosingTeamImage() {
        return losingTeam;
    }

    public String getLosingTeam() {
        if (losingTeam == null) return "";
        return losingTeam.getValue();
    }

    public TraceableImage getMatchResultImage() {
        return matchResult;
    }

    public String getMatchResult() {
        if (matchResult == null) return "";
        return matchResult.getValue();
    }

    public String getBattleTime() {
        if (battleTime == null) return "";
        return battleTime.getValue();
    }

    public TraceableImage getBattleTimeImage() {
        return battleTime;
    }

    public String getMap() {
        if (map == null) return "";
        return map.getValue();
    }

    public TraceableImage getMapImage() {
        return map;
    }

    public String getGameMode() {
        if (gameMode == null) return "";
        return gameMode.getValue().replaceAll("GAMEMODE:", "");
    }

    public TraceableImage getGameModeImage() {
        return gameMode;
    }

    public String getServer() {
        if (server == null) return "";
        return server.getValue();
    }

    public TraceableImage getServerImage() {
        return server;
    }
}
