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
import android.widget.RadioButton;
import android.widget.TextView;

import com.omneAgate.Util.TamilUtil;
import com.omneAgate.Util.Util;
import com.omneAgate.Bunker.GlobalAppState;
import com.omneAgate.Bunker.R;

/**
 * This dialog will appear on the time of user logout
 */
public class LanguageSelectionDialog extends Dialog implements
        View.OnClickListener {


    private final Activity context;  //    Context from the user

    private boolean tamil = false;

    /*Constructor class for this dialog*/
    public LanguageSelectionDialog(Activity _context) {
        super(_context);
        context = _context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.dialog_language);
        setCancelable(false);
        ((TextView) findViewById(R.id.textViewNwTitle)).setText(context.getString(R.string.languageSelection));
        setTamilText((TextView) findViewById(R.id.textViewNwTitleTamil), R.string.languageSelectionTa);
        setTamilText((TextView) findViewById(R.id.tamilText), R.string.tamil);
        if (GlobalAppState.language.equals("ta")) {
            tamil = true;
        }
        Util.LoggingQueue(context, "Dialog language", "Language Selection");
        setBackGround(tamil);
        Button okButton = (Button) findViewById(R.id.buttonNwOk);
        okButton.setOnClickListener(this);
        Button cancelButton = (Button) findViewById(R.id.buttonNwCancel);
        cancelButton.setOnClickListener(this);
        findViewById(R.id.language_english).setOnClickListener(this);
        findViewById(R.id.language_tamil).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonNwOk:
                changeLanguage();
                dismiss();
                break;
            case R.id.buttonNwCancel:
                dismiss();
                break;
            case R.id.language_english:
                tamil = false;
                setBackGround(tamil);
                break;
            case R.id.language_tamil:
                tamil = true;
                setBackGround(tamil);
                break;
        }
    }

    private void setBackGround(boolean isTamil) {
        if (isTamil) {
            Util.LoggingQueue(context, "Current language", "Tamil");
            findViewById(R.id.tamilSelection).setVisibility(View.VISIBLE);
            findViewById(R.id.englishSelection).setVisibility(View.GONE);
            findViewById(R.id.language_english).setBackgroundResource(R.drawable.background_grey);
            findViewById(R.id.language_tamil).setBackgroundResource(R.drawable.background_pink);
        } else {
            Util.LoggingQueue(context, "Current", "English");
            findViewById(R.id.tamilSelection).setVisibility(View.GONE);
            findViewById(R.id.englishSelection).setVisibility(View.VISIBLE);
            findViewById(R.id.language_english).setBackgroundResource(R.drawable.background_pink);
            findViewById(R.id.language_tamil).setBackgroundResource(R.drawable.background_grey);
        }
    }

    /**
     * Tamil text textView typeface
     * input  textView name and id for string.xml
     */
    public void setTamilText(RadioButton textName, int id) {
        Typeface tfBamini = Typeface.createFromAsset(context.getAssets(), "fonts/Bamini.ttf");
        textName.setTypeface(tfBamini);
        textName.setText(TamilUtil.convertToTamil(TamilUtil.BAMINI, context.getString(id)));
    }

    //Re-starts the application where language change take effects
    private void restartApplication() {
        Intent restart = context.getBaseContext().getPackageManager()
                .getLaunchIntentForPackage(context.getBaseContext().getPackageName());
        restart.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(restart);
        context.finish();
    }

    /**
     * Used to change language
     */
    private void changeLanguage() {
        if (tamil) {
            Util.LoggingQueue(context, "Selected", "Tamil");
            Util.changeLanguage(context, "ta");
        } else {
            Util.LoggingQueue(context, "Selected", "English");
            Util.changeLanguage(context, "en");
        }
        restartApplication();
    }

    /**
     * Tamil text textView typeface
     * input  textView name and id for string.xml
     */
    public void setTamilText(TextView textName, int id) {
        Typeface tfBamini = Typeface.createFromAsset(context.getAssets(), "fonts/Bamini.ttf");
        textName.setTypeface(tfBamini);
        textName.setText(TamilUtil.convertToTamil(TamilUtil.BAMINI, context.getString(id)));
    }
}