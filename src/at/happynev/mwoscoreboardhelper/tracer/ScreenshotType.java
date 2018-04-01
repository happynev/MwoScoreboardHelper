package at.happynev.mwoscoreboardhelper.tracer;

import org.apache.commons.lang3.StringUtils;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nev on 16.01.2017.
 */
public enum ScreenshotType {
    //PRELAUNCH,
    QP_1PREPARATION,
    //QP_2DEATH,
    QP_3REWARDS,
    QP_4SUMMARY,
    //FP_SUMMARY,
    UNDEFINED;
    private static final List<TraceableImage> lastCheck = new ArrayList<>();

    public static List<TraceableImage> getLastCheck() {
        return lastCheck;
    }

    public static ScreenshotType identifyType(BufferedImage screenshot) {
        lastCheck.clear();
        //try prep first, most time critical
        TraceableImage checkType = new TraceableImage(new ImageModifier(Offsets.getSubImage(screenshot, Offsets.getInstance(QP_1PREPARATION, screenshot).getElementLocation(ScreenGameElement.TYPEIDENTIFIER))).upscale().getImage(), OcrConfig.DEFAULT);
        lastCheck.add(checkType);
        checkType.performTrace();
        String ident = checkType.getValue();
        if (StringUtils.getLevenshteinDistance(ident, "Your Team") < 4) {
            return QP_1PREPARATION;
        } else {
            TraceableImage checkType2 = new TraceableImage(new ImageModifier(Offsets.getSubImage(screenshot, Offsets.getInstance(QP_4SUMMARY, screenshot).getElementLocation(ScreenGameElement.TYPEIDENTIFIER))).upscale().extractWhiteOnBlack().getImage(), OcrConfig.DEFAULT);
            lastCheck.add(checkType2);
            checkType2.performTrace();
            String ident2 = checkType2.getValue();
            if (StringUtils.getLevenshteinDistance(ident2, "Exit Match") < 4) {
                //TODO check QP vs FP
                TraceableImage checkType3 = new TraceableImage(new ImageModifier(Offsets.getSubImage(screenshot, Offsets.getInstance(QP_4SUMMARY, screenshot).getElementLocation(ScreenGameElement.WINNINGTEAM))).upscale().getImage(), OcrConfig.DEFAULT);
                lastCheck.add(checkType3);
                checkType3.performTrace();
                String ident3 = checkType3.getValue();
                if (ident3.contains("Your")) {
                    return QP_4SUMMARY;
                } else {
                    TraceableImage checkType4 = new TraceableImage(new ImageModifier(Offsets.getSubImage(screenshot, Offsets.getInstance(QP_3REWARDS, screenshot).getElementLocation(ScreenGameElement.TYPEIDENTIFIER))).upscale().extractWhiteOnBlack().getImage(), OcrConfig.DEFAULT);
                    lastCheck.add(checkType4);
                    checkType4.performTrace();
                    String ident4 = checkType4.getValue();
                    if (StringUtils.getLevenshteinDistance(ident4, "XP EARNED") < 3) {
                        return QP_3REWARDS;
                    }
                }
            }
        }
        return UNDEFINED;
    }

    @Override
    public String toString() {
        switch (this) {
            case QP_1PREPARATION:
                return "Quickplay Drop preparation";
            case QP_3REWARDS:
                return "Quickplay Match Rewards";
            case QP_4SUMMARY:
                return "Quickplay Match Summary";
            case UNDEFINED:
                return "Unrecognized Screenshot";
        }
        return "undefined";
    }
}
