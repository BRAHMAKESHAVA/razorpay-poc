package org.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.backend.model.SalonService;

import java.util.List;

@Data
@AllArgsConstructor
public class CategoryGroupDTO {
    private Long categoryId;
    private String categoryName;
    private List<SalonService> services;
}