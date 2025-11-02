package com.prcodeinspector.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.prcodeinspector.model.PullRequestAnalysis;
import com.prcodeinspector.service.CodeReviewService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/analysis")
@Tag(name = "Code Review Analysis", description = "Endpoints for analyzing code with AI")
public class AnalysisController {

	@Autowired
	private CodeReviewService codeReviewService;

	@PostMapping("/code")
	@Operation(summary = "Analyze Code Snippet", description = "Analyze a single code snippet")
	public ResponseEntity<?> analyzeCode(
			@Parameter(description = "File path", required = true) @RequestParam String filePath,
			@Parameter(description = "Programming language", required = true) @RequestParam String language,
			@Parameter(description = "Code content to analyze", required = true) @RequestBody String code) {

		try {
			var result = codeReviewService.analyzeCode(filePath, language, code);
			return ResponseEntity.ok(result);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}

	@GetMapping("/{analysisId}")
	@Operation(summary = "Get Analysis", description = "Get analysis by ID")
	public ResponseEntity<?> getAnalysis(
			@Parameter(description = "Analysis ID", required = true) @PathVariable Long analysisId) {

		Optional<PullRequestAnalysis> analysis = codeReviewService.getAnalysis(analysisId);

		// TODO if analysis.isPresent
	}

	@GetMapping("/repository")
	@Operation(summary = "Get Repository Analyses", description = "Get all analyses for a repository")
	public ResponseEntity<?> getRepositoryAnalyses(
			@Parameter(description = "Repository owner", required = true) @RequestParam String owner,
			@Parameter(description = "Repository name", required = true) @RequestParam String repository,
			@Parameter(description = "Page number (0-based)", required = false) @RequestParam(defaultValue = "0") int page,
			@Parameter(description = "Page size", required = false) @RequestParam(defaultValue = "20") int size) {

		try {
			List<PullRequestAnalysis> analyses = codeReviewService.getRepositoryAnalyses(owner, repository, page, size);
			return ResponseEntity.ok(analyses);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}

	@GetMapping("/status/{status}")
	@Operation(summary = "Get Analyses by Status", description = "Get analyses filtered by status")
	public ResponseEntity<?> getAnalysesByStatus(
			@Parameter(description = "Analysis status", required = true) @PathVariable PullRequestAnalysis.AnalysisStatus status,
			@Parameter(description = "Page number (0-based)", required = false) @RequestParam(defaultValue = "0") int page,
			@Parameter(description = "Page size", required = false) @RequestParam(defaultValue = "20") int size) {

		try {
			List<PullRequestAnalysis> analyses = codeReviewService.getAnalysesByStatus(status, page, size);
			return ResponseEntity.ok(analyses);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}

}