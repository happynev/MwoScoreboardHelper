package at.happynev.mwoscoreboardhelper.tracer;

import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by Nev on 27.01.2017.
 */
public class TraceHelpers {
    private static final Map<String, String> mapAmbiguousChars = new HashMap<>();

    static {
        mapAmbiguousChars.put("[TlI1tJ]", "[TlI1tJ]");
        mapAmbiguousChars.put("[2Z7z]", "[2Z7z]");
        mapAmbiguousChars.put("[G68B3E]", "[G68B3E]");
        mapAmbiguousChars.put("[QDCO0og9]", "[QDCO0og9]");
        mapAmbiguousChars.put("[yv]", "[yv]");
        mapAmbiguousChars.put("[HR]", "[HR]");
        mapAmbiguousChars.put("[S5]", "[S5]");
        //mapAmbiguousChars.put("w", "(?:w|vv)");
        //mapAmbiguousChars.put("W", "(?:W|VV)");
    }

    public static String guessValue(String input, Collection<String> possibleValues) {
        String bestMatch = input;
        int bestSimilarity = Integer.MAX_VALUE;
        for (String possible : findSimilarLookingStrings(input, possibleValues)) {
            int score = StringUtils.getLevenshteinDistance(input, possible);
            if (score < bestSimilarity) {
                bestSimilarity = score;
                bestMatch = possible;
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
        String ret = input.replaceAll("(\\[|\\])", "\\\\$1");
        for (String p : mapAmbiguousChars.keySet()) {
            ret = ret.replaceAll(p, mapAmbiguousChars.get(p));
        }
        return ret;
    }
}
