package at.happynev.mwoscoreboardhelper.controls;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

/**
 * Created by Nev on 19.02.2017.
 */
public class RatingControl extends StackPane {
    private final Label label = new Label();
    private final SimpleDoubleProperty value = new SimpleDoubleProperty(0);
    private final SimpleDoubleProperty shownValue = new SimpleDoubleProperty(0);
    private final Color colorPositive = Color.GREEN;
    private final Color colorNegative = Color.RED;
    private final Color colorNeutral = Color.GREY;

    public RatingControl(String s) {
        super();
        label.setText(s);
        label.setFont(Font.font(15));
        label.setStyle("-fx-text-fill:#FFFFFF");
        label.setBorder(new Border(new BorderStroke(null, null, null, BorderWidths.DEFAULT)));
        label.setTextAlignment(TextAlignment.CENTER);
        label.prefHeightProperty().bind(this.heightProperty().subtract(2));
        label.prefWidthProperty().bind(this.heightProperty().subtract(2));
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMaxHeight(Double.MAX_VALUE);
        label.setAlignment(Pos.CENTER);
        //label.layoutXProperty().bind(center.subtract(label.prefWidthProperty().divide(2)));
        //this.setPadding(new Insets(4));
        final SimpleDoubleProperty center = new SimpleDoubleProperty(0);
        center.bind(this.layoutXProperty().add(this.widthProperty().divide(2)));

        Line centerLine = new Line();
        centerLine.startXProperty().bind(center);
        centerLine.endXProperty().bind(center);
        centerLine.startYProperty().bind(label.layoutYProperty().subtract(label.heightProperty()).add(1));
        centerLine.endYProperty().bind(label.layoutYProperty().subtract(1));

        Rectangle border = new Rectangle();
        border.setStroke(Color.BLACK);
        border.setFill(Color.TRANSPARENT);
        border.widthProperty().bind(label.widthProperty().subtract(2));
        border.heightProperty().bind(label.heightProperty().subtract(2));
        border.setArcHeight(10);
        border.setArcWidth(10);
        //border.xProperty().bind(label.layoutXProperty());
        //border.yProperty().bind(label.layoutYProperty());

        Rectangle valueFill = new Rectangle();
        Pane widthFiller = new Pane(valueFill);
        widthFiller.prefHeightProperty().bind(widthFiller.heightProperty().subtract(2));
        widthFiller.prefWidthProperty().bind(widthFiller.heightProperty().subtract(2));
        valueFill.setStroke(Color.TRANSPARENT);
        valueFill.setFill(Color.TRANSPARENT);
        valueFill.yProperty().bind(widthFiller.layoutYProperty().add(1));
        valueFill.heightProperty().bind(widthFiller.heightProperty().subtract(2));
        valueFill.setArcHeight(10);
        valueFill.setArcWidth(10);
        SimpleDoubleProperty currentValue = new SimpleDoubleProperty();
        currentValue.bind(value);
        currentValue.addListener((observable, oldValue, newValue) -> {
            if (newValue.doubleValue() >= 0) {
                valueFill.setX(center.get());
                valueFill.setWidth(((label.getWidth() - 2) / 2) * newValue.doubleValue());
                valueFill.setFill(colorNeutral.interpolate(colorPositive, Math.max(0.2, newValue.doubleValue())));
            } else {
                double width = ((label.getWidth() - 2) / 2) * newValue.doubleValue() * -1;
                valueFill.setX(center.get() - width);
                valueFill.setWidth(width);
                valueFill.setFill(colorNeutral.interpolate(colorNegative, Math.max(0.2, newValue.doubleValue() * -1)));
            }
        });
        this.setOnMouseEntered(event1 -> currentValue.bind(shownValue));
        this.setOnMouseExited(event1 -> currentValue.bind(value));

        this.setOnMouseMoved(event -> {
            double relativeValue = event.getX() - center.get();
            double v = relativeValue / (this.getWidth() / 2);
            shownValue.set(v);
        });

        this.setOnMouseClicked(event -> value.set(shownValue.get()));

        this.getChildren().addAll(widthFiller, centerLine, label, border);
    }

    public double getValue() {
        return value.get();
    }

    public SimpleDoubleProperty valueProperty() {
        return value;
    }
}
