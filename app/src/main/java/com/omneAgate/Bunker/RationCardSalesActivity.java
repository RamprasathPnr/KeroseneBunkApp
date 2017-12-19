package com.omneAgate.Bunker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.omneAgate.DTO.BeneficiaryDto;
import com.omneAgate.DTO.EnumDTO.ServiceListenerType;
import com.omneAgate.DTO.QRTransactionResponseDto;
import com.omneAgate.DTO.TransactionBaseDto;
import com.omneAgate.DTO.TransactionTypes;
import com.omneAgate.Util.BeneficiarySalesTransaction;
import com.omneAgate.Util.CustomProgressDialog;
import com.omneAgate.Util.EntitlementResponse;
import com.omneAgate.Util.FPSDBHelper;
import com.omneAgate.Util.NetworkConnection;
import com.omneAgate.Util.TransactionBase;
import com.omneAgate.Util.Util;
import com.omneAgate.Bunker.dialog.RationCardListAdapter;
import com.omneAgate.service.HttpClientWrapper;

import java.util.ArrayList;
import java.util.List;

//Beneficiary Activity to check Beneficiary Activation
public class RationCardSalesActivity extends BaseActivity implements View.OnClickListener {

    private final static String TAG = RationCardSalesActivity.class.getCanonicalName();

    TransactionBaseDto transaction;          //Transaction base DTO

    String number = "";

    TextView suffixCard;

    ListView listViewSearch;

    RationCardListAdapter rationCardListAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "Ration Execution");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.ration_card_search);
        networkConnection = new NetworkConnection(this);
        httpConnection = new HttpClientWrapper();
        appState = (GlobalAppState) getApplication();
        transaction = new TransactionBaseDto();
        setUpInitialScreen();
    }

    private void setUpInitialScreen() {
        setUpPopUpPage();
        suffixCard = (TextView) findViewById(R.id.thirdText);
        Util.LoggingQueue(this, "Ration card Sales", "Setting up Ration card sales activity");
        findViewById(R.id.imageViewBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        setTamilText((TextView) findViewById(R.id.card_num_search), R.string.card_number_input);
        setTamilText((TextView) findViewById(R.id.select_ration_card_number), R.string.select_ration_card_number);
        setTamilText((TextView) findViewById(R.id.top_textView), R.string.sale_entry_activity);
        transaction.setType("com.omneagate.rest.dto.QRRequestDto");
        findViewById(R.id.button_one).setOnClickListener(this);
        findViewById(R.id.button_two).setOnClickListener(this);
        findViewById(R.id.button_three).setOnClickListener(this);
        findViewById(R.id.button_four).setOnClickListener(this);
        findViewById(R.id.button_five).setOnClickListener(this);
        findViewById(R.id.button_six).setOnClickListener(this);
        findViewById(R.id.button_seven).setOnClickListener(this);
        findViewById(R.id.button_eight).setOnClickListener(this);
        findViewById(R.id.button_nine).setOnClickListener(this);
        findViewById(R.id.button_zero).setOnClickListener(this);
        findViewById(R.id.button_bkSp).setOnClickListener(this);
        List<BeneficiaryDto> benef = new ArrayList<BeneficiaryDto>();
        rationCardListAdapter = new RationCardListAdapter(this, benef);
        listViewSearch = (ListView) findViewById(R.id.listView_search);
        listViewSearch.setAdapter(rationCardListAdapter);
        transaction.setTransactionType(TransactionTypes.BUNK_SALE_QR_OTP_DISABLED);
        TransactionBase.getInstance().setTransactionBase(transaction);
    }


    /**
     * Send FPS_ID and QRCode to get entitlement
     *
     * @params qrCode received from card
     */
    private void getEntitlement(String qrCodeString,long fpsid) {
        progressBar = new CustomProgressDialog(this);
        try {
            progressBar.show();
            BeneficiaryDto benef = FPSDBHelper.getInstance(this).beneficiaryFromOldCard(qrCodeString,fpsid);
            if (benef != null) {
                BeneficiarySalesTransaction beneficiary = new BeneficiarySalesTransaction(this);
                Log.e("Entitlement", "Calculating entitlement");
                /*--------------------------------------------------------------------*/
                QRTransactionResponseDto qrCodeResponseReceived = beneficiary.getBeneficiaryDetails(qrCodeString,fpsid);

                if (qrCodeResponseReceived != null)
                    Log.e("QRTransactionResponse", "qrCodeResponseReceived tostring" + qrCodeResponseReceived.toString());
                if (beneficiary.getBeneficiaryDetails(qrCodeString,fpsid) != null && qrCodeResponseReceived.getEntitlementList() != null && qrCodeResponseReceived.getEntitlementList().size() > 0) {
                    if (networkConnection.isNetworkAvailable()) {
                        qrCodeResponseReceived.setMode('D');
                    } else {
                        qrCodeResponseReceived.setMode('F');
                    }

                    Util.LoggingQueue(RationCardSalesActivity.this, "Ration card Sales", "Moving to Sales Entry Page");
                    qrCodeResponseReceived.setRegistered(true);
                    EntitlementResponse.getInstance().setQrcodeTransactionResponseDto(qrCodeResponseReceived);
                    Log.e("entitltement_demo", "" + qrCodeResponseReceived.getEntitlementList().toString());
                    startActivity(new Intent(this, SalesEntryActivity.class));
                    finish();
                } else if (beneficiary.getBeneficiaryDetails(qrCodeString,fpsid) != null && qrCodeResponseReceived.getEntitlementList() != null && qrCodeResponseReceived.getEntitlementList().size() == 0) {
                    Util.LoggingQueue(RationCardSalesActivity.this, "Ration card Sales", "Beneficiary Entitlement details not available");
                    errorNavigation(getString(R.string.entitlemnt_finished));
                } else {
                    Util.LoggingQueue(RationCardSalesActivity.this, "Ration card Sales", "Beneficiary Entitlement details not available");
                    errorNavigation(getString(R.string.fpsBeneficiaryMismatch));
                }
            } else {
                Util.LoggingQueue(RationCardSalesActivity.this, "Ration card Sales", "Beneficiary Data is not available in db");
                errorNavigation(getString(R.string.fpsBeneficiaryMismatch));
            }
        } catch (Exception e) {
            Util.LoggingQueue(this, "Error", e.getStackTrace().toString());
            Log.e("RationCardSalesActivity", e.toString(), e);
            errorNavigation(getString(R.string.invalid_card_no));
        } finally {
            if (progressBar != null)
                progressBar.dismiss();
        }
    }

    @Override
    protected void processMessage(Bundle message, ServiceListenerType what) {
        switch (what) {
            default:
                if (progressBar != null) {
                    progressBar.dismiss();
                }
                break;
        }
    }


    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, SaleOrderActivity.class));
        Util.LoggingQueue(this, "Ration card Sales", "On Back pressed Called");
        finish();
    }


    //Handler for 5 secs
    private void errorNavigation(String messages) {
        Intent intent = new Intent(this, SuccessFailureSalesActivity.class);
        Util.LoggingQueue(this, "Error in QRcode", "Navigating to error page");
        intent.putExtra("message", messages);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_one:
                addNumber("1");
                break;
            case R.id.button_two:
                addNumber("2");
                break;
            case R.id.button_three:
                addNumber("3");
                break;
            case R.id.button_four:
                addNumber("4");
                break;
            case R.id.button_five:
                addNumber("5");
                break;
            case R.id.button_six:
                addNumber("6");
                break;
            case R.id.button_seven:
                addNumber("7");
                break;
            case R.id.button_eight:
                addNumber("8");
                break;
            case R.id.button_nine:
                addNumber("9");
                break;
            case R.id.button_zero:
                addNumber("0");
                break;
            case R.id.button_bkSp:
                removeNumber();
                break;
            case R.id.imageView5:
                number = "";
                setText();
                break;
            default:
                break;
        }

    }

    private void removeNumber() {
        if (number.length() > 0) {
            number = number.substring(0, number.length() - 1);
        } else {
            number = "";
        }
        setText();
    }

    private void addNumber(String text) {
        try {
            if (number.length() >= 4) {
                return;
            }
            number = number + text;
            setText();
        } catch (Exception e) {
            Log.e("RationCardSalesActivity", e.toString(), e);
        }
    }

    private void setText() {
        suffixCard.setText(number);
        Util.LoggingQueue(this, "Ration card Sales", "Entering the number:" + number);
        if (number.length() == 4) {
            new BeneficiarySearchTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, number);
        } else {
            List<BeneficiaryDto> benef = new ArrayList<BeneficiaryDto>();
            findViewById(R.id.noRecordsFound).setVisibility(View.GONE);
            rationCardListAdapter = new RationCardListAdapter(this, benef);
            listViewSearch.setAdapter(rationCardListAdapter);
        }
    }

    private class BeneficiarySearchTask extends AsyncTask<String, Void, List<BeneficiaryDto>> {

        @Override
        protected void onPreExecute() {
            try {
                progressBar = new CustomProgressDialog(RationCardSalesActivity.this);
                progressBar.show();
            } catch (Exception e) {
                Log.e("Progress Bar", e.toString(), e);
            }
        }

        // automatically done on worker thread (separate from UI thread)
        protected List<BeneficiaryDto> doInBackground(final String... args) {
            return FPSDBHelper.getInstance(RationCardSalesActivity.this).retrieveAllBeneficiary(number);
        }

        // can use UI thread here
        protected void onPostExecute(final List<BeneficiaryDto> beneficiary) {
            if (progressBar != null) {
                progressBar.dismiss();
            }
            if (beneficiary != null && beneficiary.size() > 0) {
                Util.LoggingQueue(RationCardSalesActivity.this, "Ration card Sales", "Beneficiary Length:" + beneficiary.size());
                listViewSearch.setVisibility(View.VISIBLE);
                findViewById(R.id.noRecordsFound).setVisibility(View.GONE);
                rationCardListAdapter = new RationCardListAdapter(RationCardSalesActivity.this, beneficiary);
                listViewSearch.setAdapter(rationCardListAdapter);
                listViewSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        listViewSearch.setOnItemClickListener(null);
                        Log.e("** Ration ***","Old RationCard Number "+beneficiary.get(position).getOldRationNumber() + "FPs ID "+beneficiary.get(position).getFpsId());
                        getEntitlement(beneficiary.get(position).getOldRationNumber(),beneficiary.get(position).getFpsId());
                    }
                });
            } else {
                Util.LoggingQueue(RationCardSalesActivity.this, "Ration card Sales", "Beneficiary Length is Zero");
                listViewSearch.setVisibility(View.GONE);
                findViewById(R.id.noRecordsFound).setVisibility(View.VISIBLE);
                setTamilText(((TextView) findViewById(R.id.noRecordsFound)), getString(R.string.noRecordsFound));
            }
        }
    }


}