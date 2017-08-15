package at.happynev.mwoscoreboardhelper;

import at.happynev.mwoscoreboardhelper.stat.*;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.effect.Bloom;
import javafx.scene.effect.Glow;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

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
    public static final Color DEFAULT_FRONT_COLOR = Color.WHITE;
    public static final Color DEFAULT_BACK_COLOR = Color.BLACK;
    private static final Bloom HOVERBLOOM = new Bloom(0);

    static {
        HOVERBLOOM.setInput(new Glow(1));
    }

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
        for (CustomizableStatTemplate stat : match.getStatsToDisplay(table)) {
            Label label = applyHeaderFormat(new Label(stat.getShortName()), stat);
            grid.getColumnConstraints().add(getColumnConstraint(label));
            applyStatFormat(label, stat);
            grid.add(label, col++, 0);
        }
    }

    private static void clickPlayer(MouseEvent event, PlayerRuntime pr) {
        if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
            PlayerTabController.getInstance().selectPlayerFromList(pr);
        }
    }

    private static Label applyHeaderFormat(Label node, DisplayableStat stat) {
        applyDefaultFormat(node);
        Font fontHeader = Font.font("System", FontWeight.BOLD, SettingsTabController.getInstance().getFontSize() + 2);
        node.setFont(fontHeader);
        node.setMaxWidth(Double.MAX_VALUE);
        return node;
    }

    public static Control applyPlayerFormat(Control node, PlayerRuntime player) {
        SimpleObjectProperty<Color> frontColor = new SimpleObjectProperty<>(DEFAULT_FRONT_COLOR);
        SimpleObjectProperty<Color> backColor = new SimpleObjectProperty<>(DEFAULT_BACK_COLOR);
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
        Label labelName = new Label();
        BooleanExpression hoveringProperty = BooleanBinding.booleanExpression(labelName.hoverProperty());
        ObjectProperty<Bloom> hoverEffectProperty = new SimpleObjectProperty<>();
        if (table != StatTable.WATCHER_PERSONAL) {
            Label labelUnit = new Label();
            TextField textShortNote = new TextField();
            applyPlayerFormat(labelUnit, player);
            hoveringProperty = hoveringProperty.or(labelUnit.hoverProperty());
            labelName.effectProperty().bind(hoverEffectProperty);
            labelUnit.effectProperty().bind(hoverEffectProperty);
            applyPlayerFormat(labelName, player);
            applyPlayerFormat(textShortNote, player);
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
            //applyPlayerFormat(l, player);
            l.setText(value);
            Tooltip tt = new Tooltip();
            tt.setText(statRuntime.getExplanationString());
            //tt.setGraphic();
            l.setTooltip(tt);
            applyDefaultFormat(l);
            applyStatFormat(l, statRuntime);
            l.effectProperty().bind(hoverEffectProperty);
            ColumnConstraints tmp = GuiUtils.getColumnConstraint(l);
            ColumnConstraints cc = parent.getColumnConstraints().get(col);
            if (cc.getPrefWidth() < tmp.getPrefWidth()) {
                cc.setPrefWidth(tmp.getPrefWidth());
            }
            parent.add(l, col++, row);
            hoveringProperty = hoveringProperty.or(l.hoverProperty());
        }
        hoverEffectProperty.bind(Bindings.when(hoveringProperty).then(HOVERBLOOM).otherwise((Bloom) null));
    }

    private static void applyDefaultFormat(Label l) {
        l.setFont(new Font(SettingsTabController.getInstance().getFontSize()));
        l.setTextFill(DEFAULT_FRONT_COLOR);
        l.setBackground(new Background(new BackgroundFill(DEFAULT_BACK_COLOR, CornerRadii.EMPTY, Insets.EMPTY)));
        GridPane.setFillWidth(l, true);
        l.setMaxWidth(Double.MAX_VALUE);
        l.setPadding(PLAYER_INSETS);
    }

    public static void applyStatFormat(Label l, DisplayableStat stat) {
        Font tooltipFont = new Font(SettingsTabController.getInstance().getFontSize() - 2);
        Color startingColor = DEFAULT_FRONT_COLOR;
        GridPane fancyTooltip = new GridPane();
        fancyTooltip.setHgap(5);
        int row = 0;
        Label labelHeader = new Label(stat.getLongName());
        labelHeader.setFont(tooltipFont);
        fancyTooltip.add(labelHeader, 0, row++, GridPane.REMAINING, 1);
        List<Paint> colors = new ArrayList<>();
        colors.add(startingColor);
        for (StatExplanationStep step : stat.getExplanation()) {
            String text = step.getDescription();
            Label labelText = new Label(text);
            labelText.setFont(tooltipFont);
            Label labelColor = new Label("   ");
            labelColor.setFont(tooltipFont);
            if (step.getPaint() != null) {
                colors.add(step.getPaint());
                labelText.setTextFill(step.getPaint());
                Paint avg = getAverageColor(colors);
                if (avg == null) {
                    labelColor.setBackground(new Background(new BackgroundFill(step.getPaint(), CornerRadii.EMPTY, Insets.EMPTY)));
                } else {
                    labelColor.setBackground(new Background(new BackgroundFill(avg, CornerRadii.EMPTY, Insets.EMPTY)));
                }
            }

            fancyTooltip.add(labelColor, 0, row);

            fancyTooltip.add(labelText, 1, row);
            if (!step.getResult().isEmpty()) {
                Label labelResult = new Label("--> " + step.getResult());
                fancyTooltip.add(labelResult, 2, row);
                labelResult.setFont(tooltipFont);
            }
            row++;
        }
        Paint finalColor = null;
        if (stat.getOverridePaint() != null) {
            finalColor = stat.getOverridePaint();
        } else {
            finalColor = getAverageColor(colors);
            if (finalColor == null) {
                finalColor = new Color(0, 0, 0, 0);
            }
        }
        Tooltip tt = new Tooltip();
        tt.setGraphic(fancyTooltip);
        l.setTooltip(tt);
        l.setTextFill(finalColor);
        l.setBackground(new Background(new BackgroundFill(DEFAULT_BACK_COLOR, CornerRadii.EMPTY, Insets.EMPTY)));
        l.setMaxWidth(Double.MAX_VALUE);
    }

    private static Paint getAverageColor(List<Paint> colors) {
        double r = 0;
        double g = 0;
        double b = 0;
        double a = 0;
        int len = colors.size();
        for (Paint paint : colors) {
            if (paint instanceof Color) {
                Color color = (Color) paint;
                r += color.getRed();
                g += color.getGreen();
                b += color.getBlue();
                a += color.getOpacity();
            } else {
                return null;
            }
        }
        return new Color(r / len, g / len, b / len, a / len);
    }
}
