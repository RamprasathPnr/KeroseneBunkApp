package com.omneAgate.DTO.UserDto;

import android.database.Cursor;

import com.omneAgate.Util.FPSDBHelper;

import lombok.Data;

/**
 * Created by OASYS
 */

@Data
public class MigrationOutDTO {

    long id;

    long beneficiaryId;

    public  MigrationOutDTO(){

    }

    public  MigrationOutDTO(Cursor cursor){

        id = cursor.getLong(cursor.getColumnIndex(FPSDBHelper.KEY_ID));

        beneficiaryId = cursor.getLong(cursor.getColumnIndex("beneficiary_id"));

    }

}
