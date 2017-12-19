package com.omneAgate.Bunker.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.omneAgate.Bunker.R;
import com.omneAgate.Util.AndroidDeviceProperties;
import com.omneAgate.Util.TamilUtil;
import com.omneAgate.Util.Util;
import com.omneAgate.Bunker.GlobalAppState;

import java.lang.reflect.Method;
import java.util.EnumMap;
import java.util.Hashtable;
import java.util.Map;


/**
 * This dialog will appear on the time of user logout
 */
public class DeviceIdDialog extends Dialog implements
        View.OnClickListener {

    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;
    private final Activity context;  //    Context from the user

    /*Constructor class for this dialog*/
    public DeviceIdDialog(Activity _context) {
        super(_context);
        context = _context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.dialog_deviceid);
        setCancelable(false);
        TextView message = (TextView) findViewById(R.id.textViewNwText);
        TextView textViewDeviceId = (TextView) findViewById(R.id.textViewDeviceId);
        TextView apkVersion = (TextView) findViewById(R.id.textViewDevice);
        TextView textViewVersion = (TextView) findViewById(R.id.textViewVersion);
        ImageView iv = (ImageView) findViewById(R.id.barcodeimage);
        setTamilText((TextView) findViewById(R.id.textViewNwTitle), R.string.device_details);
        AndroidDeviceProperties props = new AndroidDeviceProperties(context);
        setTamilText(message, R.string.deviceId);
        textViewDeviceId.setText(props.getDeviceProperties().getSerialNumber());
        apkVersion.setText("Apk Version");
        Util.LoggingQueue(context, "Device id dialog", props.getDeviceProperties().getSerialNumber());
        textViewVersion.setText(props.getDeviceProperties().getVersionName());
        Button okButton = (Button) findViewById(R.id.buttonNwOk);
        setTamilText(okButton, R.string.ok);
        okButton.setOnClickListener(this);
        Bitmap bitmap = null;
        Log.e("device_number", "" + props.getDeviceProperties().getSerialNumber());
        Log.e("devicenumber_serial",getSerialNumber());
        //Util.messageBar(context, getSerialNumber());

        try {
            bitmap = encodeAsBitmap(props.getDeviceProperties().getSerialNumber(), BarcodeFormat.CODE_128, 600, 100);
            iv.setImageBitmap(bitmap);

        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    public String getSerialNumber() {
        String serial = null;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            serial = (String) get.invoke(c, "ro.serialno");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return serial;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonNwOk:
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

    Bitmap encodeAsBitmap(String contents, BarcodeFormat format, int img_width, int img_height) throws WriterException {
        String contentsToEncode = contents;
        if (contentsToEncode == null) {
            return null;
        }
        Map<EncodeHintType, Object> hints = null;
        String encoding = guessAppropriateEncoding(contentsToEncode);
        if (encoding != null) {
            hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, encoding);
        }
        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix result;
        try {
            result = writer.encode(contentsToEncode, format, img_width, img_height, (Hashtable) hints);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    private static String guessAppropriateEncoding(CharSequence contents) {
        // Very crude at the moment
        for (int i = 0; i < contents.length(); i++) {
            if (contents.charAt(i) > 0xFF) {
                return "UTF-8";
            }
        }
        return null;
    }
}