import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Database {
    static String url = "jdbc:sqlite:"+ System.getProperty("user.home") + File.separator + "AIMealTracker" + File.separator + "database.db";

    public Database() {
    }

    public static void databaseSetup() {
        String userHome = System.getProperty("user.home");
        String dbDir = userHome + File.separator + "AIMealTracker";
        String dbPath = userHome + File.separator + "database.db";

        File directory = new File(dbDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        File dbFile = new File(dbPath);
        boolean isNewDatabase = !dbFile.exists();

        Statement statement = null;
        try {
            if (isNewDatabase) {
                Connection connection = DriverManager.getConnection(url);
                statement = connection.createStatement();
                statement.execute("CREATE TABLE IF NOT EXISTS meals (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "date TEXT," +
                        "name TEXT," +
                        "description TEXT);");
                statement.close();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public static void insertMeal(String date, String name, String description) {
        String sql = "INSERT INTO meals (date, name, description) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, date);
            pstmt.setString(2, name);
            pstmt.setString(3, description);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteMeal(int mealId) {
        String sql = "DELETE FROM meals WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, mealId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Map<String, String>> getMealsByDate(String date) {
        String sql = "SELECT id, name, description FROM meals WHERE date = ?";
        List<Map<String, String>> meals = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, date);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, String> meal = new HashMap<>();
                meal.put("id", String.valueOf(rs.getInt("id")));
                meal.put("name", rs.getString("name"));
                meal.put("description", rs.getString("description"));
                meals.add(meal);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return meals;
    }

    public static Map<String, Double> getMealSummaryByDate(String date) {
        String sql = "SELECT description FROM meals WHERE date = ?";
        Map<String, Double> mealSummary = new HashMap<>();
        mealSummary.put("totalCalories", 0.0);
        mealSummary.put("totalProtein", 0.0);
        mealSummary.put("totalFat", 0.0);
        mealSummary.put("totalCarbs", 0.0);

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, date);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String description = rs.getString("description");
                if (description != null && description.contains("Summary:")) {
                    extractAndSumNutrients(description, mealSummary);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return mealSummary;
    }

    private static void extractAndSumNutrients(String description, Map<String, Double> mealSummary) {
        String[] lines = description.split("\n");
        boolean summarySection = false;

        for (String line : lines) {
            if (line.contains("Summary:")) {
                summarySection = true;
                continue;
            }
            if (summarySection) {
                if (line.contains("Calories:")) {
                    mealSummary.put("totalCalories", mealSummary.get("totalCalories") + extractNumber(line));
                } else if (line.contains("Protein:")) {
                    mealSummary.put("totalProtein", mealSummary.get("totalProtein") + extractNumber(line));
                } else if (line.contains("Fat:")) {
                    mealSummary.put("totalFat", mealSummary.get("totalFat") + extractNumber(line));
                } else if (line.contains("Carbohydrates:")) {
                    mealSummary.put("totalCarbs", mealSummary.get("totalCarbs") + extractNumber(line));
                }
            }
        }
    }

    private static double extractNumber(String line) {
        String number = line.replaceAll("[^0-9.]", "");
        return number.isEmpty() ? 0.0 : Double.parseDouble(number);
    }

}