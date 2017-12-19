package com.omneAgate.Bunker.dialog;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.omneAgate.DTO.BeneficiarySearchDto;
import com.omneAgate.Bunker.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created for BeneficiaryListAdapter
 */
public class BeneficiaryListAdapter extends BaseAdapter {

    List<BeneficiarySearchDto> beneficiaryActivation = new ArrayList<>();
    LayoutInflater inflater;
    Activity context;


    public BeneficiaryListAdapter(Activity context, List<BeneficiarySearchDto> myList) {
        this.beneficiaryActivation = myList;
        this.context = context;
        inflater = LayoutInflater.from(this.context);        // only context can also be used
    }

    @Override
    public int getCount() {
        return beneficiaryActivation.size();
    }

    @Override
    public BeneficiarySearchDto getItem(int position) {
        return beneficiaryActivation.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final RegistrationViewHolder mViewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.layout_bene_list_item, null);
            mViewHolder = new RegistrationViewHolder();
            mViewHolder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
            mViewHolder.tvDesc = (TextView) convertView.findViewById(R.id.tvDesc);
            mViewHolder.tvPosition = (TextView) convertView.findViewById(R.id.tvHash);
            mViewHolder.regDate = (TextView) convertView.findViewById(R.id.tvRegistration);
            mViewHolder.noOfAdults = (TextView) convertView.findViewById(R.id.noOfAdults);
            mViewHolder.noOfChild = (TextView) convertView.findViewById(R.id.noOfChild);
            mViewHolder.noOfCylinder = (TextView) convertView.findViewById(R.id.noOfCylinder);
            mViewHolder.mobile_avail = (TextView) convertView.findViewById(R.id.mobile_avail);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (RegistrationViewHolder) convertView.getTag();
        }
        BeneficiarySearchDto beneficiaryActivate = beneficiaryActivation.get(position);
        mViewHolder.tvTitle.setText(beneficiaryActivate.getMobileNo());
        mViewHolder.tvDesc.setText(beneficiaryActivate.getCardNo());
        mViewHolder.regDate.setText(beneficiaryActivate.getCardType());
        mViewHolder.noOfAdults.setText(String.valueOf(beneficiaryActivate.getNoOfAdult()));
        mViewHolder.noOfChild.setText(String.valueOf(beneficiaryActivate.getNoOfChild()));
        mViewHolder.noOfCylinder.setText(String.valueOf(beneficiaryActivate.getNoOfCylinder() ));
        mViewHolder.tvPosition.setText(String.valueOf((position + 1)));
        if(beneficiaryActivate.isMobileAvail()){
            mViewHolder.mobile_avail.setText("\u2714");
        }else{
            mViewHolder.mobile_avail.setText("X");
        }
        return convertView;
    }


    private class RegistrationViewHolder {
        TextView tvTitle, tvDesc, tvPosition, regDate, noOfAdults, noOfChild, noOfCylinder,mobile_avail;
    }
}