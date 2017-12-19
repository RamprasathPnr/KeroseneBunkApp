package com.omneAgate.service;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.omneAgate.DTO.GodownStockOutwardDto;
import com.omneAgate.DTO.StockReqBaseDto;
import com.omneAgate.DTO.StockRequestDto;
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
 * Offline data sync to server service
 */
public class OfflineInwardManager extends Service {

    List<StockRequestDto> stockRequestDtoList; //list of bills in offline

    Timer syncTimer;

    SyncTimerTask syncTimerTask;

    private String serverUrl = "";

    @Override
    public void onCreate() {
        super.onCreate();
        syncTimer = new Timer();
        syncTimerTask = new SyncTimerTask();
        serverUrl = FPSDBHelper.getInstance(this).getMasterData("serverUrl");
        Util.LoggingQueue(OfflineInwardManager.this, "Info", "Service OfflineTransactionInward created ");
        Long timerWaitTime = 15 * 60 * 1000l;
        syncTimer.schedule(syncTimerTask, 0, timerWaitTime);

    }

    @Override
    public void onDestroy() {
        try {
            if (syncTimer != null) {
                syncTimer.cancel();
                syncTimer = null;
            }
        } catch (Exception e) {
            Log.e("Inward excep", "Error in Service", e);
        }

    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    /**
     * Check bill size
     * <p/>
     * Async task to call bills
     */
    private void syncBillsToServer(StockRequestDto stockRequestDto) {
        StockReqBaseDto stockReqBaseDto = new StockReqBaseDto();
        stockReqBaseDto.setType("com.omneagate.rest.dto.StockRequestDto");
        stockReqBaseDto.setBaseDto(stockRequestDto);
        new OfflineDataSyncTask().execute(stockReqBaseDto);

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

    class SyncTimerTask extends TimerTask {

        @Override
        public void run() {
            try {
                Util.LoggingQueue(OfflineInwardManager.this, "Info", "Starting up");
                stockRequestDtoList = new ArrayList<>();
                stockRequestDtoList = FPSDBHelper.getInstance(OfflineInwardManager.this).getAllStockSync();
                Log.e("DataSRequest", "bills:" + stockRequestDtoList.size());
                Util.LoggingQueue(OfflineInwardManager.this, "Info", "Retrieved number of unsent StockRequest [" + stockRequestDtoList.size() + "]");

                NetworkConnection network = new NetworkConnection(OfflineInwardManager.this);
                if (network.isNetworkAvailable()) {
                    for (StockRequestDto stockRequest : stockRequestDtoList) {
                        syncBillsToServer(stockRequest);
                        Util.LoggingQueue(OfflineInwardManager.this, "Info", "Processing bill id " + stockRequest.getDate());
                    }
                }
            } catch (Exception e) {
                Log.e("Offline Inward",e.toString(),e);
                Util.LoggingQueue(OfflineInwardManager.this, "Error", e.getMessage());
            }
        }

    }

    //Async   task for connection offline Inward
    private class OfflineDataSyncTask extends AsyncTask<StockReqBaseDto, String, String> {

        @Override
        protected String doInBackground(StockReqBaseDto... billData) {
            BufferedReader in = null;
            try {
                String url = serverUrl + "/bunkstock/inward";
                URI website = new URI(url);
                Log.e("Stocks ", billData[0].toString());
                String stocks = new Gson().toJson(billData[0]);
                StringEntity entity = new StringEntity(stocks, HTTP.UTF_8);
                HttpResponse response = requestType(website, entity);
                in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
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
                Util.LoggingQueue(OfflineInwardManager.this, "Error", "Network exception" + e.getMessage());
                Log.e("Inward excep: ", e.toString());
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
                Log.e("Srock Req Dto",response);
                StockRequestDto stockRequestDto = gson.fromJson(response, StockRequestDto.class);
                Log.e("Srock Req Dto",stockRequestDto.toString());
                if (stockRequestDto != null && stockRequestDto.getStatusCode() == 0 && stockRequestDto.getReferenceNo() != null) {
                    FPSDBHelper.getInstance(OfflineInwardManager.this).updateStockInward(stockRequestDto.getReferenceNo());
                    Util.LoggingQueue(OfflineInwardManager.this, "Info", "Updating status of Manual Inward to status T for inwardKey " + stockRequestDto.getReferenceNo());
                } else {
                    Util.LoggingQueue(OfflineInwardManager.this, "Error", "Received null response ");
                }
            }

        }
    }
}
