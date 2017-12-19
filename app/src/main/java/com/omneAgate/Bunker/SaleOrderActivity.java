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
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.omneAgate.DTO.EnumDTO.Roll_Feature;
import com.omneAgate.DTO.EnumDTO.ServiceListenerType;
import com.omneAgate.DTO.RoleFeatureDto;
import com.omneAgate.DTO.RollMenuDto;
import com.omneAgate.Util.FPSDBHelper;
import com.omneAgate.Util.SessionId;
import com.omneAgate.Util.Util;
import com.omneAgate.Bunker.dialog.fpsRollViewAdpter;

import java.util.ArrayList;
import java.util.List;

public class SaleOrderActivity extends BaseActivity {
    final ArrayList<String> fpsRoleName = new ArrayList<>();
    GridView fps_rollview;
    List<RollMenuDto> roleMenuDto = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_sale_order);
        rollViews();
        setUpSaleOrders();
    }


    private void rollViews() {
        long id = FPSDBHelper.getInstance(this).retrieveId("KB_SALES_ORDER_MENU");
        List<RoleFeatureDto> retriveRollFeature;
        retriveRollFeature = FPSDBHelper.getInstance(this).retrieveSalesOrderData(id, SessionId.getInstance().getUserId());
        int rollFeatureSize = retriveRollFeature.size();
        for (int i = 0; i < rollFeatureSize; i++) {
            String roll_Name = retriveRollFeature.get(i).getRollName();
            Log.e("rolefeatures", "" + roll_Name);
            try {
                if (!roll_Name.equalsIgnoreCase("KB_RATION_CARD_BASED")) {
                    Roll_Feature rolls = Roll_Feature.valueOf(roll_Name);
                    roleMenuDto.add(new RollMenuDto(getString(rolls.getRollName()), rolls.getBackground(), rolls.getColorCode(), rolls.getDescription()));
                    fpsRoleName.add(roll_Name);
                }
            } catch (Exception e) {
                Log.e("Error",e.toString(),e);
            }
        }
        Log.e("retrive_arr_feature", "" + fpsRoleName);
        fps_rollview = (GridView) findViewById(R.id.fpsroll);
        fps_rollview.setAdapter(new fpsRollViewAdpter(this, roleMenuDto));
        fps_rollview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    Util.LoggingQueue(SaleOrderActivity.this, "SaleActivity", "Moving to Sale Order activity");
                    String myClass = "com.omneAgate.Bunker." + roleMenuDto.get(i).getClassName();
                    Intent myIntent = new Intent(getApplicationContext(), Class.forName(myClass));
                    startActivity(myIntent);
                    finish();
                } catch (ClassNotFoundException e) {
                    Log.e("Error", "" + e.toString(), e);
                }


            }
        });

    }

    /**
     * Initial setUp
     */
    private void setUpSaleOrders() {
        Util.LoggingQueue(this, "Sale order activity", "Setting up sales order activity");
        setUpPopUpPage();
        setTamilText((TextView) findViewById(R.id.top_textView), R.string.sales_order);
        findViewById(R.id.imageViewBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }


    /*
    *  Mobile OTP page calling
    * */
    private void mobileOTP() {
        Util.LoggingQueue(this, "Sale order activity", "Mobile otp called");
        startActivity(new Intent(this, MobileOTPOptionsActivity.class));
        finish();
    }


    /*
    *
    * Qr code scan by user
    * if OTP enabled or not
    *
    * */
    private void qrCodeDataScan() {
        Util.LoggingQueue(this, "Sale order activity", "Qr code sales activity called");
        startActivity(new Intent(this, QRCodeSalesActivity.class));
        finish();
    }


    /**
     * Called when user press back button
     */
    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, SaleActivity.class));
        Util.LoggingQueue(this, "Sale order activity", "Back button pressed");
        finish();
    }

    //Concrete method
    @Override
    public void processMessage(Bundle message, ServiceListenerType what) {

    }


}