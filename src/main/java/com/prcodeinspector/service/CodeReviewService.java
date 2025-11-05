package com.prcodeinspector.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.prcodeinspector.model.AnalysisIssue;
import com.prcodeinspector.model.AnalysisSuggestion;
import com.prcodeinspector.model.CodeAnalysisResult;
import com.prcodeinspector.model.PullRequestAnalysis;
import com.prcodeinspector.repository.CodeAnalysisResultRepository;
import com.prcodeinspector.repository.PullRequestAnalysisRepository;

@Service
@Transactional
public class CodeReviewService {

	private static final Logger logger = LoggerFactory.getLogger(CodeReviewService.class);

	@Autowired
	private GitHubService gitHubService;

	@Autowired
	private OpenAIClientService openAIClientService;

	@Autowired
	private PullRequestAnalysisRepository analysisRepository;

	@Autowired
	private CodeAnalysisResultRepository resultRepository;

	@Value("${code.analysis.supported-extensions}")
	private String supportedExtensions;

	@Value("${code.analysis.max-file-size}")
	private long maxFileSize;

	@Async
	public CompletableFuture<PullRequestAnalysis> analyzePullRequest(String owner, String repository, int prNumber) {
		logger.info("Starting analysis for PR: {}/{}#{}", owner, repository, prNumber);

		try {
			// Check if analysis already exists
			PullRequestAnalysis existingAnalysis = analysisRepository.findByOwnerRepositoryAndPRNumber(owner,
					repository, prNumber);

			if (existingAnalysis != null
					&& existingAnalysis.getStatus() == PullRequestAnalysis.AnalysisStatus.COMPLETED) {
				logger.info("Analysis already exists for PR: {}/{}#{}", owner, repository, prNumber);
				return CompletableFuture.completedFuture(existingAnalysis);
			}

			PullRequestAnalysis analysis = new PullRequestAnalysis(owner, repository, "");
			analysis.setPullRequestNumber(prNumber);
			analysis.setStatus(PullRequestAnalysis.AnalysisStatus.IN_PROGRESS);
			analysisRepository.save(analysis);

			// Get PR information and files
			GitHubService.PullRequestInfo prInfo = gitHubService.getPullRequestInfo(owner, repository, prNumber);
			analysis.setBranchName(prInfo.getTitle());

			List<GitHubService.PullRequestFile> prFiles = gitHubService.getPullRequestFiles(owner, repository,
					prNumber);
			Map<String, String> codeFiles = gitHubService.getCodeFilesForAnalysis(owner, repository, prNumber);

			logger.info("Found {} files to analyze for PR: {}/{}#{}", codeFiles.size(), owner, repository, prNumber);

			// Analyze each file
			List<CodeAnalysisResult> results = new ArrayList<>();
			for (Map.Entry<String, String> fileEntry : codeFiles.entrySet()) {
				String filePath = fileEntry.getKey();
				String code = fileEntry.getValue();

				try {
					// Skip files that are too large
					if (code.length() > maxFileSize) {
						logger.warn("Skipping file {} due to size limit", filePath);
						continue;
					}

					CodeAnalysisResult result = openAIClientService.analyzeCode(filePath, getFileName(filePath),
							getLanguageFromFileName(filePath), code);

					result.setPullRequestAnalysis(analysis);
					results.add(result);

					// Save individual result
					resultRepository.save(result);

				} catch (Exception e) {
					logger.error("Failed to analyze file: {}", filePath, e);
					// Continue with other files
				}
			}

			analysis.setStatus(PullRequestAnalysis.AnalysisStatus.COMPLETED);
			analysisRepository.save(analysis);

			logger.info("Analysis completed for PR: {}/{}#{}", owner, repository, prNumber);

			return CompletableFuture.completedFuture(analysis);

		} catch (Exception e) {
			logger.error("Failed to analyze PR: {}/{}#{}", owner, repository, prNumber, e);

			// Update analysis with error
			PullRequestAnalysis analysis = null;
			try {
				analysis = analysisRepository.findByOwnerRepositoryAndPRNumber(owner, repository, prNumber);
				if (analysis != null) {
					analysis.setStatus(PullRequestAnalysis.AnalysisStatus.FAILED);
					analysis.setErrorMessage(e.getMessage());
					analysisRepository.save(analysis);
				}
			} catch (Exception updateException) {
				logger.error("Failed to update analysis with error", updateException);
			}

			return CompletableFuture.failedFuture(e);
		}
	}

	public CodeAnalysisResult analyzeCode(String filePath, String language, String code) {
		if (!isFileSupported(filePath)) {
			throw new IllegalArgumentException("File type not supported: " + filePath);
		}

		if (code.length() > maxFileSize) {
			throw new IllegalArgumentException("File size exceeds limit: " + code.length());
		}

		return openAIClientService.analyzeCode(filePath, getFileName(filePath), language, code);
	}

	public Optional<PullRequestAnalysis> getAnalysis(Long analysisId) {
		return analysisRepository.findById(analysisId);
	}

	public Optional<PullRequestAnalysis> getAnalysisByPR(String owner, String repository, int prNumber) {
		return Optional.ofNullable(analysisRepository.findByOwnerRepositoryAndPRNumber(owner, repository, prNumber));
	}

	public List<PullRequestAnalysis> getRepositoryAnalyses(String owner, String repository, int page, int size) {
		org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
		return analysisRepository.findByOwnerAndRepository(owner, repository, pageable).getContent();
	}

	public List<PullRequestAnalysis> getAnalysesByStatus(PullRequestAnalysis.AnalysisStatus status, int page,
			int size) {
		org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
		return analysisRepository.findByStatus(status, pageable).getContent();
	}

	public List<PullRequestAnalysis> getRecentAnalyses(int days) {
		java.time.LocalDateTime since = java.time.LocalDateTime.now().minusDays(days);
		return analysisRepository.findRecentAnalyses(since);
	}

	public List<CodeAnalysisResult> getAnalysisResults(Long analysisId) {
		return resultRepository.findByPullRequestAnalysisId(analysisId);
	}

	public AnalysisSummary getAnalysisSummary(Long analysisId) {
		PullRequestAnalysis analysis = analysisRepository.findById(analysisId)
				.orElseThrow(() -> new IllegalArgumentException("Analysis not found: " + analysisId));

		List<CodeAnalysisResult> results = getAnalysisResults(analysisId);

		AnalysisSummary summary = new AnalysisSummary();
		summary.setAnalysisId(analysisId);
		summary.setTotalFiles(results.size());
		summary.setTotalIssues(0);
		summary.setTotalSuggestions(0);
		summary.setAverageScores(new HashMap<>());

		Map<String, Integer> issuesBySeverity = new HashMap<>();
		Map<String, Integer> suggestionsByType = new HashMap<>();

		for (CodeAnalysisResult result : results) {
			summary.setTotalIssues(summary.getTotalIssues() + result.getIssues().size());
			summary.setTotalSuggestions(summary.getTotalSuggestions() + result.getSuggestions().size());

			// Count issues by severity
			for (AnalysisIssue issue : result.getIssues()) {
				issuesBySeverity.merge(issue.getSeverity(), 1, Integer::sum);
			}

			// Count suggestions by type
			for (AnalysisSuggestion suggestion : result.getSuggestions()) {
				suggestionsByType.merge(suggestion.getType(), 1, Integer::sum);
			}

			// Collect scores
			if (result.getSecurityScore() != null) {
				summary.getAverageScores().put("security",
						summary.getAverageScores().getOrDefault("security", 0.0) + result.getSecurityScore());
			}
			if (result.getPerformanceScore() != null) {
				summary.getAverageScores().put("performance",
						summary.getAverageScores().getOrDefault("performance", 0.0) + result.getPerformanceScore());
			}
			if (result.getBestPracticesScore() != null) {
				summary.getAverageScores().put("bestPractices",
						summary.getAverageScores().getOrDefault("bestPractices", 0.0) + result.getBestPracticesScore());
			}
			if (result.getOverallScore() != null) {
				summary.getAverageScores().put("overall",
						summary.getAverageScores().getOrDefault("overall", 0.0) + result.getOverallScore());
			}
		}

		// Calculate averages
		int fileCount = results.size() > 0 ? results.size() : 1;
		summary.getAverageScores().forEach((key, value) -> summary.getAverageScores().put(key, value / fileCount));

		summary.setIssuesBySeverity(issuesBySeverity);
		summary.setSuggestionsByType(suggestionsByType);

		return summary;
	}

	private boolean isFileSupported(String filePath) {
		String[] extensions = supportedExtensions.split(",");
		return Arrays.asList(extensions).stream().anyMatch(filePath::endsWith);
	}

	private String getFileName(String filePath) {
		if (filePath.contains("/")) {
			return filePath.substring(filePath.lastIndexOf('/') + 1);
		}
		return filePath;
	}

	private String getLanguageFromFileName(String fileName) {
		if (fileName.endsWith(".java"))
			return "java";
		if (fileName.endsWith(".js") || fileName.endsWith(".jsx"))
			return "javascript";
		if (fileName.endsWith(".ts") || fileName.endsWith(".tsx"))
			return "typescript";
		if (fileName.endsWith(".py"))
			return "python";
		if (fileName.endsWith(".go"))
			return "go";
		if (fileName.endsWith(".rb"))
			return "ruby";
		if (fileName.endsWith(".php"))
			return "php";
		if (fileName.endsWith(".cpp") || fileName.endsWith(".cc"))
			return "cpp";
		if (fileName.endsWith(".c"))
			return "c";
		if (fileName.endsWith(".cs"))
			return "csharp";
		if (fileName.endsWith(".kt"))
			return "kotlin";
		if (fileName.endsWith(".swift"))
			return "swift";
		return "unknown";
	}

	public static class AnalysisSummary {
		private Long analysisId;
		private int totalFiles;
		private int totalIssues;
		private int totalSuggestions;
		private Map<String, Double> averageScores;
		private Map<String, Integer> issuesBySeverity;
		private Map<String, Integer> suggestionsByType;

		// Getters and setters
		public Long getAnalysisId() {
			return analysisId;
		}

		public void setAnalysisId(Long analysisId) {
			this.analysisId = analysisId;
		}

		public int getTotalFiles() {
			return totalFiles;
		}

		public void setTotalFiles(int totalFiles) {
			this.totalFiles = totalFiles;
		}

		public int getTotalIssues() {
			return totalIssues;
		}

		public void setTotalIssues(int totalIssues) {
			this.totalIssues = totalIssues;
		}

		public int getTotalSuggestions() {
			return totalSuggestions;
		}

		public void setTotalSuggestions(int totalSuggestions) {
			this.totalSuggestions = totalSuggestions;
		}

		public Map<String, Double> getAverageScores() {
			return averageScores;
		}

		public void setAverageScores(Map<String, Double> averageScores) {
			this.averageScores = averageScores;
		}

		public Map<String, Integer> getIssuesBySeverity() {
			return issuesBySeverity;
		}

		public void setIssuesBySeverity(Map<String, Integer> issuesBySeverity) {
			this.issuesBySeverity = issuesBySeverity;
		}

		public Map<String, Integer> getSuggestionsByType() {
			return suggestionsByType;
		}

		public void setSuggestionsByType(Map<String, Integer> suggestionsByType) {
			this.suggestionsByType = suggestionsByType;
		}
	}
}