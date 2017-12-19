package com.omneAgate.Bunker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.omneAgate.DTO.EnumDTO.ServiceListenerType;
import com.omneAgate.DTO.FPSStockDto;
import com.omneAgate.DTO.FPSStockHistoryDto;
import com.omneAgate.DTO.FpsStockEntryDto;
import com.omneAgate.DTO.ProductDto;
import com.omneAgate.Util.CustomProgressDialog;
import com.omneAgate.Util.FPSDBHelper;
import com.omneAgate.Util.SessionId;
import com.omneAgate.Util.Util;

import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by user1 on 5/8/15.
 */
public class OpenStockActivity extends BaseActivity implements View.OnFocusChangeListener, View.OnClickListener {

    RelativeLayout keyBoardCustom;

    KeyboardView keyView;

    int keyBoardFocused;

    Map<Integer, ProductDto> productReceived;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activityopenstock);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        configureData();

    }


    @Override
    protected void processMessage(Bundle message, ServiceListenerType what) {

    }

    private void configureData() {
        try {
            setUpPopUpPageForAdmin();
            keyView = (KeyboardView) findViewById(R.id.customkeyboard);
            Util.LoggingQueue(this, "Stock Status activity", "Main page Called");
            setTamilText((TextView) findViewById(R.id.top_textView), R.string.openingstockheading);
            setTamilText((TextView) findViewById(R.id.commodity), R.string.commodity);
            setTamilText((TextView) findViewById(R.id.current_stock), R.string.current_stock);
            setTamilText((TextView) findViewById(R.id.opening_stock), R.string.opening_stock);
            setTamilText((TextView) findViewById(R.id.btnClose), R.string.submit);
            setTamilText((TextView) findViewById(R.id.cancel_button), R.string.close);
            keyBoardCustom = (RelativeLayout) findViewById(R.id.key_board_custom);
            new fpsStockListTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);

            findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();

                }
            });
            findViewById(R.id.btnClose).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getResult();

                }
            });

            findViewById(R.id.imageViewBack).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        } catch (Exception e) {
            Log.e("StockCheckActivity", e.toString(), e);
        }
    }


    private void checkVisibility() {
        if (keyBoardCustom.getVisibility() == View.GONE) {
            keyBoardCustom.setVisibility(View.VISIBLE);
        }
    }

    private void keyBoardAppear() {
        Keyboard keyboard = new Keyboard(this, R.layout.keyboardopenstock);
        keyView.setKeyboard(keyboard);
        keyView.setPreviewEnabled(false);
        keyView.bringToFront();
        keyView.setVisibility(KeyboardView.VISIBLE);
        keyView.setOnKeyboardActionListener(new KeyList());
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, AdminActivity.class));
        Util.LoggingQueue(this, "openStock activity", "Back button pressed");
        finish();
    }

    @Override
    public void onFocusChange(View view, boolean focusChanged) {
        if (focusChanged) {
            keyBoardFocused = view.getId();
            checkVisibility();
            keyBoardAppear();
        }
    }

    @Override
    public void onClick(View view) {
        keyBoardFocused = view.getId();
        checkVisibility();
        keyBoardAppear();
    }

    private void getResult() {
        List<FPSStockDto> fpsStockDto = new ArrayList<>();
        for (Integer data : productReceived.keySet()) {
            int position = data;
            String value = ((EditText) findViewById(position)).getText().toString().trim();
            if (StringUtils.isNotEmpty(value) && Double.parseDouble(value) > 0.0) {
                FPSStockDto addfpsstockDto = new FPSStockDto();
                addfpsstockDto.setProductId(productReceived.get(data).getId());
                addfpsstockDto.setFpsId(SessionId.getInstance().getFpsId());
                addfpsstockDto.setProductName(productReceived.get(data).getName());
                addfpsstockDto.setQuantity(Double.parseDouble(value));
                addfpsstockDto.setEmailAction(true);
                addfpsstockDto.setSmsMSAction(true);
                fpsStockDto.add(addfpsstockDto);
            }
        }
        if (fpsStockDto.size() > 0) {
            String androidDeviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                    Settings.Secure.ANDROID_ID).toUpperCase();
            FpsStockEntryDto fpsEntryDto = new FpsStockEntryDto();
            fpsEntryDto.setDeviceNumber(androidDeviceId);
            fpsEntryDto.setOpeningStockList(fpsStockDto);
            String fpsStcokList = new Gson().toJson(fpsEntryDto);
            Intent nextStockIntent;
            nextStockIntent = new Intent(getApplicationContext(), OpenstockActivityEntry.class);
            nextStockIntent.putExtra("Dtolist", fpsStcokList);
            startActivity(nextStockIntent);
            Util.LoggingQueue(OpenStockActivity.this, "openstockEntry activity", "Back button pressed");
            finish();

        } else {
            Util.messageBar(OpenStockActivity.this, getString(R.string.no_records));
        }
    }

    private void processData(List<ProductDto> productDtos) {
        try {
            if (productDtos.size() != 0) {
                productReceived = new HashMap<>();
                int position = 0;
                LinearLayout transactionLayout = (LinearLayout) findViewById(R.id.listView_linearLayout_stock_status);
                transactionLayout.removeAllViews();
                for (ProductDto products : productDtos) {
                    LayoutInflater lin = LayoutInflater.from(OpenStockActivity.this);
                    FPSStockHistoryDto stockHistory = FPSDBHelper.getInstance(OpenStockActivity.this).getAllProductOpeningStockDetails(products.getId());

                    FPSStockDto fpsStock = FPSDBHelper.getInstance(OpenStockActivity.this).getAllProductStockDetails(products.getId());

                    if (fpsStock != null && stockHistory != null && fpsStock.getProductId() != 0) {
                        transactionLayout.addView(returnViewTextView(lin, products, fpsStock.getQuantity(), stockHistory.getPrevQuantity()));
                    } else {
                        findViewById(R.id.btnClose).setVisibility(View.VISIBLE);
                        setTamilText((TextView) findViewById(R.id.cancel_button), R.string.cancel);
                        productReceived.put(position, products);
                        transactionLayout.addView(returnView(lin, products, 0.0, position));
                        Log.e("opening stock1", "" + products.getId().toString());
                    }
                    position++;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            Log.e("Exception while process",""+e.toString());
        }
    }

    private View returnView(LayoutInflater entitle, final ProductDto data, double sale, final int position) {
        View convertView = entitle.inflate(R.layout.adpter_openstock, null);
        TextView productNameTv = (TextView) convertView.findViewById(R.id.entitlementName);
        TextView unitTv = (TextView) convertView.findViewById(R.id.entitlementUnit);
        TextView openingStockTv = (TextView) convertView.findViewById(R.id.entitlement_opening);
        EditText addopenstockEt = (EditText) convertView.findViewById(R.id.amount_current);
        addopenstockEt.setShowSoftInputOnFocus(false);
        NumberFormat format = new DecimalFormat("#0.000");
        format.setMaximumFractionDigits(3);
        addopenstockEt.setId(position);
        addopenstockEt.setOnFocusChangeListener(this);
        addopenstockEt.setOnClickListener(this);
        productNameTv.setText(data.getName());
        if (GlobalAppState.language.equalsIgnoreCase("ta") && StringUtils.isNotEmpty(data.getLocalProductUnit())) {
            setTamilText(productNameTv, data.getLocalProductName());
        }

        unitTv.setText(data.getProductUnit());
        if (GlobalAppState.language.equalsIgnoreCase("ta") && StringUtils.isNotEmpty(data.getLocalProductUnit())) {
            setTamilText(unitTv, data.getLocalProductUnit());
        }
        openingStockTv.setText(format.format(sale));


        return convertView;
    }

    private View returnViewTextView(LayoutInflater entitle, final ProductDto data, double sale,double openStock) {
        View convertView = entitle.inflate(R.layout.adpter_openstock_ne, null);
        TextView productNameTv = (TextView) convertView.findViewById(R.id.entitlementName);
        TextView unitTv = (TextView) convertView.findViewById(R.id.entitlementUnit);
        TextView openingStockTv = (TextView) convertView.findViewById(R.id.entitlement_opening);
        TextView addOpenStockEt = (TextView) convertView.findViewById(R.id.amount_current);
        NumberFormat format = new DecimalFormat("#0.000");
        openingStockTv.setTextColor(Color.GRAY);
        addOpenStockEt.setText(String.valueOf(format.format(sale)));

        productNameTv.setText(data.getName());
        if (GlobalAppState.language.equalsIgnoreCase("ta") && StringUtils.isNotEmpty(data.getLocalProductUnit())) {
            setTamilText(productNameTv, data.getLocalProductName());
        }

        unitTv.setText(data.getProductUnit());
        if (GlobalAppState.language.equalsIgnoreCase("ta") && StringUtils.isNotEmpty(data.getLocalProductUnit())) {
            setTamilText(unitTv, data.getLocalProductUnit());
        }
        openingStockTv.setText(format.format(openStock));


        return convertView;
    }

    class KeyList implements KeyboardView.OnKeyboardActionListener {
        public void onKey(View v, int keyCode, KeyEvent event) {

        }

        public void onText(CharSequence text) {

        }

        public void swipeLeft() {

        }

        public void onKey(int primaryCode, int[] keyCodes) {

        }

        public void swipeUp() {

        }

        public void swipeDown() {

        }

        public void swipeRight() {

        }

        public void onPress(int primaryCode) {
            try {
                // Back Space key
                if (primaryCode == 8) {
                    String number = ((EditText) findViewById(keyBoardFocused)).getText().toString();
                    Log.e("number_count for dot", "" + number.length());
                    if (number.length() > 0) {

                        number = number.substring(0, number.length() - 1);
                        ((EditText) findViewById(keyBoardFocused)).setText(number);
                        ((EditText) findViewById(keyBoardFocused)).setSelection(number.length());
                    }

                } else if (primaryCode == 46) { //Done key
                    keyBoardCustom.setVisibility(View.GONE);
                } else if (primaryCode == 21) { //Dot Key
                    String value = ((EditText) findViewById(keyBoardFocused)).getText().toString().trim();
                    if (!StringUtils.contains(value, "."))
                        ((EditText) findViewById(keyBoardFocused)).append(".");
                } else { //Other Keys
                    char ch = (char) primaryCode;
                    String number = ((EditText) findViewById(keyBoardFocused)).getText().toString();
                    String testArray[] = StringUtils.split(number, ".");
                    if (testArray.length == 0 || testArray.length == 1) {
                        ((EditText) findViewById(keyBoardFocused)).append("" + ch);
                    } else if (testArray.length > 1 && testArray[1].length() <= 2)
                        ((EditText) findViewById(keyBoardFocused)).append("" + ch);


                }
            } catch (Exception e) {
                Log.e("onPress", e.toString(), e);
            }
        }

        public void onRelease(int primaryCode) {

        }
    }

    private class fpsStockListTask extends AsyncTask<String, Void, List<ProductDto>> {

        // can use UI thread here
        protected void onPreExecute() {
            try {
                progressBar = new CustomProgressDialog(OpenStockActivity.this);
                progressBar.show();
            } catch (Exception e) {
                Log.e("Error in Progress", e.toString(), e);
            }
        }

        // automatically done on worker thread (separate from UI thread)
        protected List<ProductDto> doInBackground(final String... args) {
            return FPSDBHelper.getInstance(OpenStockActivity.this).getAllProductDetails();
        }

        // can use UI thread here
        protected void onPostExecute(final List<ProductDto> result) {
            Log.e("productDtoList", "" + result.toString());
            if (progressBar != null) {
                progressBar.dismiss();
            }
            processData(result);
        }
    }


}