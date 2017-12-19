package com.omneAgate.Bunker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.provider.Settings;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.omneAgate.DTO.LoginDto;
import com.omneAgate.DTO.LoginHistoryDto;
import com.omneAgate.DTO.LoginResponseDto;
import com.omneAgate.Util.FPSDBHelper;
import com.omneAgate.Util.NetworkUtil;
import com.omneAgate.Util.SessionId;
import com.omneAgate.Util.Util;
import com.omneAgate.service.ConnectionHeartBeat;
import com.omneAgate.service.LoginHistoryService;
import com.omneAgate.service.OfflineCloseSaleManager;
import com.omneAgate.service.OfflineInwardManager;
import com.omneAgate.service.OfflineTransactionManager;
import com.omneAgate.service.RemoteLoggingService;
import com.omneAgate.service.StatisticsService;
import com.omneAgate.service.UpdateDataService;

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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by user1 on 30/3/15.
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    String serverUrl = "";
    StringEntity stringEntity;
    Context context;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        int status = NetworkUtil.getConnectivityStatus(context);
        Log.e("offlinetoonline",""+status);
        if (status != 0) {

            if (GlobalAppState.localLogin) {

                this.context = context;
                try {
                    serverUrl = FPSDBHelper.getInstance(context).getMasterData("serverUrl");

                    LoginResponseDto loginResponseDto = FPSDBHelper.getInstance(context).getUserDetails(SessionId.getInstance().getUserId());
                    LoginDto loginCredentials = new LoginDto();
                    loginCredentials.setUserName(loginResponseDto.getUserDetailDto().getUserId());
                    loginCredentials.setPassword(Util.DecryptPassword(loginResponseDto.getUserDetailDto().getEncryptedPassword()));
                    loginCredentials.setDeviceId(Settings.Secure.getString(
                            context.getContentResolver(), Settings.Secure.ANDROID_ID).toUpperCase());
                    String login = new Gson().toJson(loginCredentials);
                    stringEntity = new StringEntity(login, HTTP.UTF_8);
                    new BackgroundSuccess().execute("");
                } catch (Exception e) {
                    Log.e("NetworkChangeReceiver", e.toString(), e);
                }
            }
        }

    }

    /*return http POST method using parameters*/
    private HttpResponse requestType(URI website, StringEntity entity) throws IOException {

        HttpParams httpParameters = new BasicHttpParams();
        int timeoutConnection = 50000;
        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
        int timeoutSocket = 50000;
        HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
        HttpClient client = new DefaultHttpClient(httpParameters);
        HttpPost postRequest = new HttpPost();
        postRequest.setURI(website);
        postRequest.setHeader("Content-Type", "application/json");
        postRequest.setHeader("Cookie", "JSESSIONID=" + SessionId.getInstance().getSessionId());
        postRequest.setEntity(entity);
        return client.execute(postRequest);

    }

    /**
     * Async   task for Download Sync for data in table
     */
    private class BackgroundSuccess extends AsyncTask<String, String, String> {


        @Override
        protected String doInBackground(String... f_url) {
            BufferedReader in = null;
            try {
                String url = serverUrl + "/login/bunk/authenticate";
                URI website = new URI(url);
                HttpResponse response = requestType(website, stringEntity);
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
                Log.e("NetworkChangeReceiver", e.toString(), e);
                try {
                    if (in != null)
                        in.close();
                } catch (Exception e1) {
                    // Intentional swallow of exception
                }
            }
            return null;
        }


        @Override
        protected void onPostExecute(String response) {
            if (response != null) {
                try {
                    Log.e("Response",""+ response);
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    Gson gson = gsonBuilder.create();
                    LoginResponseDto loginResponse = gson.fromJson(response, LoginResponseDto.class);

                   if(!loginResponse.getUserDetailDto().getProfile().equalsIgnoreCase("ADMIN")) {
                       Log.e("***networkchangeListner", "profile type"+ loginResponse.getUserDetailDto().getProfile());
                       SessionId.getInstance().setSessionId(loginResponse.getSessionid());
                       context.startService(new Intent(context, ConnectionHeartBeat.class));
                       context.startService(new Intent(context, UpdateDataService.class));
                       context.startService(new Intent(context, RemoteLoggingService.class));
                       context.startService(new Intent(context, OfflineTransactionManager.class));
                       context.startService(new Intent(context, OfflineInwardManager.class));
                       context.startService(new Intent(context, StatisticsService.class));
                       context.startService(new Intent(context, OfflineCloseSaleManager.class));
                    }
                } catch (Exception e) {
                    Log.e("NetworkChangeReceiver", e.toString(), e);
                }
            }
        }
    }
}