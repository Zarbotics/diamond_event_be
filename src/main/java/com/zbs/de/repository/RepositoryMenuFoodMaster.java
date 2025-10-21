package com.zbs.de.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.MenuFoodMaster;

@Repository("repositoryMenuFoodMaster")
public interface RepositoryMenuFoodMaster extends JpaRepository<MenuFoodMaster, Integer> {
	List<MenuFoodMaster> findByBlnIsDeleted(Boolean blnIsDeleted);

	@Query("SELECT m FROM MenuFoodMaster m WHERE m.blnIsDeleted = false AND ("
			+ "(:type = 'Dessert' AND m.blnIsDessert = true) OR " + "(:type = 'Drink' AND m.blnIsDrink = true) OR "
			+ "(:type = 'Starter' AND m.blnIsStarter = true) OR "
			+ "(:type = 'Appetiser' AND m.blnIsAppetiser = true) OR "
			+ "(:type = 'SaladAndCondiment' AND m.blnIsSaladAndCondiment = true) OR "
			+ "(:type = 'MainCourse' AND m.blnIsMainCourse = true))")
	List<MenuFoodMaster> findByFoodType(@Param("type") String type);

	@Query("SELECT e FROM MenuFoodMaster e where e.blnIsDeleted = false and e.blnIsActive ")
	List<MenuFoodMaster> getAllActiveMenuFoodMaster();
}