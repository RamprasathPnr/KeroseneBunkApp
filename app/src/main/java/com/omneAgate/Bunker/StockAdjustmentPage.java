package com.omneAgate.Bunker;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.omneAgate.Bunker.dialog.StockAdjustmentDialog;
import com.omneAgate.DTO.EnumDTO.ServiceListenerType;
import com.omneAgate.DTO.POSStockAdjustmentDto;
import com.omneAgate.Util.FPSDBHelper;
import com.omneAgate.Util.PullToRefresh.LoadMoreListView;
import com.omneAgate.Util.Util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by ramprasath on 5/7/16.
 */
public class StockAdjustmentPage extends BaseActivity {

    int loadMore = 0;
    LoadMoreListView billByDate;

     private List<POSStockAdjustmentDto> StockAdjustmentList = null;
     RelativeLayout noRecords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.page_stock_adjustment);
    }

    @Override
    protected void onStart() {
        super.onStart();
        configureData();
    }

    private void setTopTextView() {

        Util.LoggingQueue(this, "StockAdjustmentPage ", "Main page Called");
        TextView topTv = (TextView) findViewById(R.id.top_textView);
        setTamilText(topTv, R.string.stock_adjust);
        setTamilText(((TextView) findViewById(R.id.btnClose)), R.string.submit);
        setTamilText(((TextView) findViewById(R.id.fpsInvardoutwardDateLabel)), R.string.reference_no);
        //setTamilText(((TextView) findViewById(R.id.godownRefNoLabel)), R.string.godown_reference_no);
        setTamilText(((TextView) findViewById(R.id.fpsInvardactionLabel)), R.string.dispatch_date);
        setTamilText(((TextView) findViewById(R.id.fpsInvardoutwardGodownNameLabel)), R.string.commodity);
        setTamilText(((TextView) findViewById(R.id.fpsInvardoutwardLapsedTimeabel)), R.string.fpsInvardDetailQuantity);
        setTamilText(((TextView) findViewById(R.id.fpsInvardactionGodownStatusLabel)), R.string.adjustment_type);
        setTamilText(((TextView) findViewById(R.id.fpsAckLabel)),R.string.status);
        setTamilText(((TextView) findViewById(R.id.tvViewStockHistory)), R.string.stock_adjustment_history_view);

        TextView tvViewStockHistory = (TextView) findViewById(R.id.tvViewStockHistory);
        tvViewStockHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(com.omneAgate.Bunker.StockAdjustmentPage.this, FpsStockAdjustmentListActivity.class));
                finish();
            }
        });



        TextView submitButton = (TextView) findViewById(R.id.btnClose);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (Util.ackAdjustmentList.size() == 0) {
                    Util.messageBar(StockAdjustmentPage.this, getString(R.string.ack_alert));

                }
                else {
                    findViewById(R.id.btnClose).setOnClickListener(null);
                    findViewById(R.id.btnClose).setBackgroundColor(Color.LTGRAY);

                    FPSDBHelper.getInstance(StockAdjustmentPage.this).stockAdjustmentData(Util.ackAdjustmentList);
                    StockAdjustmentDialog stockAdjustmentDialog = new StockAdjustmentDialog(StockAdjustmentPage.this);
                    stockAdjustmentDialog.show();
                }
            }
        });
    }

    /*Data from server has been set inside this function*/
    private void configureData() {
        try {
            setUpPopUpPage();
            setTopTextView();

            noRecords = (RelativeLayout) findViewById(R.id.linearLayoutNoRecords);
            Util.ackAdjustmentList = new ArrayList<POSStockAdjustmentDto>();
            StockAdjustmentList = new ArrayList<>();
            StockAdjustmentList = FPSDBHelper.getInstance(com.omneAgate.Bunker.StockAdjustmentPage.this).showFpsStockAdjustment(false, loadMore);
            LinearLayout fpsInwardLinearLayout = (LinearLayout) findViewById(R.id.listView_fps_stock_inward_detail);
            Log.i("Detail", StockAdjustmentList.toString());
            fpsInwardLinearLayout.removeAllViews();
            for (int position = 0; position < StockAdjustmentList.size(); position++) {
                LayoutInflater lin = LayoutInflater.from(StockAdjustmentPage.this);
                fpsInwardLinearLayout.addView(returnView(lin, StockAdjustmentList.get(position), position));
            }
            if(StockAdjustmentList.size() == 0) {
                noRecords.setVisibility(View.VISIBLE);
                findViewById(R.id.btnClose).setOnClickListener(null);
                findViewById(R.id.btnClose).setBackgroundColor(Color.LTGRAY);
            }
            else {
                noRecords.setVisibility(View.INVISIBLE);
            }

        } catch (Exception e) {
            Util.LoggingQueue(this, "Stock Inward activity", "Error:" + e.toString());
            Log.e("Error", e.toString(), e);
        } finally {
            findViewById(R.id.imageViewBack).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });

        }
    }

    private View returnView(LayoutInflater entitle, final POSStockAdjustmentDto data, final int itemPosition) {
        View convertView = entitle.inflate(R.layout.stock_adjustment_adapter, null);
        TextView sNo = (TextView) convertView.findViewById(R.id.tvSerialNo);
        TextView referenceNo = (TextView) convertView.findViewById(R.id.tvDeliveryChellanId);
        TextView dispatchDate = (TextView) convertView.findViewById(R.id.tvOutwardDate);
        TextView commodity = (TextView) convertView.findViewById(R.id.tvGodownName);
        TextView quantity = (TextView) convertView.findViewById(R.id.tvLapsedTime);
        TextView adjustmentType = (TextView) convertView.findViewById(R.id.btnStatus);
        CheckBox acknowledgeCbox = (CheckBox) convertView.findViewById(R.id.fpsAdjustmentAcknowledge);


        String productName = FPSDBHelper.getInstance(StockAdjustmentPage.this).getProductName(data.getProductId());
        sNo.setText(String.valueOf(itemPosition + 1));
          referenceNo.setText(String.valueOf(data.getId()));

        Log.e("Stock adjustment page", "created date" + data.getCreatedDate());
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        String dateString = sdf.format(data.getCreatedDate());

        dispatchDate.setText(dateString);
       // dispatchDate.setText(new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(data.getCreatedDate()));
        commodity.setText(productName);
        quantity.setText(String.valueOf(data.getQuantity()));
        if(data.getRequestType().equalsIgnoreCase("STOCK_INCREMENT")) {
            setTamilText(adjustmentType, R.string.stock_increment);
        }
        else if(data.getRequestType().equalsIgnoreCase("STOCK_DECREMENT")) {
            setTamilText(adjustmentType, R.string.stock_decrement);
        }
        acknowledgeCbox.setId(itemPosition);
        acknowledgeCbox.setChecked(false);

        acknowledgeCbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
         @Override
         public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
           if (isChecked) {
           if (!Util.ackAdjustmentList.contains(data)) {
                 Util.ackAdjustmentList.add(data);
                 }
             }
            else {

           if (Util.ackAdjustmentList.contains(data)) {
           Util.ackAdjustmentList.remove(data);
                     }
                  }
               }
             }
           );

         return convertView;
        }

    public void onClose(View view) {
        onBackPressed();

    }

    @Override
    public void onBackPressed() {
        Util.LoggingQueue(this, "Stock adjustment activity", "Back Pressed Called");
        startActivity(new Intent(this, StockManagementActivity.class));
        finish();
    }

    @Override
    protected void processMessage(Bundle message, ServiceListenerType what) {

    }

    @Override
    protected void onDestroy() {
        try {
            if (progressBar != null) {
                progressBar.dismiss();
            }
        }
        catch(Exception e) {}
        super.onDestroy();
    }
}

