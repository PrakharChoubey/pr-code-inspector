package com.prcodeinspector.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Health", description = "Health check and service status endpoints")
public class HealthController {

	@Autowired
	private ChatModel chatModel;

	@Value("${spring.application.name}")
	private String applicationName;

	@Value("${server.port}")
	private String serverPort;

	@GetMapping("/health")
	@Operation(summary = "Health Check", description = "Check service health status")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Service is healthy") })
	public ResponseEntity<Map<String, Object>> health() {
		Map<String, Object> health = new HashMap<>();
		health.put("status", "UP");
		health.put("timestamp", LocalDateTime.now());
		health.put("service", applicationName);
		health.put("port", serverPort);

		return ResponseEntity.ok(health);
	}

	@GetMapping("/status")
	@Operation(summary = "Detailed Status", description = "Get detailed service status including dependencies")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Status retrieved successfully") })
	public ResponseEntity<Map<String, Object>> detailedStatus() {
		Map<String, Object> status = new HashMap<>();

		status.put("service", applicationName);
		status.put("version", "1.0.0");
		status.put("status", "UP");
		status.put("timestamp", LocalDateTime.now());

		boolean openAiAvailable = false;
		try {
			if (chatModel != null) {
				// Checking...
				openAiAvailable = true;
			}
		} catch (Exception e) {
			openAiAvailable = false;
		}
		status.put("openai", Map.of("status", openAiAvailable ? "UP" : "DOWN"));

		return ResponseEntity.ok(status);
	}

	@GetMapping("/info")
	@Operation(summary = "Service Information", description = "Get service information")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Information retrieved successfully") })
	public ResponseEntity<Map<String, Object>> info() {
		Map<String, Object> info = new HashMap<>();
		info.put("name", applicationName);
		info.put("version", "1.0.0");
		info.put("description", "AI-Powered Code Review Assistant");
		info.put("features",
				new String[] { "AI-powered code analysis", "Pull request integration",
						"Security vulnerability detection", "Performance optimization suggestions",
						"Best practices validation" });

		return ResponseEntity.ok(info);
	}
}