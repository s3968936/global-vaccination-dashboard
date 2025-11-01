package app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.javalin.http.Context;
import io.javalin.http.Handler;

public class InsightsPage implements Handler {

    public static final String URL = "/insights";
    private static final String TEMPLATE = "insights.html";

    final private JDBCConnection connection;

    public InsightsPage(JDBCConnection connection) {
        this.connection = connection;
    }

    @Override
    public void handle(Context context) throws Exception {
        Map<String, Object> model = new HashMap<>();

        try {
            // Get filter parameters from query string
            String startYear = context.queryParam("start_year");
            String endYear = context.queryParam("end_year");
            String antigen = context.queryParam("antigen");

            // Default to "All Vaccines" if not specified
            if (antigen == null || antigen.isEmpty()) {
                antigen = "All Vaccines";
            }

            // Get dropdown data
            ArrayList<HashMap<String, String>> years = connection.getAllYears();
            ArrayList<HashMap<String, String>> antigens = connection.getAllAntigens();

            // Extract year values for dropdown
            ArrayList<String> yearList = new ArrayList<>();
            for (HashMap<String, String> yearMap : years) {
                yearList.add(yearMap.get("year"));
            }

            // Extract antigen values for dropdown
            ArrayList<String> antigenList = new ArrayList<>();
            for (HashMap<String, String> antigenMap : antigens) {
                antigenList.add(antigenMap.get("antigen"));
            }

            model.put("years", yearList);
            model.put("antigens", antigenList);
            model.put("selectedStartYear", startYear);
            model.put("selectedEndYear", endYear);
            model.put("selectedAntigen", antigen);

            // Only query data if both years are selected
            if (startYear != null && !startYear.isEmpty() && endYear != null && !endYear.isEmpty()) {
                // Get vaccination improvement data
                ArrayList<HashMap<String, String>> geoData = connection.getVaccinationImprovements(startYear, endYear, antigen);

                // Build chart data
                ArrayList<ArrayList<Object>> chartData = new ArrayList<>();

                // Add header row
                ArrayList<Object> header = new ArrayList<>();
                header.add("Country");
                header.add("Improvement (% points)");
                chartData.add(header);

                // Add data rows
                int count = 0;
                for (HashMap<String, String> row : geoData) {
                    String countryName = row.get("country_name");
                    String improvement = row.get("improvement");

                    if (countryName != null && improvement != null && !improvement.isEmpty()) {
                        try {
                            String escapedCountry = escapeCountryName(countryName);
                            double improvementValue = Double.parseDouble(improvement);

                            // Only show countries with positive improvement or slight negative
                            if (improvementValue >= -10) {
                                ArrayList<Object> dataRow = new ArrayList<>();
                                dataRow.add(escapedCountry);
                                dataRow.add(Math.max(0, improvementValue)); // Show 0 for negative values on map
                                chartData.add(dataRow);
                                count++;
                            }

                        } catch (NumberFormatException e) {
                            // Skip invalid data
                        }
                    }
                }

                // Convert to JSON for the geo chart
                String geoChartData = convertToJson(chartData);

                // Add all data to model
                model.put("geoChartData", geoChartData);
                model.put("geoData", geoData);
                model.put("hasData", count > 0);
                model.put("dataCount", count);
            } else {
                // No filters selected yet
                model.put("hasData", false);
                model.put("geoData", new ArrayList<>());
            }

        } catch (Exception e) {
            model.put("error", "Error loading vaccination improvement data: " + e.getMessage());
            model.put("hasData", false);
        }

        // Render to the insights.html template
        context.render(TEMPLATE, model);
    }

    /**
     * Safe JSON conversion without manual string building
     */
    private String convertToJson(ArrayList<ArrayList<Object>> data) {
        StringBuilder json = new StringBuilder();
        json.append("[");
        
        for (int i = 0; i < data.size(); i++) {
            if (i > 0) json.append(",");
            json.append("[");
            
            ArrayList<Object> row = data.get(i);
            for (int j = 0; j < row.size(); j++) {
                if (j > 0) json.append(",");
                
                Object value = row.get(j);
                if (value instanceof String string) {
                    // Properly escape strings for JSON
                    json.append("\"").append(escapeJsonString(string)).append("\"");
                } else {
                    json.append(value);
                }
            }
            
            json.append("]");
        }
        
        json.append("]");
        return json.toString();
    }

    /**
     * Properly escape strings for JSON
     */
    private String escapeJsonString(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\b", "\\b")
                  .replace("\f", "\\f")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    /**
     * Helper method to handle country name variations for Google Geo Chart
     */
    private String escapeCountryName(String countryName) {
        if (countryName == null) return "";
        
        // Handle common country name variations for Google Geo Chart
        Map<String, String> countryMappings = new HashMap<>();
        countryMappings.put("United States", "United States");
        countryMappings.put("USA", "United States");
        countryMappings.put("United Kingdom", "United Kingdom");
        countryMappings.put("UK", "United Kingdom");
        countryMappings.put("Russia", "Russian Federation");
        countryMappings.put("South Korea", "Korea, Republic of");
        countryMappings.put("North Korea", "Korea, Democratic People's Republic of");
        countryMappings.put("Iran", "Iran, Islamic Republic of");
        countryMappings.put("Vietnam", "Viet Nam");
        countryMappings.put("Bolivia", "Bolivia, Plurinational State of");
        countryMappings.put("Venezuela", "Venezuela, Bolivarian Republic of");
        countryMappings.put("Tanzania", "Tanzania, United Republic of");
        countryMappings.put("Syria", "Syrian Arab Republic");
        countryMappings.put("Moldova", "Moldova, Republic of");
        countryMappings.put("Laos", "Lao People's Democratic Republic");
        countryMappings.put("Congo", "Congo");
        countryMappings.put("Democratic Republic of Congo", "Congo, The Democratic Republic of the");
        countryMappings.put("St. Vincent and Grenadines", "Saint Vincent and the Grenadines");
        countryMappings.put("St. Lucia", "Saint Lucia");
        countryMappings.put("St. Kitts and Nevis", "Saint Kitts and Nevis");
        
        return countryMappings.getOrDefault(countryName, countryName);
    }
}