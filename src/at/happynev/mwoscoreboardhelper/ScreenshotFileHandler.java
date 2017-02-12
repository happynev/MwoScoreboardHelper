package at.happynev.mwoscoreboardhelper;

import at.happynev.mwoscoreboardhelper.tracer.MatchInfoTracer;
import at.happynev.mwoscoreboardhelper.tracer.Offsets;
import at.happynev.mwoscoreboardhelper.tracer.PlayerInfoTracer;
import at.happynev.mwoscoreboardhelper.tracer.ScreenshotType;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
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
        timestamp = screenshot.lastModified();
        img = ImageIO.read(screenshot);
        type = ScreenshotType.identifyType(img);
        offsets = Offsets.getInstance(type, img);
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

    public void postProcessFile(int matchId) {
        String fileName = "";
        File archivedMatch = new File(SettingsTabController.getPostProcessedDirectory(), "match-" + matchId);
        if (!screenshot.getName().contains(ScreenshotType.QP_1PREPARATION.toString()) && !screenshot.getName().contains(ScreenshotType.QP_3SUMMARY.toString())) {
            fileName = SettingsTabController.getPlayername() + "-match-" + type + "." + screenshot.getName();
        } else {
            fileName = screenshot.getName();
        }
        archivedMatch.mkdirs();
        boolean deleteScreenshots = SettingsTabController.isDeleteScreenshots();
        File arch = new File(archivedMatch, fileName);
        if (deleteScreenshots) {
            if (arch.exists()) {
                arch.delete();
            }
            boolean success = screenshot.renameTo(arch);
            if (!success) {
                Logger.alertPopup("Failed to move " + screenshot.getName() + " to " + arch.toString());
            }
        } else {
            try {
                PreparedStatement prep = DbHandler.getInstance().prepareStatement("insert into processed(filename,processing_time) values(?,?)");
                prep.setString(1, screenshot.getName());
                prep.setTimestamp(2, new Timestamp(timestamp));
                prep.executeUpdate();
                WatcherTabController.getInstance().getAlreadyProcessed().add(screenshot.getName());
                Utils.copyFile(screenshot, arch);
            } catch (Exception e) {
                Logger.error(e);
            }
        }
    }

    public ScreenshotType getType() {
        return type;
    }
}
