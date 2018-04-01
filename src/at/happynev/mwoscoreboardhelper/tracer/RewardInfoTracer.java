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
        gameMode = new TraceableImage(new ImageModifier(Offsets.getSubImage(screenshot, off.getElementLocation(ScreenGameElement.GAMEMODE))).extractYellow().getImage(), OcrConfig.DEFAULT);
        map = new TraceableImage(new ImageModifier(Offsets.getSubImage(screenshot, off.getElementLocation(ScreenGameElement.MAP))).extractWhiteOnBlack().getImage(), OcrConfig.DEFAULT);
        battleTime = new TraceableImage(new ImageModifier(Offsets.getSubImage(screenshot, off.getElementLocation(ScreenGameElement.BATTLETIME))).extractYellow().getImage(), OcrConfig.TIME);
        matchResult = new TraceableImage(new ImageModifier(Offsets.getSubImage(screenshot, off.getElementLocation(ScreenGameElement.MATCHRESULT))).extractWhiteOnBlack().getImage(), OcrConfig.MATCHRESULT);
        xp = new TraceableImage(new ImageModifier(Offsets.getSubImage(screenshot, off.getRewardLocation(ScreenRewardElement.XP))).extractYellow().getImage(), OcrConfig.LARGENUMERIC);
        cbills = new TraceableImage(new ImageModifier(Offsets.getSubImage(screenshot, off.getRewardLocation(ScreenRewardElement.CBILLS))).extractYellow().getImage(), OcrConfig.LARGENUMERIC);
        performanceNames = new TraceableImage[9];
        performanceValues = new TraceableImage[9];
        for (int i = 0; i < performanceNames.length; i++) {
            performanceNames[i] = new TraceableImage(new ImageModifier(Offsets.getSubImage(screenshot, off.getPerformanceName(i))).upscale().extractWhiteOnBlack().invert().getImage(), OcrConfig.DEFAULT);
            performanceValues[i] = new TraceableImage(new ImageModifier(Offsets.getSubImage(screenshot, off.getPerformanceValue(i))).upscale().extractWhiteOnBlack().invert().getImage(), OcrConfig.NUMERIC);
        }
        List<TraceableImage> fields = new ArrayList<>();
        fields.addAll(Arrays.asList(map, gameMode, battleTime, matchResult, xp, cbills));
        fields.addAll(Arrays.asList(performanceNames));
        fields.addAll(Arrays.asList(performanceValues));
        traceAllAsync(fields);
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
        return gameMode.getValue();
    }

    public TraceableImage getGameModeImage() {
        return gameMode;
    }

    public int getXp() {
        if (xp == null) return 0;
        return Integer.parseInt(xp.getValue().replaceAll("\\D", ""));
    }

    public TraceableImage getXpImage() {
        return xp;
    }

    public int getCbills() {
        if (cbills == null) return 0;
        return Integer.parseInt(cbills.getValue().replaceAll("\\D", ""));
    }

    public TraceableImage getCbillsImage() {
        return cbills;
    }

    public String getPerformanceName(int i) {
        if (i > performanceNames.length) return "";
        if (performanceNames[i] == null) return "";
        return performanceNames[i].getValue();
    }

    public TraceableImage getPerformanceNameImage(int i) {
        return performanceNames[i];
    }

    public int getPerformanceValue(int i) {
        if (i > performanceValues.length) return 0;
        if (performanceValues[i] == null || performanceValues[i].getValue().isEmpty()) return 0;
        return Integer.parseInt(performanceValues[i].getValue().replaceAll("\\D", ""));
    }

    public TraceableImage getPerformanceValueImage(int i) {
        return performanceValues[i];
    }
}
