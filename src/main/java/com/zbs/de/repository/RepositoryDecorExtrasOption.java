package com.zbs.de.repository;

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

}
