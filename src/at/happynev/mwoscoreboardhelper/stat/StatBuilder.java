package at.happynev.mwoscoreboardhelper.stat;

import at.happynev.mwoscoreboardhelper.stat.aggregator.StatAggregatorType;
import at.happynev.mwoscoreboardhelper.stat.calculator.*;
import at.happynev.mwoscoreboardhelper.stat.filter.RecordFilterType;
import at.happynev.mwoscoreboardhelper.stat.formatter.StatResultFormatterType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Nev on 06.08.2017.
 */
public class StatBuilder {
    private static final List<CustomizableStatTemplate> defaultStats;
    private static final List<CustomizableStatTemplate> defaultPlayerTabMechStats;

    static {
        defaultStats = new ArrayList<>();
        defaultStats.add(StatBuilder.newStat("#", "Number of records collected for a player")
                .addCalculationStep(RecordFilterType.PLAYER.getInstance())
                .addCalculationStep(StatAggregatorType.COUNT.getInstance(StatType.MATCHES))
                .build());
        defaultStats.add(StatBuilder.newStat("#Today", "Number of records collected for a player in the last 24h")
                .addCalculationStep(RecordFilterType.PLAYER.getInstance())
                .addCalculationStep(RecordFilterType.MATCHRECENT.getInstance("1"))
                .addCalculationStep(StatAggregatorType.COUNT.getInstance(StatType.MATCHES))
                .build());
        defaultStats.add(StatBuilder.newStat("#MechV", "Number of records collected for a player in this mech variant")
                .addCalculationStep(RecordFilterType.PLAYER.getInstance())
                .addCalculationStep(RecordFilterType.MECHVARIANT.getInstance())
                .addCalculationStep(StatAggregatorType.COUNT.getInstance(StatType.MATCHES))
                .build());
        defaultStats.add(StatBuilder.newStat("#MechC", "Number of records collected for a player in this mech chassis")
                .addCalculationStep(RecordFilterType.PLAYER.getInstance())
                .addCalculationStep(RecordFilterType.MECHCHASSIS.getInstance())
                .addCalculationStep(StatAggregatorType.COUNT.getInstance(StatType.MATCHES))
                .build());
        defaultStats.add(StatBuilder.newStat("#Class", "Number of records collected for a player in this weightclass")
                .addCalculationStep(RecordFilterType.PLAYER.getInstance())
                .addCalculationStep(RecordFilterType.MECHCLASS.getInstance())
                .addCalculationStep(StatAggregatorType.COUNT.getInstance(StatType.MATCHES))
                .build());
        defaultStats.add(StatBuilder.newStat("#Win", "Number of wins for this player")
                .addCalculationStep(RecordFilterType.PLAYER.getInstance())
                .addCalculationStep(StatAggregatorType.COUNT.getInstance(StatType.WINS))
                .build());
        defaultStats.add(StatBuilder.newStat("#Loss", "Number of losses for this player")
                .addCalculationStep(RecordFilterType.PLAYER.getInstance())
                .addCalculationStep(StatAggregatorType.COUNT.getInstance(StatType.LOSSES))
                .build());
        defaultStats.add(StatBuilder.newStat("W/L", "Win/Loss Ratio for this player")
                .addCalculationStep(RecordFilterType.PLAYER.getInstance())
                .addCalculationStep(StatAggregatorType.COUNT.getInstance(StatType.LOSSES))
                .addCalculationStep(StatAggregatorType.COUNT.getInstance(StatType.WINS))
                .addCalculationStep(AggregatedStatCalculatorType.RATIO.getInstance())
                .build());
        defaultStats.add(StatBuilder.newStat("MW/L", "Win/Loss Ratio with this mech")
                .addCalculationStep(RecordFilterType.PLAYER.getInstance())
                .addCalculationStep(RecordFilterType.MECHVARIANT.getInstance())
                .addCalculationStep(StatAggregatorType.COUNT.getInstance(StatType.LOSSES))
                .addCalculationStep(StatAggregatorType.COUNT.getInstance(StatType.WINS))
                .addCalculationStep(AggregatedStatCalculatorType.RATIO.getInstance())
                .build());
        defaultStats.add(StatBuilder.newStat("%Mech", "Relative mech performance vs player average")
                .addCalculationStep(RecordFilterType.PLAYER.getInstance())
                .addCalculationStep(StatAggregatorType.AVERAGE.getInstance(StatType.SCORE))
                .addCalculationStep(RecordFilterType.RESET.getInstance())
                .addCalculationStep(RecordFilterType.PLAYER.getInstance())
                .addCalculationStep(RecordFilterType.MECHVARIANT.getInstance())
                .addCalculationStep(StatAggregatorType.AVERAGE.getInstance(StatType.SCORE))
                .addCalculationStep(AggregatedStatCalculatorType.RELATIVE.getInstance())
                .build());

        for (StatType stat : StatType.values()) {
            StatBuilder.StatBuildingStep tmpStat = StatBuilder.newStat(stat.toString(), stat.getDescription())
                    .addCalculationStep(MatchStatCalculatorType.RAWVALUE.getInstance(stat));
            if (stat == StatType.STATUS) {
                tmpStat = tmpStat.addCalculationStep(StatResultFormatterType.STATUS.getInstance());
            }
            CustomizableStatTemplate statTemplate = tmpStat.build();
            defaultStats.add(statTemplate);
        }
        defaultStats.add(StatBuilder.newStat("Rank", "Overall Rank according to the Jarl's list")
                .addCalculationStep(RecordFilterType.PLAYER.getInstance())
                .addCalculationStep(new StatCalculatorLeaderboardRank())
                .build());
        defaultStats.add(StatBuilder.newStat("Adj.Score", "Previous season's Adjusted Score according to the Jarl's list")
                .addCalculationStep(RecordFilterType.PLAYER.getInstance())
                .addCalculationStep(new StatCalculatorLeaderboardScore())
                .build());
        for (StatType stat : Arrays.asList(StatType.ASSISTS, StatType.KILLS, StatType.DAMAGE, StatType.SCORE, StatType.PING, StatType.SOLO_KILLS, StatType.KMDDS, StatType.COMPONENT_DESTROYED, StatType.REWARD_CBILLS, StatType.REWARD_XP))
            defaultStats.add(StatBuilder.newStat("~" + stat.toString(), stat.getDescription() + " average for this player")
                    .addCalculationStep(RecordFilterType.PLAYER.getInstance())
                    .addCalculationStep(StatAggregatorType.AVERAGE.getInstance(stat))
                    .build());
        defaultStats.add(StatBuilder.newStat("~CB/MGC", "Average C-Bill rewards for this gamemode on this map with this weightclass")
                .addCalculationStep(RecordFilterType.PLAYER.getInstance())
                .addCalculationStep(RecordFilterType.GAMEMODE.getInstance())
                .addCalculationStep(RecordFilterType.MAP.getInstance())
                .addCalculationStep(RecordFilterType.MECHCLASS.getInstance())
                .addCalculationStep(StatAggregatorType.AVERAGE.getInstance(StatType.REWARD_CBILLS))
                .build());
        defaultStats.add(StatBuilder.newStat("%CB/GM", "Relative C-Bill rewards for this gamemode")
                .addCalculationStep(RecordFilterType.PLAYER.getInstance())
                .addCalculationStep(RecordFilterType.GAMEMODE.getInstance())
                .addCalculationStep(StatAggregatorType.AVERAGE.getInstance(StatType.REWARD_CBILLS))
                .addCalculationStep(MatchStatCalculatorType.RAWVALUE.getInstance(StatType.REWARD_CBILLS))
                .addCalculationStep(AggregatedStatCalculatorType.RELATIVE.getInstance())
                .addCalculationStep(StatResultFormatterType.PERCENT.getInstance())
                .build());
        defaultStats.add(StatBuilder.newStat("*TScore", "Team Rank by score")
                .addCalculationStep(RecordFilterType.MATCH.getInstance())
                .addCalculationStep(RecordFilterType.TEAM.getInstance())
                .addCalculationStep(MatchStatCalculatorType.RANKING.getInstance(StatType.SCORE))
                .addCalculationStep(StatResultFormatterType.RANKING.getInstance())
                .build());
        defaultStats.add(StatBuilder.newStat("%Score", "Score relative to average for this player")
                .addCalculationStep(RecordFilterType.PLAYER.getInstance())
                .addCalculationStep(StatAggregatorType.AVERAGE.getInstance(StatType.SCORE))
                .addCalculationStep(MatchStatCalculatorType.RAWVALUE.getInstance(StatType.SCORE))
                .addCalculationStep(AggregatedStatCalculatorType.RELATIVE.getInstance())
                .addCalculationStep(StatResultFormatterType.PERCENT.getInstance())
                .build());
        defaultStats.add(StatBuilder.newStat("%MScore", "Score relative to average for this player's mech")
                .addCalculationStep(RecordFilterType.PLAYER.getInstance())
                .addCalculationStep(RecordFilterType.MECHVARIANT.getInstance())
                .addCalculationStep(StatAggregatorType.AVERAGE.getInstance(StatType.SCORE))
                .addCalculationStep(MatchStatCalculatorType.RAWVALUE.getInstance(StatType.SCORE))
                .addCalculationStep(AggregatedStatCalculatorType.RELATIVE.getInstance())
                .addCalculationStep(StatResultFormatterType.PERCENT.getInstance())
                .build());
        defaultStats.add(StatBuilder.newStat("%MScoreG", "Score relative to global average of this mech")
                .addCalculationStep(RecordFilterType.MECHVARIANT.getInstance())
                .addCalculationStep(StatAggregatorType.AVERAGE.getInstance(StatType.SCORE))
                .addCalculationStep(MatchStatCalculatorType.RAWVALUE.getInstance(StatType.SCORE))
                .addCalculationStep(AggregatedStatCalculatorType.RELATIVE.getInstance())
                .addCalculationStep(StatResultFormatterType.PERCENT.getInstance())
                .build());
        defaultStats.add(StatBuilder.newStat("%CScore", "Score relative to average for this player's mech's weightclass")
                .addCalculationStep(RecordFilterType.PLAYER.getInstance())
                .addCalculationStep(RecordFilterType.MECHCLASS.getInstance())
                .addCalculationStep(StatAggregatorType.AVERAGE.getInstance(StatType.SCORE))
                .addCalculationStep(MatchStatCalculatorType.RAWVALUE.getInstance(StatType.SCORE))
                .addCalculationStep(AggregatedStatCalculatorType.RELATIVE.getInstance())
                .addCalculationStep(StatResultFormatterType.PERCENT.getInstance())
                .build());
        defaultStats.add(StatBuilder.newStat("Rel.Score", "MatchScore relative to previous season's Adjusted Score according to the Jarl's list")
                .addCalculationStep(RecordFilterType.MATCH.getInstance())
                .addCalculationStep(RecordFilterType.PLAYER.getInstance())
                .addCalculationStep(new StatCalculatorLeaderboardScore())
                .addCalculationStep(MatchStatCalculatorType.RAWVALUE.getInstance(StatType.SCORE))
                .addCalculationStep(AggregatedStatCalculatorType.RELATIVE.getInstance())
                .addCalculationStep(StatResultFormatterType.PERCENT.getInstance())
                .build());
        defaultStats.add(StatBuilder.newStat("S/D", "Score to Damage Ratio for this player")
                .addCalculationStep(RecordFilterType.PLAYER.getInstance())
                .addCalculationStep(RecordFilterType.MATCH.getInstance())
                .addCalculationStep(MatchStatCalculatorType.RAWVALUE.getInstance(StatType.DAMAGE))
                .addCalculationStep(MatchStatCalculatorType.RAWVALUE.getInstance(StatType.SCORE))
                .addCalculationStep(AggregatedStatCalculatorType.RATIO.getInstance())
                .build());
        defaultStats.add(StatBuilder.newStat("%Dam", "Percentage of damage in the team")
                .addCalculationStep(RecordFilterType.MATCH.getInstance())
                .addCalculationStep(RecordFilterType.TEAM.getInstance())
                .addCalculationStep(StatAggregatorType.SUM.getInstance(StatType.DAMAGE))
                .addCalculationStep(MatchStatCalculatorType.RAWVALUE.getInstance(StatType.DAMAGE))
                .addCalculationStep(AggregatedStatCalculatorType.RELATIVE.getInstance())
                .addCalculationStep(StatResultFormatterType.PERCENT.getInstance("0", "10", "20"))
                .build());
        defaultStats.add(StatBuilder.newStat("UD", "Underdog factor")
                .addCalculationStep(RecordFilterType.MATCHOLD.getInstance("30"))
                .addCalculationStep(RecordFilterType.MECHVARIANT.getInstance())
                .addCalculationStep(RecordFilterType.NOTSELF.getInstance())
                .addCalculationStep(StatAggregatorType.COUNT.getInstance(StatType.MATCHES))
                .addCalculationStep(RecordFilterType.RESET.getInstance())
                .addCalculationStep(RecordFilterType.MATCHOLD.getInstance("30"))
                .addCalculationStep(RecordFilterType.NOTSELF.getInstance())
                .addCalculationStep(StatAggregatorType.COUNT.getInstance(StatType.MATCHES))
                .addCalculationStep(AggregatedStatCalculatorType.RATIO.getInstance())
                .build());
        defaultStats.add(StatBuilder.newStat("New", "Ratio of mechs prevalent mechs this week vs previous week")
                .addCalculationStep(RecordFilterType.MECHVARIANT.getInstance())
                .addCalculationStep(RecordFilterType.MATCHRECENT.getInstance("14"))
                .addCalculationStep(RecordFilterType.MATCHOLD.getInstance("7"))
                .addCalculationStep(RecordFilterType.NOTSELF.getInstance())
                .addCalculationStep(StatAggregatorType.COUNT.getInstance(StatType.MATCHES))
                .addCalculationStep(RecordFilterType.RESET.getInstance())
                .addCalculationStep(RecordFilterType.MECHVARIANT.getInstance())
                .addCalculationStep(RecordFilterType.MATCHRECENT.getInstance("7"))
                .addCalculationStep(RecordFilterType.NOTSELF.getInstance())
                .addCalculationStep(StatAggregatorType.COUNT.getInstance(StatType.MATCHES))
                .addCalculationStep(AggregatedStatCalculatorType.RATIO.getInstance())
                .build());
        defaultStats.add(StatBuilder.newStat("Classes", "Best performing mech classes for this player, sorted by average score")
                .addCalculationStep(RecordFilterType.PLAYER.getInstance())
                .addCalculationStep(StatAggregatorType.TOPLISTCLASS.getInstance(StatType.SCORE))
                .build());
        defaultStats.add(StatBuilder.newStat("Mechs", "Best performing mechs for a player, sorted by average score")
                .addCalculationStep(RecordFilterType.PLAYER.getInstance())
                .addCalculationStep(StatAggregatorType.TOPLISTMECH.getInstance(StatType.SCORE))
                .build());
        defaultStats.add(StatBuilder.newStat("Class Prob%", "Mech Class probability according to the Jarl's list")
                .addCalculationStep(RecordFilterType.PLAYER.getInstance())
                .addCalculationStep(new StatCalculatorLeaderboardMechClass())
                .build());
        defaultStats.add(StatBuilder.newStat("Total Score", "Total Score per team")
                .addCalculationStep(RecordFilterType.MATCH.getInstance())
                .addCalculationStep(RecordFilterType.TEAM.getInstance())
                .addCalculationStep(StatAggregatorType.SUM.getInstance(StatType.SCORE))
                .build());
        defaultStats.add(StatBuilder.newStat("Total Damage", "Total Damage per team")
                .addCalculationStep(RecordFilterType.MATCH.getInstance())
                .addCalculationStep(RecordFilterType.TEAM.getInstance())
                .addCalculationStep(StatAggregatorType.SUM.getInstance(StatType.DAMAGE))
                .build());
        defaultStats.add(StatBuilder.newStat("Total Weight", "Total Weight per team")
                .addCalculationStep(RecordFilterType.MATCH.getInstance())
                .addCalculationStep(RecordFilterType.TEAM.getInstance())
                .addCalculationStep(StatAggregatorType.SUM.getInstance(StatType.MECH_TONS))
                .build());
        defaultStats.add(StatBuilder.newStat("#Lights", "Number of light mechs per team")
                .addCalculationStep(RecordFilterType.MATCH.getInstance())
                .addCalculationStep(RecordFilterType.TEAM.getInstance())
                .addCalculationStep(RecordFilterType.MECHCLASS.getInstance("Light"))
                .addCalculationStep(StatAggregatorType.COUNT.getInstance(StatType.MECH_CLASS))
                .build());
        defaultStats.add(StatBuilder.newStat("#Mediums", "Number of medium mechs per team")
                .addCalculationStep(RecordFilterType.MATCH.getInstance())
                .addCalculationStep(RecordFilterType.TEAM.getInstance())
                .addCalculationStep(RecordFilterType.MECHCLASS.getInstance("Medium"))
                .addCalculationStep(StatAggregatorType.COUNT.getInstance(StatType.MECH_CLASS))
                .build());
        defaultStats.add(StatBuilder.newStat("#Heavies", "Number of heavy mechs per team")
                .addCalculationStep(RecordFilterType.MATCH.getInstance())
                .addCalculationStep(RecordFilterType.TEAM.getInstance())
                .addCalculationStep(RecordFilterType.MECHCLASS.getInstance("Heavy"))
                .addCalculationStep(StatAggregatorType.COUNT.getInstance(StatType.MECH_CLASS))
                .build());
        defaultStats.add(StatBuilder.newStat("#Assaults", "Number of assault mechs per team")
                .addCalculationStep(RecordFilterType.MATCH.getInstance())
                .addCalculationStep(RecordFilterType.TEAM.getInstance())
                .addCalculationStep(RecordFilterType.MECHCLASS.getInstance("Assault"))
                .addCalculationStep(StatAggregatorType.COUNT.getInstance(StatType.MECH_CLASS))
                .build());
        defaultPlayerTabMechStats = new ArrayList<>();
        defaultPlayerTabMechStats.add(StatBuilder.newStat("#", "Number of records collected with this mech")
                .addCalculationStep(RecordFilterType.PLAYER.getInstance())
                .addCalculationStep(RecordFilterType.MECHVARIANT.getInstance())
                .addCalculationStep(StatAggregatorType.COUNT.getInstance(StatType.MATCHES))
                .build());
        for (StatType stat : Arrays.asList(StatType.ASSISTS, StatType.KILLS, StatType.DAMAGE, StatType.SCORE, StatType.SOLO_KILLS))
            defaultPlayerTabMechStats.add(StatBuilder.newStat("~" + stat.toString(), stat.getDescription() + " average in this mech")
                    .addCalculationStep(RecordFilterType.PLAYER.getInstance())
                    .addCalculationStep(RecordFilterType.MECHVARIANT.getInstance())
                    .addCalculationStep(StatAggregatorType.AVERAGE.getInstance(stat))
                    .build());
    }

    public static List<CustomizableStatTemplate> getDefaultPlayerTabMechStats() {
        return defaultPlayerTabMechStats;
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
