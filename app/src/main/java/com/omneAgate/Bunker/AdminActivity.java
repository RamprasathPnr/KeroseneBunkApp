package com.omneAgate.Bunker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.omneAgate.DTO.ApplicationType;
import com.omneAgate.DTO.BaseDto;
import com.omneAgate.DTO.EnumDTO.RequestType;
import com.omneAgate.DTO.EnumDTO.Roll_Feature;
import com.omneAgate.DTO.EnumDTO.ServiceListenerType;
import com.omneAgate.DTO.POSLocationDto;
import com.omneAgate.DTO.RollMenuDto;
import com.omneAgate.Util.AndroidDeviceProperties;
import com.omneAgate.Util.Constants.FPSDBConstants;
import com.omneAgate.Util.CustomProgressDialog;
import com.omneAgate.Util.FPSDBHelper;
import com.omneAgate.Util.GPSService;
import com.omneAgate.Util.LocalDbRecoveryProcess;
import com.omneAgate.Util.SessionId;
import com.omneAgate.Util.Util;
import com.omneAgate.Bunker.dialog.LocationReceivedDialog;
import com.omneAgate.Bunker.dialog.LogoutDialog;
import com.omneAgate.Bunker.dialog.fpsRollViewAdpter;
import com.omneAgate.service.HttpClientWrapper;

import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AdminActivity extends BaseActivity {


    private static final String TAG=AdminActivity.class.getCanonicalName();
    final ArrayList<String> fpsRoleName = new ArrayList<>();
    GridView fpsRoleView;
    List<RollMenuDto> roleMenu = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_admin);
        configureInitialPage();
        rollFeatureView();
        progressBar = new CustomProgressDialog(this);
    }



    public void rollFeatureView() {
        long start=System.currentTimeMillis();
        Set<String> retrieveRoleFeature ;

        retrieveRoleFeature = FPSDBHelper.getInstance(this).retrieveRolesDataString(SessionId.getInstance().getUserId());

        setTamilText((TextView) findViewById(R.id.top_textView), R.string.dashboard);
        for (String roleUser : retrieveRoleFeature) {

            try {
                if (roleUser.equalsIgnoreCase("OPEN_STOCK") || roleUser.equalsIgnoreCase("GEO_LOCATION") || roleUser.equalsIgnoreCase("STATISTICS")
                        || roleUser.equalsIgnoreCase("RETRIEVE_DB") || roleUser.equalsIgnoreCase("RESTORE_DB") || roleUser.equalsIgnoreCase("VERSION_UPGRADE")) {
                    Roll_Feature roles = Roll_Feature.valueOf(roleUser);
                    roleMenu.add(new RollMenuDto(getString(roles.getRollName()), roles.getBackground(), roles.getColorCode(), roles.getDescription()));
                    fpsRoleName.add(roleUser);
                }
            } catch (Exception e) {
                Log.e("Excep", e.toString(), e);
            }
        }
      //  Log.e("retrive_arr_feature", "" + fpsRoleName);
        fpsRoleView = (GridView) findViewById(R.id.fpsroll);
        fpsRoleView.setAdapter(new fpsRollViewAdpter(this, roleMenu));
        fpsRoleView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                printData(roleMenu.get(i).getClassName());
            }
        });
        long end=System.currentTimeMillis();
        Log.e("total timing"," :"+(end-start));

    }

    private void printData(String funcName) {
        try {
            Method method = getClass().getDeclaredMethod(funcName);
            method.invoke(this);
        } catch (Exception e) {
            Log.e("Error", e.toString(), e);
        }
    }

    private void restoreDB() {
        LocalDbRecoveryProcess localDbRecoveryProcess = new LocalDbRecoveryProcess(AdminActivity.this);
        localDbRecoveryProcess.restoresDb();
    }

    private void getStatistics() {
        startActivity(new Intent(this, StatisticsActivity.class));
        finish();
    }

    private void retrieveDB() {
        LocalDbRecoveryProcess localDbRecoveryPro = new LocalDbRecoveryProcess(AdminActivity.this);
       // localDbRecoveryPro.backupDb(true, "", FPSDBHelper.DATABASE_NAME, "");
        localDbRecoveryPro.backupDb(FPSDBHelper.DATABASE_NAME);
    }

    private void openStock() {
        startActivity(new Intent(this, OpenStockActivity.class));
        finish();
    }

    private void versionUpgrade() {

        startActivity(new Intent(this, VersionUpgradeInfo.class));
        finish();
    }

    private void findLocation() {
        findGPSLocation();
    }

    private void configureInitialPage() {
        setUpPopUpPageForAdmin();
    }

    private void findGPSLocation() {
        LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabled = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!enabled) {
            turnGPSOn();
        } else {
            GPSService mGPSService = new GPSService(this);
            Location locationB = mGPSService.getLocation();
            if (locationB != null) {
                new LocationReceivedDialog(this, locationB).show();
            } else {
                if(GlobalAppState.language.equalsIgnoreCase("ta")) {
                    Toast.makeText(this, "இருப்பிடம் பெற முடியாது", Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(this, "Location can not be received", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public void turnGPSOn() {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabled = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (enabled) {
            progressBar.show();
            setLocation();
        } else {
            if(GlobalAppState.language.equalsIgnoreCase("ta")) {
                Toast.makeText(this, "இருப்பிடம் அமைப்பை செயல்படுத்தவும்", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, "Please Enable Location Setting", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setLocation() {
        final Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (progressBar != null)
                    progressBar.dismiss();
                findGPSLocation();
            }
        }, 5000);
    }


    @Override
    public void onBackPressed() {
        userLogoutResponse();
    }

    //After user give logout this method will call dialog
    private void userLogoutResponse() {
        LogoutDialog logout = new LogoutDialog(this);
        logout.show();
    }

    public void sendLocation(Location location) {
        try {
            httpConnection = new HttpClientWrapper();
            POSLocationDto posLocation = new POSLocationDto();
            posLocation.setLatitude(String.valueOf(location.getLatitude()));
            posLocation.setLongitude(String.valueOf(location.getLongitude()));
            posLocation.setAppType(ApplicationType.KEROSENE_BUNK);
            AndroidDeviceProperties device = new AndroidDeviceProperties(this);
            posLocation.setDeviceNumber(device.getDeviceProperties().getSerialNumber());
            String deviceLocation = new Gson().toJson(posLocation);
            Log.i(TAG,"add location input"+deviceLocation);
            StringEntity se = new StringEntity(deviceLocation, HTTP.UTF_8);
           String url = "/remoteLogging/addlocation";

                httpConnection.sendRequest(url, null, ServiceListenerType.DEVICE_STATUS,
                        SyncHandler, RequestType.POST, se, this);

        } catch (Exception e) {
            Util.messageBar(this, getString(R.string.internalError));
            Util.LoggingQueue(this, "POS location Error", "Locayion Error in Pos");
            Log.e("error", e.toString(), e);

        }
    }

    @Override
    protected void processMessage(Bundle message, ServiceListenerType what) {
        switch (what) {
            case DEVICE_STATUS:
                setStatusCheck(message);
                break;
            default:
                Util.messageBar(this, getString(R.string.serviceNotAvailable));
                break;
        }
    }

    /**
     * status Response from server
     */
    private void setStatusCheck(Bundle message) {
        try {
            String response = message.getString(FPSDBConstants.RESPONSE_DATA);
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            BaseDto base = gson.fromJson(response, BaseDto.class);
            if (base.getStatusCode() == 0) {
                Util.messageBar(this, getString(R.string.successInUpdate));
            } else {
                Util.messageBar(this, getString(R.string.internalError));
            }
        } catch (Exception e) {
            Util.messageBar(this, getString(R.string.internalError));
        }
    }


}
