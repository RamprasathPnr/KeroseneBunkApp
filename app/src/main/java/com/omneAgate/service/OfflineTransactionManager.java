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
import com.omneAgate.DTO.TransactionBaseDto;
import com.omneAgate.DTO.TransactionTypes;
import com.omneAgate.DTO.UpdateStockRequestDto;
import com.omneAgate.DTO.UpdateStockResponseDto;
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
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Offline data sync to server service
 */
public class OfflineTransactionManager extends Service {

    List<BillDto> billList; //list of bills in offline

    Timer billSyncTimer;

    BillSyncTimerTask billSyncTimerTask;

    private String serverUrl = "";

    @Override
    public void onCreate() {
        super.onCreate();
        billSyncTimer = new Timer();
        billSyncTimerTask = new BillSyncTimerTask();
        Util.LoggingQueue(OfflineTransactionManager.this, "Info", "Service OfflineTransactionManager created ");
        serverUrl = FPSDBHelper.getInstance(this).getMasterData("serverUrl");
        Long timerWaitTime = Long.parseLong(getString(R.string.serviceTimeout));
        Long timerWaitTime2 = 3 * 30 * 1000l;
        billSyncTimer.schedule(billSyncTimerTask, 0, timerWaitTime2);
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
    private void syncBillsToServer(BillDto bill) {
        Log.e("Bills from back",""+ bill);
        TransactionBaseDto base = new TransactionBaseDto();
        base.setTransactionType(TransactionTypes.BUNK_SALE_QR_OTP_DISABLED);
        bill.setMode('F');
        bill.setChannel('G');
        bill.setBunkId(SessionId.getInstance().getFpsId());
        UpdateStockRequestDto updateStock = new UpdateStockRequestDto();
        updateStock.setReferenceId(bill.getTransactionId());
        updateStock.setDeviceId(Settings.Secure.getString(
                OfflineTransactionManager.this.getContentResolver(), Settings.Secure.ANDROID_ID).toUpperCase());
        updateStock.setUfc(bill.getUfc());
        updateStock.setBillDto(bill);
        base.setBaseDto(updateStock);
        base.setType("com.omneagate.rest.dto.QRRequestDto");
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
                Util.LoggingQueue(OfflineTransactionManager.this, "Info", "Starting up");
                billList = new ArrayList<>();
                billList = FPSDBHelper.getInstance(OfflineTransactionManager.this).getAllBillsForSync();
               // FPSDBHelper.getInstance(OfflineTransactionManager.this).stockAdjustmentData();
                Log.i("billList size", "bills:" + billList.size());
                Util.LoggingQueue(OfflineTransactionManager.this, "Info", "Retrieved number of unsent bills [" + billList.size() + "]");
                NetworkConnection network = new NetworkConnection(OfflineTransactionManager.this);
                if (network.isNetworkAvailable()) {
                    UpdateAdjustment updates = new UpdateAdjustment(OfflineTransactionManager.this,serverUrl);
                    updates.updateStockAdjustment();
                    for (BillDto bill : billList) {
                        Util.LoggingQueue(OfflineTransactionManager.this, "Info", "Processing bill id" + bill.getTransactionId());
                        syncBillsToServer(bill);
                    }
                }
                Util.LoggingQueue(OfflineTransactionManager.this, "Info", "Waking up");
            } catch (Exception e) {
                Util.LoggingQueue(OfflineTransactionManager.this, "Error", e.getMessage());
                Log.e("Offline Trans", e.toString(), e);
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
                String url = serverUrl + "/bunktransaction/process";
                URI website = new URI(url);
                Log.e("Bills", billData[0].toString());
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
                Util.LoggingQueue(OfflineTransactionManager.this, "Error", "Network exception" + e.getMessage());
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
                    Log.e("offline Transaction","< ====  offline Transaaction started ==== >");
                    Log.e("offline Transaction","Response for offline bills :"+response);
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    Gson gson = gsonBuilder.create();
                    UpdateStockResponseDto updateStock = gson.fromJson(response, UpdateStockResponseDto.class);
                    if (updateStock != null && updateStock.getStatusCode() == 0 && updateStock.getBillDto() != null) {
                        FPSDBHelper.getInstance(OfflineTransactionManager.this).billUpdate(updateStock.getBillDto());
                        Util.LoggingQueue(OfflineTransactionManager.this, "Info", "Updating status of bill to status T for bill id " + updateStock.getBillDto().getTransactionId());
                    } else if (updateStock != null && updateStock.getStatusCode() == 5085) {
                        FPSDBHelper.getInstance(OfflineTransactionManager.this).billUpdate(updateStock.getBillDto());
                        Util.LoggingQueue(OfflineTransactionManager.this, "Info", "Updating status of bill to status T for bill id " + updateStock.getBillDto().getTransactionId());
                    } else {
                        Util.LoggingQueue(OfflineTransactionManager.this, "Error", "Received null response ");
                    }
                    Log.e("offline Transaction","< ====  offline Transaaction ended ==== >");
                }
            } catch (Exception e) {
                Log.e("Insert Error", e.toString(), e);
            }
        }
    }

}
