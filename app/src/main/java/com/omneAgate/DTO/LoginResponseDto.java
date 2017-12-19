package com.omneAgate.DTO;

import android.database.Cursor;

import com.omneAgate.DTO.EnumDTO.DeviceStatus;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import lombok.Data;

@Data
public class LoginResponseDto extends BaseDto implements Serializable {

    boolean authenticationStatus;   // Status of Authentication - true - authenticated  , false - not authenticated

    String sessionid;    //Session id only if authentication is successful.

    UserDetailDto userDetailDto; //user details

    DeviceStatus deviceStatus;

    String key;

    long serverTime;

    String timezone;

    long loginDetailsId;

    List<GlobalConfigsDTO> globalConfigs;

    Set<AppfeatureDto> roleFeatures;

    KeroseneDto keroseneBunkDto;

    public LoginResponseDto() {

    }

    public LoginResponseDto(Cursor cur) {

        userDetailDto = new UserDetailDto(cur);
        keroseneBunkDto=new KeroseneDto(cur);
    }
}
