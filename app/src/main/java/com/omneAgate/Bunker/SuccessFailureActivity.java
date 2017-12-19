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
import com.omneAgate.Util.Util;

import org.apache.commons.lang3.StringUtils;

public class SuccessFailureActivity extends BaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success_failure);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        String message = getIntent().getStringExtra("message");
        continuePage(message);
    }



    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, SaleActivity.class));
        Util.LoggingQueue(this, "Success/Error page", "Back pressed");
        finish();
    }

    @Override
    protected void processMessage(Bundle message, ServiceListenerType what) {

    }

    private void continuePage(String message) {
        findViewById(R.id.imageViewBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        setTamilText((TextView) findViewById(R.id.top_textView), R.string.success_status);
        setTamilText((TextView) findViewById(R.id.buttonContinue), getString(R.string.sales));
        if (StringUtils.isEmpty(message)) {
            message = getString(R.string.internalError);
        }
        Util.LoggingQueue(this, "Success/Error page", "Error message:" + message);
        ((TextView) findViewById(R.id.textViewMessage)).setText(message);
        findViewById(R.id.buttonContinue).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SuccessFailureActivity.this, SaleActivity.class));
                Util.LoggingQueue(SuccessFailureActivity.this, "Success/Error page", "Continue button called");
                finish();
            }
        });
    }


}
