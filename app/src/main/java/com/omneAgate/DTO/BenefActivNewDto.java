package com.omneAgate.DTO;

import android.database.Cursor;

import com.omneAgate.Util.Constants.FPSDBConstants;

import lombok.Data;

/**
 * Created for BenefActivNewDto
 */

@Data
public class BenefActivNewDto extends BaseDto {

    String deviceNum;

    String fpsId;

    String rationCardNumber;

    String mobileNum;

    String otpEntryTime;

    String otp;

    String activationType;

    String requestedTime;

    String imageAddress;

    String aregisterNum;
    /**
     * number of cylinder
     */
    int numOfCylinder;

    String transactionId;

    boolean valueAdded = false;

    /**
     * number of adult
     */
    int numOfAdults;

    /**
     * number of child
     */
    int numOfChild;

    char cardType;

    String cardTypeDef;

    byte[] benefImage;

    String encryptedUfc;

    String channel;

    Long reqDate;

    public BenefActivNewDto() {

    }


    public BenefActivNewDto(Cursor cursor) {

        rationCardNumber = cursor.getString(cursor
                .getColumnIndex(FPSDBConstants.KEY_REGISTRATION_CARD_NO));

        mobileNum = cursor.getString(cursor
                .getColumnIndex(FPSDBConstants.KEY_REGISTRATION_RMN));

        transactionId = cursor.getString(cursor
                .getColumnIndex("card_ref_id"));

        requestedTime = cursor.getString(cursor
                .getColumnIndex(FPSDBConstants.KEY_REGISTRATION_TIME));

        reqDate = cursor.getLong(cursor
                .getColumnIndex("reqTime"));

        channel = cursor.getString(cursor
                .getColumnIndex("channel"));

        aregisterNum = cursor.getString(cursor
                .getColumnIndex("aRegister"));


    }
}