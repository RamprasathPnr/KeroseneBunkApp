package com.omneAgate.Bunker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.omneAgate.DTO.EnumDTO.ServiceListenerType;
import com.omneAgate.DTO.RoleFeatureDto;
import com.omneAgate.Util.FPSDBHelper;
import com.omneAgate.Util.SessionId;
import com.omneAgate.Util.Util;

import java.util.ArrayList;
import java.util.List;

public class StockManagementActivity extends BaseActivity {

    final ArrayList<String> fpsRoleName = new ArrayList<String>();

    RelativeLayout stockInwards, stockBalance;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_stock_management);
        setUpInitialPage();

    }

    public void rollViews() {
        long id = FPSDBHelper.getInstance(this).retrieveId("STOCK_MANAGEMENT_MENU");
        List<RoleFeatureDto> retriveRollFeature = new ArrayList<>();
        retriveRollFeature = FPSDBHelper.getInstance(this).retrieveSalesOrderData(id, SessionId.getInstance().getUserId());
        int rollFeatureSize = retriveRollFeature.size();
        for (int i = 0; i < rollFeatureSize; i++) {
            String roleName = retriveRollFeature.get(i).getRollName();
            fpsRoleName.add("" + roleName);
            if (roleName.equalsIgnoreCase("STOCK_INWARD")) {
                stockInwards.setVisibility(View.VISIBLE);
            }
            if (roleName.equalsIgnoreCase("STOCK_STATUS")) {
                stockBalance.setVisibility(View.VISIBLE);
            }
        }
        Log.e("retrive_arr_feature", "" + fpsRoleName);
    }

    private void setUpInitialPage() {
        setUpPopUpPage();
        Util.LoggingQueue(this, "Stock mgmt activity", "Start up page Called");
        stockInwards = (RelativeLayout) findViewById(R.id.stock_inward_lay);
        stockBalance = (RelativeLayout) findViewById(R.id.stock_check_lay);
        setTamilText((TextView) findViewById(R.id.correct_history_tv), R.string.correction_history_tv);
        setTamilText((TextView) findViewById(R.id.top_textView), R.string.stock_management);
        setTamilText((TextView) findViewById(R.id.stock_inward_tv), R.string.stock_inward_tv);
        setTamilText((TextView) findViewById(R.id.stock_status_tv), R.string.stock_status_tv);
        findViewById(R.id.stock_inward).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.LoggingQueue(StockManagementActivity.this, "Stock mgmt activity", "Clicked stock Inward");
                startActivity(new Intent(StockManagementActivity.this, FpsStockInwardActivity.class));
                finish();
            }
        });
        findViewById(R.id.stock_check).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.LoggingQueue(StockManagementActivity.this, "Stock mgmt activity", "Clicked Stock Check");
                startActivity(new Intent(StockManagementActivity.this, StockCheckActivity.class));
                finish();
            }
        });
        findViewById(R.id.imageViewBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        findViewById(R.id.correct_history).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.LoggingQueue(StockManagementActivity.this, "Stock mgmt activity", "Clicked correction history");
                startActivity(new Intent(StockManagementActivity.this, StockAdjustmentPage.class));
                finish();
            }
        });
        rollViews();
    }

    @Override
    public void onBackPressed() {
        Util.LoggingQueue(this, "Stock mgmt activity", "On Back pressed Called");
        startActivity(new Intent(this, SaleActivity.class));
        finish();
    }


    @Override
    protected void processMessage(Bundle message, ServiceListenerType what) {

    }


}