package com.omneAgate.DTO;

import lombok.Data;

/**
 * Created for DeviceRegistrationDto
 */
@Data
public class DeviceRegistrationDto extends BaseDto {

    LoginDto loginDto;

    DeviceDetailsDto deviceDetailsDto;


}
