package at.happynev.mwoscoreboardhelper.stat;

import javafx.scene.paint.Paint;

import java.util.List;

/**
 * Created by Nev on 03.02.2017.
 */
public interface DisplayableStat {
    String getShortName();

    String getLongName();

    List<StatExplanationStep> getExplanation();

    Paint getPaint();
}
