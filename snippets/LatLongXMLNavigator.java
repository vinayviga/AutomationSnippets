package Tests.scripts.CardLoad;

import Pages.GenericLib.AVDLauncher;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.remote.MobileCapabilityType;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import org.openqa.selenium.html5.Location;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;

/**
 * This class will iterate through the latitudes and longitudes through appium which keeps updating the location
 * on the map which resembles a travel
 * IMPORTANT NOTE : PLEASE DISABLE YOUR COMPUTER'S LOCATION SERVICES if an emulator is used as it causes distortion of location
 */
public class LatLongXMLNavigator
{


    public void parser(AppiumDriver driver) throws ParserConfigurationException, SAXException, IOException, InterruptedException {
	    //Location to your GPX file
		File inputFile = new File("./mapstogpx20180706_100747.gpx");
	    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	    Document doc = dBuilder.parse(inputFile);
	    doc.getDocumentElement().normalize();
        System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
        NodeList nList = doc.getElementsByTagName("trkpt");
        System.out.println("----------------------------");
        //using List as it supports duplicate entries
        List<Double> lat = Collections.synchronizedList (new ArrayList<Double>());
        List<Double> lon = Collections.synchronizedList( new ArrayList<Double>());
        List<List<Double>> coordinates = Collections.synchronizedList( new ArrayList<List<Double>>());
        System.out.println("number of points "+nList.getLength());

        //Logic to extract Latitudes and Longitudes
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
            System.out.println("\nCurrent Element :" + nNode.getNodeName());
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                System.out.println("point number : "
                        + eElement
                        .getElementsByTagName("name")
                        .item(0)
                        .getTextContent());

                DecimalFormat df = new DecimalFormat("#.####");
                double latit = Double.parseDouble(eElement.getAttribute("lat").toString());
                double longit = Double.parseDouble(eElement.getAttribute("lon").toString());
                lat.add(Double.valueOf(df.format(latit)));
                lon.add(Double.valueOf(df.format(longit)));

            }
        }
        System.out.println("----------------------------");
        coordinates.add(lat);
        coordinates.add(lon);

        networkOFF();
        synchronized(coordinates)
        {
            if (coordinates.get(0).size()==lon.size())
            {
                System.out.println("size of tags "+lat.size());
                for(int i =0;i<lat.size();i++)
                {
                    System.out.println("current location : "+coordinates.get(0).get(i)+" Jai Google Talli "+coordinates.get(1).get(i));

                    Location l = new Location(coordinates.get(0).get(i),coordinates.get(1).get(i),0.0);
                    synchronized (driver) {
                        driver.wait(1000);
                        driver.setLocation(l);
                    }
                }
            }
        }
        networkON();
        }

    public static AppiumDriver driver;
    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException, InterruptedException {

        /**
         * Please comment/remove this line of code if you're using an actual device or an iOS device
         * If you're using an android emulator, pass the name of the emulator as a string parameter. My emulator's name is test.
         */
        AVDLauncher.launchEmulator("test");

        //Please update the paths for appium and node with respect to your machine
        AppiumDriverLocalService service = AppiumDriverLocalService.buildService(new AppiumServiceBuilder()
                .usingDriverExecutable(new File("/usr/local/bin/node"))
                .withAppiumJS(new File("/usr/local/bin/appium"))
                .withIPAddress("127.0.0.1")
                .usingPort(4723));

        service.start();
        //Change your capabilities to
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("deviceName", "emulator-5554");
        capabilities.setCapability("automationName","UiAutomator2");
        capabilities.setCapability("platformName", "Android");
        capabilities.setCapability(CapabilityType.VERSION, "7.0");
        capabilities.setCapability(MobileCapabilityType.NO_RESET, true);
        capabilities.setCapability("appPackage", "com.google.android.apps.maps");
        capabilities.setCapability("appActivity", "com.google.android.maps.MapsActivity");
        capabilities.setCapability(MobileCapabilityType.TAKES_SCREENSHOT, "true");
        driver = new AndroidDriver(new URL("http://127.0.0.1:4723/wd/hub"), capabilities);

        LatLongXMLNavigator ll = new LatLongXMLNavigator();
        ll.parser(driver);

    }

    //Stops the network inclusion for the app which creates GPS fluctuation
    public static void networkOFF()
    {
         Process process;
        String[] aCommand = new String[]{"adb", "shell", "settings", "put", "secure", "location_providers_allowed", "-network"};
        try {
            process = new ProcessBuilder(aCommand).start();
            Thread.sleep(1000);
            System.out.println("network off!");
            process.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //Restores the network
    public static void networkON()
    {
        Process process;
        String[] aCommand = new String[]{"adb", "shell", "settings", "put", "secure", "location_providers_allowed", "+network"};
        try {
            process = new ProcessBuilder(aCommand).start();
            Thread.sleep(1000);
            System.out.println("network on!");
            process.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
