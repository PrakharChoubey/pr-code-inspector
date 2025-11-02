package com.prcodeinspector.service;

import com.prcodeinspector.model.*;
import com.prcodeinspector.model.PullRequestAnalysis.AnalysisStatus;
import com.prcodeinspector.repository.CodeAnalysisResultRepository;
import com.prcodeinspector.repository.PullRequestAnalysisRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Transactional
public class CodeReviewService {

	private static final Logger logger = LoggerFactory.getLogger(CodeReviewService.class);

	@Autowired
	private CodeAnalysisResultRepository resultRepository;

	@Value("${code.analysis.supported-extensions}")
	private String supportedExtensions;

	@Value("${code.analysis.max-file-size}")
	private long maxFileSize;

	@Async
	public CompletableFuture<PullRequestAnalysis> analyzePullRequest(String owner, String repository, int prNumber) {
		logger.info("Starting analysis for PR: {}/{}#{}", owner, repository, prNumber);

		return null;
	}

	public Object analyzeCode(String filePath, String language, String code) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<PullRequestAnalysis> getRepositoryAnalyses(String owner, String repository, int page, int size) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<PullRequestAnalysis> getAnalysesByStatus(AnalysisStatus status, int page, int size) {
		// TODO Auto-generated method stub
		return null;
	}

	public Optional<PullRequestAnalysis> getAnalysis(Long analysisId) {
		// TODO Auto-generated method stub
		return null;
	}

}