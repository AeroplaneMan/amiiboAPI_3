import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;


public class amiiboAPI extends JFrame{
    private JLabel imageLabel;
    private static int width;
    private static int height;
    public amiiboAPI() {
        super("Skibidi");
        // Get user input
        Scanner scan = new Scanner(System.in);
        System.out.println("Use this link (https://amiibo.life/series) to find the amiibo you want data about\n");
        System.out.println("Please enter an amiibo series (for Dark Souls and Mario Cereal enter \"Others\"):");
        String series = scan.nextLine();
        System.out.println("Please enter the exact name of an amiibo from that series: ");
        String name = scan.nextLine();

        // Initialize components
        imageLabel = new JLabel();
        add(imageLabel, BorderLayout.CENTER);

        // Get amiibo data and display image
        String amiiboData = getAmiiboInfo(series, name);
        String formattedAmiiboData = formatAmiiboData(amiiboData);
        System.out.println(formattedAmiiboData);
        displayAmiiboImage(series, name);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(width + 15, height + 30);
        setLocationRelativeTo(null);
    }



    private void displayAmiiboImage(String series, String name) {
        String amiiboData = getAmiiboInfo(series, name);
        String imageUrl = parseImageUrl(amiiboData);
        if (imageUrl != null) {
            try {
                Image image = new ImageIcon(new URL(imageUrl)).getImage();
                width = image.getWidth(null);
                height = image.getHeight(null);
                imageLabel.setIcon(new ImageIcon(image));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getAmiiboInfo(String series, String name){
        try {
            // Encode the series name
            String encodedSeries = URLEncoder.encode(series, "UTF-8");

            // Encode the amiibo name
            String encodedName = URLEncoder.encode(name, "UTF-8");

            // Constructing the url for the API request
            String url = "https://www.amiiboapi.com/api/amiibo/?amiiboSeries=" + encodedSeries + "&name=" + encodedName;

            // Create an HTTP client object, so we can send a request
            HttpClient client = HttpClient.newHttpClient();

            // Build an HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .build();

            // Send the request to the API, and get a response
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // If there's an issue, check that response.statusCode() returns a 200
            return response.body();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
    public static String parseImageUrl(String json) {
        // Parse the input JSON string to a JsonObject
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();

        // Extract the "amiibo" array from the JsonObject
        JsonArray amiiboArray = jsonObject.getAsJsonArray("amiibo");

        // Check if the "amiibo" array contains at least one element
        if (!amiiboArray.isEmpty()) {
            // Get the first object in the "amiibo" array
            JsonObject amiiboObject = amiiboArray.get(0).getAsJsonObject();

            // Return the value associated with the "image" key in the first amiibo object
            return amiiboObject.get("image").getAsString();
        }
        // Return null if the "amiibo" array is empty
        System.out.println("Amiibo not found.");
        return null;
    }

    public static String formatAmiiboData(String json) {
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
        JsonArray amiiboArray = jsonObject.getAsJsonArray("amiibo");

        StringBuilder formattedData = new StringBuilder();
        System.out.println(formattedData);

        for (int i = 0; i < amiiboArray.size(); i++) {
            JsonObject amiiboObject = amiiboArray.get(i).getAsJsonObject();

            String amiiboSeries = getStringOrEmpty(amiiboObject, "amiiboSeries");
            String character = getStringOrEmpty(amiiboObject, "character");
            String gameSeries = getStringOrEmpty(amiiboObject, "gameSeries");
            String name = getStringOrEmpty(amiiboObject, "name");
            String image = getStringOrEmpty(amiiboObject, "image");
            String tail = getStringOrEmpty(amiiboObject, "tail");
            String type = getStringOrEmpty(amiiboObject, "type");

            JsonObject release = amiiboObject.getAsJsonObject("release");
            String releaseDates = String.format("AU: %s, EU: %s, JP: %s, NA: %s",
                    getStringOrEmpty(release, "au"),
                    getStringOrEmpty(release, "eu"),
                    getStringOrEmpty(release, "jp"),
                    getStringOrEmpty(release, "na"));

            formattedData.append(String.format(
                    "%nAmiibo Series: %s%nCharacter: %s%nGame Series: %s%nName: %s%nImage URL: %s%nRelease Dates: %s%nTail: %s%nType: %s%n",
                    amiiboSeries, character, gameSeries, name, image, releaseDates, tail, type
            ));
        }

        return formattedData.toString();
    }

    private static String getStringOrEmpty(JsonObject jsonObject, String memberName) {
        JsonElement jsonElement = jsonObject.get(memberName);
        return jsonElement != null && !jsonElement.isJsonNull() ? jsonElement.getAsString() : "";
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new amiiboAPI().setVisible(true));
    } // main() method closing
}
