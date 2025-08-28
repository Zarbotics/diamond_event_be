package com.zbs.de.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.DecorExtrasOption;

@Repository("repositoryDecorExtrasOption")
public interface RepositoryDecorExtrasOption extends JpaRepository<DecorExtrasOption, Integer> {

	@Query("SELECT o FROM DecorExtrasOption o WHERE o.serExtraOptionId = :id AND o.blnIsDeleted = false")
	Optional<DecorExtrasOption> findByIdAndNotDeleted(@Param("id") Integer id);

	@Query("SELECT DISTINCT o FROM DecorExtrasOption o LEFT JOIN o.decorExtrasMaster em WHERE (o.blnIsDeleted = false  OR o.blnIsDeleted IS NULL) AND em.serExtrasId = :id ")
	List<DecorExtrasOption> findAllByExtrasIdAndNotDeleted(@Param("id") Integer serExtrasId);

	@Query("SELECT DISTINCT o FROM DecorExtrasOption o LEFT JOIN o.decorExtrasMaster em WHERE (o.blnIsDeleted = false  OR o.blnIsDeleted IS NULL)")
	List<DecorExtrasOption> findAllNotDeleted();
}
