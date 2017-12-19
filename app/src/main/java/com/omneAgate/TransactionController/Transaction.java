package com.omneAgate.TransactionController;

import android.content.Context;

import com.omneAgate.DTO.TransactionBaseDto;
import com.omneAgate.DTO.UpdateStockRequestDto;

/**
 * Created by user1 on 8/4/15.
 */
public interface Transaction {

    boolean process(Context context, TransactionBaseDto transaction, UpdateStockRequestDto updateStock);

}

