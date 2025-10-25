package app;

import io.javalin.Javalin;
import io.javalin.core.util.RouteOverviewPlugin;


/**
 * Main Application Class.
 * <p>
 * Running this class as regular java application will start the 
 * Javalin HTTP Server and our web application.
 *
 * @author Timothy Wiley, 2023. email: timothy.wiley@rmit.edu.au
 * @author Santha Sumanasekara, 2021. email: santha.sumanasekara@rmit.edu.au
 */
public class App {

    public static final int         JAVALIN_PORT    = 7001;
    public static final String      CSS_DIR         = "css/";
    public static final String      IMAGES_DIR      = "images/";

    public static JDBCConnection connection;


    public static void main(String[] args) {

        connection = new JDBCConnection();

        // Create our HTTP server and listen in port 7001
        Javalin app = Javalin.create(config -> {
            config.registerPlugin(new RouteOverviewPlugin("/help/routes"));
            
            // Uncomment this if you have files in the CSS Directory
            config.addStaticFiles(CSS_DIR);

            // Uncomment this if you have files in the Images Directory
            config.addStaticFiles(IMAGES_DIR);
        }).start(JAVALIN_PORT);


        // Configure Web Routes
        configureRoutes(app);
    }

    public static void configureRoutes(Javalin app) {
        // Note in this example we must add Movies Type as a GET and a POST!
        
        // ADD ALL OF YOUR WEBPAGES HERE
        app.get(PageIndex.URL, new PageIndex(connection));
        app.get(ExploreDataPage.URL, new ExploreDataPage(connection));
        app.get(TrendingPage.URL, new TrendingPage(connection));
        app.get(InsightsPage.URL, new InsightsPage(connection));
        app.get(InfectionPage.URL,  new InfectionPage(connection));
        app.get(MissionPage.URL, new MissionPage(connection));
        

        // POST pages can accept form data
         app.post(ExploreDataPage.URL, new ExploreDataPage(connection));
         app.post(ExploreDataPage.URL, new ExploreDataPage(connection));
    } 

}