package com.omneAgate.Bunker;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.omneAgate.DTO.BillItemDto;
import com.omneAgate.DTO.EnumDTO.ServiceListenerType;
import com.omneAgate.DTO.FPSStockHistoryDto;
import com.omneAgate.DTO.POSStockAdjustmentDto;
import com.omneAgate.DTO.UserDto.StockCheckDto;
import com.omneAgate.Util.FPSDBHelper;
import com.omneAgate.Util.Util;

import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StockCheckActivity extends BaseActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_stockcommodity);
        configureData();

    }


    /*Data from server has been set inside this function*/
    private void configureData() {
        try {
            setUpPopUpPage();
            Util.LoggingQueue(this, "Stock Status activity", "Main page Called");
            setTamilText((TextView) findViewById(R.id.top_textView), R.string.stock_status_tv);
            setTamilText((TextView) findViewById(R.id.commodity), R.string.commodity);
            setTamilText((TextView) findViewById(R.id.opening_stock), R.string.opening_stock);
            setTamilText((TextView) findViewById(R.id.inward_qty), R.string.inward_qty);
            setTamilText((TextView) findViewById(R.id.sale_qty), R.string.sale_qty);
            setTamilText((TextView) findViewById(R.id.current_stock), R.string.current_stock);
            setTamilText((TextView) findViewById(R.id.btnClose), R.string.close);
            setTamilText((TextView) findViewById(R.id.stock_adjustment), R.string.stock_adjust);
            LinearLayout transactionLayout = (LinearLayout) findViewById(R.id.listView_linearLayout_stock_status);
            findViewById(R.id.btnClose).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(StockCheckActivity.this, StockManagementActivity.class));
                    finish();
                }
            });
            findViewById(R.id.imageViewBack).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String date = df2.format(c.getTime());
            List<StockCheckDto> productDtoList = FPSDBHelper.getInstance(this).getAllProductStockDetails(date);
            transactionLayout.removeAllViews();
            for (StockCheckDto products : productDtoList) {
                LayoutInflater lin = LayoutInflater.from(this);
                transactionLayout.addView(returnView(lin, products));
            }

        } catch (Exception e) {
            Log.e("StockCheckActivity", e.toString(), e);
        }
    }


    private Double getProductInward(long productId) {
//        SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
//        String date = df2.format(new Date());
       // BillItemDto productInwardToday = FPSDBHelper.getInstance(this).getAllInwardListToday(date,productId);
        BillItemDto productInwardToday = FPSDBHelper.getInstance(this).getAllInwardListTodaylist(productId);
        Log.e("***stock check ACTIVITY",""+productInwardToday.getQuantity());
        return productInwardToday.getQuantity();
    }

    /**
     * User entitlement view
     */
    private View returnView(LayoutInflater entitle, StockCheckDto fpsStock) {
        View convertView = entitle.inflate(R.layout.adapter_commodity_stock, null);
        TextView productNameTv = (TextView) convertView.findViewById(R.id.entitlementName);
        TextView unitTv = (TextView) convertView.findViewById(R.id.entitlementUnit);
        TextView openingStockTv = (TextView) convertView.findViewById(R.id.entitlement_opening);
        TextView adjustmentStock = (TextView) convertView.findViewById(R.id.entitlement_adjustment);
        TextView saleQuantityTv = (TextView) convertView.findViewById(R.id.entitlement_sale);
        TextView currentStockTv = (TextView) convertView.findViewById(R.id.amount_current);
        TextView inwardQuantityTv = (TextView) convertView.findViewById(R.id.entitlement_inward_quantity);

        NumberFormat format = new DecimalFormat("#0.000");
        FPSStockHistoryDto fpsStockHistory = FPSDBHelper.getInstance(this).getAllProductStockHistoryDetails(fpsStock.getProductId());
        List<POSStockAdjustmentDto> fpsStockAdjustment = FPSDBHelper.getInstance(this).getStockAdjustment(fpsStock.getProductId());
        double adjustment = 0.0;
        if (fpsStockAdjustment.size() == 0) {
            adjustmentStock.setText(format.format(adjustment));
        } else {
            adjustment = getAdjustedValue(fpsStockAdjustment);
            adjustmentStock.setText(format.format(adjustment));
            if(adjustment<0){
                adjustmentStock.setTextColor(Color.RED);
            }
        }
        productNameTv.setText(fpsStock.getName());
        if (GlobalAppState.language.equalsIgnoreCase("ta") && StringUtils.isNotEmpty(fpsStock.getLocalName())) {
            productNameTv.setText(unicodeToLocalLanguage(fpsStock.getLocalName()));
        }
        unitTv.setText(fpsStock.getUnit());
        if (GlobalAppState.language.equalsIgnoreCase("ta") && StringUtils.isNotEmpty(fpsStock.getLocalUnit())) {
            unitTv.setText(unicodeToLocalLanguage(fpsStock.getLocalUnit()));
        }
        openingStockTv.setText(format.format(fpsStockHistory.getCurrQuantity()));
        currentStockTv.setText(format.format(fpsStock.getQuantity()));
        saleQuantityTv.setText(format.format(fpsStock.getSold()));
        double inward = getProductInward(fpsStock.getProductId());
        inwardQuantityTv.setText(format.format(inward));
        return convertView;

    }


    private double getAdjustedValue(List<POSStockAdjustmentDto> adjustment) {
        double quantity = 0.0;
        for (POSStockAdjustmentDto productValue : adjustment) {
            double productQuantity = productValue.getQuantity();
            if (productValue.getRequestType().equalsIgnoreCase("STOCK_DECREMENT")) {
                productQuantity = -1 * productQuantity;
            }
            quantity = quantity + productQuantity;
        }
        return quantity;
    }


    @Override
    protected void processMessage(Bundle message, ServiceListenerType what) {

    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, StockManagementActivity.class));
        Util.LoggingQueue(this, "Stock Status activity", "Back pressed Called");
        finish();
    }


}
