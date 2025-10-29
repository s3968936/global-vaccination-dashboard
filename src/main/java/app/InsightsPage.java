package app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.javalin.http.Context;
import io.javalin.http.Handler;

public class InsightsPage implements Handler {

    public static final String URL = "/insights";
    private static final String TEMPLATE = "insights.html";

    private JDBCConnection connection;

    public InsightsPage(JDBCConnection connection) {
        this.connection = connection;
    }

    @Override
    public void handle(Context context) throws Exception {
        Map<String, Object> model = new HashMap<>();

        try {
            // Get vaccination coverage data for the geo chart
            ArrayList<HashMap<String, String>> geoData = connection.getTopVaccinationsByCoverage();
            
            System.out.println("Retrieved " + geoData.size() + " countries for geo chart");
            
            if (!geoData.isEmpty()) {
                System.out.println("First country: " + geoData.get(0).get("country_name") + 
                                 " - " + geoData.get(0).get("coverage_percentage") + "%");
            }

            // FIXED: Proper JSON formatting without manual string building
            ArrayList<ArrayList<Object>> chartData = new ArrayList<>();
            
            // Add header row
            ArrayList<Object> header = new ArrayList<>();
            header.add("Country");
            header.add("Coverage (%)");
            chartData.add(header);
            
            // Add data rows
            int count = 0;
            for (HashMap<String, String> row : geoData) {
                String countryName = row.get("country_name");
                String coverage = row.get("coverage_percentage");
                
                if (countryName != null && coverage != null && !coverage.isEmpty()) {
                    try {
                        String escapedCountry = escapeCountryName(countryName);
                        double coverageValue = Double.parseDouble(coverage);
                        
                        ArrayList<Object> dataRow = new ArrayList<>();
                        dataRow.add(escapedCountry);
                        dataRow.add(coverageValue);
                        chartData.add(dataRow);
                        
                        count++;
                        
                    } catch (NumberFormatException e) {
                        // Skip invalid coverage values
                        continue;
                    }
                }
            }
            
            // Convert to JSON for the geo chart
            String geoChartData = convertToJson(chartData);
            
            System.out.println("Final data has " + count + " valid countries");
            System.out.println("First few rows: " + geoChartData.substring(0, Math.min(200, geoChartData.length())) + "...");
            
            // Add all data to model
            model.put("geoChartData", geoChartData);
            model.put("geoData", geoData);
            model.put("hasData", count > 0);
            model.put("dataCount", count);

        } catch (Exception e) {
            e.printStackTrace();
            model.put("error", "Error loading geo chart data: " + e.getMessage());
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
                if (value instanceof String) {
                    // Properly escape strings for JSON
                    json.append("\"").append(escapeJsonString((String) value)).append("\"");
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