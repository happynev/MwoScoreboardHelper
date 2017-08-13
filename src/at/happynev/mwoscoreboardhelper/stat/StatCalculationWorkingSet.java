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
    private final List<String> resultValues;
    private List<String> explanation;
    private Collection<PlayerMatchRecord> records;

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
