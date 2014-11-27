import junit.framework.*;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

public class test  extends TestCase{
    
    public void test_overall() {
        System.out.println("Overall Testing.");
        
        WebDriver driver= new HtmlUnitDriver();
        driver.get("http://apt-public.appspot.com/testing-lab-login.html");
        WebElement element=driver.findElement(By.name("userId"));
        element.clear();
        WebElement element1=driver.findElement(By.name("userPassword"));
        element1.clear();
        System.out.println("Log in with (Andy,apple).");
        element.sendKeys("Andy");
        element1.sendKeys("apple");
        element.submit();
        
        assertEquals("Online temperature conversion calculator",driver.getTitle());
        //System.out.println("Page title is: " + driver.getPageSource());
        
        element=driver.findElement(By.name("farenheitTemperature"));
        element.clear();
        System.out.println("Input value 21.2e1.");
        element.sendKeys("21.2e1");
        element.submit();
        assertTrue(driver.getPageSource().contains("21.2e1 Farenheit = 100 Celsius"));
        System.out.println("21.2e1 Farenheit = 100 Celsius");
        
        System.out.println("Select city: Berkeley");
        element=driver.findElement(By.cssSelector("input[value='Berkeley']"));
        element.click();
        element.submit();
        
        assertTrue(driver.getPageSource().contains("Temperature in Berkeley = 72 degrees Farenheit"));
        System.out.println("Temperature in Berkeley = 72 degrees Farenheit");
        driver.quit();
    }
    
    public void test_login(){
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        System.out.println("Bad Log in Testing: Andy, Apple");
        WebDriver driver= new HtmlUnitDriver();
        driver.get("http://apt-public.appspot.com/testing-lab-login.html");
        WebElement element=driver.findElement(By.name("userId"));
        element.clear();
        WebElement element1=driver.findElement(By.name("userPassword"));
        element1.clear();
        element.sendKeys("Andy");
        element1.sendKeys("Apple");
        element.submit();
        
        assertEquals("Bad Login",driver.getTitle());
        System.out.println("Bad Login");
        driver.quit();
    }
    
    public void test_input(){
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        System.out.println("Input Testing.");
        
        WebDriver driver= new HtmlUnitDriver();
        driver.get("http://apt-public.appspot.com/testing-lab-login.html");
        WebElement element=driver.findElement(By.name("userId"));
        element.clear();
        WebElement element1=driver.findElement(By.name("userPassword"));
        element1.clear();
        element.sendKeys("bob");
        element1.sendKeys("bathtub");
        element.submit();
        
        System.out.println("Put in 211.99199");
        element=driver.findElement(By.name("farenheitTemperature"));
        element.clear();
        element.sendKeys("211.99199");
        element.submit();
        assertTrue(driver.getPageSource().contains("211.99199 Farenheit = 100 Celsius"));
        System.out.println("211.99199 Farenheit = 100 Celsius");
        
        System.out.println("Put in abc");
        driver.navigate().back();
        element=driver.findElement(By.name("farenheitTemperature"));
        element.clear();
        element.sendKeys("abc");
        element.submit();
        assertTrue(driver.getPageSource().contains("Got a NumberFormatException on abc"));
        System.out.println("Got a NumberFormatException on abc");
        
        System.out.println("Put in 123.456");
        driver.navigate().back();
        element=driver.findElement(By.name("farenheitTemperature"));
        element.clear();
        element.sendKeys("123.456");
        element.submit();
        assertTrue(driver.getPageSource().contains("123.456 Farenheit = 50.81 Celsius"));
        System.out.println("123.456 Farenheit = 50.81 Celsius");
        
        System.out.println("Put in 300");
        driver.navigate().back();
        element=driver.findElement(By.name("farenheitTemperature"));
        element.clear();
        element.sendKeys("300");
        element.submit();
        assertTrue(driver.getPageSource().contains("300 Farenheit = 148.89 Celsius"));
        System.out.println("300 Farenheit = 148.89 Celsius");
        
        System.out.println("Put in -456");
        driver.navigate().back();
        element=driver.findElement(By.name("farenheitTemperature"));
        element.clear();
        element.sendKeys("-456");
        element.submit();
        assertTrue(driver.getPageSource().contains("-456 Farenheit = -271.11 Celsius"));
        System.out.println("-456 Farenheit = -271.11 Celsius");
        
        
        driver.quit();
    }
}
