package com.omneAgate.printer;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.omneAgate.DTO.BillDto;
import com.omneAgate.DTO.UserDto.BillItemProductDto;
import com.omneAgate.Util.CustomProgressDialog;
import com.omneAgate.Util.SessionId;
import com.omneAgate.Bunker.BillSuccessActivity;
import com.omneAgate.Bunker.R;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * Created for BlueToothPrint
 */
public class BlueToothPrint {

    Set<BluetoothDevice> pairedDevices;

    BluetoothAdapter mBluetoothAdapter;

    BluetoothSocket mmSocket;

    BluetoothDevice mmDevice;

    CustomProgressDialog prog;

    OutputStream mmOutputStream;

    InputStream mmInputStream;

    BillDto updateStockRequestDto;

    Context context;

    String rationCardNumber;

    public BlueToothPrint(Context context, BillDto updateStockRequestDto, String cardNumber) {
        this.updateStockRequestDto = updateStockRequestDto;
        this.context = context;
        this.rationCardNumber = cardNumber;
    }

    private void findBT() {

        try {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            Log.e("bluetooth", "" + mBluetoothAdapter.toString());

            if (mBluetoothAdapter == null) {
                Log.e("bluetooth", "No bluetooth adapter available");
            }
            mBluetoothAdapter.startDiscovery();
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
            context.registerReceiver(mReceiver, filter);
            pairedDevices = mBluetoothAdapter.getBondedDevices();
            Log.e("BluetoothpairedDevices", "" + pairedDevices);
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    mmDevice = device;
                    break;
                }
            }

        } catch (Exception e) {
            Log.e("findBT Error", e.toString(), e);
        } finally {
            if (prog != null && prog.isShowing()) {
                prog.dismiss();
            }
            ((BillSuccessActivity) context).enableButton();
        }
    }

    public void printCall() {
        try {
            prog = new CustomProgressDialog(context);
            prog.setCancelable(false);
            prog.show();
            findBT();
            openBT();//socket connection
            BluetoothPrintData();
        } catch (Exception e) {
            Log.e("Print Call", e.toString(), e);
        } finally {
            if (prog != null && prog.isShowing()) {
                prog.dismiss();
            }
            ((BillSuccessActivity) context).enableButton();
        }
    }

    private void BluetoothPrintData() {
        StringBuilder textData = new StringBuilder();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
        Date convertedDate = new Date();
        Calendar myCalendar = Calendar.getInstance();
        int am_pm = myCalendar.get(Calendar.AM_PM);
        String amOrpm = ((am_pm == Calendar.AM) ? "am" : "pm");
        try {
            convertedDate = dateFormat.parse(updateStockRequestDto.getBillDate());
        } catch (ParseException e) {
            Log.e("Error", "Date Parse Error");
        }
        dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss", Locale.getDefault());
        String refNo = updateStockRequestDto.getTransactionId();
        String billDate = dateFormat.format(convertedDate);
        List<BillItemProductDto> billItems = new ArrayList<BillItemProductDto>(updateStockRequestDto.getBillItemDto());
        NumberFormat formatter = new DecimalFormat("00.00");
        NumberFormat formatSignle = new DecimalFormat("#0.00");
        textData.append("                " + context.getString(R.string.print_title) + "    ");
        textData.append("\n");
        textData.append("\n");
        textData.append("" + context.getString(R.string.print_billid) + "     : " + refNo + "\n");
        textData.append("" + context.getString(R.string.print_date) + "        : " + billDate + " " + amOrpm.toUpperCase() + "\n");
        textData.append("" + context.getString(R.string.print_shopcode) + "   : " + SessionId.getInstance().getFpsCode() + "\n");
        textData.append("" + context.getString(R.string.print_cardno) + "     : " + rationCardNumber + "\n");
        textData.append("-----------------------------------------\n");
        textData.append("#    " + context.getString(R.string.print_commodity) + "        " + context.getString(R.string.print_qty) + "      " + context.getString(R.string.print_price) + "\n");
        textData.append("-----------------------------------------\n");
        int i = 1;
        for (BillItemProductDto bItems : billItems) {
            String productName = bItems.getProductName() + "                                 ";
            String unit = bItems.getProductUnit();
            if (unit.equals("LTR")) {
                unit = "LT";
            }
            String quantity = "" + formatter.format(bItems.getQuantity()) + "(" + unit + ")";
            String price = "" + formatSignle.format(bItems.getCost() * bItems.getQuantity());
            String serialNo = i + "                        ";
            textData.append("" + StringUtils.substring(serialNo, 0, 2) + "  " + StringUtils.substring(productName, 0, 11) + "     " + quantity + "  " + pad(price, 7, " ") + "\n");
            i++;
        }
        textData.append("-----------------------------------------\n");
        String ledgerAmount = "   " + pad(formatSignle.format(updateStockRequestDto.getAmount()), 7, " ");
        textData.append("      " + context.getString(R.string.print_total) + "            " + ledgerAmount + "\n");
        textData.append("-----------------------------------------\n");
        textData.append("\n");
        textData.append("            " + context.getString(R.string.print_wishes) + " \n");
        textData.append("\n");
        textData.append("\n");
        textData.append("\n");
        IntentPrint(textData.toString());
    }

    // Tries to open a connection to the bluetooth printer device
    private void openBT() throws IOException {
        try {
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
            mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
            mBluetoothAdapter.cancelDiscovery();
            Log.e("state", "" + mmDevice.getBondState());
            mmSocket = mmDevice.createInsecureRfcommSocketToServiceRecord(uuid);
            if (mmDevice.getBondState() == 2) {
                mmSocket.connect();
                mmOutputStream = mmSocket.getOutputStream();
            }
            Log.e("socket", "" + mmSocket);

            mmSocket.connect();
            mmOutputStream = mmSocket.getOutputStream();

        } catch (Exception e) {
            Log.e("connection error", e.toString(), e);
        } finally {
            if (prog != null && prog.isShowing()) {
                prog.dismiss();
            }
            ((BillSuccessActivity) context).enableButton();
        }
    }

    private void IntentPrint(String txtValue) {
        try {
            Thread.sleep(2000);
            byte[] buffer = txtValue.getBytes();
            byte[] PrintHeader = {(byte) 0x55, 0, 0};
            PrintHeader[2] = (byte) buffer.length;
            if (PrintHeader.length > 128) {
                String value = "\nValue is more than 128 size\n";
            } else {
                for (int i = 4; i <= PrintHeader.length - 1; i++) {
                    if (mmOutputStream != null)
                        mmOutputStream.write(PrintHeader[i]);

                }
                for (int i = 4; i <= buffer.length - 1; i++) {
                    if (mmOutputStream != null)
                        mmOutputStream.write(buffer[i]);
                }
                if (mmOutputStream != null)
                    mmOutputStream.flush();
                printDialogStatus(context.getString(R.string.print_success));
                context.unregisterReceiver(mReceiver);
                if (mmOutputStream != null)
                    mmOutputStream.close();
                if (mmInputStream != null)
                    mmInputStream.close();
                mmSocket.close();

            }
        } catch (Exception e) {
            Log.e("errorPrint", e.toString(), e);
        } finally {
            if (prog != null && prog.isShowing()) {
                prog.dismiss();
            }
            ((BillSuccessActivity) context).enableButton();
        }
    }

    private String pad(String value, int length, String with) {
        StringBuilder result = new StringBuilder(length);
        // Pre-fill a String value
        result.append(fill(Math.max(0, length - value.length()), with));
        result.append(value);
        return result.toString();
    }

    private void printDialogStatus(String printerstatus) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View dialogview = inflater.inflate(R.layout.printerstatusdialog, null);
        final AlertDialog popupDia = builder.create();
        TextView status = (TextView) dialogview.findViewById(R.id.tvResponseTitle);
        popupDia.setView(dialogview);
        popupDia.setCanceledOnTouchOutside(true);
        popupDia.setCancelable(true);
        popupDia.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        status.setText(printerstatus);
        popupDia.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                popupDia.dismiss();
            }
        });
        WindowManager.LayoutParams wmlp = popupDia.getWindow().getAttributes();
        wmlp.gravity = Gravity.BOTTOM;
        popupDia.show();
        final Timer t = new Timer();
        t.schedule(new TimerTask() {
            public void run() {
                popupDia.dismiss(); // when the task active then close the dialog
                t.cancel(); // also just top the timer thread, otherwise, you may receive a crash report
            }
        }, 3000); // after 2 second (or 2000 miliseconds), the task will be active.


    }


    private String fill(int length, String with) {
        StringBuilder sb = new StringBuilder(length);
        while (sb.length() < length) {
            sb.append(with);
        }
        return sb.toString();
    }


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                // When discovery finds a device
                if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    String deviceName = device.getName();
                    String deviceAddress = device.getAddress();
                    Log.e("bluetoothActionA", "" + deviceName + "--->" + deviceAddress);

                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    Log.e("bluetoothActionB", "" + action);

                } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                    Log.e("bluetoothActionC", "" + action);
                }
            } catch (Exception e) {
                System.out.println("Broadcast Error : " + e.toString());
            } finally {
                if (prog != null && prog.isShowing()) {
                    prog.dismiss();
                }
                ((BillSuccessActivity) context).enableButton();
            }
        }
    };


}