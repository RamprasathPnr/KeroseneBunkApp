package com.omneAgate.DTO;

import java.util.Set;

import lombok.Data;

@Data
public class GroupDto {
    /**
     * auto generated
     */
    Long id;

    /**
     * Name of the group
     */
    String groupName;

    /**
     * collection of product
     */
    Set<ProductDto> productDto;

    /**
     * status flag
     */
    Boolean deleted;
}
