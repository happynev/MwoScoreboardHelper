package at.happynev.mwoscoreboardhelper.stat;

import at.happynev.mwoscoreboardhelper.PlayerMatchRecord;
import at.happynev.mwoscoreboardhelper.SettingsTabController;
import at.happynev.mwoscoreboardhelper.tracer.ScreenshotType;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nev on 29.07.2017.
 */
public class CustomizableStatTemplate implements DisplayableStat {

    private final CustomizableStatRuntime sampleRuntime;
    private final List<StatPipelineStep> calculationSteps = new ArrayList<>();
    private final String shortName;
    private final String longName;

    public CustomizableStatTemplate(String shortName, String longName) {
        sampleRuntime = getRuntimeInstance(PlayerMatchRecord.getReferenceRecord(false));
        this.shortName = shortName;
        this.longName = longName;
    }

    public List<StatPipelineStep> getCalculationSteps() {
        return calculationSteps;
    }

    public boolean canDisplay(ScreenshotType sstype, StatTable table) {
        return SettingsTabController.getInstance().getShouldDisplay(sstype, this, table);
    }

    public CustomizableStatRuntime getRuntimeInstance(PlayerMatchRecord pmr) {
        CustomizableStatRuntime rt = new CustomizableStatRuntime(calculationSteps, pmr);
        return rt;
    }

    public String getDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append(longName).append("\n");
        for (StatPipelineStep step : calculationSteps) {
            sb.append(step.getStepDescription()).append("\n");
        }
        return sb.toString();
    }

    @Override
    public Color getColor() {
        return Color.BURLYWOOD;
    }

    public String getShortName() {
        return shortName;
    }

    public String getLongName() {
        return longName;
    }
}
