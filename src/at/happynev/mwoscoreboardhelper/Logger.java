package at.happynev.mwoscoreboardhelper;

import javafx.scene.control.Alert;
import org.slf4j.LoggerFactory;

/**
 * Created by Nev on 29.01.2017.
 */
public class Logger {
    //private static final java.util.logging.Logger logger = java.util.logging.Logger.getAnonymousLogger();
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("root");

    public static void dberror(Throwable e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(e.getMessage());
        //alertPopup.setContentText();
        alert.showAndWait();
        logger.error(getSource() + "Fehler hat passiert", e);
    }

    private static String getSource() {
        boolean sawLogger = false;
        for (StackTraceElement stackline : Thread.currentThread().getStackTrace()) {
            if (!sawLogger) {
                if (stackline.getClassName().contains("Logger")) {
                    sawLogger = true;
                }
            } else if (!stackline.getClassName().contains("Logger")) {
                return stackline.getClassName().replaceAll(".*\\.", "") + "." + stackline.getMethodName() + "():";
            }
        }
        return "?:?\t";
    }

    public static void error(Throwable e) {
        if (SettingsTabController.getInstance().popupsAllowed()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(e.getMessage());
            //alertPopup.setContentText();
            alert.showAndWait();
        }
        logger.error(getSource() + "Fehler hat passiert", e);
    }

    public static void infoPopup(String info) {
        logger.info(getSource() + "User Info: " + info);
        if (SettingsTabController.getInstance().popupsAllowed()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Info");
            alert.setHeaderText(info);
            //alertPopup.setContentText();
            alert.showAndWait();
        }
    }

    public static void log(String s) {
        log(s, false);
    }

    public static void log(String s, boolean includeStack) {
        logger.info(getSource() + s);
        if (includeStack) {
            boolean sawLogger = false;
            for (StackTraceElement stackline : Thread.currentThread().getStackTrace()) {
                if (!sawLogger) {
                    if (stackline.getClassName().contains("Logger")) {
                        sawLogger = true;
                    }
                } else if (!stackline.getClassName().contains("Logger")) {
                    logger.info("\t\t "+stackline.getClassName() + "." + stackline.getMethodName() + ":" + stackline.getLineNumber());
                }
            }
        }
    }

    public static void alertPopup(String info) {
        if (SettingsTabController.getInstance().popupsAllowed()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Info");
            alert.setHeaderText(info);
            //alertPopup.setContentText();
            alert.showAndWait();
        }
        warning("User Alert: " + info);
    }

    public static void warning(String s) {
        logger.warn(getSource() + s);
    }
}
