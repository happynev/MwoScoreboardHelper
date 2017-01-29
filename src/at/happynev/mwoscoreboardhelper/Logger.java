package at.happynev.mwoscoreboardhelper;

import javafx.scene.control.Alert;
import org.slf4j.LoggerFactory;

/**
 * Created by Nev on 29.01.2017.
 */
public class Logger {
    //private static final java.util.logging.Logger logger = java.util.logging.Logger.getAnonymousLogger();
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("root");

    public static void error(Throwable e) {
        if (SettingsTabController.getInstance().popupsAllowed()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(e.getMessage());
            //alertPopup.setContentText();
            alert.showAndWait();
        }
        logger.error("Fehler hat passiert", e);
    }

    public static void infoPopup(String info) {
        logger.info("User Info: " + info);
        if (SettingsTabController.getInstance().popupsAllowed()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Info");
            alert.setHeaderText(info);
            //alertPopup.setContentText();
            alert.showAndWait();
        }
    }

    public static void log(String s) {
        logger.info(s);
    }

    public static void alertPopup(String info) {
        if (SettingsTabController.getInstance().popupsAllowed()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Info");
            alert.setHeaderText(info);
            //alertPopup.setContentText();
            alert.showAndWait();
        }
        logger.warn("User Alert: " + info);
    }
}
