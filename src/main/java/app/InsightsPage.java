package app;

import java.util.HashMap;
import java.util.Map;

import io.javalin.http.Context;
import io.javalin.http.Handler;

public class InsightsPage implements Handler {

    private final JDBCConnection connection;

    public InsightsPage(JDBCConnection connection) {
        this.connection = connection;
    }

    public static final String URL = "/insights";
    private static final String TEMPLATE = "insights.html";

    @Override
    public void handle(Context context) throws Exception {
        Map<String, Object> model = new HashMap<>();

        // Add page title
        model.put("title", "Insights");

        try {
            // Fetch map data for GeoChart
            model.put("mapDataJson", connection.getMapDataJson());
        } catch (Exception e) {
            e.printStackTrace();
            model.put("error", "Error loading map data: " + e.getMessage());
        }

        // Render the Thymeleaf template
        context.render(TEMPLATE, model);
    }
}
