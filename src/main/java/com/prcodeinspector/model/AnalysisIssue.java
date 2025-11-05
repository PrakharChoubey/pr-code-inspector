package com.prcodeinspector.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;

@Embeddable
public class AnalysisIssue {

	@NotBlank
	private String category; // SECURITY, PERFORMANCE, BEST_PRACTICE

	@NotBlank
	private String severity; // HIGH, MEDIUM, LOW

	@NotBlank
	private String title;

	@NotBlank
	private String description;

	private String filePath;

	private Integer lineNumber;

	private String codeSnippet;

	private String recommendation;

	// Constructors
	public AnalysisIssue() {
	}

	public AnalysisIssue(String category, String severity, String title, String description) {
		this.category = category;
		this.severity = severity;
		this.title = title;
		this.description = description;
	}

	// Getters and Setters
	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getSeverity() {
		return severity;
	}

	public void setSeverity(String severity) {
		this.severity = severity;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public Integer getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(Integer lineNumber) {
		this.lineNumber = lineNumber;
	}

	public String getCodeSnippet() {
		return codeSnippet;
	}

	public void setCodeSnippet(String codeSnippet) {
		this.codeSnippet = codeSnippet;
	}

	public String getRecommendation() {
		return recommendation;
	}

	public void setRecommendation(String recommendation) {
		this.recommendation = recommendation;
	}

	@Override
	public String toString() {
		return "AnalysisIssue{" + "category='" + category + '\'' + ", severity='" + severity + '\'' + ", title='"
				+ title + '\'' + '}';
	}
}