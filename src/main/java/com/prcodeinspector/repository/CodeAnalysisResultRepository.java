package com.prcodeinspector.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.prcodeinspector.model.CodeAnalysisResult;

@Repository
public interface CodeAnalysisResultRepository extends JpaRepository<CodeAnalysisResult, Long> {

	@Query("SELECT car FROM CodeAnalysisResult car WHERE car.pullRequestAnalysis.id = :analysisId ORDER BY car.filePath")
	List<CodeAnalysisResult> findByPullRequestAnalysisId(@Param("analysisId") Long analysisId);

	@Query("SELECT car FROM CodeAnalysisResult car WHERE car.filePath LIKE :pattern ORDER BY car.filePath")
	List<CodeAnalysisResult> findByFilePathPattern(@Param("pattern") String pattern);

	@Query("SELECT car FROM CodeAnalysisResult car JOIN car.issues issue WHERE issue.severity = 'HIGH' AND car.pullRequestAnalysis.id = :analysisId")
	List<CodeAnalysisResult> findWithHighSeverityIssues(@Param("analysisId") Long analysisId);

	@Query("SELECT AVG(car.overallScore), AVG(car.securityScore), AVG(car.performanceScore), AVG(car.bestPracticesScore) "
			+ "FROM CodeAnalysisResult car WHERE car.pullRequestAnalysis.id = :analysisId")
	Object[] calculateAverageScores(@Param("analysisId") Long analysisId);

	@Query("SELECT car FROM CodeAnalysisResult car WHERE car.language = :language ORDER BY car.filePath")
	List<CodeAnalysisResult> findByLanguage(@Param("language") String language);

	@Query("SELECT issue.severity, COUNT(issue) FROM CodeAnalysisResult car JOIN car.issues issue "
			+ "WHERE car.pullRequestAnalysis.id = :analysisId GROUP BY issue.severity")
	List<Object[]> countIssuesBySeverity(@Param("analysisId") Long analysisId);
}