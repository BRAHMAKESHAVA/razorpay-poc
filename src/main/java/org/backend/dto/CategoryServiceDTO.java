package org.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CategoryServiceDTO {
    private String categoryName;
    private String serviceName;
}