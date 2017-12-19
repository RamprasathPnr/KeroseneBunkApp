package com.omneAgate.DTO;

import android.database.Cursor;

import lombok.Data;

/**
 * Created by user1 on 27/7/15.
 */
@Data
public class LoginHistoryDto {

    String loginTime;

    String loginType;

    long userId;

    String logoutTime;

    String logoutType;

    long bunkId;

    String transactionId;

    String deviceId;


    ApplicationType appType = ApplicationType.KEROSENE_BUNK;

    public LoginHistoryDto() {

    }

    public LoginHistoryDto(Cursor cur) {
        loginTime = cur.getString(cur.getColumnIndex("login_time"));
        loginType = cur.getString(cur.getColumnIndex("login_type"));
        logoutTime = cur.getString(cur.getColumnIndex("logout_time"));
        logoutType = cur.getString(cur.getColumnIndex("logout_type"));
        transactionId = cur.getString(cur.getColumnIndex("transaction_id"));
        bunkId = cur.getLong(cur.getColumnIndex("bunk_id"));
        userId = cur.getLong(cur.getColumnIndex("user_id"));
    }
}
