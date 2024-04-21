package opencart;

import static io.restassured.RestAssured.given;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import org.testng.collections.Lists;
import static io.restassured.RestAssured.*;

import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.http.ContentType;
import io.restassured.internal.common.assertion.Assertion;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;

public class APIUICookieIntegration {

	private static RemoteWebDriver driver;

	public static void main(String[] args) throws IOException, InterruptedException {
		SoftAssert assertion = new SoftAssert();
		System.setProperty("webdriver.gecko.driver",
				"D:\\eclipse\\workspace\\opencart\\src\\main\\resources\\geckodriver.exe");

		// Initialize browser
		WebDriver driver = new FirefoxDriver();

		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		driver.manage().timeouts().setScriptTimeout(30, TimeUnit.SECONDS);
		driver.manage().timeouts().pageLoadTimeout(40, TimeUnit.SECONDS);
		WebDriverWait wait = new WebDriverWait(driver, 10);
		driver.manage().window().maximize();

		// visiting the login page and verifying it
		driver.get("https://opensource-demo.orangehrmlive.com/web/index.php/auth/login");
		driver.findElement(By.name("username")).sendKeys("Admin");
		driver.findElement(By.name("password")).sendKeys("admin123");
		driver.findElement(By.xpath("//button[@type='submit']")).click();
		waitTillPageLoad(wait);

		// extracting session data from cookie to trigger API requests
		String sessionCookie = "";
		try {
			Cookie session = driver.manage().getCookieNamed("orangehrm");
			System.out.println(session.getName() + "=" + session.getValue());
			sessionCookie = session.getName() + "=" + session.getValue();
		} catch (Exception e) {
			// In case we fail to extract session data from the cookie
			e.printStackTrace();
		}

		driver.get("https://opensource-demo.orangehrmlive.com/web/index.php/leave/viewMyLeaveList");
		// basic arraylist to store the ID's of all the leaves
		List<String> leaveIDs = new ArrayList<String>();
		int dateCount = 0;

		// creating a leave request
		while (getLeaveBalance(sessionCookie) > 1) {
			// int count= balance.getLeaveBalance(session);

			dateCount++;
			String formattedNumber = String.format("%02d", dateCount);
			Response postResponse = given().header("Content-Type", "application/json").header("cookie", sessionCookie)
					// Please do make changes to the year and month accordingly
					.body("{\r\n" + "    \"leaveTypeId\": 7,\r\n" + "    \"fromDate\": \"2024-04-" + formattedNumber
							+ "\",\r\n" + "    \"toDate\": \"2024-04-" + formattedNumber + "\",\r\n"
							+ "    \"comment\": \"test\",\r\n" + "    \"duration\": {\r\n"
							+ "        \"type\": \"full_day\"\r\n" + "    }\r\n" + "}")
					.when().post("https://opensource-demo.orangehrmlive.com/web/index.php/api/v2/leave/leave-requests");

			JsonPath jsonPathEvaluator = postResponse.jsonPath();

			if (postResponse.statusCode() == 200) {
				System.out.println("post response code " + postResponse.getStatusCode());
				leaveIDs.add(jsonPathEvaluator.get("data.id").toString());
			}

		}

		try {
			// refreshing the page and checking whether the entries are reflecting on the UI
			driver.navigate().refresh();
			waitTillPageLoad(wait);
			driver.findElement(By.xpath("//span[text()='Cancelled ']/i")).click();
			Thread.sleep(1000);
			driver.findElement(By.xpath("//button[text()=' Search ']")).click();
			waitTillPageLoad(wait);
			WebElement e = driver.findElement(By.xpath("//div/span[text()=' (22) Records Found']"));
			assertion.assertTrue(e.isDisplayed());
			
			//flaky UI, fixing the code below. You can run the rest of the code
			/*
			  //create an extra leave to validate the error message 
			driver.navigate().to(
			  "https://opensource-demo.orangehrmlive.com/web/index.php/leave/applyLeave");
			  waitTillPageLoad(wait); driver.findElement(By.
			  xpath("//i[@class=\"oxd-icon bi-caret-down-fill oxd-select-text--arrow\"]")).
			  click();
			  driver.findElement(By.xpath("//span[contains(text(),'CAN')]")).click();
			  driver.findElement(By.xpath("//label[text()='From Date']/../..//input")).
			  sendKeys("2024-30-04");
			  driver.findElement(By.xpath("//label[text()='To Date']/../..//input")).
			  sendKeys("2024-30-04");
			  driver.findElement(By.xpath("//textArea")).sendKeys("test");
			  driver.findElement(By.xpath("//button[text()=' Apply ']")).click();
			  WebElement e1 = driver.findElement(By.
			  xpath("//h6[text()='Overlapping Leave Request(s) Found']"));
			  assertion.assertTrue(e1.isDisplayed());
			  
			  */
			 
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		//due to flaky UI issues, if the program breaks we cannot cancel the created leaves. 
		//So, I'm using finally block to ensure smooth cancellations and cleanup
		} finally {
			for (String leaveID : leaveIDs) {
				// cleanup, canceling the leave requests and closing the browser
				Response putResponse = given().header("Content-Type", "application/json")
						.header("cookie", sessionCookie).body("{\r\n" + "    \"action\": \"CANCEL\"\r\n" + "}").when()
						.put("https://opensource-demo.orangehrmlive.com/web/index.php/api/v2/leave/employees/leave-requests/"
								+ leaveID);

				System.out.println("put response code for leave cancellation " + putResponse.getStatusCode());
				assertion.assertEquals(putResponse.getStatusCode(), 200);
			}
			assertion.assertAll();
			driver.quit();
		}
	}

	// Support methods

	public static void waitTillPageLoad(WebDriverWait wait) {
		// Wait for the 'document.readyState' to be 'complete'
		wait.until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				return ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete");
			}
		});
	}

	// method to get the latest count on available leaves
	public static int getLeaveBalance(String sessionCookie) {
		Response postResponse = given().header("Content-Type", "application/json").header("cookie", sessionCookie)
				.when()
				.get("https://opensource-demo.orangehrmlive.com/web/index.php/api/v2/leave/leave-balance/leave-type/7");

		JsonPath jsonPathEvaluator = postResponse.jsonPath();
		int leaveBalance = jsonPathEvaluator.get("data.balance.balance");
		System.out.println("remaining number of leaves " + leaveBalance);

		return leaveBalance;
	}

}
