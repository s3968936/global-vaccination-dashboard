package app;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.javalin.http.Context;
import io.javalin.http.Handler;

public class PageMoviesList implements Handler {

    // ✅ Properly declare the field
    private JDBCConnection connection;

    // ✅ Constructor
    public PageMoviesList(JDBCConnection connection) {
        this.connection = connection;
    }

    // ✅ Page URL
    public static final String URL = "/movies.html";

    // ✅ Template file
    private static final String TEMPLATE = "movies.html";

    @Override
    public void handle(Context context) throws Exception {
        Map<String, Object> model = new HashMap<>();

        model.put("title", "All Movies in the Database");

        // ✅ Use the injected connection
        ArrayList<Movie> movies = connection.getMovies();
        ArrayList<String> titles = new ArrayList<>();
        for (Movie movie : movies) {
            titles.add(movie.name);
        }

        model.put("movies", titles);

        context.render(TEMPLATE, model);
    }
}
