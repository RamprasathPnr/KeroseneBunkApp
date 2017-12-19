package com.omneAgate.Bunker.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import com.omneAgate.Bunker.GlobalAppState;
import com.omneAgate.Bunker.R;
import com.omneAgate.Util.TamilUtil;

/**
 * Created by ramprasath on 20/5/16.
 */
public class StockAdjustAlertDialog extends Dialog implements View.OnClickListener {
    Context dialogContext;
    Button okButton;


    public StockAdjustAlertDialog(Context context) {
        super(context);
        dialogContext = context;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.dialog_stock_adjust_alert);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        setCancelable(false);


        okButton = (Button) findViewById(R.id.okButton);
        okButton.setOnClickListener(this);
        setTamilText((Button) findViewById(R.id.okButton), R.string.ok);
        setTamilText((TextView) findViewById(R.id.alertTitleText), R.string.stock_adjust_alert_msg);



    }

    public void setTamilText(TextView textName, int id) {
        if (GlobalAppState.language.equalsIgnoreCase("ta")) {
            Typeface tfBamini = Typeface.createFromAsset(dialogContext.getAssets(), "fonts/Bamini.ttf");
            textName.setTypeface(tfBamini, Typeface.BOLD);
            textName.setText(TamilUtil.convertToTamil(TamilUtil.BAMINI, dialogContext.getString(id)));
        } else {
            textName.setText(dialogContext.getString(id));
        }
    }

    @Override
    public void onClick(View v) {
        InputMethodManager imm = (InputMethodManager) dialogContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        switch (v.getId()) {
            case R.id.okButton:
                dismiss();
                break;
            default:
                break;
        }

    }

}
