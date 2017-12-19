package com.omneAgate.Bunker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.omneAgate.DTO.EnumDTO.ServiceListenerType;

/**
 * Created by user1 on 18/8/15.
 */
public class VersionUpgradeDetail extends BaseActivity {


    @Override
    protected void processMessage(Bundle message, ServiceListenerType what) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.version_upgrade);
        setUpPopUpPage();
        setTamilText((TextView) findViewById(R.id.top_textView), R.string.version_upgrade);
        findViewById(R.id.imageViewBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        TextView newversionTv = (TextView)findViewById(R.id.deviceNum);
        TextView oldversionTv = (TextView)findViewById(R.id.batteryHealth);
        TextView stateTv = (TextView)findViewById(R.id.deviceScale);
        TextView descriptionTv = (TextView)findViewById(R.id.batteryLvl);
        TextView createDateTv = (TextView)findViewById(R.id.batteryLevel);
        TextView UpdateDateTv = (TextView)findViewById(R.id.batteryStatus);
        TextView statusTv = (TextView)findViewById(R.id.batteryTemp);
        try
        {
            Intent intent = getIntent();
            String newVersion = intent.getStringExtra("currVersion");
            String oldVersion = intent.getStringExtra("oldVersion");
            String ststus = intent.getStringExtra("status");
            String description = intent.getStringExtra("descrption");
            String createDate = intent.getStringExtra("dateCreate");
            String UpdateDate = intent.getStringExtra("dateUpdate");
            String state = intent.getStringExtra("state");
            if(!newVersion.isEmpty())
            {
                newversionTv.setText(newVersion);
            }
            if(!oldVersion.isEmpty())
            {
                oldversionTv.setText(oldVersion);
            }
            if(!ststus.equals(null))
            {
                stateTv.setText(ststus);
            }
            if(!description.equals(null) )
            {
                descriptionTv.setText(description);
            }
            if(!createDate.isEmpty())
            {
                createDateTv.setText(createDate);
            }
            if(!UpdateDate.equals(null))
            {
                UpdateDateTv.setText(UpdateDate);
            }
            if(!state.equals(null))
            {
                statusTv.setText(state);
            }
        }
        catch(Exception e)
        {
            Log.e("error",e.toString(),e);
        }





    }


    @Override
    public void onBackPressed() {

        startActivity(new Intent(this, VersionUpgradeInfo.class));
        finish();
    }


}
