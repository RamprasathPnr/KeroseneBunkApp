package com.omneAgate.Bunker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.GridLayout;
import android.widget.TextView;

import com.omneAgate.DTO.EntitlementDTO;
import com.omneAgate.DTO.EnumDTO.ServiceListenerType;
import com.omneAgate.DTO.FPSStockDto;
import com.omneAgate.DTO.GiveItUpRequestDetailDto;
import com.omneAgate.DTO.GiveItUpRequestDto;
import com.omneAgate.DTO.QRTransactionResponseDto;
import com.omneAgate.DTO.TransactionTypes;
import com.omneAgate.Util.EntitlementResponse;
import com.omneAgate.Util.FPSDBHelper;
import com.omneAgate.Util.NetworkConnection;
import com.omneAgate.Util.TransactionBase;
import com.omneAgate.Util.Util;
import com.omneAgate.Bunker.dialog.SaleDialog;
import com.omneAgate.Bunker.dialog.UserDetailsDialog;

import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FPS User view the entitled and he can enter the availed
 */
public class SalesEntryActivity extends BaseActivity {

    GridLayout productGrid;

    QRTransactionResponseDto entitlementResponseDTO;

    /*List of item entitled and price is in this variable*/
    private List<EntitlementDTO> entitleList;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_entitlement);
        networkConnection = new NetworkConnection(this);


        entitlementResponseDTO = EntitlementResponse.getInstance().getQrcodeTransactionResponseDto();

        if (entitlementResponseDTO != null) {
            giveItUpEntitlementCheck(entitlementResponseDTO);
            EntitlementResponse.getInstance().setQrcodeTransactionResponseDto(entitlementResponseDTO);
        }
        setUpInitialPage();
    }




    /**
     * Initial Setup
     */
    private void setUpInitialPage() {
        setUpPopUpPage();
        Util.LoggingQueue(this, "Sales Entry", "Inside Sales entry page");
        setTamilText((TextView) findViewById(R.id.top_textView), R.string.sale_entry_activity);
        setTamilText((TextView) findViewById(R.id.full_entitlements), R.string.full_entitlements);
        setTamilText((TextView) findViewById(R.id.commodity), R.string.commodity);
        setTamilText((TextView) findViewById(R.id.entitled_qty), R.string.entitled_qty);
        setTamilText((TextView) findViewById(R.id.purchased_qty), R.string.purchased_qty);
        setTamilText((TextView) findViewById(R.id.availed_qty), R.string.availed_qty);
        setTamilText((TextView) findViewById(R.id.qty_to_bill), R.string.qty_to_bill);
        setTamilText((TextView) findViewById(R.id.submitEntitlement), R.string.submit);
        setTamilText((TextView) findViewById(R.id.cancel_button), R.string.cancel);
        setTamilText((TextView) findViewById(R.id.card_user_details), R.string.card_user_details);
        entitleList = entitlementResponseDTO.getEntitlementList();
        int rows = entitleList.size();
        productGrid = (GridLayout) findViewById(R.id.gridLayout);
        productGrid.removeAllViews();
        productGrid.setRowCount(rows);
        productGrid.setColumnCount(6);
        productGrid.setUseDefaultMargins(false);
        productGrid.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
        productGrid.setRowOrderPreserved(false);
        setLayoutForProducts(rows);
        findViewById(R.id.submitEntitlement).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSummaryPage();
            }
        });

        findViewById(R.id.full_entitlement).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new UserDetailsDialog(SalesEntryActivity.this, entitlementResponseDTO.getUfc()).show();
            }
        });

        findViewById(R.id.info_sale_entry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFullEntitlement();
            }
        });
        findViewById(R.id.imageViewBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    //GridLayout for product with group
    private void setLayoutForProducts(int rowCount) {
        for (int position = 0, column = 0, rows = 0; position < rowCount * 6; position++, column++) {
            if (column == 6) {
                column = 0;
                rows++;
            }
            if (column == 2 || column == 3 || column == 4) {
                if (findPreviousEquals(rows)) {
                } else {
                    int value = valueForRows(rows);
                    productGrid.addView(getViewForColumns(rows, column, value));
                }
            } else if (column == 0) {
                productGrid.addView(getProductView(rows, column));
            } else if (column == 5) {
                productGrid.addView(getProductAmountView(rows, column));
            } else if (column == 1) {
                productGrid.addView(getUnitView(rows, column));
            }

        }
    }

    private View getUnitView(int rows, int column) {
        LayoutInflater lin = LayoutInflater.from(this);
        View convertView = lin.inflate(R.layout.layout_back_product_unit, null);
        TextView productUnit = (TextView) convertView.findViewById(R.id.productName);
        GridLayout.LayoutParams param = new GridLayout.LayoutParams();
        param.height = 60;
        param.width = 80;
        param.columnSpec = GridLayout.spec(column);
        param.rowSpec = GridLayout.spec(rows);
        convertView.setLayoutParams(param);
        if (GlobalAppState.language.equals("ta") && StringUtils.isNotEmpty(entitleList.get(rows).getLproductUnit())) {
            setTamilText(productUnit, entitleList.get(rows).getLproductUnit());
        } else
            productUnit.setText(entitleList.get(rows).getProductUnit());
        return convertView;
    }

    private View getProductView(int rows, int column) {
        GridLayout.LayoutParams param = new GridLayout.LayoutParams();
        param.height = 60;
        param.width = 280;
        entitleList = entitlementResponseDTO.getEntitlementList();

        LayoutInflater lin = LayoutInflater.from(this);
        View convertView = lin.inflate(R.layout.layout_back_product_name, null);
        TextView productName = (TextView) convertView.findViewById(R.id.productName);
        param.columnSpec = GridLayout.spec(column);
        param.rowSpec = GridLayout.spec(rows);
        convertView.setLayoutParams(param);
        if (GlobalAppState.language.equals("ta") && StringUtils.isNotEmpty(entitleList.get(rows).getLproductName())) {
            setTamilText(productName, entitleList.get(rows).getLproductName());
        } else
            productName.setText(entitleList.get(rows).getProductName());
        return convertView;
    }

    private View getProductAmountView(int rows, int column) {
        LayoutInflater lin = LayoutInflater.from(this);
        View convertView = lin.inflate(R.layout.layout_back_product_amount, null);
        TextView productUnit = (TextView) convertView.findViewById(R.id.productName);
        GridLayout.LayoutParams param = new GridLayout.LayoutParams();
        param.height = 60;
        param.width = 137;
        param.columnSpec = GridLayout.spec(column);
        param.rowSpec = GridLayout.spec(rows);
        convertView.setLayoutParams(param);
        NumberFormat formatter = new DecimalFormat("#0.000");
        productUnit.setText(formatter.format(entitleList.get(rows).getBought()));
        productUnit.setId(rows);
        productUnit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    if(EntitlementResponse.getInstance().getQrcodeTransactionResponseDto().getEntitlementList().get(v.getId()).isGiveitup()){
                        Util.messageBar(SalesEntryActivity.this,getString(R.string.give_it_errormsg));
                        return;
                    }

                if (EntitlementResponse.getInstance().getQrcodeTransactionResponseDto().getEntitlementList().get(v.getId()).getCurrentQuantity() > 0.0)
                    new SaleDialog(SalesEntryActivity.this, v.getId()).show();
                else {
                    Util.messageBar(SalesEntryActivity.this, getString(R.string.entitlemnt_finished));
                }
            }
        });
        return convertView;
    }

    private View getViewForColumns(int rows, int column, int value) {
        LayoutInflater lin = LayoutInflater.from(this);
        View convertView = lin.inflate(R.layout.layout_back_product_unit, null);
        TextView productUnit = (TextView) convertView.findViewById(R.id.productName);
        GridLayout.LayoutParams param = new GridLayout.LayoutParams();
        param.height = 60 * value;//+ ((value - 1) * 2)
        param.width = 137;
        param.columnSpec = GridLayout.spec(column);
        param.rowSpec = GridLayout.spec(rows, value);
        convertView.setLayoutParams(param);
        NumberFormat formatter = new DecimalFormat("#0.000");
        if (column == 2) {
            productUnit.setText(formatter.format(entitleList.get(rows).getEntitledQuantity()));
        } else if (column == 3) {
            productUnit.setText(formatter.format(entitleList.get(rows).getPurchasedQuantity()));
        } else {
            productUnit.setText(formatter.format(entitleList.get(rows).getCurrentQuantity()));
        }
        return convertView;
    }


    //Is previous is
    private boolean findPreviousEquals(int position) {
        if (position > 0) {
            long productId = entitleList.get(position).getGroupId();
            if (productId == entitleList.get(position - 1).getGroupId()) {
                return true;
            }
        }
        return false;
    }

    //Span rows in gridlayout
    private int valueForRows(int position) {

        Log.e("position", "" + position);
        Log.e("entitleList",""+entitleList.toString());
        Long productId = entitleList.get(position).getGroupId();
        Log.e("position",""+productId);
        List<EntitlementDTO> entitleData = entitlementResponseDTO.getUserEntitlement().get(productId);
        return entitleData.size();
    }

    //Used to set full entitlemnt for the product
    private void setFullEntitlement() {
        Util.LoggingQueue(this, "Sales Entry", "Full entitlement clicked");
        for (int i = 0; i < entitleList.size(); i++) {
            EntitlementDTO currentEntitle = entitleList.get(i);
            if (!isGroupSelected(i) && isFPSStockAvailable(currentEntitle.getCurrentQuantity(), currentEntitle.getProductId())) {
                NumberFormat formatter = new DecimalFormat("#0.000");
                ((TextView) findViewById(i)).setText(formatter.format(entitleList.get(i).getCurrentQuantity()));
                double total = currentEntitle.getCurrentQuantity() * currentEntitle.getProductPrice();
                EntitlementResponse.getInstance().getQrcodeTransactionResponseDto().getEntitlementList().get(i).setBought(currentEntitle.getCurrentQuantity());
                EntitlementResponse.getInstance().getQrcodeTransactionResponseDto().getEntitlementList().get(i).setTotalPrice(total);
            }
        }
    }

    //Any product in this group already selected
    private boolean isGroupSelected(int position) {
        for (int i = 0; i < position; i++) {
            String textValue = ((TextView) findViewById(i)).getText().toString().trim();
            if (entitleList.get(i).getGroupId() == entitleList.get(position).getGroupId() && Double.parseDouble(textValue) > 0.0) {
                return true;
            }
        }
        return false;
    }

    /**
     * FPS stock available or not
     * if quantity availed is greater than stock returns false else true
     *
     * @param bought quantity and productId
     */
    private boolean isFPSStockAvailable(double bought, long productId) {
        try {
            FPSStockDto fpsStockDto = FPSDBHelper.getInstance(this).getAllProductStockDetails(productId);
            return fpsStockDto.getQuantity() >= bought;
        } catch (Exception e) {
            return false;
        }
    }

    public void setEntitlementText(int id, double entitlement) {
        NumberFormat formatter = new DecimalFormat("#0.000");
        ((TextView) findViewById(id)).setText(formatter.format(entitlement));

    }

    /**
     * check validation for summary page navigation
     * check entitlement of products available for user
     * stock available in the store
     * if any fails showing error message
     */

    private void showSummaryPage() {
        boolean valueEntered = false;
        entitleList = EntitlementResponse.getInstance().getQrcodeTransactionResponseDto().getEntitlementList();
        for (EntitlementDTO entitlementResult : entitleList) {
            if (entitlementResult.getBought() > 0) {
                valueEntered = true;
                break;
            }
        }
        Map<Long, List<EntitlementDTO>> userEntitle = new HashMap<>();
        for (EntitlementDTO entitle : entitleList) {
            if (userEntitle.containsKey(entitle.getGroupId())) {
                userEntitle.get(entitle.getGroupId()).add(entitle);
            } else {
                List<EntitlementDTO> entitles = new ArrayList<>();
                entitles.add(entitle);
                userEntitle.put(entitle.getGroupId(), entitles);
            }
        }
        if (!getValueForList(userEntitle)) {
            Util.messageBar(this, getString(R.string.exceedsLimit));
            return;
        }
        if (valueEntered) {
            Util.LoggingQueue(this, "Sales Entry", entitleList.toString());
            if (TransactionBase.getInstance().getTransactionBase().getTransactionType() == TransactionTypes.BUNK_SALE_QR_OTP_DISABLED) {
                startActivity(new Intent(this, SalesSummaryWithOutOTPActivity.class));
                finish();
            } else {
                startActivity(new Intent(this, SalesSummaryActivity.class));
                finish();
            }
        } else {
            Util.LoggingQueue(this, "Sales Entry", "No Items selected");
            Util.messageBar(this, getString(R.string.noItemSelected));
        }
    }

    private boolean getValueForList(Map<Long, List<EntitlementDTO>> userEntitle) {
        boolean userValue = true;
        for (Long keys : userEntitle.keySet()) {
            List<EntitlementDTO> entitles = userEntitle.get(keys);
            double bought = getListBySize(entitles);
            if (bought > entitles.get(0).getCurrentQuantity()) {
                userValue = false;
            }
        }
        return userValue;
    }

    private double getListBySize(List<EntitlementDTO> entitles) {
        double totalSize = 0.0;
        for (EntitlementDTO entitlementDTO : entitles) {
            totalSize = totalSize + entitlementDTO.getBought();
        }
        return totalSize;

    }


    /**
     * server Response is set
     */

    public void giveItUpEntitlementCheck(QRTransactionResponseDto dto) {

        List<GiveItUpRequestDto> giveItUpRequestList = FPSDBHelper.getInstance(this).check_has_giveitup(dto.getBenficiaryId());


        if (giveItUpRequestList != null && giveItUpRequestList.size() > 0) {
            for (GiveItUpRequestDto rdto : giveItUpRequestList) {
                List<GiveItUpRequestDetailDto> giveituplist = rdto.getGiveItUpRequestDetailDtoList();
                for (GiveItUpRequestDetailDto gdto : giveituplist) {

                    List<EntitlementDTO> ent_list = dto.getEntitlementList();
                    for (EntitlementDTO edto : ent_list) {
                        if (gdto.getGroupDto().getId() == edto.getGroupId()) {
                            edto.setEntitledQuantity(0);
                            edto.setCurrentQuantity(0);
                            edto.setGiveitup(true);
                        }
                    }
                }
            }
        }

    }

    @Override
    public void onBackPressed() {
        Util.LoggingQueue(this, "Sales Entry", "Back pressed called");
        startActivity(new Intent(this, SaleOrderActivity.class));
        finish();
    }


    //Concrete method
    @Override
    public void processMessage(Bundle message, ServiceListenerType what) {
    }


}
