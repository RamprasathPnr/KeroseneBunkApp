package com.omneAgate.DTO;

import com.omneAgate.DTO.EnumDTO.StockTransactionType;

import java.io.Serializable;

import lombok.Data;


@Data
public class KeroseneBunkStockHistoryDto implements Serializable {


    private static final long serialVersionUID = 5906490671417857893L;


    Long id;

    /**
     * Device Number of the device
     */
    String deviceNum;


    /**
     * product identifier
     */
    ProductDto productDto;

    /**
     * updated details
     */
    StockTransactionType operation;

    /**
     * Stock quantity which is to be increased or decrease
     */
    Double quantity;

    /**
     * Previous quantity of the stock
     */
    Double prevQuantity;

    /**
     * Current quantity of the stock
     */
    Double currQuantity;

    /**
     * Date of creation
     */
    long createdDate;

    /**
     * User id
     */
    long createdBy;

    Long transactionReference;

}
