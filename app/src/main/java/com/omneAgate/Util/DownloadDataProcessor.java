package com.omneAgate.Util;

import android.content.Context;
import android.os.AsyncTask;
import android.provider.Settings;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.omneAgate.Bunker.GlobalAppState;
import com.omneAgate.Bunker.dialog.StockAdjustAlertDialog;
import com.omneAgate.Bunker.dialog.StockInwardAlertDialog;
import com.omneAgate.DTO.EnumDTO.TableNames;
import com.omneAgate.DTO.FirstSynchReqDto;
import com.omneAgate.DTO.FirstSynchResDto;
import com.omneAgate.DTO.FistSyncInputDto;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Used to download master data
 */
public class DownloadDataProcessor {


    //Activity context
    private final Context context;

    StringEntity stringEntity;

    List<FistSyncInputDto> firstSync;  //FistSync items

    private String serverUrl = "";
    List<Long> bunkerfpsList ;

    //Constructor
    public DownloadDataProcessor(Context context) {
        this.context = context;
    }


    // Send Request to the server
    public void processor() {
        try {
            String lastModifiedDate = FPSDBHelper.getInstance(context).getMasterData("syncTime");
            FirstSynchReqDto fpsRequest = new FirstSynchReqDto();
            String deviceId = Settings.Secure.getString(
                    context.getContentResolver(), Settings.Secure.ANDROID_ID).toUpperCase();
            fpsRequest.setDeviceNum(deviceId);
            fpsRequest.setLastSyncTime(lastModifiedDate);
            bunkerfpsList=FPSDBHelper.getInstance(context).getAllDistinctFps();
            fpsRequest.setBunkerFpsIdList(bunkerfpsList);
            serverUrl = FPSDBHelper.getInstance(context).getMasterData("serverUrl");
            String updateData = new Gson().toJson(fpsRequest);
            Log.e("Request", updateData);
            stringEntity = new StringEntity(updateData, HTTP.UTF_8);
            new UpdateSyncTask().execute("");

        } catch (Exception e) {
            Util.LoggingQueue(context, "Download Error", "Error:" + e.toString());
        }
    }


    // After response received from server successfully in android
    private void processSyncResponseData(Map<String, Integer> tableDetails) {
        try {

            firstSync = new ArrayList<>();
            if (tableDetails.containsKey("TABLE_CARDTYPE"))
                firstSync.add(getInputDTO("TABLE_CARDTYPE", TableNames.TABLE_CARDTYPE));

            if (tableDetails.containsKey("TABLE_PRODUCT"))
                firstSync.add(getInputDTO("TABLE_PRODUCT", TableNames.TABLE_PRODUCT));

            if (tableDetails.containsKey("TABLE_BUNKSTOCK"))
                firstSync.add(getInputDTO("TABLE_BUNKSTOCK", TableNames.TABLE_BUNKSTOCK));

            if (tableDetails.containsKey("TABLE_SMSPROVIDER"))
                firstSync.add(getInputDTO("TABLE_SMSPROVIDER", TableNames.TABLE_SMSPROVIDER));

            if (tableDetails.containsKey("TABLE_SPLRULES"))
                firstSync.add(getInputDTO("TABLE_SPLRULES", TableNames.TABLE_SPLRULES));

            if (tableDetails.containsKey("TABLE_ENTITLEMENTMASTER"))
                firstSync.add(getInputDTO("TABLE_ENTITLEMENTMASTER", TableNames.TABLE_ENTITLEMENTMASTER));

            if (tableDetails.containsKey("TABLE_FPSMIGRATION"))
                firstSync.add(getInputDTO("TABLE_FPSMIGRATION", TableNames.TABLE_FPSMIGRATION));

            if (tableDetails.containsKey("TABLE_PERSONBASEDRULE"))
                firstSync.add(getInputDTO("TABLE_PERSONBASEDRULE", TableNames.TABLE_PERSONBASEDRULE));

            if (tableDetails.containsKey("TABLE_REGIONBASEDRULE"))
                firstSync.add(getInputDTO("TABLE_REGIONBASEDRULE", TableNames.TABLE_REGIONBASEDRULE));


            if (tableDetails.containsKey("TABLE_BENEFICIARY"))
                firstSync.add(getInputDTO("TABLE_BENEFICIARY", TableNames.TABLE_BENEFICIARY));

            if (tableDetails.containsKey("TABLE_GODOWNSTKOUTWARD"))
                firstSync.add(getInputDTO("TABLE_GODOWNSTKOUTWARD", TableNames.TABLE_FPSSTOCKINWARD));

            if (tableDetails.containsKey("TABLE_PRODUCTGROUP"))
                firstSync.add(getInputDTO("TABLE_PRODUCTGROUP", TableNames.TABLE_PRODUCTGROUP));

            if (tableDetails.containsKey("TABLE_PRICEOVERRIDE"))
                firstSync.add(getInputDTO("TABLE_PRICEOVERRIDE", TableNames.TABLE_PRICEOVERRIDE));

            if (tableDetails.containsKey("TABLE_BILL"))
                firstSync.add(getInputDTO("TABLE_BILL", TableNames.TABLE_BILL));

            if (tableDetails.containsKey("TABLE_SERVICEPROVIDER"))
                firstSync.add(getInputDTO("TABLE_SERVICEPROVIDER", TableNames.TABLE_SERVICEPROVIDER));


      /*  if (tableDetails.containsKey("TABLE_USERDETAIL"))
            firstSync.add(getInputDTO("TABLE_USERDETAIL", TableNames.TABLE_USERDETAIL));*/

            if (tableDetails.containsKey("TABLE_STOCKADJUSTMENT"))
                firstSync.add(getInputDTO("TABLE_STOCKADJUSTMENT", TableNames.TABLE_STOCKADJUSTMENT));

            if(tableDetails.containsKey("TABLE_ACTIVEFPS"))
                firstSync.add(getInputDTO("TABLE_ACTIVEFPS",TableNames.TABLE_ACTIVEFPS));

            if(tableDetails.containsKey("TABLE_NEWACTIVEFPSBENEFICIARY"))
                firstSync.add(getInputDTO("TABLE_NEWACTIVEFPSBENEFICIARY",TableNames.TABLE_NEWACTIVEFPSBENEFICIARY));

            if (tableDetails.containsKey("TABLE_GIVEITUPREQUEST"))
                firstSync.add(getInputDTO("TABLE_GIVEITUPREQUEST", TableNames.TABLE_GIVEITUPREQUEST));


            if (firstSync.size() > 0) {
                FirstSynchReqDto fpsRequest = new FirstSynchReqDto();
                String lastModifiedDate = FPSDBHelper.getInstance(context).getMasterData("syncTime");
                fpsRequest.setLastSyncTime(lastModifiedDate);
                String deviceId = Settings.Secure.getString(
                        context.getContentResolver(), Settings.Secure.ANDROID_ID).toUpperCase();
                fpsRequest.setDeviceNum(deviceId);
                fpsRequest.setTableName(firstSync.get(0).getTableName());

                setTableSyncCall(fpsRequest);
            } else {
                firstSyncSuccess();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Request for datas by giving name of table to server
     * <p/>
     * input FirstSynchReqDto fpsRequest
     */

    private void setTableSyncCall(FirstSynchReqDto fpsRequest) {
        try {
            serverUrl = FPSDBHelper.getInstance(context).getMasterData("serverUrl");
            String updateData = new Gson().toJson(fpsRequest);
            Log.e("Request sync call", updateData);
            Util.LoggingQueue(context, "Download Sync", "Req:" + updateData);
            stringEntity = new StringEntity(updateData, HTTP.UTF_8);
            new UpdateTablesTask().execute("");
        } catch (Exception e) {
            Log.e("Exception", " setTableSyncCall "+e.toString(), e);
        }
    }

    /**
     * Response received from server
     */
    private void setTableResponse(String response) {
        try {
            if (StringUtils.isNotEmpty(response)) {
                GsonBuilder gsonBuilder = new GsonBuilder();
                Gson gson = gsonBuilder.create();
                Log.e("setTableResponse", response);
                FirstSynchResDto fpsDataDto = gson.fromJson(response, FirstSynchResDto.class);
                if (fpsDataDto.getStatusCode() == 0) {
                    insertIntoDatabase(fpsDataDto);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Database insertion of received data
     */
    private void insertIntoDatabase(FirstSynchResDto firstSynchResDto) {
        try {

            FistSyncInputDto fistSyncInputDto = firstSync.get(0);
            switch (fistSyncInputDto.getTableNames()) {

                case TABLE_CARDTYPE:
                    FPSDBHelper.getInstance(context).insertCardTypeData(firstSynchResDto.getCardtypeDto());
                    break;
                case TABLE_BUNKSTOCK:
                    FPSDBHelper.getInstance(context).insertFpsStockData(firstSynchResDto.getKeroseneBunkStockDtos());
                    break;
                case TABLE_PRODUCT:
                    FPSDBHelper.getInstance(context).insertProductData(firstSynchResDto.getProductDto());
                    break;
                case TABLE_BILL:
                    FPSDBHelper.getInstance(context).insertBillData(firstSynchResDto.getBillDto());
                    break;
                case TABLE_BENEFICIARY:
                    FPSDBHelper.getInstance(context).insertBeneficiaryData(firstSynchResDto.getBeneficiaryDto());
                    break;
               /* case TABLE_BENEFREGREQ:
                    FPSDBHelper.getInstance(context).insertRegistrationRequestData(firstSynchResDto.getBenefRegReqDto());
                    break;*/
                case TABLE_REGIONBASEDRULE:
                    FPSDBHelper.getInstance(context).insertRegionRules(firstSynchResDto.getRegionBasedRulesDto());
                    break;
                case TABLE_PRICEOVERRIDE:
                    FPSDBHelper.getInstance(context).insertProductPriceOverride(firstSynchResDto.getOverrideDto());
                    break;
                case TABLE_PRODUCTGROUP:
                    FPSDBHelper.getInstance(context).insertProductGroup(firstSynchResDto.getProductGroupDtos());
                    break;
                case TABLE_FPSSTOCKINWARD:
                    FPSDBHelper.getInstance(context).insertFpsStockInwardDetails(firstSynchResDto.getGodownStockOutwardDto());

                    try {
                        StockInwardAlertDialog inward_dialog = new StockInwardAlertDialog(GlobalAppState.getInstance().getBaseContext());
                        inward_dialog.show();
                        // new StockInwardAlertDialog(GlobalAppState.getInstance().getBaseContext()).show();
                    } catch (Exception e) {
                        Log.e("exception", "" + e.toString());
                    }

                    break;
                case TABLE_USERDETAIL:
                    FPSDBHelper.getInstance(context).insertUserDetailData(firstSynchResDto.getUserdetailDto());
                    break;
                case TABLE_ENTITLEMENTMASTER:
                    FPSDBHelper.getInstance(context).insertMasterRules(firstSynchResDto.getEntitlementMasterRulesDto());
                    break;
                case TABLE_FPSMIGRATION:
                    FPSDBHelper.getInstance(context).insertMigrations(firstSynchResDto.getFpsMigrationDtos());
                    break;
                case TABLE_SPLRULES:
                    FPSDBHelper.getInstance(context).insertSpecialRules(firstSynchResDto.getSplEntitlementRulesDto());
                    break;
                case TABLE_PERSONBASEDRULE:
                    FPSDBHelper.getInstance(context).insertPersonRules(firstSynchResDto.getPersonBasedRulesDto());
                    break;
                case TABLE_SERVICEPROVIDER:
//                    FPSDBHelper.getInstance(context).insertLpgProviderDetails(firstSynchResDto.getServiceProviderDto());
                    break;
                case TABLE_SMSPROVIDER:
                    FPSDBHelper.getInstance(context).insertSmsProvider(firstSynchResDto.getSmsProviderDtos());
                    break;
                case TABLE_STOCKADJUSTMENT:
                    try {
                      Log.e("**Download","stock_adjustment");
                        FPSDBHelper.getInstance(context).stockAdjustmentFirstSync(firstSynchResDto.getStockAdjusmentDtos());
                        new StockAdjustAlertDialog(GlobalAppState.getInstance().getBaseContext()).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    break;

                case TABLE_ACTIVEFPS:
                    try {
                        Log.e("Regular sync", "Active Fps Insertion started");
                        FPSDBHelper.getInstance(context).deleteFromActiveFps();
                        FPSDBHelper.getInstance(context).insertActiveFpsDetails(firstSynchResDto.getFpsstoreDto());
                        FPSDBHelper.getInstance(context).UpdateInactiveFPsStore();
                    } catch (Exception e) {
                        Log.e("Regular sync", "TABLE_ACTIVEFPS" + e.toString());
                    }

                    break;

                case TABLE_NEWACTIVEFPSBENEFICIARY:
                    try {
                        FPSDBHelper.getInstance(context).insertBeneficiaryData(firstSynchResDto.getBeneficiaryDto());
                    }catch (Exception e){
                        Log.e("Regular sync", "TABLE_NEWACTIVEFPSBENEFICIARY" + e.toString());
                    }
                    break;

                case TABLE_GIVEITUPREQUEST:
                    try {
                        FPSDBHelper.getInstance(context).insert_giveitup(firstSynchResDto.getGiveItUpRequestDto());
                    }catch (Exception e){
                        Log.e("Regular sync", "Error inserting Give Up Table " + e.toString());
                    }
                    break;
                default:
                    break;
            }

            afterDatabaseInsertion(firstSynchResDto);

        } catch (Exception e) {
            Log.e("Exception "," insertIntoDatabase : "+e.toString());
        }
    }

    /*
    * After database insertion by user master this function called
    * */
    private void afterDatabaseInsertion(FirstSynchResDto firstSynchResDto) throws Exception {
        try {
            FirstSynchReqDto fpsRequest = new FirstSynchReqDto();
            String deviceId = Settings.Secure.getString(
                    context.getContentResolver(), Settings.Secure.ANDROID_ID).toUpperCase();
            fpsRequest.setDeviceNum(deviceId);
            String lastModifiedDate = FPSDBHelper.getInstance(context).getMasterData("syncTime");
            fpsRequest.setLastSyncTime(lastModifiedDate);
            if (firstSynchResDto.isHasMore()) {
                fpsRequest.setTotalCount(firstSynchResDto.getTotalCount());
                fpsRequest.setTotalSentCount(firstSynchResDto.getTotalSentCount());
                fpsRequest.setCurrentCount(firstSynchResDto.getCurrentCount());
                fpsRequest.setTableName(firstSync.get(0).getTableName());
                if (firstSync.get(0).getTableName().equalsIgnoreCase("TABLE_NEWACTIVEFPSBENEFICIARY")) {
                    fpsRequest.setBunkerFpsIdList(bunkerfpsList);
                }
                setTableSyncCall(fpsRequest);
            } else {
                firstSync.remove(0);
                if (firstSync.size() > 0) {
                    if (firstSync.get(0).getTableName().equalsIgnoreCase("TABLE_NEWACTIVEFPSBENEFICIARY")) {
                        fpsRequest.setBunkerFpsIdList(bunkerfpsList);
                    }
                    fpsRequest.setTableName(firstSync.get(0).getTableName());
                    setTableSyncCall(fpsRequest);
                } else {
                    firstSyncSuccess();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * After sync success this method will call
     */
    private void firstSyncSuccess() {
        try {
            FirstSynchReqDto fpsRequest = new FirstSynchReqDto();
            String deviceId = Settings.Secure.getString(
                    context.getContentResolver(), Settings.Secure.ANDROID_ID).toUpperCase();
            fpsRequest.setDeviceNum(deviceId);
            serverUrl = FPSDBHelper.getInstance(context).getMasterData("serverUrl");
            String updateData = new Gson().toJson(fpsRequest);
            stringEntity = new StringEntity(updateData, HTTP.UTF_8);
            Util.LoggingQueue(context, "Download Sync success", "Req:" + updateData);
            new SyncSuccess().execute("");

        } catch (Exception e) {
            Util.LoggingQueue(context, "Error", e.toString());
            Log.e("Error in First sync", e.toString(), e);
        }
    }

    private void firstSyncSuccessResponse(String response) {
        try {
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            FirstSynchResDto fpsDataDto = gson.fromJson(response, FirstSynchResDto.class);
            int statusCode = fpsDataDto.getStatusCode();
            if (statusCode == 0 && fpsDataDto.getLastSyncTime()!=null) {
                FPSDBHelper.getInstance(context).updateMaserData("syncTime", fpsDataDto.getLastSyncTime());
            }
        } catch (Exception e) {
            Log.e("Exception",e.toString(),e);
        }
    }

    /**
     * returns FistSyncInputDto of details of tables received from server
     */
    private FistSyncInputDto getInputDTO(String tableName, TableNames names) {
        FistSyncInputDto inputDto = new FistSyncInputDto();
        inputDto.setTableName(tableName);
        inputDto.setTableNames(names);
        inputDto.setDynamic(true);
        return inputDto;
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
     * Async   task for Download Sync for data in table
     */
    private class UpdateTablesTask extends AsyncTask<String, String, String> {


        @Override
        protected String doInBackground(String... f_url) {
            BufferedReader in = null;
            try {
                String url = serverUrl + "/bunk/getrstabledetail";
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
                Log.e("Response", responseData);
                return responseData;
            } catch (Exception e) {
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
            Util.LoggingQueue(context, "Download Sync", "Response:" + response);
            setTableResponse(response);
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
                Log.e("server Url", url);
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
                return sb.toString();
            } catch (Exception e) {
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
                Util.LoggingQueue(context, "Download Sync success", "Response:" + response);
                firstSyncSuccessResponse(response);
            }
        }
    }

    //Async   task for Download Sync
    private class UpdateSyncTask extends AsyncTask<String, String, String> {


        @Override
        protected String doInBackground(String... f_url) {
            BufferedReader in = null;
            try {
                String url = serverUrl + "/bunk/regularsynch";
                URI website = new URI(url);
                HttpResponse response = requestType(website, stringEntity);
               int status=  response.getStatusLine().getStatusCode();
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
                try {
                    if (in != null)
                        in.close();
                } catch (Exception e1) {
                    Log.e("Exception","UpdateSyncTask "+e1.toString());
                }
            }


            return null;
        }


        @Override
        protected void onPostExecute(String response) {
            try {
                GsonBuilder gsonBuilder = new GsonBuilder();
                Gson gson = gsonBuilder.create();
                Log.e("Table name and count ","Response for table "+response);
                FirstSynchResDto fpsDataDto = gson.fromJson(response, FirstSynchResDto.class);
                int statusCode = fpsDataDto.getStatusCode();
                if (statusCode == 0) {
                    processSyncResponseData(fpsDataDto.getTableDetails());
                }
            } catch (Exception e) {
                Log.e("Excep in Resp"," UpdateSyncTask "+ e.toString(), e);
            }
        }
    }

}