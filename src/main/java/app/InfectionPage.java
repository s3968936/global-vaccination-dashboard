package app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import app.model.InfectionData;
import io.javalin.http.Context;
import io.javalin.http.Handler;

public class InfectionPage implements Handler {

    private JDBCConnection connection;

    public InfectionPage(JDBCConnection connection) {
        this.connection = connection;
    }

    public static final String URL = "/infection";
    private static final String TEMPLATE = "infection.html";

    @Override
    public void handle(Context context) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("title", "Infection Data");
        
        try {
            // Get filter data from database
            ArrayList<String> infectionTypes = connection.getInfectionTypes();
            ArrayList<HashMap<String, String>> countries = connection.getAllCountries();
            ArrayList<HashMap<String, String>> years = connection.getAllYears();

            // Convert to simple lists for dropdowns
            ArrayList<String> countryList = new ArrayList<>();
            ArrayList<String> yearList = new ArrayList<>();

            

            for (HashMap<String, String> country : countries) {
                countryList.add(country.get("country"));
            }
            for (HashMap<String, String> year : years) {
                yearList.add(year.get("year"));
            }

            model.put("infectionTypes", infectionTypes);
            model.put("countries", countryList);
            model.put("years", yearList);

            // Get filter parameters from request
            String country = context.queryParam("country");
            String infectionType = context.queryParam("infectionType");
            String yearStart = context.queryParam("yearStart");
            String yearEnd = context.queryParam("yearEnd");

            // Check if filters are applied
            boolean hasFilters = (country != null && !country.isEmpty()) ||
                                (infectionType != null && !infectionType.isEmpty()) ||
                                (yearStart != null && !yearStart.isEmpty()) ||
                                (yearEnd != null && !yearEnd.isEmpty());

            model.put("hasFilters", hasFilters);

            if (hasFilters) {
                // Get filtered infection data - NOTE: parameter order is (infType, country, yearStart, yearEnd)
                ArrayList<InfectionData> infectionData = connection.getInfectionData(infectionType, country, yearStart, yearEnd);
                model.put("infectionData", infectionData);

                // Keep form selections
                model.put("selectedCountry", country);
                model.put("selectedInfectionType", infectionType);
                model.put("selectedYearStart", yearStart);
                model.put("selectedYearEnd", yearEnd);

                // Prepare chart data
                if (!infectionData.isEmpty()) {
                    StringBuilder chartDataJson = new StringBuilder("[");
                    int validDataCount = 0;

                    for (InfectionData data : infectionData) {
                        int year = data.getYear();
                        double cases = data.getCases();

                        // Only include valid cases
                        if (cases >= 0) {
                            if (validDataCount > 0) {
                                chartDataJson.append(",");
                            }

                            chartDataJson.append("[\"")
                                        .append(year)
                                        .append("\", ")
                                        .append(cases)
                                        .append("]");
                            validDataCount++;
                        }
                    }

                    chartDataJson.append("]");
                    model.put("chartDataJson", chartDataJson.toString());
                    model.put("hasChartData", validDataCount > 0);
                } else {
                    model.put("hasChartData", false);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            model.put("error", "Error loading infection data: " + e.getMessage());
        }

        context.render(TEMPLATE, model);
    }
}