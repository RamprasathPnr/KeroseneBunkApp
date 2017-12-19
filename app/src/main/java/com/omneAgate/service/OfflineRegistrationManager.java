package com.omneAgate.service;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.omneAgate.DTO.BillDto;
import com.omneAgate.DTO.OfflineActivationSynchDto;
import com.omneAgate.DTO.TransactionBaseDto;
import com.omneAgate.DTO.TransactionTypes;
import com.omneAgate.Util.FPSDBHelper;
import com.omneAgate.Util.NetworkConnection;
import com.omneAgate.Util.SessionId;
import com.omneAgate.Util.Util;
import com.omneAgate.Bunker.R;

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
import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Offline data sync to server service
 */
public class OfflineRegistrationManager extends Service {

    List<OfflineActivationSynchDto> offlineActivationList; //list of bills in offline
    Timer billSyncTimer;
    BillSyncTimerTask billSyncTimerTask;
    private String serverUrl = "";

    @Override
    public void onCreate() {
        super.onCreate();
        billSyncTimer = new Timer();
        billSyncTimerTask = new BillSyncTimerTask();
        Util.LoggingQueue(OfflineRegistrationManager.this, "Info", "Service OfflineTransactionManager created ");
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
            Log.e("offlineActivationList", "Error in Service", e);
        }

    }

    /**
     * Check bill size
     * <p/>
     * Async task to call bills
     */
    private void syncBillsToServer(OfflineActivationSynchDto activateSync) {
        TransactionBaseDto base = new TransactionBaseDto();
        base.setTransactionType(TransactionTypes.OFFLINE_BENEFACTIV_DATASYNCH);
        activateSync.setDeviceNum(Settings.Secure.getString(
                OfflineRegistrationManager.this.getContentResolver(), Settings.Secure.ANDROID_ID).toUpperCase());

        base.setBaseDto(activateSync);
        base.setType("com.omneagate.rest.dto.OfflineActivationSynchDto");
        Log.i("Offline Registration", activateSync.toString());
        new OfflineDataSyncTask().execute(base);

    }

    /**
     * return http POST method using parameters
     */
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

    class BillSyncTimerTask extends TimerTask {

        @Override
        public void run() {
            try {
                Util.LoggingQueue(OfflineRegistrationManager.this, "Info", "Starting up");
                offlineActivationList = new ArrayList<OfflineActivationSynchDto>();
                offlineActivationList = FPSDBHelper.getInstance(OfflineRegistrationManager.this).getAllRegistrationForSync();
                Log.i("offlineActivationList", "offlineActivationList:" + offlineActivationList.size());
                Util.LoggingQueue(OfflineRegistrationManager.this, "Info", "Retrieved number of unsent Registration [" + offlineActivationList.size() + "]");
                NetworkConnection network = new NetworkConnection(OfflineRegistrationManager.this);
                if (network.isNetworkAvailable()) {
                    for (OfflineActivationSynchDto bill : offlineActivationList) {
                        Util.LoggingQueue(OfflineRegistrationManager.this, "Info", "Processing bill id" + bill.getRationCardNumber());
                        syncBillsToServer(bill);
                    }
                }
                Util.LoggingQueue(OfflineRegistrationManager.this, "Info", "Waking up");
            } catch (Exception e) {
                Util.LoggingQueue(OfflineRegistrationManager.this, "Error", e.getMessage());
            }
        }

    }

    /**
     * Async   task for connection heartbeat
     */
    private class OfflineDataSyncTask extends AsyncTask<TransactionBaseDto, String, String> {

        @Override
        protected String doInBackground(TransactionBaseDto... billData) {
            BufferedReader in = null;
            try {
                String url = serverUrl + "/transaction/process";
                URI website = new URI(url);
                Log.e("offline activation", billData[0].toString());
                String bill = new Gson().toJson(billData[0]);
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
                String responseData = sb.toString();
                return responseData;
            } catch (Exception e) {
                Util.LoggingQueue(OfflineRegistrationManager.this, "Error", "Network exception" + e.getMessage());
                Log.e("offline connection exp ", e.toString(), e);
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
            if (response != null) {
                GsonBuilder gsonBuilder = new GsonBuilder();
                Gson gson = gsonBuilder.create();
                Log.e("Offline Registration", response);
                OfflineActivationSynchDto updateStock = gson.fromJson(response, OfflineActivationSynchDto.class);
                if (updateStock != null && updateStock.getStatusCode() == 0 && updateStock.getBillDtos() != null) {
                    FPSDBHelper.getInstance(OfflineRegistrationManager.this).insertBeneficiaryNew(updateStock.getBeneficairyDto());
                    FPSDBHelper.getInstance(OfflineRegistrationManager.this).insertBillData(new HashSet<BillDto>(updateStock.getBillDtos()));
                    FPSDBHelper.getInstance(OfflineRegistrationManager.this).updateCardRegistration(updateStock.getRationCardNumber());
                    Util.LoggingQueue(OfflineRegistrationManager.this, "Info", "Updating status of bill to status T for bill id " + updateStock.getBillDtos().toString());
                } else if (updateStock != null && updateStock.getStatusCode() == 5040) {
                    FPSDBHelper.getInstance(OfflineRegistrationManager.this).insertBeneficiaryNew(updateStock.getBeneficairyDto());
                    FPSDBHelper.getInstance(OfflineRegistrationManager.this).insertBillData(new HashSet<BillDto>(updateStock.getBillDtos()));
                    FPSDBHelper.getInstance(OfflineRegistrationManager.this).updateCardRegistration(updateStock.getRationCardNumber());
                    Util.LoggingQueue(OfflineRegistrationManager.this, "Info", "Updating status of bill to status T for bill id " + updateStock.getBillDtos().toString());
                } else {
                    Util.LoggingQueue(OfflineRegistrationManager.this, "Error", "Received null response ");
                }
            }
        }
    }

}
