package app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.javalin.http.Context;
import io.javalin.http.Handler;

public class ViewFeedbackPage implements Handler {

    private JDBCConnection connection;
    public static final String URL = "/secret-feedback-view";
    private static final String TEMPLATE = "viewfeedback.html";

    public ViewFeedbackPage(JDBCConnection connection) {
        this.connection = connection;
    }

    @Override
    public void handle(Context ctx) throws Exception {
        Map<String, Object> model = new HashMap<>();
        model.put("title", "View Feedback Messages");

        // Retrieve all feedback from the database
        ArrayList<HashMap<String, String>> feedbackList = connection.getAllFeedback();
        model.put("feedbackList", feedbackList);
        model.put("feedbackCount", feedbackList.size());

        ctx.render(TEMPLATE, model);
    }
}
