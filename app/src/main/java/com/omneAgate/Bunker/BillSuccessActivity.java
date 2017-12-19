package com.omneAgate.Bunker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.omneAgate.DTO.BeneficiaryDto;
import com.omneAgate.DTO.BeneficiaryMemberDto;
import com.omneAgate.DTO.BillDto;
import com.omneAgate.DTO.UserDto.BillItemProductDto;
import com.omneAgate.DTO.EnumDTO.ServiceListenerType;
import com.omneAgate.Util.AddressForBeneficiary;
import com.omneAgate.Util.CustomProgressDialog;
import com.omneAgate.Util.FPSDBHelper;
import com.omneAgate.Util.SessionId;
import com.omneAgate.Util.Util;
import com.omneAgate.printer.BlueToothPrint;

import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class BillSuccessActivity extends BaseActivity {

    double totalCost = 0.0;

    MediaPlayer mediaPlayer;
    // android built in classes for bluetooth operations

    String oldCardNumber;

    BillDto billDto;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_bill_success);
        appState = (GlobalAppState) getApplication();
        String message = getIntent().getStringExtra("message");
        Util.LoggingQueue(this, "Sales Summary", "Bill success");
        billDto = FPSDBHelper.getInstance(this).getBillByTransactionId(message);
        setTamilText((TextView) findViewById(R.id.summarySubmit), getString(R.string.sales));
        setTamilText((TextView) findViewById(R.id.summaryEdit), getString(R.string.printBill));
        (findViewById(R.id.imageViewBack)).setVisibility(View.GONE);
        setUpInitialPage();
        playSound();
        roleFeaturePrintReceipt();
    }

    private void playSound() {
        try {
            Util.LoggingQueue(this, "Sales Summary", "Media player running");
            mediaPlayer = MediaPlayer.create(this, R.raw.beep);
            mediaPlayer.start();
        } catch (Exception e) {
            Log.e("BillSuccess", e.toString(), e);
        }
    }

    /*
*
* Initial Setup
*
* */
    private void setUpInitialPage() {
        appState = (GlobalAppState) getApplication();
        Util.LoggingQueue(this, "Sales Summary", "Setting up bill summary");
        setTamilText((TextView) findViewById(R.id.top_textView), R.string.bill_summary);
        setTamilText((TextView) findViewById(R.id.commoditys), R.string.commodity);
        setTamilText((TextView) findViewById(R.id.tvBillDetailUfcLabel), R.string.billDetailTxnBill);
        setTamilText((TextView) findViewById(R.id.tvBillDetailAmountLabel), R.string.billDetailAmountLabel);
        setTamilText((TextView) findViewById(R.id.tvBillDateLabel), R.string.ration_card_number1);
        setTamilText((TextView) findViewById(R.id.tvBillDetailHeadOfTheFamilyLabel), R.string.billDetailFamilyHeadOfTheFamilyLabel);
        setTamilText((TextView) findViewById(R.id.tvBillDetailAddressLabel), R.string.billDetailAddressLabel);
        setTamilText((TextView) findViewById(R.id.billDetailQuantity), R.string.billDetailQuantity);
        setTamilText((TextView) findViewById(R.id.billDetailProductPrice), R.string.billDetailProductPrice);

        findViewById(R.id.summarySubmit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying())
                    mediaPlayer.stop();
                Util.LoggingQueue(BillSuccessActivity.this, "Sales Summary", "Continue button called");
                startActivity(new Intent(BillSuccessActivity.this, SaleOrderActivity.class));
                finish();
            }
        });
        submitBills();
        setUpPopUpPage();


    }

    private void submitBills() {
        Util.LoggingQueue(this, "Sales Summary", " bill summary:" + billDto.toString());
        try {
            TextView tvUfc = (TextView) findViewById(R.id.tvBillDetailUfc);
            TextView tvTotalAmount = (TextView) findViewById(R.id.tvBillDetailAmount);
            tvUfc.setText(billDto.getTransactionId());
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS",Locale.getDefault());
            Date convertedDate = dateFormat.parse(billDto.getBillDate());
            dateFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.getDefault());
            tvTotalAmount.setText(dateFormat.format(convertedDate));
            new SearchBillTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, billDto.getBeneficiaryId());
        } catch (Exception e) {
            Log.e("BillSuccess", "Date Parse Error", e);
            Util.LoggingQueue(this, "Sales Summary", "Sales Error :" + e.toString());
        } finally {
            configureData(billDto);
        }
    }

    private String headOfFamily(List<BeneficiaryMemberDto> beneficiaryMember) {

        String head = "";
        Log.e("Bill success","print head of family :"+beneficiaryMember.toString());
        try {
            for (BeneficiaryMemberDto benef : beneficiaryMember) {
                if (benef.getRelName() !=null && benef.getRelName().equalsIgnoreCase("Family Head")) {
                    if (GlobalAppState.language.equalsIgnoreCase("ta") && StringUtils.isNotEmpty(benef.getLocalName())) {
                        return benef.getLocalName();
                    } else if (GlobalAppState.language.equalsIgnoreCase("en") && StringUtils.isNotEmpty(benef.getName())) {
                        return benef.getName();
                    } else {
                        return head;
                    }
                }
            }
        }catch (Exception e){
           Log.e("Exception ","head of family" +e.toString());
        }
        return head;

    }

    /*Data from server has been set inside this function*/
    private void configureData(BillDto bills) {
        try {
            LinearLayout fpsInwardLinearLayout = (LinearLayout) findViewById(R.id.listView_bill_detail);
            fpsInwardLinearLayout.removeAllViews();
            List<BillItemProductDto> billItems = new ArrayList<>(bills.getBillItemDto());
            for (BillItemProductDto items : billItems) {
                LayoutInflater lin = LayoutInflater.from(this);
                fpsInwardLinearLayout.addView(returnView(lin, items));
            }
        } catch (Exception e) {
            Util.LoggingQueue(this, "Error", e.toString());
            Log.e("BillSuccess", e.toString(), e);
        } finally {
            TextView tvBillDetailTotal = (TextView) findViewById(R.id.tvBillDetailTotal);
            NumberFormat formatter = new DecimalFormat("#0.00");
            tvBillDetailTotal.setText(" ₹ " + formatter.format(totalCost));
            setTamilText(tvBillDetailTotal, getString(R.string.total) + ":\u20B9 " + formatter.format(totalCost));
        }
    }

    /*User Bill Detail view*/
    private View returnView(LayoutInflater entitle, BillItemProductDto data) {
        View convertView = entitle.inflate(R.layout.adapter_bill_detail_activity, null);
        TextView entitlementName = (TextView) convertView.findViewById(R.id.entitlementName);
        TextView entitlementPurchased = (TextView) convertView.findViewById(R.id.entitlementPurchased);
        TextView entitlementUnit = (TextView) convertView.findViewById(R.id.entitlementUnit);
        TextView amountOfSelection = (TextView) convertView.findViewById(R.id.amountOfSelection);
        NumberFormat formatter = new DecimalFormat("#0.000");
        String unit = data.getProductUnit();
        entitlementUnit.setText(unit);
        if (GlobalAppState.language.equalsIgnoreCase("ta") && StringUtils.isNotEmpty(data.getLocalProductUnit())) {
            unit = data.getLocalProductUnit();
            setTamilText(entitlementUnit, unit);
        }
        if (GlobalAppState.language.equalsIgnoreCase("ta") && StringUtils.isNotEmpty(data.getLocalProductName()))
            setTamilText(entitlementName, data.getLocalProductName());
        else
            entitlementName.setText(data.getProductName());


        if (data.getQuantity() != null)
            entitlementPurchased.setText(formatter.format(data.getQuantity()));
        else
            entitlementPurchased.setText("0.000");
        formatter = new DecimalFormat("#0.00");
        double amountPerItem = data.getCost() * data.getQuantity();
        totalCost = totalCost + amountPerItem;
        amountOfSelection.setText(formatter.format(amountPerItem));
        return convertView;

    }

    @Override
    protected void processMessage(Bundle message, ServiceListenerType what) {

    }

    @Override
    public void onBackPressed() {
        if (mediaPlayer.isPlaying())
            mediaPlayer.stop();
        Util.LoggingQueue(this, "Sales Summary", "Back pressed");
        startActivity(new Intent(this, SaleOrderActivity.class));
        finish();
    }

    private class SearchBillTask extends AsyncTask<Long, Void, BeneficiaryDto> {

        @Override
        protected void onPreExecute() {
            try {
                progressBar = new CustomProgressDialog(BillSuccessActivity.this);
                progressBar.show();
            } catch (Exception e) {
                Log.e("Error in Progress", e.toString(), e);
            }
        }

        // automatically done on worker thread (separate from UI thread)
        protected BeneficiaryDto doInBackground(final Long... args) {
            return FPSDBHelper.getInstance(BillSuccessActivity.this).retrieveBeneficiary(args[0]);
        }

        // can use UI thread here
        protected void onPostExecute(final BeneficiaryDto beneficiaryDto) {
            if (progressBar != null) {
                progressBar.dismiss();
            }

            Log.e("syncdetail", beneficiaryDto.toString());
            TextView tvBillDate = (TextView) findViewById(R.id.tvBillDetailDate);
            TextView tvAddress = (TextView) findViewById(R.id.tvBillDetailAddress);
            TextView tvHeadOfTheFamily = (TextView) findViewById(R.id.tvBillDetailHeadOfTheFamily);
            String aReg = "";
            if(StringUtils.isNotEmpty(beneficiaryDto.getAregisterNum())){
                aReg = " / "+beneficiaryDto.getAregisterNum();
            }
            tvBillDate.setText(beneficiaryDto.getOldRationNumber()+aReg);
            oldCardNumber = beneficiaryDto.getOldRationNumber();
            Set<BeneficiaryMemberDto> beneficiaryMembers = beneficiaryDto.getBenefMembersDto();
            List<BeneficiaryMemberDto> beneficiaryMember = new ArrayList<>(beneficiaryMembers);
            if (beneficiaryMember.size() > 0) {
                setTamilText(tvAddress, AddressForBeneficiary.addressForBeneficiary(beneficiaryMember.get(0)));
                setTamilText(tvHeadOfTheFamily, headOfFamily(beneficiaryMember));
            }
        }
    }




    public void enableButton(){
        findViewById(R.id.summaryEdit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.summaryEdit).setOnClickListener(null);
                findViewById(R.id.summarySubmit).setOnClickListener(null);
                Log.e("Ssdfsdfsd","sdfjhgsdhjgkfhgkjdsfhgjkfddsfgdf");
                BlueToothPrint printing = new BlueToothPrint(BillSuccessActivity.this,billDto,oldCardNumber);
                printing.printCall();
            }
        });

        findViewById(R.id.summarySubmit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying())
                    mediaPlayer.stop();
                Util.LoggingQueue(BillSuccessActivity.this, "Sales Summary", "Continue button called");
                startActivity(new Intent(BillSuccessActivity.this, SaleOrderActivity.class));
                finish();
            }
        });
    }
    private void roleFeaturePrintReceipt() {
        boolean retrieveRollFeature = FPSDBHelper.getInstance(this).retrievePrintAllowed(SessionId.getInstance().getUserId());
            if (retrieveRollFeature) {
                findViewById(R.id.summaryEdit).setVisibility(View.VISIBLE);
                findViewById(R.id.summaryEdit).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(BillSuccessActivity.this,"Printing Process started",Toast.LENGTH_LONG).show();
                        findViewById(R.id.summaryEdit).setOnClickListener(null);
                        findViewById(R.id.summarySubmit).setOnClickListener(null);
                        BlueToothPrint printing = new BlueToothPrint(BillSuccessActivity.this,billDto,oldCardNumber);
                        printing.printCall();
                    }
                });
            } else {
                findViewById(R.id.summaryEdit).setVisibility(View.INVISIBLE);
            }

    }


}

