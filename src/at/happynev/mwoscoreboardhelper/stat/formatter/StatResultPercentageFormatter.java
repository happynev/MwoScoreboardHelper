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
public class StatResultPercentageFormatter extends StatResultFormatter {
    private static final Color min = Color.RED;
    private static final Color mid = Color.LIME;
    private static final Color max = Color.CYAN;
    private final static LinearGradient gradient = new LinearGradient(0, 0, 1, 0, true, null, Arrays.asList(new Stop(0, min), new Stop(0.5, mid), new Stop(1, max)));
    private final double minValue;
    private final double midValue;
    private final double maxValue;

    public StatResultPercentageFormatter(String... parameters) {
        if (parameters.length == 3) {
            minValue = Double.parseDouble(parameters[0]);
            midValue = Double.parseDouble(parameters[1]);
            maxValue = Double.parseDouble(parameters[2]);
        } else {
            minValue = 50;
            midValue = 100;
            maxValue = 150;
        }
    }

    @Override
    public StatCalculationWorkingSet calculateStep(StatCalculationWorkingSet input) {
        int results = input.getResultValues().size();
        if (results > 0) {
            String svalue = input.getResultValues().get(results - 1);
            try {
                double percent = Double.parseDouble(svalue.replaceAll("[^\\d.]", ""));
                if (percent < midValue) {
                    input.setOverridePaint(min.interpolate(mid, percent / midValue));
                } else {
                    percent -= midValue;
                    input.setOverridePaint(mid.interpolate(max, percent / midValue));
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
        return new StatExplanationStep(gradient, "apply percentage formatting (" + minValue + "-" + midValue + "-" + maxValue + ")");
    }
}
