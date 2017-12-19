package com.omneAgate.TransactionController;

import com.omneAgate.DTO.BenefActivNewDto;
import com.omneAgate.DTO.UpdateStockRequestDto;

/**
 * Created by user1 on 17/4/15.
 */
public interface SMSForCardListener {
    void smsCardReceived(BenefActivNewDto benefActivNewDto);
}
