package com.omneAgate.Util;

import lombok.Getter;
import lombok.Setter;

/**
 * SingleTon class for maintain the sessionId
 */
public class LocationId {
    private static LocationId mInstance = null;

    @Getter
    @Setter
    private String longitude;

    @Getter
    @Setter
    private String latitude;

    private LocationId() {
        longitude = "";
        latitude = "";
    }

    public static synchronized LocationId getInstance() {
        if (mInstance == null) {
            mInstance = new LocationId();
        }
        return mInstance;
    }

}
