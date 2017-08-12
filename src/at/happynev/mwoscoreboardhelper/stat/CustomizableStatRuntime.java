package at.happynev.mwoscoreboardhelper.stat;

import at.happynev.mwoscoreboardhelper.PlayerMatchRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by Nev on 29.07.2017.
 */
public class CustomizableStatRuntime {
    private final List<? extends StatPipelineStep> calculationSteps;
    private final PlayerMatchRecord currentRecord;
    private final List<String> stepExplanation = new ArrayList<>();
    private String value = "";

    public CustomizableStatRuntime(List<? extends StatPipelineStep> calculationSteps, PlayerMatchRecord currentRecord) {
        this.calculationSteps = calculationSteps;
        this.currentRecord = currentRecord;
    }

    public List<? extends StatPipelineStep> getCalculationSteps() {
        return calculationSteps;
    }

    public String getValue() {
        if (value.isEmpty()) {
            Set<PlayerMatchRecord> records = PlayerMatchRecord.getAllRecords();
            StatCalculationWorkingSet currentWorkingSet = new StatCalculationWorkingSet(records, currentRecord);
            for (StatPipelineStep step : calculationSteps) {
                currentWorkingSet = step.calculateStep(currentWorkingSet);
            }
            String value = currentWorkingSet.getCurrentValue();
            stepExplanation.addAll(currentWorkingSet.getExplanation());
            if (value == null) {
                value = "null?";
            }
            this.value = value;
        }
        return value;
    }

    public String getExplanation() {
        StringBuilder sb = new StringBuilder();
        for (String s : stepExplanation) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(s);
        }
        return sb.toString();
    }
}
