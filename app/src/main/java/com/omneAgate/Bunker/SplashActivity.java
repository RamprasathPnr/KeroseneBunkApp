package com.omneAgate.Bunker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import com.omneAgate.DTO.EnumDTO.ServiceListenerType;
import com.omneAgate.Util.FPSDBHelper;
import com.omneAgate.Util.InsertIntoDatabase;
import com.omneAgate.Util.Util;


//SplashActivity initial activity of this App

public class SplashActivity extends BaseActivity {

    public static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setTamilHeader(((TextView) findViewById(R.id.tamilHeader)), R.string.headerAllPageEnglish);
        setTamil(((TextView) findViewById(R.id.tamilHeader2)), R.string.headerAllPage);
        setTamil(((TextView) findViewById(R.id.tamilHeader3)), R.string.fpsposapplication);
        context = this;
        if (FPSDBHelper.getInstance(this).getFirstSync()) {
            InsertIntoDatabase db = new InsertIntoDatabase(this);
            db.insertIntoDatabase();
            FPSDBHelper.getInstance(this).insertValues();
        }
        try {
            SQLiteDatabase db = new FPSDBHelper(context).getWritableDatabase();
            SharedPreferences sharedpreferences = getSharedPreferences("DBData", Context.MODE_PRIVATE);
            int oldVersion = sharedpreferences.getInt("version", db.getVersion());
            Log.e("onupgrade called splash","oldversion"+oldVersion);
//            FPSDBHelper.getInstance(this).onUpgrade(db, oldVersion, 5);
            TelephonyManager telMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            int simState = telMgr.getSimState();
            switch (simState) {
                case TelephonyManager.SIM_STATE_ABSENT:
                    GlobalAppState.smsAvailable = false;
                    break;
                case TelephonyManager.SIM_STATE_UNKNOWN:
                    GlobalAppState.smsAvailable = false;
                    break;
            }

            String languageCode = FPSDBHelper.getInstance(this).getMasterData("language");
            if (languageCode == null) {
                languageCode = "ta";
            }
            Util.changeLanguage(this, languageCode);
            GlobalAppState.language = languageCode;
        } catch (Exception e) {
            Log.e("SplashActivity", e.toString(), e);
        }
    }

    /*
    *  Database helper called as writable database
    *  Starting services for android application
    *  */
    @Override
    protected void onStart() {
        super.onStart();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("On Stop", "Login On Stop");
    }


    /*Concrete method*/
    @Override
    public void processMessage(Bundle message, ServiceListenerType what) {

    }
}