package com.omneAgate.Bunker;

import android.content.Intent;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.neopixl.pixlui.components.edittext.EditText;
import com.omneAgate.Bunker.dialog.BeneficiaryListAdapter;
import com.omneAgate.DTO.BeneficiarySearchDto;
import com.omneAgate.DTO.EnumDTO.KeyBoardEnum;
import com.omneAgate.DTO.EnumDTO.ServiceListenerType;
import com.omneAgate.Util.CustomProgressDialog;
import com.omneAgate.Util.FPSDBHelper;
import com.omneAgate.Util.NetworkConnection;
import com.omneAgate.Util.PullToRefresh.LoadMoreListView;
import com.omneAgate.Util.Util;
import com.omneAgate.service.HttpClientWrapper;

import java.util.ArrayList;
import java.util.List;

public class BeneficiaryListActivity extends BaseActivity implements View.OnFocusChangeListener, View.OnClickListener {
    LoadMoreListView listView;
    EditText inputCardSearch, inputMobileSearch;
    BeneficiaryListAdapter adapter;
    RelativeLayout keyBoardCustom;
    KeyboardView keyview;
    KeyBoardEnum keyBoardFocused;
    int loadMore = 0;
    List<BeneficiarySearchDto> beneficiarySearchDtos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_beneficiary_list);
        appState = (GlobalAppState) getApplication();
        networkConnection = new NetworkConnection(this);
        httpConnection = new HttpClientWrapper();
        setUpInitialPage();
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    private void setUpInitialPage() {
        Util.LoggingQueue(this, "Beneficiary List", "Setting up main page");
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setTamilText((TextView) findViewById(R.id.tvTitle), R.string.card_no);
        setTamilText((TextView) findViewById(R.id.tvDesc), R.string.aRegisterNo);
        setTamilText((TextView) findViewById(R.id.reg_date), R.string.cardTypes);
        setTamilText((Button) findViewById(R.id.back_pressed), R.string.close);
        setTamilText((TextView) findViewById(R.id.top_textView), getString(R.string.card_details));
        setTamilText((TextView) findViewById(R.id.total_cards), getString(R.string.total_cards));
        ((TextView) findViewById(R.id.total_cards_value)).setText(String.valueOf(FPSDBHelper.getInstance(this).getBeneficiaryCount()));
        keyBoardCustom = (RelativeLayout) findViewById(R.id.key_board_custom);
        keyBoardCustom.setVisibility(View.GONE);
        setUpPopUpPage();
        findViewById(R.id.imageViewBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        findViewById(R.id.back_pressed).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        inputCardSearch = (EditText) findViewById(R.id.inputCardSearch);
        inputMobileSearch = (EditText) findViewById(R.id.inputMobileSearch);
        listView = (LoadMoreListView) findViewById(R.id.listView);
        beneficiarySearchDtos = new ArrayList<>();
        String cardSearch = inputCardSearch.getText().toString().trim();
        String mobileSearch = inputMobileSearch.getText().toString().trim();
        beneficiarySearchDtos = FPSDBHelper.getInstance(BeneficiaryListActivity.this).retrieveAllBeneficiarySearch(cardSearch, mobileSearch, loadMore);
//        for (int i = 0; beneficiarySearchDtos.size() < 4000; i++) {
//            beneficiarySearchDtos.addAll(beneficiarySearchDtos);
//        }
        if (beneficiarySearchDtos != null && beneficiarySearchDtos.size() > 0) {
            adapter = new BeneficiaryListAdapter(BeneficiaryListActivity.this, beneficiarySearchDtos);
            listView.setAdapter(adapter);
        }
        searchAdapter();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.inputCardSearch) {
            checkVisibility();
            keyBoardAppear();
            inputCardSearch.requestFocus();
            keyBoardFocused = KeyBoardEnum.AREGISTER;
        } else if (v.getId() == R.id.inputMobileSearch) {
            checkVisibility();
            keyBoardAppear();
            inputMobileSearch.requestFocus();
            keyBoardFocused = KeyBoardEnum.MOBILE;
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (v.getId() == R.id.inputCardSearch && hasFocus) {
            checkVisibility();
            keyBoardAppear();
            inputCardSearch.requestFocus();
            keyBoardFocused = KeyBoardEnum.AREGISTER;
        } else if (v.getId() == R.id.inputMobileSearch && hasFocus) {
            checkVisibility();
            keyBoardAppear();
            inputMobileSearch.requestFocus();
            keyBoardFocused = KeyBoardEnum.MOBILE;
        }
    }

    private void checkVisibility() {
        if (keyBoardCustom.getVisibility() == View.GONE) {
            keyBoardCustom.setVisibility(View.VISIBLE);
        }
    }

    private void keyBoardAppear() {
        Keyboard keyboard = new Keyboard(this, R.layout.keyboard);
        keyview.setKeyboard(keyboard);
        keyview.setPreviewEnabled(false);
        keyview.setOnKeyboardActionListener(new KeyList());
    }

    private void searchAdapter() {
        findViewById(R.id.linearSearch).setVisibility(View.VISIBLE);
        Keyboard keyboard = new Keyboard(this, R.layout.keyboard);
        //create KeyboardView object
        keyview = (KeyboardView) findViewById(R.id.customkeyboard);
        //attache the keyboard object to the KeyboardView object
        keyview.setKeyboard(keyboard);
        //show the keyboard
        keyview.setVisibility(KeyboardView.VISIBLE);
        keyview.setPreviewEnabled(false);
        //take the keyboard to the front
        keyview.bringToFront();
        //register the keyboard to receive the key pressed
        keyview.setOnKeyboardActionListener(new KeyList());
        keyBoardFocused = KeyBoardEnum.AREGISTER;
        inputMobileSearch.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(inputCardSearch.getWindowToken(), 0);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            inputMobileSearch.setShowSoftInputOnFocus(false);
            inputCardSearch.setShowSoftInputOnFocus(false);
//        }
        inputCardSearch.setOnFocusChangeListener(this);
        inputCardSearch.disableCopyAndPaste();
        inputMobileSearch.disableCopyAndPaste();
        inputMobileSearch.setOnFocusChangeListener(this);
        inputCardSearch.setOnClickListener(this);
        inputMobileSearch.setOnClickListener(this);
        inputCardSearch.setLongClickable(false);
        inputMobileSearch.setLongClickable(false);
//        listView.setOnLoadMoreListener(new LoadMoreListView.OnLoadMoreListener() {
//            @Override
//            public void onLoadMore() {
//                loadMore++;
//                String cardSearch = inputCardSearch.getText().toString().trim();
//                String mobileSearch = inputMobileSearch.getText().toString().trim();
//                Log.e("LoadMore", "loading");
//                new BeneficiarySearchTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, cardSearch, mobileSearch);
//            }
//        });
        keyBoardCustom.setVisibility(View.GONE);
        inputCardSearch.setText("");
        inputMobileSearch.setText("");
//        changeAdapter();
    }

    private void changeAdapter() {
        try {
            String cardSearch = inputCardSearch.getText().toString().trim();
            String mobileSearch = inputMobileSearch.getText().toString().trim();
            new BeneficiarySearchTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, cardSearch, mobileSearch);
        } catch (Exception e) {
            inputCardSearch.setText("");
            inputMobileSearch.setText("");
            Util.LoggingQueue(this, "BeneficiaryListActivity", "Error in search Invalid value entered");
        }
    }

    @Override
    public void processMessage(Bundle message, ServiceListenerType what) {
        if (progressBar != null) {
            progressBar.dismiss();
        }
        switch (what) {
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, BeneficiaryMenuActivity.class));
        Util.LoggingQueue(this, "Beneficiary List", "Back pressed");
        finish();
    }

    private class BeneficiarySearchTask extends AsyncTask<String, Void, List<BeneficiarySearchDto>> {
        @Override
        protected void onPreExecute() {
            try {
                progressBar = new CustomProgressDialog(BeneficiaryListActivity.this);
                progressBar.show();
            } catch (Exception e) {
                Log.e("Error in Progress", e.toString(), e);
            }
        }

        // automatically done on worker thread (separate from UI thread)
        protected List<BeneficiarySearchDto> doInBackground(final String... args) {
            try {
                return FPSDBHelper.getInstance(BeneficiaryListActivity.this).retrieveAllBeneficiarySearch(args[0], args[1], loadMore);
            } catch (Exception e) {
                Log.e("sdfsdf", e.toString(), e);
                return null;
            }
        }

        // can use UI thread here
        protected void onPostExecute(final List<BeneficiarySearchDto> beneficiary) {
            if (progressBar != null) {
                progressBar.dismiss();
            }
            if (beneficiary == null) {
                inputCardSearch.setText("");
                inputMobileSearch.setText("");
                Util.LoggingQueue(BeneficiaryListActivity.this, "BeneficiaryListActivity", "Error in search Invalid value entered");
                return;
            }
            if (beneficiary.size() > 0) {
                beneficiarySearchDtos.addAll(beneficiary);
                listView.setVisibility(View.VISIBLE);
                findViewById(R.id.textNoRecord).setVisibility(View.GONE);
                //   setTamilText((TextView) findViewById(R.id.textViewTop), getString(R.string.num_of_ration_card) + " " + beneficiary.size());
                adapter.notifyDataSetChanged();
                listView.invalidate();
                listView.onLoadMoreComplete();
            } else if (beneficiary.size() == 0 && beneficiarySearchDtos.size() == 0) {
                listView.setVisibility(View.GONE);
                findViewById(R.id.textNoRecord).setVisibility(View.VISIBLE);
            } else {
                listView.onLoadMoreComplete();
            }
        }
    }

    class KeyList implements KeyboardView.OnKeyboardActionListener {
        public void onKey(View v, int keyCode, KeyEvent event) {
        }

        public void onText(CharSequence text) {
        }

        public void swipeLeft() {
        }

        public void onKey(int primaryCode, int[] keyCodes) {
        }

        public void swipeUp() {
        }

        public void swipeDown() {
        }

        public void swipeRight() {
        }

        public void onPress(int primaryCode) {
            if (primaryCode == 8) {
                if (keyBoardFocused == KeyBoardEnum.AREGISTER) {
                    String text = inputCardSearch.getText().toString();
                    if (inputCardSearch.length() > 0) {
                        text = text.substring(0, text.length() - 1);
                        inputCardSearch.setText(text);
                        inputCardSearch.setSelection(text.length());
                    }
                } else if (keyBoardFocused == KeyBoardEnum.MOBILE) {
                    String text = inputMobileSearch.getText().toString();
                    if (inputMobileSearch.length() > 0) {
                        text = text.substring(0, text.length() - 1);
                        inputMobileSearch.setText(text);
                        inputMobileSearch.setSelection(text.length());
                    }
                }
            } else if (primaryCode == 46) {
                keyBoardCustom.setVisibility(View.GONE);
                String cardSearch = inputCardSearch.getText().toString().trim();
                String mobileSearch = inputMobileSearch.getText().toString().trim();
                loadMore = 0;
                beneficiarySearchDtos = new ArrayList<>();
                adapter = new BeneficiaryListAdapter(BeneficiaryListActivity.this, beneficiarySearchDtos);
                listView.setAdapter(adapter);
                new BeneficiarySearchTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, cardSearch, mobileSearch);
            } else {
                char ch = (char) primaryCode;
                if (keyBoardFocused == KeyBoardEnum.AREGISTER) {
                    inputCardSearch.append("" + ch);
                } else if (keyBoardFocused == KeyBoardEnum.MOBILE) {
                    inputMobileSearch.append("" + ch);
                }
            }
        }

        public void onRelease(int primaryCode) {
        }
    }
}
