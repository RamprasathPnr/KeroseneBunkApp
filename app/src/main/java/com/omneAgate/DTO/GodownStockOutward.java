package com.omneAgate.DTO;


import android.database.Cursor;
import android.util.Log;

import com.omneAgate.Util.Constants.FPSDBConstants;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

import lombok.Data;

/**
 * store the GodownStockOutwardDto data
 */
@Data
public class GodownStockOutward extends BaseDto {

    /**
     * godown identifier
     */
    long godownId;

    String godownName;

    /**
     * FPS Identifier
     */
    long fpsId;

    /**
     * Stock outward date
     */
    long outwardDate;

    /**
     * Product identifier
     */
    long productId;

    /**
     * Stock quantity
     */
    Double quantity;

    /**
     * Unit of measurement from Master table
     */
    String unit;

    /**
     * Internal batch number auto generated
     */
    long batchno;

    /**
     * Indicates whether the corresponding FPS has received the stock allotment
     */
    int fpsAckStatus;

    /**
     * Date of acknowledgement from FPS
     */
    long fpsAckDate;

    /**
     * The actual quantity received by the FPS
     */
    Double fpsReceiQuantity;

    /**
     * user id
     */
    long createdby;

    /**
     * Delivery challan id from Deliver_challan table
     */
    long deliveryChallanId;

    String godownCode;

    String referenceNo;

    /**
     * collection allotment data
     */
    Set<ChellanProductDto> productDto;

    public GodownStockOutward() {

    }

    public GodownStockOutward(Cursor cursor) {
        try {
            godownId = cursor.getInt(cursor.getColumnIndex(FPSDBConstants.KEY_FPS_STOCK_INWARD_GODOWNID));
            fpsId = cursor.getLong(cursor.getColumnIndex(FPSDBConstants.KEY_FPS_STOCK_INWARD_FPSID));
            outwardDate = cursor.getLong(cursor.getColumnIndex(FPSDBConstants.KEY_FPS_STOCK_INWARD_OUTWARD_DATE));
            productId = cursor.getInt(cursor.getColumnIndex(FPSDBConstants.KEY_FPS_STOCK_INWARD_PRODUCTID));

            quantity = cursor.getDouble(cursor.getColumnIndex(FPSDBConstants.KEY_FPS_STOCK_INWARD_QUANTITY));
            unit = cursor.getString(cursor.getColumnIndex(FPSDBConstants.KEY_FPS_STOCK_INWARD_UNIT));
            godownName = cursor.getString(cursor.getColumnIndex("godown_name"));
            godownCode = cursor.getString(cursor.getColumnIndex("godown_code"));
            referenceNo = cursor.getString(cursor.getColumnIndex("referenceNo"));
            batchno = cursor.getLong(cursor.getColumnIndex(FPSDBConstants.KEY_FPS_STOCK_INWARD_BATCH_NO));
            createdby = cursor.getInt(cursor.getColumnIndex(FPSDBConstants.KEY_FPS_STOCK_INWARD_CREATEDBY));
            String value = cursor.getString(cursor.getColumnIndex(FPSDBConstants.KEY_FPS_STOCK_INWARD_FPSACKDATE));
            fpsAckDate = 0l;
            if(value!=null){
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Date date = sdf.parse(value);
                fpsAckDate = date.getTime();
            }
            fpsReceiQuantity = cursor.getDouble(cursor.getColumnIndex(FPSDBConstants.KEY_FPS_STOCK_INWARD_FPSRECEIVEIQUANTITY));
            deliveryChallanId = cursor.getLong(cursor.getColumnIndex("delivery_challan_id"));
            fpsAckStatus = cursor.getInt(cursor.getColumnIndex(FPSDBConstants.KEY_FPS_STOCK_INWARD_IS_FPSACKSTATUS));
            Log.e("fpsAckStatusInt","fpsAckStatusInt:"+fpsAckStatus);
        }catch (Exception e){
            Log.e("Excep", e.toString(), e);
        }
    }
}
