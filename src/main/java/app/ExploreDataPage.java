package app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import app.model.Vaccination;

/**
 * Handler for the Explore Data page
 * Processes filter requests and displays vaccination data with dynamic chart titles
 */
public class ExploreDataPage implements Handler {

    private JDBCConnection connection;

    public ExploreDataPage(JDBCConnection connection) {
        this.connection = connection;
    }

    public static final String URL = "/explore";
    private static final String TEMPLATE = "exploredata.html";

    /**
     * Handles HTTP requests for the explore data page
     * Processes filters, validates input, and prepares data for display
     */
    @Override
    public void handle(Context context) throws Exception {
        Map<String, Object> model = new HashMap<>();

        try {
            // Get filter data from database for dropdowns
            ArrayList<HashMap<String, String>> countries = connection.getAllCountries();
            ArrayList<HashMap<String, String>> regions = connection.getAllRegions();
            ArrayList<HashMap<String, String>> antigens = connection.getAllAntigens();
            ArrayList<HashMap<String, String>> years = connection.getAllYears();

            // Convert to simple lists for Thymeleaf dropdowns
            ArrayList<String> countryList = new ArrayList<>();
            ArrayList<String> regionList = new ArrayList<>();
            ArrayList<String> antigenList = new ArrayList<>();
            ArrayList<String> yearList = new ArrayList<>();

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

            // Add lists to model for Thymeleaf template
            model.put("countries", countryList);
            model.put("regions", regionList);
            model.put("antigens", antigenList);
            model.put("years", yearList);

            // Get filter parameters from HTTP request
            String country = context.queryParam("country");
            String region = context.queryParam("region");
            String antigen = context.queryParam("antigen");
            String yearStart = context.queryParam("yearStart");
            String yearEnd = context.queryParam("yearEnd");

            // ================= YEAR RANGE VALIDATION =================
            // Validate that years are within acceptable range and logically consistent
            int minYear = 2000;
            int maxYear = 2024;
            boolean validYearRange = true;
            String warningMessage = null;

            try {
                // Validate start year
                if (yearStart != null && !yearStart.isEmpty()) {
                    int start = Integer.parseInt(yearStart);
                    if (start < minYear || start > maxYear) {
                        validYearRange = false;
                        warningMessage = "⚠️ Start year must be between " + minYear + " and " + maxYear + ".";
                    }
                }
                // Validate end year
                if (yearEnd != null && !yearEnd.isEmpty()) {
                    int end = Integer.parseInt(yearEnd);
                    if (end < minYear || end > maxYear) {
                        validYearRange = false;
                        warningMessage = "⚠️ End year must be between " + minYear + " and " + maxYear + ".";
                    }
                }
                
                // Validate that start year is not greater than end year
                if (yearStart != null && !yearStart.isEmpty() && yearEnd != null && !yearEnd.isEmpty()) {
                    int start = Integer.parseInt(yearStart);
                    int end = Integer.parseInt(yearEnd);
                    if (start > end) {
                        validYearRange = false;
                        warningMessage = "⚠️ Start year cannot be greater than end year.";
                    }
                }
            } catch (NumberFormatException e) {
                validYearRange = false;
                warningMessage = "⚠️ Please enter valid numeric years.";
            }

            // If year range is invalid, show warning and stop processing
            if (!validYearRange) {
                model.put("warning", warningMessage);
                
                // Keep form selections even when there's a warning for better UX
                model.put("selectedCountry", country);
                model.put("selectedRegion", region);
                model.put("selectedAntigen", antigen);
                model.put("selectedYearStart", yearStart);
                model.put("selectedYearEnd", yearEnd);
                
                context.render(TEMPLATE, model);
                return; // Stop processing to prevent wrong data display
            }

            // Dynamic country filtering - if region is selected, show only countries in that region
            if (region != null && !region.isEmpty()) {
                ArrayList<HashMap<String, String>> countriesInRegion = connection.getCountriesByRegion(region);
                countryList.clear();
                for (HashMap<String, String> countryMap : countriesInRegion) {
                    countryList.add(countryMap.get("country"));
                }
            }

            // Add updated lists to model (after potential filtering)
            model.put("countries", countryList);
            model.put("regions", regionList);
            model.put("antigens", antigenList);
            model.put("years", yearList);

            // Check if any filters are applied (not just initial page load)
            boolean hasFilters = (country != null && !country.isEmpty()) ||
                                 (region != null && !region.isEmpty()) ||
                                 (antigen != null && !antigen.isEmpty()) ||
                                 (yearStart != null && !yearStart.isEmpty()) ||
                                 (yearEnd != null && !yearEnd.isEmpty());

            model.put("hasFilters", hasFilters);

            // Process data only if filters are applied
            if (hasFilters) {
                // Get filtered vaccination data from database
                ArrayList<Vaccination> vaccinationData = connection.getVaccinationData(country, region, antigen, yearStart, yearEnd);
                model.put("vaccinationData", vaccinationData);

                            // Keep form selections for better user experience
                model.put("selectedCountry", country);
                model.put("selectedRegion", region);
                model.put("selectedAntigen", antigen);
                model.put("selectedYearStart", yearStart);
                model.put("selectedYearEnd", yearEnd);

                // Generate dynamic chart title based on selected filters
                String chartTitle = determineChartTitle(country, region, antigen);
                model.put("chartTitle", chartTitle);

                // Prepare chart data for JavaScript visualization
                if (!vaccinationData.isEmpty()) {
                    StringBuilder chartDataJson = new StringBuilder("[");
                    int validDataCount = 0;

                    // Convert vaccination data to JSON format for Google Charts
                    for (Vaccination data : vaccinationData) {
                        int year = data.getYear();
                        double coverage = data.getCoverage();

                        // Only include valid coverage data (non-negative)
                        if (coverage >= 0) {
                            if (validDataCount > 0) {
                                chartDataJson.append(",");
                            }

                            // Format as JSON array: ["year", coverage]
                            chartDataJson.append("[\"")
                                         .append(year)
                                         .append("\", ")
                                         .append(coverage)
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
            model.put("error", "Error loading data: " + e.getMessage());
        }

        // Render the template with all model data
        context.render(TEMPLATE, model);
    }

    /**
     * Generates a dynamic chart title based on the selected filters
     * Makes the chart more informative by showing what data is being displayed
     * 
     * @param country The selected country (can be null)
     * @param region The selected region (can be null)  
     * @param antigen The selected antigen/vaccine type (can be null)
     * @return A descriptive chart title like "Vaccination Coverage Over Time in France - Measles"
     */
    private String determineChartTitle(String country, String region, String antigen) {
        StringBuilder title = new StringBuilder("Vaccination Coverage Over Time");
        
        // Add location information (country or region)
        if (country != null && !country.isEmpty()) {
            title.append(" in ").append(country);
        } else if (region != null && !region.isEmpty()) {
            title.append(" in ").append(region).append(" Region");
        }
        
        // Add antigen/vaccine type information
        if (antigen != null && !antigen.isEmpty()) {
            title.append(" - ").append(antigen);
        }
        
        return title.toString();
    }
}
               