package com.omneAgate.TransactionController;

import com.omneAgate.DTO.UpdateStockRequestDto;

/**
 * Created by user1 on 17/4/15.
 */
public interface SMSListener {
    void smsReceived(UpdateStockRequestDto stockRequestDto);
}
