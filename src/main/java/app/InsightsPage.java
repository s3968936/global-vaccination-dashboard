package app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.javalin.http.Context;
import io.javalin.http.Handler;

public class InsightsPage implements Handler {

    public static final String URL = "/insights";
    private static final String TEMPLATE = "insights.html";

    private JDBCConnection connection;

    public InsightsPage(JDBCConnection connection) {
        this.connection = connection;
    }
    @Override
    public void handle(Context context) throws Exception {
        Map<String, Object> model = new HashMap<>();
        model.put("title", "Insights");

        // Fetch data
        ArrayList<HashMap<String, String>> mapData = connection.getAverageVaccinationCoverageByCountry();
        model.put("mapData", mapData);

        // ---- Convert to Google Charts JS array format ----
        StringBuilder chartDataJS = new StringBuilder();
        chartDataJS.append("[['Country', 'Coverage'],"); // header row

        for (HashMap<String, String> row : mapData) {
            String country = row.get("country_name");
            String coverage = row.get("avg_coverage");

            // escape quotes in names (for countries like Iran, Islamic Rep.)
            country = country.replace("'", "\\'");

            chartDataJS.append("['").append(country).append("', ").append(coverage).append("],");
        }

        // Remove trailing comma and close array
        if (chartDataJS.charAt(chartDataJS.length() - 1) == ',') {
            chartDataJS.setLength(chartDataJS.length() - 1);
        }
        chartDataJS.append("]");

        model.put("chartDataJS", chartDataJS.toString());

        // Render to the insights.html template
        context.render("insights.html", model);
    }

}
