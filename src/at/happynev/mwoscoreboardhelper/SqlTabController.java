package at.happynev.mwoscoreboardhelper;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Nev on 15.01.2017.
 */
public class SqlTabController {
    @FXML
    TextArea textSql;

    @FXML
    Label labelStatus;

    @FXML
    Button buttonExecuteQuery;

    @FXML
    RadioButton radioUpdate;
    @FXML
    RadioButton radioQuery;
    @FXML
    Label labelTableInfo;

    @FXML
    Pane paneResult;
    File lastExportDir = Utils.getHomeDir();
    private PreparedStatement prep = null;

    @FXML
    private void initialize() {
        //load values
        radioQuery.setSelected(true);
        textSql.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                prep = DbHandler.getInstance().getConnection().prepareStatement(newValue);
                buttonExecuteQuery.setDisable(false);
                labelStatus.setText("");
            } catch (Exception e) {
                labelStatus.setText(e.getMessage());
                buttonExecuteQuery.setDisable(true);
            }
            buttonExecuteQuery.setOnAction(event -> executeQuery());
        });
        textSql.setText("SELECT match.id AS match, match.gamemode, match.map ,match.matchresult, player.player_data_id AS player, player.enemy, player.mech, player.status, player.score, player.damage, player.kills, player.assists, player.ping \n" +
                "FROM match_data match, player_matchdata player \n" +
                "WHERE match.id=player.match_data_id AND match.matchresult IS NOT NULL\n" +
                "ORDER BY match.id, player.enemy");
        try {
            PreparedStatement prep = DbHandler.getInstance().prepareStatement("select TABLE_NAME from INFORMATION_SCHEMA.TABLES where TABLE_SCHEMA='PUBLIC' order by id");
            ResultSet rs = prep.executeQuery();
            StringBuilder sb = new StringBuilder("DB Schema Version: ").append(Main.getDbVersion()).append("\n");
            sb.append("Available Tables:");
            while (rs.next()) {
                sb.append("\n");
                sb.append(rs.getString(1));
            }
            rs.close();
            labelTableInfo.setText(sb.toString());
        } catch (Exception e) {
            Logger.error(e);
        }
    }

    private void executeQuery() {
        try {
            if (prep != null) {
                if (radioQuery.isSelected()) {
                    ResultSet rs = prep.executeQuery();
                    TableView<List<String>> resultTable = new TableView<>();
                    List<String> columnHeader = new ArrayList<>();
                    paneResult.getChildren().clear();
                    for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                        String colname = rs.getMetaData().getColumnLabel(i + 1);
                        columnHeader.add(colname);
                        TableColumn<List<String>, String> col = new TableColumn<>(colname);

                        col.setCellValueFactory(param -> {
                            String value = param.getValue().get(resultTable.getColumns().indexOf(param.getTableColumn()));
                            return new SimpleStringProperty(value);
                        });
                        resultTable.getColumns().add(col);
                    }
                    VBox.setVgrow(resultTable, Priority.ALWAYS);
                    paneResult.getChildren().add(resultTable);
                    while (rs.next()) {
                        List<String> line = new ArrayList<>();
                        for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                            line.add(rs.getString(i + 1));
                        }
                        resultTable.getItems().add(line);
                    }
                    rs.close();
                    Button buttonExport = new Button("Export as CSV");
                    buttonExport.setOnAction(a -> {
                        FileChooser fc = new FileChooser();
                        fc.setTitle("Select export file");
                        fc.setInitialDirectory(lastExportDir);
                        fc.setInitialFileName("export.csv");
                        File chosen = fc.showSaveDialog(null);
                        if (chosen != null) {
                            lastExportDir = chosen.getParentFile();
                            exportData(chosen, textSql.getText(), columnHeader, resultTable.getItems());
                        }
                    });
                    paneResult.getChildren().add(buttonExport);
                } else if (radioUpdate.isSelected()) {
                    int rows = prep.executeUpdate();
                    paneResult.getChildren().clear();
                    paneResult.getChildren().add(new Label("Rows affected: " + rows));
                }
            }
        } catch (Exception e) {
            Logger.error(e);
        }
    }

    private void exportData(File outputfile, String query, List<String> columns, List<List<String>> lines) {
        try (FileOutputStream fos = new FileOutputStream(outputfile, false); BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            String intro = "#MwoScoreboardHelper CSV export by " + SettingsTabController.getSelfPlayerInstance().getPilotname() + " on " + new Date() + "\r\n";
            String cleanedquery = "#" + query.replaceAll("\r?\n", " ") + "\r\n";
            bos.write(intro.getBytes(StandardCharsets.UTF_8));
            bos.write(cleanedquery.getBytes(StandardCharsets.UTF_8));
            bos.write(collectLine(columns, ";"));
            for (List<String> line : lines) {
                bos.write(collectLine(line, ";"));
            }
        } catch (Exception e) {
            Logger.error(e);
        }
    }

    private byte[] collectLine(List<String> fields, String delimiter) {
        StringBuilder sb = new StringBuilder();
        for (String f : fields) {
            if (sb.length() > 0) {
                sb.append(delimiter);
            }
            if (f == null) f = "";
            f = f.replaceAll("\\r?\\n", "\\\\n");
            if (f.contains(delimiter)) {
                f = "\"" + f.replaceAll("\"", "'") + "\"";
            }
            sb.append(f);
        }
        sb.append("\r\n");
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }
}

