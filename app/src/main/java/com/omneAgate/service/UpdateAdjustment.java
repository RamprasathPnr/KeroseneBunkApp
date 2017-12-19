package com.omneAgate.service;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.omneAgate.DTO.POSStockAdjustmentDto;
import com.omneAgate.Util.FPSDBHelper;
import com.omneAgate.Util.SessionId;

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
import java.util.List;

/**
 * Created for Update Adjustment
 */
public class UpdateAdjustment {

    Context context;

    String serverUrl;

    public UpdateAdjustment(Context context, String serverUrl) {
        this.context = context;
        this.serverUrl = serverUrl;

    }

    public void updateStockAdjustment() {
        List<POSStockAdjustmentDto> posAcknowledge = FPSDBHelper.getInstance(context).stockAdjustmentDataToServer();
        Log.e("Bills from back", "Update:" + posAcknowledge.toString());
        for (POSStockAdjustmentDto acknowledgedData : posAcknowledge) {
            syncBillsToServer(acknowledgedData);
        }
    }

    /**
     * Check bill size
     * <p/>
     * Async task to call bills
     */
    private void syncBillsToServer(POSStockAdjustmentDto bill) {
        Log.e("Bills from back", "Update:" + bill.toString());
        new OfflineDataSyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, bill);
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

    /**
     * Async   task for connection heartbeat
     */
    private class OfflineDataSyncTask extends AsyncTask<POSStockAdjustmentDto, String, String> {

        @Override
        protected String doInBackground(POSStockAdjustmentDto... billData) {
            BufferedReader in = null;
            try {
                String url = serverUrl + "/posstockadjustment/kerosenebunk/acknowledge";
                URI website = new URI(url);
                Log.i("Bills", billData[0].toString());
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
                return sb.toString();
            } catch (Exception e) {
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
                    Log.e("Bills from back", "Update:" + response);
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    Gson gson = gsonBuilder.create();
                    POSStockAdjustmentDto posData = gson.fromJson(response, POSStockAdjustmentDto.class);
                    if (posData.getStatusCode() == 0 || posData.getStatusCode() == 12008 || posData.getStatusCode() == 12009) {
                        FPSDBHelper.getInstance(context).adjustmentUpdate(posData);
                    }
                }
            } catch (Exception e) {
                Log.e("Insert Error", e.toString(), e);
            }
        }
    }
}
