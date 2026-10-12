package org.openscience.nmrshiftdb2.test.web;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openscience.nmrshiftdb2.test.TestConstants;

public class QuickCheck{
	
	WebDriver driver;
    
    @Before
    public void setUp() throws Exception {
    	System.setProperty("webdriver.gecko.driver", "geckodriver");
        driver = new FirefoxDriver();
        String url=TestConstants.TESTSERVER+TestConstants.NMRSHIFTDB_APPLICATION_PATH;
        driver.get(url);
    }

    @Test
    public void testQuickcheck1java() throws Exception {
        driver.findElement(By.linkText("Quick Check")).click();
        String testfile = this.getClass().getResource("/org/openscience/nmrshiftdb2/test/web/(3,4-dimethoxyphenyl)propionsaeure.mol").getFile();
        //it seems the sleeps are needed to avoid the page not being loaded
        Thread.sleep(6000);
        driver.findElement(By.id("uploadmnova")).click();
        driver.findElement(By.id("uploadmnova")).sendKeys(testfile);
        //these should not be needed, but for unknown reasons the test doesn't work without
        driver.findElement(By.id("shiftsc")).sendKeys("111.5\n132.7\n120.0\n111.2\n147.5\n148.8\n30.3\n36.0\n179.0\n55.8\n55.9");
        driver.findElement(By.id("shiftsh")).sendKeys("6.73\n6.75\n6.79\n2.90\n2.66\n3.85\n3.86");
        driver.findElement(By.id("eventSubmit_doSubmitquickcheck")).click();
        Thread.sleep(6000);
        driver.findElement(By.id("shiftsc")).sendKeys("111.5\n132.7\n120.0\n111.2\n147.5\n148.8\n30.3\n36.0\n179.0\n55.8\n55.9");
        driver.findElement(By.id("shiftsh")).sendKeys("6.73\n6.75\n6.79\n2.90\n2.66\n3.85\n3.86");
        driver.findElement(By.id("eventSubmit_doSubmitquickcheck")).click();
        Thread.sleep(6000);
        boolean[] checkc={true,true,true,true,true,true,true,true,true,true,false,false,false,false,true,false};
        boolean[] checkh={true,false,true,true,false,false,true,true,true,false,false,false,false,false,true,false};
        for(int i=0;i<16;i++){
        	if(checkc[i]){
		        String text = driver.findElement(By.id("atc"+i)).getText();
		        //System.out.println(i+" "+text);
		        float diff=Float.parseFloat(text.substring(text.indexOf("diff: ")+6).trim());
		        Assert.assertTrue(diff<0.6);
        	}
        	if(checkh[i]){
    	        String text = driver.findElement(By.id("atc"+i)).getText();
    	        //System.out.println(i+" "+text);
    	        float diff=Float.parseFloat(text.substring(text.indexOf("diff: ")+6).trim());
    	        Assert.assertTrue(diff<0.2);
        	}
        }
    }

    @Test
    public void testQuickcheck2java() throws Exception {
        driver.findElement(By.linkText("Quick Check")).click();
        String testfile = this.getClass().getResource("/org/openscience/nmrshiftdb2/test/web/pyrolle.mol").getFile();
        //it seems the sleeps are needed to avoid the page not being loaded
        Thread.sleep(6000);
        driver.findElement(By.id("uploadmnova")).click();
        driver.findElement(By.id("uploadmnova")).sendKeys(testfile);
        //these should not be needed, but for unknown reasons it doesn't work without
        driver.findElement(By.id("eventSubmit_doSubmitquickcheck")).click();
        Thread.sleep(6000);
        driver.findElement(By.name("inputc1")).sendKeys("106");
        driver.findElement(By.name("inputc2")).sendKeys("117");
        driver.findElement(By.id("eventSubmit_doSubmitquickcheck")).click();
        Thread.sleep(6000);
        driver.findElement(By.name("inputc1")).clear();
        driver.findElement(By.name("inputc1")).sendKeys("106");
        driver.findElement(By.name("inputc2")).clear();
        driver.findElement(By.name("inputc2")).sendKeys("117");
        driver.findElement(By.id("eventSubmit_doSubmitquickcheck")).click();
        Thread.sleep(6000);
        String text = driver.findElement(By.id("atc1")).getText();
        float diff=Float.parseFloat(text.substring(text.indexOf("diff: ")+6).trim());
        Assert.assertTrue(diff>8);
        text = driver.findElement(By.id("atc2")).getText();
        diff=Float.parseFloat(text.substring(text.indexOf("diff: ")+6).trim());
        Assert.assertTrue(diff>8);
        driver.findElement(By.id("assignc1")).sendKeys("11");
        driver.findElement(By.id("assignc2")).sendKeys("10");
        driver.findElement(By.id("eventSubmit_doSubmitquickcheck")).click();
        Thread.sleep(6000);
        text = driver.findElement(By.id("atc1")).getText();
        diff=Float.parseFloat(text.substring(text.indexOf("diff: ")+6).trim());
        Assert.assertTrue(diff<1.1);
        text = driver.findElement(By.id("atc2")).getText();
        diff=Float.parseFloat(text.substring(text.indexOf("diff: ")+6).trim());
        Assert.assertTrue(diff<1.1);
    }

    
    @After
    public void tearDown() throws Exception {
        driver.quit();
    }
}