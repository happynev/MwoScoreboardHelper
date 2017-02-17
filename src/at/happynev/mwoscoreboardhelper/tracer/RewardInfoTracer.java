package at.happynev.mwoscoreboardhelper.tracer;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Nev on 15.01.2017.
 */
public class RewardInfoTracer extends AsyncTracer {
    private final TraceableImage map;
    private final TraceableImage gameMode;
    private final TraceableImage battleTime;
    private final TraceableImage matchResult;
    private final TraceableImage xp;
    private final TraceableImage cbills;
    private final TraceableImage[] performanceNames;
    private final TraceableImage[] performanceValues;

    public RewardInfoTracer(BufferedImage screenshot, Offsets off) {
        gameMode = new TraceableImage(extractYellow(Offsets.getSubImage(screenshot, off.getElementLocation(ScreenGameElement.GAMEMODE))), OcrConfig.DEFAULT);
        map = new TraceableImage(extractWhite(Offsets.getSubImage(screenshot, off.getElementLocation(ScreenGameElement.MAP))), OcrConfig.DEFAULT);
        battleTime = new TraceableImage(extractYellow(Offsets.getSubImage(screenshot, off.getElementLocation(ScreenGameElement.BATTLETIME))), OcrConfig.TIME);
        matchResult = new TraceableImage(extractWhite(Offsets.getSubImage(screenshot, off.getElementLocation(ScreenGameElement.MATCHRESULT))), OcrConfig.MATCHRESULT);
        xp = new TraceableImage(extractYellow(Offsets.getSubImage(screenshot, off.getRewardLocation(ScreenRewardElement.XP))), OcrConfig.DEFAULT);
        cbills = new TraceableImage(extractYellow(Offsets.getSubImage(screenshot, off.getRewardLocation(ScreenRewardElement.CBILLS))), OcrConfig.DEFAULT);
        performanceNames = new TraceableImage[9];
        performanceValues = new TraceableImage[9];
        for (int i = 0; i < performanceNames.length; i++) {
            performanceNames[i] = new TraceableImage(extractWhite(Offsets.getSubImage(screenshot, off.getPerformanceName(i))), OcrConfig.DEFAULT);
            performanceValues[i] = new TraceableImage(extractWhite(Offsets.getSubImage(screenshot, off.getPerformanceValue(i))), OcrConfig.NUMERIC);
        }
        List<TraceableImage> fields = new ArrayList<>();
        fields.addAll(Arrays.asList(map, gameMode, battleTime, matchResult, xp, cbills));
        fields.addAll(Arrays.asList(performanceNames));
        fields.addAll(Arrays.asList(performanceValues));
        traceAllAsync(fields);
    }

    private BufferedImage extractWhite(BufferedImage input) {
        return TraceHelpers.extractSpecificColor(input, new int[]{180, 180, 180}, new int[]{255, 255, 255});
    }

    private BufferedImage extractYellow(BufferedImage input) {
        return TraceHelpers.extractSpecificColor(input, new int[]{170, 150, 30}, new int[]{255, 230, 115});
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

    public String getXp() {
        if (xp == null) return "0";
        return xp.getValue();
    }

    public TraceableImage getXpImage() {
        return xp;
    }

    public String getCbills() {
        if (cbills == null) return "0";
        return cbills.getValue();
    }

    public TraceableImage getCbillsImage() {
        return cbills;
    }

    public String getPerformanceName(int i) {
        if (i > performanceNames.length) return "0";
        if (performanceNames[i] == null) return "0";
        return performanceNames[i].getValue();
    }

    public TraceableImage getPerformanceNameImage(int i) {
        return performanceNames[i];
    }

    public String getPerformanceValue(int i) {
        if (i > performanceValues.length) return "0";
        if (performanceValues[i] == null) return "0";
        return performanceValues[i].getValue();
    }

    public TraceableImage getPerformanceValueImage(int i) {
        return performanceValues[i];
    }
}
