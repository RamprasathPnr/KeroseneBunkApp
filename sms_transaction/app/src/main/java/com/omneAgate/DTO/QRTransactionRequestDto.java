package com.omneAgate.DTO;

import java.io.Serializable;

import lombok.Data;

@Data
public class QRTransactionRequestDto implements Serializable {

    String qrCode;

    String deviceId;

}
