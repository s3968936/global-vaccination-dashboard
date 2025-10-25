package app;

import java.util.HashMap;
import java.util.Map;

import io.javalin.http.Context;
import io.javalin.http.Handler;

public class PrivacyPage implements Handler {

    public static final String URL = "/privacy";

    @Override
    public void handle(Context context) throws Exception {
        // Model can be used to pass a title to Thymeleaf
        Map<String, Object> model = new HashMap<>();
        model.put("title", "Privacy Statement");

        // Render the privacy template (no personas needed)
        context.render("privacy.html", model);
    }
}
