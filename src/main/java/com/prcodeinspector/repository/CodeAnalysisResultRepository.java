package com.prcodeinspector.repository;

import com.prcodeinspector.model.CodeAnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CodeAnalysisResultRepository extends JpaRepository<CodeAnalysisResult, Long> {

	@Query("SELECT car FROM CodeAnalysisResult car WHERE car.pullRequestAnalysis.id = :analysisId ORDER BY car.filePath")
	List<CodeAnalysisResult> findByPullRequestAnalysisId(@Param("analysisId") Long analysisId);

	@Query("SELECT car FROM CodeAnalysisResult car WHERE car.filePath LIKE :pattern ORDER BY car.filePath")
	List<CodeAnalysisResult> findByFilePathPattern(@Param("pattern") String pattern);

}