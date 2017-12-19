package com.omneAgate.Bunker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.omneAgate.Bunker.dialog.LogoutDialog;
import com.omneAgate.Bunker.dialog.fpsRollViewAdpter;
import com.omneAgate.DTO.BaseDto;
import com.omneAgate.DTO.EnumDTO.CommonStatuses;
import com.omneAgate.DTO.EnumDTO.RequestType;
import com.omneAgate.DTO.EnumDTO.Roll_Feature;
import com.omneAgate.DTO.EnumDTO.ServiceListenerType;
import com.omneAgate.DTO.RoleFeatureDto;
import com.omneAgate.DTO.RollMenuDto;
import com.omneAgate.DTO.UpgradeDetailsDto;
import com.omneAgate.Util.Constants.FPSDBConstants;
import com.omneAgate.Util.FPSDBHelper;
import com.omneAgate.Util.NetworkConnection;
import com.omneAgate.Util.SessionId;
import com.omneAgate.Util.Util;
import com.omneAgate.service.HttpClientWrapper;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SaleActivity extends BaseActivity {

    final ArrayList<String> fpsRoleName = new ArrayList<String>();
    GridView fpsRollView;
    List<RollMenuDto> roleMenus = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_scan);
//        int size = FPSDBHelper.getInstance(this).retrieveBeneficiary(129631).getBenefMembersDto().size();
//        Toast.makeText(this,"size="+size,Toast.LENGTH_SHORT).show();
        appState = (GlobalAppState) getApplication();
        Util.LoggingQueue(this, "Sale activity", "Inside Sale activity");
        if (!checkLocationDetails()) {
            return;
        }
        upgradeSuccessMessage();
        setUpDashBoard();

    }



    public void rollFeatureView() {
        List<RoleFeatureDto> retriveRollFeature = new ArrayList<>();

        retriveRollFeature = FPSDBHelper.getInstance(this).retrieveData(SessionId.getInstance().getUserId());
        for (int i = 0; i < retriveRollFeature.size(); i++) {
            String roleName = retriveRollFeature.get(i).getRollName();
            try {
                if (roleName.equalsIgnoreCase("KB_SALES_ORDER_MENU")  || roleName.equalsIgnoreCase("KB_STOCK_MANAGEMENT_MENU")
                        || roleName.equalsIgnoreCase("KB_TRANSACTIONS_MENU") || roleName.equalsIgnoreCase("KB_CLOSE_SALES_MENU") || roleName.equalsIgnoreCase("KB_OTHER_MENUS")) {
                    Roll_Feature rolls = Roll_Feature.valueOf(roleName);
                    roleMenus.add(new RollMenuDto(getString(rolls.getRollName()), rolls.getBackground(), rolls.getColorCode(), rolls.getDescription()));
                    fpsRoleName.add(roleName);
                }
            } catch (Exception e) {
                Log.e("Excep", e.toString(), e);
            }
        }
        fpsRollView = (GridView) findViewById(R.id.fpsroll);
        fpsRollView.setAdapter(new fpsRollViewAdpter(this, roleMenus));
        fpsRollView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    Util.LoggingQueue(SaleActivity.this, "SaleActivity", "Moving to Sale Order activity");
                    String myClass = "com.omneAgate.Bunker." + roleMenus.get(i).getClassName();
                    Intent myIntent = new Intent(getApplicationContext(), Class.forName(myClass));
                    startActivity(myIntent);
                    finish();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    Log.e("Error", "" + e.toString(), e);
                }
            }
        });

    }

    /**
     * Used to set the dashboard page
     * <p/>
     * user name and onCLickListeners are in this function
     */
    private void setUpDashBoard() {
        Util.LoggingQueue(this, "Sale activity", "Setting up sales activity");
        if (StringUtils.isEmpty(SessionId.getInstance().getUserName())) {
            SessionId.getInstance().setUserName("");
        }
        //((TextView) findViewById(R.id.user_fps_store)).setText(getString(R.string.fps_code) + SessionId.getInstance().getFpsCode());
        setTamilText((TextView) findViewById(R.id.top_textView), R.string.dashboard);
        setUpPopUpPage();
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        SessionId.getInstance().setQrOTPEnabled(pref.getBoolean("isActive", false));
        rollFeatureView();
    }

    //After user give logout this method will call dialog
    private void userLogoutResponse() {
        LogoutDialog logout = new LogoutDialog(this);
        Util.LoggingQueue(this, "Logout", "Logout called");
        logout.show();

    }

    //Called when user press back button
    @Override
    public void onBackPressed() {
        userLogoutResponse();
    }

    //Concrete method
    @Override
    public void processMessage(Bundle message, ServiceListenerType what) {
        switch (what) {
            default:
                changeValue(message);
                break;
        }
    }

    private void changeValue(Bundle message) {
        try {
            String response = message.getString(FPSDBConstants.RESPONSE_DATA);
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            if (response != null && !response.contains("Empty")) {
                BaseDto base = gson.fromJson(response, BaseDto.class);
                if (base.getStatusCode() == 0) {
                    FPSDBHelper.getInstance(this).updateUpgradeExec();
                    Cursor cursor = FPSDBHelper.getInstance(this).getCurerntVersonExec();
                    cursor.moveToFirst();
                    FPSDBHelper.getInstance(this).insertTableUpgrade(cursor.getInt(cursor.getColumnIndex("android_old_version")), "Upgrade completed successfully", "success", "UPGRADE_END", cursor.getInt(cursor.getColumnIndex("android_new_version")),
                            cursor.getString(cursor.getColumnIndex("ref_id")), cursor.getString(cursor.getColumnIndex("refer_id")));
                    cursor.close();
                }
            }
        } catch (Exception e) {
            Log.e("SaleActivity", e.toString(), e);
        }

    }

    private void upgradeSuccessMessage() {
        try {
            NetworkConnection netStatus = new NetworkConnection(this);
            if (FPSDBHelper.getInstance(this).checkUpgradeFinished() && netStatus.isNetworkAvailable()) {
                httpConnection = new HttpClientWrapper();
                UpgradeDetailsDto upgradeDto = FPSDBHelper.getInstance(this).getUpgradeData();
                upgradeDto.setCreatedTime(new Date().getTime());
                upgradeDto.setStatus(CommonStatuses.UPDATE_COMPLETE);
                Cursor cursor = FPSDBHelper.getInstance(this).getCurerntVersonExec();
                upgradeDto.setPreviousVersion(cursor.getInt(cursor.getColumnIndex("android_old_version")));
                upgradeDto.setCurrentVersion(cursor.getInt(cursor.getColumnIndex("android_new_version")));
                upgradeDto.setReferenceNumber(cursor.getString(cursor.getColumnIndex("refer_id")));
                upgradeDto.setDeviceNum(Settings.Secure.getString(
                        getContentResolver(), Settings.Secure.ANDROID_ID).toUpperCase());
                String checkVersion = new Gson().toJson(upgradeDto);
                StringEntity se = new StringEntity(checkVersion, HTTP.UTF_8);
                String url = "/upgradedetails/kerosenebunk/adddetails";
                Util.LoggingQueue(this, "Device Register Version", "Checking version of apk in device");
                httpConnection.sendRequest(url, null, ServiceListenerType.CHECKVERSION,
                        SyncHandler, RequestType.POST, se, this);
            }
        } catch (Exception e) {
            Log.e("SaleActivity", e.toString(), e);
        }
    }


}


