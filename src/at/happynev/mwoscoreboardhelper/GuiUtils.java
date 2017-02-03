package at.happynev.mwoscoreboardhelper;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nev on 03.02.2017.
 */
public class GuiUtils {

    public static ColumnConstraints getColumnConstraint(Label label) {
        Text measure = new Text(label.getText());
        double prefWidth = measure.getLayoutBounds().getWidth();
        ColumnConstraints c = new ColumnConstraints(prefWidth, prefWidth, Double.MAX_VALUE, Priority.SOMETIMES, HPos.LEFT, true);
        return c;
    }

    public static void prepareGrid(GridPane grid) {
        grid.getChildren().clear();
        grid.getColumnConstraints().clear();
        PlayerRuntime pr = PlayerRuntime.getReferencePlayer();
        int col = 0;
        Label labelUnit = applyHeaderFormat(new Label("Unit"));
        grid.getColumnConstraints().add(getColumnConstraint(labelUnit));
        grid.add(labelUnit, col++, 0);

        Label labelPilotname = applyHeaderFormat(new Label("Pilot Name"));
        grid.getColumnConstraints().add(getColumnConstraint(labelPilotname));
        grid.add(labelPilotname, col++, 0);

        Label labelShortnote = applyHeaderFormat(new Label("Short Note"));
        grid.getColumnConstraints().add(getColumnConstraint(labelShortnote));
        grid.add(labelShortnote, col++, 0);

        for (Stat key : pr.getCalculatedValues().keySet()) {
            Label label = applyHeaderFormat(new Label(key.toString()));
            grid.getColumnConstraints().add(getColumnConstraint(label));
            grid.add(label, col++, 0);
        }
    }

    public static Label applyHeaderFormat(Label node) {
        Font fontHeader = Font.font("System", FontWeight.BOLD, 22);
        node.setFont(fontHeader);
        node.setTextFill(Color.WHITE);
        node.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
        GridPane.setFillWidth(node, true);
        node.setMaxWidth(Double.MAX_VALUE);
        return node;
    }
}
