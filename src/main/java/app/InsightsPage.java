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
            String country = context.queryParam("country");

            // Get dropdown data
            ArrayList<HashMap<String, String>> years = connection.getAllYears();
            ArrayList<HashMap<String, String>> antigens = connection.getAllAntigens();
            ArrayList<HashMap<String, String>> countries = connection.getAllCountries();

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

            // Extract country values for dropdown
            ArrayList<String> countryList = new ArrayList<>();
            for (HashMap<String, String> countryMap : countries) {
                countryList.add(countryMap.get("country"));
            }

            model.put("years", yearList);
            model.put("antigens", antigenList);
            model.put("countries", countryList);
            model.put("selectedStartYear", startYear);
            model.put("selectedEndYear", endYear);
            model.put("selectedAntigen", antigen);
            model.put("selectedCountry", country);

            // ================= YEAR RANGE VALIDATION =================
            // Validate that years are within acceptable range and logically consistent
            int minYear = 2000;
            int maxYear = 2024;
            boolean validYearRange = true;
            String warningMessage = null;

            // Only validate if both years are provided
            if (startYear != null && !startYear.isEmpty() && endYear != null && !endYear.isEmpty()) {
                try {
                    // Validate start year
                    int start = Integer.parseInt(startYear);
                    if (start < minYear || start > maxYear) {
                        validYearRange = false;
                        warningMessage = "Start year must be between " + minYear + " and " + maxYear + ".";
                    }

                    // Validate end year
                    int end = Integer.parseInt(endYear);
                    if (end < minYear || end > maxYear) {
                        validYearRange = false;
                        warningMessage = "End year must be between " + minYear + " and " + maxYear + ".";
                    }

                    // Validate that start year is not greater than end year
                    if (validYearRange && start > end) {
                        validYearRange = false;
                        warningMessage = "Start year cannot be greater than end year.";
                    }
                } catch (NumberFormatException e) {
                    validYearRange = false;
                    warningMessage = "Please enter valid numeric years.";
                }
            }

            // If year range is invalid, show warning and stop processing
            if (!validYearRange && warningMessage != null) {
                model.put("warning", warningMessage);
                model.put("hasData", false);
                model.put("geoData", new ArrayList<>());
                context.render(TEMPLATE, model);
                return; // Stop processing to prevent wrong data display
            }

            // Only query data if both years are selected and valid
            if (startYear != null && !startYear.isEmpty() && endYear != null && !endYear.isEmpty() && validYearRange) {
                // Get vaccination improvement data
                ArrayList<HashMap<String, String>> geoData = connection.getVaccinationImprovements(startYear, endYear, antigen, country);

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

                            // Show all countries including negative improvements
                            ArrayList<Object> dataRow = new ArrayList<>();
                            dataRow.add(escapedCountry);
                            dataRow.add(improvementValue); // Show actual value including negatives
                            chartData.add(dataRow);
                            count++;

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

        // Keep official names as-is (database format → Google format)
        countryMappings.put("Russian Federation", "Russia");
        countryMappings.put("Russia", "Russia");
        countryMappings.put("United States of America", "United States");
        countryMappings.put("United States", "United States");
        countryMappings.put("USA", "United States");
        countryMappings.put("United Kingdom of Great Britain and Northern Ireland", "United Kingdom");
        countryMappings.put("United Kingdom", "United Kingdom");
        countryMappings.put("UK", "United Kingdom");
        countryMappings.put("Korea, Republic of", "South Korea");
        countryMappings.put("South Korea", "South Korea");
        countryMappings.put("Korea, Democratic People's Republic of", "North Korea");
        countryMappings.put("North Korea", "North Korea");
        countryMappings.put("Iran, Islamic Republic of", "Iran");
        countryMappings.put("Iran", "Iran");
        countryMappings.put("Viet Nam", "Vietnam");
        countryMappings.put("Vietnam", "Vietnam");
        countryMappings.put("Bolivia, Plurinational State of", "Bolivia");
        countryMappings.put("Bolivia", "Bolivia");
        countryMappings.put("Venezuela, Bolivarian Republic of", "Venezuela");
        countryMappings.put("Venezuela", "Venezuela");
        countryMappings.put("Tanzania, United Republic of", "Tanzania");
        countryMappings.put("Tanzania", "Tanzania");
        countryMappings.put("Syrian Arab Republic", "Syria");
        countryMappings.put("Syria", "Syria");
        countryMappings.put("Moldova, Republic of", "Moldova");
        countryMappings.put("Moldova", "Moldova");
        countryMappings.put("Lao People's Democratic Republic", "Laos");
        countryMappings.put("Laos", "Laos");
        countryMappings.put("Congo, Democratic Republic of the", "Congo (Kinshasa)");
        countryMappings.put("Democratic Republic of Congo", "Congo (Kinshasa)");
        countryMappings.put("Congo", "Congo (Brazzaville)");
        countryMappings.put("Republic of Congo", "Congo (Brazzaville)");
        countryMappings.put("St. Vincent and Grenadines", "Saint Vincent and the Grenadines");
        countryMappings.put("St. Lucia", "Saint Lucia");
        countryMappings.put("St. Kitts and Nevis", "Saint Kitts and Nevis");
        countryMappings.put("Czechia", "Czech Republic");
        countryMappings.put("Czech Republic", "Czech Republic");
        countryMappings.put("Brunei Darussalam", "Brunei");
        countryMappings.put("Brunei", "Brunei");
        countryMappings.put("Côte d'Ivoire", "Ivory Coast");
        countryMappings.put("Ivory Coast", "Ivory Coast");

        return countryMappings.getOrDefault(countryName, countryName);
    }
}