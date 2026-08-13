package com.example.game.e2e;

import java.time.Duration;
import java.util.logging.Level;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.PageLoadStrategy;

final class WebDrivers {

	private WebDrivers() {
	}

	static WebDriver createChrome() {
		ChromeOptions options = new ChromeOptions();
		options.setPageLoadStrategy(PageLoadStrategy.EAGER);
		options.addArguments(
				"--headless=new",
				"--disable-gpu",
				"--window-size=1280,900",
				"--no-sandbox",
				"--disable-dev-shm-usage",
				"--disable-extensions");
		LoggingPreferences logs = new LoggingPreferences();
		logs.enable(LogType.BROWSER, Level.ALL);
		options.setCapability("goog:loggingPrefs", logs);
		WebDriver driver = new ChromeDriver(options);
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
		driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
		driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(30));
		return driver;
	}
}
