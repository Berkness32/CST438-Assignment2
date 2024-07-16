package com.cst438.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class StudentSystemTest {

    public static final String CHROME_DRIVER_FILE_LOCATION =
            "chromedriver-mac-arm64/chromedriver";

    //"/Users/aaronberkness/Documents/CST_438/CST438-Assignment2/chrome-mac-arm64"

    //public static final String CHROME_DRIVER_FILE_LOCATION =
    //        "~/chromedriver_macOS/chromedriver";
    public static final String URL = "http://localhost:3000";

    public static final int SLEEP_DURATION = 1000; // 1 second.


    // add selenium dependency to pom.xml

    // these tests assumes that test data does NOT contain any
    // sections for course cst499 in 2024 Spring term.

    WebDriver driver;

    @BeforeEach
    public void setUpDriver() throws Exception {

        // set properties required by Chrome Driver
        System.setProperty(
                "webdriver.chrome.driver", CHROME_DRIVER_FILE_LOCATION);
        ChromeOptions ops = new ChromeOptions();
        ops.addArguments("--remote-allow-origins=*");

        // start the driver
        driver = new ChromeDriver(ops);

        driver.get(URL);
        // must have a short wait to allow time for the page to download
        Thread.sleep(SLEEP_DURATION);

    }

    @AfterEach
    public void terminateDriver() {
        if (driver != null) {
            // quit driver
            driver.close();
            driver.quit();
            driver = null;
        }
    }

    @Test
    public void studentEnrolls() throws Exception {

        driver.findElement(By.id("addCourse")).click();

        try {
            Thread.sleep(SLEEP_DURATION);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        List<WebElement> rows = driver.findElements(By.xpath("//tbody/tr"));
        assertFalse(rows.isEmpty(), "No rows found in the table.");

        // Click on add course
        String secNo = rows.get(0).findElement(By.xpath("td[1]")).getText();
        System.out.println(secNo);
        rows.get(0).findElement(By.xpath("td[10]//button")).click();
        Thread.sleep(SLEEP_DURATION);

        // Locate and click the "Yes" button within the dialog
        WebElement yesButton = driver.findElement(By.xpath("//button[contains(text(), 'Yes')]"));
        yesButton.click();
        Thread.sleep(SLEEP_DURATION);

        // Switch over to the schedule to check that the student is enrolled.
        driver.findElement(By.id("schedule")).click();
        Thread.sleep(SLEEP_DURATION);

        driver.findElement(By.id("year")).sendKeys("2024");
        driver.findElement(By.id("semester")).sendKeys("Fall");

        driver.findElement(By.id("getSchedule")).click();
        Thread.sleep(SLEEP_DURATION);

        // Get the rows from the schedule
        rows = driver.findElements(By.xpath("//tbody/tr"));
        assertFalse(rows.isEmpty(), "No rows found in the table.");

        int last = (rows.size() - 1);
        String testSecNo = rows.get(last).findElement(By.xpath("td[2]")).getText();
        assertEquals(secNo, testSecNo);

        // Drop the course to make sure the test is repeatable
        rows.get(last).findElement(By.xpath("//tbody/tr/td/button[contains(text(), 'Drop')]")).click();
        Thread.sleep(SLEEP_DURATION);

        yesButton = driver.findElement(By.xpath("//button[contains(text(), 'Yes')]"));
        yesButton.click();
        Thread.sleep(SLEEP_DURATION);
    }
}
