package com.zbs.de.mapper;

import com.zbs.de.model.DecorCategoryMaster;
import com.zbs.de.model.dto.DtoDecorCategoryMaster;

public class MapperDecorCategoryMaster {
    public static DecorCategoryMaster toEntity(DtoDecorCategoryMaster dto) {
        DecorCategoryMaster entity = new DecorCategoryMaster();
        entity.setSerDecorCategoryId(dto.getSerDecorCategoryId());
        entity.setTxtDecorCategoryCode(dto.getTxtDecorCategoryCode());
        entity.setTxtDecorCategoryName(dto.getTxtDecorCategoryName());
        entity.setBlnIsActive(dto.getBlnIsActive());
        return entity;
    }

    public static DtoDecorCategoryMaster toDto(DecorCategoryMaster entity) {
        DtoDecorCategoryMaster dto = new DtoDecorCategoryMaster();
        dto.setSerDecorCategoryId(entity.getSerDecorCategoryId());
        dto.setTxtDecorCategoryCode(entity.getTxtDecorCategoryCode());
        dto.setTxtDecorCategoryName(entity.getTxtDecorCategoryName());
        dto.setBlnIsActive(entity.getBlnIsActive());
        return dto;
    }
}
