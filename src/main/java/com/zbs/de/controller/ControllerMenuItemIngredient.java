package com.zbs.de.controller;

import com.zbs.de.model.dto.DtoMenuItemIngredient;
import com.zbs.de.service.ServiceMenuItemIngredient;
import com.zbs.de.util.ResponseMessage;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/menu/item/{menuItemId}/ingredient")
@CrossOrigin(origins = "*") // adjust as necessary
public class ControllerMenuItemIngredient {

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerMenuItemIngredient.class);

	@Autowired
	private ServiceMenuItemIngredient svc;

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage add(@PathVariable Long menuItemId, @RequestBody DtoMenuItemIngredient dto,
			HttpServletRequest request) {
		try {
			LOGGER.info("Adding ingredient to menuItemId {}: {}", menuItemId, dto);
			svc.addIngredientToMenuItem(menuItemId, dto);
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Ingredient added", null);
		} catch (Exception e) {
			LOGGER.error("Failed to add ingredient to menuItemId {}", menuItemId, e);
			return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST,
					"Failed to add ingredient", null);
		}
	}

	@DeleteMapping(value = "/{ingredientId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage remove(@PathVariable Long menuItemId, @PathVariable Long ingredientId) {
		try {
			LOGGER.info("Removing ingredientId {} from menuItemId {}", ingredientId, menuItemId);
			svc.removeIngredientFromMenuItem(menuItemId, ingredientId);
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Ingredient removed", null);
		} catch (Exception e) {
			LOGGER.error("Failed to remove ingredientId {} from menuItemId {}", ingredientId, menuItemId, e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to remove ingredient", null);
		}
	}

	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseMessage list(@PathVariable Long menuItemId) {
		try {
			LOGGER.info("Listing ingredients for menuItemId {}", menuItemId);
			List<DtoMenuItemIngredient> list = svc.listIngredients(menuItemId);
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully fetched", list);
		} catch (Exception e) {
			LOGGER.error("Failed to list ingredients for menuItemId {}", menuItemId, e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to fetch ingredients", null);
		}
	}
}
