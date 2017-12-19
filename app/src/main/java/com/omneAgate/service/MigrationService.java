package com.omneAgate.service;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.omneAgate.DTO.UserDto.MigrationOutDTO;
import com.omneAgate.Util.FPSDBHelper;

import java.util.List;

/**
 * Created for Migration Service.
 */
public class MigrationService {

    Context context;

    List<MigrationOutDTO> migrationOut;

    private String serverUrl = "";

    public MigrationService(Context context) {
        this.context = context;
    }


    public void migrationOutData() {
        migrationOut = FPSDBHelper.getInstance(context).getMigrationOut();
        serverUrl = FPSDBHelper.getInstance(context).getMasterData("serverUrl");
        Log.e("MigrationService","migrationOut List size : "+migrationOut.size());
        if (migrationOut.size() > 0) {
            new MigrationOutTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, migrationOut);
        }
    }
    //Local Migration Out Process
    private class MigrationOutTask extends AsyncTask< List <MigrationOutDTO>, Void, String> {


        @Override
        protected String doInBackground(List<MigrationOutDTO>... migrationOutList) {

            Log.e("MigrationService","<====== Migration started  ======>");

            List<MigrationOutDTO> migrationOut = migrationOutList[0];
            for (int i = 0; i < migrationOut.size(); i++) {
                try {
                    FPSDBHelper.getInstance(context).updateBeneficiary(migrationOut.get(i));
                    FPSDBHelper.getInstance(context).updateMigrationOut(migrationOut.get(i));
                }catch (Exception e ){
                    Log.e("Exception ","While creating MigrationTask "+e.toString());
                    e.printStackTrace();
                }
            }
            Log.e("MigrationService","<====== Migration Ended  ======>");
            return null;
        }
    }
}
