package com.omneAgate.service;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.provider.Settings;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;

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
import com.omneAgate.Bunker.BillSearchActivity;
import com.omneAgate.Bunker.R;
import com.omneAgate.Bunker.dialog.CloseSaleDialog;

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

/**
 * Created for BillSyncManually.
 */
public class BillSyncManually {

    private static final String TAG=BillSyncManually.class.getCanonicalName();

    List<BillDto> billList; //list of bills in offline

    Activity context;

    private String serverUrl = "";

    int billCount = 0;

    Dialog dialog;

    int sentCount = 1;

    public BillSyncManually(Activity context) {
        this.context = context;
    }

    public void billSync() {
        serverUrl = FPSDBHelper.getInstance(context).getMasterData("serverUrl");
        billList = new ArrayList<>();
        billList = FPSDBHelper.getInstance(context).getAllBillsForSync();
        billCount = billList.size();
        Log.e(TAG,"BILL SYNC Execution "+billCount);
        showDialog();
        billData();
    }


    private void showDialog() {
        dialog = new Dialog(context, android.R.style.Theme_Translucent);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //here we set layout of progress dialog
        dialog.setContentView(R.layout.dialog_waiting);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        String textData = sentCount+" / "+ billCount +" "+ context.getString(R.string.billSyncing);
        ((TextView)dialog.findViewById(R.id.billText)).setText(textData);
        dialog.show();
    }

    private void billData() {
        if (billList.size() > 0) {
            Log.e(TAG,"BILL SYNC Execution in");
            syncBillsToServer(billList.get(0));
            if (dialog != null && dialog.isShowing()) {
                String textData = sentCount + " / " + billCount + " " + context.getString(R.string.billSyncing);
                ((TextView) dialog.findViewById(R.id.billText)).setText(textData);
            }
        } else {
            Log.e(TAG,"BILL SYNC Execution out");
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
            if (context.getLocalClassName().contains("TransactionCommodityActivity")) {
                new CloseSaleDialog(context).show();

            } else {
                context.startActivity(new Intent(context, BillSearchActivity.class));
                context.finish();
            }
        }
    }

    /**
     * Check bill size
     * <p/>
     * Async task to call bills
     */
    private void syncBillsToServer(BillDto bill) {
        Log.e(TAG, "BILL SYNC Execution");
        Log.e("Bills from back", bill.toString());
        TransactionBaseDto base = new TransactionBaseDto();
        base.setTransactionType(TransactionTypes.BUNK_SALE_QR_OTP_DISABLED);
        NetworkConnection network = new NetworkConnection(context);
        if (network.isNetworkAvailable()) {
            bill.setMode('D');
            bill.setChannel('G');
        }
        else {
            bill.setMode('F');
            bill.setChannel('G');
        }
        UpdateStockRequestDto updateStock = new UpdateStockRequestDto();
        updateStock.setReferenceId(bill.getTransactionId());
        updateStock.setDeviceId(Settings.Secure.getString(
                context.getContentResolver(), Settings.Secure.ANDROID_ID).toUpperCase());
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

    /**
     * Async   task for connection heartbeat
     */
    private class OfflineDataSyncTask extends AsyncTask<TransactionBaseDto, String, String> {

        @Override
        protected String doInBackground(TransactionBaseDto... billData) {
            BufferedReader in = null;
            try {
                Log.e(TAG,"BILL SYNC Execution 1 ");
                String url = serverUrl + "/bunktransaction/process";
                URI website = new URI(url);
                Log.i("Bills", billData[0].toString());
                Log.e(TAG, "BILL SYNC Execution 2 " +billData[0].toString());
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
                Util.LoggingQueue(context, "Error", "Network exception" + e.getMessage());
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
                    Log.e("Response force sync",""+response.toString());
                    Log.e(TAG,"BILL SYNC Execution 3 ");
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    Gson gson = gsonBuilder.create();
                    UpdateStockResponseDto updateStock = gson.fromJson(response, UpdateStockResponseDto.class);
                    if (updateStock != null && updateStock.getStatusCode() == 0 && updateStock.getBillDto() != null) {
                        FPSDBHelper.getInstance(context).billUpdate(updateStock.getBillDto());
                        Util.LoggingQueue(context, "Info", "Updating status of bill to status T for bill id " + updateStock.getBillDto().getTransactionId());
                    } else if (updateStock != null && updateStock.getStatusCode() == 5085) {
                        FPSDBHelper.getInstance(context).billUpdate(updateStock.getBillDto());
                        Util.LoggingQueue(context, "Info", "Updating status of bill to status T for bill id " + updateStock.getBillDto().getTransactionId());
                    } else {
                        Util.LoggingQueue(context, "Error", "Received null response ");
                    }
                }
            } catch (Exception e) {
                Log.e("Insert Error", e.toString(), e);
            } finally {
                sentCount++;
                billList.remove(0);
                billData();
            }
        }
    }

}
