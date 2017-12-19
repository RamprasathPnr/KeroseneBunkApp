package com.omneAgate.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.omneAgate.Util.FPSDBHelper;


/**
 * Created by user1 on 2/2/16.
 */
public class AlarmReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        String days = FPSDBHelper.getInstance(context).getMasterData("purgeBill");
        int purgeDays = Integer.parseInt(days);
        Log.e("purging days...........", "" + purgeDays);

        if(purgeDays > 0) {
            FPSDBHelper.getInstance(context).purge(purgeDays+1);
        }
    }
}
