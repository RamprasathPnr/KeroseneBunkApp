package com.omneAgate.DTO;

import lombok.Data;

/**
 * Created by ramprasath on 12/5/16.
 */
@Data
public class LogoutDto {
    String sessionId;
    String logoutStatus;
    String logoutTime;
    ApplicationType appType = ApplicationType.KEROSENE_BUNK;


}
