package com.prcodeinspector.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prcodeinspector.model.AnalysisIssue;
import com.prcodeinspector.model.AnalysisSuggestion;
import com.prcodeinspector.model.CodeAnalysisResult;

@Service
public class OpenAIClientService {

	@Autowired
	private ChatModel chatModel;

	@Autowired
	private ObjectMapper objectMapper;

	@Value("${spring.ai.openai.chat.options.temperature:0.1}")
	private Double temperature;

	@Value("${spring.ai.openai.chat.options.max-tokens:2000}")
	private Integer maxTokens;

	public CodeAnalysisResult analyzeCode(String filePath, String fileName, String language, String code) {
		try {
			AnalysisRules rules = loadAnalysisRules();
			String prompt = buildAnalysisPrompt(filePath, fileName, language, code, rules);
			String aiResponse = callOpenAI(prompt);

			// Parse the AI response
			return parseAIResponse(filePath, fileName, language, code, aiResponse);

		} catch (Exception e) {
			throw new RuntimeException("Failed to analyze code with OpenAI: " + e.getMessage(), e);
		}
	}

	public List<CodeAnalysisResult> analyzeMultipleFiles(Map<String, String> codeFiles) {
		return codeFiles.entrySet().stream().map(entry -> {
			String filePath = entry.getKey();
			String code = entry.getValue();
			String fileName = getFileName(filePath);
			String language = getLanguageFromFileName(fileName);
			return analyzeCode(filePath, fileName, language, code);
		}).collect(Collectors.toList());
	}

	private String callOpenAI(String prompt) {
		Prompt request = new Prompt(prompt);
		ChatResponse response = chatModel.call(request);
		return response.getResult().getOutput().getContent();
	}

	private String buildAnalysisPrompt(String filePath, String fileName, String language, String code,
			AnalysisRules rules) {
		StringBuilder prompt = new StringBuilder();

		prompt.append(
				"You are an expert code reviewer with deep knowledge of software security, performance optimization, and best practices. ");
		prompt.append("Analyze the following code and provide detailed feedback.\n\n");

		prompt.append("File Information:\n");
		prompt.append("- Path: ").append(filePath).append("\n");
		prompt.append("- Language: ").append(language).append("\n");
		prompt.append("- Name: ").append(fileName).append("\n\n");

		prompt.append("Code to analyze:\n");
		prompt.append("```").append(language).append("\n");
		prompt.append(code).append("\n");
		prompt.append("```\n\n");

		prompt.append(
				"Please analyze this code and provide your response in JSON format with the following structure:\n\n");
		prompt.append("{\n");
		prompt.append("  \"summary\": \"Brief summary of the analysis\",\n");
		prompt.append("  \"securityScore\": 0-100,\n");
		prompt.append("  \"performanceScore\": 0-100,\n");
		prompt.append("  \"bestPracticesScore\": 0-100,\n");
		prompt.append("  \"issues\": [\n");
		prompt.append("    {\n");
		prompt.append("      \"category\": \"SECURITY|PERFORMANCE|BEST_PRACTICE\",\n");
		prompt.append("      \"severity\": \"HIGH|MEDIUM|LOW\",\n");
		prompt.append("      \"title\": \"Issue title\",\n");
		prompt.append("      \"description\": \"Detailed description\",\n");
		prompt.append("      \"lineNumber\": 123,\n");
		prompt.append("      \"codeSnippet\": \"Problematic code snippet\",\n");
		prompt.append("      \"recommendation\": \"How to fix the issue\"\n");
		prompt.append("    }\n");
		prompt.append("  ],\n");
		prompt.append("  \"suggestions\": [\n");
		prompt.append("    {\n");
		prompt.append("      \"type\": \"REFACTORING|OPTIMIZATION|CONVENTION|ENHANCEMENT\",\n");
		prompt.append("      \"title\": \"Suggestion title\",\n");
		prompt.append("      \"description\": \"Detailed suggestion\",\n");
		prompt.append("      \"suggestedCode\": \"Improved code\",\n");
		prompt.append("      \"benefits\": \"Benefits of the change\",\n");
		prompt.append("      \"effort\": \"LOW|MEDIUM|HIGH\"\n");
		prompt.append("    }\n");
		prompt.append("  ]\n");
		prompt.append("}\n\n");

		prompt.append("Focus on the following areas:\n");
		prompt.append("- Security vulnerabilities\n");
		prompt.append("- Performance issues\n");
		prompt.append("- Best practices violations\n");
		prompt.append("- Code maintainability\n");
		prompt.append("- Potential bugs\n");

		if (rules != null && rules.getSpecificRules() != null) {
			prompt.append("\nCustom analysis rules:\n");
			prompt.append(rules.getSpecificRules());
		}

		return prompt.toString();
	}

	private CodeAnalysisResult parseAIResponse(String filePath, String fileName, String language, String code,
			String aiResponse) {
		try {
			// Clean the response
			String cleanResponse = cleanJsonResponse(aiResponse);

			Map<String, Object> responseMap = objectMapper.readValue(cleanResponse, Map.class);

			CodeAnalysisResult result = new CodeAnalysisResult(filePath, fileName,
					CodeAnalysisResult.AnalysisType.MODIFIED);
			result.setLanguage(language);
			result.setChangedCode(code);
			result.setSummary((String) responseMap.get("summary"));
			result.setSecurityScore(getDouble(responseMap.get("securityScore")));
			result.setPerformanceScore(getDouble(responseMap.get("performanceScore")));
			result.setBestPracticesScore(getDouble(responseMap.get("bestPracticesScore")));

			Double overallScore = calculateOverallScore(result);
			result.setOverallScore(overallScore);

			// Parse issues
			List<Map<String, Object>> issuesList = (List<Map<String, Object>>) responseMap.get("issues");
			if (issuesList != null) {
				for (Map<String, Object> issueMap : issuesList) {
					AnalysisIssue issue = parseIssue(issueMap);
					result.getIssues().add(issue);
				}
			}

			// Parse suggestions
			List<Map<String, Object>> suggestionsList = (List<Map<String, Object>>) responseMap.get("suggestions");
			if (suggestionsList != null) {
				for (Map<String, Object> suggestionMap : suggestionsList) {
					AnalysisSuggestion suggestion = parseSuggestion(suggestionMap);
					result.getSuggestions().add(suggestion);
				}
			}

			return result;

		} catch (Exception e) {
			throw new RuntimeException("Failed to parse AI response: " + e.getMessage(), e);
		}
	}

	private String cleanJsonResponse(String response) {
		// Remove any markdown code block markers
		response = response.replaceAll("```json", "").replaceAll("```", "");

		int startIndex = response.indexOf('{');
		int endIndex = response.lastIndexOf('}');

		if (startIndex >= 0 && endIndex > startIndex) {
			return response.substring(startIndex, endIndex + 1);
		}

		return response.trim();
	}

	private AnalysisIssue parseIssue(Map<String, Object> issueMap) {
		AnalysisIssue issue = new AnalysisIssue();
		issue.setCategory((String) issueMap.get("category"));
		issue.setSeverity((String) issueMap.get("severity"));
		issue.setTitle((String) issueMap.get("title"));
		issue.setDescription((String) issueMap.get("description"));
		issue.setLineNumber(
				issueMap.get("lineNumber") != null ? ((Number) issueMap.get("lineNumber")).intValue() : null);
		issue.setCodeSnippet((String) issueMap.get("codeSnippet"));
		issue.setRecommendation((String) issueMap.get("recommendation"));
		return issue;
	}

	private AnalysisSuggestion parseSuggestion(Map<String, Object> suggestionMap) {
		AnalysisSuggestion suggestion = new AnalysisSuggestion();
		suggestion.setType((String) suggestionMap.get("type"));
		suggestion.setTitle((String) suggestionMap.get("title"));
		suggestion.setDescription((String) suggestionMap.get("description"));
		suggestion.setLineNumber(
				suggestionMap.get("lineNumber") != null ? ((Number) suggestionMap.get("lineNumber")).intValue() : null);
		suggestion.setCodeSnippet((String) suggestionMap.get("codeSnippet"));
		suggestion.setSuggestedCode((String) suggestionMap.get("suggestedCode"));
		suggestion.setBenefits((String) suggestionMap.get("benefits"));
		suggestion.setEffort((String) suggestionMap.get("effort"));
		return suggestion;
	}

	private Double getDouble(Object value) {
		if (value instanceof Number) {
			return ((Number) value).doubleValue();
		}
		return 0.0;
	}

	private Double calculateOverallScore(CodeAnalysisResult result) {
		if (result.getSecurityScore() != null && result.getPerformanceScore() != null
				&& result.getBestPracticesScore() != null) {
			return (result.getSecurityScore() + result.getPerformanceScore() + result.getBestPracticesScore()) / 3.0;
		}
		return 0.0;
	}

	private String getFileName(String filePath) {
		if (StringUtils.hasText(filePath)) {
			String[] parts = filePath.split("/");
			return parts[parts.length - 1];
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
		return "unknown";
	}

	private AnalysisRules loadAnalysisRules() {
		// For now, return hardcoded default rules
		AnalysisRules rules = new AnalysisRules();
		rules.setSpecificRules("Focus on Spring Boot best practices for Java files.");
		return rules;
	}

	public static class AnalysisRules {
		private String specificRules;

		public String getSpecificRules() {
			return specificRules;
		}

		public void setSpecificRules(String specificRules) {
			this.specificRules = specificRules;
		}
	}
}