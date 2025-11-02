package com.prcodeinspector.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.prcodeinspector.model.PullRequestAnalysis;

@Repository
public interface PullRequestAnalysisRepository extends JpaRepository<PullRequestAnalysis, Long> {

	@Query("SELECT pra FROM PullRequestAnalysis pra WHERE pra.owner = :owner AND pra.repository = :repository ORDER BY pra.createdAt DESC")
	Page<PullRequestAnalysis> findByOwnerAndRepository(@Param("owner") String owner,
			@Param("repository") String repository, Pageable pageable);

	@Query("SELECT pra FROM PullRequestAnalysis pra WHERE pra.status = :status ORDER BY pra.createdAt DESC")
	Page<PullRequestAnalysis> findByStatus(@Param("status") PullRequestAnalysis.AnalysisStatus status,
			Pageable pageable);

	@Query("SELECT pra FROM PullRequestAnalysis pra WHERE pra.owner = :owner ORDER BY pra.createdAt DESC")
	Page<PullRequestAnalysis> findByOwner(@Param("owner") String owner, Pageable pageable);

	@Query("SELECT pra FROM PullRequestAnalysis pra WHERE pra.createdAt >= :since ORDER BY pra.createdAt DESC")
	List<PullRequestAnalysis> findRecentAnalyses(@Param("since") LocalDateTime since);

	@Query("SELECT pra FROM PullRequestAnalysis pra WHERE pra.owner = :owner AND pra.repository = :repository AND pra.pullRequestNumber = :prNumber")
	PullRequestAnalysis findByOwnerRepositoryAndPRNumber(@Param("owner") String owner,
			@Param("repository") String repository, @Param("prNumber") Integer prNumber);
}