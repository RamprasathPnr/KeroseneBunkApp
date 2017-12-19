package com.omneAgate.Bunker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import com.omneAgate.DTO.EnumDTO.CommonStatuses;
import com.omneAgate.DTO.EnumDTO.RequestType;
import com.omneAgate.DTO.EnumDTO.ServiceListenerType;
import com.omneAgate.DTO.UpgradeDetailsDto;
import com.omneAgate.Util.Constants.FPSDBConstants;
import com.omneAgate.Util.FPSDBHelper;
import com.omneAgate.Util.LocalDbRecoveryProcess;
import com.omneAgate.Util.NetworkConnection;
import com.omneAgate.Util.SessionId;
import com.omneAgate.Util.Util;
import com.omneAgate.service.HttpClientWrapper;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

//Auto Upgradation of apk file
public class AutoUpgrationActivity extends BaseActivity {

    String refId = "", serverRefId = "";
    Integer oldVersion, newVersion;
    String downloadApkPath;
    //Downloading the progressbar
    private ProgressBar progressBar;
    // Download percentage
    private TextView tvUploadCount;




    @Override
    public void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_auto_upgradation_version);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        progressBar = (ProgressBar) findViewById(R.id.autoUpgradeprogressBar);
        progressBar.setVisibility(View.VISIBLE);
        tvUploadCount = (TextView) findViewById(R.id.tvUploadCount);
        SimpleDateFormat regDate = new SimpleDateFormat("ddMMyyhhmmss", Locale.getDefault());
        refId = regDate.format(new Date());
        downloadApkPath = getIntent().getStringExtra("downloadPath");
        Log.i("downloadApkPath", downloadApkPath);
        newVersion = getIntent().getIntExtra("newVersion", 0);
        try {
            setTamilHeader((TextView) findViewById(R.id.login_actionbar), R.string.headerAllPageEnglish);
            setTamil(((TextView) findViewById(R.id.login_actionbarTamil)), R.string.headerAllPage);
            findViewById(R.id.popupMenu).setVisibility(View.GONE);
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            oldVersion = pInfo.versionCode;
            UpgradeDetailsDto upgradeDto = FPSDBHelper.getInstance(this).getUpgradeData();
            upgradeDto.setCreatedTime(new Date().getTime());
            upgradeDto.setPreviousVersion(pInfo.versionCode);
            upgradeDto.setCurrentVersion(newVersion);
            upgradeDto.setStatus(CommonStatuses.UPDATE_START);
            upgradeDto.setDeviceNum(Settings.Secure.getString(
                    getContentResolver(), Settings.Secure.ANDROID_ID).toUpperCase());
            upgradeDatas(upgradeDto, ServiceListenerType.CHECKVERSION);
            View viewOnline = findViewById(R.id.onLineOffline);
            TextView textViewOnline = (TextView) findViewById(R.id.textOnline);
            networkConnection = new NetworkConnection(this);
            if (StringUtils.isEmpty(SessionId.getInstance().getSessionId()) || !networkConnection.isNetworkAvailable()) {
                viewOnline.setBackgroundResource(R.drawable.rounded_circle_red);
                textViewOnline.setTextColor(Color.parseColor("#FFFF0000"));
                setTamilText(textViewOnline, R.string.offlineText);
            } else {
                viewOnline.setBackgroundResource(R.drawable.rounded_circle_green);
                textViewOnline.setTextColor(Color.parseColor("#038203"));
                setTamilText(textViewOnline, R.string.onlineText);
            }
        } catch (Exception e) {
            Log.e("AutoUpgrade", e.toString(), e);
            FPSDBHelper.getInstance(this).insertTableUpgrade(oldVersion, "Upgrade failed because:" + e.toString(), "fail", "FAILURE", newVersion, refId, serverRefId);
            Util.messageBar(this, getString(R.string.internalError));
        }

    }

    private void upgradeDatas(UpgradeDetailsDto upgradeDto, ServiceListenerType type) throws Exception {
        httpConnection = new HttpClientWrapper();
        String checkVersion = new Gson().toJson(upgradeDto);
        StringEntity se = new StringEntity(checkVersion, HTTP.UTF_8);
        //String url = "/upgradedetails/adddetails";
        String url = "/upgradedetails/kerosenebunk/adddetails";
        Util.LoggingQueue(this, "Device Register Version", "Checking version of apk in device");
        httpConnection.sendRequest(url, null, type,SyncHandler, RequestType.POST, se, this);
    }

    public void showPopupMenu(View v) {

    }

    /**
     * Tamil text textView typeface
     * input  textView name and id for string.xml
     */
    public void setTamilHeader(TextView textName, int id) {
        Typeface tfBamini = Typeface.createFromAsset(getAssets(), "Impact.ttf");
        textName.setTypeface(tfBamini);
        textName.setText(getString(id));
    }


    private void getFutureFile(String path) {

        Ion.with(AutoUpgrationActivity.this).load(downloadApkPath)
                .progressBar(progressBar)
                .progressHandler(new ProgressCallback() {
                    @Override
                    public void onProgress(long downloaded, long total) {
                        double ratio = downloaded / (double) total;
                        DecimalFormat percentFormat = new DecimalFormat("#.#%");
                        tvUploadCount.setText(percentFormat.format(ratio));

                    }
                })
                .write(new File(path))
                .setCallback(new FutureCallback<File>() {
                    @Override
                    public void onCompleted(Exception e, File file) {
                        if (e != null) {
                            Toast.makeText(AutoUpgrationActivity.this, "Error downloading file.Try again", Toast.LENGTH_LONG).show();
                            FPSDBHelper.getInstance(AutoUpgrationActivity.this).insertTableUpgrade(oldVersion, "Download failed because of" + e.toString(), "failed", "DOWNLOAD_FAIL", newVersion, refId, serverRefId);
                            onBackPressed();
                            return;
                        }
                        upGradeComplete();
                        FPSDBHelper.getInstance(AutoUpgrationActivity.this).insertTableUpgrade(oldVersion, "Download completed successfully in:" + file.getAbsolutePath(), "success", "DOWNLOAD_END", newVersion, refId, serverRefId);
                        Intent i = new Intent();
                        i.setAction(Intent.ACTION_VIEW);
                        i.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                        startActivity(i);
                        FPSDBHelper.getInstance(AutoUpgrationActivity.this).insertTableUpgradeExec(oldVersion, "Download completed successfully in:" + file.getAbsolutePath(), "success", "EXECUTION", newVersion, refId, serverRefId);
                        finish();
                    }

                });
    }


    @Override
    protected void processMessage(Bundle message, ServiceListenerType what) {
        switch (what) {
            case CHECKVERSION:
                setData(message);
                break;
            default:
                Log.e("AutoUpgrade", "Auto upgrade");
                break;
        }

    }

    private void setData(Bundle message) {
        try {
            String response = message.getString(FPSDBConstants.RESPONSE_DATA);
            if (response != null) {
                GsonBuilder gsonBuilder = new GsonBuilder();
                Gson gson = gsonBuilder.create();
                Util.LoggingQueue(this, "Mobile OTP Reg", "Response for otp:" + response);
                UpgradeDetailsDto benefActivNewDto = gson.fromJson(response, UpgradeDetailsDto.class);
                serverRefId = benefActivNewDto.getReferenceNumber();
                downloadStart();
            }
        } catch (Exception e) {
            Log.e("AutoUpgrade ", e.toString(), e);
            FPSDBHelper.getInstance(this).insertTableUpgrade(oldVersion, "Upgrade failed because:" + e.toString(), "fail", "FAILURE", newVersion, refId, serverRefId);

        }
    }


    private void downloadStart() throws Exception {
        PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        oldVersion = pInfo.versionCode;
        FPSDBHelper.getInstance(this).insertTableUpgrade(oldVersion, "Upgrade Available in the paths:" + downloadApkPath, "success", "UPGRADE_START", newVersion, refId, serverRefId);
        String dbName = refId + ".db";
        FPSDBHelper.getInstance(this).insertTableUpgrade(oldVersion, "Back up the DB file:" + dbName, "success", "BACKUP_START", newVersion, refId, serverRefId);
        LocalDbRecoveryProcess localDbRecoveryPro = new LocalDbRecoveryProcess(this);
        if (localDbRecoveryPro.backupDb(false, refId, dbName, serverRefId)) {
            FPSDBHelper.getInstance(this).insertTableUpgrade(oldVersion, "Back up the DB file finished:" + dbName, "success", "BACKUP_END", newVersion, refId, serverRefId);
            File file = new File(Environment.getExternalStorageDirectory(), "POS");
            if (!file.exists()) {
                file.mkdir();
            }
            final String path = Environment.getExternalStorageDirectory() + "/POS/POS.apk";
            FPSDBHelper.getInstance(this).insertTableUpgrade(pInfo.versionCode, "Download starts:" + path, "success", "DOWNLOAD_START", newVersion, refId, serverRefId);
            getFutureFile(path);
        } else {
            Util.messageBar(this, getString(R.string.internalError));
        }
    }


    private void upGradeComplete() {
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            UpgradeDetailsDto upgradeDto = FPSDBHelper.getInstance(this).getUpgradeData();
            upgradeDto.setCreatedTime(new Date().getTime());
            upgradeDto.setPreviousVersion(pInfo.versionCode);
            upgradeDto.setCurrentVersion(newVersion);
            upgradeDto.setReferenceNumber(serverRefId);
            upgradeDto.setStatus(CommonStatuses.APK_DOWNLOAD);
            upgradeDto.setDeviceNum(Settings.Secure.getString(
                    getContentResolver(), Settings.Secure.ANDROID_ID).toUpperCase());
            upgradeDatas(upgradeDto, ServiceListenerType.CARD_ACTIVATION);
        } catch (Exception e) {
            Log.e("AutoUpgrade", e.toString(), e);
        }

    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }


}