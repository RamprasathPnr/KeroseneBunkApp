package com.omneAgate.activityKerosene;

import android.app.Application;
import android.content.Context;

import com.omneAgate.TransactionController.SMSForCardListener;
import com.omneAgate.TransactionController.SMSListener;
import com.omneAgate.TransactionController.SmsRegistrationConnector;
import com.omneAgate.Util.FontsOverride;
import com.omneAgate.Util.Queue;

import lombok.Data;


/**
 * Used to create application variable all over application
 */
@Data
public class GlobalAppState extends Application {

    Long fpsId;    /*FPS id from server*/

    public final Queue queue = new Queue();//Queue used for log in entire application

    public final boolean isLoggingEnabled = false; //Check loging enabled

    public static String language; //language of user

    public static String deviceId; //Device id

    public static int transactionType;

    public static SMSListener listener;

    public static SMSForCardListener smsListener;

    public String refId;

    public static Context context;

    public static String serverUrl;

    @Override
    public void onCreate() {
        super.onCreate();
        FontsOverride.setDefaultFont(this, "monospace", "fonts/Bamini.ttf");
    }

}
