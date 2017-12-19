package com.omneAgate.Bunker.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.omneAgate.DTO.BillDto;
import com.omneAgate.Util.FPSDBHelper;
import com.omneAgate.Util.Util;
import com.omneAgate.Bunker.R;

import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class BillDialog extends Dialog implements View.OnClickListener {
    Context dialogContext;
    Button okButton;
    String ufc;
    Long beneficiaryId;

    public BillDialog(Context context, String ufc, Long beneficiaryId) {
        super(context);
        dialogContext = context;
        this.ufc = ufc;
        this.beneficiaryId = beneficiaryId;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.listforbillcust);
        setCancelable(false);
        okButton = (Button) findViewById(R.id.changeTable);
        TextView alertTitle = (TextView) findViewById(R.id.alerttitle);
        TextView textViewMonth = (TextView) findViewById(R.id.textViewMonth);
        TextView textViewUfc = (TextView) findViewById(R.id.textViewufc);
        alertTitle.setText(dialogContext.getString(R.string.purchaseHistory));
        textViewUfc.setText(dialogContext.getString(R.string.ufcData) + ufc);
        TextView textViewQty = (TextView) findViewById(R.id.textVieword);
        textViewQty.setText(dialogContext.getString(R.string.totalAmt));
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM", Locale.getDefault());
        textViewMonth.setText(dialogContext.getString(R.string.monthData) + dateFormat.format(date));
//
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        DateTime month = new DateTime();
        final List<BillDto> billList = FPSDBHelper.getInstance(dialogContext).getAllBillsUser(beneficiaryId);
        ListView list = (ListView) findViewById(R.id.listView1);
        list.setAdapter(new BillAdapter(dialogContext, billList));
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showBill(billList.get(position));
            }
        });
        okButton.setOnClickListener(this);
        try {
            textViewUfc.setText(dialogContext.getString(R.string.ufcData) + Util.DecryptedBeneficiary(dialogContext, ufc));
        } catch (Exception e) {
            Log.e("error", e.toString(), e);
        }

    }

    private void showBill(BillDto bill) {
        dismiss();
        new ViewBillDialog(dialogContext, beneficiaryId, bill).show();

    }

    @Override
    public void onClick(View v) {
        InputMethodManager imm = (InputMethodManager) dialogContext
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(
                getWindow().getDecorView().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
        switch (v.getId()) {
            case R.id.changeTable:
                dismiss();
                break;
            case R.id.textViewDetails:
                dismiss();
                break;
            default:
                break;
        }

    }

}
