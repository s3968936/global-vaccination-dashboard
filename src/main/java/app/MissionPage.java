package app;

import app.model.Persona;
import io.javalin.http.Context;
import io.javalin.http.Handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MissionPage implements Handler {

    public static final String URL = "/mission";
    private final JDBCConnection connection;

    public MissionPage(JDBCConnection connection) {
        this.connection = connection;
    }

    @Override
    public void handle(Context context) throws Exception {
        Map<String, Object> model = new HashMap<>();
        model.put("title", "Mission Statement & Personas");

        try {
            // Fetch all personas as Persona objects
            ArrayList<Persona> personas = connection.getAllPersonas();

            // Pass to Thymeleaf
            model.put("personas", personas);

        } catch (Exception e) {
            e.printStackTrace();
            model.put("error", "Error loading personas: " + e.getMessage());
        }

        // Render the Thymeleaf template with model data
        context.render("mission.html", model);
    }
}
