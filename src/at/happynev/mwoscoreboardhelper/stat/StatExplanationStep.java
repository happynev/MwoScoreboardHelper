package at.happynev.mwoscoreboardhelper.stat;

import javafx.scene.paint.Paint;

/**
 * Created by Nev on 14.08.2017.
 */
public class StatExplanationStep {
    private final Paint paint;
    private final String description;
    private final String result;

    public StatExplanationStep(Paint paint, String description) {
        this.paint = paint;
        this.description = description;
        this.result = "";
    }

    public StatExplanationStep(StatExplanationStep step, String result) {
        this.paint = step.getPaint();
        this.description = step.getDescription();
        this.result = result;
    }


    public Paint getPaint() {
        return paint;
    }

    public String getDescription() {
        return description;
    }

    public String getResult() {
        return result;
    }
}
