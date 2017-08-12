package at.happynev.mwoscoreboardhelper;

import at.happynev.mwoscoreboardhelper.stat.CustomizableStatRuntime;
import at.happynev.mwoscoreboardhelper.stat.CustomizableStatTemplate;
import at.happynev.mwoscoreboardhelper.stat.DisplayableStat;
import at.happynev.mwoscoreboardhelper.stat.StatTable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.effect.Bloom;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
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
    public static final Insets DATA_INSETS = new Insets(2, 5, 2, 5);
    public static final Insets PLAYER_INSETS = new Insets(0, 10, 0, 10);

    public static ColumnConstraints getColumnConstraint(Label label) {
        Text measure = new Text(label.getText());
        measure.setFont(label.getFont());
        double prefWidth = measure.getLayoutBounds().getWidth();
        ColumnConstraints c = new ColumnConstraints(prefWidth, prefWidth, Double.MAX_VALUE, Priority.SOMETIMES, HPos.LEFT, true);
        return c;
    }

    public static void prepareGrid(GridPane grid, MatchRuntime match, StatTable table) {
        grid.getChildren().clear();
        grid.getColumnConstraints().clear();
        int col = 0;
        if (table != StatTable.WATCHER_PERSONAL) {
            if (SettingsTabController.getInstance().getLayoutShowUnit()) {
                Label labelUnit = applyHeaderFormat(new Label("Unit"), null);
                grid.getColumnConstraints().add(getColumnConstraint(labelUnit));
                labelUnit.setTooltip(new Tooltip(labelUnit.getText()));
                grid.add(labelUnit, col++, 0);
            }
            if (SettingsTabController.getInstance().getLayoutShowName()) {
                Label labelPilotname = applyHeaderFormat(new Label("Pilot Name"), null);
                grid.getColumnConstraints().add(getColumnConstraint(labelPilotname));
                labelPilotname.setTooltip(new Tooltip(labelPilotname.getText()));
                grid.add(labelPilotname, col++, 0);
            }
            if (SettingsTabController.getInstance().getLayoutShowNote()) {
                Label labelShortnote = applyHeaderFormat(new Label("Short Note"), null);
                grid.getColumnConstraints().add(getColumnConstraint(labelShortnote));
                labelShortnote.setTooltip(new Tooltip(labelShortnote.getText()));
                grid.add(labelShortnote, col++, 0);
            }
        }
        for (CustomizableStatTemplate key : match.getStatsToDisplay(table)) {
            Label label = applyHeaderFormat(new Label(key.getShortName()), key);
            grid.getColumnConstraints().add(getColumnConstraint(label));
            label.setTooltip(new Tooltip(key.getDescription()));
            grid.add(label, col++, 0);
        }
    }

    private static void clickPlayer(MouseEvent event, PlayerRuntime pr) {
        if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
            PlayerTabController.getInstance().selectPlayerFromList(pr);
        }
    }

    private static Label applyHeaderFormat(Label node, DisplayableStat stat) {
        Font fontHeader = Font.font("System", FontWeight.BOLD, SettingsTabController.getInstance().getFontSize() + 2);
        node.setFont(fontHeader);
        Color textColor = Color.WHITE;
        if (stat != null) {
            textColor = stat.getColor();
        }
        node.setTextFill(textColor);
        node.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
        GridPane.setFillWidth(node, true);
        node.setMaxWidth(Double.MAX_VALUE);
        return node;
    }

    public static Control applyPlayerFormat(Control node, PlayerRuntime player) {
        SimpleObjectProperty<Color> frontColor = new SimpleObjectProperty<>(Color.WHITE);
        SimpleObjectProperty<Color> backColor = new SimpleObjectProperty<>(Color.BLACK);
        frontColor.bind(player.guicolor_frontProperty());
        backColor.bind(player.guicolor_backProperty());
        ObjectBinding<Background> backBinding = Bindings.createObjectBinding(() -> {
            BackgroundFill fill = new BackgroundFill(backColor.getValue(), CornerRadii.EMPTY, Insets.EMPTY);
            return new Background(fill);
        }, backColor);
        ObjectBinding<String> textBinding = Bindings.createObjectBinding(() -> "-fx-text-fill:" + Utils.getWebColor(frontColor.get()).replaceAll("0x", "#"), frontColor);
        node.backgroundProperty().bind(backBinding);
        GridPane.setFillWidth(node, true);
        node.setMaxWidth(Double.MAX_VALUE);
        node.setPadding(PLAYER_INSETS);
        if (node instanceof Labeled) {
            Labeled lnode = (Labeled) node;
            lnode.setFont(new Font(SettingsTabController.getInstance().getFontSize()));
            lnode.textFillProperty().bind(frontColor);
        }
        if (node instanceof TextInputControl) {
            TextInputControl tnode = (TextInputControl) node;
            tnode.styleProperty().bind(textBinding);
            tnode.setFont(new Font(SettingsTabController.getInstance().getFontSize() - 2));
        }
        return node;
    }

    public static void addDataToGrid(GridPane parent, int row, MatchRuntime match, PlayerRuntime player, StatTable table) {
        PlayerMatchRecord thisMatchRecord = match.getPlayerMatchRecord(player);
        int col = 0;
        if (table != StatTable.WATCHER_PERSONAL) {
            Label labelUnit = new Label();
            Label labelName = new Label();
            TextField textShortNote = new TextField();
            applyPlayerFormat(labelUnit, player);
            applyPlayerFormat(labelName, player);
            applyPlayerFormat(textShortNote, player);

            labelName.effectProperty().bind(Bindings.when(labelName.hoverProperty()).then(new Bloom(0)).otherwise((Bloom) null));
            labelName.setTooltip(new Tooltip("Double-click to jump to player tab"));
            labelName.setOnMouseClicked(event -> clickPlayer(event, player));
            labelUnit.textProperty().bind(player.unitProperty());
            labelName.textProperty().bind(player.pilotnameProperty());
            Tooltip noteTooltip = new Tooltip();
            noteTooltip.textProperty().bind(textShortNote.textProperty());
            textShortNote.textProperty().bindBidirectional(player.shortnoteProperty());
            textShortNote.setTooltip(noteTooltip);
            if (SettingsTabController.getInstance().getLayoutShowUnit()) {
                parent.add(labelUnit, col++, row);
            }
            if (SettingsTabController.getInstance().getLayoutShowName()) {
                ColumnConstraints tmp = GuiUtils.getColumnConstraint(labelName);
                ColumnConstraints cc = parent.getColumnConstraints().get(col);
                if (cc.getMinWidth() < tmp.getPrefWidth()) {
                    cc.setMinWidth(tmp.getPrefWidth());
                }
                parent.add(labelName, col++, row);
            }
            if (SettingsTabController.getInstance().getLayoutShowNote()) {
                parent.add(textShortNote, col++, row);
            }
        }
        for (CustomizableStatTemplate stat : match.getStatsToDisplay(table)) {
            CustomizableStatRuntime statRuntime = stat.getRuntimeInstance(thisMatchRecord);
            String value = statRuntime.getValue();
            Label l = new Label();
            applyPlayerFormat(l, player);
            l.setText(value);
            Tooltip tt = new Tooltip();
            tt.setText(statRuntime.getExplanation());
            l.setTooltip(tt);
            ColumnConstraints tmp = GuiUtils.getColumnConstraint(l);
            ColumnConstraints cc = parent.getColumnConstraints().get(col);
            if (cc.getPrefWidth() < tmp.getPrefWidth()) {
                cc.setPrefWidth(tmp.getPrefWidth());
            }
            parent.add(l, col++, row);
        }
    }
}
