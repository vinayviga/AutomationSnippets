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
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
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

import org.openqa.selenium.Cookie;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;

public class UIAPICookieIntegration {

	static SoftAssert assertion = new SoftAssert();
	private static RemoteWebDriver driver;

	public static void main(String[] args) throws IOException, InterruptedException {

		System.setProperty("webdriver.gecko.driver",
				"D:\\eclipse\\workspace\\opencart\\src\\main\\resources\\geckodriver.exe");

		// Initialize ChromeDriver
		WebDriver driver = new FirefoxDriver();

		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		driver.manage().timeouts().setScriptTimeout(30, TimeUnit.SECONDS);
		driver.manage().timeouts().pageLoadTimeout(40, TimeUnit.SECONDS);
		driver.manage().window().maximize();

		driver.get("https://authenticationtest.com/simpleFormAuth/");

		RestAssured.baseURI = "https://authenticationtest.com";
		Response auth_response = RestAssured.given().contentType(ContentType.URLENC)
				.formParam("email", "simpleForm@authenticationtest.com").formParam("password", "pa$$w0rd")
				.post("/login/?mode=simpleFormAuth");

		System.out.println(auth_response.statusCode());

		// Retrieve RestAssured cookies as Cookies object
		io.restassured.http.Cookies restAssuredCookies = auth_response.detailedCookies();
		System.out.println(restAssuredCookies);

		// Convert RestAssured cookies to Selenium cookies and add them to WebDriver
		restAssuredCookies.forEach(cookie -> {
			Cookie seleniumCookie = new Cookie(cookie.getName(), cookie.getValue());
			driver.manage().addCookie(seleniumCookie);
		});

		Thread.sleep(4000);
		driver.get("https://authenticationtest.com/loginSuccess/");
		Thread.sleep(4000);
		assertion.assertTrue(driver.findElement(By.xpath("//a[text() = 'Sign Out']")).isDisplayed());

		// visiting the login page and verifying it
		driver.get("https://rahulshettyacademy.com/oauthapi/getcoursedetails");
		/* creating a session ID and passing it through cookies to selenium */

		String response =

				given().formParams("client_id","")//enter client ID
				.formParams("client_secret", "")//enter client secret
				.formParams("grant_type", "client_credentials").formParams("scope", "trust").when().log().all()

				.post("https://rahulshettyacademy.com/oauthapi/oauth2/resourceOwner/token").asString();

		System.out.println(response);
		JsonPath jsonPath = new JsonPath(response);
		String accessToken = jsonPath.getString("access_token");
		System.out.println(accessToken);

		// visiting the page with access token
		Thread.sleep(4000);
		driver.get("https://rahulshettyacademy.com/oauthapi/getCourseDetails?access_token=" + accessToken);
		Thread.sleep(4000);

		driver.get("file:///C:/Users/Vinay/Downloads/dictator.gif");// remove this URL and you can use your own gif :)
		Thread.sleep(6000);
		// cleanup
		driver.quit();
	}

}
