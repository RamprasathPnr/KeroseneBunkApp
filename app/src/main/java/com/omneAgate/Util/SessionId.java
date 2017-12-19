package com.omneAgate.Util;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

/**
 * SingleTon class for maintain the sessionId
 */
public class SessionId {

    private static SessionId mInstance = null;

    @Getter
    @Setter
    private String sessionId;

    @Getter
    @Setter
    private long userId;

    @Getter
    @Setter
    private String password;

    @Getter
    @Setter
    private String transactionId;

    @Getter
    @Setter
    private String userName;

    @Getter
    @Setter
    private boolean qrOTPEnabled = false;

    @Getter
    @Setter
    private String fpsCode;


    @Getter
    @Setter
    private long fpsId;

    @Getter
    @Setter
    private Date loginTime;

    @Getter
    @Setter
    private Date lastLoginTime;

    @Getter
    @Setter
    private Long rrc_districtid;

    @Getter
    @Setter
    private Long rrc_talukid;

    @Getter
    @Setter
    private Long rrc_villageid;

    @Getter
    @Setter
    private String entitlementClassification;

    private SessionId() {
        sessionId = "";
        userName = "";
        fpsCode = "";
        userId = 0l;
        fpsId = 0l;
        password = "";
        transactionId = "";
        loginTime = new Date();
        lastLoginTime = new Date();
        rrc_districtid=0l;
        rrc_talukid=0l;
        rrc_villageid=0l;
        entitlementClassification = "";
    }

    public static synchronized SessionId getInstance() {
        if (mInstance == null) {
            mInstance = new SessionId();
        }
        return mInstance;
    }

}
