package com.omneAgate.service;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.omneAgate.DTO.LoginHistoryDto;
import com.omneAgate.DTO.UserDetailDto;
import com.omneAgate.Util.CustomProgressDialog;
import com.omneAgate.Util.FPSDBHelper;
import com.omneAgate.Util.SessionId;
import com.omneAgate.Util.StringDigesterString;
import com.omneAgate.Util.Util;
import com.omneAgate.Bunker.AdminActivity;
import com.omneAgate.Bunker.GlobalAppState;
import com.omneAgate.Bunker.R;
import com.omneAgate.Bunker.SaleActivity;

import org.apache.commons.lang3.StringUtils;
import org.jasypt.digest.StringDigester;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by user1 on 3/3/15.
 */
public class LocalDBLogin {

    Activity context;
    CustomProgressDialog progressBar;
    UserDetailDto profile;

    public LocalDBLogin(Activity context, CustomProgressDialog progressBar, UserDetailDto profile) {
        this.context = context;
        this.progressBar = progressBar;
        this.profile = profile;
    }

    /**
     * async task for login
     *
     * @param passwordUser and hash
     */
    public void setLoginProcess(String passwordUser, String hashDbPassword) {
        new LocalLoginProcess(passwordUser, hashDbPassword).execute();

    }

    private boolean localDbPassword(String passwordUser, String passwordDbHash) {

        StringDigester stringDigester = StringDigesterString.getPasswordHash(context);

        return stringDigester.matches(passwordUser, passwordDbHash);
    }

    private void insertLoginHistoryDetails() {
        LoginHistoryDto loginHistoryDto = new LoginHistoryDto();
        if (profile.getKerosene() != null)
            loginHistoryDto.setBunkId(profile.getKerosene().getId());
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
        loginHistoryDto.setLoginTime(df.format(new Date()));
        loginHistoryDto.setLoginType("OFFLINE_LOGIN");
        loginHistoryDto.setUserId(profile.getId());
        df = new SimpleDateFormat("ddMMyyHHmmss", Locale.getDefault());
        String transactionId = df.format(new Date());
        loginHistoryDto.setTransactionId(transactionId);
        SessionId.getInstance().setTransactionId(transactionId);
        FPSDBHelper.getInstance(context).insertLoginHistory(loginHistoryDto);
    }

    //Local login Process
    private class LocalLoginProcess extends AsyncTask<String, Void, Boolean> {
        // user password and local db password
        String passwordUser, hashDbPassword;


        // LocalLoginProcess Constructor
        LocalLoginProcess(String passwordUser, String hashDbPassword) {
            this.passwordUser = passwordUser;
            this.hashDbPassword = hashDbPassword;
        }

        /**
         * Local login Background Process
         * return true if user hash and dbhash equals else false
         */
        protected Boolean doInBackground(String... params) {
            try {
                return localDbPassword(passwordUser, hashDbPassword);
            } catch (Exception e) {
                Log.e("loca lDb", "Interrupted", e);
                return false;

            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (progressBar != null) progressBar.dismiss();
            if (result) {
                GlobalAppState.localLogin = true;
                insertLoginHistoryDetails();
                String lastModifiedDate = FPSDBHelper.getInstance(context).getMasterData("syncTime");
                if (StringUtils.isNotEmpty(lastModifiedDate)) {
                    if ("ADMIN".equalsIgnoreCase(profile.getProfile())) {
                        context.startActivity(new Intent(context, AdminActivity.class));
                    } else {
                        context.startActivity(new Intent(context, SaleActivity.class));
                    }
                    context.finish();
                } else {
                    Util.messageBar(context, context.getString(R.string.loginInvalidUserPassword));
                }
            }else{
                Util.messageBar(context, context.getString(R.string.noNetworkConnection));
            }
        }
    }
}
