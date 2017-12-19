package com.omneAgate.Bunker;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.omneAgate.DTO.BeneficiaryDto;
import com.omneAgate.DTO.BeneficiaryMemberDto;
import com.omneAgate.DTO.BillDto;
import com.omneAgate.DTO.EnumDTO.ServiceListenerType;
import com.omneAgate.DTO.UpdateStockRequestDto;
import com.omneAgate.DTO.UserDto.BillItemProductDto;
import com.omneAgate.Util.AddressForBeneficiary;
import com.omneAgate.Util.CustomProgressDialog;
import com.omneAgate.Util.FPSDBHelper;
import com.omneAgate.Util.Util;
import com.omneAgate.printer.PrintBillData;

import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;


/**
 * Created for unsync bills
 */
public class UnsyncBillDetailActivity extends BaseActivity {

    double totalCost = 0.0;

    BillDto billDto;

    UpdateStockRequestDto updateStockRequestDto;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_bill_success);
        appState = (GlobalAppState) getApplication();
        String message = getIntent().getExtras().getString("billData");
        billDto = new Gson().fromJson(message, BillDto.class);
        setTamilText((TextView) findViewById(R.id.summarySubmit), getString(R.string.sales));
        setTamilText((TextView) findViewById(R.id.summaryEdit), getString(R.string.printBill));
        updateStockRequestDto = new UpdateStockRequestDto();
        setUpInitialPage();
    }



    /*
*
* Initial Setup
*
* */
    private void setUpInitialPage() {
        setUpPopUpPage();
        Util.LoggingQueue(this, "Bill search Details", "Setting up main page");
        appState = (GlobalAppState) getApplication();
        setTamilText((TextView) findViewById(R.id.top_textView), R.string.bill_summary);
        setTamilText((TextView) findViewById(R.id.commoditys), R.string.commodity);
        setTamilText((TextView) findViewById(R.id.tvBillDetailUfcLabel), R.string.billDetailTxnBill);
        setTamilText((TextView) findViewById(R.id.tvBillDetailAmountLabel), R.string.billDetailAmountLabel);
        setTamilText((TextView) findViewById(R.id.tvBillDateLabel), R.string.ration_card_number1);
        setTamilText((TextView) findViewById(R.id.tvBillDetailHeadOfTheFamilyLabel), R.string.billDetailFamilyHeadOfTheFamilyLabel);
        setTamilText((TextView) findViewById(R.id.tvBillDetailAddressLabel), R.string.billDetailAddressLabel);
        setTamilText((TextView) findViewById(R.id.billDetailQuantity), R.string.billDetailQuantity);
        setTamilText((TextView) findViewById(R.id.billDetailProductPrice), R.string.billDetailProductPrice);
        String myPairedDevice = FPSDBHelper.getInstance(this).getMasterData("printer");

        if (myPairedDevice != null) {
            findViewById(R.id.summaryEdit).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Util.LoggingQueue(UnsyncBillDetailActivity.this, "Bill search Details", "Printer called");
                    PrintBillData print = new PrintBillData(UnsyncBillDetailActivity.this, updateStockRequestDto);
                    print.printBill();
                }
            });
        } else {
            findViewById(R.id.summaryEdit).setVisibility(View.INVISIBLE);
        }

        findViewById(R.id.summarySubmit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.LoggingQueue(UnsyncBillDetailActivity.this, "Bill search Details", "Continue button called");
                onBackPressed();
            }
        });
        submitBills();
    }

    private void submitBills() {
        try {
            TextView tvUfc = (TextView) findViewById(R.id.tvBillDetailUfc);
            TextView tvTotalAmount = (TextView) findViewById(R.id.tvBillDetailAmount);
            tvUfc.setText(billDto.getTransactionId());
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS",Locale.getDefault());
            Date convertedDate = dateFormat.parse(billDto.getBillDate());
            dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
            tvTotalAmount.setText(dateFormat.format(convertedDate));
            configureData(billDto);
            new SearchBillTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, billDto.getBeneficiaryId());
            Util.LoggingQueue(UnsyncBillDetailActivity.this, "Bill search Details", "Bills:" + billDto.toString());
        } catch (Exception e) {
            Util.LoggingQueue(this, "Bill search Details", "Error:" + e.toString());
        } finally {
            findViewById(R.id.imageViewBack).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                    Util.LoggingQueue(UnsyncBillDetailActivity.this, "Bill search Details", "Back pressed Calling");
                }
            });
        }
    }

    private String headOfFamily(Set<BeneficiaryMemberDto> beneficiaryMember) {
        String head = "";
        for (BeneficiaryMemberDto benef : beneficiaryMember) {
            if (benef.getRelName()!=null && benef.getRelName().equalsIgnoreCase("Family Head")) {
                if (GlobalAppState.language.equalsIgnoreCase("ta") && StringUtils.isNotEmpty(benef.getLocalName())) {
                    return benef.getLocalName();
                } else if (GlobalAppState.language.equalsIgnoreCase("en") && StringUtils.isNotEmpty(benef.getName())) {
                    return benef.getName();
                }
            }
        }
        return head;

    }


    /*Data from server has been set inside this function*/
    private void configureData(BillDto bills) {
        try {
            LinearLayout fpsInwardLinearLayout = (LinearLayout) findViewById(R.id.listView_bill_detail);
            fpsInwardLinearLayout.removeAllViews();
            List<BillItemProductDto> billItems = FPSDBHelper.getInstance(this).getAllBillItems(bills.getTransactionId());
            Util.LoggingQueue(this, "Bill search Details", "Bill Items:" + billItems.toString());
            for (BillItemProductDto items : billItems) {
                LayoutInflater lin = LayoutInflater.from(this);
                fpsInwardLinearLayout.addView(returnView(lin, items));
            }
            TextView tvBillDetailTotal = (TextView) findViewById(R.id.tvBillDetailTotal);
            NumberFormat formatter = new DecimalFormat("#0.00");
            tvBillDetailTotal.setText("\u20B9 " + formatter.format(totalCost));
            setTamilText(tvBillDetailTotal, getString(R.string.total) + ":\u20B9 " + formatter.format(totalCost));
        } catch (Exception e) {
            Util.LoggingQueue(this, "Error", e.toString());
            Log.e("BillDetailActivity", e.toString(), e);
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
        Intent intent = new Intent(this, UnsyncBillActivity.class);
        startActivity(intent);
        finish();
    }

    private class SearchBillTask extends AsyncTask<Long, Void, BeneficiaryDto> {

        @Override
        protected void onPreExecute() {
            try {
                progressBar = new CustomProgressDialog(UnsyncBillDetailActivity.this);
                progressBar.show();
            } catch (Exception e) {
                Log.e("Progress bar", e.toString(), e);
            }
        }

        // automatically done on worker thread (separate from UI thread)
        protected BeneficiaryDto doInBackground(final Long... args) {
            return FPSDBHelper.getInstance(UnsyncBillDetailActivity.this).retrieveBeneficiary(args[0]);
        }

        // can use UI thread here
        protected void onPostExecute(final BeneficiaryDto beneficiaryDto) {
            if (progressBar != null) {
                progressBar.dismiss();
            }
            TextView tvBillDate = (TextView) findViewById(R.id.tvBillDetailDate);
            TextView tvAddress = (TextView) findViewById(R.id.tvBillDetailAddress);
            TextView tvHeadOfTheFamily = (TextView) findViewById(R.id.tvBillDetailHeadOfTheFamily);
            String aReg = "";
            if(StringUtils.isNotEmpty(beneficiaryDto.getAregisterNum())){
                aReg = " / "+beneficiaryDto.getAregisterNum();
            }
            tvBillDate.setText(beneficiaryDto.getOldRationNumber()+aReg);
            Set<BeneficiaryMemberDto> beneficiaryMembers = beneficiaryDto.getBenefMembersDto();
            List<BeneficiaryMemberDto> beneficiaryMember = new ArrayList<>(beneficiaryMembers);
            if (beneficiaryMember.size() > 0) {
                setTamilText(tvAddress,  AddressForBeneficiary.addressForBeneficiary(beneficiaryMember.get(0)));
                setTamilText(tvHeadOfTheFamily, headOfFamily(beneficiaryMembers));
            }
        }
    }


}
