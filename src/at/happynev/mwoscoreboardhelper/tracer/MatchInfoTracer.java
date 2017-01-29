package at.happynev.mwoscoreboardhelper.tracer;

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
        gameMode = new TraceableImage(extractYellow(Offsets.getSubImage(screenshot, off.gameMode())), OcrConfig.DEFAULT);
        if (off.getType() == ScreenshotType.QP_1PREPARATION) {
            map = new TraceableImage(extractYellow(Offsets.getSubImage(screenshot, off.map())), OcrConfig.DEFAULT);
            server = new TraceableImage(extractWhite(Offsets.getSubImage(screenshot, off.server())), OcrConfig.DEFAULT);
            battleTime = null;
            winningTeam = null;
            losingTeam = null;
            matchResult = null;
        } else if (off.getType() == ScreenshotType.QP_3SUMMARY) {
            map = new TraceableImage(extractWhite(Offsets.getSubImage(screenshot, off.map())), OcrConfig.DEFAULT);
            battleTime = new TraceableImage(extractYellow(Offsets.getSubImage(screenshot, off.battleTime())), OcrConfig.TIME);
            winningTeam = new TraceableImage(extractWhite(Offsets.getSubImage(screenshot, off.winningTeam())), OcrConfig.TEAMS);
            losingTeam = new TraceableImage(extractWhite(Offsets.getSubImage(screenshot, off.losingTeam())), OcrConfig.TEAMS);
            matchResult = new TraceableImage(extractWhite(Offsets.getSubImage(screenshot, off.matchResult())), OcrConfig.MATCHRESULT);
            server = null;
        } else {
            map = null;
            battleTime = null;
            server = null;
            winningTeam = null;
            losingTeam = null;
            matchResult = null;
        }
        traceAllAsync(Arrays.asList(map, gameMode, server, battleTime, winningTeam, losingTeam, matchResult));
    }

    private BufferedImage extractWhite(BufferedImage input) {
        return TraceHelpers.extractSpecificColor(input, new int[]{180, 180, 180}, new int[]{255, 255, 255});
    }

    private BufferedImage extractYellow(BufferedImage input) {
        return TraceHelpers.extractSpecificColor(input, new int[]{170, 150, 30}, new int[]{255, 230, 115});
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
