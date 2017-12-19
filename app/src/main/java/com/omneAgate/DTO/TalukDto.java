package com.omneAgate.DTO;

import android.database.Cursor;

import com.omneAgate.Util.Constants.FPSDBConstants;

import lombok.Data;

/**
 * Created by user1 on 29/9/15.
 */

@Data
public class TalukDto {

    long id;

    DistrictDto districtDto;

    public TalukDto(Cursor c){
        id = c.getLong(c.getColumnIndex(FPSDBConstants.KEY_USERS_TALUK_ID));
        districtDto=new DistrictDto(c);
    }
}
