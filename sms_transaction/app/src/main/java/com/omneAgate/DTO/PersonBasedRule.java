package com.omneAgate.DTO;

import android.database.Cursor;

import com.omneAgate.Util.Constants.FPSDBConstants;
import com.omneAgate.Util.FPSDBHelper;

import lombok.Data;

/**
 * Created by user1 on 28/4/15.
 */

@Data
public class PersonBasedRule {

    long id;

    long productId;

    double min;

    ProductDto productDto;

    double max;

    double perChild;

    double perAdult;

    long lastUpdatedDate;

    long lastUpdatedBy;

    public PersonBasedRule() {

    }

    public PersonBasedRule(Cursor cursor) {

        id = cursor.getLong(cursor
                .getColumnIndex(FPSDBHelper.KEY_ID));

        productId = cursor.getLong(cursor
                .getColumnIndex(FPSDBConstants.KEY_RULES_PRODUCT_ID));

        min = cursor.getDouble(cursor
                .getColumnIndex(FPSDBConstants.KEY_PERSON_MIN));

        max = cursor.getDouble(cursor
                .getColumnIndex(FPSDBConstants.KEY_PERSON_MAX));

        perChild = cursor.getDouble(cursor
                .getColumnIndex(FPSDBConstants.KEY_PERSON_CHILD));

        perAdult = cursor.getDouble(cursor
                .getColumnIndex(FPSDBConstants.KEY_PERSON_ADULT));

    }

}
