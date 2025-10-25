package app;

import java.util.HashMap;
import java.util.Map;

import io.javalin.http.Context;
import io.javalin.http.Handler;

public class Feedback implements Handler {

    private JDBCConnection connection;
    public static final String URL = "/feedback";
    private static final String TEMPLATE = "feedback.html";

    public Feedback(JDBCConnection connection) {
        this.connection = connection;
    }

    @Override
    public void handle(Context ctx) throws Exception {

        Map<String, Object> model = new HashMap<>();
        model.put("title", "Feedback");

        if (ctx.method().equalsIgnoreCase("POST")) {
            // --- Handle form submission ---
            String name = ctx.formParam("name");
            String email = ctx.formParam("email");
            String feedback = ctx.formParam("feedback");

            if (name == null || email == null || feedback == null ||
                name.isEmpty() || email.isEmpty() || feedback.isEmpty()) {
                model.put("error", "All fields are required!");
            } else {
                // Insert feedback into the database
                String sql = "INSERT INTO Feedback (name, email, feedback) VALUES (?, ?, ?)";
                connection.executeUpdate(sql, name, email, feedback);

                // Set success flags for the page
                model.put("submitted", true);
                model.put("name", name);
            }
        }

        // Render the page (GET or POST)
        ctx.render(TEMPLATE, model);
    }
}
