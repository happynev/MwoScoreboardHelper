package at.happynev.mwoscoreboardhelper;

import javafx.fxml.FXML;
import org.apache.commons.lang3.time.FastDateFormat;

/**
 * Created by Nev on 15.01.2017.
 */
public class PersonalTabController {
    private static PersonalTabController instance;


    FastDateFormat fdfMatch = FastDateFormat.getInstance("yyyy-MM-dd HH:mm");

    public PersonalTabController() {
        instance = this;
    }

    public static PersonalTabController getInstance() {
        if (instance == null) {
            instance = new PersonalTabController();
        }
        return instance;
    }


    @FXML
    private void initialize() {

    }

}
