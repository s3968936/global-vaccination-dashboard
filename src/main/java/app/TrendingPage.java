package app;

import java.util.HashMap;
import java.util.Map;

import io.javalin.http.Context;
import io.javalin.http.Handler;

public class TrendingPage implements Handler {

    private JDBCConnection connection;

    public TrendingPage(JDBCConnection connection) {
        this.connection = connection;
    }

    public static final String URL = "/trending";
    private static final String TEMPLATE = "trending.html";

    @Override
    public void handle(Context context) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        // No data - just render the page
        context.render(TEMPLATE, model);
    }
}