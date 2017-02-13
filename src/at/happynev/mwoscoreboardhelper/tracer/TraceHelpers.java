package at.happynev.mwoscoreboardhelper.tracer;

import at.happynev.mwoscoreboardhelper.Logger;
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
public class TraceHelpers {
    private static final Map<String, String> mapAmbiguousChars = new HashMap<>();
    private static final String regexMetaChars = "([.*+?()\\[\\]|^${}\\\\])";

    static {
        mapAmbiguousChars.put("[TlI1tJ]", "[TlI1tJ]");
        mapAmbiguousChars.put("[2Z7z]", "[2Z7z]");
        mapAmbiguousChars.put("[G68B3E]", "[G68B3E]");
        mapAmbiguousChars.put("[QDCO0og9]", "[QDCO0og9]");
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

    public static BufferedImage extractSpecificColor(BufferedImage input, int[] min, int[] max) {
        byte[] matchMin = new byte[]{(byte) min[0], (byte) min[1], (byte) min[2]};
        byte[] matchMax = new byte[]{(byte) max[0], (byte) max[1], (byte) max[2]};
        int white = buildColorInt(new byte[]{(byte) 255, (byte) 255, (byte) 255});
        int black = buildColorInt(new byte[]{(byte) 0, (byte) 0, (byte) 0});
        BufferedImage out = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_RGB);
        if (input.getColorModel().getColorSpace().getType() == ColorSpace.TYPE_RGB && input.getRaster().getTransferType() == DataBuffer.TYPE_INT) {
            int len = input.getWidth() * input.getHeight();
            int[] buffer = new int[len];
            input.getRaster().getDataElements(0, 0, input.getWidth(), input.getHeight(), buffer);
            for (int i = 0; i < len; i++) {
                byte[] src = splitColorInt(buffer[i]);
                if (checkBetween(src, matchMin, matchMax)) {
                    buffer[i] = white;
                } else {
                    buffer[i] = black;
                }
            }

            out.getRaster().setDataElements(0, 0, input.getWidth(), input.getHeight(), buffer);
        } else {
            //Logger.warning("Wrong colorspace (" + input.getColorModel().getColorSpace().getType() + ") and/or transfertype (" + input.getRaster().getTransferType() + ")");
            return input;
        }
        return out;
    }

    public static BufferedImage threshold(BufferedImage input, int[] min) {
        byte[] matchMin = new byte[]{(byte) min[0], (byte) min[1], (byte) min[2]};
        byte[] black = new byte[]{(byte) 0, (byte) 0, (byte) 0};
        BufferedImage out = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_RGB);
        if (input.getColorModel().getColorSpace().getType() == ColorSpace.TYPE_RGB && input.getRaster().getTransferType() == DataBuffer.TYPE_INT) {
            int len = input.getWidth() * input.getHeight();
            int[] buffer = new int[len];
            input.getRaster().getDataElements(0, 0, input.getWidth(), input.getHeight(), buffer);
            for (int i = 0; i < len; i++) {
                byte[] src = splitColorInt(buffer[i]);
                if (checkBetween(src, black, matchMin)) {
                    buffer[i] = buildColorInt(black);
                }
            }

            out.getRaster().setDataElements(0, 0, input.getWidth(), input.getHeight(), buffer);
        } else {
            //Logger.warning("Wrong colorspace (" + input.getColorModel().getColorSpace().getType() + ") and/or transfertype (" + input.getRaster().getTransferType() + ")");
            return input;
        }
        return out;
    }

    private static float getHueDistance(float[] a, float[] b) {
        return Math.abs(a[0] - b[0]);
    }

    private static boolean checkBetween(byte[] input, byte[] min, byte[] max) {
        //System.out.println("check " + (input[0] & (0xff)) + "/" + (input[1] & (0xff)) + "/" + (input[2] & (0xff)));
        for (int i = 0; i < input.length; i++) {
            //System.out.println(i + " check vs " + (min[i] & (0xff)) + " and " + (max[i] & (0xff)));
            if ((input[i] & (0xff)) < (min[i] & (0xff))) return false;
            if ((input[i] & (0xff)) > (max[i] & (0xff))) return false;
        }
        return true;
    }

    private static boolean checkHueDistance(byte[] a, byte[] b, int max) {
        float fmax = (max) / 255f;
        float[] fa = convertRGBtoHSB(a);
        float[] fb = convertRGBtoHSB(b);
        return getHueDistance(fa, fb) < fmax;
    }

    private static boolean checkRgbDistance(byte[] a, byte[] b, int max) {
        int dr = Math.abs(a[0] - b[0]);
        if (dr >= max) return false;
        int dg = Math.abs(a[1] - b[1]);
        if (dg >= max) return false;
        int db = Math.abs(a[2] - b[2]);
        return db < max;
    }

    private static int getRgbDistance(byte[] a, byte[] b) {
        double dist = 0;
        int ravg = (a[0] + b[0]) / 2;
        int dr = Math.abs(a[0] - b[0]);
        int dg = Math.abs(a[1] - b[1]);
        int db = Math.abs(a[2] - b[2]);
        dist += (512 + ravg) * dr * dr;
        dist += 4 * dg * dg;
        dist += (767 - ravg) * db * db;
        //(((512+rmean)*r*r)>>8)
        // + 4*g*g
        // + (((767-rmean)*b*b)>>8);
        return (int) (Math.sqrt(dist));
    }

    private static float[] convertRGBtoHSB(byte[] in) {
        return Color.RGBtoHSB(in[0], in[1], in[2], null);
    }

    private static int convertHSBtoRGB(float[] in) {
        return Color.HSBtoRGB(in[0], in[1], in[3]);
    }

    private static int buildColorInt(byte[] rgb) {
        return (((int) rgb[2] & 0xFF) << 16) |
                (((int) rgb[1] & 0xFF) << 8) |
                (((int) rgb[0] & 0xFF));
    }

    private static byte[] invert(byte[] rgb) {
        rgb[0] = (byte) (255 - rgb[0]);
        rgb[1] = (byte) (255 - rgb[1]);
        rgb[2] = (byte) (255 - rgb[2]);
        return rgb;
    }

    private static byte[] splitColorInt(int rgb) {
        byte[] ret = new byte[3];
        ret[0] = (byte) (rgb >> 16);
        ret[1] = (byte) (rgb >> 8);
        ret[2] = (byte) (rgb >> 0);
        return ret;
    }
}
