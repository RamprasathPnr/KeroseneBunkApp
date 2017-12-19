package com.omneAgate.Bunker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.omneAgate.Bunker.dialog.RetryDialog;
import com.omneAgate.Bunker.dialog.RetryFailedDialog;
import com.omneAgate.Bunker.dialog.RetryFailedMasterDataDialog;
import com.omneAgate.DTO.EnumDTO.ServiceListenerType;
import com.omneAgate.DTO.EnumDTO.TableNames;
import com.omneAgate.DTO.FirstSynchReqDto;
import com.omneAgate.DTO.FirstSynchResDto;
import com.omneAgate.DTO.FistSyncInputDto;
import com.omneAgate.DTO.KeroseneBunkStockHistoryDto;
import com.omneAgate.Util.FPSDBHelper;
import com.omneAgate.Util.SessionId;
import com.omneAgate.Util.SyncPageUpdate;
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
import java.util.Map;

public class SyncPageActivity extends BaseActivity {


    private static final String TAG=SyncPageActivity.class.getCanonicalName();

    ScrollView loadScroll; //Scroll bar instance

    StringEntity stringEntity;   //StringEntity for sending data

    LinearLayout layout; // Layout for textView insert

    ProgressBar progressBar;  //ProgressBar for loading

    int retryCount = 0;   //user retry count

    List<FistSyncInputDto> firstSync;  //FistSync items

    int totalProgress = 6;  //Progressbar item add

    String serverUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_page);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        layout = (LinearLayout) findViewById(R.id.info);
        loadScroll = (ScrollView) findViewById(R.id.scrollData);
        progressBar = (ProgressBar) findViewById(R.id.progressBar2);
       findViewById(R.id.syncContinue).setVisibility(View.INVISIBLE);
        firstTimeSyncDetails();
        Util.LoggingQueue(this, "Sync Page", "Starting up Sync");
    }

    /**
     * Send request to server
     * <p/>
     * for getting table details
     */
    public void firstTimeSyncDetails() {
        try {
            Log.e(TAG,"Sync Execution");
            FirstSynchReqDto fpsRequest = new FirstSynchReqDto();
            String deviceId = Settings.Secure.getString(
                    getContentResolver(), Settings.Secure.ANDROID_ID).toUpperCase();


            fpsRequest.setDeviceNum(deviceId);
            serverUrl = FPSDBHelper.getInstance(this).getMasterData("serverUrl");


            String updateData = new Gson().toJson(fpsRequest);



            Util.LoggingQueue(this, "Sync Page", "First Request:" + updateData);
            stringEntity = new StringEntity(updateData, HTTP.UTF_8);



            new UpdateSyncTask().execute("");

        } catch (Exception e) {
            Util.LoggingQueue(this, "Sync Page", "Error in sync:" + e.toString());
            Log.e("SyncPageActivity", e.toString(), e);
        }
    }


    /**
     * After response received from server successfully in android
     * Table details fetched in MAP
     * if tableDetails is empty or null user need to retry
     */
    private void processSyncResponseData(String response) {
        try {
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            FirstSynchResDto fpsDataDto = gson.fromJson(response, FirstSynchResDto.class);
            int statusCode = fpsDataDto.getStatusCode();
            if (statusCode == 0) {
                if ((fpsDataDto.getTableDetails() == null || fpsDataDto.getTableDetails().isEmpty())) {
                    errorInSync();
                    return;
                }
                syncTableDetails(fpsDataDto.getTableDetails());

            }
        } catch (Exception e) {
            Util.LoggingQueue(this, "Error", e.toString());
            Log.e("SyncPageActivity", e.toString(), e);
            errorInSync();
        }

    }

    /**
     * Used to get number of items in server table
     * TableDetails map is input
     * If masters count 0 user should retry
     */
    private void syncTableDetails(Map<String, Integer> tableDetails) {
        firstSync = new ArrayList<>();
        List<String> masterDataEmpty = new ArrayList<>();
        int count = tableDetails.get("TABLE_CARDTYPE");
        if (count == 0) {
            masterDataEmpty.add("Card Type");
        }
        firstSync.add(getInputDTO("TABLE_CARDTYPE", count, "Table Card type downloading", "Card type downloaded with ", TableNames.TABLE_CARDTYPE));
        count = tableDetails.get("TABLE_PRODUCT");
        if (count == 0) {
            masterDataEmpty.add("Product");
        }
        firstSync.add(getInputDTO("TABLE_PRODUCT", count, "Table Product downloading", "Product downloaded with ", TableNames.TABLE_PRODUCT));
        count = tableDetails.get("TABLE_BUNKSTOCK");
        if (count > 0)
            firstSync.add(getInputDTO("TABLE_BUNKSTOCK", count, "Table Stock downloading", "Stock downloaded with ", TableNames.TABLE_BUNKSTOCK));
        count = tableDetails.get("TABLE_SMSPROVIDER");
        if (count == 0) {
            masterDataEmpty.add("SMS provider");
        }
        firstSync.add(getInputDTO("TABLE_SMSPROVIDER", count, "Table SMS Provider downloading", "SMS Provider downloaded with ", TableNames.TABLE_SMSPROVIDER));

        count = tableDetails.get("TABLE_SPLRULES");
        if (count == 0) {
            masterDataEmpty.add("Special rules");
        }
        firstSync.add(getInputDTO("TABLE_SPLRULES", count, "Table Special Rules downloading", "Special rules downloaded with ", TableNames.TABLE_SPLRULES));
        count = tableDetails.get("TABLE_ENTITLEMENTMASTER");
        if (count == 0) {
            masterDataEmpty.add("Entitlement master");
        }
        firstSync.add(getInputDTO("TABLE_ENTITLEMENTMASTER", count, "Table Entitlement Rules downloading", "Entitlement Rules  downloaded with ", TableNames.TABLE_ENTITLEMENTMASTER));

        count = tableDetails.get("TABLE_PERSONBASEDRULE");
        if (count == 0) {
            masterDataEmpty.add("Person based rule");
        }
        firstSync.add(getInputDTO("TABLE_PERSONBASEDRULE", count, "Table Person Based Rules downloading", "Person Based Rules downloaded with ", TableNames.TABLE_PERSONBASEDRULE));

        count = tableDetails.get("TABLE_REGIONBASEDRULE");
        if (count == 0) {
            masterDataEmpty.add("region based rules");
        }
        if (masterDataEmpty.size() > 0) {
            new RetryFailedMasterDataDialog(this, masterDataEmpty).show();
            return;
        }
        firstSync.add(getInputDTO("TABLE_REGIONBASEDRULE", count, "Table Region Based Rules downloading", "Region Based Rules downloaded with", TableNames.TABLE_REGIONBASEDRULE));

        count = tableDetails.get("TABLE_PRODUCTGROUP");
        if (count > 0)
            firstSync.add(getInputDTO("TABLE_PRODUCTGROUP", count, "Table Product Group downloading", "Store downloaded with ", TableNames.TABLE_PRODUCTGROUP));
        count = tableDetails.get("TABLE_PRICEOVERRIDE");
        if (count > 0)
            firstSync.add(getInputDTO("TABLE_PRICEOVERRIDE", count, "Table Product Group downloading", "Store downloaded with ", TableNames.TABLE_PRICEOVERRIDE));

        count = tableDetails.get("TABLE_BENEFICIARY");
        if (count > 0)
            firstSync.add(getInputDTO("TABLE_BENEFICIARY", count, "Table Beneficiary downloading", "Beneficiary downloaded with", TableNames.TABLE_BENEFICIARY));

        count = tableDetails.get("TABLE_GODOWNSTKOUTWARD");
        if (count > 0)
            firstSync.add(getInputDTO("TABLE_GODOWNSTKOUTWARD", count, "Table Stock Inward downloading", "Stock Inward downloaded with", TableNames.TABLE_FPSSTOCKINWARD));
        count = tableDetails.get("TABLE_BILL");
        if (count > 0)
            firstSync.add(getInputDTO("TABLE_BILL", count, "Table Bill downloading", "Bill downloaded with", TableNames.TABLE_BILL));
        count = tableDetails.get("TABLE_STOCKADJUSTMENT");
        if (count > 0)
            firstSync.add(getInputDTO("TABLE_STOCKADJUSTMENT", count, "Table Stock Adjustment details downloading", "Stock Adjustment details downloaded with", TableNames.TABLE_STOCKADJUSTMENT));

        count = tableDetails.get("TABLE_GIVEITUPREQUEST");
        Log.e("SyncPageActivity","TABLE_GIVEITUPREQUEST : "+count);
        if(count > 0)
            firstSync.add(getInputDTO("TABLE_GIVEITUPREQUEST", count, "Table Give It Up details downloading", "Give It Up details downloaded with", TableNames.TABLE_GIVEITUPREQUEST));

       /* count = tableDetails.get("TABLE_FPSMIGRATION");
        if (count > 0)
            firstSync.add(getInputDTO("TABLE_FPSMIGRATION", count, "Table Migration data downloading", "Migration data downloaded with", TableNames.TABLE_FPSMIGRATION));
    */    /*count = tableDetails.get("TABLE_USERDETAIL");
        if (count > 0)
            firstSync.add(getInputDTO("TABLE_USERDETAIL", count, "Table User details downloading", "User details downloaded with", TableNames.TABLE_USERDETAIL));*/
        totalProgress = 100 / firstSync.size();
        FirstSynchReqDto fpsRequest = new FirstSynchReqDto();
        String deviceId = Settings.Secure.getString(
                getContentResolver(), Settings.Secure.ANDROID_ID).toUpperCase();
        fpsRequest.setDeviceNum(deviceId);
        fpsRequest.setTableName(firstSync.get(0).getTableName());
        setTableSyncCall(fpsRequest);
        setTextStrings(firstSync.get(0).getTextToDisplay() + "....");


    }

    /**
     * Request for datas by giving name of table to server
     * <p/>
     * input FirstSynchReqDto fpsRequest
     */

    private void setTableSyncCall(FirstSynchReqDto fpsRequest) {
        try {
            String updateData = new Gson().toJson(fpsRequest);
            Log.e("sync Page","Table wise sync called : "+updateData);
            stringEntity = new StringEntity(updateData, HTTP.UTF_8);
            new UpdateTablesTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
        } catch (Exception e) {
            Log.e("SyncPageActivity", e.toString(), e);
        }
    }

    /**
     * returns FistSyncInputDto of details of tables received from server
     */
    private FistSyncInputDto getInputDTO(String tableName, int count, String textToDisplay, String endText, TableNames names) {
        FistSyncInputDto inputDto = new FistSyncInputDto();
        inputDto.setTableName(tableName);
        inputDto.setCount(count);
        inputDto.setTableNames(names);
        inputDto.setTextToDisplay(textToDisplay);
        inputDto.setEndTextToDisplay(endText);
        inputDto.setDynamic(true);
        return inputDto;
    }

    /**
     * After sync success this method will call
     */
    private void firstSyncSuccess() {
        try {
            FirstSynchReqDto fpsRequest = new FirstSynchReqDto();
            String deviceId = Settings.Secure.getString(
                    getContentResolver(), Settings.Secure.ANDROID_ID).toUpperCase();
            fpsRequest.setDeviceNum(deviceId);
            String updateData = new Gson().toJson(fpsRequest);
            Util.LoggingQueue(this, "Sync Page", "Sync Success req:" + updateData);
            stringEntity = new StringEntity(updateData, HTTP.UTF_8);
            new SyncSuccess().execute("");

        } catch (Exception e) {
            Util.LoggingQueue(this, "Error", e.toString());
            Log.e("SyncPageActivity", e.toString(), e);
        }
    }

    private void firstSyncSuccessResponse(String response) {
        try {
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            FirstSynchResDto fpsDataDto = gson.fromJson(response, FirstSynchResDto.class);
            int statusCode = fpsDataDto.getStatusCode();
            if (statusCode == 0) {
                if ((fpsDataDto.getLastSyncTime() == null)) {
                    errorInSync();
                    return;
                }
                progressBar.setProgress(100);
                FPSDBHelper.getInstance(SyncPageActivity.this).updateMaserData("syncTime", fpsDataDto.getLastSyncTime());
                Button continueButton = (Button) findViewById(R.id.syncContinue);
                continueButton.setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.downloadCompleted)).setText("Download Completed......");
                ((TextView) findViewById(R.id.syncIndicator)).setText("Database Sync Completed........");
                continueButton.setBackgroundColor(Color.parseColor("#00b7be"));
                continueButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        syncSuccessCompletion();
                    }
                });
            }
        } catch (Exception e) {
            Util.LoggingQueue(this, "Sync Page", "Error in sync:" + e.getStackTrace().toString());
            errorInSync();
        }
    }

    private void syncSuccessCompletion() {
        SyncPageUpdate syncPage = new SyncPageUpdate(this);
        syncPage.setSync();
        startActivity(new Intent(this, AdminActivity.class));
        finish();
      /*  if (LoginData.getInstance().getLoginData().getUserDetailDto().getProfile().equalsIgnoreCase("ADMIN")) {
            startActivity(new Intent(this, AdminActivity.class));
            finish();
        } else {
            startService(new Intent(this, ConnectionHeartBeat.class));
            startService(new Intent(this, UpdateDataService.class));
            startService(new Intent(this, RemoteLoggingService.class));
            startService(new Intent(this, OfflineTransactionManager.class));
            startService(new Intent(this, OfflineInwardManager.class));
            startActivity(new Intent(SyncPageActivity.this, SaleActivity.class));
            finish();
        }*/
    }


    /**
     * Progress bar setting in activity
     */
    private void setDownloadedProgress() {
        int progress = progressBar.getProgress();
        progress = progress + totalProgress;
        progressBar.setProgress(progress);
    }

    /**
     * Response received from server
     */
    private void setTableResponse(String response) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        FirstSynchResDto fpsDataDto = gson.fromJson(response, FirstSynchResDto.class);
        if (fpsDataDto.getStatusCode() == 0) {
            insertIntoDatabase(fpsDataDto);
        } else {
            errorInSync();
        }
    }

    /**
     * Scrolling of received String
     */
    private void setTextStrings(String syncString) {
        TextView tv = new TextView(SyncPageActivity.this);
        tv.setText(syncString);
        tv.setTextColor(Color.parseColor("#5B5B5B"));
        tv.setTextSize(22);
        layout.addView(tv);
        loadScroll.post(new Runnable() {
            @Override
            public void run() {
                loadScroll.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    /**
     * Database insertion of received data
     */
    private void insertIntoDatabase(FirstSynchResDto firstSynchResDto) {
        FistSyncInputDto fistSyncInputDto = firstSync.get(0);

        setTextStrings(firstSync.get(0).getEndTextToDisplay() + " items " + firstSynchResDto.getTotalSentCount() + "....");
        switch (fistSyncInputDto.getTableNames()) {
            case TABLE_CARDTYPE:
                FPSDBHelper.getInstance(this).insertCardTypeData(firstSynchResDto.getCardtypeDto());
                break;
            case TABLE_BUNKSTOCK:
                FPSDBHelper.getInstance(this).insertFpsStockData(firstSynchResDto.getKeroseneBunkStockDtos());
                break;
            case TABLE_PRODUCT:
                FPSDBHelper.getInstance(this).insertProductData(firstSynchResDto.getProductDto());
                break;
            case TABLE_BILL:
                FPSDBHelper.getInstance(this).insertBillData(firstSynchResDto.getBillDto());
                break;
            case TABLE_BENEFICIARY:
                FPSDBHelper.getInstance(this).insertBeneficiaryData(firstSynchResDto.getBeneficiaryDto());
                break;
           /* case TABLE_BENEFREGREQ:
                FPSDBHelper.getInstance(this).insertRegistrationRequestData(firstSynchResDto.getBenefRegReqDto());
                break;*/
            case TABLE_REGIONBASEDRULE:
                FPSDBHelper.getInstance(this).insertRegionRules(firstSynchResDto.getRegionBasedRulesDto());
                break;
            case TABLE_PRICEOVERRIDE:
                FPSDBHelper.getInstance(this).insertProductPriceOverride(firstSynchResDto.getOverrideDto());
                break;
            case TABLE_PRODUCTGROUP:
                FPSDBHelper.getInstance(this).insertProductGroup(firstSynchResDto.getProductGroupDtos());
                break;
            case TABLE_FPSSTOCKINWARD:
                FPSDBHelper.getInstance(this).insertFpsStockInwardDetails(firstSynchResDto.getGodownStockOutwardDto());
                break;
            case TABLE_USERDETAIL:
                FPSDBHelper.getInstance(this).insertUserDetailData(firstSynchResDto.getUserdetailDto());
                break;
            case TABLE_ENTITLEMENTMASTER:
                FPSDBHelper.getInstance(this).insertMasterRules(firstSynchResDto.getEntitlementMasterRulesDto());
                break;
            case TABLE_SPLRULES:
                FPSDBHelper.getInstance(this).insertSpecialRules(firstSynchResDto.getSplEntitlementRulesDto());
                break;
            case TABLE_PERSONBASEDRULE:
                FPSDBHelper.getInstance(this).insertPersonRules(firstSynchResDto.getPersonBasedRulesDto());
                break;
            case TABLE_FPSMIGRATION:
                FPSDBHelper.getInstance(this).insertMigrations(firstSynchResDto.getFpsMigrationDtos());
                break;
            case TABLE_STOCKADJUSTMENT:
                 FPSDBHelper.getInstance(this).stockAdjustmentFirstSync(firstSynchResDto.getStockAdjusmentDtos());
                break;
            case TABLE_SERVICEPROVIDER:
                break;
            case TABLE_SMSPROVIDER:
                FPSDBHelper.getInstance(this).insertSmsProvider(firstSynchResDto.getSmsProviderDtos());
                break;
            case TABLE_GIVEITUPREQUEST:
                Log.e("First Sync Activity ","Give it Response : "+firstSynchResDto.getGiveItUpRequestDto());
                FPSDBHelper.getInstance(this).insert_giveitup(firstSynchResDto.getGiveItUpRequestDto());
                break;
            default:
                break;
        }

        afterDatabaseInsertion(firstSynchResDto);

    }

    /*
    * After database insertion by user master this function called
    * */
    private void afterDatabaseInsertion(FirstSynchResDto firstSynchResDto) {
        FirstSynchReqDto fpsRequest = new FirstSynchReqDto();
        String deviceId = Settings.Secure.getString(
                getContentResolver(), Settings.Secure.ANDROID_ID).toUpperCase();
        fpsRequest.setDeviceNum(deviceId);
        if (firstSynchResDto.isHasMore()) {
            fpsRequest.setTotalCount(firstSynchResDto.getTotalCount());
            fpsRequest.setTotalSentCount(firstSynchResDto.getTotalSentCount());
            fpsRequest.setCurrentCount(firstSynchResDto.getCurrentCount());
            fpsRequest.setTableName(firstSync.get(0).getTableName());
            setTableSyncCall(fpsRequest);
        } else {
            firstSync.remove(0);
            setDownloadedProgress();
            if (firstSync.size() > 0) {
                fpsRequest.setTableName(firstSync.get(0).getTableName());
                setTextStrings(firstSync.get(0).getTextToDisplay() + "....");
                setTableSyncCall(fpsRequest);
            } else {
                getOpeningStock();
                firstSyncSuccess();
            }
        }
    }

    /**
     * user logout
     */
    public void logOut() {
        FPSDBHelper.getInstance(this).closeConnection();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void errorInSync() {
        layout.removeAllViews();
        Util.LoggingQueue(this, "Sync Page", "Error in sync");
        progressBar.setProgress(0);
        retryCount++;
        if (retryCount >= 3) {
            new RetryFailedDialog(this).show();
        } else {
            new RetryDialog(this, retryCount).show();
        }
    }


    /*return http POST method using parameters*/
    private HttpResponse requestType(URI website, StringEntity entity) throws IOException {

        HttpParams httpParameters = new BasicHttpParams();
        int timeoutConnection = 50000;
        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
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
     * Concrete method
     */
    @Override
    protected void processMessage(Bundle message, ServiceListenerType what) {

    }

    @Override
    public void onBackPressed() {
    }

    /**
     * Async   task for Download Sync for table details
     */
    private class UpdateSyncTask extends AsyncTask<String, String, String> {


        @Override
        protected String doInBackground(String... f_url) {
            BufferedReader in = null;
            try {
                String url = serverUrl + "/bunk/getdetails";

                URI website = new URI(url);
                HttpResponse response = requestType(website, stringEntity);
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
                Util.LoggingQueue(SyncPageActivity.this, "Error", "Network exception" + e.getMessage());
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
                Log.e("Response", response);
                Util.LoggingQueue(SyncPageActivity.this, "Sync Page", "First sync resp:" + response);
                processSyncResponseData(response);
            } else {
                errorInSync();
            }
        }
    }

    /**
     * Async   task for Download Sync for data in table
     */
    private class UpdateTablesTask extends AsyncTask<String, String, String> {


        @Override
        protected String doInBackground(String... f_url) {
            BufferedReader in = null;
            try {
                String url = serverUrl + "/bunk/gettabledetails";
                URI website = new URI(url);
                HttpResponse response = requestType(website, stringEntity);
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
                Util.LoggingQueue(SyncPageActivity.this, "Error", "Network exception" + e.getMessage());
                Log.e("SyncPageActivity", e.toString(), e);
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
                Util.LoggingQueue(SyncPageActivity.this, "Sync Page", "Table Wise Sync resp" + response);
                Log.e("Response", response);
                setTableResponse(response);
            } else {
                errorInSync();
            }
        }
    }

    private void getOpeningStock(){
        try {
            String deviceId = Settings.Secure.getString(
                    getContentResolver(), Settings.Secure.ANDROID_ID).toUpperCase();
            KeroseneBunkStockHistoryDto open = new KeroseneBunkStockHistoryDto();
            open.setDeviceNum(deviceId);
            String updateData = new Gson().toJson(open);
            Util.LoggingQueue(this, "Sync Page", "Sync Success req:" + updateData);
            stringEntity = new StringEntity(updateData, HTTP.UTF_8);
            new getOpeningStockTask().execute("");
        }catch (Exception e){
            Log.e("Error",e.toString(),e);
        }
    }


    /**
     * Async   task for Download Sync for table details
     */
    private class getOpeningStockTask extends AsyncTask<String, String, String> {


        @Override
        protected String doInBackground(String... f_url) {
            BufferedReader in = null;
            try {
                String url = serverUrl + "/bunk/getlastdayclosingstock";
              //  Log.e(TAG, "sync Execution 9 getlastdayclosingstock response " + url);
                URI website = new URI(url);
                HttpResponse response = requestType(website, stringEntity);
                in = new BufferedReader(new InputStreamReader(response
                        .getEntity().getContent()));
                StringBuffer sb = new StringBuffer("");
                String l;
                String nl = System.getProperty("line.separator");
                while ((l = in.readLine()) != null) {
                    sb.append(l + nl);
                }
                in.close();
              //  Log.e(TAG, "sync Execution 9 getlastdayclosingstock response " + sb.toString());
                return  sb.toString();
            } catch (Exception e) {
                Util.LoggingQueue(SyncPageActivity.this, "Error", "Network exception" + e.getMessage());
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
                Log.e("Response", response);
                Util.LoggingQueue(SyncPageActivity.this, "Sync Page", "First sync resp:" + response);
                openStockHistory(response);
            } else {
                errorInSync();
            }
        }
    }


    private void openStockHistory(String result){
        try {
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            FirstSynchResDto fpsDataDto = gson.fromJson(result, FirstSynchResDto.class);
            int statusCode = fpsDataDto.getStatusCode();
            if (statusCode == 0) {
                FPSDBHelper.getInstance(this).insertProductHistory(fpsDataDto.getKeroseneBunkStockHistoryDtos());
            }
        }catch (Exception e){
            Log.e("Error",e.toString(),e);
        }finally {
            firstSyncSuccess();
        }

    }
    /**
     * Async   task for Download Sync for data in table
     */
    private class SyncSuccess extends AsyncTask<String, String, String> {


        @Override
        protected String doInBackground(String... f_url) {
            BufferedReader in = null;
            try {
                String url = serverUrl + "/bunk/completesynch";
                URI website = new URI(url);
                HttpResponse response = requestType(website, stringEntity);
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
                Util.LoggingQueue(SyncPageActivity.this, "Error", "Network exception" + e.getMessage());
                Log.e("SyncPageActivity", e.toString(), e);
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
                Log.i("Response", response);
                Util.LoggingQueue(SyncPageActivity.this, "Sync Page", "Sync Success Resp:" + response);
                firstSyncSuccessResponse(response);
            }
        }
    }


}
