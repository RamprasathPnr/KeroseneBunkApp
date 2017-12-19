package com.omneAgate.Bunker;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.omneAgate.DTO.EnumDTO.ServiceListenerType;
import com.omneAgate.DTO.StatisticsDto;
import com.omneAgate.Util.FPSDBHelper;
import com.omneAgate.Util.GPSService;
import com.omneAgate.Util.LocationId;
import com.omneAgate.Util.SessionId;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StatisticsActivity extends BaseActivity {

    StatisticsDto statisticsDto;


    int scale, health, level, plugged, status, temperature, voltage;
    String technology;
    boolean present;
    private int batteryLevel = 0;
    Location location;
    //Broadcast receiver for battery
    private final BroadcastReceiver batteryLevelReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            context.unregisterReceiver(this);
            int currentLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0);
            level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
            present = intent.getExtras().getBoolean(BatteryManager.EXTRA_PRESENT);
            status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0);
            technology = intent.getExtras().getString(BatteryManager.EXTRA_TECHNOLOGY);
            temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
            voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);
            if (currentLevel >= 0 && scale > 0) {
                batteryLevel = (currentLevel * 100) / scale;
                Log.e("Heart beat", "Current:" + currentLevel + "::" + "scale:" + scale + "::" + batteryLevel);
            }
            statisticsDto.setScale(scale);
            statisticsDto.setHealth(health);
            statisticsDto.setLevel(level);
            statisticsDto.setPlugged(plugged);
            statisticsDto.setStatus(status);
            statisticsDto.setTemperature(temperature);
            statisticsDto.setVoltage(voltage);
            statisticsDto.setTechnology(technology);
            statisticsDto.setPresent(present);
            changeData();
            Log.e("statisticsDto", statisticsDto.toString());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryLevelReceiver, batteryLevelFilter);
        configureInitialPage();

    }


    @Override
    protected void processMessage(Bundle message, ServiceListenerType what) {

    }

    private void configureInitialPage() {
        try {
            setUpPopUpPageForAdmin();
            if(GlobalAppState.language.equalsIgnoreCase("ta")) {
                ((TextView) findViewById(R.id.title_unsync_bill)).setTextSize(15);
                ((TextView)findViewById(R.id.title_updated_time)).setTextSize(17);
                ((TextView)findViewById(R.id.title_no_beneficary)).setTextSize(17);
                ((TextView)findViewById(R.id.title_devicenumber)).setTextSize(17);
                ((TextView)findViewById(R.id.title_scale)).setTextSize(17);
                ((TextView)findViewById(R.id.title_level)).setTextSize(17);
                ((TextView)findViewById(R.id.title_plugged)).setTextSize(17);
                ((TextView)findViewById(R.id.title_tech)).setTextSize(17);
                ((TextView)findViewById(R.id.title_voltage)).setTextSize(17);
                ((TextView)findViewById(R.id.title_latitude)).setTextSize(17);
                ((TextView)findViewById(R.id.title_versionNumber)).setTextSize(17);
                ((TextView)findViewById(R.id.title_RegCount)).setTextSize(17);
                ((TextView)findViewById(R.id.title_installed_time)).setTextSize(17);
                ((TextView)findViewById(R.id.title_Memoryused)).setTextSize(17);
                ((TextView)findViewById(R.id.title_networkType)).setTextSize(17);
                ((TextView)findViewById(R.id.title_health)).setTextSize(17);
                ((TextView)findViewById(R.id.title_status)).setTextSize(17);
                ((TextView)findViewById(R.id.title_temp)).setTextSize(17);
                ((TextView)findViewById(R.id.title_Present)).setTextSize(17);
                ((TextView)findViewById(R.id.title_longitude)).setTextSize(17);
                ((TextView)findViewById(R.id.title_versionName)).setTextSize(17);
                ((TextView)findViewById(R.id.title_unsynclogin)).setTextSize(17);
                ((TextView)findViewById(R.id.title_cpu_utilaization)).setTextSize(17);
                ((TextView)findViewById(R.id.title_freememory)).setTextSize(17);
                ((TextView)findViewById(R.id.title_totalmemory)).setTextSize(17);
                ((TextView)findViewById(R.id.title_harddisk_size)).setTextSize(17);
                ((TextView)findViewById(R.id.title_simid)).setTextSize(17);




            }
            else {
                ((TextView) findViewById(R.id.title_unsync_bill)).setTextSize(20);
            }
            statisticsDto = new StatisticsDto();
            setTamilText((TextView) findViewById(R.id.top_textView), R.string.statistics);
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            statisticsDto.setVersionNum(pInfo.versionCode);
            statisticsDto.setApkInstalledTime(pInfo.firstInstallTime);
            statisticsDto.setLastUpdatedTime(pInfo.lastUpdateTime);
            statisticsDto.setVersionName(pInfo.versionName);
            long totalFreeMemory = getAvailableInternalMemorySize() + getAvailableExternalMemorySize();
            statisticsDto.setHardDiskSizeFree(formatSize(totalFreeMemory));
            statisticsDto.setUserId(String.valueOf(SessionId.getInstance().getUserId()));
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            statisticsDto.setSimId(telephonyManager.getDeviceId());
            statisticsDto.setDeviceNum((Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID)).toUpperCase());
            statisticsDto.setBeneficiaryCount(FPSDBHelper.getInstance(this).getBeneficiaryCount());
           // statisticsDto.setRegistrationCount(FPSDBHelper.getInstance(this).getBeneficiaryUnSyncCount());
            statisticsDto.setUnSyncBillCount(FPSDBHelper.getInstance(this).getBillUnSyncCount());
            statisticsDto.setCpuUtilisation(String.valueOf(readUsage()));
            ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
            ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            activityManager.getMemoryInfo(mi);
            long availableMegs = mi.availMem / 1048576L;
            long totalMegs = mi.totalMem / 1048576L;
            statisticsDto.setMemoryRemaining(String.valueOf(availableMegs));
            statisticsDto.setTotalMemory(String.valueOf(totalMegs));
            statisticsDto.setMemoryUsed(String.valueOf(totalMegs - availableMegs));
            statisticsDto.setBatteryLevel(batteryLevel);
            GPSService mGPSService = new GPSService(this);
            Location locationB = mGPSService.getLocation();
            location = locationB;
            statisticsDto.setLatitude("" + location.getLatitude());
            statisticsDto.setLongtitude("" + location.getLongitude());
           /* statisticsDto.setLatitude(LocationId.getInstance().getLatitude());
            statisticsDto.setLongtitude(LocationId.getInstance().getLongitude());*/
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null)
                statisticsDto.setNetworkInfo(cm.getActiveNetworkInfo().getTypeName());
            Log.e("Statics", statisticsDto.toString());


        } catch (Exception e) {
            Log.e("statistics error", e.toString(), e);
        } finally {
            setData();
            findViewById(R.id.imageViewBack).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(StatisticsActivity.this, AdminActivity.class));
                    finish();
                }
            });
        }
    }

    private void setData() {
        ((TextView) findViewById(R.id.deviceNum)).setText(statisticsDto.getDeviceNum());
        ((TextView) findViewById(R.id.latitudeDevice)).setText(statisticsDto.getLatitude());
        ((TextView) findViewById(R.id.longitudeData)).setText(statisticsDto.getLongtitude());
        ((TextView) findViewById(R.id.cpuUtil)).setText(statisticsDto.getCpuUtilisation());
        ((TextView) findViewById(R.id.versionNo)).setText(statisticsDto.getVersionNum() + "");
        ((TextView) findViewById(R.id.noOfBeneficiary)).setText(statisticsDto.getBeneficiaryCount() + "");
        ((TextView) findViewById(R.id.unSyncBill)).setText(statisticsDto.getUnSyncBillCount() + "");
        ((TextView) findViewById(R.id.versionName)).setText(statisticsDto.getVersionName() + "");
        ((TextView) findViewById(R.id.regCount)).setText(statisticsDto.getRegistrationCount() + "");
        ((TextView) findViewById(R.id.simId)).setText(statisticsDto.getSimId() + "");
        ((TextView) findViewById(R.id.memUsed)).setText(statisticsDto.getMemoryUsed() + " MB");
        ((TextView) findViewById(R.id.memoryRemain)).setText(statisticsDto.getMemoryRemaining() + " MB");
        ((TextView) findViewById(R.id.totMemory)).setText(statisticsDto.getTotalMemory() + " MB");
        ((TextView) findViewById(R.id.hardDiskSize)).setText(statisticsDto.getHardDiskSizeFree());
        ((TextView) findViewById(R.id.networkType)).setText(statisticsDto.getNetworkInfo());
        ((TextView) findViewById(R.id.unSyncLoginCount)).setText(FPSDBHelper.getInstance(this).getAllLoginHistory().size() + "");
        SimpleDateFormat dateApp = new SimpleDateFormat("dd-MM-yy HH:mm", Locale.getDefault());
        ((TextView) findViewById(R.id.appInstalledTime)).setText(dateApp.format(new Date(statisticsDto.getApkInstalledTime())));
        ((TextView) findViewById(R.id.appUpdateTime)).setText(dateApp.format(new Date(statisticsDto.getLastUpdatedTime())));
    }

    private void changeData() {
        ((TextView) findViewById(R.id.deviceScale)).setText(statisticsDto.getScale() + "");
        ((TextView) findViewById(R.id.batteryPlugged)).setText(statisticsDto.getPlugged() + "");
        ((TextView) findViewById(R.id.batteryTech)).setText(statisticsDto.getTechnology());
        ((TextView) findViewById(R.id.batteryVoltage)).setText(statisticsDto.getVoltage() + "V");
        ((TextView) findViewById(R.id.batteryHealth)).setText(statisticsDto.getHealth() + "");
        ((TextView) findViewById(R.id.batteryLvl)).setText(statisticsDto.getLevel() + "");
        ((TextView) findViewById(R.id.batteryStatus)).setText(statisticsDto.getStatus() + "");
        ((TextView) findViewById(R.id.batteryTemp)).setText(statisticsDto.getTemperature() + " C");
        ((TextView) findViewById(R.id.batteryPresent)).setText(statisticsDto.isPresent() + "");
    }


    private float readUsage() {
        try {
            RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
            String load = reader.readLine();

            String[] toks = load.split(" ");

            long idle1 = Long.parseLong(toks[5]);
            long cpu1 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4])
                    + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

            try {
                Thread.sleep(360);
            } catch (Exception e) {
            }

            reader.seek(0);
            load = reader.readLine();
            reader.close();

            toks = load.split(" ");

            long idle2 = Long.parseLong(toks[5]);
            long cpu2 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[4])
                    + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

            return (float) (cpu2 - cpu1) / ((cpu2 + idle2) - (cpu1 + idle1));

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return 0;
    }

    private boolean externalMemoryAvailable() {
        return android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
    }

    private long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long availableBlocks = stat.getAvailableBlocksLong();
        return availableBlocks * blockSize;
    }

    private long getAvailableExternalMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getAvailableBlocksLong();
            long availableBlocks = stat.getBlockSizeLong();
            return availableBlocks * blockSize;
        } else {
            return 0;
        }
    }

    private String formatSize(long size) {
        String suffix = null;

        if (size >= 1024) {
            suffix = " KB";
            size /= 1024;
            if (size >= 1024) {
                suffix = " MB";
                size /= 1024;
            }
        }

        StringBuilder resultBuffer = new StringBuilder(Long.toString(size));

        int commaOffset = resultBuffer.length() - 3;
        while (commaOffset > 0) {
            resultBuffer.insert(commaOffset, ',');
            commaOffset -= 3;
        }

        if (suffix != null) resultBuffer.append(suffix);
        return resultBuffer.toString();
    }


    @Override
    public void onBackPressed() {
//        unregisterReceiver(batteryLevelReceiver);
        startActivity(new Intent(this, AdminActivity.class));
        finish();
    }


}
