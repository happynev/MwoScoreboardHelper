package at.happynev.mwoscoreboardhelper.tracer;

import java.awt.image.BufferedImage;

/**
 * Created by Nev on 15.01.2017.
 */
public class QuickplayPreparationOffsets_16_9 extends Offsets {
    private final static int WIDTH = 3840;
    private final static int HEIGHT = 2160;

    private final static int FIRST_PLAYERLINE_OFFSET = 402;
    private final static int FIRST_PLAYERFIELD_OFFSET_FRIEND = 555;
    private final static int FIRST_PLAYERFIELD_OFFSET_ENEMY = 2477;
    private final static int PLAYER_LINE_HEIGHT = 45;
    private final static int PLAYER_UNIT_WIDTH = 146;
    private final static int PLAYER_PILOTNAME_WIDTH_FRIEND = 586;
    private final static int PLAYER_PILOTNAME_WIDTH_ENEMY = 669;
    private final static int PLAYER_MECH_WIDTH = 333;
    private final static int PLAYER_STATUS_WIDTH_FRIEND = 297;
    private final static int PLAYER_STATUS_WIDTH_ENEMY = 243;
    private final static int PLAYER_PING_WIDTH = 120;

    private final static double PLAYER_LINE_GAP = 8;
    private final static double LANCE_LINE_GAP = 9.5;

    private final static int MAP_OFFSET_X = 2326;
    private final static int MAP_OFFSET_Y = 1193;
    private final static int MAP_WIDTH = 465;
    private final static int MAP_HEIGHT = 50;

    private final static int GAMEMODE_OFFSET_X = 640;
    private final static int GAMEMODE_OFFSET_Y = 1130;
    private final static int GAMEMODE_WIDTH = 355;
    private final static int GAMEMODE_HEIGHT = 55;

    private final static int SERVER_OFFSET_X = 2677;
    private final static int SERVER_OFFSET_Y = 130;
    private final static int SERVER_WIDTH = 610;
    private final static int SERVER_HEIGHT = 45;

    private final static int IDENT_OFFSET_X = 340;
    private final static int IDENT_OFFSET_Y = 227;
    private final static int IDENT_WIDTH = 320;
    private final static int IDENT_HEIGHT = 72;

    private final double scaleFactor;

    public QuickplayPreparationOffsets_16_9(BufferedImage img) {
        this(img.getWidth(), img.getHeight());
    }

    public QuickplayPreparationOffsets_16_9(int width, int height) {
        this(getScaleFactor(width, height, WIDTH, HEIGHT));
    }

    public QuickplayPreparationOffsets_16_9(double scale) {
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
    public Rectangle getElementLocation(ScreenGameElement element) {
        switch (element) {
            case MAP:
                return map();
            case GAMEMODE:
                return gameMode();
            case SERVER:
                return server();
            case TYPEIDENTIFIER:
                return typeIdentifier();
            default:
                throw new IllegalArgumentException(element + " not applicable for " + getType());
        }
    }

    @Override
    public Rectangle getPlayerElementLocation(ScreenPlayerElement element, int playerNumber) {
        switch (element) {
            case UNIT:
                return playerUnit(playerNumber);
            case PILOTNAME:
                return playerPilotName(playerNumber);
            case MECH:
                return playerMech(playerNumber);
            case STATUS:
                return playerStatus(playerNumber);
            case PING:
                return playerPing(playerNumber);
            default:
                throw new IllegalArgumentException(element + " not applicable for " + getType());
        }
    }

    @Override
    public Rectangle getPerformanceName(int line) {
        throw new IllegalArgumentException("performance not applicable for " + getType());
    }

    @Override
    public Rectangle getPerformanceValue(int line) {
        throw new IllegalArgumentException("performance not applicable for " + getType());
    }

    @Override
    public Rectangle getRewardLocation(ScreenRewardElement element) {
        throw new IllegalArgumentException(element + " not applicable for " + getType());
    }

    private Rectangle map() {
        return new Rectangle(MAP_OFFSET_X, MAP_OFFSET_Y, MAP_WIDTH, MAP_HEIGHT, scaleFactor);
    }

    private Rectangle gameMode() {
        return new Rectangle(GAMEMODE_OFFSET_X, GAMEMODE_OFFSET_Y, GAMEMODE_WIDTH, GAMEMODE_HEIGHT, scaleFactor);
    }

    private int getPlayerLineOffset(int p) {
        p = p % 12;
        double playerLineOffset = FIRST_PLAYERLINE_OFFSET;
        for (int i = 0; i < p; i++) {
            playerLineOffset += PLAYER_LINE_HEIGHT;
            if (i == 3 || i == 7) {
                playerLineOffset += LANCE_LINE_GAP;
            } else {
                playerLineOffset += PLAYER_LINE_GAP;
            }
        }
        return (int) playerLineOffset;
    }

    private Rectangle server() {
        return new Rectangle(SERVER_OFFSET_X, SERVER_OFFSET_Y, SERVER_WIDTH, SERVER_HEIGHT, scaleFactor);
    }

    private Rectangle typeIdentifier() {
        return new Rectangle(IDENT_OFFSET_X, IDENT_OFFSET_Y, IDENT_WIDTH, IDENT_HEIGHT, scaleFactor);
    }

    private Rectangle playerUnit(int i) {
        int x = 0;
        if (i < 12) {
            x = FIRST_PLAYERFIELD_OFFSET_FRIEND;
        } else {
            x = FIRST_PLAYERFIELD_OFFSET_ENEMY;
        }
        return new Rectangle(x, getPlayerLineOffset(i), PLAYER_UNIT_WIDTH, PLAYER_LINE_HEIGHT, scaleFactor);
    }

    private Rectangle playerPilotName(int i) {
        int x = 0;
        int w = 0;
        if (i < 12) {
            x = FIRST_PLAYERFIELD_OFFSET_FRIEND + PLAYER_UNIT_WIDTH;
            w = PLAYER_PILOTNAME_WIDTH_FRIEND;
        } else {
            x = FIRST_PLAYERFIELD_OFFSET_ENEMY + PLAYER_UNIT_WIDTH;
            w = PLAYER_PILOTNAME_WIDTH_ENEMY;
        }
        return new Rectangle(x, getPlayerLineOffset(i), w, PLAYER_LINE_HEIGHT, scaleFactor);
    }

    private Rectangle playerMech(int i) {
        int x = 0;
        if (i < 12) {
            x = FIRST_PLAYERFIELD_OFFSET_FRIEND + PLAYER_UNIT_WIDTH + PLAYER_PILOTNAME_WIDTH_FRIEND;
        } else {
            return new Rectangle(0, 0, 0, 0, scaleFactor);
        }
        return new Rectangle(x, getPlayerLineOffset(i), PLAYER_MECH_WIDTH, PLAYER_LINE_HEIGHT, scaleFactor);
    }

    private Rectangle playerStatus(int i) {
        int x = 0;
        int w = 0;
        if (i < 12) {
            x = FIRST_PLAYERFIELD_OFFSET_FRIEND + PLAYER_UNIT_WIDTH + PLAYER_PILOTNAME_WIDTH_FRIEND + PLAYER_MECH_WIDTH;
            w = PLAYER_STATUS_WIDTH_FRIEND;
        } else {
            x = FIRST_PLAYERFIELD_OFFSET_ENEMY + PLAYER_UNIT_WIDTH + PLAYER_PILOTNAME_WIDTH_ENEMY;
            w = PLAYER_STATUS_WIDTH_ENEMY;
        }
        return new Rectangle(x, getPlayerLineOffset(i), w, PLAYER_LINE_HEIGHT, scaleFactor);
    }

    private Rectangle playerPing(int i) {
        int x = 0;
        if (i < 12) {
            x = FIRST_PLAYERFIELD_OFFSET_FRIEND + PLAYER_UNIT_WIDTH + PLAYER_PILOTNAME_WIDTH_FRIEND + PLAYER_MECH_WIDTH + PLAYER_STATUS_WIDTH_FRIEND;
        } else {
            x = FIRST_PLAYERFIELD_OFFSET_ENEMY + PLAYER_UNIT_WIDTH + PLAYER_PILOTNAME_WIDTH_ENEMY + PLAYER_STATUS_WIDTH_ENEMY;
        }
        return new Rectangle(x, getPlayerLineOffset(i), PLAYER_PING_WIDTH, PLAYER_LINE_HEIGHT, scaleFactor);
    }

    @Override
    protected Offsets getScaledInstance(int width, int height) {
        return new QuickplayPreparationOffsets_16_9(width, height);
    }

    @Override
    public ScreenshotType getType() {
        return ScreenshotType.QP_1PREPARATION;
    }
}
