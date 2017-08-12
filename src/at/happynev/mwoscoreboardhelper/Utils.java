package at.happynev.mwoscoreboardhelper;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.paint.Color;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;

/**
 * Created by Nev on 15.01.2017.
 */
public class Utils {
    private static Comparator<String> numberComparator = new NumericStringComparator();

    public static Comparator<String> getNumberComparator() {
        return numberComparator;
    }

    public static File getHomeDir() {
        File f = new File(System.getProperty("user.home"), "/.MwoScoreboardHelper");
        if (!f.isDirectory()) f.mkdirs();
        return f;
    }

    public static File getInstallDir() {
        File f = new File(".").getAbsoluteFile();
        return f;
    }

    public static BigDecimal getMedianValue(BigDecimal[] values) {
        BigDecimal ret = BigDecimal.ZERO;
        if (values.length > 0) {
            for (int i = 0; i < values.length; i++) {
                //safety padding
                if (values[i] == null) values[i] = BigDecimal.ZERO;
            }
            Arrays.sort(values, (o1, o2) -> o1.compareTo(o2));
            if (values.length % 2 == 0) {
                BigDecimal mid1 = values[values.length / 2];
                BigDecimal mid2 = values[(values.length / 2) + 1];
                ret = mid1.add(mid2).divide(BigDecimal.valueOf(2));
            } else {
                int mid = (values.length + 1) / 2;
                ret = values[mid];
            }
        }
        return ret;
    }

    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    public static boolean confirmationDialog(String header, String info) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(header);
        alert.setHeaderText(info);
        //alertPopup.setContentText();
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get().getButtonData().isDefaultButton();
    }

    public static String getWebColor(Color newValue) {
        return newValue.toString().replaceAll("..;?$", "");
    }

    public static String getRatio(double numerator, double denominator) {
        return getRatio(numerator, denominator, 2);
    }

    public static String getPercentage(double fraction, double total) {
        if (total == 0.0d) {
            return "100%";
        }
        return new BigDecimal(fraction / total).multiply(new BigDecimal(100)).setScale(0, BigDecimal.ROUND_HALF_UP).toPlainString() + "%";
    }

    public static String getRatio(Double numerator, Double denominator, int precision) {
        if (numerator == null) {
            numerator = 0.0;
        }
        if (denominator == null) {
            denominator = 0.0;
        }
        if (numerator == 0.0d && denominator == 0.0d) {
            return "N/A";
        }
        if (denominator > 0.0d) {
            return new BigDecimal(numerator.doubleValue() / denominator.doubleValue()).setScale(precision, BigDecimal.ROUND_HALF_UP).toPlainString();
        } else {
            return new BigDecimal(numerator).setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString();
        }
    }

    public static class NumericStringComparator implements Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            if (o1 == null && o2 != null) {
                return -1;
            }
            if (o1 != null && o2 == null) {
                return 1;
            }
            if (o1 == null && o2 == null) {
                return 0;
            }
            try {
                if (o1.endsWith("%")) {
                    o1 = o1.substring(0, o1.length() - 1);
                }
                if (o2.endsWith("%")) {
                    o2 = o2.substring(0, o2.length() - 1);
                }
                Double i1 = Double.valueOf(o1);
                Double i2 = Double.valueOf(o2);
                return i1.compareTo(i2);
            } catch (NumberFormatException e) {
                //not numeric then let String compare
                return o1.compareTo(o2);
            }
        }
    }
}
