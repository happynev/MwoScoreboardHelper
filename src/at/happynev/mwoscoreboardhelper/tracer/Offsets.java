package at.happynev.mwoscoreboardhelper.tracer;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Nev on 15.01.2017.
 */
public abstract class Offsets {

    private final static double MARGIN = 0.01d;
    private static final List<Offsets> reference = new ArrayList<>(Arrays.asList(
            new QuickplaySummaryOffsets_16_9(1),
            new QuickplaySummaryOffsets_16_10(1),
            new QuickplayPreparationOffsets_16_9(1),
            new QuickplayPreparationOffsets_16_10(1),
            new QuickplayRewardsOffsets_16_9(1),
            new QuickplayRewardsOffsets_16_10(1)
    ));

    public final static Offsets getInstance(ScreenshotType type, BufferedImage img) {
        for (Offsets o : reference) {
            if (o.getType() == type && o.matchesAspect(img.getWidth(), img.getHeight()))
                return o.getScaledInstance(img.getWidth(), img.getHeight());
        }
        return null;
    }

    public static BufferedImage getSubImage(BufferedImage img, Rectangle crop) {
        if (crop.w == 0 || crop.h == 0) {
            return null;
        }
        return img.getSubimage(crop.x, crop.y, crop.w, crop.h);
    }

    public final static double getScaleFactor(int sourceWidth, int sourceHeight, int targetWidth, int targetHeight) throws IllegalArgumentException {
        double widthScale = (double) sourceWidth / (double) targetWidth;
        double heightScale = (double) sourceHeight / (double) targetHeight;
        if (!doubleEquals(widthScale, heightScale)) throw new IllegalArgumentException("Aspect Ratio mismatch");
        return widthScale;
    }

    protected final static double calculateAspect(double w, double h) {
        return w / h;
    }

    protected static boolean doubleEquals(double a, double b) {
        if (a - b > MARGIN) return false;
        return a - b >= -MARGIN;
    }

    protected abstract Offsets getScaledInstance(int width, int height);

    public abstract ScreenshotType getType();

    public abstract int getAbsoluteWidth();

    public abstract int getAbsoluteHeight();

    public final boolean matchesAspect(int width, int height) {
        double a1 = calculateAspect(width, height);
        double a2 = getAspectRatio();
        boolean ret = doubleEquals(a1, a2);
        return ret;
    }

    public final double getAspectRatio() {
        double ret = calculateAspect(getAbsoluteWidth(), getAbsoluteHeight());
        return ret;
    }

    public abstract Rectangle getElementLocation(ScreenGameElement element);

    public abstract Rectangle getPlayerElementLocation(ScreenPlayerElement element, int playerNumber);

    public abstract Rectangle getPerformanceName(int line);

    public abstract Rectangle getPerformanceValue(int line);

    public abstract Rectangle getRewardLocation(ScreenRewardElement element);

    public static class Rectangle {
        public int x = 0;
        public int y = 0;
        public int w = 0;
        public int h = 0;

        public Rectangle(int x, int y, int w, int h, double scale) {
            this.x = (int) (x * scale);
            this.y = (int) (y * scale);
            this.w = (int) (w * scale);
            this.h = (int) (h * scale);
        }
    }
}
