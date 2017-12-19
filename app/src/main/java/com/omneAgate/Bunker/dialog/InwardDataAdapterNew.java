package com.omneAgate.Bunker.dialog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.omneAgate.DTO.GodownStockOutwardDto;
import com.omneAgate.Util.TamilUtil;
import com.omneAgate.Bunker.FpsStockInwardDetailActivity;
import com.omneAgate.Bunker.GlobalAppState;
import com.omneAgate.Bunker.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class InwardDataAdapterNew extends BaseAdapter {

    Context ct;

    List<GodownStockOutwardDto> billDto;

    public InwardDataAdapterNew(Context context, List<GodownStockOutwardDto> billDto) {
        ct = context;
        this.billDto = billDto;

    }

    public int getCount() {
        return billDto.size();
    }

    public GodownStockOutwardDto getItem(int position) {
        return billDto.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        View view;
        GodownStockOutwardDto bills = billDto.get(position);
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) ct
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = mInflater.inflate(R.layout.adapter_fps_stock_inward, null);

            holder = new ViewHolder();
            holder.serialNo = (TextView) view.findViewById(R.id.tvSerialNo);
            holder.deliveryChellanId = (TextView) view.findViewById(R.id.tvDeliveryChellanId);
            holder.dispatchDate = (TextView) view.findViewById(R.id.tvOutwardDate);
            holder.godownName = (TextView) view.findViewById(R.id.tvGodownName);
            holder.lapsedTime = (TextView) view
                    .findViewById(R.id.tvLapsedTime);
            holder.status = (TextView) view.findViewById(R.id.btnStatus);
            holder.status.setTag(position);
            view.setMinimumHeight(50);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) convertView.getTag();
        }
        holder.serialNo.setText(String.valueOf(position + 1));
        setTamilText(holder.status, R.string.transit);
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.getDefault());
        String formattedOutwardDate = df.format(bills.getOutwardDate());
        holder.deliveryChellanId.setText(bills.getReferenceNo());
        holder.dispatchDate.setText(formattedOutwardDate);

        String lapsedString = lapsedTimeAndDay(bills.getOutwardDate());
        holder.lapsedTime.setText(lapsedString);
        holder.godownName.setText(bills.getGodownCode());
        return view;
    }

    private String lapsedTimeAndDay(long outwardDateAndTime) {

        long difference = System.currentTimeMillis() - outwardDateAndTime;
        if (difference < 0) {
            difference = 1;
        }
        long differenceInMins = difference / 60000;
        long minutes = differenceInMins % 60;
        differenceInMins /= 60;
        long hours = differenceInMins % 24;
        differenceInMins /= 24;
        long days = differenceInMins;
        String dateTimeData = "";
        if (days > 0) {
            dateTimeData = days + " " + ct.getString(R.string.day) + " ";
            if (hours > 0) {
                dateTimeData = dateTimeData + hours + " " + ct.getString(R.string.hr) + " ";
            } else {
                dateTimeData = dateTimeData + minutes + " " + ct.getString(R.string.min);
            }
        } else if (hours > 0) {
            dateTimeData = dateTimeData + hours + " " + ct.getString(R.string.hr) + " ";
            if (minutes > 0)
                dateTimeData = dateTimeData + minutes + " " + ct.getString(R.string.min);
        } else {
            dateTimeData = dateTimeData + minutes + " " + ct.getString(R.string.min);
        }
        return dateTimeData;
    }

    /**
     * Tamil text textView typeface
     * input  textView name and id for string.xml
     */
    public void setTamilText(TextView textName, int id) {
        if (GlobalAppState.language.equalsIgnoreCase("ta")) {
            Typeface tfBamini = Typeface.createFromAsset(ct.getAssets(), "fonts/Bamini.ttf");
            textName.setTypeface(tfBamini, Typeface.BOLD);
            textName.setText(TamilUtil.convertToTamil(TamilUtil.BAMINI, ct.getString(id)));
        } else {
            textName.setText(ct.getString(id));
        }
    }

    class ViewHolder {
        TextView serialNo;
        TextView deliveryChellanId;
        TextView dispatchDate;
        TextView godownName, lapsedTime;
        TextView status;
    }
}