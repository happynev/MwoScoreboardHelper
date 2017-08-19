package at.happynev.mwoscoreboardhelper;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.time.FastDateFormat;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Nev on 15.01.2017.
 */
public class GraphTabController {
    private static GraphTabController instance;

    @FXML
    VBox paneGraph;
    FastDateFormat fdfMatch = FastDateFormat.getInstance("yyyy-MM-dd HH:mm");

    public GraphTabController() {
        instance = this;
    }

    public static GraphTabController getInstance() {
        if (instance == null) {
            instance = new GraphTabController();
        }
        return instance;
    }

    @FXML
    private void initialize() throws SQLException {
        //defining the axes
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("timestamp");
        yAxis.setLabel("matches");
        xAxis.setTickLabelRotation(90);
        //creating the chart
        final BarChart<String, Number> fxChart = new BarChart<>(xAxis, yAxis);
        fxChart.setTitle("title 1234");
        //defining a series
        XYChart.Series series = new XYChart.Series();
        series.setName("My portfolio");
        ///////////////////
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        JFreeChart freechart = ChartFactory.createBarChart("matches/day", "timestamp", "matches", dataset);

        PreparedStatement prep = DbHandler.getInstance().prepareStatement("select count(*),to_char(matchtime,'YYYY-MM-DD')from match_data group by to_char(matchtime,'YYYY-MM-DD') order by to_char(matchtime,'YYYY-MM-DD')");
        ResultSet rs = prep.executeQuery();
        while (rs.next()) {
            int count = rs.getInt(1);
            String date = rs.getString(2);
            series.getData().add(new XYChart.Data(date, count));
            dataset.addValue(count, "matches", date);
        }
        rs.close();
        prep.close();
        ImageView iv = new ImageView(SwingFXUtils.toFXImage(freechart.createBufferedImage(1800, 500), null));

        fxChart.getData().add(series);
        paneGraph.getChildren().add(fxChart);
        paneGraph.getChildren().add(iv);
    }
}
