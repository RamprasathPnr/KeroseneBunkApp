package com.omneAgate.Bunker;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.zxing.client.android.Intents;
import com.omneAgate.DTO.EnumDTO.RequestType;
import com.omneAgate.DTO.EnumDTO.ServiceListenerType;
import com.omneAgate.DTO.OTPTransactionDto;
import com.omneAgate.DTO.QROTPRequestDto;
import com.omneAgate.DTO.QROTPResponseDto;
import com.omneAgate.DTO.QRTransactionResponseDto;
import com.omneAgate.DTO.TransactionBaseDto;
import com.omneAgate.DTO.TransactionTypes;
import com.omneAgate.DTO.UpdateStockRequestDto;
import com.omneAgate.TransactionController.SMSListener;
import com.omneAgate.TransactionController.Transaction;
import com.omneAgate.TransactionController.TransactionFactory;
import com.omneAgate.Util.AndroidDeviceProperties;
import com.omneAgate.Util.BeneficiarySalesQRTransaction;
import com.omneAgate.Util.Constants.FPSDBConstants;
import com.omneAgate.Util.CustomProgressDialog;
import com.omneAgate.Util.EntitlementResponse;
import com.omneAgate.Util.FPSDBHelper;
import com.omneAgate.Util.NetworkConnection;
import com.omneAgate.Util.SessionId;
import com.omneAgate.Util.TransactionBase;
import com.omneAgate.Util.TransactionMessage;
import com.omneAgate.Util.Util;
import com.omneAgate.service.HttpClientWrapper;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;



//Beneficiary Activity to check Beneficiary Activation
public class QRCodeSalesActivity extends BaseActivity implements SMSListener, View.OnClickListener {

    final Handler handler = new Handler();
    TransactionBaseDto transaction;          //Transaction base DTO
    Timer timer;
    TimerTask timerTask;
    String number = "";
    private int count = 0;//Retry count
    private String qrCodeData = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_qrcode_otp);
        networkConnection = new NetworkConnection(this);
        httpConnection = new HttpClientWrapper();
        appState = (GlobalAppState) getApplication();
        transaction = new TransactionBaseDto();
        setUpInitialScreen();
    }



    private void setUpInitialScreen() {
        setUpPopUpPage();
        Util.LoggingQueue(this, "QRcode sales", "Setting up QRcode sales activity");
        findViewById(R.id.imageViewBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        LinearLayout mobileOtp = (LinearLayout) findViewById(R.id.myMobileOTPBackground);
        mobileOtp.removeAllViews();
        LayoutInflater vi = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = vi.inflate(R.layout.view_received_otp, null);
        mobileOtp.addView(view);
        setTamilText((TextView) findViewById(R.id.top_textView), R.string.sale_entry_activity);
        transaction.setType("com.omneagate.rest.dto.QRRequestDto");
        if (SessionId.getInstance().isQrOTPEnabled()) {
            transaction.setTransactionType(TransactionTypes.SALE_QR_OTP_AUTHENTICATION);
        } else {
            transaction.setTransactionType(TransactionTypes.BUNK_SALE_QR_OTP_DISABLED);
        }
        TransactionBase.getInstance().setTransactionBase(transaction);
        launchQRScanner();
    }


    /**
     * Calling QR Scanner
     */
    private void launchQRScanner() {
       /* Util.LoggingQueue(this, "QRcode sales", "QR scanner called");
        IntentIntegrator.initiateScan(this, R.layout.activity_capture,
                R.id.viewfinder_view, R.id.preview_view, true);*/
        String packageString = getApplicationContext().getPackageName();
        Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        intent.setPackage(packageString);
        intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
        startActivityForResult(intent, 0);
    }

    /**
     * QR code response received for card
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

          /*  case IntentIntegrator.REQUEST_CODE:
                try {
                    IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode,
                            resultCode, data);
                    Util.LoggingQueue(this, "QRcode sales", "QR response:" + scanResult.getContents());
                    qrResponse(scanResult.getContents());
                } catch (Exception e) {
                    Util.messageBar(QRCodeSalesActivity.this, getString(R.string.qrCodeInvalid));
                    Util.LoggingQueue(this, "QRcode sales", "QR exception called:" + e.toString());
                    Log.e("QRCodeSalesActivity", "Empty", e);
                }
                break;*/

            case 0:
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        String contents = data.getStringExtra(Intents.Scan.RESULT);
                        Log.e("EncryptedUFC",contents);
                        qrResponse(contents);
                    } catch (Exception e) {
                        Util.messageBar(this, getString(R.string.qrCodeInvalid));
                        Util.LoggingQueue(this, "QRcode sales", "QR exception called:" + e.toString());
                        Log.e("QRCodeSalesActivity", "Empty", e);
                    }
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    Log.e(QRCodeSalesActivity.class.getSimpleName(),"Scan cancelled");
                    onBackPressed();
                }
            default:
                break;
        }
    }

    //Response from QR reader
    private void qrResponse(String result) {
        String languageCode = FPSDBHelper.getInstance(this).getMasterData("language");
        Util.changeLanguage(this, languageCode);
        GlobalAppState.language = languageCode;
        if (result == null) {
            Util.LoggingQueue(this, "Result null", "Back page");
            startActivity(new Intent(this, SaleOrderActivity.class));
            finish();
        } else {
            String qrCode = Util.DecryptedBeneficiary(this, result);
            if (StringUtils.isEmpty(qrCode)) {
                Util.LoggingQueue(this, "QRcode invalid", "back page called");
                startActivity(new Intent(this, SaleOrderActivity.class));
                finish();
                return;
            }
            String lines[] = result.split("\\r?\\n");
            getEntitlement(lines[0]);
        }
    }

    /**
     * Send FPS_ID and QRCode to get entitlement
     *
     * @params qrCode received from card
     */
    private void getEntitlement(String qrCodeString) {
        try {

            BeneficiarySalesQRTransaction beneficiary = new BeneficiarySalesQRTransaction(this);
            QRTransactionResponseDto qrCodeResponseReceived = beneficiary.getBeneficiaryDetails(qrCodeString);
            if (qrCodeResponseReceived != null)
                Util.LoggingQueue(this, "QRTransactionResponseDto", qrCodeResponseReceived.toString());
            if (beneficiary.getBeneficiaryDetails(qrCodeString) != null && qrCodeResponseReceived.getEntitlementList() != null && qrCodeResponseReceived.getEntitlementList().size() > 0) {
                qrCodeResponseReceived.setMode('Q');
                EntitlementResponse.getInstance().setQrcodeTransactionResponseDto(qrCodeResponseReceived);
                if (SessionId.getInstance().isQrOTPEnabled()) {
                  //  getOTPFromServer(qrCodeString);
                } else {
                    Log.e("entitltementqty",""+qrCodeResponseReceived.getEntitlementList().toString());
                    startActivity(new Intent(this, SalesEntryActivity.class));
                    finish();

                }

            } else {
                errorNavigation(getString(R.string.fpsBeneficiaryMismatch));

            }
        } catch (Exception e) {
            Util.LoggingQueue(this, "Error", e.getStackTrace().toString());
            Log.e("QRCodeSalesActivity", e.toString(), e);
            errorNavigation(getString(R.string.qrCodeInvalid));
        }
    }

    /*private void getEntitlement(String qrCodeString) {
        progressBar = new CustomProgressDialog(this);
        try {
            progressBar.show();
            Log.e("qr_vased_qrcode",""+qrCodeString);
            BeneficiaryDto benef = FPSDBHelper.getInstance(this).beneficiaryFromUFCCode(qrCodeString);
            if (benef != null) {
                BeneficiarySalesTransaction beneficiary = new BeneficiarySalesTransaction(this);
                Util.LoggingQueue(this, "Entitlement", "Calculating entitlement");
                QRTransactionResponseDto qrCodeResponseReceived = beneficiary.getBeneficiaryDetails(qrCodeString);
                if (qrCodeResponseReceived != null)
                    Util.LoggingQueue(this, "QRTransactionResponseDto", qrCodeResponseReceived.toString());
                if (beneficiary.getBeneficiaryDetails(qrCodeString) != null && qrCodeResponseReceived.getEntitlementList() != null && qrCodeResponseReceived.getEntitlementList().size() > 0) {
                    qrCodeResponseReceived.setMode('A');
                    qrCodeResponseReceived.setRegistered(true);
                    EntitlementResponse.getInstance().setQrcodeTransactionResponseDto(qrCodeResponseReceived);
                   *//* startActivity(new Intent(this, SalesEntryActivity.class));
                    finish();*//*
                } else if (beneficiary.getBeneficiaryDetails(qrCodeString) != null && qrCodeResponseReceived.getEntitlementList() != null && qrCodeResponseReceived.getEntitlementList().size() == 0) {

                    errorNavigation(getString(R.string.entitlemnt_finished));
                } else {

                    errorNavigation(getString(R.string.fpsBeneficiaryMismatch));
                }
            } else {
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
    }*/

    @Override
    protected void processMessage(Bundle message, ServiceListenerType what) {
        if (progressBar != null) {
            progressBar.dismiss();
        }
        switch (what) {
            case QR_CODE:
                if (progressBar != null) {
                    progressBar.dismiss();
                }
                QRCodeResponseReceived(message);
                break;
            default:
                if (progressBar != null) {
                    progressBar.dismiss();
                }
                break;
        }
    }


    //QrCode response from server for respective card
    private void QRCodeResponseReceived(Bundle message) {
        try {
            String response = message.getString(FPSDBConstants.RESPONSE_DATA);
            Util.LoggingQueue(this, "OTP received", response);
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            QROTPResponseDto qrCodeResponseReceived = gson.fromJson(response,
                    QROTPResponseDto.class);
            if (qrCodeResponseReceived.getStatusCode() == 0) {
                qrResponseData(qrCodeResponseReceived);
            } else {

                String messages = Util.messageSelection(FPSDBHelper.getInstance(this).retrieveLanguageTable(qrCodeResponseReceived.getStatusCode()));
                Util.LoggingQueue(this, "Error in OTP", messages);
                errorNavigation(messages);
            }
        } catch (Exception e) {
            Log.e("QRCodeSalesActivity", e.toString(), e);
            Util.LoggingQueue(this, "Error in OTP", e.toString());
        }

    }

    /**
     * send request to get otp from server
     *
     * @param qrCodeString
     */
    private void getOTPFromServer(String qrCodeString) {
        try {
            QROTPRequestDto qrCode = new QROTPRequestDto();
            AndroidDeviceProperties devices = new AndroidDeviceProperties(this);
            qrCode.setDeviceId(devices.getDeviceProperties().getSerialNumber());
            qrCode.setUfc(qrCodeString);
            qrCodeData = qrCodeString;
            TransactionBaseDto base = new TransactionBaseDto();
            base.setType("com.omneagate.rest.dto.QRRequestDto");
            base.setTransactionType(TransactionTypes.SALE_QR_OTP_GENERATE);
            base.setBaseDto(qrCode);
            UpdateStockRequestDto update = new UpdateStockRequestDto();
            update.setUfc(qrCodeString);
            if (networkConnection.isNetworkAvailable()) {
                if (SessionId.getInstance().getSessionId().length() > 0) {
                    String url = "/transaction/process";
                    String qrCodes = new Gson().toJson(base);
                    Util.LoggingQueue(this, "Otp for qr", qrCodes);
                    StringEntity se = new StringEntity(qrCodes, HTTP.UTF_8);
                    progressBar = new CustomProgressDialog(this);
                    progressBar.setCancelable(false);
                    progressBar.show();
                    httpConnection.sendRequest(url, null, ServiceListenerType.QR_CODE,
                            SyncHandler, RequestType.POST, se, this);
                } else {
                    sendBySMS(base, update);
                }
            } else {
                sendBySMS(base, update);
            }
        } catch (Exception e) {
            Util.LoggingQueue(this, "Error", e.toString());
            Log.e("QRCodeSalesActivity", e.toString(), e);
        }
    }

    private void sendBySMS(TransactionBaseDto base, UpdateStockRequestDto update) {
        GlobalAppState.listener = this;
        checkMessage();
        progressBar = new CustomProgressDialog(this);
        progressBar.setCancelable(false);
        progressBar.show();
        Transaction trans = TransactionFactory.getTransaction(0);
        trans.process(this, base, update);
    }

    private void removeData(QROTPResponseDto qrResponse) {
        progressBar.dismiss();
        Map<String, String> data = TransactionMessage.getInstance().getTransactionMessage();
        String value = data.get(qrResponse.getUfc());
        if (StringUtils.isNotEmpty(qrResponse.getUfc())) {
            data.remove(qrResponse.getUfc());
            qrResponseData(qrResponse);
        } else {
            errorNavigation(getString(R.string.connectionError));
        }
    }


    /*
* After qrCode sent to server and response received this function calls
* */
    private void qrResponseData(final QROTPResponseDto qrCodeResponseReceived) {

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
        findViewById(R.id.imageView5).setOnClickListener(this);
        findViewById(R.id.buttonNeedOTP).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkOTPValidate(qrCodeResponseReceived)) {
                    otpDialogResult(qrCodeResponseReceived);
                }
            }
        });
    }

    private void removeNumber() {
        if (number.length() > 0) {
            number = number.substring(0, number.length() - 1);
        } else {
            number = "";
        }
        setText();
    }

    private void checkMessage() {
        timer = new Timer();
        initializeTimerTask();
        //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
        timer.schedule(timerTask, 60000);
    }

    public void initializeTimerTask() {

        timerTask = new TimerTask() {
            public void run() {

                //use a handler to run a toast that shows the current timestamp
                handler.post(new Runnable() {
                    public void run() {
                        stopTimerTask(new QROTPResponseDto());
                    }
                });
            }
        };
    }

    public void stopTimerTask(QROTPResponseDto qrResponse) {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        GlobalAppState.listener = null;
        removeData(qrResponse);
    }


    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, SaleOrderActivity.class));
        finish();
    }


    @Override
    public void smsReceived(UpdateStockRequestDto stockRequestDto) {
        appState.refId = stockRequestDto.getRefNumber();
        QROTPResponseDto qrResponse = new QROTPResponseDto();
        OTPTransactionDto otpData = new OTPTransactionDto();
        otpData.setId(stockRequestDto.getOtpId());
        otpData.setOtp(stockRequestDto.getOtp());
        qrResponse.setOtpTransactionDto(otpData);
        qrResponse.setUfc(stockRequestDto.getUfc());
        qrResponse.setReferenceId(stockRequestDto.getRefNumber());
        stopTimerTask(qrResponse);
    }

    //Response from otp received is true
    private void otpDialogResult(QROTPResponseDto qrResponse) {
        BeneficiarySalesQRTransaction beneficiary = new BeneficiarySalesQRTransaction(this);
        QRTransactionResponseDto qrCodeResponseReceived = beneficiary.getBeneficiaryDetails(qrCodeData);
        appState.refId = qrResponse.getReferenceId();
        Util.LoggingQueue(this, "Calculating Entitlement", qrCodeResponseReceived.toString());
        if (qrCodeResponseReceived != null && qrCodeResponseReceived.getEntitlementList() != null && qrCodeResponseReceived.getEntitlementList().size() > 0) {
            qrCodeResponseReceived.setMode('A');
            EntitlementResponse.getInstance().setQrcodeTransactionResponseDto(qrCodeResponseReceived);
            startActivity(new Intent(this, SalesEntryActivity.class));
            finish();
        } else {
            Util.LoggingQueue(this, "Error in calculating Entitlement", "Internal error");
            errorNavigation(getString(R.string.internalError));
        }
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

    private void addNumber(String text) {
        try {
            if (number.length() >= 7) {
                return;
            }
            number = number + text;
            setText();
        } catch (Exception e) {
            Log.e("QRCodeSalesActivity", e.toString(), e);
        }
    }

    private void setText() {
        ((TextView) findViewById(R.id.mobileNumberOTP)).setText(number);
    }

    /**
     * OTP validation
     *
     * @param qrResponse received from server
     *                   return true if otp equals user entry else false
     */
    private boolean checkOTPValidate(QROTPResponseDto qrResponse) {
        String oTP = number;
        Util.LoggingQueue(this, "Checking OTP", "OTP check");
        if (StringUtils.isEmpty(oTP)) {
            Util.LoggingQueue(this, "Error in OTP", "Empty OTP page");
            return false;
        }
        if (oTP.equals(qrResponse.getOtpTransactionDto().getOtp())) {
            return true;
        } else if (count == 3) {
            Util.LoggingQueue(this, "Error in OTP", "Retry count exceed");
            Util.messageBar(this, getString(R.string.retryCount));
            errorNavigation(getString(R.string.retryCount));
        } else {
            Util.LoggingQueue(this, "Error in OTP", "Invalid page");
            Util.messageBar(this, getString(R.string.mobileOTPWrong));
        }

        count++;
        return false;
    }


}