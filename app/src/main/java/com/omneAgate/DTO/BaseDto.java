package com.omneAgate.DTO;

import lombok.Data;

/**
 * Created for BaseDto
 */

@Data
public class BaseDto {

    int statusCode; // error code. 0 if success else unique error code value

    TransactionTypes transactionType; //Transactiontype

    String trackId;

    ApplicationType appType = ApplicationType.KEROSENE_BUNK;

}
