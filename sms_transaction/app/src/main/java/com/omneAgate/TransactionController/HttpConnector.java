package com.omneAgate.TransactionController;

import android.content.Context;

import com.omneAgate.DTO.TransactionBaseDto;
import com.omneAgate.DTO.UpdateStockRequestDto;

/**
 * Created by Rajesh on 4/8/2015.
 */
public class HttpConnector implements Transaction {
    @Override
    public boolean process(Context context, TransactionBaseDto transaction, UpdateStockRequestDto updateStock) {
        return false;
    }
}
