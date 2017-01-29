package at.happynev.mwoscoreboardhelper.tracer;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import net.sourceforge.tess4j.Tesseract1;
import net.sourceforge.tess4j.TesseractException;

import java.awt.image.BufferedImage;

/**
 * Created by Nev on 15.01.2017.
 */
public class TraceableImage {
    private final BufferedImage image;
    private final Tesseract1 tesseract = new Tesseract1();
    private final SimpleStringProperty value = new SimpleStringProperty("");
    private long traceDuration = 0;

    public TraceableImage(BufferedImage image) {
        this(image, OcrConfig.DEFAULT);
    }

    public TraceableImage(BufferedImage image, OcrConfig cfg) {
        if (image == null) {
            throw new IllegalArgumentException("image is null");
        } else {
            cfg.applyConfig(tesseract);
            this.image = image;
        }
    }

    public String getValue() {
        return value.get();
    }

    public SimpleStringProperty valueProperty() {
        return value;
    }

    public BufferedImage getImage() {
        return image;
    }

    public void performTraceAsync() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                String v = performTrace();
                Platform.runLater(() -> value.set(v));
            }
        }.start();
    }

    public String performTrace() {
        long start = System.currentTimeMillis();
        String v = performTraceSync();
        traceDuration = System.currentTimeMillis() - start;
        value.set(v);
        return v;
    }

    public long getTraceDuration() {
        return traceDuration;
    }

    private String performTraceSync() {
        try {
            String ret = tesseract.doOCR(image).replaceAll("\r?\n *", "").trim();
            return ret;
        } catch (TesseractException e) {
            return e.toString();
        }
    }
}
