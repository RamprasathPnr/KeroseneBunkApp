package com.omneAgate.Bunker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.omneAgate.DTO.EnumDTO.ServiceListenerType;
import com.omneAgate.DTO.VersionDto;
import com.omneAgate.Util.CustomProgressDialog;
import com.omneAgate.Util.FPSDBHelper;
import com.omneAgate.Util.Util;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by user1 on 17/8/15.
 */
public class VersionUpgradeInfo extends BaseActivity {
    List<VersionDto> versionupgrade;
    VersionDto versioninfo = new VersionDto();
    LinearLayout transactionLayout;

    @Override
    protected void processMessage(Bundle message, ServiceListenerType what) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.versionupgrade_activity);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        configureInitialPage();
        Duplicate();
        new fpsVersionUpgradeTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");



        findViewById(R.id.imageViewBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void configureInitialPage() {
        setUpPopUpPage();
        setTamilText((TextView) findViewById(R.id.top_textView), R.string.version_upgrade);
        transactionLayout  = (LinearLayout) findViewById(R.id.listView_linearLayout_stock_status);

    }
    private void  Duplicate()
    {
       /* FPSDBHelper.getInstance(VersionUpgradeInfo.this).insertTableUpgrade(12, "data", "UPGRADE_START ", "Success", 13, "12","123");
        FPSDBHelper.getInstance(VersionUpgradeInfo.this).insertTableUpgrade(12, "data", "BACKUP_START", "Success", 14, "12", "123");
        FPSDBHelper.getInstance(VersionUpgradeInfo.this).insertTableUpgrade(12, "data", "BACKUP_END", "Success", 14, "12", "123");
        FPSDBHelper.getInstance(VersionUpgradeInfo.this).insertTableUpgrade(12, "data", "DOWNLOAD_START", "Success", 16, "12", "123");
        FPSDBHelper.getInstance(VersionUpgradeInfo.this).insertTableUpgrade(12, "data", "EXCUTION", "Success", 17, "12", "123");*/
    }
    @Override
    public void onBackPressed() {

        startActivity(new Intent(this, AdminActivity.class));
        finish();
    }
    private void VersionInfo(List<VersionDto> versionu) {
        try
        {
            versionupgrade = versionu;
            versioninfo = new VersionDto();
            LayoutInflater lin = LayoutInflater.from(VersionUpgradeInfo.this);
            int sno = 1;
            for (VersionDto version : versionupgrade) {

                transactionLayout.addView(returnView(lin,sno,version.getCurrentVersion(),version.getPreviousVersion(),version.getState(),version.getStatus() ));
                sno++;

            }
        } catch (Exception e) {
            Log.e("Error", e.toString(), e);
        }
    }

    private View returnView(LayoutInflater entitle,int sno,String newversion,String oldversion,String commands,String status) {
        View convertView = entitle.inflate(R.layout.adpter_versionupgrade, null);
        TextView snoTv = (TextView) convertView.findViewById(R.id.sno);
        TextView newversionTv = (TextView) convertView.findViewById(R.id.newversion);
        TextView oldversionTv = (TextView) convertView.findViewById(R.id.oldversion);
        TextView descriptionTv = (TextView) convertView.findViewById(R.id.description);
        TextView statustv = (TextView) convertView.findViewById(R.id.status);
        Button viewMore = (Button) convertView.findViewById(R.id.viewmore);
        RelativeLayout unitEntitlement = (RelativeLayout)convertView.findViewById(R.id.unitEntitlement);
        LinearLayout commodityBackground = (LinearLayout)convertView.findViewById(R.id.commodityBackground);
        LinearLayout viewmo = (LinearLayout)convertView.findViewById(R.id.viewmo);
        LinearLayout stat = (LinearLayout)convertView.findViewById(R.id.stat);
        LinearLayout lin = (LinearLayout)convertView.findViewById(R.id.lin);

        viewMore.setId(sno-1);
        snoTv.setText("" +sno);
        newversionTv.setText(""+newversion);
        oldversionTv.setText(""+oldversion);
        descriptionTv.setText(status);
        statustv.setText(commands);
        int[] subColor = getResources().getIntArray(R.array.subColor);
        int[] unitColor = getResources().getIntArray(R.array.unitColor);
        int[] mainColor = getResources().getIntArray(R.array.mainColor);
        /*unitEntitlement.setBackgroundColor(unitColor[sno % 6]);
        commodityBackground.setBackgroundColor(unitColor[sno % 6]);
        viewmo.setBackgroundColor(unitColor[sno % 6]);
        stat.setBackgroundColor(unitColor[sno % 6]);
        lin.setBackgroundColor(unitColor[sno % 6]);*/
        viewMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    int itemIndex = view.getId();
                    Intent nextStockIntent;
                    String currentVersion = versionupgrade.get(itemIndex).getCurrentVersion();
                    String oldVersion = versionupgrade.get(itemIndex).getPreviousVersion();
                    String status = versionupgrade.get(itemIndex).getStatus();
                    String state = versionupgrade.get(itemIndex).getState();
                    String descrption = versionupgrade.get(itemIndex).getDescription();
                    String dateCreate = versionupgrade.get(itemIndex).getCreatedTime();
                    String dateUpdate = versionupgrade.get(itemIndex).getUpdatedTime();
                    nextStockIntent = new Intent(getApplicationContext(), VersionUpgradeDetail.class);
                    nextStockIntent.putExtra("currVersion", currentVersion);
                    nextStockIntent.putExtra("oldVersion", oldVersion);
                    nextStockIntent.putExtra("status", status);
                    nextStockIntent.putExtra("state", state);
                    nextStockIntent.putExtra("descrption", descrption);
                    SimpleDateFormat dateApp = new SimpleDateFormat("dd-MM-yy HH:mm", Locale.getDefault());
                    nextStockIntent.putExtra("dateCreate", dateCreate);
                    nextStockIntent.putExtra("dateUpdate",dateCreate);
                    startActivity(nextStockIntent);

                } catch (Exception e) {

                    Log.e("Error",e.toString(),e);

                }
            }
        });
        return convertView;
    }

    private class fpsVersionUpgradeTask extends AsyncTask<String, Void, List<VersionDto>> {

        // can use UI thread here
        protected void onPreExecute() {
            try {
                progressBar = new CustomProgressDialog(VersionUpgradeInfo.this);
                progressBar.show();
            } catch (Exception e) {
                Log.e("Error in Progress", e.toString(), e);
            }
        }

        // automatically done on worker thread (separate from UI thread)
        protected List<VersionDto> doInBackground(final String... args) {
            return FPSDBHelper.getInstance(VersionUpgradeInfo.this).getVersionInfo();
        }

        // can use UI thread here
        protected void onPostExecute(final List<VersionDto> result) {
            Log.e("productDtoList", "" + result.toString());
            if (progressBar != null) {
                progressBar.dismiss();
            }
            if(result.size()!=0)
            {
                VersionInfo(result);
            }
            else
            {
                Util.messageBar(VersionUpgradeInfo.this, getString(R.string.no_records));

            }

        }
    }


}

