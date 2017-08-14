package at.happynev.mwoscoreboardhelper.stat;

import at.happynev.mwoscoreboardhelper.PlayerMatchRecord;
import javafx.scene.paint.Paint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created by Nev on 06.08.2017.
 */
public class StatCalculationWorkingSet {
    private final PlayerMatchRecord reference;
    private final List<String> resultValues;
    private List<StatExplanationStep> explanation;
    private Collection<PlayerMatchRecord> records;
    private Paint overridePaint = null;

    public StatCalculationWorkingSet(Collection<PlayerMatchRecord> records, PlayerMatchRecord reference) {
        this.records = records;
        this.reference = reference;
        this.resultValues = new ArrayList<>();
        explanation = new ArrayList<>();
    }

    public StatCalculationWorkingSet(StatCalculationWorkingSet previous) {
        this.records = previous.getRecords();
        this.reference = previous.getReference();
        this.resultValues = previous.getResultValues();
        this.explanation = previous.getExplanation();
    }

    public Paint getOverridePaint() {
        return overridePaint;
    }

    public void setOverridePaint(Paint overridePaint) {
        this.overridePaint = overridePaint;
    }

    public boolean hasOverridePaint() {
        return overridePaint != null;
    }

    public void addStepExplanation(StatExplanationStep exp) {
        explanation.add(exp);
    }

    public List<StatExplanationStep> getExplanation() {
        return explanation;
    }

    public Collection<PlayerMatchRecord> getRecords() {
        return records;
    }

    public void setRecords(Set<PlayerMatchRecord> records) {
        this.records = records;
    }

    public List<String> getResultValues() {
        return resultValues;
    }

    public String getLastValue() {
        if (resultValues.isEmpty()) {
            return "";
        } else {
            return resultValues.get(resultValues.size() - 1);
        }
    }

    public PlayerMatchRecord getReference() {
        return reference;
    }
}
