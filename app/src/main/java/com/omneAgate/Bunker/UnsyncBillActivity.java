package com.omneAgate.Bunker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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
import com.omneAgate.Bunker.dialog.BillISearchAdapterDate;
import com.omneAgate.service.BillSyncManually;

import java.util.ArrayList;
import java.util.List;

/**
 * Created for show the bills
 */
public class UnsyncBillActivity extends BaseActivity {

    List<BillDto> bills;


    LoadMoreListView billSearch;

    int loadMore = 0;

    BillISearchAdapterDate adapter;


    ProgressBar progressBarSpin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_unsync);
        progressBarSpin = (ProgressBar) findViewById(R.id.progressBar1);
        configureData();
    }

    /*Data from server has been set inside this function*/
    private void configureData() {
        try {
            setUpPopUpPage();
            Util.LoggingQueue(this, "Bill search List", "Setting up main page");
            setTamilText((TextView) findViewById(R.id.top_textView), R.string.unsyncBills);
            setTamilText((TextView) findViewById(R.id.fpsInvardactionLabel), R.string.transaction_id);
            setTamilText((TextView) findViewById(R.id.fpsInvardoutwardDateLabel), R.string.billDetailAmountLabel);
            setTamilText((TextView) findViewById(R.id.fpsInvardoutwardGodownNameLabel), R.string.billDetailProductPrice);
            setTamilText((TextView) findViewById(R.id.btnClose), R.string.close);
            billSearch = (LoadMoreListView) findViewById(R.id.listView_fps_stock_inward);
            bills = new ArrayList<>();
            findViewById(R.id.syncBills).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new BillSyncManually(UnsyncBillActivity.this).billSync();
                }
            });
            billSearch = (LoadMoreListView) findViewById(R.id.listView_fps_stock_inward);
            adapter = new BillISearchAdapterDate(this, bills);
            billSearch.setAdapter(adapter);
            billSearch.setOnLoadMoreListener(new LoadMoreListView.OnLoadMoreListener() {
                @Override
                public void onLoadMore() {
                    loadMore++;
                    new SearchBillsUnsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            });
            billSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    submitBill(bills.get(position).getTransactionId());
                }
            });
            new SearchBillsUnsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            Util.LoggingQueue(this, "Error", e.toString());
            Log.e("BillActivity", e.toString(), e);
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
        Util.LoggingQueue(this, "Bill search List", "On back pressed Called");
        finish();
    }

    private class SearchBillsUnsync extends AsyncTask<Long, Void, List<BillDto>> {

        // can use UI thread here
        protected void onPreExecute() {
            progressBarSpin.setVisibility(View.VISIBLE);
        }

        // automatically done on worker thread (separate from UI thread)
        protected List<BillDto> doInBackground(final Long... args) {
            return FPSDBHelper.getInstance(UnsyncBillActivity.this).getAllBillByUnsync(loadMore);
        }

        // can use UI thread here
        protected void onPostExecute(final List<BillDto> billDtos) {

            progressBarSpin.setVisibility(View.GONE);
            if (billDtos.size() > 0) {
                bills.addAll(billDtos);
                billSearch.setVisibility(View.VISIBLE);
                (findViewById(R.id.linearLayoutNoRecords)).setVisibility(View.GONE);
                adapter.notifyDataSetChanged();
                billSearch.invalidate();
                billSearch.onLoadMoreComplete();
            } else if (billDtos.size() == 0 && bills.size() == 0) {
                billSearch.setVisibility(View.GONE);
                findViewById(R.id.linearLayoutNoRecords).setVisibility(View.VISIBLE);
            } else {
                billSearch.onLoadMoreComplete();
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
            return FPSDBHelper.getInstance(UnsyncBillActivity.this).getBillByTransactionId(args[0]);
        }

        // can use UI thread here
        protected void onPostExecute(final BillDto billDtos) {
            progressBarSpin.setVisibility(View.GONE);
            Intent intent = new Intent(UnsyncBillActivity.this, UnsyncBillDetailActivity.class);
            intent.putExtra("billData", new Gson().toJson(billDtos));
            Util.LoggingQueue(UnsyncBillActivity.this, "Bill search List", "Selected bill:" + new Gson().toJson(billDtos));
            startActivity(intent);
            finish();

        }
    }


}