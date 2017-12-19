package com.omneAgate.Bunker.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.omneAgate.Util.TamilUtil;
import com.omneAgate.Bunker.GlobalAppState;
import com.omneAgate.Bunker.R;
import com.omneAgate.Bunker.SaleActivity;


//Stock Manual Inward Dialog
public class StockManualInwardDialog extends Dialog implements
        View.OnClickListener {

    //Activity context
    private final Activity context;  //    Context from the user

    /*Constructor class for this dialog*/
    public StockManualInwardDialog(Activity _context) {
        super(_context);
        context = _context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.dialog_stock_inward_response);
        setCancelable(false);
        TextView message = (TextView) findViewById(R.id.textViewMessage);
        setTamilText((TextView) findViewById(R.id.tvResponseTitle), R.string.stockInwardResponseTitle);
        setTamilText(message, R.string.messageStockManualInwardupdate);
        Button okButton = (Button) findViewById(R.id.buttonNwOk);
        setTamilText(okButton, R.string.ok);
        okButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.buttonNwOk:
                dismiss();
                context.startActivity(new Intent(context, SaleActivity.class));
                context.finish();
                break;

            default:
                dismiss();
                break;
        }
    }


    //Tamil text textView typeface
    private void setTamilText(TextView textName, int id) {
        if (GlobalAppState.language.equals("ta")) {
            Typeface tfBamini = Typeface.createFromAsset(context.getAssets(), "fonts/Bamini.ttf");
            textName.setTypeface(tfBamini);
            textName.setText(TamilUtil.convertToTamil(TamilUtil.BAMINI, context.getString(id)));
        } else {
            textName.setText(context.getString(id));
        }
    }


}
