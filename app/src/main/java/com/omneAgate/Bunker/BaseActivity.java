package com.omneAgate.Bunker;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.gson.Gson;
import com.omneAgate.DTO.ApplicationType;
import com.omneAgate.DTO.EnumDTO.RequestType;
import com.omneAgate.DTO.EnumDTO.ServiceListenerType;
import com.omneAgate.DTO.LogoutDto;
import com.omneAgate.Util.CustomProgressDialog;
import com.omneAgate.Util.FPSDBHelper;
import com.omneAgate.Util.GPSService;
import com.omneAgate.Util.NetworkConnection;
import com.omneAgate.Util.SessionId;
import com.omneAgate.Util.TamilUtil;
import com.omneAgate.Util.Util;
import com.omneAgate.Bunker.dialog.GeoFencingDialog;
import com.omneAgate.Bunker.dialog.LogoutDialog;
import com.omneAgate.service.HttpClientWrapper;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * BaseActivity is the base class for all activities
 */
public abstract class BaseActivity extends Activity {

    //Network connectivity
    NetworkConnection networkConnection;

    //HttpConnection service
    HttpClientWrapper httpConnection;

    //Global application context for this application
    GlobalAppState appState;

    //Progressbar for waiting
    CustomProgressDialog progressBar;

    PopupWindow popupMessage;            //User menu popup

    View layoutOfPopup;                  //Popup window view

    ImageView imageViewUserProfile;    //User profile imageview

    /*Handler used to get response from server*/
    protected final Handler SyncHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            ServiceListenerType type = (ServiceListenerType) msg.obj;
            switch (type) {
                case LOGIN_USER:
                    processMessage(msg.getData(), ServiceListenerType.LOGIN_USER);
                    break;
                case LOGOUT_USER:
                    processMessage(msg.getData(), ServiceListenerType.LOGOUT_USER);
                    break;
                case FPS_INTENT_REQUEST:
                    processMessage(msg.getData(), ServiceListenerType.FPS_INTENT_REQUEST);
                    break;
                case CARD_OTP_REGENERATE:
                    processMessage(msg.getData(), ServiceListenerType.CARD_OTP_REGENERATE);
                    break;
                case QR_CODE:
                    processMessage(msg.getData(), ServiceListenerType.QR_CODE);
                    break;
                case CARD_ACTIVATION:
                    processMessage(msg.getData(), ServiceListenerType.CARD_ACTIVATION);
                    break;
                case CHECKVERSION:
                    processMessage(msg.getData(), ServiceListenerType.CHECKVERSION);
                    break;
                case CARD_REGISTRATION:
                    processMessage(msg.getData(), ServiceListenerType.CARD_REGISTRATION);
                    break;
                case DEVICE_STATUS:
                    processMessage(msg.getData(), ServiceListenerType.DEVICE_STATUS);
                    break;
                case UPGRADE_RESPONSE:
                    processMessage(msg.getData(), ServiceListenerType.UPGRADE_RESPONSE);
                    break;
                case BENEFICIARY_UPDATION:
                    processMessage(msg.getData(), ServiceListenerType.BENEFICIARY_UPDATION);
                    break;
                case SEND_BILL:
                    processMessage(msg.getData(), ServiceListenerType.SEND_BILL);
                    break;
                case DEVICE_REGISTER:
                    processMessage(msg.getData(), ServiceListenerType.DEVICE_REGISTER);
                    break;
                case CARD_OTP:
                    processMessage(msg.getData(), ServiceListenerType.CARD_OTP);
                    break;
                case OPEN_STOCK:
                    processMessage(msg.getData(), ServiceListenerType.OPEN_STOCK);
                    break;
                case CONFIGURATION:
                    processMessage(msg.getData(), ServiceListenerType.CONFIGURATION);
                    break;
                case CLOSE_SALE:
                    Log.e("base activity","default is called");
                    processMessage(msg.getData(), ServiceListenerType.CLOSE_SALE);
                    break;
                default:
                    GlobalAppState.localLogin = true;
                    processMessage(msg.getData(), ServiceListenerType.ERROR_MSG);
                    break;

            }
        }

    };



    /*
     * abstract method for all activity
     * */
    protected abstract void processMessage(Bundle message, ServiceListenerType what);

    /**
     * Tamil text textView typeface
     * input  textView name and id for string.xml
     */
    public void setTamilText(TextView textName, int id) {
        if (GlobalAppState.language.equalsIgnoreCase("ta")) {
            Typeface tfBamini = Typeface.createFromAsset(getAssets(), "fonts/Bamini.ttf");
            textName.setTypeface(tfBamini, Typeface.BOLD);
            textName.setText(TamilUtil.convertToTamil(TamilUtil.BAMINI, getString(id)));
        } else {
            textName.setText(getString(id));
        }
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String languageCode = FPSDBHelper.getInstance(this).getMasterData("language");
        if (languageCode == null) {
            languageCode = "ta";
        }
        Util.changeLanguage(this, languageCode);
        GlobalAppState.language = languageCode;
    }


    public boolean checkLocationDetails() {
        boolean isInsideLimit = true;
        GPSService mGPSService = new GPSService(this);
        Location locationB = mGPSService.getLocation();
        SharedPreferences prefs = getSharedPreferences("FPS", MODE_PRIVATE);
        boolean fencing = prefs.getBoolean("fencing", false);
        if (fencing) {
            String longitude = prefs.getString("longitude", "");
            String latitude = prefs.getString("latitude", "");
            if (StringUtils.isNotEmpty(latitude) && StringUtils.isNotEmpty(longitude)) {
                Location locationA = new Location("point A");
                locationA.setLatitude(Float.parseFloat(latitude));
                locationA.setLongitude(Float.parseFloat(longitude));
                if (locationB != null) {
                    float distance = locationA.distanceTo(locationB);
                    if (distance > 200f) {
                        new GeoFencingDialog(this).show();
                        isInsideLimit = false;
                    }
                    Log.e("distance", distance + ":::::" + distance);
                    Log.e("distance", locationA.getLatitude() + ":::::" + locationA.getLongitude());
                    Log.e("distance", locationB.getLatitude() + ":::::" + locationB.getLongitude());
                }
            }
        }
        return isInsideLimit;
    }

    /**
     * Tamil text textView typeface
     * input  textView name and text string input
     */
    public void setTamilText(TextView textName, String text) {
        if (GlobalAppState.language.equalsIgnoreCase("ta")) {
            Typeface tfBamini = Typeface.createFromAsset(getAssets(), "fonts/Bamini.ttf");
            textName.setTypeface(tfBamini);
            textName.setText(TamilUtil.convertToTamil(TamilUtil.BAMINI, text));
        } else {
            textName.setText(text);
        }
    }

    public void setUpPopUpPage() {
        try {
            layoutOfPopup = LayoutInflater.from(this).inflate(R.layout.popup_user_image, null);
            popupMessage = new PopupWindow(layoutOfPopup, LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            popupMessage.setContentView(layoutOfPopup);
            Util.LoggingQueue(this, "Popup", "Pop up called");
            if (StringUtils.isNotEmpty(SessionId.getInstance().getUserName()))
                ((TextView) layoutOfPopup.findViewById(R.id.popup_userName)).setText(SessionId.getInstance().getUserName().toUpperCase());
            setTamilText(((TextView) layoutOfPopup.findViewById(R.id.welcome_view)), R.string.welcome_view);
            setTamilText(((TextView) layoutOfPopup.findViewById(R.id.last_login)), R.string.last_login);
            setTamilText(((TextView) layoutOfPopup.findViewById(R.id.logout)), R.string.logout);
            setTamilText(((TextView) layoutOfPopup.findViewById(R.id.fps_profile_view)), R.string.fps_profile);
            setTamilText(((TextView) layoutOfPopup.findViewById(R.id.store_details)), R.string.fps_code);
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.getDefault());
            if (SessionId.getInstance().getLastLoginTime() != null)
                ((TextView) layoutOfPopup.findViewById(R.id.popup_last_login)).setText(formatter.format(SessionId.getInstance().getLastLoginTime()).toUpperCase());
            popupMessage.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            if (SessionId.getInstance().getFpsCode() != null)
                ((TextView) layoutOfPopup.findViewById(R.id.popup_store_details)).setText(SessionId.getInstance().getFpsCode().toUpperCase());
            String className = getLocalClassName().toString();
            if (className.contains("ProfileActivity")) {
                layoutOfPopup.findViewById(R.id.fps_profile).setVisibility(View.GONE);
            }
            popupMessage.setOutsideTouchable(true);
            imageViewUserProfile = (ImageView) findViewById(R.id.imageViewUserProfile);
            imageViewUserProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupMessage.showAsDropDown(imageViewUserProfile, 0, 0);

                }
            });
            layoutOfPopup.findViewById(R.id.fps_profile).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupMessage.dismiss();
                    profilePage();

                }
            });
            layoutOfPopup.findViewById(R.id.logout_layout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupMessage.dismiss();
                    userLogoutResponse();
                }
            });
            getNetworkStatus();

        } catch (Exception e) {
            Log.e("BaseActivity", e.toString(), e);
        }
    }
   public void getNetworkStatus(){
       try{
           View viewOnline = findViewById(R.id.onLineOffline);
           TextView textViewOnline = (TextView) findViewById(R.id.textOnline);
           networkConnection = new NetworkConnection(this);
           if (SessionId.getInstance().getSessionId()!= null && StringUtils.isNotEmpty(SessionId.getInstance().getSessionId()) && networkConnection.isNetworkAvailable()) {
               viewOnline.setBackgroundResource(R.drawable.rounded_circle_green);
               textViewOnline.setTextColor(Color.parseColor("#038203"));
               setTamilText(textViewOnline, R.string.onlineText);
               Log.e("loginactivity","Online");
           } else {
               viewOnline.setBackgroundResource(R.drawable.rounded_circle_red);
               textViewOnline.setTextColor(Color.parseColor("#FFFF0000"));
               setTamilText(textViewOnline, R.string.offlineText);
               Log.e("loginactivity","offline");
           }
       }catch (Exception e){
           e.printStackTrace();
       }
   }

    public String unicodeToLocalLanguage(String keyString) {
        String unicodeString = null;
        try {
            unicodeString = new String(keyString.getBytes(), "UTF8");
        } catch (Exception e) {
            Log.e("Exception while UTF", keyString);
        }
        return unicodeString;
    }


    public void setUpPopUpPageForAdmin() {
        try {
            layoutOfPopup = LayoutInflater.from(this).inflate(R.layout.popup_admin_image, null);
            popupMessage = new PopupWindow(layoutOfPopup, LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            popupMessage.setContentView(layoutOfPopup);
            Util.LoggingQueue(this, "Popup", "Pop up called");
            if (StringUtils.isNotEmpty(SessionId.getInstance().getUserName()))
                ((TextView) layoutOfPopup.findViewById(R.id.popup_userName)).setText(SessionId.getInstance().getUserName().toUpperCase());
            setTamilText(((TextView) layoutOfPopup.findViewById(R.id.welcome_view)), R.string.welcome_view);
            setTamilText(((TextView) layoutOfPopup.findViewById(R.id.last_login)), R.string.last_login);
            setTamilText(((TextView) layoutOfPopup.findViewById(R.id.logout)), R.string.logout);
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm a",Locale.getDefault());
            if (SessionId.getInstance().getLastLoginTime() != null)
                ((TextView) layoutOfPopup.findViewById(R.id.popup_last_login)).setText(formatter.format(SessionId.getInstance().getLastLoginTime()).toUpperCase());

            popupMessage.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            popupMessage.setOutsideTouchable(true);
            imageViewUserProfile = (ImageView) findViewById(R.id.imageViewUserProfile);
            imageViewUserProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupMessage.showAsDropDown(imageViewUserProfile, 0, 0);

                }
            });
            layoutOfPopup.findViewById(R.id.logout_layout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupMessage.dismiss();
                    userLogoutResponse();
                }
            });
//            View viewOnline = findViewById(R.id.onLineOffline);
//            TextView textViewOnline = (TextView) findViewById(R.id.textOnline);
//            networkConnection = new NetworkConnection(this);
//            if (StringUtils.isEmpty(SessionId.getInstance().getSessionId()) || !networkConnection.isNetworkAvailable()) {
//                viewOnline.setBackgroundResource(R.drawable.rounded_circle_red);
//                textViewOnline.setTextColor(Color.parseColor("#FFFF0000"));
//                setTamilText(textViewOnline, R.string.offlineText);
//            } else {
//                viewOnline.setBackgroundResource(R.drawable.rounded_circle_green);
//                textViewOnline.setTextColor(Color.parseColor("#038203"));
//                setTamilText(textViewOnline, R.string.onlineText);
//            }
            getNetworkStatus();
        } catch (Exception e) {
            Log.e("BaseActivity", e.toString(), e);
        }
    }

    private void profilePage() {
        startActivity(new Intent(this, ProfileActivity.class));
        Util.LoggingQueue(this, "Pro activity", "calling for profile activity");
        finish();
    }

    //After user give logout this method will call dialog
    private void userLogoutResponse() {
        LogoutDialog logout = new LogoutDialog(this);
        Util.LoggingQueue(this, "Logout", "Logout dialog appearence");
        logout.show();
    }

    /**
     * Tamil text textView typeface
     * input  textView name and id for string.xml
     */
    public void setTamil(TextView textName, int id) {
        Typeface tfBamini = Typeface.createFromAsset(getAssets(), "fonts/Bamini.ttf");
        textName.setTypeface(tfBamini);
        textName.setText(TamilUtil.convertToTamil(TamilUtil.BAMINI, getString(id)));
    }

    //Logout request from user success and send to server
    public void logOutSuccess() {
      //  SessionId.getInstance().setSessionId("");
        networkConnection = new NetworkConnection(this);
        String logoutString = "OFFLINE_LOGOUT";
        if (networkConnection.isNetworkAvailable()) {
            try {
                logoutString = "ONLINE_LOGOUT";
                String url = "/login/logout";

                LogoutDto logoutDto = new LogoutDto();
                logoutDto.setSessionId(SessionId.getInstance().getSessionId());
                logoutDto.setLogoutStatus(logoutString);
                logoutDto.setAppType(ApplicationType.KEROSENE_BUNK);
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
                String dateStr = df.format(new Date());
                logoutDto.setLogoutTime(dateStr);
                String logout = new Gson().toJson(logoutDto);
                Log.e("base activity", "logout..." + logout);
                StringEntity se = new StringEntity(logout, HTTP.UTF_8);

                httpConnection = new HttpClientWrapper();
                httpConnection.sendRequest(url, null, ServiceListenerType.LOGOUT_USER,
                        SyncHandler, RequestType.POST, se, this);
            }catch (Exception e){
                Log.e("LOGOut","Exception occured"+e);
            }
        }
        FPSDBHelper.getInstance(this).updateLoginHistory(SessionId.getInstance().getTransactionId(), logoutString);
      //  FPSDBHelper.getInstance(this).closeConnection();
        startActivity(new Intent(this, LoginActivity.class));
        finish();

    }
}
