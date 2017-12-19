package com.omneAgate.Bunker.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.omneAgate.Util.TamilUtil;
import com.omneAgate.Bunker.GlobalAppState;
import com.omneAgate.Bunker.R;
import com.omneAgate.Bunker.TransactionCommodityActivity;

import org.apache.commons.lang3.StringUtils;

/**
 * This dialog will appear on the time of user logout
 */
public class CloseSalePasswordDialog extends Dialog implements
        View.OnClickListener {


    private final Activity context;  //    Context from the user

    /*Constructor class for this dialog*/
    public CloseSalePasswordDialog(Activity _context) {
        super(_context);
        context = _context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.dialog_close_sale_pass);
        setCancelable(false);
        setTamilText((TextView) findViewById(R.id.textViewNwTitle), R.string.password);
        Button okButton = (Button) findViewById(R.id.buttonNwOk);
        setTamilText(okButton, R.string.ok);
        okButton.setOnClickListener(this);
        Button cancelButton = (Button) findViewById(R.id.buttonNwCancel);
        setTamilText(cancelButton, R.string.cancel);
        cancelButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonNwOk:
                if (checkPassword()) {
                    ((TransactionCommodityActivity)context).passwordChecking(((EditText) findViewById(R.id.editTextUrl)).getText().toString().trim());
                    dismiss();
                }
                break;
            case R.id.buttonNwCancel:
                InputMethodManager imm = (InputMethodManager) context.getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                dismiss();
                break;
        }
    }

    /**
     * Tamil text textView typeface
     * input  textView name and id for string.xml
     */
    public void setTamilText(TextView textName, int id) {
        if (GlobalAppState.language.equals("ta")) {
            Typeface tfBamini = Typeface.createFromAsset(context.getAssets(), "fonts/Bamini.ttf");
            textName.setTypeface(tfBamini);
            textName.setText(TamilUtil.convertToTamil(TamilUtil.BAMINI, context.getString(id)));
        } else {
            textName.setText(context.getString(id));
        }
    }

    /**
     * Store changed ip in shared preference
     * returns true if value present else false
     */
    private boolean checkPassword() {
        EditText urlText = (EditText) findViewById(R.id.editTextUrl);
        String url = urlText.getText().toString().trim();
        if (StringUtils.isEmpty(url) ) {
            return false;
        }
        InputMethodManager imm = (InputMethodManager) context.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(urlText.getWindowToken(), 0);
        return true;
    }
}