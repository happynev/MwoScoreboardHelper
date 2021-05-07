package at.happynev.mwoscoreboardhelper.tracer;

import java.awt.image.BufferedImage;

/**
 * Created by Nev on 15.01.2017.
 */
public class QuickplaySummaryOffsets_16_9 extends Offsets {
    private final static int WIDTH = 3840;
    private final static int HEIGHT = 2160;

    private final static int FIRST_PLAYERLINE_OFFSET = 360;
    private final static int FIRST_PLAYERFIELD_OFFSET = 1214;
    private final static int PLAYER_LINE_HEIGHT = 45;
    private final static int PLAYER_UNIT_WIDTH = 146;
    private final static int PLAYER_PILOTNAME_WIDTH = 488;
    private final static int PLAYER_MECH_WIDTH = 281;
    private final static int PLAYER_STATUS_WIDTH = 240;
    private final static int PLAYER_MATCHSCORE_WIDTH = 180;
    private final static int PLAYER_CAPTIME_WIDTH = 220;
    private final static int PLAYER_KILLS_WIDTH = 136;
    private final static int PLAYER_ASSISTS_WIDTH = 149;
    private final static int PLAYER_DAMAGE_WIDTH = 120;
    private final static int PLAYER_PING_WIDTH = 85;
    private final static int PLAYER_UNIT_OFFSET = FIRST_PLAYERFIELD_OFFSET;
    private final static int PLAYER_PILOTNAME_OFFSET = PLAYER_UNIT_OFFSET + PLAYER_UNIT_WIDTH;
    private final static int PLAYER_MECH_OFFSET = PLAYER_PILOTNAME_OFFSET + PLAYER_PILOTNAME_WIDTH;
    private final static int PLAYER_STATUS_OFFSET = PLAYER_MECH_OFFSET + PLAYER_MECH_WIDTH;
    private final static int PLAYER_MATCHSCORE_OFFSET = PLAYER_STATUS_OFFSET + PLAYER_STATUS_WIDTH;
    private final static int PLAYER_KILLS_OFFSET = PLAYER_MATCHSCORE_OFFSET + PLAYER_MATCHSCORE_WIDTH+PLAYER_CAPTIME_WIDTH;
    private final static int PLAYER_ASSISTS_OFFSET = PLAYER_KILLS_OFFSET + PLAYER_KILLS_WIDTH;
    private final static int PLAYER_DAMAGE_OFFSET = PLAYER_ASSISTS_OFFSET + PLAYER_ASSISTS_WIDTH;
    private final static int PLAYER_PING_OFFSET = PLAYER_DAMAGE_OFFSET + PLAYER_DAMAGE_WIDTH;
    //1.7778
    private final static double PLAYER_LINE_GAP = 6.7;
    private final static double LANCE_LINE_GAP = 20;
    private final static double TEAM_LINE_GAP = 33;

    private final static int MAP_OFFSET_X = 1290;
    private final static int MAP_OFFSET_Y = 49;
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

    private final static int IDENT_OFFSET_X = 2980;
    private final static int IDENT_OFFSET_Y = 2050;
    private final static int IDENT_WIDTH = 170;
    private final static int IDENT_HEIGHT = 45;

    private static final int TEAM_OFFSETX = 530;
    private static final int TEAM_OFFSET_WINNER = 708;
    private static final int TEAM_OFFSET_LOSER = 1381;
    private static final int TEAM_RESULT_WIDTH = 268;
    private static final int TEAM_RESULT_HEIGHT = 65;

    private static final int MATCH_RESULT_X = 1750;
    private static final int MATCH_RESULT_Y = 217;
    private static final int MATCH_RESULT_WIDTH = 309;
    private static final int MATCH_RESULT_HEIGHT = 76;

    private final double scaleFactor;

    public QuickplaySummaryOffsets_16_9(BufferedImage img) {
        this(img.getWidth(), img.getHeight());
    }

    public QuickplaySummaryOffsets_16_9(int width, int height) {
        this(getScaleFactor(width, height, WIDTH, HEIGHT));
    }

    public QuickplaySummaryOffsets_16_9(double scale) {
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
        return new QuickplaySummaryOffsets_16_9(width, height);
    }

    private Rectangle map() {
        return new Offsets.Rectangle(MAP_OFFSET_X, MAP_OFFSET_Y, MAP_WIDTH, MAP_HEIGHT, scaleFactor);
    }

    private Rectangle gameMode() {
        return new Offsets.Rectangle(GAMEMODE_OFFSET_X, GAMEMODE_OFFSET_Y, GAMEMODE_WIDTH, GAMEMODE_HEIGHT, scaleFactor);
    }

    private Rectangle battleTime() {
        return new Offsets.Rectangle(BATTLETIME_OFFSET_X, BATTLETIME_OFFSET_Y, BATTLETIME_WIDTH, BATTLETIME_HEIGHT, scaleFactor);
    }

    private Rectangle typeIdentifier() {
        return new Rectangle(IDENT_OFFSET_X, IDENT_OFFSET_Y, IDENT_WIDTH, IDENT_HEIGHT, scaleFactor);
    }

    private int getPlayerLineOffset(int p) {
        double playerLineOffset = FIRST_PLAYERLINE_OFFSET;
        for (int i = 0; i < p; i++) {
            playerLineOffset += PLAYER_LINE_HEIGHT;
            if (i == 3 || i == 7 || i == 15 || i == 19) {
                playerLineOffset += LANCE_LINE_GAP;
            } else if (i == 11) {
                playerLineOffset += TEAM_LINE_GAP;
            } else {
                playerLineOffset += PLAYER_LINE_GAP;
            }
        }
        return (int) playerLineOffset;
    }

    private Rectangle playerUnit(int i) {
        return new Rectangle(PLAYER_UNIT_OFFSET, getPlayerLineOffset(i), PLAYER_UNIT_WIDTH, PLAYER_LINE_HEIGHT, scaleFactor);
    }

    private Rectangle playerPilotName(int i) {
        return new Rectangle(PLAYER_PILOTNAME_OFFSET, getPlayerLineOffset(i), PLAYER_PILOTNAME_WIDTH, PLAYER_LINE_HEIGHT, scaleFactor);
    }

    private Rectangle playerMech(int i) {
        return new Rectangle(PLAYER_MECH_OFFSET, getPlayerLineOffset(i), PLAYER_MECH_WIDTH, PLAYER_LINE_HEIGHT, scaleFactor);
    }

    private Rectangle playerStatus(int i) {
        return new Rectangle(PLAYER_STATUS_OFFSET, getPlayerLineOffset(i), PLAYER_STATUS_WIDTH, PLAYER_LINE_HEIGHT, scaleFactor);
    }

    private Rectangle playerMatchScore(int i) {
        return new Rectangle(PLAYER_MATCHSCORE_OFFSET, getPlayerLineOffset(i), PLAYER_MATCHSCORE_WIDTH, PLAYER_LINE_HEIGHT, scaleFactor);
    }

    private Rectangle playerKills(int i) {
        return new Rectangle(PLAYER_KILLS_OFFSET, getPlayerLineOffset(i), PLAYER_KILLS_WIDTH, PLAYER_LINE_HEIGHT, scaleFactor);
    }

    private Rectangle playerAssists(int i) {
        return new Rectangle(PLAYER_ASSISTS_OFFSET, getPlayerLineOffset(i), PLAYER_ASSISTS_WIDTH, PLAYER_LINE_HEIGHT, scaleFactor);
    }

    private Rectangle playerDamage(int i) {
        return new Rectangle(PLAYER_DAMAGE_OFFSET, getPlayerLineOffset(i), PLAYER_DAMAGE_WIDTH, PLAYER_LINE_HEIGHT, scaleFactor);
    }

    private Rectangle playerPing(int i) {
        return new Rectangle(PLAYER_PING_OFFSET, getPlayerLineOffset(i), PLAYER_PING_WIDTH, PLAYER_LINE_HEIGHT, scaleFactor);
    }

    @Override
    public ScreenshotType getType() {
        return ScreenshotType.QP_4SUMMARY;
    }

    private Rectangle winningTeam() {
        return new Rectangle(TEAM_OFFSETX, TEAM_OFFSET_WINNER, TEAM_RESULT_WIDTH, TEAM_RESULT_HEIGHT, scaleFactor);
    }

    private Rectangle losingTeam() {
        return new Rectangle(TEAM_OFFSETX, TEAM_OFFSET_LOSER, TEAM_RESULT_WIDTH, TEAM_RESULT_HEIGHT, scaleFactor);
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
            case WINNINGTEAM:
                return winningTeam();
            case LOSINGTEAM:
                return losingTeam();
            case MATCHRESULT:
                return matchResult();
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
            case MATCHSCORE:
                return playerMatchScore(playerNumber);
            case KILLS:
                return playerKills(playerNumber);
            case ASSISTS:
                return playerAssists(playerNumber);
            case DAMAGE:
                return playerDamage(playerNumber);
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
}
