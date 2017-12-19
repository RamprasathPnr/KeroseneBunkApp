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
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.omneAgate.DTO.ApplicationType;
import com.omneAgate.DTO.BillItemDto;
import com.omneAgate.DTO.CloseOfProductDto;
import com.omneAgate.DTO.CloseSaleTransactionDto;
import com.omneAgate.DTO.EnumDTO.RequestType;
import com.omneAgate.DTO.EnumDTO.ServiceListenerType;
import com.omneAgate.DTO.FPSStockHistoryDto;
import com.omneAgate.DTO.LogoutDto;
import com.omneAgate.DTO.POSStockAdjustmentDto;
import com.omneAgate.DTO.UserDto.StockCheckDto;
import com.omneAgate.Util.Constants.FPSDBConstants;
import com.omneAgate.Util.CustomProgressDialog;
import com.omneAgate.Util.FPSDBHelper;
import com.omneAgate.Util.NetworkConnection;
import com.omneAgate.Util.NetworkUtil;
import com.omneAgate.Util.SessionId;
import com.omneAgate.Util.StringDigesterString;
import com.omneAgate.Util.Util;
import com.omneAgate.Bunker.dialog.CloseSaleDialog;
import com.omneAgate.Bunker.dialog.CloseSalePasswordDialog;
import com.omneAgate.service.HttpClientWrapper;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import org.jasypt.digest.StringDigester;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class TransactionCommodityActivity extends BaseActivity {
    CloseSaleTransactionDto closeSaleTransactionDto;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_transaction_commodity);
        configureData();
    }


    /*Data from server has been set inside this function*/
    private void configureData() {
        try {
            closeSaleTransactionDto = new CloseSaleTransactionDto();
            setUpPopUpPage();
            Util.LoggingQueue(this, "Close sale activity", "Page stating up");
            SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy",Locale.getDefault());
            String formattedDate = df.format(new Date());
            setTamilText((TextView) findViewById(R.id.top_textView), R.string.close_sale);
            setTamilText((TextView) findViewById(R.id.commodity), R.string.commodity);
            setTamilText((TextView) findViewById(R.id.opening_stock), R.string.opening_stock);
            setTamilText((TextView) findViewById(R.id.inward_qty), R.string.inward_qty);
            setTamilText((TextView) findViewById(R.id.sale_qty), R.string.sale_qty);
            setTamilText((TextView) findViewById(R.id.current_stock), R.string.current_stock);
            setTamilText((TextView) findViewById(R.id.btnClose), R.string.close_sale);
            setTamilText((TextView) findViewById(R.id.stock_adjustment), R.string.stock_adjust);
            setTamilText((TextView) findViewById(R.id.totAmount), R.string.totAmount);
            setTamilText((TextView) findViewById(R.id.totBills), R.string.total_bill);
            ((TextView) findViewById(R.id.fpsCode)).setText("Kerosene Bunker Code : " + SessionId.getInstance().getFpsCode().toUpperCase());
            ((TextView) findViewById(R.id.date_today)).setText(formattedDate);
            LinearLayout transactionLayout = (LinearLayout) findViewById(R.id.listView_linearLayout_stock_status);
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String date = df2.format(c.getTime());
            List<StockCheckDto> productDtoList = FPSDBHelper.getInstance(this).getAllProductStockDetails(date);
            Log.i("product Dto", productDtoList.toString());
            int count = FPSDBHelper.getInstance(this).totalBillsToday(date);
            closeSaleTransactionDto.setDateOfTxn(new Date().getTime());

            closeSaleTransactionDto.setNumofTrans(count);
            closeSaleTransactionDto.setDeviceId(Settings.Secure.getString(
                    getContentResolver(), Settings.Secure.ANDROID_ID).toUpperCase());

            SimpleDateFormat df3  = new SimpleDateFormat("ddMMyymmss", Locale.getDefault());
            String transactionId = df3.format(new Date());
            transactionId = SessionId.getInstance().getFpsId() + transactionId;

            closeSaleTransactionDto.setTransactionId(Long.parseLong(transactionId));

            Double sum_amount = FPSDBHelper.getInstance(this).totalAmountToday(date);
            ((TextView) findViewById(R.id.close_sale_total_bills)).setText(count + "  ");
            NumberFormat format = new DecimalFormat("#0.00");
            ((TextView) findViewById(R.id.close_sale_total_amt)).setText("\u20B9 " + format.format(sum_amount));
            transactionLayout.removeAllViews();
            closeSaleTransactionDto.setTotalSaleCost(Double.parseDouble(format.format(sum_amount)));
            for (StockCheckDto products : productDtoList) {
                LayoutInflater lin = LayoutInflater.from(this);
                transactionLayout.addView(returnView(lin, products));
            }

        } catch (Exception e) {
            Util.LoggingQueue(this, "Close sale activity", "Error:" + e.toString());
            Log.e("TransactionCommodity", e.toString(), e);
        } finally {
            findViewById(R.id.btnClose).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new CloseSaleDialog(TransactionCommodityActivity.this).show();

                }
            });

            findViewById(R.id.imageViewBack).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }
    }




    public void getUserPassword() {
        new CloseSalePasswordDialog(this).show();
    }

    public void passwordChecking(String password) {
        String passwordHash = FPSDBHelper.getInstance(this).getUserDetails(SessionId.getInstance().getUserId()).getUserDetailDto().getPassword();
        new LocalPasswordProcessCheck().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, password, passwordHash);
    }

    private boolean localDbPassword(String passwordUser, String passwordDbHash) {

        StringDigester stringDigester = StringDigesterString.getPasswordHash(this);

        return stringDigester.matches(passwordUser, passwordDbHash);
    }

    //Local login Process
    private class LocalPasswordProcessCheck extends AsyncTask<String, Void, Boolean> {


        /**
         * Local login Background Process
         * return true if user hash and dbhash equals else false
         */
        protected Boolean doInBackground(String... params) {
            try {
                return localDbPassword(params[0], params[1]);
            } catch (Exception e) {
                Log.e("loca lDb", "Interrupted", e);
                return false;

            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (progressBar != null) progressBar.dismiss();
            if (result) {
                sendCloseSale();
            } else {
                Util.messageBar(TransactionCommodityActivity.this, getString(R.string.loginInvalidUserPassword));
            }
        }
    }

    /*public void logOutSuccess() {
        //Logout request from user success and send to server
        SessionId.getInstance().setSessionId("");
        networkConnection = new NetworkConnection(this);
        String logoutString = "CLOSE_SALE_LOGOUT_OFFLINE";
        if (networkConnection.isNetworkAvailable()) {
            String url = "/login/logmeout";
            httpConnection = new HttpClientWrapper();
            logoutString = "CLOSE_SALE_LOGOUT_ONLINE";
            httpConnection.sendRequest(url, null, ServiceListenerType.LOGOUT_USER,
                    SyncHandler, RequestType.GET, null, this);
        }
        FPSDBHelper.getInstance(this).updateLoginHistory(SessionId.getInstance().getTransactionId(), logoutString);
        FPSDBHelper.getInstance(this).closeConnection();
        startActivity(new Intent(this, LoginActivity.class));
        finish();

    }*/
    private CloseOfProductDto getCloseSaleProduct(CloseOfProductDto closeOfProductDto, List<CloseOfProductDto> closeSale) {
        for (CloseOfProductDto close : closeSale) {
            if (close.getProductId() == closeOfProductDto.getProductId()) {
                closeOfProductDto.setTotalCost(close.getTotalCost());
                closeOfProductDto.setTotalQuantity(close.getTotalQuantity());
                closeOfProductDto.setOpeningStock(close.getOpeningStock());
                closeOfProductDto.setClosingStock(close.getClosingStock());
                closeOfProductDto.setInward(close.getInward());
            }
        }
        if (closeOfProductDto.getTotalCost() == null || StringUtils.isEmpty(closeOfProductDto.getTotalCost())) {
            closeOfProductDto.setTotalCost("0.00");
        }
        if (closeOfProductDto.getTotalQuantity() == null || StringUtils.isEmpty(closeOfProductDto.getTotalQuantity())) {
            closeOfProductDto.setTotalQuantity("0.000");
        }
        return closeOfProductDto;
    }

    private void sendCloseSale() {
        try {
            findViewById(R.id.btnClose).setOnClickListener(null);
            List<CloseOfProductDto> closeSale = FPSDBHelper.getInstance(this).getCloseSale();
            List<CloseOfProductDto> closeSaleUpdated = new ArrayList<>();

            for (CloseOfProductDto closeOfProductDto : closeSale) {
                closeSaleUpdated.add(getCloseSaleProduct(closeOfProductDto, closeSale));
            }
            closeSaleTransactionDto.setCloseOfProductDtoList(new HashSet<>(closeSaleUpdated));
            if (NetworkUtil.getConnectivityStatus(this) == 0 || SessionId.getInstance().getSessionId().length() <= 0) {
                closeSaleTransactionDto.setIsServerAdded(1);
                FPSDBHelper.getInstance(this).insertIntoCloseSale(closeSaleTransactionDto);

                logOutCloseSaleSuccess();
            } else {


                closeSaleTransactionDto.setIsServerAdded(2);
                closeSaleTransactionDto.setStatusCode(2000);
                closeSaleTransactionDto.setAppType(ApplicationType.KEROSENE_BUNK);
                FPSDBHelper.getInstance(this).insertIntoCloseSale(closeSaleTransactionDto);

                httpConnection = new HttpClientWrapper();
                String url = "/bunk/closeofsale/save";

                String beneRegReq = new Gson().toJson(closeSaleTransactionDto);
                Log.e("Check", beneRegReq);
                StringEntity se = new StringEntity(beneRegReq, HTTP.UTF_8);
                Util.LoggingQueue(this, "Ration Card Registration", "Sending Benefeciary registration request to FPS server" + beneRegReq);
                progressBar = new CustomProgressDialog(this);
                progressBar.setCancelable(false);
                progressBar.show();
                httpConnection.sendRequest(url, null, ServiceListenerType.CLOSE_SALE,
                        SyncHandler, RequestType.POST, se, this);
            }
        } catch (Exception e) {
            Log.e("Error", e.toString(), e);
        }
    }

//    public void logOutSuccess() {
//        SessionId.getInstance().setSessionId("");
//        FPSDBHelper.getInstance(TransactionCommodityActivity.this).updateLoginHistory(SessionId.getInstance().getTransactionId(), "CLOSE_SALE_LOGOUT_OFFLINE");
//        FPSDBHelper.getInstance(TransactionCommodityActivity.this).closeConnection();
//        startActivity(new Intent(TransactionCommodityActivity.this, LoginActivity.class));
//        finish();
//    }


    public void logOutCloseSaleSuccess() {
        //  SessionId.getInstance().setSessionId("");
        networkConnection = new NetworkConnection(this);
        String logoutString = "CLOSE_SALE_LOGOUT_OFFLINE";
        if (networkConnection.isNetworkAvailable()) {
            try {
                logoutString = "CLOSE_SALE_LOGOUT_ONLINE";
                String url = "/login/logout";

                LogoutDto logoutDto = new LogoutDto();
                logoutDto.setSessionId(SessionId.getInstance().getSessionId());
                logoutDto.setLogoutStatus(logoutString);
                logoutDto.setAppType(ApplicationType.KEROSENE_BUNK);
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
                String dateStr = df.format(new Date());
                logoutDto.setLogoutTime(dateStr);
                String logout = new Gson().toJson(logoutDto);
                Log.e("base activity", "logout..." + logout);
                StringEntity se = new StringEntity(logout, HTTP.UTF_8);

                httpConnection = new HttpClientWrapper();
                httpConnection.sendRequest(url, null, ServiceListenerType.LOGOUT_USER,
                        SyncHandler, RequestType.POST, se, this);
            }catch (Exception e){
                Log.e("LOGOut","Exception occured"+e);
            }
        }
        FPSDBHelper.getInstance(this).updateLoginHistory(SessionId.getInstance().getTransactionId(), logoutString);
        //  FPSDBHelper.getInstance(this).closeConnection();
        startActivity(new Intent(this, LoginActivity.class));
        finish();

    }

    private View returnView(LayoutInflater entitle, StockCheckDto fpsStock) {
        View convertView = entitle.inflate(R.layout.adapter_commodity_stock, null);
        TextView productNameTv = (TextView) convertView.findViewById(R.id.entitlementName);
        TextView unitTv = (TextView) convertView.findViewById(R.id.entitlementUnit);
        TextView openingStockTv = (TextView) convertView.findViewById(R.id.entitlement_opening);
        TextView adjustmentStock = (TextView) convertView.findViewById(R.id.entitlement_adjustment);
        TextView saleQuantityTv = (TextView) convertView.findViewById(R.id.entitlement_sale);
        TextView currentStockTv = (TextView) convertView.findViewById(R.id.amount_current);
        TextView inwardQuantityTv = (TextView) convertView.findViewById(R.id.entitlement_inward_quantity);

        NumberFormat format = new DecimalFormat("#0.000");
        FPSStockHistoryDto fpsStockHistory = FPSDBHelper.getInstance(this).getAllProductStockHistoryDetails(fpsStock.getProductId());
        List<POSStockAdjustmentDto> fpsStockAdjustment = FPSDBHelper.getInstance(this).getStockAdjustment(fpsStock.getProductId());
        double adjustment = 0.0;
        if (fpsStockAdjustment.size() == 0) {
            adjustmentStock.setText(format.format(adjustment));
        } else {
            adjustment = getAdjustedValue(fpsStockAdjustment);
            adjustmentStock.setText(format.format(adjustment));
            if(adjustment<0){
                adjustmentStock.setTextColor(Color.RED);
            }
        }
        productNameTv.setText(fpsStock.getName());
        if (GlobalAppState.language.equalsIgnoreCase("ta") && StringUtils.isNotEmpty(fpsStock.getLocalName())) {
            productNameTv.setText(unicodeToLocalLanguage(fpsStock.getLocalName()));
        }
        unitTv.setText(fpsStock.getUnit());
        if (GlobalAppState.language.equalsIgnoreCase("ta") && StringUtils.isNotEmpty(fpsStock.getLocalUnit())) {
            unitTv.setText(unicodeToLocalLanguage(fpsStock.getLocalUnit()));
        }
        openingStockTv.setText(format.format(fpsStockHistory.getCurrQuantity()));
        currentStockTv.setText(format.format(fpsStock.getQuantity()));
        saleQuantityTv.setText(format.format(fpsStock.getSold()));
        double inward = getProductInward(fpsStock.getProductId());
        inwardQuantityTv.setText(format.format(inward));
        return convertView;

    }



    private double getAdjustedValue(List<POSStockAdjustmentDto> adjustment) {
        double quantity = 0.0;
        for (POSStockAdjustmentDto productValue : adjustment) {
            double productQuantity = productValue.getQuantity();
            if (productValue.getRequestType().equalsIgnoreCase("STOCK_DECREMENT")) {
                productQuantity = -1 * productQuantity;
            }
            quantity = quantity + productQuantity;
        }
        return quantity;
    }

    private Double getProductInward(long productId) {
        SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String date = df2.format(new Date());
        BillItemDto productInwardToday = FPSDBHelper.getInstance(this).getAllInwardListToday(date,productId);
        return productInwardToday.getQuantity();
    }

    @Override
    protected void processMessage(Bundle message, ServiceListenerType what) {
        try {
            if (progressBar != null) {
                progressBar.dismiss();
            }
        } catch (Exception e) {
        }
        switch (what) {

            case CLOSE_SALE:
                Log.e("Close sale","close service called");
                updateAndLogOut(message);
                break;
            default:
                Log.e("Close sale","default service called");
                break;
        }
    }
    private void updateAndLogOut(Bundle message) {
        try {
            Log.e("updateAndLogout","called");
            String response = message.getString(FPSDBConstants.RESPONSE_DATA);
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            CloseSaleTransactionDto closeSaleTransactionDto = gson.fromJson(response, CloseSaleTransactionDto.class);
            if (closeSaleTransactionDto != null && (closeSaleTransactionDto.getStatusCode() == 0 || closeSaleTransactionDto.getStatusCode() == 7001)) {
               FPSDBHelper.getInstance(this).updateCloseDate(closeSaleTransactionDto);
            }
            else {
                Util.LoggingQueue(TransactionCommodityActivity.this, "Error", "Received null response ");
            }
        } catch (Exception e) {
            Log.e("Error", e.toString(), e);
        } finally {
            Log.e("close transaction","logout service called");
            logOutCloseSaleSuccess();
        }
    }


    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, SaleActivity.class));
        Util.LoggingQueue(this, "Close sale Activity", "Back press called");
        finish();
    }


}
