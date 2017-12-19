package com.omneAgate.DTO;

import lombok.Data;

/**
 * Created by user1 on 15/7/15.
 */
@Data
public class StatisticsDto extends BaseDto {

    String deviceNum;

    int health;

    int scale;

    int level;

    int batteryLevel;

    int plugged;

    int status;

    String technology;

    int temperature;

    int voltage;

    boolean present;

    String latitude;

    String longtitude;

    int versionNum;

    int beneficiaryCount;

    int registrationCount;

    int unSyncBillCount;

    long lastUpdatedTime;

    String versionName;

    long apkInstalledTime;

    String cpuUtilisation;

    String memoryUsed;

    String memoryRemaining;

    String totalMemory;

    String networkInfo;

    String hardDiskSizeFree;

    String userId;

    String simId;

}
