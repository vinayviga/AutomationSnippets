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
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;

public class APIUICookieIntegration {

	SoftAssert assertion = new SoftAssert();
	private static RemoteWebDriver driver;

	public static void main(String[] args) throws IOException, InterruptedException {

		System.setProperty("webdriver.gecko.driver",
				"D:\\eclipse\\workspace\\opencart\\src\\main\\resources\\geckodriver.exe");

		// Initialize ChromeDriver
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
		Cookie session = driver.manage().getCookieNamed("orangehrm");
		System.out.println(session.getName()+"="+session.getValue());
		
		driver.get("https://opensource-demo.orangehrmlive.com/web/index.php/leave/viewMyLeaveList");
		
		List<String> leaveIDs = new ArrayList<String>();
		int i=0;
		
		APIUICookieIntegration balance = new APIUICookieIntegration();
		
		// creating a leave request		
		while(balance.getLeaveBalance(session)>1)
		{
			//int count= balance.getLeaveBalance(session);
			
			i++;
			String formattedNumber = String.format("%02d", i);
			 Response postResponse = given()
	            .header("Content-Type", "application/json")
	            .header("cookie", session.getName()+"="+session.getValue())
	            //Please do make changes to the year and month accordingly
	            .body("{\r\n"
	            		+ "    \"leaveTypeId\": 7,\r\n"
	            		+ "    \"fromDate\": \"2024-04-"+formattedNumber+"\",\r\n"
	            		+ "    \"toDate\": \"2024-04-"+formattedNumber+"\",\r\n"
	            		+ "    \"comment\": \"test\",\r\n"
	            		+ "    \"duration\": {\r\n"
	            		+ "        \"type\": \"full_day\"\r\n"
	            		+ "    }\r\n"
	            		+ "}")
	            .when()
	            .post("https://opensource-demo.orangehrmlive.com/web/index.php/api/v2/leave/leave-requests"); 
	

			 
			 JsonPath jsonPathEvaluator = postResponse.jsonPath();
			 
			 if(postResponse.statusCode() == 200)
			 {
				 System.out.println("post response code "+ postResponse.getStatusCode());
				 leaveIDs.add( jsonPathEvaluator.get("data.id").toString());
			 }
			 
			
		}
			 
			 
			 //refreshing the page and checking whether the entries are reflecting on the UI
			 driver.navigate().refresh();
			 waitTillPageLoad(wait);
			 driver.findElement(By.xpath("//span[text()='Cancelled ']/i")).click();
			 Thread.sleep(1000);
			 driver.findElement(By.xpath("//button[text()=' Search ']")).click();
			 
			 
			 
			 //create an extra leave to validate the error message
			 driver.navigate().to("https://opensource-demo.orangehrmlive.com/web/index.php/leave/applyLeave");
			
			 for(String leaveID:leaveIDs)
			 {
			 // cleanup, canceling the leave requests and closing the browser 
			 Response putResponse = given()
			            .header("Content-Type", "application/json")
			            .header("cookie", session.getName()+"="+session.getValue())
			            .body("{\r\n"
			            		+ "    \"action\": \"CANCEL\"\r\n"
			            		+ "}")
			            .when()
			            .put("https://opensource-demo.orangehrmlive.com/web/index.php/api/v2/leave/employees/leave-requests/"+leaveID);
			
			 System.out.println("put response code for leave cancellation "+ putResponse.getStatusCode());
			 }
		
		driver.quit();
	}
	
	public static void waitTillPageLoad(WebDriverWait wait)
	{
		// Wait for the 'document.readyState' to be 'complete'
				wait.until(new ExpectedCondition<Boolean>() {
				    @Override
				    public Boolean apply(WebDriver driver) {
				        return ((JavascriptExecutor) driver).executeScript(
				            "return document.readyState").equals("complete");
				    }
				});
	}
	
	public int getLeaveBalance(Cookie session)
	{
		Response postResponse = given()
	            .header("Content-Type", "application/json")
	            .header("cookie", session.getName()+"="+session.getValue())
	            .when()
	            .get("https://opensource-demo.orangehrmlive.com/web/index.php/api/v2/leave/leave-balance/leave-type/7");
		
		JsonPath jsonPathEvaluator = postResponse.jsonPath();
		int leaveBalance = jsonPathEvaluator.get("data.balance.balance");
		System.out.println("remaining number of leaves "+leaveBalance);
		
		return leaveBalance;
	}

}
