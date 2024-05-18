package opencart;

import static io.restassured.RestAssured.given;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.asserts.SoftAssert;

import java.io.OutputStream;

public class Gemini {
	public static Map<String, String> aiDataGenerator() throws IOException {
		String apiKey = "";// your API key
		String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro-latest:generateContent";

		// Create the request body object
		RequestBody requestBody = new RequestBody();
		requestBody.getContents().add(new Content("user", new Part(
				"generate a list of Harry potter characters with their names and single worded corporate job roles in simple json")));
		requestBody.setGenerationConfig(new GenerationConfig(1, 0, 0.95, 8192, new String[] {}));
		requestBody.getSafetySettings().add(new SafetySetting("HARM_CATEGORY_HARASSMENT", "BLOCK_MEDIUM_AND_ABOVE"));
		requestBody.getSafetySettings().add(new SafetySetting("HARM_CATEGORY_HATE_SPEECH", "BLOCK_MEDIUM_AND_ABOVE"));
		requestBody.getSafetySettings()
				.add(new SafetySetting("HARM_CATEGORY_SEXUALLY_EXPLICIT", "BLOCK_MEDIUM_AND_ABOVE"));
		requestBody.getSafetySettings()
				.add(new SafetySetting("HARM_CATEGORY_DANGEROUS_CONTENT", "BLOCK_MEDIUM_AND_ABOVE"));

		// Convert the request body object to JSON
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		String requestBodyJson = mapper.writeValueAsString(requestBody);

		// Send the request
		URL requestUrl = new URL(url + "?key=" + apiKey);
		HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "application/json");
		connection.setDoOutput(true);

		try (OutputStream os = connection.getOutputStream()) {
			byte[] input = requestBodyJson.getBytes("utf-8");
			os.write(input, 0, input.length);
		}

		int responseCode = connection.getResponseCode();
		System.out.println("Response code: " + responseCode);

		// Read the response
		StringBuilder response = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
			String responseLine = null;
			while ((responseLine = br.readLine()) != null) {
				response.append(responseLine.trim());
			}
		}

		JsonPath jsonPath = new JsonPath(response.toString());

		String json = jsonPath.getString("candidates.content.parts.text");

		// Find the index of the first opening curly brace {
		int startIndex = json.indexOf("{");
		// Find the index of the last closing curly brace }
		int endIndex = json.lastIndexOf("}");

		if (startIndex != -1 && endIndex != -1) {
			// Extract the substring between the first opening and last closing curly braces
			json = json.substring(startIndex, endIndex + 1);
		}

		// Parse JSON string into a list of maps
		Map<String, String> dataList = mapper.readValue(json, Map.class);

		// Convert list of maps into JSON using Jackson
		String jsonArray = mapper.writeValueAsString(dataList);

		// Print the resulting map
		System.out.println(dataList);

		connection.disconnect();
		return dataList;
	}

	public static void main(String[] args) throws IOException, GeneralSecurityException, InterruptedException {

		SoftAssert assertion = new SoftAssert();
		System.setProperty("webdriver.gecko.driver",
				"D:\\eclipse\\workspace\\opencart\\src\\main\\resources\\geckodriver.exe");

		// Initialize browser
		WebDriver driver = new FirefoxDriver();

		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		driver.manage().timeouts().setScriptTimeout(30, TimeUnit.SECONDS);
		driver.manage().timeouts().pageLoadTimeout(40, TimeUnit.SECONDS);
		// WebDriverWait wait = new WebDriverWait(driver, 10);

		// Step 1: Generating the employee data
		Map<String, String> employeeData = Gemini.aiDataGenerator();

		driver.get("https://docs.google.com/spreadsheets/d/1SP_qfbkIEYf1P8DIV8d-yiDCXCFlUjzvzk0rjbnrTdQ/edit#gid=0");
		// Step 2: Passing the employee data to reqres.in and creating the users
		List<List<String>> users = new ArrayList<List<String>>();
		int count = 0;
		for (Map.Entry<String, String> entry : employeeData.entrySet()) {
			JsonPath path = Reqres.reqresPost(entry);
			users.add(new ArrayList<>());
			users.get(count).add(path.getString("name"));
			users.get(count).add(path.getString("job"));
			users.get(count).add(path.getString("id"));
			users.get(count).add(path.getString("createdAt"));
			count++;
		}

		// Step 3: updating the employee details sheet
		GoogleSheetsIntegration.sheetAppend(users);

		// Step 4: deleting all the users and updating the same in the sheet

		Map<String, String> seamus = new HashMap<String, String>();
		seamus.put("SEAMUS FINNIGAN", "GLAD YOU ASKED!!");
		System.out.println(seamus);
		List<List<String>> finnigan = new ArrayList<List<String>>();
		count = 0;
		for (Map.Entry<String, String> entry : seamus.entrySet()) {
			JsonPath path = Reqres.reqresPost(entry);
			finnigan.add(new ArrayList<>());
			finnigan.get(count).add(path.getString("name"));
			finnigan.get(count).add(path.getString("job"));
			finnigan.get(count).add(path.getString("id"));
			finnigan.get(count).add(path.getString("createdAt"));
			count++;
		}
		GoogleSheetsIntegration.sheetAppend(finnigan);

		for (int i = 0; i < users.size(); i++) {
			Reqres.reqresDelete(users.get(i).get(2));

		}
		Reqres.reqresDelete(finnigan.get(0).get(2));
		GoogleSheetsIntegration.deleteCells(users.size() + 1);
		driver.get("file:///C:/Users/Vinay/Downloads/finnigan.gif");//use your own gif
		Thread.sleep(3000);
		driver.get("https://docs.google.com/spreadsheets/d/1SP_qfbkIEYf1P8DIV8d-yiDCXCFlUjzvzk0rjbnrTdQ/edit#gid=0");
		Thread.sleep(3000);

		driver.quit();
	}
}

// POJO classes representing the request body
class RequestBody {
	@JsonProperty("contents")
	private java.util.List<Content> contents = new java.util.ArrayList<>();

	@JsonProperty("generationConfig")
	private GenerationConfig generationConfig; // This is the field

	@JsonProperty("safetySettings")
	private java.util.List<SafetySetting> safetySettings = new java.util.ArrayList<>();

	// Getter and setter for contents
	public java.util.List<Content> getContents() {
		return contents;
	}

	public void setContents(java.util.List<Content> contents) {
		this.contents = contents;
	}

	// Getter and setter for generationConfig
	public GenerationConfig getGenerationConfig() {
		return generationConfig;
	}

	public void setGenerationConfig(GenerationConfig generationConfig) {
		this.generationConfig = generationConfig;
	}

	// Getter and setter for safetySettings
	public java.util.List<SafetySetting> getSafetySettings() {
		return safetySettings;
	}

	public void setSafetySettings(java.util.List<SafetySetting> safetySettings) {
		this.safetySettings = safetySettings;
	}
}

class Content {
	@JsonProperty("role")
	private String role;

	@JsonProperty("parts")
	private java.util.List<Part> parts = new java.util.ArrayList<>();

	// Default constructor
	public Content() {
	}

	// Constructor with parameters
	public Content(String role, Part part) {
		this.role = role;
		this.parts.add(part);
	}

	// Getter and setter for role
	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	// Getter and setter for parts
	public java.util.List<Part> getParts() {
		return parts;
	}

	public void setParts(java.util.List<Part> parts) {
		this.parts = parts;
	}
}

class Part {
	@JsonProperty("text")
	private String text;

	// Default constructor
	public Part() {
	}

	// Constructor with parameter
	public Part(String text) {
		this.text = text;
	}

	// Getter and setter for text
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
}

class GenerationConfig {
	@JsonProperty("temperature")
	private int temperature;

	@JsonProperty("topK")
	private int topK;

	@JsonProperty("topP")
	private double topP;

	@JsonProperty("maxOutputTokens")
	private int maxOutputTokens;

	@JsonProperty("stopSequences")
	private String[] stopSequences;

	// Default constructor
	public GenerationConfig() {
	}

	// Constructor with parameters
	public GenerationConfig(int temperature, int topK, double topP, int maxOutputTokens, String[] stopSequences) {
		this.temperature = temperature;
		this.topK = topK;
		this.topP = topP;
		this.maxOutputTokens = maxOutputTokens;
		this.stopSequences = stopSequences;
	}

	// Getter and setter methods for all fields
	// Omitted for brevity
}

class SafetySetting {
	@JsonProperty("category")
	private String category;

	@JsonProperty("threshold")
	private String threshold;

	// Default constructor
	public SafetySetting() {
	}

	// Constructor with parameters
	public SafetySetting(String category, String threshold) {
		this.category = category;
		this.threshold = threshold;
	}

}
