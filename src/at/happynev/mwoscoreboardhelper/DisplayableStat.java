package at.happynev.mwoscoreboardhelper;

import at.happynev.mwoscoreboardhelper.tracer.ScreenshotType;
import javafx.scene.paint.Color;

/**
 * Created by Nev on 03.02.2017.
 */
public interface DisplayableStat {
    Color COLOR_PERSONALDATA = Color.NAVAJOWHITE;
    Color COLOR_MATCHDATA = Color.CORAL;
    Color COLOR_PLAYERDATA = Color.LIGHTGREEN;
    Color COLOR_PLAYERMATCHDATA = Color.CORNFLOWERBLUE;

    String getDescription();

    Color getColor();

    boolean canDisplay(ScreenshotType type);
}
