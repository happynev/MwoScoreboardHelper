package at.happynev.mwoscoreboardhelper;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.paint.Color;

import java.io.*;
import java.math.BigDecimal;
import java.net.URL;
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
            Arrays.sort(values, Comparator.naturalOrder());
            if (values.length % 2 == 0) {
                BigDecimal mid1 = values[(values.length / 2) - 1];
                BigDecimal mid2 = values[values.length / 2];
                ret = mid1.add(mid2).divide(BigDecimal.valueOf(2));
            } else {
                int mid = (values.length - 1) / 2;
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

    public static String getPercentage(double fraction, double total) {
        if (total == 0.0d) {
            return "100%";
        }
        return new BigDecimal(fraction / total).multiply(new BigDecimal(100)).setScale(0, BigDecimal.ROUND_HALF_UP).toPlainString() + "%";
    }

    public static String getRatio(Double numerator, Double denominator) {
        if (numerator == null) {
            numerator = 0.0;
        }
        if (denominator == null || denominator == 0.0d) {
            denominator = 1.0;//
        }

        BigDecimal ret = new BigDecimal(numerator.doubleValue() / denominator.doubleValue()).setScale(10, BigDecimal.ROUND_HALF_UP);
        int precision = 2;
        if (ret.abs().doubleValue() > 100 || ret.signum() == 0) {
            precision = 0;
        } else if (ret.abs().doubleValue() > 10) {
            precision = 1;
        } else if (ret.abs().doubleValue() < 1) {
            precision = 3;
        }
        return ret.setScale(precision, BigDecimal.ROUND_HALF_UP).toPlainString();
    }

    public static void reportMemoryUsage(String info) {
        long total = Runtime.getRuntime().totalMemory() / (1024 * 1024);
        long used = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024);
        Logger.log("memory usage " + info + ": " + used + "mb of " + total + "mb");
    }

    public static long streamCopy(InputStream input, OutputStream output, int bufferSize) throws IOException {
        byte[] buffer = new byte[bufferSize];
        long count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    public static byte[] httpGet(URL url) {
        try {
            InputStream is = url.openStream();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            streamCopy(is, bos, 8096);
            bos.close();
            is.close();
            return bos.toByteArray();
        } catch (IOException e) {
            Logger.error(e);
        }
        return null;
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
