package com.prcodeinspector.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "code_analysis_results")
public class CodeAnalysisResult {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	private String filePath;

	@NotBlank
	private String fileName;

	private String originalCode;

	private String changedCode;

	private String language;

	@ElementCollection
	@CollectionTable(name = "analysis_issues", joinColumns = @JoinColumn(name = "result_id"))
	private List<AnalysisIssue> issues = new ArrayList<>();

	@ElementCollection
	@CollectionTable(name = "analysis_suggestions", joinColumns = @JoinColumn(name = "result_id"))
	private List<AnalysisSuggestion> suggestions = new ArrayList<>();

	private String summary;

	// Constructors
	public CodeAnalysisResult() {
	}

	public CodeAnalysisResult(String filePath, String fileName) {
		this.filePath = filePath;
		this.fileName = fileName;
	}

	// Getters and Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getOriginalCode() {
		return originalCode;
	}

	public void setOriginalCode(String originalCode) {
		this.originalCode = originalCode;
	}

	public String getChangedCode() {
		return changedCode;
	}

	public void setChangedCode(String changedCode) {
		this.changedCode = changedCode;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public List<AnalysisIssue> getIssues() {
		return issues;
	}

	public void setIssues(List<AnalysisIssue> issues) {
		this.issues = issues;
	}

	public List<AnalysisSuggestion> getSuggestions() {
		return suggestions;
	}

	public void setSuggestions(List<AnalysisSuggestion) suggestions) {
        this.suggestions = suggestions;
    }

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

}