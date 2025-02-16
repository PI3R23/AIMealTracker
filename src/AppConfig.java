import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AppConfig {
    private static final String CONFIG_PATH = System.getProperty("user.home") + File.separator + "AIMealTracker" + File.separator + ".AIMealTracker_config";

    public static boolean isFirstRun() {
        return !Files.exists(Paths.get(CONFIG_PATH));
    }

    public static void saveApiKey(String apiKey) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CONFIG_PATH))) {
            writer.write("API_KEY=" + apiKey);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String loadApiKey() {
        try (BufferedReader reader = new BufferedReader(new FileReader(CONFIG_PATH))) {
            String line = reader.readLine();
            if (line != null && line.startsWith("API_KEY=")) {
                return line.substring(8);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
