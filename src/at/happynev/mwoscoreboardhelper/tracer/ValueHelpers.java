package at.happynev.mwoscoreboardhelper.tracer;

import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by Nev on 27.01.2017.
 */
public class ValueHelpers {
    private static final Map<String, String> mapAmbiguousChars = new HashMap<>();
    private static final String regexMetaChars = "([.*+?()\\[\\]|^${}\\\\])";

    static {
        mapAmbiguousChars.put("[TlI1tJ]", "[TlI1tJ]");
        mapAmbiguousChars.put("[2Z7z]", "[2Z7z]");
        mapAmbiguousChars.put("[G68B3E]", "[G68B3E]");
        mapAmbiguousChars.put("[QDCO0og9U]", "[QDCO0og9U]");
        mapAmbiguousChars.put("[yv]", "[yv]");
        mapAmbiguousChars.put("[HR]", "[HR]");
        mapAmbiguousChars.put("[S5]", "[S5]");
        //mapAmbiguousChars.put("G\\)", "\\(I\\)");
        //mapAmbiguousChars.put("W", "(?:W|VV)");
    }

    public static String guessValue(String input, Collection<String> possibleValues) {
        String bestMatch = "";
        int bestSimilarity = Integer.MAX_VALUE;
        List<String> similar = findSimilarStrings(input, possibleValues, (input.length() + 1) / 2);
        List<String> similarLooking = findSimilarLookingStrings(input, similar);
        if (similarLooking.isEmpty()) {
            //Logger.log("found nothing similar looking to " + input);
            for (String possible : similar) {
                int score = StringUtils.getLevenshteinDistance(input, possible);
                if (score < bestSimilarity) {
                    bestSimilarity = score;
                    bestMatch = possible;
                }
            }
        } else {
            for (String possible : similarLooking) {
                int score = StringUtils.getLevenshteinDistance(input, possible);
                if (score < bestSimilarity) {
                    bestSimilarity = score;
                    bestMatch = possible;
                }
            }
        }
        return bestMatch;
    }

    public static List<String> findSimilarStrings(String input, Collection<String> possibleValues, int cutoff) {
        List<String> ret = new ArrayList<>();
        for (String possible : possibleValues) {
            int score = StringUtils.getLevenshteinDistance(input, possible);
            if (score <= cutoff) {
                ret.add(possible);
            }
        }
        return ret;
    }

    public static List<String> findSimilarLookingStrings(String input, Collection<String> possibleValues) {
        Pattern regex = Pattern.compile(".?" + makeSimilarityRegex(input) + ".?");
        TreeSet<String> s = new TreeSet(possibleValues);
        List<String> ret = s.stream().filter(p -> {
            boolean match = regex.matcher(p).matches();
            return match;
        }).collect(Collectors.toList());
        return ret;
    }

    private static String makeSimilarityRegex(String input) {
        String ret = input.replaceAll(regexMetaChars, ".");
        //if (!input.equals(ret)) Logger.log("regex " + input + "-->" + ret);
        for (String p : mapAmbiguousChars.keySet()) {
            ret = ret.replaceAll(p, mapAmbiguousChars.get(p));
        }
        return ret;
    }

    public enum ValueList {
        MAP,
        GAMEMODE,
        MATCHRESULT,
        MATCHPERFORMANCE;

        public List<String> getItems() {
            switch (this) {
                case MAP:
                    return Arrays.asList("", "ALPINE PEAKS", "CANYON NETWORK", "CAUSTIC VALLEY", "CRIMSON STRAIT", "FOREST COLONY", "FROZEN CITY",
                            "GRIM PLEXUS", "HPG MANIFOLD", "POLAR HIGHLANDS", "RIVER CITY", "TERRA THERMA", "THE MINING COLLECTIVE", "TOURMALINE DESERT",
                            "VIRIDIAN BOG", "RUBELLITE OASIS", "SOLARIS CITY", "SOLARIS CITY","FROZEN CITY NIGHT (CLASSIC)","FOREST COLONY SNOW (CLASSIC)",
                            "HIBERNAL RIFT");
                case GAMEMODE:
                    List<String> ret = Arrays.asList("", "SKIRMISH", "DOMINATION", "ASSAULT", "CONQUEST", "INCURSION", "INVASION", "SIEGE", "ESCORT");
                    List<String> gameModeRet = new ArrayList<>(ret.size());
                    ret.forEach(s -> gameModeRet.add("GAMEMODE: " + s)); //some modes with GAMEMODE:, some without
                    gameModeRet.addAll(ret);
                    return gameModeRet;
                case MATCHRESULT:
                    return Arrays.asList("", "VICTORY", "DEFEAT", "MISSION RESULT - TIE", "DEFEAT - OBJECTIVE FAILED", "VICTORY - OBJECTIVE SUCCEEDED", "DEFEAT - ENEMY GATHERED MAX RESOURCES", "VICTORY - GATHERED MAXIMUM RESOURCES");
                case MATCHPERFORMANCE:
                    return Arrays.asList("", "KILLING BLOW", "KILL ASSIST", "SOLO KILL", "KILL MOST DAMAGE DEALT", "DAMAGE DONE",
                            "COMPONENT DESTROYED", "SCOUTING", "BRAWLING", "HIT AND RUN", "FLANKING", "SAVIOR KILL", "TEAM DAMAGE",
                            "PROTECTION PROXIMITY", "PROTECTED LIGHT", "PROTECTED MEDIUM", "PROTECTED HEAVY", "PROTECTED ASSAULT",
                            "SPOTTING ASSIST", "TAG DAMAGE", "NARC KILL", "TAG KILL", "STEALTH TAG", "FIRST CAPTURE", "TEAM KILL",
                            "LANCE IN FORMATION", "DEFENSIVE KILL", "UAV KILL", "UAV LOCKED DAMAGE", "UAV DETECTION", "COUNTER ECM",
                            "COUNTER ECM LOCKED DAMAGE", "TURRET KILL", "AMS MISSILE DESTROYED");
            }
            return Arrays.asList();
        }
    }
}
