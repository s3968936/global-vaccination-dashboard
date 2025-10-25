package app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.javalin.http.Context;
import io.javalin.http.Handler;

public class MissionPage implements Handler {

    public static final String URL = "/mission";
    private final JDBCConnection connection;

    public MissionPage(JDBCConnection connection) {
        this.connection = connection;
    }

    @Override
    public void handle(Context context) throws Exception {
        Map<String, Object> model = new HashMap<>();

        // Page title for mission.html
        model.put("title", "Mission Statement & Personas");

        try {
            // Query personas from database
            ArrayList<HashMap<String, String>> personas = connection.getAllPersonas();
            model.put("personas", personas);
        } catch (Exception e) {
            e.printStackTrace();
            model.put("error", "Error loading personas: " + e.getMessage());
        }

        // Render the Thymeleaf template with model data
        context.render("mission.html", model);
    }
}
