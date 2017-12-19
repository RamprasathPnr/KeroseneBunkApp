package com.omneAgate.DTO;

import android.database.Cursor;

import com.omneAgate.Util.Constants.FPSDBConstants;

import lombok.Data;

@Data
public class FPSStockDto {

    /**
     * FPS identifier
     */
    long fpsId;

    /**
     * product identifier
     */

    long productId;

    /**
     * Stock quantity
     */

    Double quantity;

    /**
     * Reorder Level quantity for a product
     */

    Double reorderLevel;

    /**
     * Action flag to send email if true
     */

    boolean emailAction;

    /**
     * Action flag to send SMS if true
     */

    boolean smsMSAction;


    public FPSStockDto() {

    }


    public FPSStockDto(Cursor cursor) {
        fpsId = cursor.getLong(cursor
                .getColumnIndex(FPSDBConstants.KEY_STOCK_FPS_ID));

        productId = cursor.getLong(cursor
                .getColumnIndex(FPSDBConstants.KEY_STOCK_PRODUCT_ID));

        quantity = cursor.getDouble(cursor
                .getColumnIndex(FPSDBConstants.KEY_STOCK_QUANTITY));
    }


}
