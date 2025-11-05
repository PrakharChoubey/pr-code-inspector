package com.prcodeinspector.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.prcodeinspector.model.CodeAnalysisResult;
import com.prcodeinspector.service.CodeReviewService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/results")
@Tag(name = "Analysis Results", description = "Endpoints for retrieving analysis results and statistics")
public class ResultsController {

	@Autowired
	private CodeReviewService codeReviewService;

	@GetMapping("/{analysisId}")
	@Operation(summary = "Get Analysis Results", description = "Get all file analysis results for a specific analysis")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Results retrieved successfully"),
			@ApiResponse(responseCode = "404", description = "Analysis not found"),
			@ApiResponse(responseCode = "500", description = "Internal server error") })
	public ResponseEntity<?> getAnalysisResults(
			@Parameter(description = "Analysis ID", required = true) @PathVariable Long analysisId) {

		try {
			List<CodeAnalysisResult> results = codeReviewService.getAnalysisResults(analysisId);
			return ResponseEntity.ok(results);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}

	@GetMapping("/{analysisId}/summary")
	@Operation(summary = "Get Analysis Summary", description = "Get summary statistics for a specific analysis")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Summary retrieved successfully"),
			@ApiResponse(responseCode = "404", description = "Analysis not found"),
			@ApiResponse(responseCode = "500", description = "Internal server error") })
	public ResponseEntity<?> getAnalysisSummary(
			@Parameter(description = "Analysis ID", required = true) @PathVariable Long analysisId) {

		try {
			var summary = codeReviewService.getAnalysisSummary(analysisId);
			return ResponseEntity.ok(summary);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}
}