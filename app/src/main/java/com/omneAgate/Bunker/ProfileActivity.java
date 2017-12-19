package com.omneAgate.Bunker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.omneAgate.DTO.EnumDTO.ServiceListenerType;
import com.omneAgate.DTO.KeroseneDto;
import com.omneAgate.Util.LoginData;
import com.omneAgate.Util.SessionId;
import com.omneAgate.Util.Util;

import org.apache.commons.lang3.StringUtils;


public class ProfileActivity extends BaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_profile);
        appState = (GlobalAppState) getApplication();
        setUpDashBoard();

    }



    /**
     * Used to set the dashboard page
     * <p/>
     * user name and onCLickListeners are in this function
     */
    private void setUpDashBoard() {
        Util.LoggingQueue(this, "Profile page", "Profile page opened");
        setTamilText((TextView) findViewById(R.id.top_textView), R.string.fps_profile);
        setTamilText((TextView) findViewById(R.id.fps_code), R.string.fps_code1);
        setTamilText((TextView) findViewById(R.id.bunker_name), R.string.bunker_name);
        setTamilText((TextView) findViewById(R.id.store_type), R.string.store_type);
        setTamilText((TextView) findViewById(R.id.contact_person), R.string.bunker_seller_name);
        setTamilText((TextView) findViewById(R.id.contact_number), R.string.bunker_mobile_no);
        setTamilText((TextView) findViewById(R.id.address), R.string.bunker_address);
        setTamilText((TextView) findViewById(R.id.button_cancel), R.string.close);
        KeroseneDto fpsStore = LoginData.getInstance().getLoginData().getKeroseneBunkDto();
        String fpsCode = "";
        if(StringUtils.isNotEmpty(SessionId.getInstance().getFpsCode())){
            fpsCode = " / "+SessionId.getInstance().getFpsCode().toUpperCase();
        }
        ((TextView) findViewById(R.id.profile_fps_code)).setText(""+fpsStore.getCode());
         String category = "full time";
        ((TextView) findViewById(R.id.profile_fps_working_hours)).setText(""+fpsStore.getName());
        ((TextView) findViewById(R.id.profile_location)).setText(""+fpsStore.getContactPersonName());
        ((TextView) findViewById(R.id.profile_agency)).setText(""+fpsStore.getContactNumber());

         ((TextView) findViewById(R.id.profile_sim_no)).setText(""+fpsStore.getAddress());

        if(fpsStore.getKeroseneBunkCategory()!=null) {
            category = fpsStore.getKeroseneBunkCategory().toString();
            /*if (StringUtils.isNotEmpty(category)) {
                category = StringUtils.replace(category, "_", " ");
            }*/
            if(category.equalsIgnoreCase("full_time")){
                category="FULL TIME";
            }else if(category.equalsIgnoreCase("part_time")){
                category="PART TIME";
            }else if(category.equalsIgnoreCase("mobile")){
                category="MOBILE";
            }
            ((TextView) findViewById(R.id.profile_type)).setText(category);
        }

        setUpPopUpPage();
        findViewById(R.id.imageViewBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        findViewById(R.id.button_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, SaleActivity.class));
                finish();
            }
        });
    }


    //Called when user press back button
    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, SaleActivity.class));
        Util.LoggingQueue(this, "Profile page", "Back pressed Called");
        finish();
    }

    //Concrete method
    @Override
    public void processMessage(Bundle message, ServiceListenerType what) {

    }


}
