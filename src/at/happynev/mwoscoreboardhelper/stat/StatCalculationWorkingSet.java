package at.happynev.mwoscoreboardhelper.stat;

import at.happynev.mwoscoreboardhelper.PlayerMatchRecord;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created by Nev on 06.08.2017.
 */
public class StatCalculationWorkingSet {
    private final PlayerMatchRecord reference;
    private List<String> explanation;
    private Collection<PlayerMatchRecord> records;
    private String currentValue;

    public StatCalculationWorkingSet(Collection<PlayerMatchRecord> records, PlayerMatchRecord reference) {
        this.records = records;
        this.reference = reference;
        this.currentValue = "";
        explanation = new ArrayList<>();
    }

    public StatCalculationWorkingSet(StatCalculationWorkingSet previous) {
        this.records = previous.getRecords();
        this.reference = previous.getReference();
        this.currentValue = previous.getCurrentValue();
        this.explanation = previous.getExplanation();
    }

    public void addStepExplanation(String exp) {
        explanation.add(exp);
    }

    public List<String> getExplanation() {
        return explanation;
    }

    public Collection<PlayerMatchRecord> getRecords() {
        return records;
    }

    public void setRecords(Set<PlayerMatchRecord> records) {
        this.records = records;
    }

    public String getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(String currentValue) {
        this.currentValue = currentValue;
    }

    public PlayerMatchRecord getReference() {
        return reference;
    }
}
