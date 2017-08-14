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
public class StatResultStatusFormatter extends StatResultFormatter {
    private static final Color min = Color.RED;
    private static final Color max = Color.LIME;
    private final static LinearGradient gradient = new LinearGradient(0, 0, 1, 0, true, null, Arrays.asList(new Stop(0, min), new Stop(0.15, min), new Stop(0.85, max), new Stop(1, max)));

    public StatResultStatusFormatter(String... parameters) {

    }

    @Override
    public StatCalculationWorkingSet calculateStep(StatCalculationWorkingSet input) {
        int results = input.getResultValues().size();
        if (results > 0) {
            String lastStatus = (input.getResultValues().get(results - 1));
            if (lastStatus.equals("ALIVE")) {
                input.setOverridePaint(max);
            } else if (lastStatus.equals("DEAD")) {
                input.setOverridePaint(min);
            } else {
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
        return new StatExplanationStep(gradient, "apply status formatting");
    }
}
