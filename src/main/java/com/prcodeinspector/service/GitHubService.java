package com.prcodeinspector.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHPullRequestFileDetail;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Service
public class GitHubService {

	@Autowired
	private ObjectMapper objectMapper;

	@Value("${github.token}")
	private String githubToken;

	@Value("${github.api.url}")
	private String githubApiUrl;

	private static final String GITHUB_API_VERSION = "2022-11-28";
	private static final int CONNECT_TIMEOUT = 30;
	private static final int READ_TIMEOUT = 60;

	private final OkHttpClient client = new OkHttpClient.Builder()
			.connectTimeout(CONNECT_TIMEOUT, java.util.concurrent.TimeUnit.SECONDS)
			.readTimeout(READ_TIMEOUT, java.util.concurrent.TimeUnit.SECONDS).build();

	private GitHub gitHubInstance;

	private GitHub getGitHubInstance() {
		if (gitHubInstance == null) {
			try {
				gitHubInstance = new GitHubBuilder().withOAuthToken(githubToken).build();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return gitHubInstance;
	}

	public PullRequestInfo getPullRequestInfo(String owner, String repository, int prNumber) throws IOException {
//		GHUser user = getGitHubInstance().getUser(owner);
		GHRepository repo = getGitHubInstance().getRepository(owner + "/" + repository);
		GHPullRequest pr = repo.getPullRequest(prNumber);

		PullRequestInfo info = new PullRequestInfo();
		info.setOwner(owner);
		info.setRepository(repository);
		info.setPrNumber(prNumber);
		info.setTitle(pr.getTitle());
		info.setDescription(pr.getBody());
		info.setAuthor(pr.getUser().getLogin());
		info.setCreatedAt(pr.getCreatedAt());
		info.setUpdatedAt(pr.getUpdatedAt());
		info.setState(pr.getState().name());
		info.setMergeable(pr.getMergeable());
		info.setMergeableState(pr.getMergeableState());

		return info;
	}

	public List<PullRequestFile> getPullRequestFiles(String owner, String repository, int prNumber) throws IOException {
		List<PullRequestFile> files = new ArrayList<>();

		List<GHPullRequestFileDetail> ghFiles = getGitHubInstance().getRepository(owner + "/" + repository)
				.getPullRequest(prNumber).listFiles().toList();

		for (GHPullRequestFileDetail ghFile : ghFiles) {
			PullRequestFile file = new PullRequestFile();
			file.setFileName(ghFile.getFilename());
			file.setFilePath(ghFile.getFilename());
			file.setStatus(ghFile.getStatus());
			file.setAdditions(ghFile.getAdditions());
			file.setDeletions(ghFile.getDeletions());
			file.setChanges(ghFile.getChanges());
			file.setPatch(ghFile.getPatch());

			// Get file content based on status
			switch (ghFile.getStatus()) {
			case "ADDED":
				file.setNewContent(getFileContent(owner, repository, ghFile.getFilename(), prNumber, "HEAD"));
				break;
			case "MODIFIED":
				file.setOriginalContent(getFileContent(owner, repository, ghFile.getFilename(), prNumber, "BASE"));
				file.setNewContent(getFileContent(owner, repository, ghFile.getFilename(), prNumber, "HEAD"));
				break;
			case "DELETED":
				file.setOriginalContent(getFileContent(owner, repository, ghFile.getFilename(), prNumber, "BASE"));
				break;
			}

			files.add(file);
		}

		return files;
	}

	public String getFileContent(String owner, String repository, String filePath, int prNumber, String version)
			throws IOException {
//		String url = String.format("%s/repos/%s/%s/pulls/%d/files/%s", githubApiUrl, owner, repository, prNumber,
//				filePath);

		if ("HEAD".equals(version) && fileHasBeenModified(owner, repository, filePath, prNumber)) {
			// Get content from the branch
			String branchName = getGitHubInstance().getRepository(owner + "/" + repository).getPullRequest(prNumber)
					.getHead().getRef();
			return getContentFromBranch(owner, repository, filePath, branchName);
		} else {
			return getContentFromBase(owner, repository, filePath);
		}
	}

	private String getContentFromBase(String owner, String repository, String filePath) throws IOException {
		String url = String.format("%s/repos/%s/%s/contents/%s", githubApiUrl, owner, repository, filePath);

		Request request = new Request.Builder().url(url).header("Authorization", "token " + githubToken)
				.header("Accept", "application/vnd.github.v3+json").header("X-GitHub-Api-Version", GITHUB_API_VERSION)
				.build();

		try (Response response = client.newCall(request).execute()) {
			if (!response.isSuccessful()) {
				throw new IOException("Failed to get file content: " + response.code());
			}

			String responseBody = response.body().string();
			JsonNode jsonNode = objectMapper.readTree(responseBody);

			if (jsonNode.has("content")) {
				return decodeBase64Content(jsonNode.get("content").asText());
			}
		}

		return "";
	}

	private String getContentFromBranch(String owner, String repository, String filePath, String branch)
			throws IOException {
		String url = String.format("%s/repos/%s/%s/contents/%s?ref=%s", githubApiUrl, owner, repository, filePath,
				branch);

		Request request = new Request.Builder().url(url).header("Authorization", "token " + githubToken)
				.header("Accept", "application/vnd.github.v3+json").header("X-GitHub-Api-Version", GITHUB_API_VERSION)
				.build();

		try (Response response = client.newCall(request).execute()) {
			if (!response.isSuccessful()) {
				throw new IOException("Failed to get file content from branch: " + response.code());
			}

			String responseBody = response.body().string();
			JsonNode jsonNode = objectMapper.readTree(responseBody);

			if (jsonNode.has("content")) {
				return decodeBase64Content(jsonNode.get("content").asText());
			}
		}

		return "";
	}

	private boolean fileHasBeenModified(String owner, String repository, String filePath, int prNumber) {
		try {
			List<GHPullRequestFileDetail> files = getGitHubInstance().getRepository(owner + "/" + repository)
					.getPullRequest(prNumber).listFiles().toList();

			return files.stream()
					.anyMatch(ghFile -> ghFile.getFilename().equals(filePath) && ghFile.getStatus().equals("MODIFIED"));
		} catch (IOException e) {
			return false;
		}
	}

	public RepositoryInfo getRepositoryInfo(String owner, String repository) throws IOException {
		GHRepository repo = getGitHubInstance().getRepository(owner + "/" + repository);

		RepositoryInfo info = new RepositoryInfo();
		info.setOwner(owner);
		info.setRepository(repository);
		info.setName(repo.getName());
		info.setDescription(repo.getDescription());
		info.setLanguage(repo.getLanguage());
		info.setStars(repo.getStargazersCount());
		info.setForks(repo.getForksCount());
		info.setSize(repo.getSize());
		info.setDefaultBranch(repo.getDefaultBranch());

		return info;
	}

	public Map<String, String> getCodeFilesForAnalysis(String owner, String repository, int prNumber)
			throws IOException {
		List<PullRequestFile> prFiles = getPullRequestFiles(owner, repository, prNumber);

		Map<String, String> codeFiles = new HashMap<>();

		for (PullRequestFile file : prFiles) {
			if (shouldAnalyzeFile(file.getFileName())) {
				if (StringUtils.hasText(file.getNewContent())) {
					codeFiles.put(file.getFilePath(), file.getNewContent());
				} else if (StringUtils.hasText(file.getOriginalContent())) {
					codeFiles.put(file.getFilePath(), file.getOriginalContent());
				}
			}
		}

		return codeFiles;
	}

	private boolean shouldAnalyzeFile(String fileName) {
		List<String> supportedExtensions = Arrays.asList(".java", ".js", ".jsx", ".ts", ".tsx", ".py", ".go", ".rb",
				".php", ".cpp", ".c", ".cs", ".kt", ".swift", ".scala", ".rs");

		return supportedExtensions.stream().anyMatch(fileName::endsWith);
	}

	private String decodeBase64Content(String base64Content) {
		try {
			byte[] decodedBytes = java.util.Base64.getDecoder().decode(base64Content);
			return new String(decodedBytes, "UTF-8");
		} catch (Exception e) {
			throw new RuntimeException("Failed to decode base64 content", e);
		}
	}

	public static class PullRequestInfo {
		private String owner;
		private String repository;
		private int prNumber;
		private String title;
		private String description;
		private String author;
		private Date createdAt;
		private Date updatedAt;
		private String state;
		private boolean mergeable;
		private String mergeableState;

		// Getters and setters
		public String getOwner() {
			return owner;
		}

		public void setOwner(String owner) {
			this.owner = owner;
		}

		public String getRepository() {
			return repository;
		}

		public void setRepository(String repository) {
			this.repository = repository;
		}

		public int getPrNumber() {
			return prNumber;
		}

		public void setPrNumber(int prNumber) {
			this.prNumber = prNumber;
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

		public String getAuthor() {
			return author;
		}

		public void setAuthor(String author) {
			this.author = author;
		}

		public Date getCreatedAt() {
			return createdAt;
		}

		public void setCreatedAt(Date createdAt) {
			this.createdAt = createdAt;
		}

		public Date getUpdatedAt() {
			return updatedAt;
		}

		public void setUpdatedAt(Date updatedAt) {
			this.updatedAt = updatedAt;
		}

		public String getState() {
			return state;
		}

		public void setState(String state) {
			this.state = state;
		}

		public boolean isMergeable() {
			return mergeable;
		}

		public void setMergeable(boolean mergeable) {
			this.mergeable = mergeable;
		}

		public String getMergeableState() {
			return mergeableState;
		}

		public void setMergeableState(String mergeableState) {
			this.mergeableState = mergeableState;
		}
	}

	public static class PullRequestFile {
		private String fileName;
		private String filePath;
		private String status;
		private int additions;
		private int deletions;
		private int changes;
		private String patch;
		private String originalContent;
		private String newContent;

		// Getters and setters
		public String getFileName() {
			return fileName;
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		public String getFilePath() {
			return filePath;
		}

		public void setFilePath(String filePath) {
			this.filePath = filePath;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public int getAdditions() {
			return additions;
		}

		public void setAdditions(int additions) {
			this.additions = additions;
		}

		public int getDeletions() {
			return deletions;
		}

		public void setDeletions(int deletions) {
			this.deletions = deletions;
		}

		public int getChanges() {
			return changes;
		}

		public void setChanges(int changes) {
			this.changes = changes;
		}

		public String getPatch() {
			return patch;
		}

		public void setPatch(String patch) {
			this.patch = patch;
		}

		public String getOriginalContent() {
			return originalContent;
		}

		public void setOriginalContent(String originalContent) {
			this.originalContent = originalContent;
		}

		public String getNewContent() {
			return newContent;
		}

		public void setNewContent(String newContent) {
			this.newContent = newContent;
		}
	}

	public static class RepositoryInfo {
		private String owner;
		private String repository;
		private String name;
		private String description;
		private String language;
		private int stars;
		private int forks;
		private int size;
		private String defaultBranch;

		// Getters and setters
		public String getOwner() {
			return owner;
		}

		public void setOwner(String owner) {
			this.owner = owner;
		}

		public String getRepository() {
			return repository;
		}

		public void setRepository(String repository) {
			this.repository = repository;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getLanguage() {
			return language;
		}

		public void setLanguage(String language) {
			this.language = language;
		}

		public int getStars() {
			return stars;
		}

		public void setStars(int stars) {
			this.stars = stars;
		}

		public int getForks() {
			return forks;
		}

		public void setForks(int forks) {
			this.forks = forks;
		}

		public int getSize() {
			return size;
		}

		public void setSize(int size) {
			this.size = size;
		}

		public String getDefaultBranch() {
			return defaultBranch;
		}

		public void setDefaultBranch(String defaultBranch) {
			this.defaultBranch = defaultBranch;
		}
	}
}