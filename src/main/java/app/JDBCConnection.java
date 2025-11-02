package app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import app.model.InfectionData;
import app.model.Persona;
import app.model.Vaccination;

/**
 * Class for Managing the JDBC Connection to a SQLite Database.
 * Handles all database operations for the Global Health Dashboard.
 */
public class JDBCConnection {

    // Database connection path - points to the WHO health database
    private static final String DATABASE = "jdbc:sqlite:database/who.db";

    public JDBCConnection() {
    }

    /**
     * Generic method to execute any SQL query and return results as ArrayList<HashMap>
     * This is a reusable method that handles database connections and query execution
     */
    public ArrayList<HashMap<String, String>> executeQuery(String query) {
        ArrayList<HashMap<String, String>> results = new ArrayList<>();
        Connection connection = null;

        try {
            // Connect to JDBC database
            connection = DriverManager.getConnection(DATABASE);
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30); // Set 30-second timeout
            
            // Execute the query and get results
            ResultSet resultSet = statement.executeQuery(query);

            // Get metadata to know column names for dynamic result handling
            int columnCount = resultSet.getMetaData().getColumnCount();

            // Process all rows in the result set
            while (resultSet.next()) {
                HashMap<String, String> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = resultSet.getMetaData().getColumnName(i);
                    String columnValue = resultSet.getString(i);
                    row.put(columnName, columnValue);
                }
                results.add(row);
            }

            statement.close();
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        } finally {
            // Always close connection to prevent resource leaks
            try {
                if (connection != null) connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }

        return results;
    }

    // ===========================
    // Dashboard Query Methods
    // These methods provide data for the main dashboard visualizations
    // ===========================

    /**
     * Gets summary statistics for the dashboard highlight cards
     */
    public HashMap<String, String> getDashboardSummary() {
        HashMap<String, String> summary = new HashMap<>();
        Connection connection = null;

        try {
            connection = DriverManager.getConnection(DATABASE);
            Statement statement = connection.createStatement();

            // Get total countries
            ResultSet rs1 = statement.executeQuery("SELECT COUNT(DISTINCT CountryID) AS total FROM Country");
            if (rs1.next()) {
                summary.put("totalCountries", String.valueOf(rs1.getInt("total")));
            }

            // Get total regions
            ResultSet rs2 = statement.executeQuery("SELECT COUNT(DISTINCT RegionID) AS total FROM Region");
            if (rs2.next()) {
                summary.put("totalRegions", String.valueOf(rs2.getInt("total")));
            }

            // Get total vaccine types (Antigens)
            ResultSet rs3 = statement.executeQuery("SELECT COUNT(DISTINCT AntigenID) AS total FROM Antigen");
            if (rs3.next()) {
                summary.put("totalVaccines", String.valueOf(rs3.getInt("total")));
            }

            // Get total infection cases
            ResultSet rs4 = statement.executeQuery("SELECT SUM(cases) AS total FROM InfectionData");
            if (rs4.next()) {
                long totalCases = rs4.getLong("total");
                summary.put("totalInfectionCases", String.format("%,d", totalCases));
            }

            statement.close();
        } catch (SQLException e) {
            System.err.println("Error getting dashboard summary: " + e.getMessage());
        } finally {
            try {
                if (connection != null) connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }

        return summary;
    }


    /**
     * Gets vaccination rate improvements between two years for all countries
     * Shows which countries improved the most in vaccination coverage
     * @param startYear The starting year for comparison
     * @param endYear The ending year for comparison
     * @param antigen Optional antigen filter (null or empty for all antigens)
     * @return List of countries with their improvement metrics
     */
    public ArrayList<HashMap<String, String>> getVaccinationImprovements(String startYear, String endYear, String antigen, String country) {
        ArrayList<HashMap<String, String>> results = new ArrayList<>();
        Connection connection = null;

        try {
            connection = DriverManager.getConnection(DATABASE);

            // Build query to calculate improvements
            String query = """
                SELECT
                    c.name AS country_name,
                    ROUND(AVG(CASE WHEN v.year = ? THEN v.coverage END), 2) AS initial_coverage,
                    ROUND(AVG(CASE WHEN v.year = ? THEN v.coverage END), 2) AS final_coverage,
                    ROUND(AVG(CASE WHEN v.year = ? THEN v.coverage END) -
                          AVG(CASE WHEN v.year = ? THEN v.coverage END), 2) AS improvement
                FROM Vaccination v
                JOIN Country c ON v.country = c.CountryID
            """;

            // Add antigen filter if specified
            if (antigen != null && !antigen.isEmpty() && !antigen.equals("All Vaccines")) {
                query += " JOIN Antigen a ON v.antigen = a.AntigenID WHERE a.name = ?";
            } else {
                query += " WHERE 1=1";
            }

            // Add country filter if specified
            if (country != null && !country.isEmpty()) {
                query += " AND c.name = ?";
            }

            query += """
                GROUP BY c.name
                HAVING initial_coverage IS NOT NULL AND final_coverage IS NOT NULL
                ORDER BY improvement DESC;
            """;

            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, startYear);
            stmt.setString(2, endYear);
            stmt.setString(3, endYear);
            stmt.setString(4, startYear);

            int paramIndex = 5;
            if (antigen != null && !antigen.isEmpty() && !antigen.equals("All Vaccines")) {
                stmt.setString(paramIndex++, antigen);
            }
            if (country != null && !country.isEmpty()) {
                stmt.setString(paramIndex, country);
            }

            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                HashMap<String, String> row = new HashMap<>();
                row.put("country_name", resultSet.getString("country_name"));
                row.put("initial_coverage", resultSet.getString("initial_coverage"));
                row.put("final_coverage", resultSet.getString("final_coverage"));
                row.put("improvement", resultSet.getString("improvement"));
                results.add(row);
            }

            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error getting vaccination improvements: " + e.getMessage());
        } finally {
            try {
                if (connection != null) connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }

        return results;
    }

    /**
     * Gets top 5 countries by overall average vaccination coverage for snapshot display
     */
    public ArrayList<HashMap<String, String>> getVaccinationCoverage() {
        String query = """
        SELECT
            c.name AS country_name,
            ROUND(AVG(v.coverage), 2) AS coverage_percentage,
            COUNT(DISTINCT v.antigen) AS vaccine_count
        FROM Vaccination v
        JOIN Country c ON v.country = c.CountryID
        WHERE v.coverage IS NOT NULL
          AND v.coverage > 0
        GROUP BY c.name
        HAVING AVG(v.coverage) > 0
        ORDER BY coverage_percentage DESC
        LIMIT 5;
        """;
        return executeQuery(query);
    }

    /**
     * Gets economic snapshot data showing vaccination coverage by economic phase
     */
    public ArrayList<HashMap<String, String>> getEconomySnapshot() {
        String query = """
            SELECT
                e.phase AS economy,
                COUNT(DISTINCT c.CountryID) AS country_count,
                ROUND(AVG(v.coverage), 2) AS avg_vaccination
            FROM Economy e
            JOIN Country c ON e.economyID = c.economy
            LEFT JOIN Vaccination v ON c.CountryID = v.country
            GROUP BY e.phase
            ORDER BY e.phase;
        """;
        return executeQuery(query);
    }

    /**
     * Gets region data with vaccination coverage for snapshot display
     */
    public ArrayList<HashMap<String, String>> getRegions() {
        String query = """
            SELECT
                r.region,
                COUNT(DISTINCT c.CountryID) AS country_count,
                ROUND(AVG(v.coverage), 2) AS avg_coverage
            FROM Region r
            LEFT JOIN Country c ON r.RegionID = c.region
            LEFT JOIN Vaccination v ON c.CountryID = v.country
            WHERE v.coverage IS NOT NULL AND v.coverage > 0
            GROUP BY r.region
            ORDER BY avg_coverage DESC
            LIMIT 6;
        """;
        return executeQuery(query);
    }

    /**
     * Gets top infection types by total cases for snapshot display
     */
    public ArrayList<HashMap<String, String>> getTopInfections() {
        String query = """
            SELECT
                it.description AS infection_type,
                CAST(SUM(id.cases) AS INTEGER) AS total_cases
            FROM InfectionData id
            JOIN Infection_Type it ON id.inf_type = it.id
            GROUP BY it.description
            ORDER BY total_cases DESC
            LIMIT 10;
        """;
        ArrayList<HashMap<String, String>> results = executeQuery(query);

        // Format total_cases with commas
        DecimalFormat formatter = new DecimalFormat("#,###");
        for (HashMap<String, String> row : results) {
            String totalCases = row.get("total_cases");
            if (totalCases != null && !totalCases.isEmpty()) {
                try {
                    long cases = Long.parseLong(totalCases);
                    row.put("total_cases", formatter.format(cases));
                } catch (NumberFormatException e) {
                    // Keep original value if parsing fails
                }
            }
        }

        return results;
    }

    // ===========================
    // Filter Dropdown Methods
    // These methods populate the filter dropdowns in the explore data page
    // ===========================
    
    /**
     * Gets all unique countries for the country filter dropdown
     */
    public ArrayList<HashMap<String, String>> getAllCountries() {
        return executeQuery("SELECT DISTINCT name AS country FROM Country ORDER BY name;");
    }

    /**
     * Gets all unique regions for the region filter dropdown
     */
    public ArrayList<HashMap<String, String>> getAllRegions() {
        return executeQuery("SELECT DISTINCT region AS region FROM Region ORDER BY region;");
    }

    /**
     * Gets all unique antigens for the antigen filter dropdown
     */
    public ArrayList<HashMap<String, String>> getAllAntigens() {
        return executeQuery("SELECT DISTINCT name AS antigen FROM Antigen ORDER BY name;");
    }

    /**
     * Gets all unique years for the year filter dropdown
     */
    public ArrayList<HashMap<String, String>> getAllYears() {
        return executeQuery("SELECT DISTINCT year AS year FROM Vaccination ORDER BY year;");
    }

    // ===========================
    // Persona Methods
    // Handles user persona data for the mission statement page
    // ===========================
    
    /**
     * Gets all personas from the database for the mission statement page
     */
    public ArrayList<Persona> getAllPersonas() {
        ArrayList<Persona> list = new ArrayList<>();
        String query = "SELECT * FROM Personas ORDER BY persona_id;";

        try (Connection conn = DriverManager.getConnection(DATABASE);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Persona p = new Persona(
                    rs.getInt("persona_id"),
                    rs.getString("title"),
                    rs.getString("name"),
                    rs.getInt("age"),
                    rs.getString("occupation"),
                    rs.getString("education"),
                    rs.getString("location"),
                    rs.getString("language"),
                    rs.getString("disability"),
                    rs.getString("needs"),
                    rs.getString("goals"),
                    rs.getString("skills"),
                    rs.getString("image"),
                    rs.getString("image_credit")
                );
                list.add(p);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Executes database updates - used for feedback form submissions
     */
    public void executeUpdate(String sql, Object... params) {
        try (Connection conn = DriverManager.getConnection(DATABASE);
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Set parameters for prepared statement to prevent SQL injection
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ===========================
    // Vaccination Data Methods
    // Handles filtered vaccination data queries for the explore data page
    // ===========================
    
    /**
     * Gets filtered vaccination data based on user selections
     */
    public ArrayList<Vaccination> getVaccinationData(String country, String region, String antigen, String yearStart, String yearEnd) {
        ArrayList<Vaccination> results = new ArrayList<>();
        Connection connection = null;

        try {
            connection = DriverManager.getConnection(DATABASE);

            // Build dynamic SQL query based on filters
            String query = """
                SELECT 
                    c.name AS country_name,
                    r.region AS region_name,
                    a.name AS antigen_name,
                    v.year AS year,
                    v.coverage AS coverage,
                    v.target_num AS target_num,
                    v.doses AS doses
                FROM Vaccination v
                JOIN Country c ON v.country = c.CountryID
                JOIN Region r ON c.region = r.RegionID
                JOIN Antigen a ON v.antigen = a.AntigenID
                WHERE 1=1
            """;

            // Add filters to query based on user input
            if (country != null && !country.isEmpty()) {
                query += " AND c.name = '" + country + "'";
            }
            if (region != null && !region.isEmpty()) {
                query += " AND r.region = '" + region + "'";
            }
            if (antigen != null && !antigen.isEmpty()) {
                query += " AND a.name = '" + antigen + "'";
            }
            if (yearStart != null && !yearStart.isEmpty()) {
                query += " AND v.year >= " + yearStart;
            }
            if (yearEnd != null && !yearEnd.isEmpty()) {
                query += " AND v.year <= " + yearEnd;
            }

            query += " ORDER BY v.year;";

            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);
            ResultSet resultSet = statement.executeQuery(query);

            // Convert result set to Vaccination objects
            while (resultSet.next()) {
                String countryName = resultSet.getString("country_name");
                String antigenName = resultSet.getString("antigen_name");
                int year = resultSet.getInt("year");
                double targetNum = resultSet.getDouble("target_num");
                double doses = resultSet.getDouble("doses");
                double coverage = resultSet.getDouble("coverage");

                Vaccination vaccination = new Vaccination("", antigenName, countryName, year, targetNum, doses, coverage);
                results.add(vaccination);
            }

            statement.close();
        } catch (SQLException e) {
            System.err.println("Error getting vaccination data: " + e.getMessage());
        } finally {
            try {
                if (connection != null) connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }

        return results;
    }

    /**
     * Gets all region-country mappings for client-side filtering
     */
    public ArrayList<HashMap<String, String>> getRegionCountryMappings() {
        String query = "SELECT r.region AS region, c.name AS country " +
                    "FROM Country c " +
                    "JOIN Region r ON c.region = r.RegionID " +
                    "ORDER BY r.region, c.name";
        return executeQuery(query);
    }

    // ===========================
    // Infection Data Methods
    // Handles filtered infection data queries
    // ===========================

    /**
     * Gets filtered infection data based on user selections
     */
 public ArrayList<InfectionData> getInfectionData(String infType, String economicStatus, String country, String yearStart, String yearEnd) {
    ArrayList<InfectionData> results = new ArrayList<>();
    Connection connection = null;

    try {
        connection = DriverManager.getConnection(DATABASE);

        // Build dynamic SQL query for infection data - MATCHING YOUR SQL STRUCTURE
        String query = """
            SELECT
                c.name AS country,
                e.phase AS economic_status,
                it.description AS infection_type,
                yd.YearID AS year,
                id.cases AS cases
            FROM InfectionData id
            JOIN Country c ON id.country = c.CountryID
            JOIN Economy e ON c.economy = e.economyID
            JOIN Infection_Type it ON id.inf_type = it.id
            JOIN YearDate yd ON id.year = yd.YearID
            WHERE 1=1
        """;

        // Add filters to query - trim whitespace and handle case
        if (infType != null && !infType.trim().isEmpty()) {
            query += " AND TRIM(it.description) = '" + infType.trim() + "'";
        }
        if (economicStatus != null && !economicStatus.trim().isEmpty()) {
            query += " AND TRIM(e.phase) = '" + economicStatus.trim() + "'";
        }
        if (country != null && !country.trim().isEmpty()) {
            query += " AND TRIM(c.name) = '" + country.trim() + "'";
        }
        if (yearStart != null && !yearStart.trim().isEmpty()) {
            query += " AND yd.YearID >= " + yearStart.trim();
        }
        if (yearEnd != null && !yearEnd.trim().isEmpty()) {
            query += " AND yd.YearID <= " + yearEnd.trim();
        }

        query += " ORDER BY yd.YearID, id.cases DESC;";

        Statement statement = connection.createStatement();
        statement.setQueryTimeout(30);
        ResultSet resultSet = statement.executeQuery(query);

        // Convert result set to InfectionData objects
        while (resultSet.next()) {
            String countryName = resultSet.getString("country");
            String economicStatusResult = resultSet.getString("economic_status");
            String infectionType = resultSet.getString("infection_type");
            int year = resultSet.getInt("year");
            double cases = resultSet.getDouble("cases");

            // Use the constructor that includes economic status
            results.add(new InfectionData(infectionType, countryName, economicStatusResult, year, cases));
        }

        statement.close();
    } catch (SQLException e) {
        System.err.println("Error getting infection data: " + e.getMessage());
    } finally {
        try {
            if (connection != null) connection.close();
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }

    return results;
}

    /**
     * Gets all economic statuses for the dropdown filter
     */
    public ArrayList<String> getEconomicStatuses() {
        ArrayList<String> economicStatuses = new ArrayList<>();
        Connection connection = null;

        try {
            connection = DriverManager.getConnection(DATABASE);
            String query = "SELECT DISTINCT phase FROM Economy ORDER BY phase;";
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                economicStatuses.add(resultSet.getString("phase"));
            }

            statement.close();
        } catch (SQLException e) {
            System.err.println("Error getting economic statuses: " + e.getMessage());
        } finally {
            try {
                if (connection != null) connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }

        return economicStatuses;
    }

    /**
     * Gets all countries for the dropdown filter
     */
    public ArrayList<String> getCountries() {
        ArrayList<String> countries = new ArrayList<>();
        Connection connection = null;

        try {
            connection = DriverManager.getConnection(DATABASE);
            String query = "SELECT DISTINCT name FROM Country ORDER BY name;";
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                countries.add(resultSet.getString("name"));
            }

            statement.close();
        } catch (SQLException e) {
            System.err.println("Error getting countries: " + e.getMessage());
        } finally {
            try {
                if (connection != null) connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }

        return countries;
    }

        /**
     * Gets all available years from YearDate table
     */
    public ArrayList<String> getYears() {
        ArrayList<String> years = new ArrayList<>();
        Connection connection = null;

        try {
            connection = DriverManager.getConnection(DATABASE);
            String query = "SELECT DISTINCT YearID FROM YearDate ORDER BY YearID;";
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                years.add(String.valueOf(resultSet.getInt("YearID")));
            }

            statement.close();
        } catch (SQLException e) {
            System.err.println("Error getting years: " + e.getMessage());
        } finally {
            try {
                if (connection != null) connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }

        return years;
    }

    /**
     * Gets all infection types for the dropdown filter
     */
    public ArrayList<String> getInfectionTypes() {
        ArrayList<String> infectionTypes = new ArrayList<>();
        Connection connection = null;

        try {
            connection = DriverManager.getConnection(DATABASE);
            String query = "SELECT DISTINCT description FROM Infection_Type ORDER BY description;";
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                infectionTypes.add(resultSet.getString("description"));
            }

            statement.close();
        } catch (SQLException e) {
            System.err.println("Error getting infection types: " + e.getMessage());
        } finally {
            try {
                if (connection != null) connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }

        return infectionTypes;
    }

    /**
     * Gets aggregated infection data by economic status
     * Used for trending page to show summary by economic phase
     */
    public ArrayList<HashMap<String, String>> getInfectionDataByEconomicStatus(String infType, String yearStart, String yearEnd) {
        ArrayList<HashMap<String, String>> results = new ArrayList<>();
        Connection connection = null;

        try {
            connection = DriverManager.getConnection(DATABASE);

            String query = """
                SELECT
                    e.phase AS economic_status,
                    it.description AS infection_type,
                    COUNT(DISTINCT c.CountryID) AS country_count,
                    SUM(id.cases) AS total_cases,
                    AVG(id.cases) AS avg_cases
                FROM InfectionData id
                JOIN Country c ON id.country = c.CountryID
                JOIN Economy e ON c.economy = e.economyID
                JOIN Infection_Type it ON id.inf_type = it.id
                JOIN YearDate yd ON id.year = yd.YearID
                WHERE 1=1
            """;

            if (infType != null && !infType.trim().isEmpty()) {
                query += " AND TRIM(it.description) = '" + infType.trim() + "'";
            }
            if (yearStart != null && !yearStart.trim().isEmpty()) {
                query += " AND yd.YearID >= " + yearStart.trim();
            }
            if (yearEnd != null && !yearEnd.trim().isEmpty()) {
                query += " AND yd.YearID <= " + yearEnd.trim();
            }

            query += " GROUP BY e.phase, it.description ORDER BY total_cases DESC;";

            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);
            ResultSet resultSet = statement.executeQuery(query);

            DecimalFormat formatter = new DecimalFormat("#,###");

            while (resultSet.next()) {
                HashMap<String, String> row = new HashMap<>();
                row.put("economic_status", resultSet.getString("economic_status"));
                row.put("infection_type", resultSet.getString("infection_type"));
                row.put("country_count", String.valueOf(resultSet.getInt("country_count")));
                row.put("total_cases", formatter.format(resultSet.getDouble("total_cases")));
                row.put("avg_cases", formatter.format(resultSet.getDouble("avg_cases")));
                results.add(row);
            }

            statement.close();
        } catch (SQLException e) {
            System.err.println("Error getting aggregated infection data: " + e.getMessage());
        } finally {
            try {
                if (connection != null) connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }

        return results;
    }

    // ===========================
    // Feedback Methods
    // ===========================

    /**
     * Retrieves all feedback messages from the database
     * @return ArrayList of HashMaps containing feedback data (name, email, feedback, submitted_at)
     */
    public ArrayList<HashMap<String, String>> getAllFeedback() {
        ArrayList<HashMap<String, String>> results = new ArrayList<>();
        Connection connection = null;

        try {
            connection = DriverManager.getConnection(DATABASE);
            String query = "SELECT name, email, feedback, submitted_at FROM Feedback ORDER BY submitted_at DESC";
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                HashMap<String, String> row = new HashMap<>();
                row.put("name", resultSet.getString("name"));
                row.put("email", resultSet.getString("email"));
                row.put("feedback", resultSet.getString("feedback"));
                row.put("submitted_at", resultSet.getString("submitted_at"));
                results.add(row);
            }

            statement.close();
        } catch (SQLException e) {
            System.err.println("Error retrieving feedback: " + e.getMessage());
        } finally {
            try {
                if (connection != null) connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }

        return results;
    }
}