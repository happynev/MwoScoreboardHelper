package at.happynev.mwoscoreboardhelper.tracer;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.binding.When;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.List;

/**
 * Created by Nev on 20.01.2017.
 */
public abstract class AsyncTracer {
    private SimpleBooleanProperty finished = new SimpleBooleanProperty(false);
    private SimpleStringProperty progress = new SimpleStringProperty("");

    private static boolean traceField(TraceableImage f) {
        if (f == null) {
            return false;
        } else {
            f.performTrace();
            return true;
        }
    }

    private static void increment(SimpleIntegerProperty i) {
        Platform.runLater(() -> i.setValue(i.getValue() + 1));
    }

    protected final void traceAllAsync(final List<TraceableImage> fields) {
        final SimpleIntegerProperty numFields = new SimpleIntegerProperty(0);
        final SimpleIntegerProperty numFinished = new SimpleIntegerProperty(0);
        fields.forEach(field -> {
            if (field != null) increment(numFields);
        });
        progress.bind(Bindings.concat("Traced ", numFinished, " of ", numFields, " fields").concat(new When(BooleanExpression.booleanExpression(numFinished.isEqualTo(numFields))).then(" - FINISHED").otherwise("")));
        new Thread() {
            @Override
            public void run() {
                super.run();
                fields.forEach(field -> {
                    if (traceField(field)) increment(numFinished);
                });
                Platform.runLater(() -> finished.setValue(true));
            }
        }.start();
    }

    public String getProgress() {
        return progress.get();
    }

    public SimpleStringProperty progressProperty() {
        return progress;
    }

    public boolean getFinished() {
        return finished.get();
    }

    public SimpleBooleanProperty finishedProperty() {
        return finished;
    }
}
