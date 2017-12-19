package com.omneAgate.DTO;

import lombok.Data;

/**
 * Created fro TransactionBaseDto
 */

@Data
public class TransactionBaseDto {

    BaseDto baseDto;        //BaseDto for all transaction related dto

    TransactionTypes transactionType; //TransactionType

    String type;
}