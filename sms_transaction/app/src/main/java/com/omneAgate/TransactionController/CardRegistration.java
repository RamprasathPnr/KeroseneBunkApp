package com.omneAgate.TransactionController;

import android.content.Context;

import com.omneAgate.DTO.BenefActivNewDto;
import com.omneAgate.DTO.TransactionBaseDto;
import com.omneAgate.DTO.UpdateStockRequestDto;

/**
 * Created by user1 on 8/4/15.
 */
public interface CardRegistration {

    public boolean process(Context context, TransactionBaseDto transaction, BenefActivNewDto benefActivNewDto);

}

