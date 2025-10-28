package app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import app.model.InfectionData;
import app.model.Persona;
import app.model.Vaccination;

/**
 * Class for Managing the JDBC Connection to a SQLite Database.
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
            connection = DriverManager.getConnection(DATABASE);
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);
            ResultSet resultSet = statement.executeQuery(query);

            int columnCount = resultSet.getMetaData().getColumnCount();

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
            System.err.println(e.getMessage());
        } finally {
            try {
                if (connection != null) connection.close();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }

        return results;
    }

    // ===========================
    // Dashboard Query Methods
    // ===========================
    public ArrayList<HashMap<String, String>> getAverageVaccinationCoverageByCountry() {
        String query = """
            SELECT 
                c.name AS country_name,
                ROUND(AVG(v.coverage), 2) AS avg_coverage
            FROM Vaccination v
            JOIN Country c ON v.country = c.CountryID
            WHERE v.coverage IS NOT NULL
            GROUP BY c.name
            HAVING avg_coverage > 0
            ORDER BY avg_coverage DESC
            LIMIT 10;
        """;
        return executeQuery(query);
    }

    public ArrayList<HashMap<String, String>> getTopVaccinationsByCoverage() {
        String query = """
            SELECT 
                c.name AS country_name,
                a.name AS vaccine_name,
                ROUND((v.doses / v.target_num) * 10, 2) AS coverage_percentage
            FROM Vaccination v
            JOIN Country c ON v.country = c.CountryID
            JOIN Antigen a ON v.antigen = a.AntigenID
            WHERE v.doses IS NOT NULL
              AND v.target_num IS NOT NULL
              AND v.target_num > 0
            ORDER BY coverage_percentage DESC
            LIMIT 5;
        """;
        return executeQuery(query);
    }

    public ArrayList<HashMap<String, String>> getEconomySnapshot() {
        String query = """
            SELECT 
                e.phase AS economy, 
                COUNT(c.CountryID) AS country_count, 
                AVG(v.coverage) AS avg_vaccination
            FROM Economy e
            JOIN Country c ON e.economyID = c.economy
            LEFT JOIN Vaccination v ON c.CountryID = v.country
            GROUP BY e.phase;
        """;
        return executeQuery(query);
    }

    public ArrayList<HashMap<String, String>> getRegions() {
        String query = """
            SELECT 
                r.region, 
                COUNT(c.CountryID) AS country_count
            FROM Region r
            LEFT JOIN Country c ON r.RegionID = c.region
            GROUP BY r.region
            ORDER BY country_count DESC
            LIMIT 5;
        """;
        return executeQuery(query);
    }

    public ArrayList<HashMap<String, String>> getTopInfections() {
        String query = """
            SELECT 
                it.description AS infection_type, 
                SUM(id.cases) AS total_cases
            FROM InfectionData id
            JOIN Infection_Type it ON id.inf_type = it.id
            GROUP BY it.description
            ORDER BY total_cases DESC
            LIMIT 10;
        """;
        return executeQuery(query);
    }

    // ===========================
    // Filter Dropdown Methods
    // ===========================
    public ArrayList<HashMap<String, String>> getAllCountries() {
        return executeQuery("SELECT DISTINCT name AS country FROM Country ORDER BY name;");
    }

    public ArrayList<HashMap<String, String>> getAllRegions() {
        return executeQuery("SELECT DISTINCT region AS region FROM Region ORDER BY region;");
    }

    public ArrayList<HashMap<String, String>> getAllAntigens() {
        return executeQuery("SELECT DISTINCT name AS antigen FROM Antigen ORDER BY name;");
    }

    public ArrayList<HashMap<String, String>> getAllYears() {
        return executeQuery("SELECT DISTINCT year AS year FROM Vaccination ORDER BY year;");
    }

    // ===========================
    // Personas
    // ===========================
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

    // This method updates the database from the feedback form
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

    // ===========================
    // Vaccination Data
    // ===========================
    public ArrayList<Vaccination> getVaccinationData(String country, String region, String antigen, String yearStart, String yearEnd) {
        ArrayList<Vaccination> results = new ArrayList<>();
        Connection connection = null;

        try {
            connection = DriverManager.getConnection(DATABASE);

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
            System.err.println(e.getMessage());
        } finally {
            try {
                if (connection != null) connection.close();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }

        return results;
    }

    // ===========================
    // Infection Data
    // ===========================
    public ArrayList<InfectionData> getInfectionData(String infType, String country, String yearStart, String yearEnd) {
        ArrayList<InfectionData> results = new ArrayList<>();
        Connection connection = null;

        try {
            connection = DriverManager.getConnection(DATABASE);

            String query = """
                SELECT 
                    it.description AS inf_type,
                    c.name AS country_name,
                    id.year AS year,
                    id.cases AS cases
                FROM InfectionData id
                JOIN Infection_Type it ON id.inf_type = it.id
                JOIN Country c ON id.country = c.CountryID
                WHERE 1=1
            """;

            if (infType != null && !infType.isEmpty()) {
                query += " AND it.description = '" + infType + "'";
            }
            if (country != null && !country.isEmpty()) {
                query += " AND c.name = '" + country + "'";
            }
            if (yearStart != null && !yearStart.isEmpty()) {
                query += " AND id.year >= " + yearStart;
            }
            if (yearEnd != null && !yearEnd.isEmpty()) {
                query += " AND id.year <= " + yearEnd;
            }

            query += " ORDER BY id.year;";

            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                String infectionType = resultSet.getString("inf_type");
                String countryName = resultSet.getString("country_name");
                int year = resultSet.getInt("year");
                double cases = resultSet.getDouble("cases");

                results.add(new InfectionData(infectionType, countryName, year, cases));
            }

            statement.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            try {
                if (connection != null) connection.close();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }

        return results;
    }

    // ===========================
    // Infection Type Dropdown
    // ===========================
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
            System.err.println(e.getMessage());
        } finally {
            try {
                if (connection != null) connection.close();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }

        return infectionTypes;
    }

    public String getMapDataJson() {
        StringBuilder json = new StringBuilder();
        json.append("[[\"Country\",\"Coverage\"]"); // header

        try (Connection conn = DriverManager.getConnection(DATABASE);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT c.name AS country_name, SUM(v.coverage) AS total_coverage " +
                "FROM Vaccination v " +
                "JOIN Country c ON v.country = c.CountryID " +
                "GROUP BY c.name " +
                "ORDER BY total_coverage DESC")) {

            while (rs.next()) {
                String country = rs.getString("country_name");
                double coverage = rs.getDouble("total_coverage");
                json.append(",[\"").append(country).append("\",").append(coverage).append("]");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        json.append("]");
        return json.toString();
    }

}
