import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CrossSiteScripting {
  //This will use OWASP bWapp site to demonstrate XSS vulnerabilities
  //To make the test run first you will need to run the bWAPP application from Virtual Box
  private String baseUrl = "http://192.168.56.101/bWAPP/login.php";
  private String getUrl ="http://192.168.56.101/bWAPP/xss_get.php";

  WebDriver driver = new ChromeDriver();

  @BeforeMethod
  public void beforeTest(){
    System.setProperty("webdriver.chrome.driver","/Users/endabrody/securtity-tests-selenium/chromedriver");
    driver = new ChromeDriver();

    driver.get(baseUrl);

    WebElement username = driver.findElement(By.xpath("//*[@id=\"login\"]"));
    WebElement password = driver.findElement(By.xpath("//*[@id=\"password\"]"));
    WebElement login = driver.findElement(By.xpath("/html/body/div[2]/form/button"));

    username.sendKeys("bee");
    password.sendKeys("bug");
    login.click();
  }

  @DataProvider(name = "scripts")
  public Object[][] paramsData(){
    return new Object[][] {
        {"<SCRIPT>console.error(\"XSS.\")</SCRIPT>"},
        {"javascript:/*--></title></style></textarea></script></xmp><svg/onload='+/\"/+/onmouseover=1/+/[*/[]/+console.error(\"XSS.\")//'>"},
        {"<IMG SRC=\"javascript:console.error('XSS');\">"},
        {"<IMG SRC=javascript:console.error('XSS')>"},
        {"<SCRIPT>var a=\"\\\\\\\\\";console.error('XSS');//\";</SCRIPT>"},
        {"</TITLE><SCRIPT>console.error(\"XSS\");</SCRIPT>"},
    };
  }

  @Test(dataProvider ="scripts")
  public void test(String scriptData) throws InterruptedException {
    driver.get(getUrl);
    WebElement firstName = driver.findElement(By.xpath("//*[@id=\"firstname\"]\n"));
    WebElement lastName = driver.findElement(By.xpath("//*[@id=\"lastname\"]\n"));
    WebElement go = driver.findElement(By.xpath("/html/body/div[2]/form/button\n"));

    firstName.sendKeys(scriptData);
    lastName.sendKeys("Test");
    go.click();

    LogEntries logEntries = driver.manage().logs().get("browser");
    for (LogEntry entry : logEntries) {
      String errorLog= entry.getMessage();
      System.out.println("Error Log message: "+errorLog);
      //We check the console logs as not to break the test and make it flakey
      Assert.assertFalse(errorLog.contains("XSS") );
    }
    //Thread.sleep(5000);

    }



  @AfterMethod
  void tearDown(){
    driver.quit();
  }


}
