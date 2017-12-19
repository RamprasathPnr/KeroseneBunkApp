package com.omneAgate.activityKerosene;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.omneAgate.DTO.BeneficiaryDto;
import com.omneAgate.DTO.BillItemDto;
import com.omneAgate.DTO.EntitlementDTO;
import com.omneAgate.DTO.EnumDTO.ServiceListenerType;
import com.omneAgate.DTO.FPSStockDto;
import com.omneAgate.DTO.QRTransactionResponseDto;
import com.omneAgate.DTO.TransactionTypes;
import com.omneAgate.Util.EntitlementResponse;
import com.omneAgate.Util.FPSDBHelper;
import com.omneAgate.Util.NetworkConnection;
import com.omneAgate.Util.TransactionBase;
import com.omneAgate.Util.Util;
import com.omneAgate.activityKerosene.dialog.BillItemDialog;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

/**
 * FPS User view the entitled and he can enter the availed
 */
public class SalesEntryActivity extends BaseActivity implements View.OnClickListener {

    //Response from server set in this variable to load data
    private QRTransactionResponseDto entitlementResponseDTO;

    /*List of item entitled and price is in this variable*/
    private List<EntitlementDTO> entitleList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_entitlement);
        actionBarCreation();

        networkConnection = new NetworkConnection(this);
        appState = (GlobalAppState) getApplication();
        setTamilText((TextView) findViewById(R.id.textViewHead), R.string.headFamily);
        setTamilText((TextView) findViewById(R.id.textViewUFCLabel), R.string.ufcCode);
        setTamilText((TextView) findViewById(R.id.textViewMobile), R.string.rMN);
        setTamilText((TextView) findViewById(R.id.textViewAddress), R.string.address);
        String rate = getString(R.string.price) + "(" + getString(R.string.rs) + ")";
        setTamilText((TextView) findViewById(R.id.priceLabel), rate);
        setTamilText((TextView) findViewById(R.id.entitlementSubmit), R.string.submit);
        setTamilText((TextView) findViewById(R.id.entitlementCancel), R.string.cancel);
        setTamilText((TextView) findViewById(R.id.textViewDetails), R.string.customers);
        setTamilText((TextView) findViewById(R.id.entitlementAmt), R.string.availed);
        setTamilText((TextView) findViewById(R.id.entitlementQty), R.string.entitled);
        setTamilText((TextView) findViewById(R.id.entitlementName), R.string.product);
        entitlementResponseDTO = EntitlementResponse.getInstance().getQrcodeTransactionResponseDto();
    }

    /**
     * server Response is set
     */
    @Override
    protected void onStart() {
        super.onStart();
        configureData(entitlementResponseDTO);
    }

    /**
     * Data from server has been set inside this function
     *
     * @params response received from server or from local database
     */
    private void configureData(QRTransactionResponseDto entitlementResponseDTO) {
        try {
            String mobileNumber = entitlementResponseDTO.getMobileNumber();
            String beneficiaryName = entitlementResponseDTO.getBeneficiaryName();
            String ufc = entitlementResponseDTO.getUfc();
            if (StringUtils.isNotEmpty(beneficiaryName))
                ((TextView) findViewById(R.id.textViewBeneficiaryName)).setText(beneficiaryName);

            if (StringUtils.isNotEmpty(mobileNumber))
                ((TextView) findViewById(R.id.textViewBeneficiaryMobile)).setText(mobileNumber);

            if (StringUtils.isNotEmpty(ufc)) {
                ufc = Util.DecryptedBeneficiary(this, ufc);
                ((TextView) findViewById(R.id.textViewBeneficiaryUfc)).setText(ufc);
            }


            String address = getAddress(entitlementResponseDTO);

            ((TextView) findViewById(R.id.textViewBeneficiaryAddress)).setText(address);

            LinearLayout entitlementList = (LinearLayout) findViewById(R.id.listView_entitlement);
            entitleList = entitlementResponseDTO.getEntitlementList();

            entitlementList.removeAllViews();
            for (int position = 0; position < entitleList.size(); position++) {
                LayoutInflater lin = LayoutInflater.from(this);
                entitlementList.addView(returnView(lin, entitleList.get(position), position));
            }
            Button submit = (Button) findViewById(R.id.entitlementSubmit);
            submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    InputMethodManager inputManager = (InputMethodManager) SalesEntryActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    getAmount();
                }
            });
            Button cancel = (Button) findViewById(R.id.entitlementCancel);
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    buttonCancel();
                }
            });
        } catch (Exception e) {
            Util.LoggingQueue(this, "Error", e.toString());
            Log.e("Error,e.to", e.toString(), e);
        }
    }

    /**
     * returns the address of beneficiary
     *
     * @params response received from server or from local database
     */
    private String getAddress(QRTransactionResponseDto entitlementResponseDTO) {
        String address = "";

        if (StringUtils.isNotEmpty(entitlementResponseDTO.getAddressline1())) {
            address = entitlementResponseDTO.getAddressline1();
        }
        if (StringUtils.isNotEmpty(entitlementResponseDTO.getAddressline2())) {
            address = address + ", " + entitlementResponseDTO.getAddressline2();
        }

        if (StringUtils.isNotEmpty(entitlementResponseDTO.getAddressline3())) {
            address = address + ", " + entitlementResponseDTO.getAddressline3();
        }

        if (StringUtils.isNotEmpty(entitlementResponseDTO.getAddressline4())) {
            address = address + ", " + entitlementResponseDTO.getAddressline4();
        }

        if (StringUtils.isNotEmpty(entitlementResponseDTO.getAddressline5())) {
            address = address + ", " + entitlementResponseDTO.getAddressline5();
        }
        return address;
    }


    /**
     * used to cancel the transaction
     * navigated to sale start page
     */
    private void buttonCancel() {
        startActivity(new Intent(this, SaleActivity.class));
        finish();
    }

    //Orientation change
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Util.messageBar(this, GlobalAppState.language);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        buttonCancel();
    }

    /**
     * Get value from user
     * setting in the entitle list
     */
    private void getAmount() {
        try {
            for (int i = 0; i < entitleList.size(); i++) {
                EditText value = (EditText) findViewById(i);
                double userBought = 0.0;
                if (value.getText().toString().length() > 0) {
                    NumberFormat formatter = new DecimalFormat("#0.000");
                    userBought = Double.parseDouble(formatter.format(Double.parseDouble(value.getText().toString())));
                }
                EntitlementDTO entitleSingle = entitleList.get(i);
                entitleSingle.setBought(userBought);
                double total = userBought * entitleSingle.getProductPrice();
                entitleSingle.setTotalPrice(total);
                entitleList.set(i, entitleSingle);
            }
            showSummaryPage(entitleList);
        } catch (Exception e) {
            Util.messageBar(this, getString(R.string.exceedsLimit));
        }

    }

    /**
     * check validation for summary page navigation
     *
     * @param entitleList of entitled
     *                    check entitlement of products available for user
     *                    stock available in the store
     *                    <p/>
     *                    if any fails showing error message
     */

    private void showSummaryPage(List<EntitlementDTO> entitleList) {
        boolean valueEntered = false;
        for (EntitlementDTO entitlementResult : entitleList) {
            if (entitlementResult.getBought() > 0) {
                valueEntered = true;
                break;
            }
        }
        if (valueEntered) {
            int position = 0;
            for (EntitlementDTO entitlementResult : entitleList) {
                if (entitlementResult.getBought() > entitlementResult.getCurrentQuantity()) {
                    Util.messageBar(this, getString(R.string.exceedsLimit));
                    EditText exceed = (EditText) findViewById(position);
                    exceed.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
                    exceed.requestFocus(View.FOCUS_RIGHT);
                    return;
                } else if (!isFPSStockAvailable(entitlementResult.getBought(), entitlementResult.getProductId())) {
                    String productName = entitlementResult.getProductName();
                    if (GlobalAppState.language.equalsIgnoreCase("ta")) {
                        productName = entitlementResult.getLproductName();
                    }
                    Util.messageBar(this, productName + " " + getString(R.string.stockNotAvailableProduct));
                    EditText exceed = (EditText) findViewById(position);
                    exceed.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
                    exceed.requestFocus(View.FOCUS_RIGHT);
                    return;
                }
                position++;
            }
            Log.e("Error", "test failed");
            if (TransactionBase.getInstance().getTransactionBase().getTransactionType() == TransactionTypes.SALE_QR_OTP_DISABLED) {
                startActivity(new Intent(this, SalesSummaryWithOutOTPActivity.class));
                finish();
            } else {
                startActivity(new Intent(this, SalesSummaryActivity.class));
                finish();
            }
           /*  }*/
        } else {
            Util.messageBar(this, getString(R.string.noItemSelected));
        }
    }

    /**
     * FPS stock available or not
     * if quantity availed is greater than stock returns false else true
     *
     * @params availed quantity and productId
     */
    private boolean isFPSStockAvailable(double bought, long productId) {
        FPSStockDto fpsStockDto = FPSDBHelper.getInstance(this).getAllProductStockDetails(productId);
        if (fpsStockDto.getQuantity() >= bought) {
            return true;
        }
        return false;
    }

    /**
     * return User entitlement view
     *
     * @params entitle data, position of entitle
     */
    private View returnView(LayoutInflater entitle, EntitlementDTO data, int position) {
        View convertView = entitle.inflate(R.layout.adapter_entitlement, null);
        TextView entitlementName = (TextView) convertView.findViewById(R.id.entitlementName);
        TextView entitlementPrice = (TextView) convertView.findViewById(R.id.entitlementPrice);
        TextView entitlementEntitled = (TextView) convertView.findViewById(R.id.entitlementEntitled);
        final EditText amountOfSelection = (EditText) convertView.findViewById(R.id.amountOfSelection);
        NumberFormat numberFormat = new DecimalFormat("#0.00");
        String productName = data.getProductName();
        if (GlobalAppState.language.equals("ta") && StringUtils.isNotEmpty(data.getLproductName()))
            setTamilText(entitlementName, data.getLproductName() + "(" + data.getLproductUnit() + ")");
        else
            entitlementName.setText(productName + "(" + data.getProductUnit() + ")");
        entitlementPrice.setText(numberFormat.format(data.getProductPrice()));
        NumberFormat formatter = new DecimalFormat("#0.000");
        entitlementEntitled.setText(formatter.format(data.getCurrentQuantity()));
        if (data.getBought() > 0)
            amountOfSelection.setText(data.getBought() + "");
        else amountOfSelection.setText("");
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(amountOfSelection.getWindowToken(), 0);
        amountOfSelection.setId(position);
        entitlementEntitled.setId(position + 100);
        entitlementName.setId(position + 200);
        entitlementEntitled.setOnClickListener(this);
        entitlementName.setOnClickListener(this);
        amountOfSelection.addTextChangedListener(new GenericTextWatcher(amountOfSelection));
        amountOfSelection.setFilters(new InputFilter[]{new DigitsKeyListener(
                Boolean.FALSE, Boolean.TRUE) {
            int beforeDecimal = 3,afterDecimal = 3;

            @Override
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {

                String etText = amountOfSelection.getText().toString();
                String temp = amountOfSelection.getText() + source.toString();

                if (temp.equals(".")) {
                    return "0.";
                } else if (temp.toString().indexOf(".") == -1) {
                    // no decimal point placed yet
                    if (temp.length() > beforeDecimal) {
                        return "";
                    }
                } else {
                    int dotPosition;
                    int cursorPositon = amountOfSelection.getSelectionStart();
                    if (etText.indexOf(".") == -1) {
                        dotPosition = temp.indexOf(".");
                    } else {
                        dotPosition = etText.indexOf(".");
                    }
                    if (cursorPositon <= dotPosition) {
                        String beforeDot = etText.substring(0, dotPosition);
                        if (beforeDot.length() < beforeDecimal) {
                            return source;
                        } else {
                            if (source.toString().equalsIgnoreCase(".")) {
                                return source;
                            } else {
                                return "";
                            }

                        }
                    } else {
                        Log.i("cursor position", "in right");
                        temp = temp.substring(temp.indexOf(".") + 1);
                        if (temp.length() > afterDecimal) {
                            return "";
                        }
                    }
                }

                return super.filter(source, start, end, dest, dstart, dend);
            }
        }});

        return convertView;

    }

    @Override
    public void onClick(View v) {
        BeneficiaryDto beneficiary = FPSDBHelper.getInstance(this).beneficiaryDetailsByCode(entitlementResponseDTO.getUfc());
        DateTime month = new DateTime();
        int productSelected = v.getId() % 100;
        long productId = entitleList.get(productSelected).getProductId();
        String productName = entitleList.get(productSelected).getProductName();
        List<BillItemDto> billItemDto = FPSDBHelper.getInstance(this).billItemsDetails(beneficiary.getId(), month.getMonthOfYear(), productId);
        new BillItemDialog(this, billItemDto, productName, entitlementResponseDTO.getUfc(), beneficiary.getId()).show();
    }

    //Text watcher called when editText value changed
    private class GenericTextWatcher implements TextWatcher {

        private final View view;

        private GenericTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            String text = editable.toString();
            try {
                Double data = Double.parseDouble(text);
                if (entitlementResponseDTO.getEntitlementList().get(view.getId()).getCurrentQuantity() >= data) {
                    EditText edit = (EditText) findViewById(view.getId());
                    edit.getBackground().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
                } else {
                    EditText edit = (EditText) findViewById(view.getId());
                    edit.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
                    InputMethodManager inputManager = (InputMethodManager) SalesEntryActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    Util.messageBar(SalesEntryActivity.this, SalesEntryActivity.this.getString(R.string.exceedsLimit));
                }
            } catch (Exception e) {
                Util.LoggingQueue(SalesEntryActivity.this, "Error", e.toString());
            }


        }
    }


    //Concrete method
    @Override
    public void processMessage(Bundle message, ServiceListenerType what) {
    }
}
