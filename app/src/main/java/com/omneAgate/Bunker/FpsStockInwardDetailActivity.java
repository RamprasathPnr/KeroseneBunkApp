package com.omneAgate.Bunker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.omneAgate.DTO.ChellanProductDto;
import com.omneAgate.DTO.EnumDTO.ServiceListenerType;
import com.omneAgate.DTO.GodownStockOutwardDto;
import com.omneAgate.DTO.ProductDto;
import com.omneAgate.Util.Constants.FPSDBConstants;
import com.omneAgate.Util.CustomProgressDialog;
import com.omneAgate.Util.FPSDBHelper;
import com.omneAgate.Util.NetworkConnection;
import com.omneAgate.Util.SessionId;
import com.omneAgate.Util.Util;
import com.omneAgate.Bunker.dialog.StockInwardDialog;
import com.omneAgate.service.InwardUpdateToServer;

import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class FpsStockInwardDetailActivity extends BaseActivity {

    TextView chellanIdTv;

    //Acknowledgement Date Textview
    TextView deliverdDateTv;

    // Batch no Textview
    TextView vehicleNoTv;

    //CheckBox for Acknowledgement status
    boolean ackStatus = false;


    // Godown Dto list
    List<GodownStockOutwardDto> fpsStockInwardDetailList;

    // Godown Dto
    GodownStockOutwardDto godownStockOutwardDtos;

    String godownName = "";

    Long timeOnClick = 0l;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fps_stock_inward_detail);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        godownName = getIntent().getExtras().getString("godown");
        godownStockOutwardDtos = new Gson().fromJson(godownName, GodownStockOutwardDto.class);
        chellanIdTv = (TextView) findViewById(R.id.tvChallan);
        deliverdDateTv = (TextView) findViewById(R.id.tvDeliverdDate);
        vehicleNoTv = (TextView) findViewById(R.id.tvVehicle);
        networkConnection = new NetworkConnection(this);
        createPage();

    }


    private void setTextView() {
        Util.LoggingQueue(this, "Stock Inward Details activity", "Main page Called");
        TextView topTv = (TextView) findViewById(R.id.top_textView);
        setTamilText(((TextView) findViewById(R.id.tvChallanNoLabel)), R.string.reference_no);
        setTamilText(((TextView) findViewById(R.id.tvDeliverLabel)), R.string.delivered_date);
        setTamilText(((TextView) findViewById(R.id.tvVehicleLabel)), R.string.vehicle_no);
        setTamilText(((TextView) findViewById(R.id.fpsInvardDetailProductIdLabel)), R.string.commodity);
        setTamilText(((TextView) findViewById(R.id.fpsInvardDetailReceivedQuantityLabel)), R.string.recvd_qty);
        setTamilText(((TextView) findViewById(R.id.fpsInvardDetailAcknowledgeLabel)), R.string.ack);
        if (!ackStatus)
            setTamilText(((TextView) findViewById(R.id.btnfpsIDSubmit)), R.string.submit);
        setTamilText(((TextView) findViewById(R.id.btnfpsIDCancel)), R.string.cancel);
        setTamilText(topTv, R.string.inward_transit);

    }


    private void createPage() {

        if (godownStockOutwardDtos.isFpsAckStatus()) {
            ackStatus = true;
            setDisableWidgets();
        } else {
            dismissDialog();

        }
        fpsStockInwardDetailList = FPSDBHelper.getInstance(this).showFpsStockInvardDetail(godownStockOutwardDtos.getReferenceNo(), ackStatus);

        Util.LoggingQueue(this, "Stock Inward detail activity", "Selected Inward:" + fpsStockInwardDetailList);
        setUpPopUpPage();
        setTextView();
        findViewById(R.id.imageViewBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        if (fpsStockInwardDetailList != null) {
            for (GodownStockOutwardDto godownStockOutwardDto : fpsStockInwardDetailList) {
                godownStockOutwardDtos = godownStockOutwardDto;
                chellanIdTv.setText(godownStockOutwardDto.getReferenceNo());
                if (StringUtils.isNotEmpty(godownStockOutwardDto.getVehicleN0()))
                    vehicleNoTv.setText(godownStockOutwardDto.getVehicleN0().toUpperCase());
            }

            deliverdDateTv.setText("" + new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime()));
            if (godownStockOutwardDtos != null)
                godownStockOutwardDtos.setFpsAckDate(System.currentTimeMillis());
            configureData();
        }


    }




    private void setDisableWidgets() {
        findViewById(R.id.btnfpsIDCancel).setVisibility(View.INVISIBLE);
        setTamilText(((TextView) findViewById(R.id.btnfpsIDSubmit)), R.string.close);
        findViewById(R.id.btnfpsIDSubmit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }


    /*Data from server has been set inside this function*/
    private void configureData() {
        try {
            LinearLayout fpsInwardLinearLayout = (LinearLayout) findViewById(R.id.listView_fps_stock_inward_detail);
            Log.i("Detail", fpsStockInwardDetailList.toString());
            fpsInwardLinearLayout.removeAllViews();
            for (int position = 0; position < fpsStockInwardDetailList.size(); position++) {
                LayoutInflater lin = LayoutInflater.from(this);
                fpsInwardLinearLayout.addView(returnView(lin, fpsStockInwardDetailList.get(position), position));
            }

        } catch (Exception e) {
            Util.LoggingQueue(this, "Error", "Inward:" + e.toString());
            Log.e("Except", e.toString(), e);
        }
    }


    /*User entitlement view*/
    private View returnView(LayoutInflater entitle, GodownStockOutwardDto data, int position) {
        View convertView = entitle.inflate(R.layout.adapter_fps_stock_inward_details, null);
        TextView productName = (TextView) convertView.findViewById(R.id.fpsInvardDetailProductId);
        TextView productUnit = (TextView) convertView.findViewById(R.id.fpsInvardDetailUnitId);

        TextView receivedQuantity = (TextView) convertView.findViewById(R.id.fpsInvardDetailReceivedQuantity);
        CheckBox acknowledgeCbox = (CheckBox) convertView.findViewById(R.id.fpsInvardDetailAcknowledge);
        ProductDto product = FPSDBHelper.getInstance(this).getProductDetails(data.getProductId());
        Log.i("Product", product.toString());
        if (product != null) {
            productName.setText(product.getName());
            if (GlobalAppState.language.equalsIgnoreCase("ta") && StringUtils.isNotEmpty(product.getLocalProductUnit())) {
                setTamilText(productName, product.getLocalProductName());
            }

            productUnit.setText(product.getProductUnit());
            if (GlobalAppState.language.equalsIgnoreCase("ta") && StringUtils.isNotEmpty(product.getLocalProductUnit())) {
                setTamilText(productUnit, product.getLocalProductUnit());
            }

        }
        acknowledgeCbox.setId(position);
        NumberFormat formatter = new DecimalFormat("#0.000");
        receivedQuantity.setText(formatter.format(data.getQuantity()));
        if (ackStatus) {
            acknowledgeCbox.setEnabled(false);
            acknowledgeCbox.setChecked(true);
        } else {
            acknowledgeCbox.setChecked(false);
        }
        return convertView;

    }

    // This method submit all data Fps stock outward data with received quantity
    private void onSubmit() {
        try {
            if (SystemClock.elapsedRealtime() - timeOnClick < 4000) {
                return;
            }
            timeOnClick = SystemClock.elapsedRealtime();
            findViewById(R.id.btnfpsIDCancel).setEnabled(false);
            findViewById(R.id.btnfpsIDSubmit).setEnabled(false);
            findViewById(R.id.btnfpsIDCancel).setClickable(false);
            findViewById(R.id.btnfpsIDSubmit).setClickable(false);
            findViewById(R.id.btnfpsIDCancel).setOnClickListener(null);
            findViewById(R.id.btnfpsIDSubmit).setOnClickListener(null);
            if (fpsStockInwardDetailList != null) {
                Set<ChellanProductDto> setChellanProductDto = new HashSet<>();
                boolean checkedReceivedQuantity = false;
                for (int i = 0; i < fpsStockInwardDetailList.size(); i++) {
                    checkedReceivedQuantity = ((CheckBox) findViewById(i)).isChecked();
                    if (!checkedReceivedQuantity) {
                        Util.messageBar(FpsStockInwardDetailActivity.this, getString(R.string.ack_product));
                        dismissDialog();
                        return;
                    }
                    ChellanProductDto chellanProductDto = new ChellanProductDto();
                    chellanProductDto.setProductId(fpsStockInwardDetailList.get(i).getProductId());
                    chellanProductDto.setQuantity(fpsStockInwardDetailList.get(i).getQuantity());
                    chellanProductDto.setReceiProQuantity(fpsStockInwardDetailList.get(i).getQuantity());
                    setChellanProductDto.add(chellanProductDto);
                }
                godownStockOutwardDtos.setProductDto(setChellanProductDto);
                getRequest();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    //Request to Server
    public void getRequest() {
        try {
            if (FPSDBHelper.getInstance(this).getStockExists(godownStockOutwardDtos)) {
                FPSDBHelper.getInstance(this).updateReceivedQuantity(godownStockOutwardDtos, true);
                if(FPSDBHelper.getInstance(this).getAllStockSync(godownStockOutwardDtos.getReferenceNo()) != null) {
                    progressBar = new CustomProgressDialog(this);
                    progressBar.setCanceledOnTouchOutside(false);
                    progressBar.show();
                    StockInwardDialog stockInwardDialog = new StockInwardDialog(this);
                    stockInwardDialog.show();
                    dismissDialog();
                    if (networkConnection.isNetworkAvailable() && StringUtils.isNotEmpty(SessionId.getInstance().getSessionId())) {
                        InwardUpdateToServer inward = new InwardUpdateToServer(this);
                        inward.sendBillToServer(godownStockOutwardDtos.getReferenceNo());
                    }
                }
            } else {
                dismissDialog();
                Util.messageBar(this, getString(R.string.internalError));
            }

        } catch (Exception e) {
            dismissDialog();
            Util.LoggingQueue(this, "Error", e.toString());
            Log.e("FPSStockInwardDetail", e.toString(), e);
        }

    }


    private void dismissDialog() {
        if (progressBar != null && progressBar.isShowing()) {
            progressBar.dismiss();
        }
        findViewById(R.id.btnfpsIDCancel).setEnabled(true);
        findViewById(R.id.btnfpsIDSubmit).setEnabled(true);
        findViewById(R.id.btnfpsIDCancel).setClickable(true);
        findViewById(R.id.btnfpsIDSubmit).setClickable(true);
        findViewById(R.id.btnfpsIDCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        findViewById(R.id.btnfpsIDSubmit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSubmit();
            }
        });
    }


    @Override
    protected void processMessage(Bundle message, ServiceListenerType what) {
        switch (what) {
            case FPS_INTENT_REQUEST:
                getStockResponse(message);
                break;
            default:
                dismissDialog();
               /* FPSDBHelper.getInstance(this).updateReceivedQuantity(godownStockOutwardDtos, true);
                StockInwardDialog stockInwardDialog = new StockInwardDialog(this);
                stockInwardDialog.show();*/
                break;
        }
    }


    // After response received from server successfully in android
    public void getStockResponse(Bundle message) {
        try {
            dismissDialog();
            String response = message.getString(FPSDBConstants.RESPONSE_DATA);
            Util.LoggingQueue(this, "Stock Inward detail activity", "Response request:" + response);
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            GodownStockOutwardDto godownStockOutwardDto = gson.fromJson(response, GodownStockOutwardDto.class);
            int statusCode = godownStockOutwardDto.getStatusCode();
            if (statusCode == 0) {
                FPSDBHelper.getInstance(this).updateReceivedQuantity(godownStockOutwardDtos, false);
                Util.LoggingQueue(this, "Stock Inward Detail activity", "Inserting into Database");
                StockInwardDialog stockInwardDialog = new StockInwardDialog(this);
                stockInwardDialog.show();
            } else {
                Util.LoggingQueue(this, "Stock Inward detail activity", "Error in insertion");
                Util.messageBar(this, Util.messageSelection(FPSDBHelper.getInstance(this).retrieveLanguageTable(godownStockOutwardDto.getStatusCode())));
                onBackPressed();
            }
        } catch (Exception e) {
            Util.LoggingQueue(this, "Error", e.toString());
            onBackPressed();
            Log.e("FPSStockInwardDetail", e.toString(), e);
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, FpsStockInwardActivity.class));
        Util.LoggingQueue(this, "Stock Inward activity", "Back pressed Called");
        finish();
    }


}
