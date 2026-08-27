package com.zbs.de.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.zbs.de.controller.auth.AuthController;
import com.zbs.de.mapper.MapperMenuItem;
import com.zbs.de.model.MenuItem;
import com.zbs.de.model.dto.DtoMenuComponentRequest;
import com.zbs.de.model.dto.DtoMenuPriceCalulationFields;
import com.zbs.de.model.dto.menu.DtoCustomerMenuCategory;
import com.zbs.de.model.dto.menu.DtoCustomerMenuSubCategory;
import com.zbs.de.repository.RepositoryMenuItem;
import com.zbs.de.service.ServiceMenuComponent;
import com.zbs.de.service.ServiceMenuSelection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zbs.de.util.enums.EnmPriceMultiplierType;

@Service("serviceMenuSelectionImpl")
public class ServiceMenuSelectionImpl implements ServiceMenuSelection {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceMenuSelectionImpl.class);

    private final AuthController authController;

	@Autowired
	RepositoryMenuItem repositoryMenuItem;

	@Autowired
	ServiceMenuComponent serviceMenuComponent;

    ServiceMenuSelectionImpl(AuthController authController) {
        this.authController = authController;
    }

	@Override
	@Transactional(readOnly = true)
	public List<DtoCustomerMenuCategory> getCustomerMenu() {

		// 1️⃣ Fetch all active categories
		List<MenuItem> categories = repositoryMenuItem.getAllActiveItemsByRoleId(1);

		List<DtoCustomerMenuCategory> result = new ArrayList<>();

		for (MenuItem category : categories) {

			DtoCustomerMenuCategory catDto = new DtoCustomerMenuCategory();
			catDto.setCategoryId(category.getSerMenuItemId());
			catDto.setCategoryName(category.getTxtName());
			catDto.setNumPrice(category.getNumPrice());
			catDto.setBlnIsSelectable(category.getBlnIsSelectable());
			// 2️⃣ Fetch subcategories
//			List<MenuItem> subCategories = repositoryMenuItem.findByParentId(category.getSerMenuItemId());
			List<MenuItem> subCategories = repositoryMenuItem.findByParentIdByDisplayOrder(category.getSerMenuItemId());

			List<DtoCustomerMenuSubCategory> subDtos = new ArrayList<>();

			for (MenuItem sub : subCategories) {

				DtoCustomerMenuSubCategory subDto = new DtoCustomerMenuSubCategory();
				subDto.setSubCategoryId(sub.getSerMenuItemId());
				subDto.setSubCategoryName(sub.getTxtName());
				subDto.setNumPrice(sub.getNumPrice());
                subDto.setTxtDescription(sub.getTxtDescription());
                subDto.setBlnIsSelectable(sub.getBlnIsSelectable());
				subDto.setBlnHasSelectionLimit(sub.getBlnHasSelectionLimit());
				subDto.setNumSelectionLimit(sub.getNumSelectionLimit());

				// 3️⃣ Normal (non-composite) items
				List<MenuItem> items = repositoryMenuItem.findByParentId(sub.getSerMenuItemId()).stream()
						.filter(i -> Boolean.TRUE.equals(i.getBlnIsSelectable()))
						.filter(i -> !Boolean.TRUE.equals(i.getBlnIsComposite())).toList();

				subDto.setItems(items.stream().map(MapperMenuItem::toDto).toList());

				// 4️⃣ Composite items
				List<MenuItem> composites = repositoryMenuItem.findByParentId(sub.getSerMenuItemId()).stream()
						.filter(i -> Boolean.TRUE.equals(i.getBlnIsComposite())).toList();

				List<DtoMenuComponentRequest> compositeDtos = new ArrayList<>();
				for (MenuItem composite : composites) {
					DtoMenuComponentRequest comp = serviceMenuComponent
							.getCompositeWithComponents(composite.getSerMenuItemId());
					if (comp != null) {
						compositeDtos.add(comp);
					}
				}

				subDto.setCompositeItems(compositeDtos);
				subDtos.add(subDto);
			}

			catDto.setSubCategories(subDtos);
			result.add(catDto);
		}

		return result;
	}

	@Override
	@Transactional(readOnly = true)
	public List<DtoCustomerMenuCategory> getCustomerCateringMenu() {

		// 1️⃣ Fetch all active categories
		List<MenuItem> categories = repositoryMenuItem.getAllActiveCateringItemsByRoleId(1);

		List<DtoCustomerMenuCategory> result = new ArrayList<>();

		for (MenuItem category : categories) {

			DtoCustomerMenuCategory catDto = new DtoCustomerMenuCategory();
			catDto.setCategoryId(category.getSerMenuItemId());
			catDto.setCategoryName(category.getTxtName());
			catDto.setNumPrice(category.getNumPrice());
			catDto.setBlnIsSelectable(category.getBlnIsSelectable());
			
			// 2️⃣ Fetch subcategories
//			List<MenuItem> subCategories = repositoryMenuItem.findCateringItemsByParentId(category.getSerMenuItemId());
			List<MenuItem> subCategories = repositoryMenuItem.findCateringItemsByParentIdByDisplayOrder(category.getSerMenuItemId());

			List<DtoCustomerMenuSubCategory> subDtos = new ArrayList<>();

			for (MenuItem sub : subCategories) {

				DtoCustomerMenuSubCategory subDto = new DtoCustomerMenuSubCategory();
				subDto.setSubCategoryId(sub.getSerMenuItemId());
				subDto.setSubCategoryName(sub.getTxtName());
				subDto.setNumPrice(sub.getNumPrice());
                subDto.setBlnIsSelectable(sub.getBlnIsSelectable());
				subDto.setBlnHasSelectionLimit(sub.getBlnHasSelectionLimit());
				subDto.setNumSelectionLimit(sub.getNumSelectionLimit());
				
				// 3️⃣ Normal (non-composite) items
				List<MenuItem> items = repositoryMenuItem.findCateringItemsByParentId(sub.getSerMenuItemId()).stream()
						.filter(i -> Boolean.TRUE.equals(i.getBlnIsSelectable()))
						.filter(i -> !Boolean.TRUE.equals(i.getBlnIsComposite())).toList();

				subDto.setItems(items.stream().map(MapperMenuItem::toDto).toList());

				// 4️⃣ Composite items
				List<MenuItem> composites = repositoryMenuItem.findCateringItemsByParentId(sub.getSerMenuItemId())
						.stream().filter(i -> Boolean.TRUE.equals(i.getBlnIsComposite())).toList();

				List<DtoMenuComponentRequest> compositeDtos = new ArrayList<>();
				for (MenuItem composite : composites) {
					DtoMenuComponentRequest comp = serviceMenuComponent
							.getCompositeWithComponents(composite.getSerMenuItemId());
					if (comp != null) {
						compositeDtos.add(comp);
					}
				}

				subDto.setCompositeItems(compositeDtos);
				subDtos.add(subDto);
			}

			catDto.setSubCategories(subDtos);
			result.add(catDto);
		}

		return result;
	}

	@Override
	@Transactional(readOnly = true)
	public List<DtoCustomerMenuCategory> getCustomerMenuWithPricing(DtoMenuPriceCalulationFields pricingCtx) {

		List<MenuItem> categories = repositoryMenuItem.getAllActiveItemsByRoleId(1);
		List<DtoCustomerMenuCategory> result = new ArrayList<>();

		for (MenuItem category : categories) {

			DtoCustomerMenuCategory catDto = new DtoCustomerMenuCategory();
			catDto.setCategoryId(category.getSerMenuItemId());
			catDto.setCategoryName(category.getTxtName());
			catDto.setBlnIsSelectable(category.getBlnIsSelectable());
			catDto.setBlnHasSelectionLimit(category.getBlnHasSelectionLimit());
			catDto.setNumSelectionLimit(category.getNumSelectionLimit());

//			List<MenuItem> subCategories = repositoryMenuItem.findByParentId(category.getSerMenuItemId());
			List<MenuItem> subCategories = repositoryMenuItem.findByParentIdByDisplayOrder(category.getSerMenuItemId());

			List<DtoCustomerMenuSubCategory> subDtos = new ArrayList<>();

			for (MenuItem sub : subCategories) {

				DtoCustomerMenuSubCategory subDto = new DtoCustomerMenuSubCategory();
				subDto.setSubCategoryId(sub.getSerMenuItemId());
				subDto.setSubCategoryName(sub.getTxtName());
                subDto.setTxtDescription(sub.getTxtDescription());
                subDto.setBlnIsSelectable(sub.getBlnIsSelectable());
				subDto.setBlnHasSelectionLimit(sub.getBlnHasSelectionLimit());
				subDto.setNumSelectionLimit(sub.getNumSelectionLimit());
                
				// -------- Normal Items --------
				List<MenuItem> items = repositoryMenuItem.findByParentId(sub.getSerMenuItemId()).stream()
						.filter(i -> Boolean.TRUE.equals(i.getBlnIsSelectable()))
						.filter(i -> !Boolean.TRUE.equals(i.getBlnIsComposite())).toList();

				subDto.setItems(items.stream().map(item -> {
					var dto = MapperMenuItem.toDto(item);

					BigDecimal calculatedPrice = calculateItemPrice(item, pricingCtx);

					dto.setNumCalculatedPrice(calculatedPrice);
					dto.setNumFinalPrice(calculatedPrice); // editable later

					return dto;
				}).toList());

				// -------- Composite Items --------
				List<MenuItem> composites = repositoryMenuItem.findByParentId(sub.getSerMenuItemId()).stream()
						.filter(i -> Boolean.TRUE.equals(i.getBlnIsComposite())).toList();

				List<DtoMenuComponentRequest> compositeDtos = new ArrayList<>();

				for (MenuItem composite : composites) {

					DtoMenuComponentRequest comp = serviceMenuComponent
							.getCompositeWithComponents(composite.getSerMenuItemId());

					if (comp != null) {
						BigDecimal compositePrice = calculateItemPrice(composite, pricingCtx);

						comp.setNumCalculatedPrice(compositePrice);
						comp.setNumFinalPrice(compositePrice);

						compositeDtos.add(comp);
					}
				}

				subDto.setCompositeItems(compositeDtos);
				subDtos.add(subDto);
			}

			catDto.setSubCategories(subDtos);
			result.add(catDto);
		}

		return result;
	}

	@Override
	@Transactional(readOnly = true)
	public List<DtoCustomerMenuCategory> getCustomerCateringMenuWithPricing(DtoMenuPriceCalulationFields pricingCtx) {

		List<MenuItem> categories = repositoryMenuItem.getAllActiveCateringItemsByRoleId(1);

		List<DtoCustomerMenuCategory> result = new ArrayList<>();

		for (MenuItem category : categories) {

			DtoCustomerMenuCategory catDto = new DtoCustomerMenuCategory();
			catDto.setCategoryId(category.getSerMenuItemId());
			catDto.setCategoryName(category.getTxtName());
			catDto.setBlnIsSelectable(category.getBlnIsSelectable());
			catDto.setBlnHasSelectionLimit(category.getBlnHasSelectionLimit());
			catDto.setNumSelectionLimit(category.getNumSelectionLimit());

//			List<MenuItem> subCategories = repositoryMenuItem.findCateringItemsByParentId(category.getSerMenuItemId());
			List<MenuItem> subCategories = repositoryMenuItem.findCateringItemsByParentIdByDisplayOrder(category.getSerMenuItemId());

			List<DtoCustomerMenuSubCategory> subDtos = new ArrayList<>();

			for (MenuItem sub : subCategories) {

				DtoCustomerMenuSubCategory subDto = new DtoCustomerMenuSubCategory();
				subDto.setSubCategoryId(sub.getSerMenuItemId());
				subDto.setSubCategoryName(sub.getTxtName());
                subDto.setBlnIsSelectable(sub.getBlnIsSelectable());
				subDto.setBlnHasSelectionLimit(sub.getBlnHasSelectionLimit());
				subDto.setNumSelectionLimit(sub.getNumSelectionLimit());
                
				// -------- Normal Catering Items --------
				List<MenuItem> items = repositoryMenuItem.findCateringItemsByParentId(sub.getSerMenuItemId()).stream()
						.filter(i -> Boolean.TRUE.equals(i.getBlnIsSelectable()))
						.filter(i -> !Boolean.TRUE.equals(i.getBlnIsComposite())).toList();

				subDto.setItems(items.stream().map(item -> {
					var dto = MapperMenuItem.toDto(item);

					BigDecimal calculatedPrice = calculateItemPrice(item, pricingCtx);

					dto.setNumCalculatedPrice(calculatedPrice);
					dto.setNumFinalPrice(calculatedPrice);

					return dto;
				}).toList());

				// -------- Composite Catering Items --------
				List<MenuItem> composites = repositoryMenuItem.findCateringItemsByParentId(sub.getSerMenuItemId())
						.stream().filter(i -> Boolean.TRUE.equals(i.getBlnIsComposite())).toList();

				List<DtoMenuComponentRequest> compositeDtos = new ArrayList<>();

				for (MenuItem composite : composites) {

					DtoMenuComponentRequest comp = serviceMenuComponent
							.getCompositeWithComponents(composite.getSerMenuItemId());

					if (comp != null) {

						BigDecimal compositePrice = calculateItemPrice(composite, pricingCtx);

						comp.setNumCalculatedPrice(compositePrice);
						comp.setNumFinalPrice(compositePrice);

						compositeDtos.add(comp);
					}
				}

				subDto.setCompositeItems(compositeDtos);
				subDtos.add(subDto);
			}

			catDto.setSubCategories(subDtos);
			result.add(catDto);
		}

		return result;
	}

	/**
	 * What a priced item comes to for this booking.
	 *
	 * <h3>The rule that was being assumed</h3>
	 *
	 * This read {@code type = PER_GUEST; // safe default}, and it is not a safe
	 * default — it is a multiplication by the guest count applied to items whose
	 * pricing nobody has ever stated. Twenty priced offerings in the live
	 * catalogue are in exactly that position, twelve of them carrying real money:
	 * a Grazing Bar at £2.50 and a Decorative Fruit Display at £3.00 become £750
	 * and £900 at a three hundred guest wedding, with nothing on any screen
	 * saying which rule was applied.
	 *
	 * <p>
	 * On the evidence those twelve <em>are</em> per-head items and the figures
	 * are right. That is the point: they are right by luck rather than by
	 * decision, and the first genuinely flat item somebody prices — a fountain
	 * hired for £250 — becomes £75,000 the same silent way.
	 *
	 * <h3>Why the fallback is still here</h3>
	 *
	 * Because removing it today would change what twenty live items cost, which
	 * is a decision for the business and not a side effect of a refactor. So it
	 * stays, but it stops being invisible: it is named, it is logged with the
	 * item that needed it, and
	 * {@code RepositoryMenuOffering.countPricedOfferingsWithNoRule} counts the
	 * population. M4 makes the rule a required field on the menu screen and shows
	 * the outstanding twenty; when that count reaches zero this whole branch
	 * goes, and {@code UNSTATED} becomes a refusal instead.
	 */
	/**
	 * What an item that has not stated its pricing rule is charged as.
	 *
	 * <p>
	 * Named rather than inlined so that it is a decision somebody can find and
	 * count, rather than a comment saying "safe default" beside a
	 * multiplication. Its population is finite and shrinking; see
	 * {@code calculateItemPrice}.
	 */
	private static final EnmPriceMultiplierType UNSTATED_RULE_MEANS = EnmPriceMultiplierType.PER_GUEST;

	private BigDecimal calculateItemPrice(MenuItem item, DtoMenuPriceCalulationFields ctx) {

		if (item.getNumPrice() == null) {
			return BigDecimal.ZERO;
		}

		BigDecimal base = item.getNumPrice();
		EnmPriceMultiplierType type = item.getEnmPriceMultiplierType();

		if (type == null) {
			type = UNSTATED_RULE_MEANS;
			LOGGER.warn("Menu item {} (#{}) is priced at {} but does not say whether that is per guest; "
					+ "charging {} — see PLATFORM.md §17 M3",
					item.getTxtName(), item.getSerMenuItemId(), base, UNSTATED_RULE_MEANS);
		}

		return switch (type) {

		case PER_GUEST -> {
			int guests = ctx.getNumGuests() != null ? ctx.getNumGuests() : 0;
			yield base.multiply(BigDecimal.valueOf(guests));
		}

		case FLAT -> base;
		};
	}

}