package com.zbs.de.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.zbs.de.mapper.MapperMenuItem;
import com.zbs.de.model.MenuComponent;
import com.zbs.de.model.MenuItem;
import com.zbs.de.model.MenuItemRole;
import com.zbs.de.model.dto.DtoMenuCsvImportResult;
import com.zbs.de.model.dto.DtoMenuCsvRowResult;
import com.zbs.de.model.dto.DtoMenuItem;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.repository.RepositoryMenuComponent;
import com.zbs.de.repository.RepositoryMenuItem;
import com.zbs.de.service.ServiceMenuItem;
import com.zbs.de.service.ServiceMenuItemRole;
import com.zbs.de.service.ServiceTreeUtility;
import com.zbs.de.util.enums.EnmMenuItemRole;
import com.zbs.de.util.enums.EnmMenuItemType;
import com.zbs.de.util.exception.NotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service("serviceMenuItemImpl")
public class ServiceMenuItemImpl implements ServiceMenuItem {

	@Autowired
	private RepositoryMenuItem repo;

	@Autowired
	private ServiceTreeUtility treeUtil;

	@Autowired
	private RepositoryMenuComponent componentRepo;
	
	@Autowired
	private ServiceMenuItemRole serivceMenuItemRole;

	@Autowired
	private ObjectMapper objectMapper;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceMenuItemImpl.class);

	private long autoCodeCounter = 1000;

	@Override
	@Transactional
	public DtoMenuItem create(DtoMenuItem dto) {
		MenuItem entity = MapperMenuItem.toEntity(dto);

		
		MenuItemRole menuItemRole = serivceMenuItemRole.getMenuItemRoleById(dto.getSerMenuItemRoleId());
		if(menuItemRole == null) {
			throw new IllegalArgumentException("Invalid menu role");
		}


		// parent handling
		if (dto.getParentId() != null) {
			MenuItem parent = repo.findById(dto.getParentId())
					.orElseThrow(() -> new NotFoundException("Parent not found"));
			validateParentRole(menuItemRole, parent!= null ? parent.getMenuItemRole() : null);
			entity.setParent(parent);
			entity.setTxtPath(treeUtil.computeChildPath(parent.getTxtPath(), entity.getTxtCode()));
		} else {
			entity.setTxtPath(treeUtil.sanitizeForLtree(entity.getTxtCode()));
		}

		entity.setMenuItemRole(menuItemRole);
		if (entity.getBlnIsSelectable() == null)
			entity.setBlnIsSelectable(true);
		repo.save(entity);
		return MapperMenuItem.toDto(entity);
	}

//	@Override
//	@Transactional
//	public DtoMenuItem update(Long id, DtoMenuItem dto) {
//		MenuItem exist = repo.findById(id).orElseThrow(() -> new NotFoundException("MenuItem not found"));
//		exist.setTxtCode(dto.getTxtCode());
//		exist.setTxtName(dto.getTxtName());
//		exist.setTxtShortName(dto.getTxtShortName());
//		exist.setTxtDescription(dto.getTxtDescription());
//		exist.setTxtRole(dto.getTxtRole());
//		exist.setNumDisplayOrder(dto.getNumDisplayOrder());
//		exist.setBlnIsSelectable(dto.getBlnIsSelectable());
//		exist.setMetadata(dto.getMetadata());
//		exist.setNumDefaultServingsPerGuest(dto.getNumDefaultServingsPerGuest());
//
//		Long newParentId = dto.getParentId();
//		Long oldParentId = exist.getParent() == null ? null : exist.getParent().getSerMenuItemId();
//		if ((newParentId == null && oldParentId != null) || (newParentId != null && !newParentId.equals(oldParentId))) {
//			MenuItem newParent = null;
//			if (newParentId != null)
//				newParent = repo.findById(newParentId).orElseThrow(() -> new NotFoundException("Parent not found"));
//			exist.setParent(newParent);
//			String newPath = newParent == null ? treeUtil.sanitizeForLtree(exist.getTxtCode())
//					: treeUtil.computeChildPath(newParent.getTxtPath(), exist.getTxtCode());
//			treeUtil.updatePathForSubtree(exist, newPath);
//		} else {
//			// if code changed, update path for subtree as well
//			if (!exist.getTxtCode().equals(dto.getTxtCode())) {
//				String basePath = exist.getParent() == null ? treeUtil.sanitizeForLtree(dto.getTxtCode())
//						: treeUtil.computeChildPath(exist.getParent().getTxtPath(), dto.getTxtCode());
//				treeUtil.updatePathForSubtree(exist, basePath);
//			}
//		}
//		repo.save(exist);
//		return MapperMenuItem.toDto(exist);
//	}

	@Override
	@Transactional
	public DtoMenuItem update(Long id, DtoMenuItem dto) {

		MenuItem exist = repo.findById(id).orElseThrow(() -> new NotFoundException("MenuItem not found"));

		// ---- BASIC FIELDS ----
		exist.setTxtCode(dto.getTxtCode());
		exist.setTxtName(dto.getTxtName());
		exist.setTxtShortName(dto.getTxtShortName());
		exist.setTxtDescription(dto.getTxtDescription());
		exist.setTxtRole(dto.getTxtRole());
		exist.setTxtType(dto.getTxtType()); // 🔴 MISSING BEFORE
		exist.setNumDisplayOrder(dto.getNumDisplayOrder());
		exist.setBlnIsSelectable(dto.getBlnIsSelectable());
		exist.setMetadata(dto.getMetadata());
		exist.setNumDefaultServingsPerGuest(dto.getNumDefaultServingsPerGuest());

		// ---- ROLE ENUM ----
//		EnmMenuItemRole currentRole = EnmMenuItemRole.of(dto.getTxtRole());
//		if (currentRole == null) {
//			throw new IllegalArgumentException("Invalid menu role");
//		}
		
		MenuItemRole currentRole = serivceMenuItemRole.getMenuItemRoleById(dto.getSerMenuItemRoleId());
		if(currentRole == null) {
			throw new IllegalArgumentException("Invalid menu role");
		}

		// ---- PARENT HANDLING ----
		Long newParentId = dto.getParentId();
		Long oldParentId = exist.getParent() == null ? null : exist.getParent().getSerMenuItemId();

		MenuItem newParent = null;
		if (newParentId != null) {
			 newParent = repo.getByMenuItemId(newParentId).map(p -> {
				p.getTxtCode(); // force init
				return p;
			}).orElseThrow(() -> new NotFoundException("Parent not found"));
		}

		// 🔴 VALIDATE ROLE ↔ PARENT (MANDATORY)
		validateParentRole(currentRole, newParent!= null ? newParent.getMenuItemRole() : null);
		exist.setMenuItemRole(currentRole);

		// ---- PATH UPDATE ----
		boolean parentChanged = (newParentId == null && oldParentId != null)
				|| (newParentId != null && !newParentId.equals(oldParentId));

		if (parentChanged) {

			exist.setParent(newParent);

			String newPath = (newParent == null) ? treeUtil.sanitizeForLtree(exist.getTxtCode())
					: treeUtil.computeChildPath(newParent.getTxtPath(), exist.getTxtCode());

			treeUtil.updatePathForSubtree(exist, newPath);

		} else {
			// Code changed → update subtree paths
			if (!exist.getTxtCode().equals(dto.getTxtCode())) {

				String basePath = (exist.getParent() == null) ? treeUtil.sanitizeForLtree(dto.getTxtCode())
						: treeUtil.computeChildPath(exist.getParent().getTxtPath(), dto.getTxtCode());

				treeUtil.updatePathForSubtree(exist, basePath);
			}
		}

		repo.save(exist);
		return MapperMenuItem.toDto(exist);
	}

	@Override
	public void delete(Long id) {
		MenuItem exist = repo.findById(id).orElseThrow(() -> new NotFoundException("MenuItem not found"));
		exist.setBlnIsDeleted(true);
		repo.save(exist);
	}

	@Override
	public DtoMenuItem getById(Long id) {
		MenuItem e = repo.findById(id).orElseThrow(() -> new NotFoundException("MenuItem not found"));
		return MapperMenuItem.toDto(e);
	}

	@Override
	public List<DtoMenuItem> getTree() {
		List<MenuItem> all = repo.findAll();
		return treeUtil.buildTreeDto(all);
	}

	@Override
	public List<DtoMenuItem> getAll() {
		List<MenuItem> all = repo.findAll();
		if (all != null && !all.isEmpty()) {
			return all.stream().map(MapperMenuItem::toDto).collect(Collectors.toList());
		} else {
			return new ArrayList<>();
		}
	}

	@Override
	public List<DtoMenuItem> getChildren(Long parentId) {
		List<MenuItem> children = repo.findByParentId(parentId);
		return children.stream().map(MapperMenuItem::toDto).collect(Collectors.toList());
	}

	@Override
	@Transactional
	public void move(Long id, Long newParentId) {
		MenuItem node = repo.findById(id).orElseThrow(() -> new NotFoundException("MenuItem not found"));
		MenuItem newParent = newParentId == null ? null
				: repo.findById(newParentId).orElseThrow(() -> new NotFoundException("Parent not found"));
		node.setParent(newParent);
		String newPath = newParent == null ? treeUtil.sanitizeForLtree(node.getTxtCode())
				: treeUtil.computeChildPath(newParent.getTxtPath(), node.getTxtCode());
		treeUtil.updatePathForSubtree(node, newPath);
		repo.save(node);
	}

	@Override
	@Transactional(readOnly = true)
	public List<DtoMenuItem> findByRole(String role) {
		if (role == null)
			return Collections.emptyList();
		return repo.findByTxtRole(role).stream().map(MapperMenuItem::toDto).collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public List<DtoMenuItem> findByType(String type) {
		if (type == null)
			return Collections.emptyList();
		return repo.findByTxtType(type).stream().map(MapperMenuItem::toDto).collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public List<DtoMenuItem> getBundleItems(Long bundleMenuItemId) {
		if (bundleMenuItemId == null)
			return Collections.emptyList();
		List<MenuComponent> comps = componentRepo.findByParentMenuItem_SerMenuItemId(bundleMenuItemId);
		List<MenuItem> items = comps.stream().map(MenuComponent::getChildMenuItem).filter(Objects::nonNull)
				.collect(Collectors.toList());
		return items.stream().map(MapperMenuItem::toDto).collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public List<DtoMenuItem> findDescendantsByPath(String ltreePath) {
		if (ltreePath == null)
			return Collections.emptyList();
		return repo.findDescendantsByTxtPath(ltreePath).stream().map(MapperMenuItem::toDto)
				.collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public List<DtoMenuItem> getSelectableItemsUnderParent(Long parentId) {
		List<MenuItem> children = repo.findByParentId(parentId);
		return children.stream()
				.filter(m -> m.getBlnIsSelectable() == null || Boolean.TRUE.equals(m.getBlnIsSelectable()))
				.map(MapperMenuItem::toDto).collect(Collectors.toList());
	}

	@Override
	public List<String> getTypes() {
		return Arrays.stream(EnmMenuItemType.values()).map(Enum::name).toList();
	}

	@Override
	public List<String> getRoles() {
		List<String> roles = Arrays.stream(EnmMenuItemRole.values()).map(Enum::name).toList();
		return roles;
	}

	@Override
	@Transactional
	public DtoMenuCsvImportResult importCsv(MultipartFile file) {
		List<DtoMenuCsvRowResult> errors = new ArrayList<>();
		int total = 0;
		int success = 0;

		if (file == null || file.isEmpty()) {
			DtoMenuCsvImportResult r = new DtoMenuCsvImportResult(0, 0, 1,
					Arrays.asList(new DtoMenuCsvRowResult(0, null, null, "No file uploaded", "")));
			return r;
		}

		try (InputStream is = file.getInputStream();
				InputStreamReader isr = new InputStreamReader(is, "UTF-8");
				CSVReader csvReader = new CSVReader(isr)) {

			// Read header first
			String[] header = csvReader.readNext();
			if (header == null) {
				return new DtoMenuCsvImportResult(0, 0, 0, errors);
			}

			// Normalize header length & positions expected
			// Expected columns (in order):
			// parent_code,code,name,short_name,type,role,description,default_servings_per_guest,is_selectable,display_order,metadata_json
			final int EXPECTED_COLS = 11;

			int rowNum = 1; // header = row 1
			String[] row;
			Map<String, Long> localCodeToId = new HashMap<>(); // mapping of codes created during this import

			// Preload existing top-level codes into local map for quick lookup (so
			// parent_code may refer to existing db)
			List<MenuItem> existingWithCodes = repo.findAll();
			if (existingWithCodes != null) {
				existingWithCodes = existingWithCodes.stream()
						.filter(mi -> mi.getTxtCode() != null && !mi.getTxtCode().isBlank()).toList();
			}

			for (MenuItem mi : existingWithCodes) {
				localCodeToId.put(mi.getTxtCode(), mi.getSerMenuItemId());
			}

			while ((row = csvReader.readNext()) != null) {
				rowNum++;
				total++;

				// Allow rows with fewer columns — pad to expected length
				if (row.length < EXPECTED_COLS) {
					String[] tmp = new String[EXPECTED_COLS];
					System.arraycopy(row, 0, tmp, 0, row.length);
					for (int i = row.length; i < EXPECTED_COLS; i++)
						tmp[i] = "";
					row = tmp;
				}

				// Trim each column safely
				for (int i = 0; i < row.length; i++) {
					if (row[i] != null)
						row[i] = row[i].trim();
					else
						row[i] = "";
				}

				// map columns
				String parentCode = row[0];
				String code = row[1];
				String name = row[2];
				String shortName = row[3];
				String type = row[4];
				String role = row[5];
				String description = row[6];
				String defaultServings = row[7];
				String isSelectable = row[8];
				String displayOrder = row[9];
				String metadataJson = row[10];

				String rawRow = String.join(",", row);

				// basic validations
				if (name == null || name.isBlank()) {
					errors.add(new DtoMenuCsvRowResult(rowNum, code, name, "Missing required field: name", rawRow));
					continue;
				}
				if (type == null || type.isBlank()) {
					errors.add(new DtoMenuCsvRowResult(rowNum, code, name, "Missing required field: type", rawRow));
					continue;
				}
				if (role == null || role.isBlank()) {
					errors.add(new DtoMenuCsvRowResult(rowNum, code, name, "Missing required field: role", rawRow));
					continue;
				}

				// Resolve or generate code (hybrid approach)
				String finalCode = null;
				if (code != null && !code.isBlank()) {
					finalCode = code;
					// ensure uniqueness: if already created in this import, append suffix
					if (localCodeToId.containsKey(finalCode)) {
						// try to append numeric suffix
						int suffix = 1;
						String base = finalCode;
						while (localCodeToId.containsKey(finalCode)) {
							finalCode = base + "_" + suffix++;
						}
					}
				} else {
					// generate code: MI + zero-padded counter
					synchronized (this) {
						autoCodeCounter++;
						finalCode = String.format("MI-%04d", autoCodeCounter);
					}
				}

				// parent resolution: empty means top-level
				Long parentId = null;
				if (parentCode != null && !parentCode.isBlank()) {
					// first try local map (items created earlier in this import)
					if (localCodeToId.containsKey(parentCode)) {
						parentId = localCodeToId.get(parentCode);
					} else {
						// try DB by code
						MenuItem parent = repo.findByTxtCode(parentCode);
						if (parent != null) {
							parentId = parent.getSerMenuItemId();
							// cache it for further reference
							localCodeToId.put(parentCode, parentId);
						} else {
							errors.add(new DtoMenuCsvRowResult(rowNum, finalCode, name,
									"Parent code not found: " + parentCode, rawRow));
							continue;
						}
					}
				}

				// parse numeric fields
				Double defaultServingsVal = null;
				if (defaultServings != null && !defaultServings.isBlank()) {
					try {
						defaultServingsVal = Double.parseDouble(defaultServings);
					} catch (NumberFormatException nfe) {
						errors.add(new DtoMenuCsvRowResult(rowNum, finalCode, name,
								"Invalid default_servings_per_guest: " + defaultServings, rawRow));
						continue;
					}
				}

				Integer displayOrderVal = null;
				if (displayOrder != null && !displayOrder.isBlank()) {
					try {
						displayOrderVal = Integer.parseInt(displayOrder);
					} catch (NumberFormatException nfe) {
						errors.add(new DtoMenuCsvRowResult(rowNum, finalCode, name,
								"Invalid display_order: " + displayOrder, rawRow));
						continue;
					}
				}

				Boolean isSelectableVal = null;
				if (isSelectable != null && !isSelectable.isBlank()) {
					String s = isSelectable.trim().toLowerCase();
					if ("true".equals(s) || "1".equals(s) || "yes".equals(s))
						isSelectableVal = true;
					else if ("false".equals(s) || "0".equals(s) || "no".equals(s))
						isSelectableVal = false;
					else {
						errors.add(new DtoMenuCsvRowResult(rowNum, finalCode, name,
								"Invalid is_selectable value: " + isSelectable, rawRow));
						continue;
					}
				}

				Map<String, Object> metadata = null;
				if (metadataJson != null && !metadataJson.isBlank()) {
					try {
						// Accept single quotes by replacing with double quotes, but prefer valid JSON
						String normalized = metadataJson.trim();
						if (normalized.startsWith("'") && normalized.endsWith("'")) {
							normalized = normalized.substring(1, normalized.length() - 1);
						}
						// Some users may use single quotes inside; try a permissive parse
						metadata = objectMapper.readValue(normalized, Map.class);
					} catch (Exception ex) {
						// final attempt: try replacing single quotes with double quotes
						try {
							String alt = metadataJson.replace("'", "\"");
							metadata = objectMapper.readValue(alt, Map.class);
						} catch (Exception ex2) {
							errors.add(new DtoMenuCsvRowResult(rowNum, finalCode, name,
									"Invalid metadata_json: " + ex2.getMessage(), rawRow));
							continue;
						}
					}
				}

				// Build DtoMenuItem and call existing service to create item
				try {
					DtoMenuItem dto = new DtoMenuItem();
					dto.setTxtCode(finalCode);
					dto.setTxtName(name);
					dto.setTxtShortName((shortName == null || shortName.isBlank()) ? null : shortName);
					dto.setTxtType(type);
					dto.setTxtRole(role);
					dto.setTxtDescription((description == null || description.isBlank()) ? null : description);
					dto.setNumDefaultServingsPerGuest(defaultServingsVal);
					dto.setBlnIsSelectable(isSelectableVal == null ? Boolean.TRUE : isSelectableVal);
					dto.setNumDisplayOrder(displayOrderVal == null ? 0 : displayOrderVal);
					dto.setMetadata(metadata);
					if (parentId != null)
						dto.setParentId(parentId);

					DtoMenuItem saved = this.create(dto); // uses existing business logic
					// cache mapping finalCode -> saved id
					localCodeToId.put(finalCode, saved.getSerMenuItemId());
					success++;
				} catch (Exception ex) {
					errors.add(
							new DtoMenuCsvRowResult(rowNum, finalCode, name, "Save error: " + ex.getMessage(), rawRow));
					// continue with next row
				}
			} // end rows loop

		} catch (IOException | CsvValidationException ex) {
			// Fatal read error — return as single failure
			return new DtoMenuCsvImportResult(0, 0, 1,
					Arrays.asList(new DtoMenuCsvRowResult(0, null, null, "CSV read error: " + ex.getMessage(), "")));
		}

		int failed = total - success;
		DtoMenuCsvImportResult result = new DtoMenuCsvImportResult(total, success, failed, errors);
		return result;
	}

	@Override
	public String generateNextCode(String prefix) {

//		List<String> types = this.getTypes();
//		if(types != null && !types.isEmpty()) {
//			STATION, BUNDLE, GROUP, CATEGORY, SUBCATEGORY, ITEM, OPTION, SELECTION;
//		}
		if (prefix.equalsIgnoreCase("SECTION")) {
			prefix = "sec";
		} else if (prefix.equalsIgnoreCase("BUNDLE")) {
			prefix = "bdl";
		} else if (prefix.equalsIgnoreCase("GROUP")) {
			prefix = "grp";
		} else if (prefix.equalsIgnoreCase("CATEGORY")) {
			prefix = "cat";
		} else if (prefix.equalsIgnoreCase("SUBCATEGORY")) {
			prefix = "sub";
		} else if (prefix.equalsIgnoreCase("ITEM")) {
			prefix = "mi";
		} else if (prefix.equalsIgnoreCase("OPTION")) {
			prefix = "opt";
		} else if (prefix.equalsIgnoreCase("SELECTION")) {
			prefix = "slc";
		} else if (prefix.equalsIgnoreCase("STATION")) {
			prefix = "sta";
		}
		prefix = prefix.toUpperCase().trim();
		String lastCode = repo.findMaxCodeByPrefix(prefix);

		// CASE 1: No code found → start at 001
		if (lastCode == null || lastCode.isBlank()) {
			if (prefix.equalsIgnoreCase("MI")) {
				return prefix + "-1001";
			} else {
				return prefix + "-001";
			}

		}

		// Extract number part safely
		String numericPart = extractNumericPart(lastCode);

		int nextNumber = 1;

		// CASE 2: numeric part exists and is a valid integer
		if (numericPart != null) {
			try {
				nextNumber = Integer.parseInt(numericPart) + 1;
			} catch (NumberFormatException e) {
				// CASE 3: if malformed, restart from 1
				nextNumber = 1;
			}
		}

		// Format with leading zeros (3 digits)
		if (prefix.equalsIgnoreCase("MI")) {
			return prefix + "-" + String.format("%04d", nextNumber);
		} else {
			return prefix + "-" + String.format("%03d", nextNumber);
		}

	}

	/**
	 * Extracts numeric portion from something like: SEC_004 → returns "004" CAT_19
	 * → returns "19" CAT → returns null BAD_CODE → returns null
	 */
	private String extractNumericPart(String code) {
		if (code == null)
			return null;

		int underscoreIndex = code.lastIndexOf("-");

		if (underscoreIndex == -1 || underscoreIndex == code.length() - 1) {
			return null; // no numeric part
		}

		return code.substring(underscoreIndex + 1);
	}

	@Override
	public List<DtoMenuItem> getValidParentsByRole(String role) {

		EnmMenuItemRole currentRole = EnmMenuItemRole.of(role);
		if (currentRole == null) {
			return List.of();
		}

		List<String> allowedParentRoles = switch (currentRole) {
		case SUBCATEGORY -> List.of(EnmMenuItemRole.CATEGORY.name());
		case STATION -> List.of(EnmMenuItemRole.SUBCATEGORY.name(), EnmMenuItemRole.CATEGORY.name());
		case GROUP -> List.of(EnmMenuItemRole.STATION.name(), EnmMenuItemRole.SUBCATEGORY.name(),
				EnmMenuItemRole.CATEGORY.name());
		case BUNDLE -> List.of(EnmMenuItemRole.GROUP.name());
		case ITEM -> List.of(EnmMenuItemRole.GROUP.name(), EnmMenuItemRole.BUNDLE.name(),
				EnmMenuItemRole.CATEGORY.name(), EnmMenuItemRole.SUBCATEGORY.name());
		default -> List.of();
		};

		if (allowedParentRoles.isEmpty()) {
			return List.of();
		}

		return allowedParentRoles.stream().flatMap(r -> repo.findByTxtRoleAndBlnIsDeletedFalse(r).stream())
				.map(MapperMenuItem::toDto).toList();
	}

//	private void validateParentRole(EnmMenuItemRole currentRole, MenuItem parent) {
//
//		// CATEGORY must be root
//		if (currentRole == EnmMenuItemRole.CATEGORY) {
//			if (parent != null) {
//				throw new IllegalArgumentException("CATEGORY cannot have a parent");
//			}
//			return;
//		}
//
//		// All others must have a parent
//		if (parent == null) {
//			throw new IllegalArgumentException(currentRole + " must have a parent");
//		}
//
//		EnmMenuItemRole parentRole = EnmMenuItemRole.of(parent.getTxtRole());
//		if (parentRole == null) {
//			throw new IllegalArgumentException("Invalid parent role");
//		}
//
//		List<String> allowedParentRoles = switch (currentRole) {
//		case SUBCATEGORY -> List.of(EnmMenuItemRole.CATEGORY.name());
//
//		case STATION -> List.of(EnmMenuItemRole.SUBCATEGORY.name(), EnmMenuItemRole.CATEGORY.name());
//
//		case GROUP -> List.of(EnmMenuItemRole.STATION.name(), EnmMenuItemRole.SUBCATEGORY.name(),
//				EnmMenuItemRole.CATEGORY.name());
//
//		case BUNDLE -> List.of(EnmMenuItemRole.GROUP.name());
//
//		case ITEM -> List.of(EnmMenuItemRole.GROUP.name(), EnmMenuItemRole.BUNDLE.name(),
//				EnmMenuItemRole.CATEGORY.name(), EnmMenuItemRole.SUBCATEGORY.name());
//
//		default -> List.of();
//		};
//
//		if (!allowedParentRoles.contains(parentRole.name())) {
//			throw new IllegalArgumentException("Invalid parent role " + parentRole + " for menu role " + currentRole
//					+ ". Allowed: " + allowedParentRoles);
//		}
//	}

	private void validateParentRole(MenuItemRole currentRole, MenuItemRole parentRole) {

		// 1️⃣ Root role validation (CATEGORY or any root-level role)
		if (currentRole.getParentMenuItemRole() == null && parentRole != null) {
			throw new IllegalArgumentException(currentRole.getTxtRoleCode() + " cannot have a parent");
		}

		// 2️⃣ Non-root roles must have parent
		if (currentRole.getParentMenuItemRole() != null && parentRole == null) {
			throw new IllegalArgumentException(currentRole.getTxtRoleCode() + " must have a parent");
		}

		// 3️⃣ Validate allowed parent based on hierarchy
		if (parentRole != null) {

			boolean isValidParent = isAllowedParent(currentRole, parentRole);

			if (!isValidParent) {
				throw new IllegalArgumentException("Invalid parent role " + parentRole.getTxtRoleCode()
						+ " for menu role " + currentRole.getTxtRoleCode());
			}
		}
	}

	private boolean isAllowedParent(MenuItemRole currentRole, MenuItemRole parentRole) {

		MenuItemRole allowedParent = currentRole.getParentMenuItemRole();

		// Direct parent match
		if (allowedParent == null) {
			return false;
		}

		// Traverse up parent chain
		MenuItemRole cursor = parentRole;
		while (cursor != null) {
			if (cursor.getSerMenuItemRoleId().equals(allowedParent.getSerMenuItemRoleId())) {
				return true;
			}
			cursor = cursor.getParentMenuItemRole();
		}

		return false;
	}
	
	@Override
	public DtoResult getAllByRoleId(Integer id) {
		try {
			List<MenuItem> items = repo.getAllItemsByRoleId(id);
			if (items != null && !items.isEmpty()) {

				List<DtoMenuItem> dtoMenuItems = items.stream().map(MapperMenuItem::toDto).collect(Collectors.toList());
				return new DtoResult("Items Fetched Successfully.", null, dtoMenuItems, null);
			} else {
				return new DtoResult("No Items Found Against This Role In Database", null, null, null);
			}
		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			return new DtoResult(e.getMessage(), null, null, null);
		}
		
	}

}
