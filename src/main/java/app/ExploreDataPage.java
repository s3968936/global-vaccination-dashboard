package app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.javalin.http.Context;
import io.javalin.http.Handler;

public class ExploreDataPage implements Handler {

    private JDBCConnection connection;

    public ExploreDataPage(JDBCConnection connection) {
        this.connection = connection;
    }

    public static final String URL = "/explore";
    private static final String TEMPLATE = "exploredata.html";

    @Override
    public void handle(Context context) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        //Call the data from the database
        
        try {
            // Get filter data from database
            ArrayList<HashMap<String, String>> countries = connection.getAllCountries();
            ArrayList<HashMap<String, String>> regions = connection.getAllRegions();
            ArrayList<HashMap<String, String>> antigens = connection.getAllAntigens();
            ArrayList<HashMap<String, String>> years = connection.getAllYears();
            
            // Convert to simple lists for the HTML dropdowns
            ArrayList<String> countryList = new ArrayList<>();
            ArrayList<String> regionList = new ArrayList<>();
            ArrayList<String> antigenList = new ArrayList<>();
            ArrayList<String> yearList = new ArrayList<>();
            
            // Extract just the values from the HashMaps
            for (HashMap<String, String> country : countries) {
                countryList.add(country.get("country"));
            }
            for (HashMap<String, String> region : regions) {
                regionList.add(region.get("region"));
            }
            for (HashMap<String, String> antigen : antigens) {
                antigenList.add(antigen.get("antigen"));
            }
            for (HashMap<String, String> year : years) {
                yearList.add(year.get("year"));
            }
            
            // Add the lists to the model for Thymeleaf
            model.put("countries", countryList);
            model.put("regions", regionList);
            model.put("antigens", antigenList);
            model.put("years", yearList);
            
        } catch (Exception e) {
            e.printStackTrace();
            model.put("error", "Error loading filter data: " + e.getMessage());
        }

        context.render(TEMPLATE, model);
    }
}