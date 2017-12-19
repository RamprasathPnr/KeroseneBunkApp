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
import com.omneAgate.DTO.EnumDTO.ServiceListenerType;
import com.omneAgate.DTO.GodownStockOutwardDto;
import com.omneAgate.Util.FPSDBHelper;
import com.omneAgate.Util.PullToRefresh.LoadMoreListView;
import com.omneAgate.Util.Util;
import com.omneAgate.Bunker.dialog.InwardDataAdapter;

import java.util.ArrayList;
import java.util.List;

public class FpsStockInwardReceivedActivity extends BaseActivity {

    //List of GodownStockDto for fps stock inward
    private List<GodownStockOutwardDto> fpsStockInwardList = null;

    int loadMore = 0;

    LoadMoreListView billByDate;

    InwardDataAdapter adapter;

    ProgressBar progressBarSpin;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_fps_stock_inward_received);
        appState = (GlobalAppState) getApplication();

    }


    @Override
    protected void onStart() {
        super.onStart();
        configureData();

    }



    private void setTopTextView() {
        progressBarSpin = (ProgressBar) findViewById(R.id.progressBar1);
        Util.LoggingQueue(this, "Stock Inward received activity", "Starting up the page");
        TextView topTv = (TextView) findViewById(R.id.top_textView);
        setTamilText(topTv, R.string.inward_register);
        setTamilText(((TextView) findViewById(R.id.btnClose)), R.string.close);
        setTamilText(((TextView) findViewById(R.id.fpsInvardoutwardDateLabel)), R.string.reference_no);
        setTamilText(((TextView) findViewById(R.id.fpsInvardactionLabel)), R.string.dispatch_date);
        setTamilText(((TextView) findViewById(R.id.fpsInvardoutwardGodownNameLabel)), R.string.Wholesaler_code);
        setTamilText(((TextView) findViewById(R.id.fpsInvardoutwardLapsedTimeabel)), R.string.lapsed_time);
        setTamilText(((TextView) findViewById(R.id.status)), R.string.status);

    }


    /*Data from server has been set inside this function*/
    private void configureData() {
        try {
            setUpPopUpPage();
            setTopTextView();
            fpsStockInwardList = new ArrayList<>();
            billByDate = (LoadMoreListView) findViewById(R.id.listView_fps_stock_inward);
            adapter = new InwardDataAdapter(this, fpsStockInwardList);
            billByDate.setAdapter(adapter);
            billByDate.setOnLoadMoreListener(new LoadMoreListView.OnLoadMoreListener() {
                @Override
                public void onLoadMore() {
                    loadMore++;
                    new InwardByDateTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            });
            new InwardByDateTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            billByDate.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    submitInward(position);
                }
            });
        } catch (Exception e) {
            Log.e("Error", e.toString(), e);
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


    private void submitInward(int position){
        Intent intent = new Intent(this, FpsStockInwardDetailActivity.class);
        Util.LoggingQueue(this, "Stock Inward activity", "Clicked Inward:" + fpsStockInwardList.get(position).toString());
        intent.putExtra("godown", new Gson().toJson(fpsStockInwardList.get(position)));
        intent.putExtra("submitBoolean",false);
        startActivity(intent);
        finish();
    }
    private class InwardByDateTask extends AsyncTask<String, Void, List<GodownStockOutwardDto>> {

        // can use UI thread here
        protected void onPreExecute() {
            progressBarSpin.setVisibility(View.VISIBLE);
        }

        // automatically done on worker thread (separate from UI thread)
        protected List<GodownStockOutwardDto> doInBackground(final String... args) {
            return FPSDBHelper.getInstance(FpsStockInwardReceivedActivity.this).showFpsStockInvard(true, loadMore);
        }

        // can use UI thread here
        protected void onPostExecute(final List<GodownStockOutwardDto> billDtos) {
            progressBarSpin.setVisibility(View.GONE);

            if (billDtos.size() > 0) {
                fpsStockInwardList.addAll(billDtos);
                billByDate.setVisibility(View.VISIBLE);
                (findViewById(R.id.linearLayoutNoRecords)).setVisibility(View.GONE);
                adapter.notifyDataSetChanged();
                billByDate.invalidate();
                billByDate.onLoadMoreComplete();
            } else if (billDtos.size() == 0 && fpsStockInwardList.size() == 0) {
                billByDate.setVisibility(View.GONE);
                findViewById(R.id.linearLayoutNoRecords).setVisibility(View.VISIBLE);
            } else {
                billByDate.onLoadMoreComplete();
            }
        }
    }

    public void onClose(View view) {
        onBackPressed();

    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, FpsStockInwardActivity.class));
        Util.LoggingQueue(this, "Stock Inward received activity", "Called Back press");
        finish();
    }

    @Override
    protected void processMessage(Bundle message, ServiceListenerType what) {

    }


}
