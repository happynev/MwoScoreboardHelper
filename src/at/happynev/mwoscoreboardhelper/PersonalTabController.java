package at.happynev.mwoscoreboardhelper;

import at.happynev.mwoscoreboardhelper.tracer.TraceHelpers;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.apache.commons.lang3.time.FastDateFormat;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

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
