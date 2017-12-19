package com.omneAgate.Bunker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.omneAgate.DTO.BillDto;
import com.omneAgate.DTO.EnumDTO.ServiceListenerType;
import com.omneAgate.Util.FPSDBHelper;
import com.omneAgate.Util.PullToRefresh.LoadMoreListView;
import com.omneAgate.Util.Util;
import com.omneAgate.Bunker.dialog.BillISearchAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created for Bill By DateActivity
 */
public class BillByDateActivity extends BaseActivity {

    List<BillDto> bills;

    int loadMore = 0;

    String data;

    LoadMoreListView billByDate;

    ProgressBar progressBarSpin;

    BillISearchAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_date);
        data = getIntent().getExtras().getString("bills");
        progressBarSpin = (ProgressBar) findViewById(R.id.progressBar1);
        configureData();
    }


    /*Data from server has been set inside this function*/
    private void configureData() {
        try {
            setUpPopUpPage();
            Util.LoggingQueue(this, "Bill search List", "Setting up main page");
            long count = FPSDBHelper.getInstance(this).getAllUnsyncBills();
            setTamilText((TextView) findViewById(R.id.top_textView), R.string.bills);
            setTamilText((TextView) findViewById(R.id.unSyncBillCount), getString(R.string.unsyncBills) + "  " + count);
            setTamilText((TextView) findViewById(R.id.fpsInvardactionLabel), R.string.billDetailTxnBill);
            setTamilText((TextView) findViewById(R.id.fpsInvardoutwardDateLabel), R.string.ration_card_number1);
            setTamilText((TextView) findViewById(R.id.fpsInvardoutwardGodownNameLabel), R.string.billDetailProductPrice);
            setTamilText((TextView) findViewById(R.id.btnClose), R.string.close);
            setTamilText((TextView) findViewById(R.id.tvViewStockHistory), R.string.unsyncBills);
            setTamilText((TextView) findViewById(R.id.reg_date_search), data);

            bills = new ArrayList<>();
            if(count>0){
                findViewById(R.id.unsyncCount).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(BillByDateActivity.this,UnsyncBillActivity.class));
                        finish();
                    }
                });
            }
            billByDate = (LoadMoreListView) findViewById(R.id.listView_fps_stock_inward);
            adapter = new BillISearchAdapter(this, bills);
            billByDate.setAdapter(adapter);
            new SearchBillsByDateTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, data);
            billByDate.setOnLoadMoreListener(new LoadMoreListView.OnLoadMoreListener() {
                @Override
                public void onLoadMore() {
                    loadMore++;
                    new SearchBillsByDateTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, data);
                }
            });
            billByDate.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    submitBill(bills.get(position).getTransactionId());
                }
            });
        } catch (Exception e) {
            Util.LoggingQueue(this, "Error", e.toString());
        } finally {
            findViewById(R.id.imageViewBack).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }
    }

    private void submitBill(String transactionId) {
        new SearchBillTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, transactionId);
    }

    @Override
    protected void processMessage(Bundle message, ServiceListenerType what) {

    }


    public void onClose(View v) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, BillSearchActivity.class));
        Util.LoggingQueue(this, "Bill search List", "Back pressed Called");
        finish();
    }

    private class SearchBillsByDateTask extends AsyncTask<String, Void, List<BillDto>> {

        // can use UI thread here
        protected void onPreExecute() {
            progressBarSpin.setVisibility(View.VISIBLE);
        }

        // automatically done on worker thread (separate from UI thread)
        protected List<BillDto> doInBackground(final String... args) {
            return FPSDBHelper.getInstance(BillByDateActivity.this).getAllBillByDate(args[0],loadMore);
        }

        // can use UI thread here
        protected void onPostExecute(final List<BillDto> billDtos) {
            progressBarSpin.setVisibility(View.GONE);

            if (billDtos.size() > 0) {
                bills.addAll(billDtos);
                billByDate.setVisibility(View.VISIBLE);
                (findViewById(R.id.linearLayoutNoRecords)).setVisibility(View.GONE);
                adapter.notifyDataSetChanged();
                billByDate.invalidate();
                billByDate.onLoadMoreComplete();
            } else if (billDtos.size() == 0 && bills.size() == 0) {
                billByDate.setVisibility(View.GONE);
                findViewById(R.id.linearLayoutNoRecords).setVisibility(View.VISIBLE);
            } else {
                billByDate.onLoadMoreComplete();
            }
        }
    }


    private class SearchBillTask extends AsyncTask<String, Void, BillDto> {

        // can use UI thread here
        protected void onPreExecute() {
            progressBarSpin.setVisibility(View.VISIBLE);
        }

        // automatically done on worker thread (separate from UI thread)
        protected BillDto doInBackground(final String... args) {
            return FPSDBHelper.getInstance(BillByDateActivity.this).getBillByTransactionId(args[0]);
        }

        // can use UI thread here
        protected void onPostExecute(final BillDto billDtos) {
            progressBarSpin.setVisibility(View.GONE);
            Intent intent = new Intent(BillByDateActivity.this, BillDetailActivity.class);
            intent.putExtra("billData", new Gson().toJson(billDtos));
            Util.LoggingQueue(BillByDateActivity.this, "Bill search List", "Selected bill:" + new Gson().toJson(billDtos));
            intent.putExtra("className", "billDateActivity");
            intent.putExtra("data", data);
            intent.putExtra("search", "date");
            startActivity(intent);
            finish();

        }

    }


}