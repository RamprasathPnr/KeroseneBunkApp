package com.omneAgate.DTO;

import android.database.Cursor;

import com.omneAgate.Util.Constants.FPSDBConstants;
import com.omneAgate.Util.FPSDBHelper;

import java.io.Serializable;

import lombok.Data;

/**
 * This class is used to transfer UserDetails
 *
 * @author user1
 */
@Data
public class UserDetailDto implements Serializable {

    Long id;                //Unique id to identify the user

    String userId;

    String username;        // Users name

    String encryptedPassword;        //Users password is hashed and stored.

    String password;        //Users password is hashed and stored.

    String profile;            // an enum values for FPS_USER, ADMIN, SUPER_ADMIN

    String active;

    KeroseneDto kerosene;

    public UserDetailDto(Cursor cur) {
        id = cur.getLong(cur.getColumnIndex(FPSDBHelper.KEY_ID));
        username = cur.getString(cur
                .getColumnIndex(FPSDBConstants.KEY_USERS_ID));
        userId = cur.getString(cur.getColumnIndex(FPSDBConstants.KEY_USERS_NAME));
        password = cur.getString(cur
                .getColumnIndex(FPSDBConstants.KEY_USERS_PASS_HASH));
        profile = cur.getString(cur.getColumnIndex(FPSDBConstants.KEY_USERS_PROFILE));
        encryptedPassword = cur.getString(cur.getColumnIndex("encrypted_password"));

        kerosene = new KeroseneDto(cur);
    }

}
