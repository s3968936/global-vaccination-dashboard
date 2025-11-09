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
            ArrayList<String> economicStatuses = connection.getEconomicStatuses();
            ArrayList<String> countries = connection.getCountries();
            ArrayList<String> years = connection.getYears();

            HashMap<String, ArrayList<String>> economicStatusCountries = connection.getEconomicStatusForCountry();

            model.put("infectionTypes", infectionTypes);
            model.put("economicStatuses", economicStatuses);
            model.put("countries", countries);
            model.put("years", years);
            model.put("economicStatusCountries", economicStatusCountries);

            // Get filter parameters from request
            String country = context.queryParam("country");
            String economicStatus = context.queryParam("economicStatus");
            String infectionType = context.queryParam("infectionType");
            String yearStart = context.queryParam("yearStart");
            String yearEnd = context.queryParam("yearEnd");

            // Year range validation
            int minYear = 2000;
            int maxYear = 2024;
            boolean validYearRange = true;
            String warningMessage = null;

            try {
                if (yearStart != null && !yearStart.isEmpty()) {
                    int start = Integer.parseInt(yearStart);
                    if (start < minYear || start > maxYear) {
                        validYearRange = false;
                        warningMessage = "Start year must be between " + minYear + " and " + maxYear + ".";
                    }
                }
                if (yearEnd != null && !yearEnd.isEmpty()) {
                    int end = Integer.parseInt(yearEnd);
                    if (end < minYear || end > maxYear) {
                        validYearRange = false;
                        warningMessage = "End year must be between " + minYear + " and " + maxYear + ".";
                    }
                }

                // Check if start year is before end year
                if (yearStart != null && !yearStart.isEmpty() && yearEnd != null && !yearEnd.isEmpty()) {
                    int start = Integer.parseInt(yearStart);
                    int end = Integer.parseInt(yearEnd);
                    if (start > end) {
                        validYearRange = false;
                        warningMessage = "Start year cannot be greater than end year.";
                    }
                }

                // Check if only end year is provided without start year
                if ((yearStart == null || yearStart.isEmpty()) && (yearEnd != null && !yearEnd.isEmpty())) {
                    validYearRange = false;
                    warningMessage = "Please enter a start year first before entering an end year.";
                }

            } catch (NumberFormatException e) {
                validYearRange = false;
                warningMessage = "Please enter valid numeric years.";
            }

            // If year range is invalid, show warning and stop processing
            if (!validYearRange) {
                model.put("warning", warningMessage);

                // Keep form selections even when there's a warning
                model.put("selectedCountry", country);
                model.put("selectedEconomicStatus", economicStatus);
                model.put("selectedInfectionType", infectionType);
                model.put("selectedYearStart", yearStart);
                model.put("selectedYearEnd", yearEnd);

                context.render(TEMPLATE, model);
                return; // Stop processing to prevent wrong data
            }

            // Determine chart title
            String chartTitle = determineChartTitle(country, economicStatus, infectionType);
            model.put("chartTitle", chartTitle);

            // Check if filters are applied
            boolean hasFilters = (country != null && !country.isEmpty()) ||
                    (economicStatus != null && !economicStatus.isEmpty()) ||
                    (infectionType != null && !infectionType.isEmpty()) ||
                    (yearStart != null && !yearStart.isEmpty()) ||
                    (yearEnd != null && !yearEnd.isEmpty());

            model.put("hasFilters", hasFilters);

            if (hasFilters) {
                // Get filtered infection data - NOW WITH 5 PARAMETERS
                ArrayList<InfectionData> infectionData = connection.getInfectionData(
                    infectionType, economicStatus, country, yearStart, yearEnd
                );
                model.put("infectionData", infectionData);

                // Keep form selections
                model.put("selectedCountry", country);
                model.put("selectedEconomicStatus", economicStatus);
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

                            // Create label with year and infection type as requested
                            String label = year + " - " + data.getInfType();
                            chartDataJson.append("[\"")
                                        .append(label)
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
                    model.put("chartDataJson", "[]");
                }
            } else {
                model.put("hasChartData", false);
                model.put("chartDataJson", "[]");
            }

        } catch (Exception e) {
            e.printStackTrace();
            model.put("error", "Error loading infection data: " + e.getMessage());
        }

        context.render(TEMPLATE, model);
    }

    private String determineChartTitle(String country, String economicStatus, String infectionType) {
        StringBuilder title = new StringBuilder("Infection Cases Over Time");

        if(country != null && !country.isEmpty()) {
            title.append(" in ").append(country);
        } else if(economicStatus != null && !economicStatus.isEmpty()) {
            title.append(" in ").append(economicStatus).append(" Countries");
        }

        if(infectionType != null && !infectionType.isEmpty()) {
            title.append(" for ").append(infectionType);
        }
        return title.toString();
    }
}