package app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import app.model.Persona;

/**
 * Class for Managing the JDBC Connection to a SQLLite Database.
 */
public class JDBCConnection {

    // Update this to your actual database path
    private static final String DATABASE = "jdbc:sqlite:database/who.db";

    public JDBCConnection() {
        System.out.println("Created JDBC Connection Object");
    }

    /**
     * Generic method to execute any SQL query and return results as ArrayList<HashMap>
     */
    public ArrayList<HashMap<String, String>> executeQuery(String query) {
        ArrayList<HashMap<String, String>> results = new ArrayList<>();
        Connection connection = null;

        try {
            // Connect to JDBC data base
            connection = DriverManager.getConnection(DATABASE);

            // Prepare a new SQL Query & Set a timeout
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);

            // Get Result
            ResultSet resultSet = statement.executeQuery(query);

            // Get metadata to know column names
            int columnCount = resultSet.getMetaData().getColumnCount();

            // Process all of the results
            while (resultSet.next()) {
                HashMap<String, String> row = new HashMap<>();
                
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = resultSet.getMetaData().getColumnName(i);
                    String columnValue = resultSet.getString(i);
                    row.put(columnName, columnValue);
                }
                
                results.add(row);
            }

            // Close the statement
            statement.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            // Safety code to cleanup
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }

        return results;
    }

    // Specific methods for your queries (optional - you can use executeQuery directly)
    
    public ArrayList<HashMap<String, String>> getTopVaccinationsByCoverage() {
        String query = "SELECT " +
                    "c.name as country_name, " +
                    "a.name as vaccine_name, " +
                    "ROUND((v.doses / v.target_num) * 10, 2) as coverage_percentage " +
                    "FROM Vaccination v " +
                    "JOIN Country c ON v.country = c.CountryID " +
                    "JOIN Antigen a ON v.antigen = a.AntigenID " +
                    "WHERE v.doses IS NOT NULL " +
                    "  AND v.target_num IS NOT NULL " +
                    "  AND v.target_num > 0 " +
                    "ORDER BY coverage_percentage DESC " +
                    "LIMIT 5";
        
        return executeQuery(query);
    }

    public ArrayList<HashMap<String, String>> getEconomySnapshot() {
        String query = "SELECT e.phase AS economy, COUNT(c.CountryID) AS country_count, " +
                      "AVG(v.coverage) AS avg_vaccination " +
                      "FROM Economy e " +
                      "JOIN Country c ON e.economyID = c.economy " +
                      "LEFT JOIN Vaccination v ON c.CountryID = v.country " +
                      "GROUP BY e.phase";
        return executeQuery(query);
    }

    public ArrayList<HashMap<String, String>> getRegions() {
        String query = "SELECT r.region, COUNT(c.CountryID) as country_count " +
                    "FROM Region r " +
                    "LEFT JOIN Country c ON r.RegionID = c.region " +
                    "GROUP BY r.region " +
                    "ORDER BY country_count DESC " +
                    "LIMIT 5";
        return executeQuery(query);
    }

    public ArrayList<HashMap<String, String>> getTopInfections() {
        String query = "SELECT it.description AS infection_type, SUM(id.cases) AS total_cases " +
                      "FROM InfectionData id " +
                      "JOIN Infection_Type it ON id.inf_type = it.id " +
                      "GROUP BY it.description " +
                      "ORDER BY total_cases DESC LIMIT 10";
        return executeQuery(query);
    }
// FILTER DROPDOWN METHODS FOR EXPLOREDATA PAGE (VACCINATION)
    // Get all unique countries for the country filter dropdown
    public ArrayList<HashMap<String, String>> getAllCountries() {
        String query = "SELECT DISTINCT name as country FROM Country ORDER BY name";
        return executeQuery(query);
    }

    //Get all unique regions for the region filter dropdown
    public ArrayList<HashMap<String, String>> getAllRegions() {
        String query = "SELECT DISTINCT region as region FROM Region ORDER BY region";
        return executeQuery(query);
    }

    //Get all unique antigens for the antigen filter dropdown
    public ArrayList<HashMap<String, String>> getAllAntigens() {
        String query = "SELECT DISTINCT name as antigen FROM Antigen ORDER BY name";
        return executeQuery(query);
    }

    //Get all unique years for the year filter dropdown
    public ArrayList<HashMap<String, String>> getAllYears() {
        String query = "SELECT DISTINCT year as year FROM Vaccination ORDER BY year";
        return executeQuery(query);
    }

    //Get all the fields from the persona table in the database
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
    
    public void executeUpdate(String sql, Object... params) {
        try (Connection conn = DriverManager.getConnection(DATABASE);
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Set parameters
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}