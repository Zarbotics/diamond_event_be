package com.zbs.de.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zbs.de.model.Ingredient;

public interface RepositoryIngredient extends JpaRepository<Ingredient, Long> {
}