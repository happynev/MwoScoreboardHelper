package at.happynev.mwoscoreboardhelper.stat;

import at.happynev.mwoscoreboardhelper.stat.aggregator.StatAggregatorType;
import at.happynev.mwoscoreboardhelper.stat.calculator.StatCalculatorType;
import at.happynev.mwoscoreboardhelper.stat.filter.RecordFilterType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nev on 06.08.2017.
 */
public class StatBuilder {
    private static final List<CustomizableStatTemplate> defaultStats;

    static {
        defaultStats = new ArrayList<>();
        defaultStats.add(StatBuilder.newStat("#", "Number of records collected for a player")
                .addCalculationStep(RecordFilterType.PLAYER.getInstance())
                .addCalculationStep(StatAggregatorType.COUNT.getInstance(StatType.MATCHES))
                .build());
        defaultStats.add(StatBuilder.newStat("#C", "Number of records collected for a player in this weightclass")
                .addCalculationStep(RecordFilterType.PLAYER.getInstance())
                .addCalculationStep(RecordFilterType.MECHCLASS.getInstance())
                .addCalculationStep(StatAggregatorType.COUNT.getInstance(StatType.MATCHES))
                .build());
        for (StatType type : StatType.values()) {
            defaultStats.add(StatBuilder.newStat(type.toString(), type.getDescription())
                    .addCalculationStep(StatCalculatorType.RAWVALUE.getInstance(type))
                    .build());
        }
        defaultStats.add(StatBuilder.newStat("~S", "Score average for this player")
                .addCalculationStep(RecordFilterType.PLAYER.getInstance())
                .addCalculationStep(StatAggregatorType.AVERAGE.getInstance(StatType.SCORE))
                .build());
        defaultStats.add(StatBuilder.newStat("~D", "Damage average for this player")
                .addCalculationStep(RecordFilterType.PLAYER.getInstance())
                .addCalculationStep(StatAggregatorType.AVERAGE.getInstance(StatType.DAMAGE))
                .build());
        defaultStats.add(StatBuilder.newStat("~K", "Kills average for this player")
                .addCalculationStep(RecordFilterType.PLAYER.getInstance())
                .addCalculationStep(StatAggregatorType.AVERAGE.getInstance(StatType.SCORE))
                .build());
        defaultStats.add(StatBuilder.newStat("*TSc", "Team Rank by score")
                .addCalculationStep(RecordFilterType.MATCH.getInstance())
                .addCalculationStep(RecordFilterType.TEAM.getInstance())
                .addCalculationStep(StatCalculatorType.RANKING.getInstance(StatType.SCORE))
                .build());
        defaultStats.add(StatBuilder.newStat("%Sc", "Score relative to average for this player")
                .addCalculationStep(RecordFilterType.PLAYER.getInstance())
                .addCalculationStep(StatAggregatorType.AVERAGE.getInstance(StatType.SCORE))
                .addCalculationStep(StatCalculatorType.RELATIVE.getInstance(StatType.SCORE))
                .build());
        defaultStats.add(StatBuilder.newStat("%MSc", "Score relative to average for this player's mech")
                .addCalculationStep(RecordFilterType.PLAYER.getInstance())
                .addCalculationStep(RecordFilterType.MECHVARIANT.getInstance())
                .addCalculationStep(StatAggregatorType.AVERAGE.getInstance(StatType.SCORE))
                .addCalculationStep(StatCalculatorType.RELATIVE.getInstance(StatType.SCORE))
                .build());
        defaultStats.add(StatBuilder.newStat("%CSc", "Score relative to average for this player's mech's weightclass")
                .addCalculationStep(RecordFilterType.PLAYER.getInstance())
                .addCalculationStep(RecordFilterType.MECHCLASS.getInstance())
                .addCalculationStep(StatAggregatorType.AVERAGE.getInstance(StatType.SCORE))
                .addCalculationStep(StatCalculatorType.RELATIVE.getInstance(StatType.SCORE))
                .build());
        defaultStats.add(StatBuilder.newStat("%fSc", "Score relative to the average of this mech's faction in this match")
                .addCalculationStep(RecordFilterType.MATCH.getInstance())
                .addCalculationStep(RecordFilterType.MECHFACTION.getInstance())
                .addCalculationStep(StatAggregatorType.AVERAGE.getInstance(StatType.DAMAGE))
                .addCalculationStep(StatCalculatorType.RELATIVE.getInstance(StatType.DAMAGE))
                .build());
        defaultStats.add(StatBuilder.newStat("Classes", "Best performing mech classes for this player, sorted by average score")
                .addCalculationStep(RecordFilterType.PLAYER.getInstance())
                .addCalculationStep(StatCalculatorType.TOPLISTCLASS.getInstance(StatType.SCORE))
                .build());
        defaultStats.add(StatBuilder.newStat("Mechs", "Best performing mechs for a player, sorted by average score")
                .addCalculationStep(RecordFilterType.PLAYER.getInstance())
                .addCalculationStep(StatCalculatorType.TOPLISTMECH.getInstance(StatType.SCORE))
                .build());
    }

    public static List<CustomizableStatTemplate> getDefaultStats() {
        return defaultStats;
    }

    public static StatBuildingStep newStat(String shortName, String longName) {
        return new StatBuildingStep(new CustomizableStatTemplate(shortName, longName));
    }

    public static class StatBuildingStep {
        private final CustomizableStatTemplate currentTemplate;

        public StatBuildingStep(CustomizableStatTemplate currentTemplate) {
            this.currentTemplate = currentTemplate;
        }

        public StatBuildingStep addCalculationStep(StatPipelineStep step) {
            currentTemplate.getCalculationSteps().add(step);
            return this;
        }

        public CustomizableStatTemplate build() {
            return currentTemplate;
        }
    }
}
