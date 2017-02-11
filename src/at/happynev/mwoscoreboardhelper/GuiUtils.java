package at.happynev.mwoscoreboardhelper;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * Created by Nev on 03.02.2017.
 */
public class GuiUtils {

    public static final String colorBack = "#A0A0A0";
    public static final String colorTeam = "#0E8DFE";
    public static final String styleTeam = "-fx-text-fill: " + colorTeam;// + "; -fx-background-color: " + colorBack + ";";
    public static final String colorEnemy = "#D30000";
    public static final String styleEnemy = "-fx-text-fill: " + colorEnemy;// + "; -fx-background-color: " + colorBack + ";";
    public static final String colorNeutral = "#EDBE34";
    public static final String styleNeutral = "-fx-text-fill: " + colorNeutral;// + "; -fx-background-color: " + colorBack + ";";

    public static ColumnConstraints getColumnConstraint(Label label) {
        Text measure = new Text(label.getText());
        double prefWidth = measure.getLayoutBounds().getWidth();
        ColumnConstraints c = new ColumnConstraints(prefWidth, prefWidth, Double.MAX_VALUE, Priority.SOMETIMES, HPos.LEFT, true);
        return c;
    }

    public static void prepareGrid(GridPane grid, MatchRuntime match) {
        grid.getChildren().clear();
        grid.getColumnConstraints().clear();
        int col = 0;
        if (SettingsTabController.getInstance().getLayoutShowUnit()) {
            Label labelUnit = applyHeaderFormat(new Label("Unit"));
            grid.getColumnConstraints().add(getColumnConstraint(labelUnit));
            labelUnit.setTooltip(new Tooltip(labelUnit.getText()));
            grid.add(labelUnit, col++, 0);
        }
        if (SettingsTabController.getInstance().getLayoutShowName()) {
            Label labelPilotname = applyHeaderFormat(new Label("Pilot Name"));
            grid.getColumnConstraints().add(getColumnConstraint(labelPilotname));
            labelPilotname.setTooltip(new Tooltip(labelPilotname.getText()));
            grid.add(labelPilotname, col++, 0);
        }
        if (SettingsTabController.getInstance().getLayoutShowNote()) {
            Label labelShortnote = applyHeaderFormat(new Label("Short Note"));
            grid.getColumnConstraints().add(getColumnConstraint(labelShortnote));
            labelShortnote.setTooltip(new Tooltip(labelShortnote.getText()));
            grid.add(labelShortnote, col++, 0);
        }
        for (Stat key : match.getStatsToDisplay()) {
            Label label = applyHeaderFormat(new Label(key.toString()));
            grid.getColumnConstraints().add(getColumnConstraint(label));
            label.setTooltip(new Tooltip(key.getDescription()));
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
