package com.cst438.controller;



import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InstructorSystemTest {


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
    public void instructorAddAssignmentTest() throws Exception {

        driver.findElement(By.id("year")).sendKeys("2024");
        driver.findElement(By.id("semester")).sendKeys("Spring");

        // Click and wait
        WebElement we = driver.findElement(By.id("sections"));
        we.click();
        try {
            Thread.sleep(SLEEP_DURATION);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        List<WebElement> assignmentsButtons = driver.findElements(By.xpath("//a[@id='assignment']"));
        assertFalse(assignmentsButtons.isEmpty(), "There are no assignment buttons");

        // Check if there is at least one assignment
        assignmentsButtons.get(0).click();
        Thread.sleep(SLEEP_DURATION);

        // Click on add assignment button
        driver.findElement(By.id("addAssignment")).click();
        Thread.sleep(SLEEP_DURATION);

        // Fill out assignment dialogue and save
        String assignmentTitle = "db assignment 3";
        String dueDate = "2024-06-12";

        driver.findElement(By.name("title")).sendKeys(assignmentTitle);
        driver.findElement(By.name("dueDate")).sendKeys(dueDate);
        driver.findElement(By.id("save")).click();
        Thread.sleep(SLEEP_DURATION);

        // Locate the table rows
        List<WebElement> rows = driver.findElements(By.xpath("//tbody/tr"));
        assertFalse(rows.isEmpty(), "No rows found in the table.");
        // Check if there are any rows
        // Get the last row
        WebElement lastRow = rows.get(rows.size() - 1);

        // Get the title and due date text from the last row
        String newTitle = lastRow.findElement(By.xpath("td[2]")).getText();
        String newDueDate = lastRow.findElement(By.xpath("td[3]")).getText();

       // Compare
       assertEquals(assignmentTitle, newTitle);
       assertEquals(dueDate, newDueDate);
    }

    @Test
    public void instructorGradesAssignment() throws Exception {

        driver.findElement(By.id("year")).sendKeys("2024");
        driver.findElement(By.id("semester")).sendKeys("Spring");

        // Click and wait
        WebElement we = driver.findElement(By.id("sections"));
        we.click();
        try {
            Thread.sleep(SLEEP_DURATION);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        List<WebElement> assignmentsButtons = driver.findElements(By.xpath("//a[@id='assignment']"));
        assertFalse(assignmentsButtons.isEmpty(), "There are no assignment buttons");

        // Click on assignments button
        assignmentsButtons.get(0).click();
        Thread.sleep(SLEEP_DURATION);

        List<WebElement> rows = driver.findElements(By.xpath("//tbody/tr"));
        assertFalse(rows.isEmpty(), "No rows found in the table.");

        // Click on Grade assignment
        rows.get(0).findElement(By.xpath("td[4]//button")).click();
        Thread.sleep(SLEEP_DURATION);

        rows = driver.findElements(By.xpath("//tbody/tr"));
        String score = "93";

        for (WebElement row : rows) {
            WebElement gradeInput = row.findElement(By.xpath("//input[@id='score']"));
            gradeInput.clear();
            gradeInput.sendKeys(score);
            Thread.sleep(SLEEP_DURATION);
        }
        driver.findElement(By.id("save")).click();
        Thread.sleep(SLEEP_DURATION);


        rows = driver.findElements(By.xpath("//tbody/tr"));

        for (WebElement row : rows) {
            WebElement gradeInput = row.findElement(By.xpath("//input[@id='score']"));
            String grade = gradeInput.getAttribute("value");
            assertEquals(score, grade);
        }
        Thread.sleep(SLEEP_DURATION);
    }

    @Test
    public void finalClassGrade() throws Exception {

        driver.findElement(By.id("year")).sendKeys("2024");
        driver.findElement(By.id("semester")).sendKeys("Spring");

        // Click and wait
        WebElement we = driver.findElement(By.id("sections"));
        we.click();
        try {
            Thread.sleep(SLEEP_DURATION);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        List<WebElement> enrollmentsButtons = driver.findElements(By.xpath("//a[@id='enrollment']"));
        assertFalse(enrollmentsButtons.isEmpty(), "There are no enrollment buttons.");

        // Click on assignments button
        enrollmentsButtons.get(0).click();
        Thread.sleep(SLEEP_DURATION);


        // Change the grade
        List<WebElement> rows = driver.findElements(By.xpath("//tbody/tr"));
        String grade = "C";

        for (WebElement row : rows) {
            WebElement gradeInput = row.findElement(By.xpath("//input[@id='grade']"));
            gradeInput.clear();
            gradeInput.sendKeys(grade);
            Thread.sleep(SLEEP_DURATION);
        }
        driver.findElement(By.id("saveGrade")).click();
        Thread.sleep(SLEEP_DURATION);


        // Check and compare the grade
        rows = driver.findElements(By.xpath("//tbody/tr"));

        for (WebElement row : rows) {
            WebElement gradeInput = row.findElement(By.xpath("//input[@id='grade']"));
            String enteredGrade = gradeInput.getAttribute("value");
            assertEquals(grade, enteredGrade);
        }

    }
}
