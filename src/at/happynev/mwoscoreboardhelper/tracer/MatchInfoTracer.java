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
        gameMode = new TraceableImage(new ImageModifier(Offsets.getSubImage(screenshot, off.getElementLocation(ScreenGameElement.GAMEMODE))).extractYellow().getImage(), OcrConfig.DEFAULT);
        if (off.getType() == ScreenshotType.QP_1PREPARATION) {
            map = new TraceableImage(new ImageModifier(Offsets.getSubImage(screenshot, off.getElementLocation(ScreenGameElement.MAP))).extractYellow().getImage(), OcrConfig.DEFAULT);
            server = new TraceableImage(new ImageModifier(Offsets.getSubImage(screenshot, off.getElementLocation(ScreenGameElement.SERVER))).extractWhiteNarrow().getImage(), OcrConfig.DEFAULT);
            battleTime = null;
            winningTeam = null;
            losingTeam = null;
            matchResult = null;
        } else if (off.getType() == ScreenshotType.QP_4SUMMARY) {
            map = new TraceableImage(new ImageModifier(Offsets.getSubImage(screenshot, off.getElementLocation(ScreenGameElement.MAP))).extractWhiteNarrow().getImage(), OcrConfig.DEFAULT);
            battleTime = new TraceableImage(new ImageModifier(Offsets.getSubImage(screenshot, off.getElementLocation(ScreenGameElement.BATTLETIME))).extractYellow().getImage(), OcrConfig.TIME);
            winningTeam = new TraceableImage(new ImageModifier(Offsets.getSubImage(screenshot, off.getElementLocation(ScreenGameElement.WINNINGTEAM))).extractWhiteNarrow().getImage(), OcrConfig.TEAMS);
            losingTeam = new TraceableImage(new ImageModifier(Offsets.getSubImage(screenshot, off.getElementLocation(ScreenGameElement.LOSINGTEAM))).extractWhiteNarrow().getImage(), OcrConfig.TEAMS);
            matchResult = new TraceableImage(new ImageModifier(Offsets.getSubImage(screenshot, off.getElementLocation(ScreenGameElement.MATCHRESULT))).extractWhiteNarrow().getImage(), OcrConfig.MATCHRESULT);
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
        if (battleTime.getValue().matches("\\d{1,2}:\\d{2}")) return battleTime.getValue();
        return "";
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
