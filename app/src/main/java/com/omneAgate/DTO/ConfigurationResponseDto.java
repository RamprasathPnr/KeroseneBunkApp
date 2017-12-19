package com.omneAgate.DTO;

import java.io.Serializable;
import java.util.HashMap;

import lombok.Data;

@Data
public class ConfigurationResponseDto extends BaseDto implements Serializable {

    HashMap<String,String> posGlobalConfigMap;

    public ConfigurationResponseDto() {

    }


}
