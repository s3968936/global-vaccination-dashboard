package app;
import java.util.HashMap;
import java.util.Map;

import io.javalin.http.Context;
import io.javalin.http.Handler;

/**
 * Example Index HTML class using Javalin
 * <p>
 * Generate a static HTML page using Javalin
 * by writing the raw HTML into a Java String object
 *
 * @author Timothy Wiley, 2023. email: timothy.wiley@rmit.edu.au
 * @author Santha Sumanasekara, 2021. email: santha.sumanasekara@rmit.edu.au
 */
public class PageIndex implements Handler {

    private JDBCConnection connection;

    public PageIndex(JDBCConnection connection) {
        this.connection = connection;
    }

    // URL of this page relative to http://localhost:7001/
    public static final String URL = "/";

    // Name of the Thymeleaf HTML template page in the resources folder
    private static final String TEMPLATE = ("index.html");

    @Override
    public void handle(Context context) throws Exception {
        Map<String, Object> model = new HashMap<>();

        // Add page title
        model.put("title", "Global Health Dashboard");

        // Get summary statistics for highlight cards
        model.put("summary", connection.getDashboardSummary());

        // Get data for snapshots
        model.put("topVaccinations", connection.getVaccinationCoverage());
        model.put("economySnapshot", connection.getEconomySnapshot());
        model.put("improvedRegions", connection.getRegions());
        model.put("topInfections", connection.getTopInfections());

        context.render(TEMPLATE, model);
    }


}
