package com.prcodeinspector.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kohsuke.github.GHPullRequestFileDetail;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class GitHubService {

	@Value("${github.token}")
	private String githubToken;

	@Value("${github.api.url}")
	private String githubApiUrl;

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

			// Get file content based on status
			switch (ghFile.getStatus()) {
			case "ADDED":
				file.setNewContent(getFileContent(owner, repository, ghFile.getFilename(), prNumber, "HEAD"));
				break;
			case "MODIFIED":
				file.setNewContent(getFileContent(owner, repository, ghFile.getFilename(), prNumber, "HEAD"));
				break;
			case "DELETED":
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

		List<GHPullRequestFileDetail> files = getGitHubInstance().getRepository(owner + "/" + repository)
				.getPullRequest(prNumber).listFiles().toList();

		boolean isModified = files.stream()
				.anyMatch(ghFile -> ghFile.getFilename().equals(filePath) && ghFile.getStatus().equals("MODIFIED"));

		if ("HEAD".equals(version) && isModified) {
			// Get content from the branch
			String branchName = getGitHubInstance().getRepository(owner + "/" + repository).getPullRequest(prNumber)
					.getHead().getRef();
			return "";
		} else {
			return "";
		}
	}

	public Map<String, String> getCodeFilesForAnalysis(String owner, String repository, int prNumber)
			throws IOException {
		List<PullRequestFile> prFiles = getPullRequestFiles(owner, repository, prNumber);

		Map<String, String> codeFiles = new HashMap<>();

		for (PullRequestFile file : prFiles) {
			if (StringUtils.hasText(file.getNewContent())) {
				codeFiles.put(file.getFilePath(), file.getNewContent());
			}
		}

		return codeFiles;
	}

	public static class PullRequestFile {
		private String fileName;
		private String filePath;
		private String status;
		private int additions;
		private int deletions;
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

		public String getNewContent() {
			return newContent;
		}

		public void setNewContent(String newContent) {
			this.newContent = newContent;
		}
	}
}