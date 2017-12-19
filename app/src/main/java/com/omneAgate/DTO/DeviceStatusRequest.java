package com.omneAgate.DTO;

import java.io.Serializable;

import lombok.Data;

/**
 * Created by user1 on 25/3/15.
 */
@Data
public class DeviceStatusRequest extends BaseDto implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /**
     * Autogenerated device id
     */
    long id;

    /**
     * SIM Card number associated with the device
     */
    String sim;

    /**
     * Device IMEI id
     */
    String deviceNumber;

    /**
     * Status check -
     */
    boolean active;

    /*
    Device ASSociated status
     */
    boolean associated;
}