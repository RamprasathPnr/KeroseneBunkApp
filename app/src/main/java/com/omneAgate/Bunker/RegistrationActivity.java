package com.omneAgate.Bunker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.omneAgate.DTO.DeviceStatusRequest;
import com.omneAgate.DTO.EnumDTO.RequestType;
import com.omneAgate.DTO.EnumDTO.ServiceListenerType;
import com.omneAgate.Util.AndroidDeviceProperties;
import com.omneAgate.Util.Constants.FPSDBConstants;
import com.omneAgate.Util.NetworkConnection;
import com.omneAgate.Util.Util;
import com.omneAgate.service.HttpClientWrapper;

import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;

public class RegistrationActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        networkConnection = new NetworkConnection(this);
        appState = (GlobalAppState) getApplication();
        httpConnection = new HttpClientWrapper();
        setTamilHeader((TextView) findViewById(R.id.login_actionbar), R.string.headerAllPageEnglish);
        setTamil(((TextView) findViewById(R.id.login_actionbarTamil)), R.string.headerAllPage);
        ((TextView) findViewById(R.id.textRegistration)).setText(R.string.deviceRegistration);
        ((Button) findViewById(R.id.registrationButton)).setText(R.string.status);
        findViewById(R.id.popupMenu).setVisibility(View.GONE);
    }


    /*Concrete method*/
    @Override
    public void processMessage(Bundle message, ServiceListenerType what) {
        try {
            switch (what) {
                case DEVICE_STATUS:
                    setStatusCheck(message);
                    break;
                default:
                    Util.messageBar(RegistrationActivity.this, getString(R.string.serviceNotAvailable));
                    break;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Tamil text textView typeface
     * input  textView name and id for string.xml
     */
    public void setTamilHeader(TextView textName, int id) {
        try {
            Typeface tfBamini = Typeface.createFromAsset(getAssets(), "Impact.ttf");
            textName.setTypeface(tfBamini);
            textName.setText(getString(id));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * status Response from server
     */
    private void setStatusCheck(Bundle message) {
        try {
            String response = message.getString(FPSDBConstants.RESPONSE_DATA);
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            DeviceStatusRequest deviceRegistrationResponse = gson.fromJson(response,
                    DeviceStatusRequest.class);
            Log.i("Resp", response);
            if (deviceRegistrationResponse.isActive() && deviceRegistrationResponse.isAssociated()) {
                Util.storePreferenceApproved(this);
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            } else {
                Util.messageBar(RegistrationActivity.this, getString(R.string.deviceRegistration));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * onclick event for login button
     */
    public void registrationStatus(View view) {
        try {
            AndroidDeviceProperties deviceProperties = new AndroidDeviceProperties(this);
            DeviceStatusRequest deviceRegister = new DeviceStatusRequest();
            deviceRegister.setDeviceNumber(deviceProperties.getDeviceProperties().getSerialNumber());
            String device = new Gson().toJson(deviceRegister);
            StringEntity se = new StringEntity(device, HTTP.UTF_8);
            String url = "/device/getStatus";
            httpConnection.sendRequest(url, null, ServiceListenerType.DEVICE_STATUS,
                    SyncHandler, RequestType.POST, se, this);
        } catch (Exception e) {
            Log.e("RegistrationActivity", e.toString(), e);
        }
    }


}
