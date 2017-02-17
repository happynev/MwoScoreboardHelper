package at.happynev.mwoscoreboardhelper.tracer;

import java.awt.image.BufferedImage;

/**
 * Created by Nev on 15.01.2017.
 */
public class QuickplayRewardsOffsets_16_10 extends Offsets {
    private final static int WIDTH = 3840;
    private final static int HEIGHT = 2400;

    private final static int MAP_OFFSET_X = 1650;
    private final static int MAP_OFFSET_Y = 169;
    private final static int MAP_WIDTH = 550;
    private final static int MAP_HEIGHT = 45;

    private final static int GAMEMODE_OFFSET_X = 2227;
    private final static int GAMEMODE_OFFSET_Y = MAP_OFFSET_Y;
    private final static int GAMEMODE_WIDTH = 590;
    private final static int GAMEMODE_HEIGHT = MAP_HEIGHT;

    private final static int BATTLETIME_OFFSET_X = 3113;
    private final static int BATTLETIME_OFFSET_Y = MAP_OFFSET_Y;
    private final static int BATTLETIME_WIDTH = 144;
    private final static int BATTLETIME_HEIGHT = MAP_HEIGHT;

    private final static int IDENT_OFFSET_X = 2652;
    private final static int IDENT_OFFSET_Y = 482;
    private final static int IDENT_WIDTH = 277;
    private final static int IDENT_HEIGHT = 57;

    private static final int MATCH_RESULT_X = 1136;
    private static final int MATCH_RESULT_Y = 335;
    private static final int MATCH_RESULT_WIDTH = 1530;
    private static final int MATCH_RESULT_HEIGHT = 76;

    private static final int REWARD_XP_X = 2475;
    private static final int REWARD_CBILLS_X = 646;
    private static final int REWARD_Y = 595;
    private static final int REWARD_HEIGHT = 165;
    private static final int REWARD_WIDTH = 757;

    private static final int PERF_OFFSET_NAME_X = 1560;
    private static final int PERF_OFFSET_VALUE_X = 2104;
    private static final int PERF_NAME_WIDTH = PERF_OFFSET_VALUE_X - PERF_OFFSET_NAME_X;
    private static final int PERF_VALUE_WIDTH = 100;
    private static final int PERF_FIRSTLINE_Y = 630;
    private static final int PERF_LINE_HEIGHT = 40;
    private static final double PERF_LINE_GAP = 24.5;
    private static final int PERF_LINE_COUNT = 9;

    private final double scaleFactor;

    public QuickplayRewardsOffsets_16_10(BufferedImage img) {
        this(img.getWidth(), img.getHeight());
    }

    public QuickplayRewardsOffsets_16_10(int width, int height) {
        this(getScaleFactor(width, height, WIDTH, HEIGHT));
    }

    public QuickplayRewardsOffsets_16_10(double scale) {
        scaleFactor = scale;
    }

    @Override
    public int getAbsoluteWidth() {
        return WIDTH;
    }

    @Override
    public int getAbsoluteHeight() {
        return HEIGHT;
    }

    @Override
    protected Offsets getScaledInstance(int width, int height) {
        return new QuickplayRewardsOffsets_16_10(width, height);
    }

    private Rectangle map() {
        return new Rectangle(MAP_OFFSET_X, MAP_OFFSET_Y, MAP_WIDTH, MAP_HEIGHT, scaleFactor);
    }

    private Rectangle gameMode() {
        return new Rectangle(GAMEMODE_OFFSET_X, GAMEMODE_OFFSET_Y, GAMEMODE_WIDTH, GAMEMODE_HEIGHT, scaleFactor);
    }

    private Rectangle battleTime() {
        return new Rectangle(BATTLETIME_OFFSET_X, BATTLETIME_OFFSET_Y, BATTLETIME_WIDTH, BATTLETIME_HEIGHT, scaleFactor);
    }

    private Rectangle typeIdentifier() {
        return new Rectangle(IDENT_OFFSET_X, IDENT_OFFSET_Y, IDENT_WIDTH, IDENT_HEIGHT, scaleFactor);
    }

    @Override
    public ScreenshotType getType() {
        return ScreenshotType.QP_3REWARDS;
    }

    private Rectangle matchResult() {
        return new Rectangle(MATCH_RESULT_X, MATCH_RESULT_Y, MATCH_RESULT_WIDTH, MATCH_RESULT_HEIGHT, scaleFactor);
    }

    @Override
    public Rectangle getElementLocation(ScreenGameElement element) {
        switch (element) {
            case MAP:
                return map();
            case GAMEMODE:
                return gameMode();
            case BATTLETIME:
                return battleTime();
            case TYPEIDENTIFIER:
                return typeIdentifier();
            case MATCHRESULT:
                return matchResult();
            default:
                throw new IllegalArgumentException(element + " not applicable for " + getType());
        }
    }

    private int getPerformanceLineOffset(int l) {
        double offset = PERF_FIRSTLINE_Y;
        for (int i = 0; i < l; i++) {
            offset += PERF_LINE_HEIGHT;
            offset += PERF_LINE_GAP;
        }
        return (int) offset;
    }

    @Override
    public Rectangle getPlayerElementLocation(ScreenPlayerElement element, int playerNumber) {
        throw new IllegalArgumentException(element + " not applicable for " + getType());
    }

    @Override
    public Rectangle getPerformanceName(int line) {
        int y = getPerformanceLineOffset(line);
        return new Rectangle(PERF_OFFSET_NAME_X, y, PERF_NAME_WIDTH, PERF_LINE_HEIGHT, scaleFactor);
    }

    @Override
    public Rectangle getPerformanceValue(int line) {
        int y = getPerformanceLineOffset(line);
        return new Rectangle(PERF_OFFSET_VALUE_X, y, PERF_VALUE_WIDTH, PERF_LINE_HEIGHT, scaleFactor);
    }

    @Override
    public Rectangle getRewardLocation(ScreenRewardElement element) {
        switch (element) {
            case CBILLS:
                return new Rectangle(REWARD_CBILLS_X, REWARD_Y, REWARD_WIDTH, REWARD_HEIGHT, scaleFactor);
            case XP:
                return new Rectangle(REWARD_XP_X, REWARD_Y, REWARD_WIDTH, REWARD_HEIGHT, scaleFactor);
            default:
                throw new IllegalArgumentException(element + " not applicable for " + getType());
        }
    }
}
