package com.omneAgate.Util;

import com.omneAgate.DTO.LoginResponseDto;

import lombok.Getter;
import lombok.Setter;

/**
 * SingleTon class for maintain the sessionId
 */
public class LoginData {
    private static LoginData mInstance = null;

    @Getter
    @Setter
    private LoginResponseDto loginData;


    @Getter
    @Setter
    private long fpsId;

    private LoginData() {
        loginData = new LoginResponseDto();
    }

    public static synchronized LoginData getInstance() {
        if (mInstance == null) {
            mInstance = new LoginData();
        }
        return mInstance;
    }

}
