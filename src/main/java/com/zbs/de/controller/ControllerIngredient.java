package com.zbs.de.controller;

import com.zbs.de.model.dto.DtoIngredient;
import com.zbs.de.service.ServiceIngredient;
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
@RequestMapping("/menu/ingredient")
@CrossOrigin(origins = "*")  // Adjust CORS for your environment
public class ControllerIngredient {

    private static final Logger LOGGER = LoggerFactory.getLogger(ControllerIngredient.class);

    @Autowired
    private  ServiceIngredient svc;


    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseMessage create(@RequestBody DtoIngredient dto, HttpServletRequest request) {
        try {
            LOGGER.info("Saving or updating Ingredient: {}", dto);
            DtoIngredient result = svc.create(dto);
            return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully saved", result);
        } catch (Exception e) {
            LOGGER.error("Failed to save Ingredient", e);
            return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, "Failed to save", dto);
        }
    }
    
    @PostMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseMessage update(@RequestBody DtoIngredient dto, HttpServletRequest request) {
        try {
            LOGGER.info("Saving or updating Ingredient: {}", dto);
            DtoIngredient result = svc.update(dto.getSerIngredientId(), dto);
            return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully updated", result);
        } catch (Exception e) {
            LOGGER.error("Failed to update Ingredient", e);
            return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, "Failed to update", dto);
        }
    }

    @PostMapping(value = "/getById", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseMessage getById(@RequestBody Long id, HttpServletRequest request) {
        try {
            LOGGER.info("Fetching Ingredient by id: {}", id);
            DtoIngredient dto = svc.getById(id);
            if (dto != null) {
                return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully fetched", dto);
            } else {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND, "Ingredient not found", null);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to fetch Ingredient with id {}", id, e);
            return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, "Failed to fetch", null);
        }
    }

    @PostMapping(value = "/getAll", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseMessage getAll(HttpServletRequest request) {
        try {
            LOGGER.info("Fetching all Ingredients");
            List<DtoIngredient> list = svc.getAll();
            return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Successfully fetched", list);
        } catch (Exception e) {
            LOGGER.error("Failed to fetch all Ingredients", e);
            return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch all", null);
        }
    }

    @PostMapping(value = "/delete", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseMessage delete(@RequestBody Long id) {
        try {
            LOGGER.info("Deleting Ingredient with id: {}", id);
            svc.delete(id);
            return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Deleted successfully", null);
        } catch (Exception e) {
            LOGGER.error("Failed to delete Ingredient with id {}", id, e);
            return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete", null);
        }
    }
}
