package com.omneAgate.service;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.omneAgate.Bunker.R;
import com.omneAgate.DTO.ApplicationType;
import com.omneAgate.DTO.CloseSaleTransactionDto;
import com.omneAgate.Util.FPSDBHelper;
import com.omneAgate.Util.NetworkConnection;
import com.omneAgate.Util.SessionId;
import com.omneAgate.Util.Util;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ramprasath on 19/5/16.
 */
public class OfflineCloseSaleManager extends Service {

    List<CloseSaleTransactionDto> closeSaleList; //list of bills in offline

    Timer billSyncTimer;

    BillSyncTimerTask billSyncTimerTask;

    private String serverUrl = "";

    @Override
    public void onCreate() {
        super.onCreate();
        billSyncTimer = new Timer();
        billSyncTimerTask = new BillSyncTimerTask();
        Log.e("Offline manager","offline closesale manager is called");
        serverUrl = FPSDBHelper.getInstance(this).getMasterData("serverUrl");
        Long timerWaitTime = Long.parseLong(getString(R.string.serviceTimeout));
        billSyncTimer.schedule(billSyncTimerTask, 0, timerWaitTime);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onDestroy() {
        try {
            if (billSyncTimer != null) {
                billSyncTimer.cancel();
                billSyncTimer = null;
            }
        } catch (Exception e) {
            Log.e("Offline transaction", "Error in Service", e);
        }

    }

    /**
     * Check bill size
     * <p/>
     * Async task to call bills
     */
    private void syncBillsToServer(CloseSaleTransactionDto bill) {
        bill.setStatusCode(2000);
        bill.setAppType(ApplicationType.KEROSENE_BUNK);
        Util.LoggingQueue(OfflineCloseSaleManager.this, "Info", "Processing close sale " + bill);
        bill.setDeviceId(Settings.Secure.getString(
                OfflineCloseSaleManager.this.getContentResolver(), Settings.Secure.ANDROID_ID).toUpperCase());
        new OfflineDataSyncTask().execute(bill);

    }

    /**
     * return http POST method using parameters
     */
    private HttpResponse requestType(URI website, StringEntity entity) throws IOException {

        HttpParams httpParameters = new BasicHttpParams();
        int timeoutConnection = 15000;
        HttpConnectionParams.setConnectionTimeout(httpParameters,
                timeoutConnection);
        int timeoutSocket = 15000;
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

    class BillSyncTimerTask extends TimerTask {

        @Override
        public void run() {
            try {
                Util.LoggingQueue(OfflineCloseSaleManager.this, "Info", "Starting up");
                closeSaleList = new ArrayList<>();
                closeSaleList = FPSDBHelper.getInstance(OfflineCloseSaleManager.this).getAllCloseSaleForSync();
                Log.e("billList size", "bills:" + closeSaleList.size());
                Log.e("Offline manager","Retrieved number of close sale bills [" + closeSaleList.size() + "]");
                NetworkConnection network = new NetworkConnection(OfflineCloseSaleManager.this);
                if (network.isNetworkAvailable()) {
                    for (CloseSaleTransactionDto bill : closeSaleList) {
                        syncBillsToServer(bill);
                        Util.LoggingQueue(OfflineCloseSaleManager.this, "Info", "Processing close sale id" + bill);
                    }
                }
                Util.LoggingQueue(OfflineCloseSaleManager.this, "Info", "Waking up");
            } catch (Exception e) {
                Log.e("Offline Trans", e.toString(), e);
            }
        }

    }

    /**
     * Async   task for connection heartbeat
     */
    private class OfflineDataSyncTask extends AsyncTask<CloseSaleTransactionDto, String, String> {

        @Override
        protected String doInBackground(CloseSaleTransactionDto... billData) {
            BufferedReader in = null;
            try {
                String url = serverUrl + "/bunk/closeofsale/save";
                URI website = new URI(url);
                Log.e("Bills", billData[0].toString());
                String bill = new Gson().toJson(billData[0]);
                Log.e("close sales", bill);
                StringEntity entity = new StringEntity(bill, HTTP.UTF_8);
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
                return  sb.toString();
            } catch (Exception e) {
                Util.LoggingQueue(OfflineCloseSaleManager.this, "Error", "Network exception" + e.getMessage());
                Log.e("Error in connection: ", e.toString());
                try {
                    if (in != null)
                        in.close();
                } catch (Exception e1) {
                    // Intentional swallow of exception
                }
            }
            return null;
        }


        @Override
        protected void onPostExecute(String response) {
            try {
                if (response != null) {
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    Gson gson = gsonBuilder.create();
                    Log.e("OfflineCloseSale res",""+response);
                    CloseSaleTransactionDto closeSaleTransactionDto = gson.fromJson(response, CloseSaleTransactionDto.class);
                    if (closeSaleTransactionDto != null && (closeSaleTransactionDto.getStatusCode() == 0 || closeSaleTransactionDto.getStatusCode() == 7001)) {
                        FPSDBHelper.getInstance(OfflineCloseSaleManager.this).updateCloseDate(closeSaleTransactionDto);
                        Log.e("OfflineCloseSale","Updating status of bill to status T for bill id " + closeSaleTransactionDto.getTransactionId());
                    } else {
                        Util.LoggingQueue(OfflineCloseSaleManager.this, "Error", "Received null response ");
                        Log.e("OfflineCloseSale","Received null response");
                    }
                }
            } catch (Exception e) {
                Log.e("Insert Error", e.toString(), e);
            }
        }
    }

}

