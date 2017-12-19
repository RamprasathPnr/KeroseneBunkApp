package com.omneAgate.Bunker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.omneAgate.DTO.BillDto;
import com.omneAgate.DTO.UserDto.BillItemProductDto;
import com.omneAgate.DTO.EntitlementDTO;
import com.omneAgate.DTO.EnumDTO.ServiceListenerType;
import com.omneAgate.DTO.FPSStockDto;
import com.omneAgate.DTO.QRTransactionResponseDto;
import com.omneAgate.DTO.TransactionBaseDto;
import com.omneAgate.DTO.TransactionTypes;
import com.omneAgate.DTO.UpdateStockRequestDto;
import com.omneAgate.TransactionController.Transaction;
import com.omneAgate.TransactionController.TransactionFactory;
import com.omneAgate.Util.AndroidDeviceProperties;
import com.omneAgate.Util.CustomProgressDialog;
import com.omneAgate.Util.EntitlementResponse;
import com.omneAgate.Util.FPSDBHelper;
import com.omneAgate.Util.NetworkConnection;
import com.omneAgate.Util.SessionId;
import com.omneAgate.Util.TransactionBase;
import com.omneAgate.Util.Util;
import com.omneAgate.service.BillUpdateToServer;

import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;


//FPS user can view the summary of selection
public class SalesSummaryActivity extends BaseActivity {


    //Response from server set in this variable to load data
    private QRTransactionResponseDto entitlementResponseDTO;
    /*List of item entitled and price is in this variable*/
    private List<EntitlementDTO> entitleList;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_sales_submission);
        networkConnection = new NetworkConnection(this);
        appState = (GlobalAppState) getApplication();
        entitlementResponseDTO = EntitlementResponse.getInstance().getQrcodeTransactionResponseDto();
        setUpInitiailPage();
    }


    /*
  *
  * Initial Setup
  *
  * */
    private void setUpInitiailPage() {
        setUpPopUpPage();
        Util.LoggingQueue(this, "Sales Summary", "Sales Summary activity called");
        appState = (GlobalAppState) getApplication();
        setTamilText((TextView) findViewById(R.id.top_textView), R.string.sale_confirm_activity);
        setTamilText((TextView) findViewById(R.id.commoditys), R.string.commodity);
        setTamilText((TextView) findViewById(R.id.rate), R.string.rate);
        setTamilText((TextView) findViewById(R.id.billDetailQuantity), R.string.billDetailQuantity);
        setTamilText((TextView) findViewById(R.id.billDetailProductPrice), R.string.billDetailProductPrice);
        setTamilText((TextView) findViewById(R.id.editEntitlement), R.string.edit);
        setTamilText((TextView) findViewById(R.id.submitEntitlement), R.string.fpsIDSubmit);
        LinearLayout entitlementList = (LinearLayout) findViewById(R.id.entitlement_background);
        entitleList = entitlementResponseDTO.getEntitlementList();
        findViewById(R.id.imageViewBack).setVisibility(View.INVISIBLE);
        entitlementList.removeAllViews();
        int position = 0;
        for (EntitlementDTO entitled : entitleList) {
            if (entitled.getBought() > 0) {
                LayoutInflater lin = LayoutInflater.from(this);
                entitlementList.addView(returnView(lin, entitled, position));
                position++;
            }
        }
        setTamilText((TextView) findViewById(R.id.totalAmount), getString(R.string.total) + ": \u20B9 " + setTotalAmount());
//        ((TextView) findViewById(R.id.totalAmount)).setText(getString(R.string.total) + ": \u20B9 " + setTotalAmount());
        Button editBill = (Button) findViewById(R.id.editEntitlement);
        editBill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editBillSummary();
            }
        });
        findViewById(R.id.submitEntitlement).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitBill();
            }
        });

    }


    /**
     * Used to get  the bill data from user
     * returns  UpdateStockRequestDto
     */
    private UpdateStockRequestDto setBillData() {
        Util.LoggingQueue(this, "Sales Summary", "Update bills");
        UpdateStockRequestDto updateRequest = new UpdateStockRequestDto();
        QRTransactionResponseDto response = EntitlementResponse.getInstance().getQrcodeTransactionResponseDto();
        updateRequest.setReferenceId(response.getReferenceId());
        BillDto bill = new BillDto();
        updateRequest.setUfc(response.getUfc());
        bill.setFpsId(response.getFpsId());
        bill.setBeneficiaryId(response.getBenficiaryId());
        bill.setCreatedby(SessionId.getInstance().getUserId() + "");
        bill.setAmount(Double.parseDouble(setTotalAmount()));
        if (response.getTransactionType() == TransactionTypes.SALE_QR_OTP_DISABLED) {
            bill.setMode('Q');
        } else if (response.getTransactionType() == TransactionTypes.SALE_QR_OTP_AUTHENTICATION) {
            bill.setMode('O');
        } else {
            bill.setMode('R');
        }
        bill.setUfc(response.getUfc());
        bill.setChannel('G');
        Date todayDate = new Date();
        SimpleDateFormat billDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
        updateRequest.setOtpUsedTime(billDate.format(todayDate));
        bill.setBillDate(billDate.format(todayDate));
        bill.setTransactionId(Util.getTransactionId(this));
        List<BillItemProductDto> billItems = new ArrayList<BillItemProductDto>();
        for (EntitlementDTO entitleSelection : EntitlementResponse.getInstance().getQrcodeTransactionResponseDto().getEntitlementList()) {
            if (entitleSelection.getBought() > 0) {
                BillItemProductDto billItem = new BillItemProductDto();
                billItem.setProductId(entitleSelection.getProductId());
                billItem.setCost(entitleSelection.getProductPrice());
                billItem.setQuantity(entitleSelection.getBought());
                billItems.add(billItem);
            }
        }
        AndroidDeviceProperties device = new AndroidDeviceProperties(this);
        updateRequest.setDeviceId(device.getDeviceProperties().getSerialNumber());
        updateRequest.setUfc(response.getUfc());
        bill.setBillItemDto(new HashSet<>(billItems));
        updateRequest.setBillDto(bill);
        updateRequest.setOtpId(response.getOtpId());
        Util.LoggingQueue(this, "Sales Summary", "Update bills:" + updateRequest);
        return updateRequest;
    }


    /**
     * Used to submit the bill to local database
     * if it inserted then sent to server
     */

    private void submitBill() {
        findViewById(R.id.submitEntitlement).setOnClickListener(null);
        UpdateStockRequestDto updateStock = setBillData();
        Util.LoggingQueue(this, "Sales Summary", "Submit bills to Server:" + updateStock.toString());
        new InsertBillTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, updateStock);

    }

    private void updateData(UpdateStockRequestDto updateStock) {
        TransactionBaseDto base = TransactionBase.getInstance().getTransactionBase();
        if (base.getTransactionType() == TransactionTypes.SALE_QR_OTP_AUTHENTICATION || base.getTransactionType() == TransactionTypes.SALE_RMN_AUTHENTICATE) {
            updateStock.setRefNumber(appState.refId);
        }
        base.setBaseDto(updateStock);
        updateStocks(updateStock);
        if (networkConnection.isNetworkAvailable() && StringUtils.isNotEmpty(SessionId.getInstance().getSessionId())) {
            Util.LoggingQueue(this, "Sales Summary", "Sending bills to server:" + base.toString());
            BillUpdateToServer bill = new BillUpdateToServer(SalesSummaryActivity.this);
            bill.sendBillToServer(base);
        } else {
            Transaction trans = TransactionFactory.getTransaction(0);
            trans.process(this, base, updateStock);
        }
        String updateStockString = updateStock.getBillDto().getTransactionId();
        Util.LoggingQueue(this, "Sales Summary", "Moving to Success page ");
        Intent intent = new Intent(this, BillSuccessActivity.class);
        intent.putExtra("message", updateStockString);
        startActivity(intent);
        finish();
    }

    private void updateStocks(UpdateStockRequestDto updateStock) {
        List<BillItemProductDto> billItems = new ArrayList<>(updateStock.getBillDto().getBillItemDto());
        List<FPSStockDto> fpsStockDto = new ArrayList<FPSStockDto>();
        for (BillItemProductDto bItems : billItems) {
            FPSStockDto fpsStockDto1 = FPSDBHelper.getInstance(this).getAllProductStockDetails(bItems.getProductId());
            double quantity = fpsStockDto1.getQuantity();
            fpsStockDto1.setQuantity(quantity - bItems.getQuantity());
            fpsStockDto.add(fpsStockDto1);
            Util.LoggingQueue(this, "Sales Summary", "Inserting inside database");
            if (bItems.getQuantity() > 0)
                FPSDBHelper.getInstance(this).insertStockHistory(quantity, fpsStockDto1.getQuantity(), "SALES", bItems.getQuantity(), bItems.getProductId());
        }
        FPSDBHelper.getInstance(this).stockUpdate(fpsStockDto);
    }

    /**
     * Find the total amount of selected product
     */
    private String setTotalAmount() {
        double totalValue = 0.0f;
        for (EntitlementDTO entitlement : entitleList) {
            totalValue = totalValue + entitlement.getTotalPrice();
        }
        NumberFormat numberFormat = new DecimalFormat("#0.00");
        return numberFormat.format(totalValue);
    }

    /**
     * return User entitlement view
     *
     * @params entitle data, position of entitle
     */
    private View returnView(LayoutInflater entitle, EntitlementDTO data, int position) {
        View convertView = entitle.inflate(R.layout.view_entitlement_data, null);
        TextView entitlementName = (TextView) convertView.findViewById(R.id.entitlementName);
        TextView entitlementPurchased = (TextView) convertView.findViewById(R.id.entitlementPurchased);
        TextView entitlementRate = (TextView) convertView.findViewById(R.id.entitlementRate);
        TextView entitlementUnit = (TextView) convertView.findViewById(R.id.entitlementUnit);
        TextView amountOfSelection = (TextView) convertView.findViewById(R.id.amountOfSelection);
        String productName = data.getProductName();
        if (GlobalAppState.language.equals("ta") && StringUtils.isNotEmpty(data.getLproductName()))
            setTamilText(entitlementName, data.getLproductName());
        else
            entitlementName.setText(productName);

        if (GlobalAppState.language.equals("ta") && StringUtils.isNotEmpty(data.getProductUnit()))
            setTamilText(entitlementUnit, data.getLproductUnit());
        else
            entitlementUnit.setText(data.getProductUnit());
        NumberFormat formatter = new DecimalFormat("#0.000");
        entitlementPurchased.setText(formatter.format(data.getBought()));
        NumberFormat numberFormat = new DecimalFormat("#0.00");
        entitlementRate.setText(numberFormat.format(data.getProductPrice()));
        amountOfSelection.setText(numberFormat.format(data.getTotalPrice()));
        amountOfSelection.setId(position);

        return convertView;

    }

    /**
     * This method is used to edit the summary
     * navigate to sales entry page
     */
    private void editBillSummary() {
        startActivity(new Intent(this, SalesEntryActivity.class));
        Util.LoggingQueue(this, "Sales Summary", "Editing bill summary");
        finish();
    }

    @Override
    public void processMessage(Bundle message, ServiceListenerType what) {
    }

    @Override
    public void onBackPressed() {

    }

    private class InsertBillTask extends AsyncTask<UpdateStockRequestDto, Void, Boolean> {

        UpdateStockRequestDto updateStock;

        @Override
        protected void onPreExecute() {
            try {
                progressBar = new CustomProgressDialog(SalesSummaryActivity.this);
                progressBar.show();
            } catch (Exception e) {
                Log.e("Error in Progress", e.toString(), e);
            }
        }

        // automatically done on worker thread (separate from UI thread)
        protected Boolean doInBackground(final UpdateStockRequestDto... args) {
            updateStock = args[0];
            return FPSDBHelper.getInstance(SalesSummaryActivity.this).insertBill(args[0].getBillDto());
        }

        // can use UI thread here
        protected void onPostExecute(final Boolean success) {
            if (progressBar != null) {
                progressBar.dismiss();
            }
            if (success) {
                updateData(updateStock);
            } else {
                Util.messageBar(SalesSummaryActivity.this, getString(R.string.internalError));
                return;
            }
        }
    }


}
