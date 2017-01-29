package at.happynev.mwoscoreboardhelper.tracer;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nev on 16.01.2017.
 */
public enum ScreenshotType {
    QP_1PREPARATION,
    QP_3SUMMARY,
    //FP_SUMMARY,
    UNDEFINED;
    private static final List<TraceableImage> lastCheck = new ArrayList<>();

    public static List<TraceableImage> getLastCheck() {
        return lastCheck;
    }

    public static ScreenshotType identifyType(BufferedImage screenshot) {
        lastCheck.clear();
        //try prep first, most time critical
        TraceableImage checkType = new TraceableImage(Offsets.getSubImage(screenshot, Offsets.getInstance(QP_1PREPARATION, screenshot).typeIdentifier()), OcrConfig.DEFAULT);
        lastCheck.add(checkType);
        checkType.performTrace();
        String ident = checkType.getValue();
        if ("Your Team".equals(ident)) {
            return QP_1PREPARATION;
        } else {
            TraceableImage checkType2 = new TraceableImage(Offsets.getSubImage(screenshot, Offsets.getInstance(QP_3SUMMARY, screenshot).typeIdentifier()), OcrConfig.DEFAULT);
            lastCheck.add(checkType2);
            checkType2.performTrace();
            String ident2 = checkType2.getValue();
            if ("Exit Match".equals(ident2)) {
                //TODO check QP vs FP
                TraceableImage checkType3 = new TraceableImage(Offsets.getSubImage(screenshot, Offsets.getInstance(QP_3SUMMARY, screenshot).winningTeam()), OcrConfig.DEFAULT);
                lastCheck.add(checkType3);
                checkType3.performTrace();
                String ident3 = checkType3.getValue();
                if (ident3.contains("Your")) {
                    return QP_3SUMMARY;
                } else {
                    //TODO: reward screen
                }
            }
        }

        return UNDEFINED;
    }
}
