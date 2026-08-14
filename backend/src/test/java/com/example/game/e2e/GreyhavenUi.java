package com.example.game.e2e;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

final class GreyhavenUi {

	private final WebDriver driver;
	private final String baseUrl;
	private final WebDriverWait wait;

	GreyhavenUi(WebDriver driver, String baseUrl) {
		this.driver = driver;
		this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
		this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
	}

	void open(String path) {
		String normalized = path.startsWith("/") ? path : "/" + path;
		driver.get(baseUrl + normalized);
	}

	void register(String email, String password) {
		open("/register");
		waitForTestId("register-page");
		type(By.cssSelector("[data-testid='register-email']"), email);
		type(By.cssSelector("[data-testid='register-password']"), password);
		driver.findElement(By.cssSelector("[data-testid='register-submit']")).click();
	}

	void login(String email, String password) {
		open("/login");
		waitForTestId("login-page");
		type(By.cssSelector("[data-testid='login-email']"), email);
		type(By.cssSelector("[data-testid='login-password']"), password);
		driver.findElement(By.cssSelector("[data-testid='login-submit']")).click();
	}

	void createCharacter(String name) {
		waitForTestId("create-character-page");
		type(By.cssSelector("[data-testid='character-name']"), name);
		driver.findElement(By.cssSelector("[data-testid='create-character-submit']")).click();
	}

	void logout() {
		if (!driver.findElements(By.cssSelector("[data-testid='topbar-menu']")).isEmpty()) {
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-testid='topbar-menu']"))).click();
		}
		wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-testid='logout-button']"))).click();
		try {
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='login-page']")));
		}
		catch (RuntimeException exception) {
			throw new IllegalStateException(
					"Timed out waiting for login page after logout at " + driver.getCurrentUrl()
							+ ". " + bodySnippet() + " console=" + browserConsole(),
					exception);
		}
	}

	void waitForCreateCharacterPage() {
		wait.until(ExpectedConditions.visibilityOfElementLocated(
				By.cssSelector("[data-testid='create-character-page']")));
	}

	void waitForGameWithCharacter(String characterName) {
		try {
			wait.until(ExpectedConditions.or(
					ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='game-layout']")),
					ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='create-character-error']")),
					ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='register-error']"))));
		}
		catch (RuntimeException exception) {
			throw new IllegalStateException(
					"Timed out waiting for game after character creation at " + driver.getCurrentUrl()
							+ ". " + bodySnippet() + " console=" + browserConsole(),
					exception);
		}
		if (!driver.findElements(By.cssSelector("[data-testid='create-character-error']")).isEmpty()) {
			throw new IllegalStateException("Character creation failed: " + alertText("create-character-error"));
		}
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='game-layout']")));
		wait.until(ExpectedConditions.textToBe(
				By.cssSelector("[data-testid='character-summary-name']"),
				characterName));
	}

	String alertText(String testId) {
		WebElement alert = wait.until(ExpectedConditions.visibilityOfElementLocated(
				By.cssSelector("[data-testid='" + testId + "']")));
		return alert.getText().trim();
	}

	String text(String testId) {
		return wait.until(ExpectedConditions.visibilityOfElementLocated(
				By.cssSelector("[data-testid='" + testId + "']"))).getText().trim();
	}

	String currentPath() {
		String current = driver.getCurrentUrl();
		URIPath path = URIPath.parse(current);
		return path.path();
	}

	void waitUntilPath(String expectedPath) {
		wait.until(driver -> expectedPath.equals(currentPath()));
	}

	void waitForLocationPanel() {
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='location-panel']")));
	}

	void waitForLocation(String locationName) {
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='location-panel']")));
		wait.until(ExpectedConditions.textToBe(
				By.cssSelector("[data-testid='current-location']"),
				locationName));
		wait.until(ExpectedConditions.textToBe(
				By.cssSelector("[data-testid='character-summary-location']"),
				locationName));
	}

	void travelTo(String locationCode, String expectedLocationName) {
		By travelButton = By.cssSelector("[data-testid='destination-" + locationCode + "']");
		wait.until(ExpectedConditions.elementToBeClickable(travelButton)).click();
		waitForLocation(expectedLocationName);
	}

	void waitForDestination(String locationCode) {
		wait.until(ExpectedConditions.visibilityOfElementLocated(
				By.cssSelector("[data-testid='destination-" + locationCode + "']")));
	}

	boolean hasDestination(String locationCode) {
		return !driver.findElements(By.cssSelector("[data-testid='destination-" + locationCode + "']")).isEmpty();
	}

	void waitForNearbyCharacter(String characterName) {
		wait.until(ExpectedConditions.visibilityOfElementLocated(
				By.cssSelector("[data-testid='nearby-" + characterName + "']")));
	}

	void waitUntilNearbyDoesNotContain(String characterName) {
		By locator = By.cssSelector("[data-testid='nearby-" + characterName + "']");
		wait.until(driver -> driver.findElements(locator).isEmpty());
	}

	void refreshGame() {
		driver.navigate().refresh();
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='game-layout']")));
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='location-panel']")));
	}

	void waitForInventory() {
		wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-testid='nav-inventory']"))).click();
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='inventory-panel']")));
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='inventory-list']")));
	}

	void waitForEquipment() {
		wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-testid='nav-equipment']"))).click();
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='equipment-panel']")));
	}

	void clickAction(String testId) {
		wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-testid='" + testId + "']"))).click();
	}

	void waitForText(String testId, String expected) {
		wait.until(ExpectedConditions.textToBe(By.cssSelector("[data-testid='" + testId + "']"), expected));
	}

	void waitForTextContaining(String testId, String expected) {
		wait.until(ExpectedConditions.textToBePresentInElementLocated(
				By.cssSelector("[data-testid='" + testId + "']"),
				expected));
	}

	boolean hasTestId(String testId) {
		return !driver.findElements(By.cssSelector("[data-testid='" + testId + "']")).isEmpty();
	}

	void waitUntilGone(String testId) {
		By locator = By.cssSelector("[data-testid='" + testId + "']");
		wait.until(driver -> driver.findElements(locator).isEmpty());
	}

	/**
	 * Posts an authenticated move intent through the browser session (CSRF + cookies).
	 * Used to exercise invalid destinations that the UI does not offer as travel buttons.
	 */
	Map<String, Object> postMoveViaBrowserSession(String destinationLocationId) {
		Object raw = ((JavascriptExecutor) driver).executeAsyncScript(
				"""
						const destinationLocationId = arguments[0];
						const done = arguments[arguments.length - 1];
						(async () => {
						  try {
						    await fetch('/api/v1/bootstrap', { method: 'GET', credentials: 'include' });
						    const csrf = document.cookie.split(';')
						      .map((part) => part.trim())
						      .find((part) => part.startsWith('XSRF-TOKEN='));
						    const token = csrf
						      ? decodeURIComponent(csrf.substring('XSRF-TOKEN='.length))
						      : '';
						    const response = await fetch('/api/v1/world/move', {
						      method: 'POST',
						      credentials: 'include',
						      headers: {
						        'Content-Type': 'application/json',
						        'X-XSRF-TOKEN': token,
						      },
						      body: JSON.stringify({ destinationLocationId }),
						    });
						    const body = await response.json();
						    done({
						      status: response.status,
						      code: body.code ?? null,
						      message: body.message ?? null,
						      name: body.name ?? null,
						    });
						  } catch (error) {
						    done({
						      status: 0,
						      code: 'SCRIPT_ERROR',
						      message: String(error),
						      name: null,
						    });
						  }
						})();
						""",
				destinationLocationId);
		if (!(raw instanceof Map<?, ?> map)) {
			throw new IllegalStateException("Unexpected move script result: " + raw);
		}
		Map<String, Object> result = new LinkedHashMap<>();
		map.forEach((key, value) -> result.put(String.valueOf(key), value));
		return result;
	}

	private void waitForTestId(String testId) {
		By locator = By.cssSelector("[data-testid='" + testId + "']");
		try {
			wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
		}
		catch (RuntimeException exception) {
			throw new IllegalStateException(
					"Timed out waiting for [data-testid='" + testId + "'] at " + driver.getCurrentUrl()
							+ ". " + bodySnippet() + " console=" + browserConsole(),
					exception);
		}
	}

	private String browserConsole() {
		try {
			return driver.manage().logs().get(LogType.BROWSER).getAll().stream()
					.map(entry -> entry.getLevel() + ":" + entry.getMessage())
					.reduce((left, right) -> left + " || " + right)
					.orElse("<empty>");
		}
		catch (RuntimeException exception) {
			return "<unavailable:" + exception.getMessage() + ">";
		}
	}

	private String bodySnippet() {
		try {
			Object rootHtml = ((org.openqa.selenium.JavascriptExecutor) driver)
					.executeScript("return document.getElementById('root') ? document.getElementById('root').innerHTML : '<no-root>';");
			Object readyState = ((org.openqa.selenium.JavascriptExecutor) driver)
					.executeScript("return document.readyState;");
			return "readyState=" + readyState + " root=" + String.valueOf(rootHtml);
		}
		catch (RuntimeException exception) {
			return "unable to read root: " + exception.getMessage();
		}
	}

	private void type(By locator, String value) {
		WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
		element.click();
		((JavascriptExecutor) driver).executeScript(
				"""
						const input = arguments[0];
						const value = arguments[1];
						const lastValue = input.value;
						input.focus();
						input.value = value;
						const tracker = input._valueTracker;
						if (tracker) {
						  tracker.setValue(lastValue);
						}
						input.dispatchEvent(new Event('input', { bubbles: true }));
						input.dispatchEvent(new Event('change', { bubbles: true }));
						""",
				element,
				value);
		// Fall back to keystrokes if the React tracker path did not stick.
		if (!value.equals(driver.findElement(locator).getDomProperty("value"))) {
			WebElement retry = driver.findElement(locator);
			retry.click();
			retry.sendKeys(Keys.chord(Keys.CONTROL, "a"));
			retry.sendKeys(Keys.BACK_SPACE);
			retry.sendKeys(value);
		}
		wait.until(driver -> value.equals(driver.findElement(locator).getDomProperty("value")));
	}

	private record URIPath(String path) {
		static URIPath parse(String url) {
			try {
				java.net.URI uri = java.net.URI.create(url);
				String path = uri.getPath();
				if (path == null || path.isBlank()) {
					return new URIPath("/");
				}
				return new URIPath(path);
			}
			catch (IllegalArgumentException exception) {
				return new URIPath("/");
			}
		}
	}
}
