package com.prcodeinspector.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "pull_request_analyses")
public class PullRequestAnalysis {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	private String repository;

	@NotBlank
	private String branchName;

	@NotBlank
	private String owner;

	private Integer pullRequestNumber;

	@NotNull
	private LocalDateTime createdAt;

	@NotNull
	@Enumerated(EnumType.STRING)
	private AnalysisStatus status;

	private String commitSha;

	private LocalDateTime updatedAt;

	@OneToMany(mappedBy = "pullRequestAnalysis", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<CodeAnalysisResult> analysisResults = new ArrayList<>();

	private String errorMessage;

	public enum AnalysisStatus {
		PENDING, IN_PROGRESS, COMPLETED, FAILED, CANCELLED
	}

	// Constructors
	public PullRequestAnalysis() {
		this.createdAt = LocalDateTime.now();
		this.status = AnalysisStatus.PENDING;
	}

	public PullRequestAnalysis(String owner, String repository, String branchName) {
		this.owner = owner;
		this.repository = repository;
		this.branchName = branchName;
		this.createdAt = LocalDateTime.now();
		this.status = AnalysisStatus.PENDING;
	}

	// Getters and Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getRepository() {
		return repository;
	}

	public void setRepository(String repository) {
		this.repository = repository;
	}

	public String getBranchName() {
		return branchName;
	}

	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public Integer getPullRequestNumber() {
		return pullRequestNumber;
	}

	public void setPullRequestNumber(Integer pullRequestNumber) {
		this.pullRequestNumber = pullRequestNumber;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public AnalysisStatus getStatus() {
		return status;
	}

	public void setStatus(AnalysisStatus status) {
		this.status = status;
		if (this.status == AnalysisStatus.COMPLETED || this.status == AnalysisStatus.FAILED) {
			this.updatedAt = LocalDateTime.now();
		}
	}

	public String getCommitSha() {
		return commitSha;
	}

	public void setCommitSha(String commitSha) {
		this.commitSha = commitSha;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public List<CodeAnalysisResult> getAnalysisResults() {
		return analysisResults;
	}

	public void setAnalysisResults(List<CodeAnalysisResult> analysisResults) {
		this.analysisResults = analysisResults;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	@Override
	public String toString() {
		return "PullRequestAnalysis{" + "id=" + id + ", owner='" + owner + '\'' + ", repository='" + repository + '\''
				+ ", branchName='" + branchName + '\'' + ", status=" + status + '}';
	}
}