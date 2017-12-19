package com.omneAgate.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.IBinder;
import android.util.Log;

import com.google.gson.Gson;
import com.omneAgate.DTO.HeartBeatDto;
import com.omneAgate.DTO.TransactionBaseDto;
import com.omneAgate.DTO.TransactionTypes;
import com.omneAgate.Util.FPSDBHelper;
import com.omneAgate.Util.GPSService;
import com.omneAgate.Util.LocationId;
import com.omneAgate.Util.NetworkConnection;
import com.omneAgate.Util.SessionId;
import com.omneAgate.Util.Util;
import com.omneAgate.Bunker.R;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Service for checking connection of server
 */
public class ConnectionHeartBeat extends Service {

    HeartBeatDto heartBeat;

    TransactionBaseDto base;

    Timer heartBeatTimer;

    HeartBeatTimerTask heartBeatTimerTask;

    GPSService mGPSService;

    private int batteryLevel = 0; //Battery level variable
    //Broadcast receiver for battery
    private final BroadcastReceiver batteryLevelReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            context.unregisterReceiver(this);
            int currentLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            if (currentLevel >= 0 && scale > 0) {
                batteryLevel = (currentLevel * 100) / scale;
            }
        }
    };
    private String serverUrl = "";

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryLevelReceiver, batteryLevelFilter);
        heartBeat = new HeartBeatDto();

        base = new TransactionBaseDto();
        serverUrl = FPSDBHelper.getInstance(this).getMasterData("serverUrl");
        heartBeatTimer = new Timer();
        heartBeatTimerTask = new HeartBeatTimerTask();
        mGPSService = new GPSService(ConnectionHeartBeat.this);
        mGPSService.getLocation();
        Util.LoggingQueue(this, "Info", "Service  HeartBeat created");
        Long timerWaitTime = Long.parseLong(getString(R.string.serviceTimeout));
        heartBeatTimer.schedule(heartBeatTimerTask, 1000, timerWaitTime);
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            heartBeat.setVersionNum(pInfo.versionCode);
        } catch (Exception e) {

        }

    }


    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onDestroy() {
        try {
            if (heartBeatTimer != null) {
                heartBeatTimer.cancel();
                heartBeatTimer = null;
                mGPSService.closeGPS();
            }
        } catch (Exception e) {
            Log.e("Heart beat exception", "Error in Service", e);
        }

    }


    /*return http POST method using parameters*/
    private HttpResponse requestType(URI website, StringEntity entity) throws IOException {

        HttpParams httpParameters = new BasicHttpParams();
        int timeoutConnection = 50000;
        HttpConnectionParams.setConnectionTimeout(httpParameters,
                timeoutConnection);
        int timeoutSocket = 50000;
        HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
        HttpClient client = new DefaultHttpClient(httpParameters);
        HttpPost postRequest = new HttpPost();
        postRequest.setURI(website);
        postRequest.setHeader("Content-Type", "application/json");
        postRequest.setHeader("Store_type", "fps");
        postRequest.setHeader("Cookie", "JSESSIONID=" + SessionId.getInstance().getSessionId());
        postRequest.setHeader("Cookie", "SESSION=" + SessionId.getInstance().getSessionId());
        postRequest.setEntity(entity);
        return client.execute(postRequest);
    }

    class HeartBeatTimerTask extends TimerTask {

        @Override
        public void run() {
            try {
                Util.LoggingQueue(ConnectionHeartBeat.this, "Info", "Started to send HeartBeat message ");
                if (StringUtils.isNotEmpty(SessionId.getInstance().getSessionId()) && SessionId.getInstance().getSessionId().length() > 0) {


                    if (mGPSService.isLocationAvailable) {
                        double latitude = mGPSService.getLatitude();
                        double longitude = mGPSService.getLongitude();
                        LocationId.getInstance().setLatitude(String.valueOf(latitude));
                        LocationId.getInstance().setLongitude(String.valueOf(longitude));
                        if (heartBeat != null) {
                            heartBeat.setLatitude(String.valueOf(latitude));
                            heartBeat.setLongtitude(String.valueOf(longitude));
                        }

                    }

                    NetworkConnection network = new NetworkConnection(ConnectionHeartBeat.this);
                    if (network.isNetworkAvailable()) {
                        Log.e("heart", getString(R.string.thresholdLimit) + "::" + batteryLevel);
                        heartBeat.setBatteryLevel(batteryLevel);
                        new ConnectionTestHearBeat().execute("");
                    }
                } else {
                    Util.LoggingQueue(ConnectionHeartBeat.this, "Error", "Session is null or zero");
                }
                Util.LoggingQueue(ConnectionHeartBeat.this, "Info", "End of HeartBeat message ");
            } catch (Exception e) {
                Util.LoggingQueue(ConnectionHeartBeat.this, "Error", e.getMessage());
            }
        }

    }

    //Async   task for connection heartbeat
    private class ConnectionTestHearBeat extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... f_url) {
            BufferedReader in = null;
            try {
                String url = serverUrl + "/transaction/process";
                URI website = new URI(url);
                heartBeat.setFpsId(SessionId.getInstance().getFpsId() + "");
                base.setBaseDto(heartBeat);
                base.setType("com.omneagate.rest.dto.HeartBeatRequestDto");
                base.setTransactionType(TransactionTypes.HEART_BEAT);
                Util.LoggingQueue(ConnectionHeartBeat.this, "Info", "Request message " + base);
                String heartBeatData = new Gson().toJson(base);
                StringEntity entity = new StringEntity(heartBeatData, HTTP.UTF_8);
                HttpResponse response = requestType(website, entity);
                in = new BufferedReader(new InputStreamReader(response
                        .getEntity().getContent()));
                StringBuffer sb = new StringBuffer("");
                String l;
                String nl = System.getProperty("line.separator");
                while ((l = in.readLine()) != null) {
                    sb.append(l + nl);
                }
                in.close();
                String responseData = sb.toString();
                Util.LoggingQueue(ConnectionHeartBeat.this, "Info", "Response message " + responseData);
                return responseData;
            } catch (Exception e) {
                Util.LoggingQueue(ConnectionHeartBeat.this, "Error", "Network exception" + e.getMessage());
                Log.e("Error in HEartbeat: ", e.toString());
                try {
                    if (in != null)
                        in.close();
                } catch (Exception e1) {
                    // Intentional swallow of exception
                }
            }
            return null;
        }


        // Once Music File is downloaded
        @Override
        protected void onPostExecute(String file_url) {

        }
    }


}
