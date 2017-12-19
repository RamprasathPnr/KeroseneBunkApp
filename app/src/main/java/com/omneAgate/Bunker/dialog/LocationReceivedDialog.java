package com.omneAgate.Bunker.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.omneAgate.Bunker.AdminActivity;
import com.omneAgate.Bunker.GlobalAppState;
import com.omneAgate.Bunker.R;

/**
 * This dialog will appear on the time of user logout
 */
public class LocationReceivedDialog extends Dialog implements
        View.OnClickListener {
    private final Activity context;  //    Context from the user

    Location location;

    /*Constructor class for this dialog*/
    public LocationReceivedDialog(Activity _context, Location location) {
        super(_context);
        context = _context;
        this.location = location;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.dialog_location_received);
        setCancelable(false);
        Button yesButton = (Button) findViewById(R.id.buttonYes);
        Button noButton = (Button) findViewById(R.id.buttonNo);
        String locationDetailsLng, locationDetailsLat, continueString, bunker_location, warningTxt;

        if(GlobalAppState.language.equalsIgnoreCase("ta")) {
            locationDetailsLng = "தீர்க்கரேகை  : " + location.getLongitude();
            locationDetailsLat = "அட்சரேகை  : " + location.getLatitude();
            setTamilText(yesButton, "ஆம்");
            setTamilText(noButton, "இல்லை");
            continueString = "தொடர விரும்புகிறீர்களா?";
            bunker_location="இந்த அமைவிடம் மண்ணெண்ணெய் கடை இயந்திரச் செயலியின்  அமைவிடமாக பொறுத்தப்படும்";
            warningTxt = "அமைவிடம்";
        }
        else {
            locationDetailsLng = "Longitude : " + location.getLongitude();
            locationDetailsLat = "Latitude  : " + location.getLatitude();
            setTamilText(yesButton, "Yes");
            setTamilText(noButton, "No");
            continueString = "Do you want to continue ?";
            bunker_location = "This location will be set Kerosene Bunk's location";
            warningTxt = "Location";
        }
        setTamilText(((TextView) findViewById(R.id.tvNextDay)), locationDetailsLng);
        setTamilText(((TextView) findViewById(R.id.tvLat)), locationDetailsLat);
        setTamilText(((TextView) findViewById(R.id.tvContinue)), continueString);
        setTamilText(((TextView) findViewById(R.id.tvloginBack)), bunker_location);
        setTamilText(((TextView) findViewById(R.id.tvWaring)), warningTxt);
        yesButton.setOnClickListener(this);
        noButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonYes:
                dismiss();
                sendLocation();
                break;
            case R.id.buttonNo:
                dismiss();
                break;
        }
    }

    private void sendLocation() {
        ((AdminActivity) context).sendLocation(location);
    }

    /**
     * Tamil text textView typeface
     * input  textView name and id for string.xml
     */
    public void setTamilText(TextView textName, String id) {
        textName.setText(id);
    }


}