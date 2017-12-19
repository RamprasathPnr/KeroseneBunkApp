package com.omneAgate.Bunker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListPopupWindow;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.omneAgate.ActivityBusinessClass.LoginCheck;
import com.omneAgate.Bunker.dialog.BluetoothDialog;
import com.omneAgate.Bunker.dialog.ChangeUrlDialog;
import com.omneAgate.Bunker.dialog.DateChangeDialog;
import com.omneAgate.Bunker.dialog.DeviceIdDialog;
import com.omneAgate.Bunker.dialog.LanguageSelectionDialog;
import com.omneAgate.Bunker.dialog.MenuAdapter;
import com.omneAgate.Bunker.dialog.PurgeBillBItemDialog;
import com.omneAgate.DTO.AppfeatureDto;
import com.omneAgate.DTO.ConfigurationResponseDto;
import com.omneAgate.DTO.DeviceRegistrationDto;
import com.omneAgate.DTO.DeviceRegistrationResponseDto;
import com.omneAgate.DTO.EnumDTO.DeviceStatus;
import com.omneAgate.DTO.EnumDTO.RequestType;
import com.omneAgate.DTO.EnumDTO.ServiceListenerType;
import com.omneAgate.DTO.GlobalConfigsDTO;
import com.omneAgate.DTO.GodownStockOutward;
import com.omneAgate.DTO.KeroseneDto;
import com.omneAgate.DTO.LoginDto;
import com.omneAgate.DTO.LoginHistoryDto;
import com.omneAgate.DTO.LoginResponseDto;
import com.omneAgate.DTO.MenuDataDto;
import com.omneAgate.DTO.UserDetailDto;
import com.omneAgate.DTO.VersionUpgradeDto;
import com.omneAgate.Util.AndroidDeviceProperties;
import com.omneAgate.Util.Constants.FPSDBConstants;
import com.omneAgate.Util.CustomProgressDialog;
import com.omneAgate.Util.FPSDBHelper;
import com.omneAgate.Util.LoginData;
import com.omneAgate.Util.NetworkConnection;
import com.omneAgate.Util.SessionId;
import com.omneAgate.Util.SyncPageUpdate;
import com.omneAgate.Util.Util;
import com.omneAgate.service.AlarmReceiver;
import com.omneAgate.service.ConnectionHeartBeat;
import com.omneAgate.service.HttpClientWrapper;
import com.omneAgate.service.LoginHistoryService;
import com.omneAgate.service.OfflineCloseSaleManager;
import com.omneAgate.service.OfflineInwardManager;
import com.omneAgate.service.OfflineRegistrationManager;
import com.omneAgate.service.OfflineTransactionManager;
import com.omneAgate.service.RemoteLoggingService;
import com.omneAgate.service.StatisticsService;
import com.omneAgate.service.UpdateDataService;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Login activity  is used to login and also used to validate user
 */
public class LoginActivity extends BaseActivity implements AdapterView.OnItemClickListener {
    //Popup window for menu
    ListPopupWindow popupWindow;
    LoginResponseDto loginResponse;
    List<BluetoothDevice> mDeviceList;
    BluetoothAdapter mBluetoothAdapter;
    //User textBox for entering username and password
    private EditText userNameTextBox, passwordTextBox;
    AlarmManager alarmMgr;
    PendingIntent alarmIntent;
    ConfigurationResponseDto configurationResponse;
    String passowrd_Str;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(
                        BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                if (state == BluetoothAdapter.STATE_ON) {
                    mBluetoothAdapter.startDiscovery();
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                mDeviceList = new ArrayList<>();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
                    .equals(action)) {
                progressBar.dismiss();
                if (mDeviceList == null) {
                    mDeviceList = new ArrayList<>();
                }
                if (mDeviceList.size() > 0)
                    new BluetoothDialog(LoginActivity.this, mDeviceList).show();
                else {
                    Util.messageBar(LoginActivity.this, getString(R.string.no_records));
                }
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                if (mDeviceList == null) {
                    mDeviceList = new ArrayList<>();
                }
                mDeviceList.add((BluetoothDevice) intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // FPSDBHelper.getInstance(getApplicationContext());
        try {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            networkConnection = new NetworkConnection(this);
            userNameTextBox = (EditText) findViewById(R.id.login_username);
            passwordTextBox = (EditText) findViewById(R.id.login_password);
            String deviceid = new AndroidDeviceProperties(this).getDeviceProperties().getSerialNumber();
            if (deviceid.equals("8C8004C0CCD7525F")) {
                userNameTextBox.setText("rampnr");
                passwordTextBox.setText("alluser");
            }
            appState = (GlobalAppState) getApplication();
            if (FPSDBHelper.getInstance(this).isTableExists(null)) {
                FPSDBHelper.getInstance(this).isTableValue();
                List<GodownStockOutward> data = FPSDBHelper.getInstance(this).allFpsStockInwardDetail();
                FPSDBHelper.getInstance(this).insertTempFpsStockInwardDetails(data);
                FPSDBHelper.getInstance(this).dropTableExists();
            }
            Util.LoggingQueue(this, "Login Entry", "Inside Login Page");
        } catch (Exception e) {
            Log.e("LoginActivity", e.toString(), e);
        }
    }

    public void bluetoothRegister(BluetoothDevice device) {
        try {
            unregisterReceiver(mReceiver);
            SharedPreferences mySharedPreferences = getSharedPreferences("FPS_POS",
                    MODE_PRIVATE);
            String json = new Gson().toJson(device);
            SharedPreferences.Editor editor = mySharedPreferences.edit();
            editor.putString("bluetoothDevices", json);
            editor.apply();
        } catch (Exception e) {
            Log.e("Error", e.toString(), e);
        }
    }

    private void printerConfiguration() {
        progressBar = new CustomProgressDialog(this);
        progressBar.setCanceledOnTouchOutside(false);
        progressBar.show();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
        mBluetoothAdapter.startDiscovery();
    }

    /**
     * Menu creation
     * Used to change language
     * *
     */
    public void showPopupMenu(View v) {
        List<MenuDataDto> menuDto = new ArrayList<>();
        menuDto.add(new MenuDataDto("Language", R.drawable.icon_language, "மொழி"));
        menuDto.add(new MenuDataDto("Change URL", R.drawable.icon_server, "யுஆர்யல் மாற்று"));
        menuDto.add(new MenuDataDto("Device Details", R.drawable.icon_device_details, "சாதனத் தகவல்"));
        menuDto.add(new MenuDataDto("Printer", R.drawable.icon_printer, "அச்சுப் பொறி"));
        popupWindow = new ListPopupWindow(this);
        ListAdapter adapter = new MenuAdapter(this, menuDto); // The view ids to map the data to
        popupWindow.setAnchorView(v);
        popupWindow.setAdapter(adapter);
        popupWindow.setWidth(400); // note: don't use pixels, use a dimen resource
        popupWindow.setOnItemClickListener(this); // the callback for when a list item is selected
        popupWindow.show();
        Util.LoggingQueue(this, "Login Entry", "Inside Popup Window");
    }

    /**
     * Menu item click listener
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        popupWindow.dismiss();
        switch (position) {
            case 0:
                new LanguageSelectionDialog(this).show();
                break;
            case 1:
                new ChangeUrlDialog(this).show();
                break;
            case 2:
                new DeviceIdDialog(this).show();
                break;
            case 4:
                new PurgeBillBItemDialog(this).show();
                break;
            case 3:
                printerConfiguration();
                break;
        }
    }

    //onclick event for login button
    public void userLogin(View view) {
        try {
            /*Keyboard disappearance*/
            InputMethodManager im = (InputMethodManager) getSystemService(
                    INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(view.getWindowToken(), 0);
            LoginDto loginCredentials = getUsernamePassword();
            if (loginCredentials == null) {
                return;
            }
            authenticateUser(loginCredentials);
        } catch (Exception e) {
            Log.e("LoginActivity", e.toString(), e);
        }
    }

    /**
     * sending login details to server if network connection available
     *
     * @param loginCredentials for user
     */
    private void authenticateUser(LoginDto loginCredentials) {
        try {
            if (networkConnection.isNetworkAvailable()) {
                progressBar = new CustomProgressDialog(this);
                progressBar.setCanceledOnTouchOutside(false);
                progressBar.show();
                String url = "/login/bunk/authenticate";
                loginCredentials.setDeviceId(Settings.Secure.getString(
                        getContentResolver(), Settings.Secure.ANDROID_ID).toUpperCase());
                String login = new Gson().toJson(loginCredentials);
                StringEntity se = new StringEntity(login, HTTP.UTF_8);
                Log.e("logindetail", "" + login);
                httpConnection = new HttpClientWrapper();
                httpConnection.sendRequest(url, null, ServiceListenerType.LOGIN_USER,
                        SyncHandler, RequestType.POST, se, this);
            } else {
                progressBar = new CustomProgressDialog(this);
                progressBar.setCanceledOnTouchOutside(false);
                SharedPreferences mySharedPreferences = getSharedPreferences("FPS_POS", MODE_PRIVATE);
                if (mySharedPreferences.getBoolean("isshopactive", false)) {
                    Util.messageBar(this, getString(R.string.storeInactive));
                    return;
                }
                if (mySharedPreferences.getBoolean("sync_complete", false)) {
                    LoginCheck loginLocal = new LoginCheck(this, progressBar);
                    loginLocal.localLogin(loginCredentials);
                } else {
                    Util.messageBar(LoginActivity.this, getString(R.string.connectionRefused));
                }
            }
        } catch (Exception e) {
            dismissProgress();
            Util.LoggingQueue(this, "Login Request Error", e.toString());
            Log.e("LoginActivity", e.toString(), e);
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

    /**
     * sending registration details to server if network connection available
     * else no network connection error message will appear
     *
     * @param loginCredentials for user
     */
    private void registerDevice(LoginDto loginCredentials) {
        try {
            if (networkConnection.isNetworkAvailable()) {
                String url = "/device/registerbunkerapp";
                AndroidDeviceProperties deviceProperties = new AndroidDeviceProperties(this);
                DeviceRegistrationDto deviceRegister = new DeviceRegistrationDto();
                deviceRegister.setLoginDto(loginCredentials);
                deviceRegister.setDeviceDetailsDto(deviceProperties.getDeviceProperties());
                String login = new Gson().toJson(deviceRegister);
                Log.e("deviceRegister", login);
                StringEntity se = new StringEntity(login, HTTP.UTF_8);
                Util.LoggingQueue(this, "Device Register request", login);
                httpConnection.sendRequest(url, null, ServiceListenerType.DEVICE_REGISTER,
                        SyncHandler, RequestType.POST, se, this);
            } else {
                Util.messageBar(this, getString(R.string.connectionError));
            }
        } catch (Exception e) {
            Log.e("LoginActivity", e.toString(), e);
        }
    }

    //return login DTO if it is valid else null
    private LoginDto getUsernamePassword() {
        LoginDto loginCredentials = new LoginDto();
        String userName = userNameTextBox.getText().toString();
        String password = passwordTextBox.getText().toString();
        //Username field empty
        if (StringUtils.isEmpty(userName)) {
            Util.messageBar(this, getString(R.string.loginUserNameEmpty));
            return null;
        }
        //password field empty
        if (StringUtils.isEmpty(password)) {
            Util.messageBar(this, getString(R.string.loginPasswordEmpty));
            return null;
        }
        loginCredentials.setUserName(userName);
        loginCredentials.setPassword(password);
        passowrd_Str = password;
        return loginCredentials;
    }

    @Override
    protected void onStart() {
        super.onStart();
        setTamilText((Button) findViewById(R.id.login_loginButton), R.string.loginButton);
        GlobalAppState.localLogin = false;
        setTamilHeader((TextView) findViewById(R.id.login_actionbar), R.string.headerAllPageEnglish);
        setTamil(((TextView) findViewById(R.id.login_actionbarTamil)), R.string.headerAllPage);
        setTamilText(((TextView) findViewById(R.id.appName)), getString(R.string.fpsposapplication));
        removeAllServices();
//        int size = FPSDBHelper.getInstance(this).getBeneficiaryCount();
//        Toast.makeText(this, "size=" + size, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    /*Concrete method*/
    @Override
    public void processMessage(Bundle message, ServiceListenerType what) {
        switch (what) {
            case LOGIN_USER:
                userLoginResponse(message);
                break;
            case DEVICE_REGISTER:
                userRegisterResponse(message);
                break;
            case CHECKVERSION:
                checkData(message);
                break;
            case CONFIGURATION:
                getConfigurationResponse(message);
                break;
            default:
                dismissProgress();
                SharedPreferences prefs = getSharedPreferences("FPS", MODE_PRIVATE);
                if (!prefs.getBoolean("approved", false)) {
                    Util.messageBar(LoginActivity.this, getString(R.string.connectionRefused));
                } else {
                    SharedPreferences mySharedPreferences = getSharedPreferences("FPS_POS",
                            MODE_PRIVATE);
                    if (mySharedPreferences.getBoolean("sync_complete", false)) {
                        LoginCheck loginLocal = new LoginCheck(this, progressBar);
                        loginLocal.localLogin(getUsernamePassword());
                    } else {
                        Util.messageBar(LoginActivity.this, getString(R.string.connectionRefused));
                    }
                }
                break;
        }
    }

    private void checkData(Bundle message) {
        try {
            String response = message.getString(FPSDBConstants.RESPONSE_DATA);
            Util.LoggingQueue(this, "Login APK version response", response);
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            VersionUpgradeDto versionUpgradeDto = gson.fromJson(response, VersionUpgradeDto.class);
            if (versionUpgradeDto == null || versionUpgradeDto.getVersion() == 0 || StringUtils.isEmpty(versionUpgradeDto.getLocation())) {
                dismissProgress();
                Util.messageBar(this, getString(R.string.errorUpgrade));
            } else {
                if (versionUpgradeDto.getStatusCode() == 0) {
                    PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                    Log.e("***loginactivity", "server apk version" + versionUpgradeDto.getVersion());
                    if (versionUpgradeDto.getVersion() > pInfo.versionCode) {
                        dismissProgress();
                        Intent intent = new Intent(this, AutoUpgrationActivity.class);
                        intent.putExtra("downloadPath", versionUpgradeDto.getLocation());
                        intent.putExtra("newVersion", versionUpgradeDto.getVersion());
                        startActivity(intent);
                        finish();
                    } else {
                        authenticationSuccess();
                    }
                } else {
                    dismissProgress();
                    Util.messageBar(this, getString(R.string.errorUpgrade));
                }
            }
        } catch (Exception e) {
            dismissProgress();
            Util.LoggingQueue(this, "Login Request Error", e.toString());
            Util.messageBar(this, getString(R.string.inCorrectUnamePword));
            Log.e("LoginActivity", e.toString(), e);
        }
    }

    private void dismissProgress() {
        if (progressBar != null) {
            if (progressBar.isShowing()) {
                progressBar.dismiss();
            }
        }
    }

    /**
     * After login response received from server successfully in android
     *
     * @param message bundle that received
     */
    private void userLoginResponse(Bundle message) {
        try {
            String response = message.getString(FPSDBConstants.RESPONSE_DATA);
            Util.LoggingQueue(this, "Login Request response", response);
            Log.e("LoginActivity", "Login Request response:" + response);
            SharedPreferences pref = getApplicationContext().getSharedPreferences("FPS_POS", MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("isshopactive", false);
            editor.apply();
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            loginResponse = gson.fromJson(response, LoginResponseDto.class);
            if (loginResponse != null) {
                if (loginResponse.isAuthenticationStatus()) {
                    checkUserApk();
                } else {
                    long diff = new Date().getTime() - new Date(loginResponse.getServerTime()).getTime();
                    long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
                    if (seconds < 300 && seconds > -300) {
                        checkDeviceStatus();
                    } else {
                        new DateChangeDialog(this, loginResponse.getServerTime()).show();
                    }
                }
                //  getConfiguration();
            } else {
                dismissProgress();
                Util.messageBar(this, getString(R.string.serviceNotAvailable));
            }
        } catch (Exception e) {
            dismissProgress();
            Util.LoggingQueue(this, "Login Request Error", e.toString());
            Util.messageBar(this, getString(R.string.inCorrectUnamePword));
            Log.e("LoginActivity", e.toString(), e);
        }
    }

    private void checkDeviceStatus() {
        SharedPreferences prefs = getSharedPreferences("FPS", MODE_PRIVATE);
        SharedPreferences pref = getApplicationContext().getSharedPreferences("FPS_POS", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("isshopactive", false);
        editor.apply();
        if (loginResponse.getDeviceStatus() == DeviceStatus.ACTIVE && loginResponse.isAuthenticationStatus()) {
            authenticationSuccess();
        } else if (loginResponse.getDeviceStatus() == DeviceStatus.UNASSOCIATED) {
            if (!prefs.getBoolean("approved", false)) {
                checkForRegistration(loginResponse.getUserDetailDto());
            } else {
                dismissProgress();
                Util.messageBar(this, getString(R.string.unassociated));
                FPSDBHelper.getInstance(this).updateMaserData("status", "" + DeviceStatus.INACTIVE);
            }
        } else if (loginResponse.getDeviceStatus() == DeviceStatus.INACTIVE) {
            dismissProgress();
            if (loginResponse.getStatusCode() == 5095) {
                Util.messageBar(this, Util.messageSelection(FPSDBHelper.getInstance(this).retrieveLanguageTable(5095)));
            } else {
                FPSDBHelper.getInstance(this).updateMaserData("status", "" + DeviceStatus.INACTIVE);
                Util.messageBar(this, getString(R.string.deviceInvalid));
            }
        } else {
            dismissProgress();
            if (loginResponse.getStatusCode() == 32000 && !loginResponse.isAuthenticationStatus()) {
                //  SharedPreferences pref = getApplicationContext().getSharedPreferences("FPS_POS", MODE_PRIVATE);
                // SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean("isshopactive", true);
                editor.apply();
                Util.messageBar(this, getString(R.string.storeInactive));
            } else {
                Util.messageBar(this, getString(R.string.inCorrectUnamePword));
            }
        }
    }

    private void checkUserApk() {
        try {
            if (networkConnection.isNetworkAvailable()) {
                SessionId.getInstance().setSessionId(loginResponse.getSessionid());
                VersionUpgradeDto version = new VersionUpgradeDto();
                String url = "/versionUpgrade/view";
                String checkVersion = new Gson().toJson(version);
                StringEntity se = new StringEntity(checkVersion, HTTP.UTF_8);
                Util.LoggingQueue(this, "Device Register Version", "Checking version of apk in device");
                httpConnection.sendRequest(url, null, ServiceListenerType.CHECKVERSION,
                        SyncHandler, RequestType.POST, se, this);
            } else {
                dismissProgress();
                Util.messageBar(this, getString(R.string.connectionError));
            }
        } catch (Exception e) {
            dismissProgress();
            Util.messageBar(this, getString(R.string.inCorrectUnamePword));
        }
    }

    /**
     * After successful login of user
     * Userdetails will be stored in Singleton class
     */
    private void authenticationSuccess() {
        try {
          /*  if (loginResponse.getDeviceStatus() == null) {
                Util.messageBar(this, getString(R.string.deviceInvalid));
            } else if (loginResponse.getUserDetailDto() == null) {
                Util.messageBar(this, getString(R.string.userInactive));
            } else {*/
            Util.LoggingQueue(this, "Login response", loginResponse.toString());
            SharedPreferences prefs = getSharedPreferences("FPS", MODE_PRIVATE);
            SessionId.getInstance().setSessionId(loginResponse.getSessionid());
            SessionId.getInstance().setUserId(loginResponse.getUserDetailDto().getId());
            SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("isActive", loginResponse.getGlobalConfigs().get(0).isActive());
            editor.apply();
            String card_type_id = "";
            List<GlobalConfigsDTO> configlist = loginResponse.getGlobalConfigs();
            for (int i = 0; i < configlist.size(); i++) {
                if (configlist.get(i).getConfigName().equals("KEROSENEBUNK_ALLOWED_CARD_TYPE_IDS")) {
                    card_type_id = configlist.get(i).getConfigValue();
                }
            }
//            if (!card_type_id.isEmpty())
//                card_type_id = card_type_id.substring(0, card_type_id.length() - 1);
            FPSDBHelper.getInstance(this).delete_inactive_beneficary(card_type_id);
            FPSDBHelper.getInstance(this).insertMaserData("KEROSENEBUNK_ALLOWED_CARD_TYPE_IDS", card_type_id);
            String lastLoginTime = FPSDBHelper.getInstance(this).getLastLoginTime(loginResponse.getUserDetailDto().getId());
            if (StringUtils.isNotEmpty(lastLoginTime)) {
                SessionId.getInstance().setLastLoginTime(new Date(Long.parseLong(lastLoginTime)));
            } else {
                SessionId.getInstance().setLastLoginTime(new Date());
            }
            SessionId.getInstance().setLoginTime(new Date());
            if (loginResponse.getKeroseneBunkDto() != null) {
                GlobalAppState.isLoggingEnabled = loginResponse.getKeroseneBunkDto().isRemoteLogEnabled();
                SessionId.getInstance().setFpsId(loginResponse.getKeroseneBunkDto().getId());
                SessionId.getInstance().setFpsCode(loginResponse.getKeroseneBunkDto().getGeneratedCode());
            } else {
                loginResponse.setKeroseneBunkDto(new KeroseneDto());
            }
            SessionId.getInstance().setUserName(loginResponse.getUserDetailDto().getUserId());
            SessionId.getInstance().setPassword(passowrd_Str);
            LoginData.getInstance().setLoginData(loginResponse);
            loginResponse.getUserDetailDto().setPassword(loginResponse.getUserDetailDto().getPassword());
            FPSDBHelper.getInstance(this).insertLoginUserData(loginResponse, passwordTextBox.getText().toString().trim());
            FPSDBHelper.getInstance(this).setLastLoginTime(loginResponse.getUserDetailDto().getId());
            FPSDBHelper.getInstance(this).purgeBill();
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString("keyEncrypt", loginResponse.getKey());
            edit.putString("longitude", loginResponse.getKeroseneBunkDto().getLongitude());
            edit.putString("latitude", loginResponse.getKeroseneBunkDto().getLatitude());
            if (loginResponse.getKeroseneBunkDto().getGeofencing() != null)
                edit.putBoolean("fencing", loginResponse.getKeroseneBunkDto().getGeofencing());
            edit.apply();
            Util.LoggingQueue(this, "Bluetooth", "Bluetooth pairing");
            if (!prefs.getBoolean("approved", false)) {
                checkForRegistration(loginResponse.getUserDetailDto());
                Util.LoggingQueue(this, "Login response", "Not Approved");
            } else {
                checkTime();
            }
            //}
        } catch (Exception e) {
            Util.LoggingQueue(this, "Log in success error", e.toString());
            Log.e("LoginActivity", e.toString(), e);
        } finally {
            dismissProgress();
        }
    }

    /**
     * Used to check whether device is registered or not
     *
     * @param userDetails from server
     */
    private void checkForRegistration(UserDetailDto userDetails) {
        if (userDetails != null && StringUtils.equalsIgnoreCase(userDetails.getProfile(), "ADMIN")) {
            LoginDto loginCredentials = getUsernamePassword();
            if (loginCredentials == null) {
                return;
            }
            SharedPreferences prefs = getSharedPreferences("FPS", MODE_PRIVATE);
            if (!prefs.getBoolean("register", false)) {
                Util.LoggingQueue(this, "Device Register", "Device Registration process started");
                registerDevice(loginCredentials);
            } else {
                dismissProgress();
                Util.LoggingQueue(this, "Device Register", "Device Registration already done");
                startActivity(new Intent(this, RegistrationActivity.class));
                finish();
            }
        } else {
            dismissProgress();
            Util.LoggingQueue(this, "Device Register", "Insufficient Credentials");
            Util.messageBar(this, getString(R.string.inCorrectUserCredential));
        }
    }

    /**
     * After registration response received from server successfully in android
     *
     * @param message bundle that received
     */
    private void userRegisterResponse(Bundle message) {
        try {
            String response = message.getString(FPSDBConstants.RESPONSE_DATA);
            Util.LoggingQueue(this, "Device Register response", response);
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            DeviceRegistrationResponseDto deviceRegistrationResponse = gson.fromJson(response,
                    DeviceRegistrationResponseDto.class);
            dismissProgress();
            if (deviceRegistrationResponse.getStatusCode() == 0) {
                Util.storePreferenceRegister(this);
                startActivity(new Intent(this, RegistrationActivity.class));
                finish();
            } else if (deviceRegistrationResponse.getStatusCode() == 5055) {
                Util.LoggingQueue(this, "Device Register", "Device already registered");
                Util.storePreferenceApproved(this);
                checkTime();
            } else {
                Util.messageBar(this, getString(R.string.inCorrectUnamePword));
            }
        } catch (Exception e) {
            Util.LoggingQueue(this, "Registered error", e.toString());
            Log.e("LoginActivity", e.toString(), e);
        }
    }

    private void checkTime() {
        long diff = new Date().getTime() - new Date(loginResponse.getServerTime()).getTime();//as given
        Util.LoggingQueue(this, "Time check", "Check user time and server time");
        long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
        Util.LoggingQueue(this, "Time check", "Time difference:" + seconds);
        if (seconds < 300 && seconds > -300) {
            loginSuccess();
        } else {
            dismissProgress();
            new DateChangeDialog(this, loginResponse.getServerTime()).show();
        }
    }

    /**
     * After login success the user navigation to Sale activity page
     * And also start the connection heartBeat service
     */
    private void loginSuccess() {
        Log.e(LoginActivity.class.getCanonicalName(), "sync Execution 1 ");
        Util.storePreferenceApproved(this);
        Util.LoggingQueue(this, "Logged in", "User login success");
        SharedPreferences mySharedPreferences = getSharedPreferences("FPS_POS",
                MODE_PRIVATE);
        LoginData.getInstance().setLoginData(loginResponse);
        Log.e("Role Features", loginResponse.getRoleFeatures().toString());
        insertRoleFeatures(loginResponse.getRoleFeatures());
        SessionId.getInstance().setRrc_districtid(loginResponse.getKeroseneBunkDto().getTalukDto().getDistrictDto().getId());
        SessionId.getInstance().setRrc_talukid(loginResponse.getKeroseneBunkDto().getTalukDto().getId());
        SessionId.getInstance().setEntitlementClassification(loginResponse.getKeroseneBunkDto().getEntitlementClassification());
        Log.e(LoginActivity.class.getCanonicalName(), "sync Execution 2 sync status" + mySharedPreferences.getBoolean("sync_complete", false));
        Log.e(LoginActivity.class.getCanonicalName(), "sync Execution 3 lastModifiedDate" + FPSDBHelper.getInstance(this).getMasterData("syncTime"));
        if (mySharedPreferences.getBoolean("sync_complete", false)) {
            navigationToAdmin();
        } else {
            insertLoginHistoryDetails();
            dismissProgress();
            String lastModifiedDate = FPSDBHelper.getInstance(this).getMasterData("syncTime");
            if (StringUtils.isNotEmpty(lastModifiedDate)) {
                SyncPageUpdate syncPage = new SyncPageUpdate(this);
                syncPage.setSync();
                navigationToAdmin();
            } else {
                if (loginResponse.getUserDetailDto().getProfile().equalsIgnoreCase("ADMIN")) {
                    startActivity(new Intent(this, SyncPageActivity.class));
                    finish();
                } else {
                    Util.messageBar(this, getString(R.string.inCorrectUserCredential));
                }
            }
        }
    }

    private void navigationToAdmin() {
        if (loginResponse.getUserDetailDto().getProfile().equalsIgnoreCase("ADMIN")) {
            Util.LoggingQueue(this, "Logged in", "Admin login");
            insertLoginHistoryDetails();
            dismissProgress();
            startActivity(new Intent(this, AdminActivity.class));
            finish();
        } else {
            moveToSales();
        }
    }

    private void insertRoleFeatures(Set<AppfeatureDto> roleFeatures) {
        Log.e("***Login activity", "role feature activity" + roleFeatures.toString());
        FPSDBHelper.getInstance(this).updateRoles(loginResponse.getUserDetailDto().getId());
        FPSDBHelper.getInstance(this).insertRoles(loginResponse.getUserDetailDto().getId(), roleFeatures);
    }

    private void insertLoginHistoryDetails() {
        LoginHistoryDto loginHistoryDto = new LoginHistoryDto();
        if (loginResponse.getKeroseneBunkDto() != null)
            loginHistoryDto.setBunkId(loginResponse.getKeroseneBunkDto().getId());
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
        loginHistoryDto.setLoginTime(df.format(new Date()));
        loginHistoryDto.setLoginType("ONLINE_LOGIN");
        loginHistoryDto.setUserId(loginResponse.getUserDetailDto().getId());
        df = new SimpleDateFormat("ddMMyyHHmmss", Locale.getDefault());
        String transactionId = df.format(new Date());
        loginHistoryDto.setTransactionId(transactionId);
        SessionId.getInstance().setTransactionId(transactionId);
        FPSDBHelper.getInstance(this).insertLoginHistory(loginHistoryDto);
    }

    private void moveToSales() {
        try {
            if (!checkLocationDetails()) {
                return;
            }
            FPSDBHelper.getInstance(this).updateMaserData("status", "" + loginResponse.getDeviceStatus());
            if (loginResponse.getDeviceStatus() == DeviceStatus.INACTIVE || loginResponse.getDeviceStatus() == DeviceStatus.UNASSOCIATED) {
                FPSDBHelper.getInstance(this).updateMaserData("status", "" + DeviceStatus.INACTIVE);
                Util.messageBar(this, getString(R.string.deviceInvalid));
            } else if (loginResponse.getUserDetailDto().getActive() == "false") {
                FPSDBHelper.getInstance(this).updateMaserData("status", "false");
                Util.messageBar(this, getString(R.string.userInactive));
            } else if (!loginResponse.getKeroseneBunkDto().isStatus()) {
                FPSDBHelper.getInstance(this).updateMaserData("status", "true");
                Util.messageBar(this, getString(R.string.storeInactive));
            } else {
                insertLoginHistoryDetails();
                dismissProgress();
                Util.LoggingQueue(this, "Logged in", "User login.All services started");
                startService(new Intent(this, ConnectionHeartBeat.class));
                startService(new Intent(this, UpdateDataService.class));
                startService(new Intent(this, RemoteLoggingService.class));
                startService(new Intent(this, OfflineTransactionManager.class));
                startService(new Intent(this, OfflineInwardManager.class));
                startService(new Intent(this, StatisticsService.class));
                startService(new Intent(this, LoginHistoryService.class));
                startActivity(new Intent(this, SaleActivity.class));
                startService(new Intent(this, OfflineCloseSaleManager.class));
                finish();
            }
        } catch (Exception e) {
            Util.LoggingQueue(this, "Logged in error", e.toString());
            Log.e("LoginActivity", e.toString(), e);
        }
    }

    @Override
    public void onBackPressed() {
    }

    /**
     * Used to stop all services running currently
     */
    private void removeAllServices() {
        stopService(new Intent(this, ConnectionHeartBeat.class));
        stopService(new Intent(this, UpdateDataService.class));
        stopService(new Intent(this, RemoteLoggingService.class));
        stopService(new Intent(this, OfflineTransactionManager.class));
        stopService(new Intent(this, OfflineInwardManager.class));
        stopService(new Intent(this, StatisticsService.class));
        stopService(new Intent(this, OfflineRegistrationManager.class));
        stopService(new Intent(this, LoginHistoryService.class));
        stopService(new Intent(this, OfflineCloseSaleManager.class));
    }

    private void getConfiguration() {
        try {
            Log.e("LoginActivity", "getConfiguration.........");
            progressBar = new CustomProgressDialog(this);
            progressBar.setCanceledOnTouchOutside(false);
            if (networkConnection.isNetworkAvailable()) {
                String url = "/posglobalconfig/getAll";
                String configReqParam = "{\n" + "\"posAppType\":\"KEROSENE_BUNK\"\n" + "}";
                StringEntity se = new StringEntity(configReqParam, HTTP.UTF_8);
                progressBar.show();
                httpConnection.sendRequest(url, null, ServiceListenerType.CONFIGURATION,
                        SyncHandler, RequestType.POST, se, this);
            } else {
            }
        } catch (Exception e) {
            dismissProgress();
            Log.e("LoginActivity", e.toString(), e);
        }
    }

    private void getConfigurationResponse(Bundle message) {
        try {
            String response = message.getString(FPSDBConstants.RESPONSE_DATA);
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            configurationResponse = gson.fromJson(response, ConfigurationResponseDto.class);
            if (configurationResponse.getStatusCode() == 0) {
                HashMap<String, String> configMap = configurationResponse.getPosGlobalConfigMap();
                String purgeDays = configMap.get("purgeDays");
                FPSDBHelper.getInstance(this).updateMaserData("purgeBill", purgeDays);
                // Alarm manager to call purge for every 24 hours
                alarmMgr = (AlarmManager) LoginActivity.this.getSystemService(Context.ALARM_SERVICE);
                Intent intent = new Intent(LoginActivity.this, AlarmReceiver.class);
                alarmIntent = PendingIntent.getBroadcast(LoginActivity.this, 0, intent, 0);
                alarmMgr.cancel(alarmIntent); // cancel any existing alarms
                //   alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), 86400000, alarmIntent);
                alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 15000, 15000, alarmIntent);
            } else if (loginResponse.getStatusCode() == 12025) {
//                Util.messageBar(this, Util.messageSelection(FPSDBHelper.getInstance(this).retrieveLanguageTable(12025)));
            }
        } catch (Exception e) {
            dismissProgress();
            Log.e("LoginActivity", e.toString(), e);
        }
    }
}