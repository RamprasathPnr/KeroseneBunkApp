package com.omneAgate.DTO;

import android.database.Cursor;

import com.omneAgate.Util.Constants.FPSDBConstants;

import lombok.Data;

@Data
public class DistrictDto {

    Long id;

    // district code
    String code;

    // district name
    String name;

    Long stateId;
    public DistrictDto(Cursor c){

        id = c.getLong(c.getColumnIndex(FPSDBConstants.KEY_USERS_DISTRICT_ID));

    }


}
