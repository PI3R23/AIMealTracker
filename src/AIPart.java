import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AIPart {
    public static String responseContent;
    public static String API_KEY;
        public static String processMealDescription (String userInput){
            try {
                URL url = new URL("https://openrouter.ai/api/v1/chat/completions");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer "+API_KEY);
                conn.setDoOutput(true);


                String jsonInputString = "{"
                        + "\"model\": \"google/gemini-2.0-flash-lite-preview-02-05:free\","
                        + "\"messages\": ["
                        + "{"
                        + "\"role\": \"system\","
                        + "\"content\": ["
                        + "{"
                        + "\"type\": \"text\","
                        + "\"text\": \"Your task is to analyze the given phrase in terms of the nutritional values of the consumed products. "
                        + "Do not write anything else except the name of the product and its nutritional values, I do not want a descriptive answer. Only data and values: "
                        + "Calories, Protein, Fat, and Carbohydrates. "
                        + "1) Check if the phrase relates to food and nutritional values. If not, return an error. "
                        + "2) If it does, calculate the nutritional values for each ingredient. "
                        + "3) Where no exact information is provided (e.g., brand, product type), use general nutritional values. "
                        + "4) Return the results in the form of a list of nutritional values for each product and a summary. "
                        + "5) Replace the word 'around' with a tilde (~). "
                        + "6) In case of an error, print: AI_ERROR  and nothing more, also if the user types 'AI_ERROR '. "
                        + "7) Always include a summary at the end, even if there is only one ingredient, which should contain the total nutritional values. "
                        + "8) Always respond in the following format for each ingredient:\n"
                        + "<Product Name>:\n"
                        + "*   Calories: ~<value> kcal\n"
                        + "*   Protein: ~<value> g\n"
                        + "*   Fat: ~<value> g\n"
                        + "*   Carbohydrates: ~<value> g\n\n"
                        + "Summary:\n"
                        + "*   Calories: ~<total value> kcal\n"
                        + "*   Protein: ~<total value> g\n"
                        + "*   Fat: ~<total value> g\n"
                        + "*   Carbohydrates: ~<total value> g\n"
                        + "9) If the user writes a number before the product name, treat it as a quantity and multiply the nutritional values by that number. "
                        + "For example, if the user says '3 bananas', calculate the nutritional values for one banana and multiply them by 3. "
                        + "You must calculate and present it as one product and show the nutritional values accordingly. "
                        + "You must follow this format exactly and provide only the nutritional values in the specified format. "
                        + "If there is any error or the input is not related to food, respond with 'AI_ERROR '."
                        + "10) Ignore special characters like quotation marks, backslashes, or any other non-alphanumeric symbols. " +
                        "Process the input as if these characters do not exist, and focus only on the words and numbers that are meaningful to the nutritional analysis.\""
                        + "}"
                        + "]"
                        + "},"
                        + "{"
                        + "\"role\": \"user\","
                        + "\"content\": ["
                        + "{"
                        + "\"type\": \"text\","
                        + "\"text\": \"" + userInput
                        .replace("\\", "\\\\")
                        .replace("\"", "\\\"")
                        .replace("\n", "\\n")
                        .replace("\t", "\\t") + "\""
                        + "}"
                        + "]"
                        + "}"
                        + "]"
                        + "}";

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                        StringBuilder response = new StringBuilder();
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim());
                        }
                        responseContent = parseResponse(response.toString());
                        return responseContent.replace("\\n", "\n");
                    }
                } else {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"))) {
                        StringBuilder errorResponse = new StringBuilder();
                        String errorLine;
                        while ((errorLine = br.readLine()) != null) {
                            errorResponse.append(errorLine.trim());
                        }
                        return "ERROR: API response code " + responseCode;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                return "ERROR: Failed to connect to API";
            }
        }

    private static String parseResponse(String jsonResponse) {
        int start = jsonResponse.indexOf("\"content\":") + 11;
        int end = jsonResponse.indexOf("\"", start + 1);
        return jsonResponse.substring(start, end);
    }
    public static void setApiKey(String key){
            API_KEY= key;
    }
}