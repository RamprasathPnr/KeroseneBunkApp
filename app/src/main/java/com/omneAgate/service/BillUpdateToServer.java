package com.omneAgate.service;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.omneAgate.DTO.TransactionBaseDto;
import com.omneAgate.DTO.UpdateStockResponseDto;
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
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;

/**
 * Used to send bill to server in background
 */
public class BillUpdateToServer {

    private final Context context; //Context for activity

    //    Constructor
    public BillUpdateToServer(Context context) {
        this.context = context;
    }

    /*Bill sending to server*/
    public void sendBillToServer(TransactionBaseDto bill) {
        NetworkConnection network = new NetworkConnection(context);
        if (network.isNetworkAvailable()) {
            new BillUpdateToServerAsync().execute(bill);
        } else {
            Util.LoggingQueue(context, "Error", "Network not available to send bill");
        }
    }

    //Async task to send bill to server
    class BillUpdateToServerAsync extends AsyncTask<TransactionBaseDto, String, String> {

        @Override
        protected String doInBackground(TransactionBaseDto... billData) {
            BufferedReader in = null;
            try {
                Log.e("serverurl","--->"+FPSDBHelper.getInstance(context).getMasterData("serverUrl"));
                String serverUrl = FPSDBHelper.getInstance(context).getMasterData("serverUrl");
                String url = serverUrl + "/bunktransaction/process";
                URI website = new URI(url);
                String bill = new Gson().toJson(billData[0]);
                StringEntity entity = new StringEntity(bill, HTTP.UTF_8);
                Log.e("Bill Dto",bill);
                Util.LoggingQueue(context, "Info", "Sending request bill" + billData[0]);
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
                Log.e("Response Bill:", responseData);
                Util.LoggingQueue(context, "Info", "Received response " + responseData);
                return responseData;
            } catch (Exception e) {
                Util.LoggingQueue(context, "Error", "Network exception" + e.getMessage());
                Log.e("Error in bill update: ", e.toString());
                try {
                    if (in != null)
                        in.close();
                } catch (Exception e1) {
                    // Intentional swallow of exception
                }
            }
            return null;
        }

        /*return http GET,POST and PUT method using parameters*/
        private HttpResponse requestType(URI website, StringEntity entity) throws IOException {
            HttpParams httpParameters = new BasicHttpParams();
            int timeoutConnection = 50000;
            HttpConnectionParams.setConnectionTimeout(httpParameters,
                    timeoutConnection);
            int timeoutSocket = 50000;
            Log.i("entity", EntityUtils.toString(entity));
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


        @Override
        protected void onPostExecute(String response) {
            try {
                if (response != null) {
                    Log.e("Response Bill:", response);
                    Log.e("force Transaction","< ====  force Transaaction started ==== >");
                    Log.e("force Transaction","Response for force bills :"+response);
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    Gson gson = gsonBuilder.create();
                    UpdateStockResponseDto updateStock = gson.fromJson(response, UpdateStockResponseDto.class);
                    Util.LoggingQueue(context, "Bill success", response);
                    if (updateStock != null && updateStock.getStatusCode() == 0 && updateStock.getBillDto() != null) {
                        FPSDBHelper.getInstance(context).billUpdate(updateStock.getBillDto());
                    }
                    else if(updateStock != null && updateStock.getStatusCode()== 5085){
                        FPSDBHelper.getInstance(context).billUpdate(updateStock.getBillDto());
                    }else{
                        Log.e("BillUpdateTo server","Error occured in BillUpdate");
                    }
                    Log.e("force Transaction","< ====  force Transaaction ended ==== >");
                }
            } catch (Exception e) {
                Log.e("Bill update to server", e.toString(), e);
            }

        }
    }
}