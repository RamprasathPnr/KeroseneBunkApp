package com.omneAgate.Bunker.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.omneAgate.Util.FPSDBHelper;
import com.omneAgate.Util.SessionId;
import com.omneAgate.Util.TamilUtil;
import com.omneAgate.Util.Util;
import com.omneAgate.Bunker.GlobalAppState;
import com.omneAgate.Bunker.R;
import com.omneAgate.Bunker.TransactionCommodityActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * This dialog will appear on the time of user logout
 */
public class CloseSaleDialog extends Dialog implements
        View.OnClickListener {
    private final Activity context;  //    Context from the user

    /*Constructor class for this dialog*/
    public CloseSaleDialog(Activity _context) {
        super(_context);
        context = _context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.dialog_closesale);
        setCancelable(false);
        setTamilText(((TextView) findViewById(R.id.tvWaring)), R.string.caution);
        String openTime = FPSDBHelper.getInstance(context).getOpeningTime(SessionId.getInstance().getUserId());
        setTamilText(((TextView) findViewById(R.id.tvloginBack)), context.getString(R.string.loginBack));
        setTamilText(((TextView) findViewById(R.id.tvNextDay)), context.getString(R.string.nextDay) + " " + openTime);
        setTamilText(((TextView) findViewById(R.id.tvContinue)), R.string.continues);
        Button yesButton = (Button) findViewById(R.id.buttonYes);
        setTamilText(yesButton, R.string.yes);
        yesButton.setOnClickListener(this);
        Util.LoggingQueue(context, "Close sale dialog", "Dialog starting up");
        Button noButton = (Button) findViewById(R.id.buttonNo);
        setTamilText(noButton, R.string.no);
        noButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonNo:
                dismiss();
                break;

            case R.id.buttonYes:
                submitCloseSale();
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
     * Tamil text textView typeface
     * input  textView name and id for string.xml
     */
    public void setTamilText(TextView textName, String text) {
        if (GlobalAppState.language.equals("ta")) {
            Typeface tfBamini = Typeface.createFromAsset(context.getAssets(), "fonts/Bamini.ttf");
            textName.setTypeface(tfBamini);
            textName.setText(TamilUtil.convertToTamil(TamilUtil.BAMINI, text));
        } else {
            textName.setText(text);
        }
    }


    private void submitCloseSale() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String date = df2.format(c.getTime());
        int count = FPSDBHelper.getInstance(context).totalBillsToday(date);
        Double sum_amount = FPSDBHelper.getInstance(context).totalAmountToday(date);
        Util.LoggingQueue(context, "Close sale dialog", "Sale closed:bill count" + count + ":tot Amt:" + sum_amount);
      //  FPSDBHelper.getInstance(context).insertCloseSale(sum_amount, count);
        ((TransactionCommodityActivity) context).getUserPassword();
     /*startActivity(new Intent(context, Lo.class));
     finish();*/
    }
}