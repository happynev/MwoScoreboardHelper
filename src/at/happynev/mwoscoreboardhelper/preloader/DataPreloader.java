package at.happynev.mwoscoreboardhelper.preloader;

import at.happynev.mwoscoreboardhelper.*;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.binding.When;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataPreloader {
    private static final List<Preloadable> loaderItems = new ArrayList<>();
    private static final Map<Preloadable, Task> loaderTasks = new HashMap<>();
    private static BooleanExpression preloadFinished = new SimpleBooleanProperty(true);
    private static SimpleIntegerProperty preloadItemsFinished = new SimpleIntegerProperty();
    private static boolean abort = false;

    public static Scene getScene(Stage stage) {
        loaderItems.add(PersonalMatchRecord.getPreloaderInstance());
        loaderItems.add(PlayerMatchRecord.getPreloaderInstance());
        loaderItems.add(PlayerRuntime.getPreloaderInstance());
        loaderItems.add(MatchRuntime.getPreloaderInstance());

        GridPane pane = new GridPane();
        pane.setHgap(10);
        pane.setVgap(5);
        int row = 0;
        pane.add(new Label("Preloading data..."), 0, row++, GridPane.REMAINING, 1);
        for (Preloadable item : loaderItems) {
            Task task = item.getLoaderTask();
            loaderTasks.put(item, task);
            ProgressBar progressBar = new ProgressBar();
            progressBar.progressProperty().bind(new When(task.progressProperty().greaterThanOrEqualTo(0)).then(task.progressProperty()).otherwise(0));
            pane.add(progressBar, 0, row);
            Label label = new Label();
            label.textProperty().bind(Bindings.concat(item.getPreloadCaption(), " ", new When(task.messageProperty().isNotEmpty()).then(task.messageProperty()).otherwise("(0/" + item.totalCountProperty().getValue() + ")")));
            pane.add(label, 1, row++);
        }
        return new Scene(pane, 300, 200);
    }

    public static BooleanExpression startPreloading() {
        serializeTask(loaderItems.get(0));
        return BooleanExpression.booleanExpression(new When(preloadItemsFinished.isEqualTo(loaderItems.size())).then(true).otherwise(false));
    }

    private static void serializeTask(final Preloadable item) {
        final long start = System.currentTimeMillis();
        Task task = loaderTasks.get(item);
        Logger.log("start preload of " + item.getPreloadCaption());
        new Thread(task).start();
        task.stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                long end = System.currentTimeMillis();
                int count = item.totalCountProperty().get();
                long duration = end - start;
                BigDecimal perItem = new BigDecimal(duration).divide(new BigDecimal(count), 3, BigDecimal.ROUND_HALF_UP).movePointRight(3);
                Logger.log("loading " + item.getPreloadCaption() + " took " + duration + "ms or " + perItem.toPlainString() + "ns avg per item");
                preloadItemsFinished.set(preloadItemsFinished.getValue() + 1);
                int next = loaderItems.indexOf(item) + 1;
                if (loaderItems.size() > next) {
                    serializeTask(loaderItems.get(next));
                }
            }
        });
    }
}
