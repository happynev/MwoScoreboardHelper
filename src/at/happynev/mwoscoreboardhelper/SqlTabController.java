package at.happynev.mwoscoreboardhelper;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
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
    Pane paneResult;

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
    }

    private void executeQuery() {
        try {
            if (prep != null) {
                if (radioQuery.isSelected()) {
                    ResultSet rs = prep.executeQuery();
                    TableView<List<String>> resultTable = new TableView<>();

                    paneResult.getChildren().clear();
                    for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                        TableColumn<List<String>, String> col = new TableColumn<>(rs.getMetaData().getColumnName(i + 1));

                        col.setCellValueFactory(param -> {
                            String value = param.getValue().get(resultTable.getColumns().indexOf(param.getTableColumn()));
                            return new SimpleStringProperty(value);
                        });
                        resultTable.getColumns().add(col);
                    }
                    paneResult.getChildren().add(resultTable);
                    while (rs.next()) {
                        List<String> line = new ArrayList<>();
                        for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                            line.add(rs.getString(i + 1));
                        }
                        resultTable.getItems().add(line);
                    }
                    rs.close();
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
}

