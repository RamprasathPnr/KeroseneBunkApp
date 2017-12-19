package com.omneAgate.activityKerosene;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.omneAgate.DTO.BaseDto;
import com.omneAgate.DTO.EnumDTO.RequestType;
import com.omneAgate.DTO.EnumDTO.ServiceListenerType;
import com.omneAgate.DTO.OTPTransactionDto;
import com.omneAgate.DTO.RMNResponseDto;
import com.omneAgate.DTO.RmnValidationReqDto;
import com.omneAgate.DTO.TransactionBaseDto;
import com.omneAgate.DTO.TransactionTypes;
import com.omneAgate.Util.Constants.FPSDBConstants;
import com.omneAgate.Util.CustomProgressDialog;
import com.omneAgate.Util.DownloadDataProcessor;
import com.omneAgate.Util.FPSDBHelper;
import com.omneAgate.Util.NetworkConnection;
import com.omneAgate.Util.Util;
import com.omneAgate.service.HttpClientWrapper;

import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;

import java.util.Date;


public class BeneficiaryOTPActivity extends BaseActivity {

    private int count = 0;          //Retry count

    private String request;         //Server Request string


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_qr_code_with_otp);
        actionBarCreation();
        networkConnection = new NetworkConnection(this);
        httpConnection = new HttpClientWrapper();
        appState = (GlobalAppState) getApplication();
        setTamilText((Button) findViewById(R.id.otpSubmit), R.string.submit);
        setTamilText((Button) findViewById(R.id.otpCancel), R.string.cancel);
        setTamilText((TextView) findViewById(R.id.textViewOtp), R.string.inputOTP);
        Bundle bundle = getIntent().getExtras();
        String data = bundle.getString("activation");
        request = bundle.getString("activationData");
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        RMNResponseDto qrCodeResponseReceived = gson.fromJson(data,
                RMNResponseDto.class);
        qrResponseData(qrCodeResponseReceived);
    }

    @Override
    protected void processMessage(Bundle message, ServiceListenerType what) {
        switch (what) {
            case QR_CODE:
                if (progressBar != null) {
                    progressBar.dismiss();
                }
                otpActivationResponseReceived(message);
                break;
            default:
                if (progressBar != null) {
                    progressBar.dismiss();
                }
                break;
        }

    }


    //QrCode response from server for respective card
    private void otpActivationResponseReceived(Bundle message) {
        try {
            String response = message.getString(FPSDBConstants.RESPONSE_DATA);
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            BaseDto qrCodeResponseReceived = gson.fromJson(response,
                    BaseDto.class);
            if (qrCodeResponseReceived.getStatusCode() == 0) {
                Util.messageBar(this, getString(R.string.cardActivated));
                ((Button) findViewById(R.id.otpSubmit)).setOnClickListener(null);
                errorNavigation(getString(R.string.cardActivated));
            } else {
                String messages = FPSDBHelper.getInstance(this).retrieveLanguageTable(qrCodeResponseReceived.getStatusCode(), GlobalAppState.language).getDescription();
                Util.messageBar(this, messages);
                ((Button) findViewById(R.id.otpSubmit)).setOnClickListener(null);
                errorNavigation(messages);
            }
        } catch (Exception e) {
            Log.e("Error", e.toString(), e);
            Util.LoggingQueue(this, "Error", e.toString());
        }

    }

    //After qrCode sent to server and response received this function calls
    private void qrResponseData(final RMNResponseDto qrCodeResponseReceived) {
        final View view = (EditText) findViewById(R.id.editTextOTP);
        ((Button) findViewById(R.id.otpSubmit)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                if (checkOTPValidate(qrCodeResponseReceived)) {
                    otpDialogResult(qrCodeResponseReceived);
                }
            }
        });
        ((Button) findViewById(R.id.otpCancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(BeneficiaryOTPActivity.this, SaleActivity.class));
                finish();
            }
        });
    }

    //Orientation change
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Util.changeLanguage(this, GlobalAppState.language);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, SaleActivity.class));
        finish();
    }

    //Handler for 5 secs
    private void errorNavigation(String messages) {
        DownloadDataProcessor sync = new DownloadDataProcessor(this);
        sync.processor();
        Intent intent = new Intent(this, SuccessFailureActivity.class);
        intent.putExtra("message", messages);
        startActivity(intent);
        finish();
    }


    //Response from otp received is true
    private void otpDialogResult(final RMNResponseDto qrCodeResponseReceived) {
        try {
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            RmnValidationReqDto qrCode = gson.fromJson(request,
                    RmnValidationReqDto.class);
            Log.e("req", qrCode.toString());
            OTPTransactionDto otpTransaction = qrCodeResponseReceived.getOtpTransactionDto();
            otpTransaction.setPosTime(new Date().getTime());
            qrCode.setOtpTransactionDto(otpTransaction);
            TransactionBaseDto base = new TransactionBaseDto();
            base.setType("com.omneagate.rest.dto.RmnValidationReqDto");
            base.setTransactionType(TransactionTypes.BENEFICIARY_ACTIVATION);
            base.setBaseDto(qrCode);
            String url = "/transaction/process";
            String qrCodes = new Gson().toJson(base);
            StringEntity se = new StringEntity(qrCodes, HTTP.UTF_8);
            progressBar = new CustomProgressDialog(this);
            progressBar.setCancelable(false);
            progressBar.show();
            httpConnection.sendRequest(url, null, ServiceListenerType.QR_CODE,
                    SyncHandler, RequestType.POST, se, this);
        } catch (Exception e) {
            Util.LoggingQueue(this, "Error", e.toString());
            Log.e("Error", e.toString(), e);
        }
    }

    /**
     * OTP validation
     *
     * @param qrResponse received from server
     *                   return true if otp equals user entry else false
     */
    private boolean checkOTPValidate(RMNResponseDto qrResponse) {
        EditText mobileOtp = (EditText) findViewById(R.id.editTextOTP);
        String oTP = mobileOtp.getText().toString();
        if (oTP.equals(qrResponse.getOtpTransactionDto().getOtp())) {
            return true;
        } else if (count == 3) {
            Util.messageBar(this, getString(R.string.retryCount));
            ((Button) findViewById(R.id.otpSubmit)).setOnClickListener(null);
            errorNavigation(getString(R.string.retryCount));
        } else
            Util.messageBar(this, getString(R.string.mobileOTPWrong));
        count++;
        return false;

    }
}
