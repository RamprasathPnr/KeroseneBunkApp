package com.omneAgate.Bunker.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.omneAgate.DTO.EntitlementDTO;
import com.omneAgate.DTO.FPSStockDto;
import com.omneAgate.Util.EntitlementResponse;
import com.omneAgate.Util.FPSDBHelper;
import com.omneAgate.Util.TamilUtil;
import com.omneAgate.Bunker.GlobalAppState;
import com.omneAgate.Bunker.R;
import com.omneAgate.Bunker.SalesEntryActivity;

import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;


public class SaleDialog extends Dialog implements View.OnClickListener {
    Activity dialogContext;

    int id;

    String number = "";

    EntitlementDTO entitled;

    NumberFormat formatter;

    List<String> numberValues;

    public SaleDialog(Activity context, int id) {
        super(context);
        dialogContext = context;
        this.id = id;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.dialog_sale_page);
        setCanceledOnTouchOutside(false);
        formatter = new DecimalFormat("#0.000");
        entitled = EntitlementResponse.getInstance().getQrcodeTransactionResponseDto().getEntitlementList().get(id);
        number = formatter.format(entitled.getBought());
        if (entitled.getBought() == 0.0) {
            number = "0";
        }
        numberValues = new ArrayList<>();
        setText();
        String productName = entitled.getProductName();
        if (GlobalAppState.language.equals("ta") && StringUtils.isNotEmpty(entitled.getLproductUnit())) {
            productName = entitled.getLproductName();
        }
        setTamilText((TextView) findViewById(R.id.sale_dialog_header), productName);
        findViewById(R.id.button_one).setOnClickListener(this);
        findViewById(R.id.button_two).setOnClickListener(this);
        findViewById(R.id.button_three).setOnClickListener(this);
        findViewById(R.id.button_four).setOnClickListener(this);
        findViewById(R.id.button_five).setOnClickListener(this);
        findViewById(R.id.button_six).setOnClickListener(this);
        findViewById(R.id.button_seven).setOnClickListener(this);
        findViewById(R.id.button_eight).setOnClickListener(this);
        findViewById(R.id.button_nine).setOnClickListener(this);
        findViewById(R.id.button_zero).setOnClickListener(this);
        findViewById(R.id.button_dot).setOnClickListener(this);
        findViewById(R.id.button_bkSp).setOnClickListener(this);
        findViewById(R.id.entitledOK).setOnClickListener(this);
        findViewById(R.id.fivekgs).setOnClickListener(this);
        findViewById(R.id.tenkgs).setOnClickListener(this);
        findViewById(R.id.fifteenkgs).setOnClickListener(this);
        findViewById(R.id.fullkgs).setOnClickListener(this);
        if (entitled.getCurrentQuantity() > 15.0) {
            numberValues.add("5.000");
            numberValues.add("10.000");
            numberValues.add("15.000");
        } else if (entitled.getCurrentQuantity() > 10) {
            numberValues.add("3.000");
            numberValues.add("6.000");
            numberValues.add("9.000");
        } else if (entitled.getCurrentQuantity() > 5) {
            numberValues.add("2.000");
            numberValues.add("4.000");
            numberValues.add("6.000");
        } else if (entitled.getCurrentQuantity() > 2) {
            numberValues.add("1.000");
            numberValues.add("2.000");
            numberValues.add("3.000");
        } else if (entitled.getCurrentQuantity() == 2) {
            numberValues.add("0.500");
            numberValues.add("1.000");
            numberValues.add("1.500");
        } else {
            numberValues.add("0.5000");
            numberValues.add("1.000");
            numberValues.add("1.000");
        }
        setButtonText();
    }

    private void setButtonText() {
        String productUnit = entitled.getProductUnit();
        if (GlobalAppState.language.equals("ta")) {
            if (StringUtils.isNotEmpty(entitled.getLproductUnit())) {
                productUnit = entitled.getLproductUnit();
            }

        }
        setTamilText((TextView) findViewById(R.id.fivekgs), numberValues.get(0) + " " + productUnit);
        setTamilText((TextView) findViewById(R.id.tenkgs), numberValues.get(1) + " " + productUnit);
        setTamilText((TextView) findViewById(R.id.fifteenkgs), numberValues.get(2) + " " + productUnit);
        setTamilText((TextView) findViewById(R.id.entitledOK), dialogContext.getString(R.string.ok));
        setTamilText((TextView) findViewById(R.id.fullkgs), dialogContext.getString(R.string.full));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_one:
                addNumber("1");
                break;
            case R.id.button_two:
                addNumber("2");
                break;
            case R.id.button_three:
                addNumber("3");
                break;
            case R.id.button_four:
                addNumber("4");
                break;
            case R.id.button_five:
                addNumber("5");
                break;
            case R.id.button_six:
                addNumber("6");
                break;
            case R.id.button_seven:
                addNumber("7");
                break;
            case R.id.button_eight:
                addNumber("8");
                break;
            case R.id.button_nine:
                addNumber("9");
                break;
            case R.id.tenkgs:
                checkAvailability(numberValues.get(1));
                break;
            case R.id.fivekgs:
                checkAvailability(numberValues.get(0));
                break;
            case R.id.fifteenkgs:
                checkAvailability(numberValues.get(2));
                break;
            case R.id.fullkgs:
                setFullKg();
                break;
            case R.id.button_zero:
                addNumber("0");
                break;
            case R.id.button_dot:
                if (number.contains(".")) {
                } else
                    addNumber(".");
                break;
            case R.id.entitledOK:
                setEntitlementData();
                break;
            case R.id.button_bkSp:
                backSpace();
                break;
            default:
                break;
        }

    }

    private void setEntitlementData() {
        if (!isFPSStockAvailable(Double.parseDouble(number), entitled.getProductId())) {
            Toast.makeText(dialogContext, dialogContext.getString(R.string.stock_not_available), Toast.LENGTH_LONG).show();
            return;
        }
        double total = Double.parseDouble(number) * entitled.getProductPrice();
        EntitlementResponse.getInstance().getQrcodeTransactionResponseDto().getEntitlementList().get(id).setBought(Double.parseDouble(number));
        EntitlementResponse.getInstance().getQrcodeTransactionResponseDto().getEntitlementList().get(id).setTotalPrice(total);
        dismiss();
        ((SalesEntryActivity) dialogContext).setEntitlementText(id, Double.parseDouble(number));
    }


    /**
     * FPS stock available or not
     * if quantity availed is greater than stock returns false else true
     *
     * @params availed quantity and productId
     */
    private boolean isFPSStockAvailable(double bought, long productId) {
        try {
            FPSStockDto fpsStockDto = FPSDBHelper.getInstance(dialogContext).getAllProductStockDetails(productId);
            return fpsStockDto.getQuantity() >= bought;
        } catch (Exception e) {
            return false;
        }
    }

    //set full entitlement
    private void setFullKg() {
        number = formatter.format(entitled.getCurrentQuantity());
        setText();
    }

    private void setText() {
        ((TextView) findViewById(R.id.sale_dialog_text)).setText(number);
    }

    private void addNumber(String text) {
        try {
            if (number.contains(".")) {
                String[] numberData = StringUtils.split(number, ".");
                if (numberData.length > 1)
                    if (numberData[1].length() == 3) {
                        return;
                    }
            }
            if (number.length() >= 7) {
                return;
            }
            if (number.equals("0") && !text.equals(".")) {
                number = text;
            } else if (text.equals(".")) {
                number = number + ".";
            } else {
                number = number + text;
            }
            double userReceived = Double.parseDouble(number);
            if (userReceived > entitled.getCurrentQuantity()) {
                Toast.makeText(dialogContext, dialogContext.getString(R.string.exceedsLimit), Toast.LENGTH_LONG).show();
                backSpace();
                return;
            }
            setText();
        } catch (Exception e) {
            Log.e("Error", e.toString(), e);
        }
    }

    private void backSpace() {
        if (number.length() > 1) {
            number = number.substring(0, number.length() - 1);
        } else {
            number = "0";
        }
        setText();
    }

    private void checkAvailability(String entitledText) {
        double userReceived = Double.parseDouble(entitledText);
        if (userReceived > entitled.getCurrentQuantity()) {
            Toast.makeText(dialogContext, dialogContext.getString(R.string.exceedsLimit), Toast.LENGTH_LONG).show();
            return;
        }
        number = entitledText;
        setText();
    }

    /**
     * Tamil text textView typeface
     * input  textView name and id for string.xml
     */
    public void setTamilText(TextView textName, String value) {
        if (GlobalAppState.language.equals("ta")) {
            Typeface tfBamini = Typeface.createFromAsset(dialogContext.getAssets(), "fonts/Bamini.ttf");
            textName.setTypeface(tfBamini);
            textName.setText(TamilUtil.convertToTamil(TamilUtil.BAMINI, value));
        } else {
            textName.setText(value);
        }
    }
}
