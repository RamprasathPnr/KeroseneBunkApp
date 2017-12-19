package com.omneAgate.DTO;

import android.database.Cursor;

import com.omneAgate.Util.Constants.FPSDBConstants;

import lombok.Data;

/**
 * Created by user1 on 29/9/15.
 */

@Data
public class KeroseneDto  {


    long id;

    String code;

    String name;

    boolean status;

    String contactNumber;

    String emailId;

    String pincode;

    String longitude;

    String latitude;

    String contactPersonName;

    String address;

    boolean remoteLogEnabled;

    Boolean geofencing;

    String generatedCode;

    String entitlementClassification;

    String operationOpeningTime;

    String operationClosingTime;

   FPSCategory  keroseneBunkCategory;

     Long districtId;

    TalukDto talukDto;

    Long talukId;

    public KeroseneDto(){

    }

    public KeroseneDto(Cursor cursor){
        id = cursor.getLong(cursor.getColumnIndex(FPSDBConstants.KEY_USERS_FPS_ID));
        int statusCode = cursor.getInt(cursor.getColumnIndex(FPSDBConstants.KEY_USERS_IS_ACTIVE));
        status = statusCode != 0;
        name = cursor.getString(cursor.getColumnIndex(FPSDBConstants.KEY_USERS_NAME));
        contactPersonName = cursor.getString(cursor.getColumnIndex(FPSDBConstants.KEY_USERS_CONTACT_PERSON));
        contactNumber = cursor.getString(cursor.getColumnIndex(FPSDBConstants.KEY_USERS_PHONE_NUMBER));
        address = cursor.getString(cursor.getColumnIndex(FPSDBConstants.KEY_USERS_ADDRESS_LINE1));
        code = cursor.getString(cursor.getColumnIndex(FPSDBConstants.KEY_USERS_CODE));
        entitlementClassification = cursor.getString(cursor.getColumnIndex("entitlement_classification"));
        operationOpeningTime = cursor.getString(cursor.getColumnIndex("operation_closing_time"));
        operationClosingTime = cursor.getString(cursor.getColumnIndex("operation_opening_time"));
        String talukName = cursor.getString(cursor.getColumnIndex("taluk_name"));
        if(talukName!=null){
            talukId = Long.parseLong(talukName);
        }
        String distName = cursor.getString(cursor.getColumnIndex("district_name"));
        if(distName!=null){
            districtId = Long.parseLong(distName);
        }
        String keroseneCategory  =  cursor.getString(cursor.getColumnIndex("agency_name"));
        keroseneBunkCategory = FPSCategory.valueOf(keroseneCategory);
        talukDto=new TalukDto(cursor);

    }

}
