package com.omneAgate.Bunker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.omneAgate.DTO.EnumDTO.RequestType;
import com.omneAgate.DTO.EnumDTO.ServiceListenerType;
import com.omneAgate.DTO.OTPTransactionDto;
import com.omneAgate.DTO.QROTPResponseDto;
import com.omneAgate.DTO.QRRequestDto;
import com.omneAgate.DTO.QRTransactionResponseDto;
import com.omneAgate.DTO.TransactionBaseDto;
import com.omneAgate.DTO.TransactionTypes;
import com.omneAgate.DTO.UpdateStockRequestDto;
import com.omneAgate.TransactionController.SMSListener;
import com.omneAgate.Util.AndroidDeviceProperties;
import com.omneAgate.Util.BeneficiarySalesTransaction;
import com.omneAgate.Util.Constants.FPSDBConstants;
import com.omneAgate.Util.CustomProgressDialog;
import com.omneAgate.Util.EntitlementResponse;
import com.omneAgate.Util.FPSDBHelper;
import com.omneAgate.Util.NetworkConnection;
import com.omneAgate.Util.SessionId;
import com.omneAgate.Util.TransactionBase;
import com.omneAgate.Util.Util;
import com.omneAgate.service.HttpClientWrapper;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;

import java.util.Timer;
import java.util.TimerTask;

public class MobileOTPActivity extends BaseActivity implements SMSListener, View.OnClickListener {

    final Handler handler = new Handler();     //Handler for user
    Timer timer;               //Initial timer
    TimerTask timerTask;          //Timer task intialization
    String rmn = "";    //User registered mobile no
    private String number = "";        //Retry count


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_otp);
        setUpInitialPage();
    }


    /*
    *
    * Initial Setup
    *
    * */
    private void setUpInitialPage() {
        networkConnection = new NetworkConnection(this);
        Util.LoggingQueue(this, "Mobile Have otp ", "Setting up main page");
        httpConnection = new HttpClientWrapper();
        appState = (GlobalAppState) getApplication();
        setUpPopUpPage();
        findViewById(R.id.imageViewBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        setTamilText((TextView) findViewById(R.id.top_textView), R.string.mobile_otp);
        viewForMobileOtp();


    }


    /*
    * On click layout user page will be changed
    * */
    private void viewForMobileOtp() {
        LinearLayout mobileOtp = (LinearLayout) findViewById(R.id.myMobileOTPBackground);
        mobileOtp.removeAllViews();
        LayoutInflater vi = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        final View view = vi.inflate(R.layout.view_received_otp, null);
        mobileOtp.addView(view);
        setBackGroundForLayout();
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
        findViewById(R.id.buttonNeedOTP).setOnClickListener(this);
        findViewById(R.id.imageView5).setOnClickListener(this);
        ((TextView) findViewById(R.id.enterOtp)).setTextColor(Color.parseColor("#fc77d2"));
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
            case R.id.buttonNeedOTP:
                iHaveOTP();
                break;
            default:
                break;
        }

    }

    /*
   * User need OTP
   * */
    private void iHaveOTP() {
        if (StringUtils.isEmpty(number)) {
            Util.messageBar(this, getString(R.string.enter_otp_empty));
        } else {
            if (number.length() < 7) {
                Util.messageBar(this, getString(R.string.invalid_otp));
            } else {
                rmn = number;
                getOTPFromServer();
                // qrResponseData(new QROTPResponseDto());
            }
        }
    }

    /**
     * send request to get otp from server
     */
    private void getOTPFromServer() {
        try {
            QRRequestDto qrCode = new QRRequestDto();
            AndroidDeviceProperties devices = new AndroidDeviceProperties(this);
            qrCode.setDeviceId(devices.getDeviceProperties().getSerialNumber());
            qrCode.setOtpId(number);
            TransactionBaseDto base = new TransactionBaseDto();
            base.setType("com.omneagate.rest.dto.QRRequestDto");
            base.setTransactionType(TransactionTypes.SALE_HAVE_OTP);
            base.setBaseDto(qrCode);
            Log.i("Base", base.toString());
            UpdateStockRequestDto update = new UpdateStockRequestDto();
            update.setRmn(rmn);
            update.setUfc("");

            if (networkConnection.isNetworkAvailable()) {
                if (SessionId.getInstance().getSessionId().length() > 0) {
                    String url = "/transaction/process";
                    String qrCodes = new Gson().toJson(base);
                    StringEntity se = new StringEntity(qrCodes, HTTP.UTF_8);
                    progressBar = new CustomProgressDialog(this);
                    Util.LoggingQueue(this, "Mobile Have otp ", "Request for OTP check:" + qrCodes);
                    progressBar.setCancelable(false);
                    progressBar.show();
                    httpConnection.sendRequest(url, null, ServiceListenerType.QR_CODE,
                            SyncHandler, RequestType.POST, se, this);
                } else {
                    Util.messageBar(this, getString(R.string.noNetworkConnection));
                    errorNavigation(getString(R.string.noNetworkConnection));
                }
            } else {
                Util.messageBar(this, getString(R.string.noNetworkConnection));
                errorNavigation(getString(R.string.noNetworkConnection));
            }
        } catch (Exception e) {
            Util.LoggingQueue(this, "Error", e.toString());
            Log.e("MobileOTP", e.toString(), e);
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
            if (number.length() >= 7) {
                return;
            }

            if (number.equals("0")) {
                number = text;
            } else {
                number = number + text;
            }
            setText();
        } catch (Exception e) {
            Log.e("MobileOTP", e.toString(), e);
        }
    }

    private void setText() {
        ((TextView) findViewById(R.id.mobileNumberOTP)).setTextColor(Color.parseColor("#fc77d2"));
        ((TextView) findViewById(R.id.mobileNumberOTP)).setText(number);
    }

    /*
    * Concrete Method for SMS receiver
    * */
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

    /*
    * Set border for the layout
    *
    * */
    private void setBackGroundForLayout() {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(Color.parseColor("#ffffff")); // Changes this drawbale to use a single color instead of a gradient
        gd.setStroke(2, Color.parseColor("#fc77d2"));
        RelativeLayout tv = (RelativeLayout) findViewById(R.id.layout_otp);
        tv.setBackground(gd);
    }


    //Concrete method
    @Override
    public void processMessage(Bundle message, ServiceListenerType what) {
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

    /*
    * Timer start up
    * */
    private void checkMessage() {
        timer = new Timer();
        initializeTimerTask();
        //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
        timer.schedule(timerTask, 75000);
    }

    /*
    *
    * Initialization for timer
    * */
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

    /*
    * Stop Timer for message receives
    * */
    public void stopTimerTask(QROTPResponseDto qrResponse) {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        GlobalAppState.listener = null;
        removeData(qrResponse);
    }


    /*
    *
    * SMS Connection Error when data
    * not received in time
    *
    * */
    private void removeData(QROTPResponseDto qrResponse) {
        progressBar.dismiss();
        if (StringUtils.isNotEmpty(qrResponse.getUfc())) {
            otpDialogResult(qrResponse);
        } else {
            Toast.makeText(this, "Connectivity Error", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, SaleOrderActivity.class));
            finish();
        }
    }


    /*
    *
    * Response from otp received is true
    *
    * */
    private void otpDialogResult(QROTPResponseDto qrResponse) {
        try {
            TransactionBaseDto transaction;//Transaction base for sending data to server
            BeneficiarySalesTransaction beneficiary = new BeneficiarySalesTransaction(this);
            if (qrResponse.getUfc() != null)
                Util.LoggingQueue(this, "Mobile Have otp ", "Calculation :" + qrResponse.getUfc());
            QRTransactionResponseDto qrCodeResponseReceived = beneficiary.getBeneficiaryDetails(qrResponse.getUfc(),qrResponse.getOtpTransactionDto().getFPSId());
            if (qrCodeResponseReceived != null)
                Util.LoggingQueue(this, "Mobile Have otp ", "Entitlements:" + qrCodeResponseReceived.toString());
            appState.refId = qrResponse.getReferenceId();
            if (qrCodeResponseReceived != null && qrCodeResponseReceived.getEntitlementList() != null && qrCodeResponseReceived.getEntitlementList().size() > 0) {
                qrCodeResponseReceived.setMode('Q');
                transaction = new TransactionBaseDto();
                qrCodeResponseReceived.setReferenceId(qrResponse.getReferenceId());
                EntitlementResponse.getInstance().setQrcodeTransactionResponseDto(qrCodeResponseReceived);
                transaction.setTransactionType(TransactionTypes.SALE_HAVE_OTP_AUTHENTICATE);
                TransactionBase.getInstance().setTransactionBase(transaction);
                startActivity(new Intent(this, SalesEntryActivity.class));
                finish();
            } else {
                Util.LoggingQueue(this, "Mobile Have otp ", "Internal Error");
                Util.messageBar(this, getString(R.string.internalError));
            }
        } catch (Exception e) {
            Util.LoggingQueue(this, "Mobile Have otp ", "Internal Error:" + e.getStackTrace().toString());
            Util.messageBar(this, getString(R.string.internalError));
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, SaleOrderActivity.class));
        Util.LoggingQueue(this, "Mobile Have otp ", "Back pressed calling");
        finish();
    }

    /*
    *
    * QrCode response from server for respective card
    *
    * */
    private void QRCodeResponseReceived(Bundle message) {
        try {
            String response = message.getString(FPSDBConstants.RESPONSE_DATA);
            Log.i("resp", response);
            Util.LoggingQueue(this, "Mobile Have otp ", "Response OTP:" + response);
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            QROTPResponseDto qrCodeResponseReceived = gson.fromJson(response,
                    QROTPResponseDto.class);
            if (qrCodeResponseReceived.getStatusCode() == 0) {
                otpDialogResult(qrCodeResponseReceived);
            } else {
                String messages = Util.messageSelection(FPSDBHelper.getInstance(this).retrieveLanguageTable(qrCodeResponseReceived.getStatusCode()));
                Util.LoggingQueue(this, "Mobile Have otp ", "Error:" + messages);
                Util.messageBar(this, messages);
                errorNavigation(messages);
            }
        } catch (Exception e) {
            Log.e("MobileOTP", e.toString(), e);
            Util.LoggingQueue(this, "Mobile Have otp ", "Error in RMN");
            Util.LoggingQueue(this, "Error in RMN", e.getStackTrace().toString());
        }

    }

    /*
    *
    * Error navigation  pages
    * */
    private void errorNavigation(String messages) {
        if (StringUtils.isEmpty(messages)) {
            messages = getString(R.string.error_otp_create);
        }
        Intent intent = new Intent(this, SuccessFailureActivity.class);
        Util.LoggingQueue(this, "Mobile Have otp ", "Navigating to failure page");
        intent.putExtra("message", messages);
        startActivity(intent);
        finish();
    }


}
