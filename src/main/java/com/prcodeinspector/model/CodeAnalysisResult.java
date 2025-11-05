package com.prcodeinspector.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "code_analysis_results")
public class CodeAnalysisResult {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "pull_request_analysis_id")
	private PullRequestAnalysis pullRequestAnalysis;

	@NotBlank
	private String filePath;

	@NotBlank
	private String fileName;

	@NotNull
	@Enumerated(EnumType.STRING)
	private AnalysisType type;

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

	@NotNull
	private LocalDateTime analyzedAt;

	private Double securityScore;

	private Double performanceScore;

	private Double bestPracticesScore;

	private Double overallScore;

	public enum AnalysisType {
		ADDED, MODIFIED, DELETED
	}

	// Constructors
	public CodeAnalysisResult() {
		this.analyzedAt = LocalDateTime.now();
	}

	public CodeAnalysisResult(String filePath, String fileName, AnalysisType type) {
		this.filePath = filePath;
		this.fileName = fileName;
		this.type = type;
		this.analyzedAt = LocalDateTime.now();
	}

	// Getters and Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public PullRequestAnalysis getPullRequestAnalysis() {
		return pullRequestAnalysis;
	}

	public void setPullRequestAnalysis(PullRequestAnalysis pullRequestAnalysis) {
		this.pullRequestAnalysis = pullRequestAnalysis;
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

	public AnalysisType getType() {
		return type;
	}

	public void setType(AnalysisType type) {
		this.type = type;
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

	public void setSuggestions(List<AnalysisSuggestion> suggestions) {
		this.suggestions = suggestions;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public LocalDateTime getAnalyzedAt() {
		return analyzedAt;
	}

	public void setAnalyzedAt(LocalDateTime analyzedAt) {
		this.analyzedAt = analyzedAt;
	}

	public Double getSecurityScore() {
		return securityScore;
	}

	public void setSecurityScore(Double securityScore) {
		this.securityScore = securityScore;
	}

	public Double getPerformanceScore() {
		return performanceScore;
	}

	public void setPerformanceScore(Double performanceScore) {
		this.performanceScore = performanceScore;
	}

	public Double getBestPracticesScore() {
		return bestPracticesScore;
	}

	public void setBestPracticesScore(Double bestPracticesScore) {
		this.bestPracticesScore = bestPracticesScore;
	}

	public Double getOverallScore() {
		return overallScore;
	}

	public void setOverallScore(Double overallScore) {
		this.overallScore = overallScore;
	}

	@Override
	public String toString() {
		return "CodeAnalysisResult{" + "id=" + id + ", filePath='" + filePath + '\'' + ", fileName='" + fileName + '\''
				+ ", type=" + type + ", language='" + language + '\'' + ", overallScore=" + overallScore + '}';
	}
}