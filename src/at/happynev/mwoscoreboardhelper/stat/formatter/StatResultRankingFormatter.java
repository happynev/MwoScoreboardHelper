package at.happynev.mwoscoreboardhelper.stat.formatter;

import at.happynev.mwoscoreboardhelper.stat.StatCalculationWorkingSet;
import at.happynev.mwoscoreboardhelper.stat.StatExplanationStep;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

import java.util.Arrays;

/**
 * Created by Nev on 14.08.2017.
 */
public class StatResultRankingFormatter extends StatResultFormatter {
    private static final Color min = Color.RED;
    private static final Color max = Color.LIME;
    private final static LinearGradient gradient = new LinearGradient(0, 0, 1, 0, true, null, Arrays.asList(new Stop(0, min), new Stop(1, max)));

    public StatResultRankingFormatter(String... parameters) {

    }

    @Override
    public StatCalculationWorkingSet calculateStep(StatCalculationWorkingSet input) {
        int results = input.getResultValues().size();
        if (results > 0) {
            String rank = input.getResultValues().get(results - 1);
            String spos = rank.replaceAll("^(\\d+) of (\\d+)$", "$1");
            String smax = rank.replaceAll("^(\\d+) of (\\d+)$", "$2");
            try {
                double pos = Double.parseDouble(spos);
                double limit = Double.parseDouble(smax);
                if (limit > 0.0d) {
                    double scale = pos / limit;
                    Color rankColor = max.interpolate(min, scale);
                    input.setOverridePaint(rankColor);
                } else {
                    input.setOverridePaint(gradient);
                }
            } catch (NumberFormatException e) {
                input.setOverridePaint(gradient);
            }
        } else {
            input.setOverridePaint(gradient);
        }
        input.getExplanation().add(new StatExplanationStep(getStepDescription(), ""));
        return input;
    }

    @Override
    public StatExplanationStep getStepDescription() {
        return new StatExplanationStep(gradient, "apply rank formatting");
    }
}
