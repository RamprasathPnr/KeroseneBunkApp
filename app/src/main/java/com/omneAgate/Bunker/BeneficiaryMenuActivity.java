package com.omneAgate.Bunker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.omneAgate.DTO.EnumDTO.ServiceListenerType;
import com.omneAgate.Util.Util;

/**
 * Created by user1 on 31/7/15.
 */
public class BeneficiaryMenuActivity extends BaseActivity {

    RelativeLayout beneficiary_menu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_othermenu);
        beneficiary_menu = (RelativeLayout) findViewById(R.id.benecifiarymenu);
        setUpPopUpPage();
        setTamilText((TextView) findViewById(R.id.top_textView), getString(R.string.other_menus));
        findViewById(R.id.imageViewBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        setTamilText((TextView) findViewById(R.id.ration_card_based), getString(R.string.benedetails));
        beneficiary_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(getApplicationContext(), BeneficiaryListActivity.class);
                startActivity(myIntent);
                finish();
            }
        });
    }


    @Override
    protected void processMessage(Bundle message, ServiceListenerType what) {

    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, SaleActivity.class));
        Util.LoggingQueue(this, "Beneficiary List", "Back pressed");
        finish();
    }


}