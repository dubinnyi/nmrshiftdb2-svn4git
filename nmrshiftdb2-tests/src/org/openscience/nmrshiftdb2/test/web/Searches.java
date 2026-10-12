package org.openscience.nmrshiftdb2.test.web;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openscience.nmrshiftdb2.test.TestConstants;

public class Searches{
	
	WebDriver driver;
    
    @Before
    public void setUp() throws Exception {
    	System.setProperty("webdriver.gecko.driver", "geckodriver");
        driver = new FirefoxDriver();
        String url=TestConstants.TESTSERVER+TestConstants.NMRSHIFTDB_APPLICATION_PATH;
        driver.get(url);
    }

    @Test
    public void testSpectrumSearch() throws Exception {
        driver.findElement(By.linkText("Search")).click();
        Thread.sleep(1000);
        driver.findElement(By.name("spectrum")).sendKeys("12.9\n21.2\n23.9\n52.0\n50.9\n171.5\n51.1");
        driver.findElement(By.name("eventSubmit_doSearchbyspectrumspec")).click();
        Thread.sleep(10000);
        String bodytext = driver.findElement(By.id("result0")).getText();
        Assert.assertTrue(bodytext.contains("100,00 %"));
        bodytext = driver.findElement(By.id("result1")).getText();
        Assert.assertTrue(bodytext.contains("72,10 %"));
    }
    
    @Test
    public void testMySpectra() throws Exception {
        driver.findElement(By.name("username")).sendKeys("shk3");
        driver.findElement(By.name("password")).sendKeys("testworker");
        driver.findElement(By.name("submit")).click();
        Thread.sleep(10000);
        driver.findElement(By.linkText("Search")).click();
        Thread.sleep(1000);
        driver.findElement(By.linkText("Show all my contributions")).click();
        Thread.sleep(1000);
        String bodytext = driver.findElement(By.id("result0")).getText();
        Assert.assertTrue(bodytext.contains("OK:0, U:0, R:0, E:0, P:1"));
    }

    @After
    public void tearDown() throws Exception {
        driver.quit();
    }
}