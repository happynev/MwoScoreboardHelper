package at.happynev.mwoscoreboardhelper.preloader;

import javafx.beans.value.ObservableIntegerValue;
import javafx.concurrent.Task;

public interface Preloadable {
    ObservableIntegerValue loadedCountProperty();

    ObservableIntegerValue totalCountProperty();

    Task getLoaderTask();

    String getPreloadCaption();
}
