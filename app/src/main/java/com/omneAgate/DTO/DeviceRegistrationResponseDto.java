package com.omneAgate.DTO;

import lombok.Data;

@Data
public class DeviceRegistrationResponseDto extends BaseDto {

    boolean registrationStatus;

    ErrorResponse errorResponse;

}
