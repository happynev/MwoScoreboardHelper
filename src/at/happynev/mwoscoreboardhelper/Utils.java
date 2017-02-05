package at.happynev.mwoscoreboardhelper;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.paint.Color;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Optional;

/**
 * Created by Nev on 15.01.2017.
 */
public class Utils {
    public static File getHomeDir() {
        File f = new File(System.getProperty("user.home"), "/.MwoScoreboardHelper");
        if (!f.isDirectory()) f.mkdirs();
        return f;
    }

    public static File getInstallDir() {
        File f = new File(".").getAbsoluteFile();
        return f;
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
}
