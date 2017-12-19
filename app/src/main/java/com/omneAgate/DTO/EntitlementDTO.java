package com.omneAgate.DTO;

import lombok.Data;

/**
 * Used to set entitlement
 */
@Data
public class EntitlementDTO implements Comparable<com.omneAgate.DTO.EntitlementDTO> {

    double entitledQuantity;

    long productId;

    double currentQuantity;

    String productName;

    String lproductName;

    double productPrice;

    String productUnit;

    String lproductUnit;

    double purchasedQuantity;

    double totalPrice;

    double bought;

    double history;

    long groupId;

    boolean giveitup=false;

    @Override
    public int compareTo(com.omneAgate.DTO.EntitlementDTO o) {
        Long first = this.getProductId();
        Long second = o.getProductId();
        return first.compareTo(second);
    }
}
