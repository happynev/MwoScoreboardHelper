package at.happynev.mwoscoreboardhelper;

import at.happynev.mwoscoreboardhelper.tracer.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.Timestamp;

/**
 * Created by Nev on 12.02.2017.
 */
public class ScreenshotFileHandler {
    private final File screenshot;
    private final long timestamp;
    private final ScreenshotType type;
    private final BufferedImage img;
    private final Offsets offsets;

    public ScreenshotFileHandler(File screenshot) throws Exception {
        this.screenshot = screenshot;
        long tmptimestamp = screenshot.lastModified();
        timestamp = tmptimestamp - (tmptimestamp % 1000);//strip millis for db
        img = ImageIO.read(screenshot);
        type = ScreenshotType.identifyType(img);
        offsets = Offsets.getInstance(type, img);
        markProcessed();
        if (type == ScreenshotType.UNDEFINED) {
            throw new Exception("Screenshot cannot be identified");
        }
    }

    public MatchInfoTracer getMatchTracer() {
        return new MatchInfoTracer(img, offsets);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public PlayerInfoTracer getPlayerInfoTracer(int playerNum) {
        return new PlayerInfoTracer(img, playerNum, offsets);
    }

    public void markProcessed() {
        try {
            PreparedStatement prep = DbHandler.getInstance().prepareStatement("insert into processed(filename,processing_time) values(?,?)");
            prep.setString(1, screenshot.getName());
            prep.setTimestamp(2, new Timestamp(timestamp));
            prep.executeUpdate();
            WatcherTabController.getInstance().getAlreadyProcessed().add(screenshot.getName());
        } catch (Exception e) {
            Logger.error(e);
        }
    }

    public void archiveFile(int matchId) {
        String fileName = "";
        File archivedMatch = new File(SettingsTabController.getPostProcessedDirectory(), "match-" + matchId);
        if (!screenshot.getName().contains(ScreenshotType.QP_1PREPARATION.toString()) && !screenshot.getName().contains(ScreenshotType.QP_4SUMMARY.toString())) {
            fileName = SettingsTabController.getPlayername() + "-match-" + type + "." + screenshot.getName();
        } else {
            fileName = screenshot.getName();
        }
        archivedMatch.mkdirs();
        boolean deleteScreenshots = SettingsTabController.isDeleteScreenshots();
        File arch = new File(archivedMatch, fileName);
        if (arch.exists()) {
            arch.delete();
        }
        if (deleteScreenshots) {
            boolean success = screenshot.renameTo(arch);
            if (!success) {
                Logger.alertPopup("Failed to move " + screenshot.getName() + " to " + arch.toString());
            }
        } else {
            try {
                Utils.copyFile(screenshot, arch);
                arch.setLastModified(screenshot.lastModified());
            } catch (IOException e) {
                Logger.alertPopup("Failed to copy " + screenshot.getName() + " to " + arch.toString());
            }
        }
    }

    public ScreenshotType getType() {
        return type;
    }

    public RewardInfoTracer getRewardInfoTracer() {
        return new RewardInfoTracer(img, offsets);
    }
}
