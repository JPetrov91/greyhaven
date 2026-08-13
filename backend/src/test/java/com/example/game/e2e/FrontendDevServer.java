package com.example.game.e2e;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Starts the Vite frontend against a running backend for Selenium browser tests.
 */
final class FrontendDevServer implements AutoCloseable {

	private final Process process;
	private final String baseUrl;
	private final Thread logPump;

	private FrontendDevServer(Process process, String baseUrl, Thread logPump) {
		this.process = process;
		this.baseUrl = baseUrl;
		this.logPump = logPump;
	}

	static FrontendDevServer start(int backendPort) throws IOException, InterruptedException {
		Path frontendDir = resolveFrontendDir();
		if (!Files.isDirectory(frontendDir.resolve("node_modules"))) {
			throw new IllegalStateException(
					"Frontend dependencies missing at " + frontendDir
							+ ". Run `npm install` in frontend/ before Selenium tests.");
		}

		int frontendPort = freePort();
		String apiProxy = "http://127.0.0.1:" + backendPort;
		String baseUrl = "http://127.0.0.1:" + frontendPort;

		Path viteBin = frontendDir.resolve("node_modules").resolve("vite").resolve("bin").resolve("vite.js");
		if (!Files.isRegularFile(viteBin)) {
			throw new IllegalStateException("Vite binary not found at " + viteBin);
		}

		List<String> command = new ArrayList<>();
		command.add(nodeCommand());
		command.add(viteBin.toAbsolutePath().toString());
		command.add("--host");
		command.add("127.0.0.1");
		command.add("--port");
		command.add(Integer.toString(frontendPort));
		command.add("--strictPort");

		ProcessBuilder builder = new ProcessBuilder(command);
		builder.directory(frontendDir.toFile());
		builder.environment().put("GREYHAVEN_API_PROXY", apiProxy);
		builder.redirectErrorStream(true);

		System.out.println("[e2e] Starting frontend with GREYHAVEN_API_PROXY=" + apiProxy + " at " + baseUrl);
		Process process = builder.start();
		Thread logPump = Thread.startVirtualThread(() -> pumpOutput(process));

		try {
			waitUntilReady(baseUrl + "/", process, Duration.ofSeconds(60));
			waitUntilReady(baseUrl + "/api/v1/bootstrap", process, Duration.ofSeconds(30));
		}
		catch (RuntimeException | IOException | InterruptedException exception) {
			process.destroyForcibly();
			logPump.interrupt();
			throw exception;
		}

		return new FrontendDevServer(process, baseUrl, logPump);
	}

	String baseUrl() {
		return baseUrl;
	}

	@Override
	public void close() {
		process.destroy();
		try {
			if (!process.waitFor(10, TimeUnit.SECONDS)) {
				process.destroyForcibly();
			}
		}
		catch (InterruptedException interrupted) {
			Thread.currentThread().interrupt();
			process.destroyForcibly();
		}
		logPump.interrupt();
	}

	private static Path resolveFrontendDir() {
		String configured = System.getProperty("greyhaven.frontend.dir");
		if (configured != null && !configured.isBlank()) {
			return Path.of(configured).toAbsolutePath().normalize();
		}
		Path sibling = Path.of("..", "frontend").toAbsolutePath().normalize();
		if (Files.isDirectory(sibling)) {
			return sibling;
		}
		Path fromModule = Path.of("frontend").toAbsolutePath().normalize();
		if (Files.isDirectory(fromModule)) {
			return fromModule;
		}
		throw new IllegalStateException(
				"Unable to locate frontend directory. Set -Dgreyhaven.frontend.dir=/path/to/frontend");
	}

	private static String nodeCommand() {
		String os = System.getProperty("os.name", "").toLowerCase();
		return os.contains("win") ? "node.exe" : "node";
	}

	private static int freePort() throws IOException {
		try (ServerSocket socket = new ServerSocket(0, 0, InetAddress.getByName("127.0.0.1"))) {
			return socket.getLocalPort();
		}
	}

	private static void waitUntilReady(String url, Process process, Duration timeout)
			throws IOException, InterruptedException {
		long deadline = System.nanoTime() + timeout.toNanos();
		URI uri = URI.create(url);
		while (System.nanoTime() < deadline) {
			if (!process.isAlive()) {
				throw new IllegalStateException("Frontend process exited before becoming ready");
			}
			if (httpOk(uri)) {
				return;
			}
			Thread.sleep(250);
		}
		throw new IllegalStateException("Frontend did not become ready at " + url + " within " + timeout);
	}

	private static boolean httpOk(URI uri) {
		try {
			HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
			connection.setConnectTimeout(1000);
			connection.setReadTimeout(2000);
			connection.setRequestMethod("GET");
			int status = connection.getResponseCode();
			connection.disconnect();
			return status >= 200 && status < 400;
		}
		catch (IOException ignored) {
			return false;
		}
	}

	private static void pumpOutput(Process process) {
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				System.out.println("[frontend] " + line);
			}
		}
		catch (IOException ignored) {
			// Process closed.
		}
	}
}
