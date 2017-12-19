package com.omneAgate.Util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;

import com.omneAgate.Bunker.GlobalAppState;
import com.omneAgate.DTO.LoggingDto;
import com.omneAgate.DTO.MessageDto;
import com.omneAgate.DTO.POSStockAdjustmentDto;
import com.omneAgate.UndoBar.UndoBar;

import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Utility class for entire application
 */
public class Util {

    public static List<POSStockAdjustmentDto> ackAdjustmentList;

    //simple messageBar for FPS
    public static void messageBar(Activity activity, String message) {
        if (StringUtils.isEmpty(message)) {
            message = "Internal Error";
        }
        UndoBar undoBar = new UndoBar.Builder(activity)//
                .setMessage(message)//
                .setStyle(UndoBar.Style.KITKAT)
                .setAlignParentBottom(true)
                .create();
        undoBar.show();

    }

    /**
     * Registration store in local preference
     *
     * @param context
     */
    public static void storePreferenceRegister(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("FPS", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("register", true);
        editor.commit();
    }

    /**
     * Registration store in local preference
     *
     * @param context
     */
    public static void storePreferenceApproved(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("FPS", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("register", true);
        editor.putBoolean("approved", true);
        editor.commit();
    }

    public static String messageSelection(MessageDto messages) {
        String errorMessage = messages.getDescription();
        if (GlobalAppState.language.equalsIgnoreCase("ta")) {
            errorMessage = messages.getLocalDescription();
        }
        return errorMessage;
    }

    public static String getTransactionId(Context context) {

        Date todayDate = new Date();

        SimpleDateFormat toDate = new SimpleDateFormat("MMyy", Locale.getDefault());
        String maxId = FPSDBHelper.getInstance(context).lastBillToday();
        String transactionId = toDate.format(todayDate);
        if (StringUtils.isNotEmpty(maxId)) {
            maxId = StringUtils.substring(maxId, 4);
            Long userTransactionId = Long.parseLong(maxId);
            userTransactionId = userTransactionId + 1;
            String billNumber = Long.toString(userTransactionId);
            if (billNumber.length() <= 6) {
                DecimalFormat formatter = new DecimalFormat("000000");
                billNumber = formatter.format(userTransactionId);
            }
            transactionId = transactionId + billNumber;

        } else {
            transactionId = transactionId + SessionId.getInstance().getFpsId() + "000001";
        }
        Log.e("transactionId ",""+transactionId);
        return transactionId;
    }

  /*  *//**
     * return long sequence number
     *
     * @paramlast inserted string value
     *//*
    private static String returnNextValue(String lastValue) {
        try {
            String data = StringUtils.substring(lastValue, 6);
            Long nextData = 001l;
            SimpleDateFormat toDate = new SimpleDateFormat("dd", Locale.getDefault());
            String transactionId = toDate.format(new Date());
            if (StringUtils.substring(lastValue, 0, 2).equals(transactionId)) {
                nextData = Long.parseLong(data) + 1;
            }
            DecimalFormat formatter = new DecimalFormat("000");
            return formatter.format(nextData);
        } catch (Exception e) {
            Log.e("Last", e.toString(), e);
            return "001";
        }

    }*/

    /**
     * Change language in android
     *
     * @param languageCode,context
     */
    public static void changeLanguage(Context context, String languageCode) {
        Resources res = context.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        android.content.res.Configuration conf = res.getConfiguration();
        conf.locale = new Locale(languageCode);
        res.updateConfiguration(conf, dm);
        FPSDBHelper.getInstance(context).updateMaserData("language", languageCode);
    }


    /**
     * get log data and set device id in log
     *
     * @param context,errorType and error string
     */
    private static LoggingDto logging(Context context, String errorType, String error) {
        LoggingDto log = new LoggingDto();
        log.setErrorType(errorType);
        log.setLogMessage(error);
        log.setDeviceId(Settings.Secure.getString(
                context.getContentResolver(), Settings.Secure.ANDROID_ID));
        return log;
    }

    /**
     * Add log to queue
     *
     * @param context,errorType and error string
     */
    public static void LoggingQueue(Context context, String errorType, String error) {

        GlobalAppState appState = (GlobalAppState) context.getApplicationContext();
        if (GlobalAppState.isLoggingEnabled && NetworkUtil.getConnectivityStatus(context) != 0)
            appState.queue.enqueue(logging(context, errorType, error));

    }

    /**
     * Add log to queue
     *
     * @param context,errorType and error string
     */
    public static String DecryptedBeneficiary(Context context, String encryptedString) {
        SharedPreferences prefs = context.getSharedPreferences("FPS", Context.MODE_PRIVATE);
        String key = prefs.getString("keyEncrypt", "");
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        Log.e("Key", encryptedString);
        BouncyCastleProvider bouncy = new BouncyCastleProvider();
        encryptor.setProvider(bouncy);
        encryptor.setAlgorithm("PBEWITHSHA-256AND256BITAES-CBC-BC");
        encryptor.setPassword(key);
        String encrypted = encryptor.decrypt(encryptedString);
        encrypted = StringUtils.substring(encrypted, 0, encrypted.length() - 4) + "****";
        return encrypted;

    }

    public static String DecryptPassword(String encryptedString) {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        BouncyCastleProvider bouncy = new BouncyCastleProvider();
        encryptor.setProvider(bouncy);
        encryptor.setAlgorithm("PBEWITHSHA-256AND256BITAES-CBC-BC");
        encryptor.setPassword("fpspos");
        String encrypted = encryptor.decrypt(encryptedString);
        return encrypted;
    }

    public static String EncryptPassword(String password) {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setProvider(new BouncyCastleProvider());
        encryptor.setAlgorithm("PBEWITHSHA-256AND256BITAES-CBC-BC");
        encryptor.setPassword("fpspos");
        return encryptor.encrypt(password);
    }
}
