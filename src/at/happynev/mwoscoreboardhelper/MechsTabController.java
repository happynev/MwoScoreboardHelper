package at.happynev.mwoscoreboardhelper;

import at.happynev.mwoscoreboardhelper.smurfyapi.ApiCaller;
import at.happynev.mwoscoreboardhelper.smurfyapi.Mech;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.PreparedStatement;
import java.util.List;

/**
 * Created by Nev on 15.01.2017.
 */
public class MechsTabController {
    @FXML
    Button buttonClearData;

    @FXML
    Button buttonImportMechData;
    @FXML
    Button buttonReloadData;
    @FXML
    TableView<MechRuntime> tableMechs;

    private static TableColumn<MechRuntime, ?> createColumn(String header, String valueName) {
        TableColumn<MechRuntime, ?> col = new TableColumn<>(header);
        col.setCellValueFactory(new PropertyValueFactory<>(valueName));
        return col;
    }

    @FXML
    private void initialize() {
        buttonClearData.setOnAction(event -> deleteMechData());
        buttonImportMechData.setOnAction(event -> downloadMechData());
        buttonReloadData.setOnAction(event -> reloadData());
        tableMechs.getColumns().addAll(
                createColumn("ID", "id"),//
                createColumn("Faction", "faction"),//
                createColumn("Name", "name"),//
                createColumn("Short Name", "shortName"),//
                createColumn("Chassis", "chassis"),//
                createColumn("Tons", "tons"),//
                createColumn("Class", "weightClass"),//
                createColumn("Speed", "speed"),//
                createColumn("Armor", "armor"),//
                createColumn("Special", "specialtype"),//
                createColumn("# Seen", "seen"),//
                createColumn("Avg. Damage", "avgDamage"),//
                createColumn("Avg. Score", "avgScore"),//
                createColumn("% of Mechs", "popularityTotal"),//
                createColumn("% of Chassis", "popularityChassis"),//
                createColumn("% of Class", "popularityClass"),//
                createColumn("% of Faction", "popularityFaction"),//
                createColumn("Damage/ton", "damagePerTon"),//
                createColumn("Score/ton", "scorePerTon")//
        );
        tableMechs.getColumns().get(0).setVisible(false);
        reloadData();
    }

    private void downloadMechData() {
        Logger.log("downloading mech data");
        List<Mech> mechs = ApiCaller.getAllMechs();
        Logger.log("downloaded " + mechs.size() + " mechs");
        if (mechs.size() > 0) {
            deleteMechData();
            try {

                PreparedStatement insert = DbHandler.getInstance().prepareStatement("insert into mech_data(api_id,internal_name,name,short_name,chassis,tons,max_speed,max_armor,faction,specialtype) values(?,?,?,?,?,?,?,?,?,?)");
                for (Mech m : mechs) {
                    if ("as7-d-dc-escort".equals(m.getName())) {
                        //not really necessary and causes the short_name column to be too large
                        continue;
                    }
                    insert.clearParameters();
                    insert.setString(1, m.getId());
                    insert.setString(2, m.getName());
                    insert.setString(3, m.getTranslated_name());
                    insert.setString(4, m.getTranslated_short_name());
                    insert.setString(5, m.getChassis_translated());
                    insert.setInt(6, m.getTons());
                    insert.setString(7, m.getTop_speed());
                    insert.setInt(8, m.getMax_armor());
                    insert.setString(9, m.getFaction());
                    insert.setString(10, m.getType());
                    insert.addBatch();
                }
                insert.executeBatch();
            } catch (Exception e) {
                Logger.error(e);
            }
        }
        reloadData();
        Utils.confirmationDialog("Mech Update Successful", "got " + mechs.size() + " mechs");
    }

    private void reloadData() {
        tableMechs.getItems().clear();
        ObservableList<MechRuntime> allMechs = FXCollections.observableArrayList();
        allMechs.addAll(MechRuntime.getKnownMechs().values());
        tableMechs.setItems(allMechs);
    }

    private void deleteMechData() {
        try {
            int deleted = DbHandler.getInstance().prepareStatement("delete from mech_data").executeUpdate();
            MechRuntime.getKnownMechs().clear();
        } catch (Exception e) {
            Logger.error(e);
        }
        reloadData();
    }
}
