package com.omneAgate.ActivityBusinessClass;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.omneAgate.DTO.KeroseneDto;
import com.omneAgate.DTO.LoginDto;
import com.omneAgate.DTO.LoginResponseDto;
import com.omneAgate.Util.CustomProgressDialog;
import com.omneAgate.Util.FPSDBHelper;
import com.omneAgate.Util.LoginData;
import com.omneAgate.Util.SessionId;
import com.omneAgate.Util.Util;
import com.omneAgate.Bunker.R;
import com.omneAgate.service.LocalDBLogin;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;

/**
 * Created for local Login
 */
public class LoginCheck {

    private static  final String TAG=LoginCheck.class.getCanonicalName();
    Activity context;
    CustomProgressDialog progressBar;

    public LoginCheck(Activity context, CustomProgressDialog progressBar) {
        this.context = context;
        this.progressBar = progressBar;
    }

    /**
     * sending login details to server if network connection available
     *
     * @params loginDto
     */

    /**
     * IF NO NETWORK AVAILABLE LOGIN WILL DONE USING LOCAL DATABASE
     */
    public void localLogin(LoginDto loginCredentials) {
        try {
            SharedPreferences prefs = context.getSharedPreferences("FPS", Context.MODE_PRIVATE);
            if (!prefs.getBoolean("approved", false)) {
                Util.messageBar(context, context.getString(R.string.noNetworkConnection));
                return;
            }
            progressBar.show();
            LoginResponseDto hashDbPassword = FPSDBHelper.getInstance(context).retrieveData(loginCredentials.getUserName());
            if (hashDbPassword == null) {
                Util.messageBar(context, context.getString(R.string.inCorrectUnamePword));
                dismissProgress();
                return;

            }
            LoginData.getInstance().setLoginData(hashDbPassword);
            String hashPassword = hashDbPassword.getUserDetailDto().getPassword();
            if (StringUtils.isNotEmpty(hashPassword)) {
                try {
                    if (hashDbPassword.getUserDetailDto().getProfile().equalsIgnoreCase("KEROSENE_BUNK")) {
                        Log.e("chekc","deviceStatus");
                        String deviceStatus = FPSDBHelper.getInstance(context).getMasterData("status");
                        Log.e("chekc",""+deviceStatus);
                        if (deviceStatus.equalsIgnoreCase("UNASSOCIATED")) {
                            dismissProgress();
                            Util.messageBar(context, context.getString(R.string.unassociated));
                            return;
                        } else if (deviceStatus.equalsIgnoreCase("INACTIVE")) {
                            dismissProgress();
                            Util.messageBar(context, context.getString(R.string.deviceInvalid));
                            return;
                        }
                        else if (deviceStatus.equalsIgnoreCase("false")) {
                            dismissProgress();
                            Util.messageBar(context, context.getString(R.string.userInactive));
                            return;
                        }
                        else if (deviceStatus.equalsIgnoreCase("true")) {
                            dismissProgress();
                            Util.messageBar(context, context.getString(R.string.storeInactive));
                            return;
                        }
                    }
                }
                catch(Exception e) {}
                try {
                    if (!hashDbPassword.getUserDetailDto().getProfile().equalsIgnoreCase("ADMIN")) {
                        if (!hashDbPassword.getKeroseneBunkDto().isStatus()) {
                            dismissProgress();
                            Util.messageBar(context, context.getString(R.string.storeInactive));
                            return;
                        }
                    }
                }
                catch(Exception e) {}

                    KeroseneDto kerosene = hashDbPassword.getUserDetailDto().getKerosene();
                    SessionId.getInstance().setUserId(hashDbPassword.getUserDetailDto().getId());
                    SessionId.getInstance().setFpsId(kerosene.getId());
                    SessionId.getInstance().setFpsCode(kerosene.getCode());
                    SessionId.getInstance().setUserName(hashDbPassword.getUserDetailDto().getUserId());


              SessionId.getInstance().setRrc_districtid(hashDbPassword.getKeroseneBunkDto().getTalukDto().getDistrictDto().getId());

              SessionId.getInstance().setRrc_talukid(hashDbPassword.getKeroseneBunkDto().getTalukDto().getId());

              SessionId.getInstance().setEntitlementClassification(hashDbPassword.getKeroseneBunkDto().getEntitlementClassification());


                         String lastLoginTime = FPSDBHelper.getInstance(context).getLastLoginTime(hashDbPassword.getUserDetailDto().getId());
                    if (StringUtils.isNotEmpty(lastLoginTime)) {
                        SessionId.getInstance().setLastLoginTime(new Date(Long.parseLong(lastLoginTime)));
                    } else {
                        SessionId.getInstance().setLastLoginTime(new Date());
                    }
                    FPSDBHelper.getInstance(context).setLastLoginTime(hashDbPassword.getUserDetailDto().getId());
                    SessionId.getInstance().setLoginTime(new Date());
                    LocalDBLogin localDBLogin = new LocalDBLogin(context, progressBar, hashDbPassword.getUserDetailDto());
                    localDBLogin.setLoginProcess(loginCredentials.getPassword(), hashPassword);


            } else {
                dismissProgress();
                Util.messageBar(context, context.getString(R.string.inCorrectUnamePword));

            }
        } catch (Exception e) {
            dismissProgress();
            Util.messageBar(context, context.getString(R.string.inCorrectUnamePword));
            Log.e("LoginActivity", "Error in Local Login", e);
        }
    }

    private void dismissProgress() {
        if (progressBar != null) {
            if (progressBar.isShowing()) {
                progressBar.dismiss();
            }
        }
    }
}
