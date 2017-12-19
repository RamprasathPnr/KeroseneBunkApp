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
import com.omneAgate.Bunker.LoginActivity;
import com.omneAgate.Bunker.R;

/**
 * This dialog will appear on the time of user logout
 */
public class GeoFencingDialog extends Dialog implements
        View.OnClickListener {
    private final Activity context;  //    Context from the user

    /*Constructor class for this dialog*/
    public GeoFencingDialog(Activity _context) {
        super(_context);
        context = _context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.dialog_geofence);
        setCancelable(false);
        setTamilText(((TextView) findViewById(R.id.tvWaring)), R.string.caution);
        setTamilText(((TextView) findViewById(R.id.tvloginBack)), R.string.geofence_reason);
        setTamilText(((TextView) findViewById(R.id.tvNextDay)), R.string.geofence_done);
        setTamilText(((TextView) findViewById(R.id.tvContinue)), R.string.fencing_message);
        Button yesButton = (Button) findViewById(R.id.buttonYes);
        setTamilText(yesButton, R.string.ok);
        yesButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonYes:
                dismiss();
                moveToLogin();
                break;
        }
    }

    private void moveToLogin() {
        context.startActivity(new Intent(context, LoginActivity.class));
        context.finish();
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


}