package com.omneAgate.DTO;

import com.omneAgate.DTO.EnumDTO.TableNames;

import lombok.Data;

/**
 * Created by user1 on 29/5/15.
 */
@Data
public class FistSyncInputDto {
    String tableName;
    int count;
    String textToDisplay;
    String endTextToDisplay;
    boolean dynamic;
    TableNames tableNames;
}
