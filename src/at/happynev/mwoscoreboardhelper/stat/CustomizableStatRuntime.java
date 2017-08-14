package at.happynev.mwoscoreboardhelper.stat;

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

    public Paint getOverridePaint() {
        return overridePaint;
    }

    public String getValue() {
        if (value.isEmpty()) {
            Collection<PlayerMatchRecord> records = PlayerMatchRecord.getAllRecords();
            StatCalculationWorkingSet currentWorkingSet = new StatCalculationWorkingSet(records, currentRecord);
            for (StatPipelineStep step : calculationSteps) {
                currentWorkingSet = step.calculateStep(currentWorkingSet);
                if (currentWorkingSet.hasOverridePaint()) {
                    this.overridePaint = currentWorkingSet.getOverridePaint();
                }
            }
            String value = currentWorkingSet.getLastValue();
            stepExplanation.addAll(currentWorkingSet.getExplanation());
            if (value == null) {
                value = "null?";
            }
            this.value = value;
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
