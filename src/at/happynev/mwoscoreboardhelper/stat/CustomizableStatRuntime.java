package at.happynev.mwoscoreboardhelper.stat;

import at.happynev.mwoscoreboardhelper.GuiUtils;
import at.happynev.mwoscoreboardhelper.PlayerMatchRecord;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Nev on 29.07.2017.
 */
public class CustomizableStatRuntime implements DisplayableStat {
    private final List<? extends StatPipelineStep> calculationSteps;
    private final PlayerMatchRecord currentRecord;
    private final List<StatExplanationStep> stepExplanation = new ArrayList<>();
    private final CustomizableStatTemplate template;
    private String value = "";
    private Paint overridePaint = null;

    public CustomizableStatRuntime(CustomizableStatTemplate template, List<? extends StatPipelineStep> calculationSteps, PlayerMatchRecord currentRecord) {
        this.calculationSteps = calculationSteps;
        this.currentRecord = currentRecord;
        this.template = template;
    }

    public Paint getPaint() {
        return overridePaint;
    }

    public String getValue() {
        return getValue(PlayerMatchRecord.getAllRecords().values());
    }

    public String getValue(Collection<PlayerMatchRecord> recordList) {
        List<Paint> colors = new ArrayList<>();
        if (value.isEmpty()) {
            StatCalculationWorkingSet currentWorkingSet = new StatCalculationWorkingSet(recordList, currentRecord);
            for (StatPipelineStep step : calculationSteps) {
                currentWorkingSet = step.calculateStep(currentWorkingSet);
                if (currentWorkingSet.hasOverridePaint()) {
                    this.overridePaint = currentWorkingSet.getOverridePaint();
                } else {
                    colors.add(step.getStepDescription().getPaint());
                }
            }
            String value = currentWorkingSet.getLastValue();
            stepExplanation.addAll(currentWorkingSet.getExplanation());
            if (value == null) {
                value = "null?";
            }
            this.value = value;
        }
        if (this.overridePaint == null) {
            this.overridePaint = GuiUtils.getAverageColor(colors);
            if (this.overridePaint == null) {
                this.overridePaint = new Color(40, 40, 40, 0);
            }
        }
        return value;
    }

    public CustomizableStatTemplate getTemplate() {
        return template;
    }

    @Override
    public String getShortName() {
        return template.getShortName();
    }

    @Override
    public String getLongName() {
        return template.getLongName();
    }

    @Override
    public List<StatExplanationStep> getExplanation() {
        return stepExplanation;
    }

    public String getExplanationString() {
        StringBuilder sb = new StringBuilder();
        sb.append(template.getLongName());
        for (StatExplanationStep step : stepExplanation) {
            sb.append("\n");
            sb.append(step.getDescription());
            if (!step.getResult().isEmpty()) {
                sb.append(" --> ").append(step.getResult());
            }
        }
        return sb.toString();
    }
}
