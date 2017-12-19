package com.omneAgate.service;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.omneAgate.DTO.ApplicationType;
import com.omneAgate.DTO.LoginHistoryDto;
import com.omneAgate.Util.FPSDBHelper;
import com.omneAgate.Util.NetworkConnection;
import com.omneAgate.Util.SessionId;
import com.omneAgate.Util.Util;
import com.omneAgate.Bunker.R;

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
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Remote logging service class
 */
public class LoginHistoryService extends Service {

    Timer loginHistoryTimer;
    RemoteLogTimerTask loginHistoryTimerTask;
    List<LoginHistoryDto> loginHistoryList;
    private String serverUrl = "";

    public void onCreate() {

        serverUrl = FPSDBHelper.getInstance(this).getMasterData("serverUrl");

        loginHistoryTimer = new Timer();
        loginHistoryTimerTask = new RemoteLogTimerTask();
        Long timerWaitTime = Long.parseLong(getString(R.string.serviceTimeout));
        loginHistoryTimer.schedule(loginHistoryTimerTask, 0, timerWaitTime);
    }

    @Override
    public void onDestroy() {
        try {
            if (loginHistoryTimer != null) {
                loginHistoryTimer.cancel();
                loginHistoryTimer = null;
            }
        } catch (Exception e) {
            Log.e("Remote log ", "Error in Service", e);
        }

    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    class RemoteLogTimerTask extends TimerTask {

        @Override
        public void run() {
            loginHistoryList = new ArrayList<>();
            loginHistoryList = FPSDBHelper.getInstance(LoginHistoryService.this).getAllLoginHistory();
            Log.e("loginHistoryList size", "loginHistoryList:" + loginHistoryList);
            Util.LoggingQueue(LoginHistoryService.this, "Info", "Retrieved number of unsent loginhistory [" + loginHistoryList.size() + "]");
            NetworkConnection network = new NetworkConnection(LoginHistoryService.this);
            if (network.isNetworkAvailable()) {
                for (LoginHistoryDto loginHistory : loginHistoryList) {
                    if(loginHistory.getLogoutType() == null){
                        continue;
                    }
                    loginHistory.setAppType(ApplicationType.KEROSENE_BUNK);

                    loginHistory.setDeviceId(Settings.Secure.getString(
                            LoginHistoryService.this.getContentResolver(), Settings.Secure.ANDROID_ID).toUpperCase());
                    Log.e("LoginHistroy", "Get transaction Id" + loginHistory.getTransactionId());
                    Util.LoggingQueue(LoginHistoryService.this, "Info", "Processing historyId id" + loginHistory.getTransactionId());
                  //  loginHistory.setSessionid(SessionId.getInstance().getSessionId());
                    new LoginHistoryAsync().execute(loginHistory);
                }
            }
            Util.LoggingQueue(LoginHistoryService.this, "Info", "Waking up");
        }

    }


    //Remote logging service server async task
    class LoginHistoryAsync extends AsyncTask<LoginHistoryDto, String, String> {

        @Override
        protected String doInBackground(LoginHistoryDto... logs) {
            BufferedReader in = null;
            try {
                String url = serverUrl + "/fpsoffline/adddetails";
                URI website = new URI(url);
                String bill = new Gson().toJson(logs[0]);
                Log.e("login histroy service","Login Histroy dto"+bill.toString());
                StringEntity entity = new StringEntity(bill, HTTP.UTF_8);
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
                return responseData;
            } catch (Exception e) {
                Util.LoggingQueue(LoginHistoryService.this, "Error", "Network exception" + e.getMessage());
                try {
                    if (in != null)
                        in.close();
                } catch (Exception e1) {
                    // Intentional swallow of exception
                }
            }
            return null;
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

        @Override
        protected void onPostExecute(String response) {
            try {
                Util.LoggingQueue(LoginHistoryService.this, "LoginHistory", "Response LoginHistory:" + response);
                if (response != null) {
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    Gson gson = gsonBuilder.create();
                    Log.e("login histroy service","Histroy dto"+response.toString());
                    LoginHistoryDto loginHistoryDto = gson.fromJson(response, LoginHistoryDto.class);
                    FPSDBHelper.getInstance(LoginHistoryService.this).updateLoginHistory(loginHistoryDto.getTransactionId());
                }
            } catch (Exception e) {
                Util.LoggingQueue(LoginHistoryService.this, "Error", "Output exception" + e.getMessage());
            }
        }
    }


}