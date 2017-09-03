package at.happynev.mwoscoreboardhelper.stat;

import at.happynev.mwoscoreboardhelper.GuiUtils;
import at.happynev.mwoscoreboardhelper.PlayerMatchRecord;
import at.happynev.mwoscoreboardhelper.stat.filter.RecordFilterByPlayer;
import at.happynev.mwoscoreboardhelper.stat.filter.RecordFilterByTeam;
import at.happynev.mwoscoreboardhelper.stat.formatter.StatResultFormatter;
import at.happynev.mwoscoreboardhelper.tracer.ScreenshotType;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

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
        sampleRuntime = getRuntimeInstance(PlayerMatchRecord.getReferenceRecord(false, -1));
        this.shortName = shortName;
        this.longName = longName;
    }

    public CustomizableStatRuntime getSampleRuntime() {
        return sampleRuntime;
    }

    @Override
    public Paint getPaint() {
        Paint finalColor = null;
        Paint override = null;
        List<Paint> colors = new ArrayList<>();
        for (StatPipelineStep step : calculationSteps) {
            colors.add(step.getStepDescription().getPaint());
            if (step instanceof StatResultFormatter) {
                override = step.getStepDescription().getPaint();
            }
        }
        if (override != null) {
            finalColor = override;
        } else {
            finalColor = GuiUtils.getAverageColor(colors);
            if (finalColor == null) {
                finalColor = new Color(40, 40, 40, 0);
            }
        }
        return finalColor;
    }

    public List<StatPipelineStep> getCalculationSteps() {
        return calculationSteps;
    }

    public boolean canDisplay(ScreenshotType sstype, StatTable table) {
        for (StatPipelineStep step : calculationSteps) {
            if (!step.canDisplay(sstype, table)) {
                return false;
            }
        }
        if (table == StatTable.WATCHER_SIDEBAR) {
            boolean isTeamFiltered = false;
            for (StatPipelineStep step : calculationSteps) {
                if (step instanceof RecordFilterByTeam) {
                    isTeamFiltered = true;
                } else if (step instanceof RecordFilterByPlayer) {
                    return false;
                }
            }
            if (!isTeamFiltered) {
                return false;
            }
        }
        return true;
    }

    public CustomizableStatRuntime getRuntimeInstance(PlayerMatchRecord pmr) {
        CustomizableStatRuntime rt = new CustomizableStatRuntime(this, calculationSteps, pmr);
        return rt;
    }

    public String getDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append(longName).append("\n");
        for (StatPipelineStep step : calculationSteps) {
            sb.append(step.getStepDescription().getDescription()).append("\n");
        }
        return sb.toString();
    }

    @Override
    public List<StatExplanationStep> getExplanation() {
        List<StatExplanationStep> explanation = new ArrayList<>(calculationSteps.size());
        for (StatPipelineStep step : calculationSteps) {
            explanation.add(step.getStepDescription());
        }
        return explanation;
    }

    @Override
    public String getShortName() {
        return shortName;
    }

    @Override
    public String getLongName() {
        return longName;
    }
}
