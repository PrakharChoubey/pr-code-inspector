package com.prcodeinspector.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;

@Embeddable
public class AnalysisSuggestion {

	@NotBlank
	private String type; // REFACTORING, OPTIMIZATION, CONVENTION, ENHANCEMENT

	@NotBlank
	private String title;

	@NotBlank
	private String description;

	private String filePath;

	private Integer lineNumber;

	private String effort; // LOW, MEDIUM, HIGH

	// Constructors
	public AnalysisSuggestion() {
	}

	public AnalysisSuggestion(String type, String title, String description) {
		this.type = type;
		this.title = title;
		this.description = description;
	}

	// Getters and Setters
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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

	public String getEffort() {
		return effort;
	}

	public void setEffort(String effort) {
		this.effort = effort;
	}

}