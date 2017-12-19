package com.omneAgate.service;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.omneAgate.DTO.BaseDto;
import com.omneAgate.DTO.EnumDTO.RequestType;
import com.omneAgate.DTO.EnumDTO.ServiceListenerType;
import com.omneAgate.Util.Constants.FPSDBConstants;
import com.omneAgate.Util.FPSDBHelper;
import com.omneAgate.Util.SessionId;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;

public class HttpClientWrapper {


    /**
     * Used to send http request to FPS server and return the data back
     *
     * @param extra bundle,requestData string from user,method POST/GET,entity,what
     */
    public void sendRequest(final String requestData, final Bundle extra,
                            final ServiceListenerType what, final Handler messageHandler,
                            final RequestType method, final StringEntity entity, final Context context) {


        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                BufferedReader in = null;
                Message msg = Message.obtain();
                msg.obj = what;
                Log.e("serverUrl1", "serverUrl1:");
                try {
                    Log.e("serverUrl", "serverUrl:");
                    String serverUrl = FPSDBHelper.getInstance(context).getMasterData("serverUrl");
                    String url = serverUrl + requestData;
                    Log.e("serverUrl", "serverUrl:" + url);
                    URI website = new URI(url);
                    HttpResponse response = requestType(website, method, entity);
                    int statusCode = response.getStatusLine().getStatusCode();
                    String responseData;
                    if(statusCode == 400 ||statusCode == 401 ||statusCode == 403 ||statusCode == 500){
                        BaseDto base = new BaseDto();
                        base.setStatusCode(statusCode);
                        responseData = new Gson().toJson(base);
                    }else {
                        in = new BufferedReader(new InputStreamReader(response
                                .getEntity().getContent()));
                        StringBuffer sb = new StringBuffer("");
                        String l;
                        String nl = System.getProperty("line.separator");
                        Log.e("getProperty", nl);
                        while ((l = in.readLine()) != null) {
                            Log.e("readLine", l);
                            sb.append(l + nl);
                        }
                        in.close();
                        responseData = sb.toString();
                    }
                    Log.e("Response", responseData);

                    if (responseData.contains("timestamp") && responseData.contains("exception")) {
                        BaseDto base = new BaseDto();
                        base.setStatusCode(401);
                        responseData = new Gson().toJson(base);
                    }
                    Bundle b = new Bundle();
                    if (extra != null)
                        b.putAll(extra);
                    if (responseData.trim().length() != 0) {
                        b.putString(FPSDBConstants.RESPONSE_DATA, responseData);
                    } else {
                        msg.obj = ServiceListenerType.ERROR_MSG;
                        b.putString(FPSDBConstants.RESPONSE_DATA, "Empty Data");
                    }
                    msg.setData(b);
                } catch (SocketTimeoutException e) {
                    Log.e("SendRequest", "SocketTimeoutException", e);
                    msg.obj = ServiceListenerType.ERROR_MSG;
                    Bundle b = new Bundle();
                    BaseDto base = new BaseDto();
                    base.setStatusCode(500);
                    if (extra != null)
                        b.putAll(extra);
                    b.putString(FPSDBConstants.RESPONSE_DATA,
                            new Gson().toJson(base));
                    msg.setData(b);
                } catch (SocketException e) {
                    Log.e("SendRequest", "SocketException", e);
                    msg.obj = ServiceListenerType.ERROR_MSG;
                    Bundle b = new Bundle();
                    BaseDto base = new BaseDto();
                    base.setStatusCode(500);
                    if (extra != null)
                        b.putAll(extra);
                    b.putString(FPSDBConstants.RESPONSE_DATA,
                            new Gson().toJson(base));
                    msg.setData(b);
                } catch (IOException e) {
                    Log.e("SendRequest", "IOException", e);
                    msg.obj = ServiceListenerType.ERROR_MSG;
                    Bundle b = new Bundle();
                    if (extra != null)
                        b.putAll(extra);
                    b.putString(FPSDBConstants.RESPONSE_DATA, ""
                            + "Internal Error.Please Try Again");
                    msg.setData(b);
                } catch (Exception e) {
                    Log.e("SendRequest", "Exception", e);
                    msg.obj = ServiceListenerType.ERROR_MSG;
                    Bundle b = new Bundle();
                    BaseDto base = new BaseDto();
                    base.setStatusCode(500);
                    if (extra != null)
                        b.putAll(extra);
                    b.putString(FPSDBConstants.RESPONSE_DATA,
                            new Gson().toJson(base));
                    b.putString(FPSDBConstants.RESPONSE_DATA, new Gson().toJson(base));
                    msg.setData(b);
                } finally {
                    try {
                        if (in != null) {
                            in.close();
                        }
                    } catch (Exception e) {
                        Log.e("HTTP", "Error", e);
                    }
                    messageHandler.sendMessage(msg);
                }

            }
        }.start();

    }

    /**
     * return http GET,POST and PUT method using parameters
     *
     * @param uri,method and entity
     */
    private HttpResponse requestType(URI uri, RequestType method,
                                     StringEntity entity) {
        try {
            HttpParams httpParameters = new BasicHttpParams();
            int timeoutConnection = 15000;
            HttpConnectionParams.setConnectionTimeout(httpParameters,
                    timeoutConnection);
            int timeoutSocket = 15000;
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
            HttpClient client = new DefaultHttpClient(httpParameters);
            switch (method) {
                case POST:
                    HttpPost postRequest = new HttpPost();
                    postRequest.setURI(uri);
                    postRequest.setHeader("Content-Type", "application/json");
                    postRequest.setHeader("Store_type", "fps");
                    postRequest.setHeader("Cookie", "JSESSIONID=" + SessionId.getInstance().getSessionId());
                    postRequest.setHeader("Cookie", "SESSION=" + SessionId.getInstance().getSessionId());
                    postRequest.setEntity(entity);
                    return client.execute(postRequest);
                case PUT:
                    HttpPut putRequest = new HttpPut();
                    putRequest.setURI(uri);
                    putRequest.setHeader("Content-Type", "application/json");
                    putRequest.setHeader("Store_type", "fps");
                    putRequest.setHeader("Cookie", "JSESSIONID=" + SessionId.getInstance().getSessionId());
                    putRequest.setHeader("Cookie", "SESSION=" + SessionId.getInstance().getSessionId());
                    putRequest.setEntity(entity);
                    return client.execute(putRequest);
                case GET:
                    HttpGet getRequest = new HttpGet();
                    getRequest.setURI(uri);
                    getRequest.setHeader("Content-Type", "application/json");
                    getRequest.setHeader("Store_type", "fps");
                    Log.i("Cookie", "JSESSIONID=" + SessionId.getInstance().getSessionId());
                    getRequest.setHeader("Cookie", "JSESSIONID:" + SessionId.getInstance().getSessionId());
                    getRequest.setHeader("Cookie", "SESSION=" + SessionId.getInstance().getSessionId());
                    return client.execute(getRequest);
            }

        } catch (Exception e) {
            Log.e("Cookie excep", e.toString(), e);
        }
        return null;
    }

}