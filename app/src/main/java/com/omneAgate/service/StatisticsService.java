package com.omneAgate.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Environment;
import android.os.IBinder;
import android.os.StatFs;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.gson.Gson;
import com.omneAgate.DTO.StatisticsDto;
import com.omneAgate.Util.FPSDBHelper;
import com.omneAgate.Util.LocationId;
import com.omneAgate.Util.NetworkConnection;
import com.omneAgate.Util.SessionId;
import com.omneAgate.Util.Util;
import com.omneAgate.Bunker.R;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.URI;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Service for checking connection of server
 */
public class StatisticsService extends Service {

    StatisticsDto statisticsDto;

    Timer heartBeatTimer;

    StatisticsTask heartBeatTimerTask;
    int scale, health, level, plugged, status, temperature, voltage;
    String technology;
    boolean present;
    private int batteryLevel = 0; //Battery level variable
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
                Log.i("Heart beat", "Current:" + currentLevel + "::" + "scale:" + scale + "::" + batteryLevel);
            }
        }
    };
    private String serverUrl = "";

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryLevelReceiver, batteryLevelFilter);
        statisticsDto = new StatisticsDto();
        serverUrl = FPSDBHelper.getInstance(this).getMasterData("serverUrl");
        heartBeatTimer = new Timer();
        heartBeatTimerTask = new StatisticsTask();
        Util.LoggingQueue(this, "Info", "Service  HeartBeat created");
        Long timerWaitTime = Long.parseLong(getString(R.string.serviceTimeout)) * 12;
        heartBeatTimer.schedule(heartBeatTimerTask, 1000, timerWaitTime);
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            statisticsDto.setVersionNum(pInfo.versionCode);
            statisticsDto.setApkInstalledTime(pInfo.firstInstallTime);
            statisticsDto.setLastUpdatedTime(pInfo.lastUpdateTime);
            statisticsDto.setVersionName(pInfo.versionName);
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            statisticsDto.setNetworkInfo(cm.getActiveNetworkInfo().getTypeName());
            long totalFreeMemory = getAvailableInternalMemorySize() + getAvailableExternalMemorySize();
            statisticsDto.setHardDiskSizeFree(formatSize(totalFreeMemory));
            statisticsDto.setUserId(String.valueOf(SessionId.getInstance().getUserId()));
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            statisticsDto.setSimId(telephonyManager.getDeviceId());
        } catch (Exception e) {
            Log.e("statistics error", e.toString(), e);
        }

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
            suffix = "KB";
            size /= 1024;
            if (size >= 1024) {
                suffix = "MB";
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
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onDestroy() {
        try {
            if (heartBeatTimer != null) {
                heartBeatTimer.cancel();
                heartBeatTimer = null;
            }
        } catch (Exception e) {
            Log.e("Error", "Error in Service", e);
        }

    }


    /*return http POST method using parameters*/
    private HttpResponse requestType(URI website, StringEntity entity) throws IOException {

        HttpParams httpParameters = new BasicHttpParams();
        int timeoutConnection = 50000;
        HttpConnectionParams.setConnectionTimeout(httpParameters,
                timeoutConnection);
        int timeoutSocket = 50000;
        HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
        HttpClient client = new DefaultHttpClient(httpParameters);
        HttpPost postRequest = new HttpPost();
        postRequest.setURI(website);
        postRequest.setHeader("Content-Type", "application/json");
        postRequest.setHeader("Store_type", "fps");
        postRequest.setHeader("Cookie", "JSESSIONID=" + SessionId.getInstance().getSessionId());
        postRequest.setHeader("Cookie", "SESSION=" + SessionId.getInstance().getSessionId());
        postRequest.setEntity(entity);
        return client.execute(postRequest);
    }

    private StatisticsDto getAllStatisticsData() {
        statisticsDto.setDeviceNum((Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID)).toUpperCase());
        statisticsDto.setScale(scale);
        statisticsDto.setHealth(health);
        statisticsDto.setLevel(level);
        statisticsDto.setPlugged(plugged);
        statisticsDto.setStatus(status);
        statisticsDto.setTemperature(temperature);
        statisticsDto.setVoltage(voltage);
        statisticsDto.setTechnology(technology);
        statisticsDto.setPresent(present);
        statisticsDto.setBeneficiaryCount(FPSDBHelper.getInstance(this).getBeneficiaryCount());
        statisticsDto.setRegistrationCount(FPSDBHelper.getInstance(this).getBeneficiaryUnSyncCount());
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
        return statisticsDto;
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

    class StatisticsTask extends TimerTask {

        @Override
        public void run() {
            try {
               /* MigrationService migrate = new MigrationService(StatisticsService.this);
                migrate.migrationOutData();*/
                Util.LoggingQueue(StatisticsService.this, "Info", "Started to send HeartBeat message ");
                if (StringUtils.isNotEmpty(SessionId.getInstance().getSessionId()) && SessionId.getInstance().getSessionId().length() > 0) {
                    NetworkConnection network = new NetworkConnection(StatisticsService.this);
                    if (network.isNetworkAvailable()) {
                        statisticsDto.setBatteryLevel(batteryLevel);
                        statisticsDto.setLatitude(LocationId.getInstance().getLatitude());
                        statisticsDto.setLongtitude(LocationId.getInstance().getLongitude());
                        statisticsDto = getAllStatisticsData();
                        new ConnectionTestHearBeat().execute("");
                    }
                } else {
                    Util.LoggingQueue(StatisticsService.this, "Error", "Session is null or zero");
                }
                Util.LoggingQueue(StatisticsService.this, "Info", "End of HeartBeat message ");
            } catch (Exception e) {
                Util.LoggingQueue(StatisticsService.this, "Error", e.getMessage());
            }
        }

    }

    //Async   task for connection heartbeat
    private class ConnectionTestHearBeat extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... f_url) {
            BufferedReader in = null;
            try {
                String url = serverUrl + "/pos/addstatistics";
                URI website = new URI(url);
                Log.i("statistics", statisticsDto.toString());
                Util.LoggingQueue(StatisticsService.this, "Info", "Request message " + statisticsDto.toString());
                String heartBeatData = new Gson().toJson(statisticsDto);
                StringEntity entity = new StringEntity(heartBeatData, HTTP.UTF_8);
                HttpResponse response = requestType(website, entity);
                in = new BufferedReader(new InputStreamReader(response
                        .getEntity().getContent()));
                StringBuffer sb = new StringBuffer("");
                String l;
                String nl = System.getProperty("line.separator");
                while ((l = in.readLine()) != null) {
                    sb.append(l + nl);
                }
                in.close();
                String responseData = sb.toString();
                Util.LoggingQueue(StatisticsService.this, "Info", "Response message " + responseData);
                return responseData;
            } catch (Exception e) {
                Util.LoggingQueue(StatisticsService.this, "Error", "Network exception" + e.getMessage());
                Log.e("Statistics excep", e.toString());
                try {
                    if (in != null)
                        in.close();
                } catch (Exception e1) {
                    // Intentional swallow of exception
                }
            }
            return null;
        }


        // Once Music File is downloaded
        @Override
        protected void onPostExecute(String file_url) {

        }
    }

}
