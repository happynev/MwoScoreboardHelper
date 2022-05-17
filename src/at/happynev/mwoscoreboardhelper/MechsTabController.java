package at.happynev.mwoscoreboardhelper;

import at.happynev.mwoscoreboardhelper.smurfyapi.Mech;
import com.google.gson.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
        buttonImportMechData.setText("Reload mechs.json");
        buttonImportMechData.setOnAction(event -> downloadMechData());
        buttonReloadData.setText("export as json");
        buttonReloadData.setOnAction(event -> exportMechData());
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

    private void exportMechData() {
        File localMechData = new File("./exported_mechs.json");
        JsonObject rootList = new JsonObject();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        MechRuntime.getKnownMechs().entrySet().stream().sorted(Comparator.comparingInt(value -> Integer.parseInt(value.getKey()))).forEach(me -> {
            Mech mech = me.getValue().getDataObject();
            JsonElement m = gson.toJsonTree(mech);
            rootList.add("" + mech.getId(), m);
        });
        try {
            Files.write(localMechData.toPath(), gson.toJson(rootList).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            Logger.error(e);
        }
    }

    private void downloadMechData() {
        List<Mech> mechs = getLocalMechData();
        Logger.log("found " + mechs.size() + " local mechs");
        if (mechs.size() > 0) {
            System.out.println(new Gson().toJson(mechs.get(0)));
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

    private List<Mech> getLocalMechData() {
        try {
            //{"id":"1","name":"hbk-4g","faction":"InnerSphere","mech_type":"medium","family":"hunchback","chassis_translated":"HUNCHBACK","translated_name":"HBK-4G","translated_short_name":"HBK-4G","details":{"type":"","tons":50,"top_speed":89.1,"max_armor":338}}
            List<Mech> mechs = new ArrayList<>();
            File localMechData = new File("./mechs.json");
            if (localMechData.exists()) {
                Gson gson = new GsonBuilder().setLenient().create();
                JsonObject root = new JsonParser().parse(new String(Files.readAllBytes(localMechData.toPath()), StandardCharsets.UTF_8)).getAsJsonObject();
                root.entrySet().forEach(e -> mechs.add(gson.fromJson(e.getValue(), Mech.class)));
            }
            return mechs;
        } catch (Exception e) {
            Logger.error(e);
            return Collections.emptyList();
        }
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
