package com.omneAgate.Util;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.Settings;
import android.util.Log;

import com.google.gson.Gson;
import com.omneAgate.Bunker.SplashActivity;
import com.omneAgate.DTO.AppfeatureDto;
import com.omneAgate.DTO.BeneficiaryDto;
import com.omneAgate.DTO.BeneficiaryMemberDto;
import com.omneAgate.DTO.BeneficiarySearchDto;
import com.omneAgate.DTO.BillDto;
import com.omneAgate.DTO.BillItemDto;
import com.omneAgate.DTO.CardTypeDto;
import com.omneAgate.DTO.ChellanProductDto;
import com.omneAgate.DTO.CloseOfProductDto;
import com.omneAgate.DTO.CloseSaleTransactionDto;
import com.omneAgate.DTO.EntitlementMasterRule;
import com.omneAgate.DTO.EntitlementMasterRuleDtod;
import com.omneAgate.DTO.EnumDTO.StockTransactionType;
import com.omneAgate.DTO.FPSIndentRequestDto;
import com.omneAgate.DTO.FPSMigrationDto;
import com.omneAgate.DTO.FPSStockDto;
import com.omneAgate.DTO.FPSStockHistoryDto;
import com.omneAgate.DTO.FpsIntentReqProdDto;
import com.omneAgate.DTO.FpsStoreDto;
import com.omneAgate.DTO.GiveItUpRequestDetailDto;
import com.omneAgate.DTO.GiveItUpRequestDto;
import com.omneAgate.DTO.GodownStockOutward;
import com.omneAgate.DTO.GodownStockOutwardDto;
import com.omneAgate.DTO.GroupDto;
import com.omneAgate.DTO.KeroseneBunkStockDto;
import com.omneAgate.DTO.KeroseneBunkStockHistoryDto;
import com.omneAgate.DTO.LoginHistoryDto;
import com.omneAgate.DTO.LoginResponseDto;
import com.omneAgate.DTO.MessageDto;
import com.omneAgate.DTO.OfflineActivationSynchDto;
import com.omneAgate.DTO.POSStockAdjustmentDto;
import com.omneAgate.DTO.PersonBasedRule;
import com.omneAgate.DTO.ProductDto;
import com.omneAgate.DTO.ProductPriceOverrideDto;
import com.omneAgate.DTO.RegionBasedRule;
import com.omneAgate.DTO.RoleFeatureDto;
import com.omneAgate.DTO.SmsProviderDto;
import com.omneAgate.DTO.SplEntitlementRule;
import com.omneAgate.DTO.StockAllotmentDto;
import com.omneAgate.DTO.StockRequestDto;
import com.omneAgate.DTO.UpgradeDetailsDto;
import com.omneAgate.DTO.UserDetailDto;
import com.omneAgate.DTO.UserDto.BillItemProductDto;
import com.omneAgate.DTO.UserDto.BillUserDto;
import com.omneAgate.DTO.UserDto.MigrationOutDTO;
import com.omneAgate.DTO.UserDto.StockCheckDto;
import com.omneAgate.DTO.UserDto.UserFistSyncDto;
import com.omneAgate.DTO.VersionDto;
import com.omneAgate.Util.Constants.FPSDBConstants;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * FPS database Helper
 */
public class FPSDBHelper extends SQLiteOpenHelper {
    private static final String TAG = FPSDBHelper.class.getCanonicalName();
    // Database Name
    public static final String DATABASE_NAME = "FPS.db";
    //Key for id in tables
    public final static String KEY_ID = "_id";
    // Database Version
    private static final int DATABASE_VERSION = 7;
    // All Static variables
    private static FPSDBHelper dbHelper = null;
    private static SQLiteDatabase database = null;
    private static Context contextValue;

    public FPSDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        database = this.getWritableDatabase();
        dbHelper = this;
        contextValue = context;
    }

    //Singleton to Instantiate the SQLiteOpenHelper
    public static FPSDBHelper getInstance(Context context) {
        if (dbHelper == null) {
            dbHelper = new FPSDBHelper(context);
            openConnection();
        }
        contextValue = context;
        return dbHelper;
    }

    // It is used to open database
    private static void openConnection() {
        if (database == null) {
            database = dbHelper.getWritableDatabase();
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            Log.e("Inside DB", "DB Creation");
            db.execSQL(FPSDBTables.CREATE_USERS_TABLE);
            db.execSQL(FPSDBTables.CREATE_PRODUCTS_TABLE);
            db.execSQL(FPSDBTables.CREATE_BENEFICIARY_TABLE);
            db.execSQL(FPSDBTables.CREATE_BENEFICIARY_MEMBER_TABLE);
            db.execSQL(FPSDBTables.CREATE_TABLE_SMS_PROVIDER);
            db.execSQL(FPSDBTables.CREATE_CARD_TABLE);
            db.execSQL(FPSDBTables.CREATE_BILL_ITEM_TABLE);
            db.execSQL(FPSDBTables.CREATE_STOCK_TABLE);
            db.execSQL(FPSDBTables.CREATE_MASTER_TABLE);
            db.execSQL(FPSDBTables.CREATE_TABLE_UPGRADE);
            db.execSQL(FPSDBTables.CREATE_ENTITLEMENT_RULES_TABLE);
            db.execSQL(FPSDBTables.CREATE_PERSON_RULES_TABLE);
            db.execSQL(FPSDBTables.CREATE_REGION_RULES_TABLE);
            db.execSQL(FPSDBTables.CREATE_CLOSE_SALE_TABLE);
            db.execSQL(FPSDBTables.CREATE_BILL_TABLE);
            db.execSQL(FPSDBTables.CREATE_SPECIAL_RULES_TABLE);
            db.execSQL(FPSDBTables.CREATE_TABLE_LANGUAGE);
            db.execSQL(FPSDBTables.CREATE_FPS_STOCK_INWARD_TABLE);
            db.execSQL(FPSDBTables.CREATE_TABLE_FPS_STOCK_HISTORY);
            db.execSQL(FPSDBTables.CREATE_TABLE_LOGIN_HISTORY);
            db.execSQL(FPSDBTables.CREATE_TABLE_ROLE_FEATURE);
            db.execSQL(FPSDBTables.CREATE_TABLE_PRODUCT_OVERRIDE);
            db.execSQL(FPSDBTables.CREATE_TABLE_PRODUCT_GROUP);
            db.execSQL(FPSDBTables.CREATE_FPS_ADVANCE_STOCK_INWARD_TABLE);
            db.execSQL(FPSDBTables.CREATE_BENEFICIARY_MEMBER_TABLE_IN);
            db.execSQL(FPSDBTables.CREATE_BENEFICIARY_TABLE_IN);
            db.execSQL(FPSDBTables.CREATE_FPS_ADVANCE_MIGRATION_IN_TABLE);
            db.execSQL(FPSDBTables.CREATE_SYNC_MASTER_TABLE);
            db.execSQL(FPSDBTables.CREATE_FPS_ADVANCE_MIGRATION_OUT_TABLE);
            db.execSQL(FPSDBTables.CREATE_FPS_ADVANCE_ADJUST_TABLE);
            db.execSQL(FPSDBTables.CREATE_CLOSE_SALE_PRODUCT_TABLE);
            db.execSQL(FPSDBTables.CREATE_CLOSE_SALE_TABLE);
            db.execSQL("Create index beneficiary_ufc on beneficiary (ufc_code)");
            db.execSQL("Create index beneficiary_register on beneficiary (aRegister)");
            db.execSQL("Create index beneficiary_ration_card on beneficiary (old_ration_card_num)");
            db.execSQL("alter table fps_stock_adjustment add column createdDate VARCHAR(100)");
            db.execSQL("alter table person_rules add column card_type_id INTEGER");
            db.execSQL("alter table region_rules add column hill_area INTEGER");
            db.execSQL("alter table region_rules add column spl_area INTEGER");
            db.execSQL("alter table region_rules add column town_panchayat INTEGER");
            db.execSQL("alter table region_rules add column village_panchayat INTEGER");

         /*   db.execSQL("alter table special_rules add column hill_area INTEGER");
            db.execSQL("alter table special_rules add column spl_area INTEGER");
            db.execSQL("alter table special_rules add column town_panchayat INTEGER");
            db.execSQL("alter table special_rules add column village_panchayat INTEGER");*/
            db.execSQL(FPSDBTables.CREATE_ACTIVE_FPS_TABLE);

            db.execSQL(FPSDBTables.CREATE_GIVE_IT_UP_TABLE);
            db.execSQL(FPSDBTables.CREATE_GIVE_IT_UP_DETAIL_TABLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            Log.e("Dbhelper", "oldversion : " + oldVersion + "new version : " + newVersion);
            switch (oldVersion) {
                case 1:
                    checkTable(db);
                    Log.e("oldversion", "" + oldVersion);
                    Log.e("new_version", "" + newVersion);
                case 2:
                    newVersion = 3;
                    Log.e("new_version", "" + newVersion);
                    db.execSQL("alter table fps_stock_adjustment add column createdDate VARCHAR(100)");
                    db.execSQL("alter table person_rules add column card_type_id INTEGER");
                    db.execSQL("alter table region_rules add column hill_area INTEGER");
                    db.execSQL("alter table region_rules add column spl_area INTEGER");
                    db.execSQL("alter table region_rules add column town_panchayat INTEGER");
                    db.execSQL("alter table region_rules add column village_panchayat INTEGER");
                    db.execSQL("alter table special_rules add column hill_area INTEGER");
                    db.execSQL("alter table special_rules add column spl_area INTEGER");
                    db.execSQL("alter table special_rules add column town_panchayat INTEGER");
                    db.execSQL("alter table special_rules add column village_panchayat INTEGER");
      /*-----------------------------------------------------------------------------------------------*/
                    try {
                        try {
                            db.execSQL(FPSDBTables.CREATE_SPECIAL_RULES_TABLE_TEMP);
                        } catch (Exception e) {
                            Log.e("$$--FPSDBHelper--$$ ", "FPSDBTables.CREATE_SPECIAL_RULES_TABLE_TEMP Exception ->");
                        }
                        try {
                            db.execSQL("INSERT INTO special_rules_temp SELECT * FROM special_rules");
                        } catch (Exception e) {
                            Log.e("$$--FPSDBHelper--$$ ", "INSERT INTO special_rules_temp SELECT * FROM special_rules Exception ->");
                        }
                        try {
                            db.execSQL("drop table if exists special_rules");
                        } catch (Exception e) {
                            Log.e("$$--FPSDBHelper--$$ ", "drop table if exists special_rules Exception ->");
                        }
                        try {
                            db.execSQL(FPSDBTables.CREATE_SPECIAL_RULES_TABLE);
                        } catch (Exception e) {
                            Log.e("$$--FPSDBHelper--$$ ", "FPSDBTables.CREATE_SPECIAL_RULES_TABLE Exception ->");
                        }
                        try {
                            db.execSQL("INSERT INTO special_rules SELECT * FROM special_rules_temp");
                        } catch (Exception e) {
                            Log.e("$$--FPSDBHelper--$$ ", "INSERT INTO special_rules SELECT * FROM special_rules_temp Exception ->");
                        }
                        try {
                            db.execSQL("drop table if exists special_rules_temp");
                        } catch (Exception e) {
                            Log.e("$$--FPSDBHelper--$$ ", "drop table if exists special_rules_temp Exception ->");
                        }
                    } catch (Exception e) {
                        Log.e("FPSDBHelper ", "special_rules_temp Excweption  " + e.toString());
                    }
                    try {
                        db.execSQL("Update users set entitlement_classification = 'District Head Quarter' where entitlement_classification = 'Head Quarter'");
                        db.execSQL("Update users set entitlement_classification = 'Village Panchayats' where entitlement_classification = 'Village Panchayat'");
                        db.execSQL("Update users set entitlement_classification = 'Municipality' where entitlement_classification = 'Other Districts' OR entitlement_classification = 'Belt Area'");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                case 3:
                    newVersion = 4;
                    db.execSQL("alter table fps_stock_adjustment add column createdDate VARCHAR(100)");
                    db.execSQL("alter table person_rules add column card_type_id INTEGER");
                    db.execSQL("alter table region_rules add column hill_area INTEGER");
                    db.execSQL("alter table region_rules add column spl_area INTEGER");
                    db.execSQL("alter table region_rules add column town_panchayat INTEGER");
                    db.execSQL("alter table region_rules add column village_panchayat INTEGER");
                    db.execSQL("alter table special_rules add column hill_area INTEGER");
                    db.execSQL("alter table special_rules add column spl_area INTEGER");
                    db.execSQL("alter table special_rules add column town_panchayat INTEGER");
                    db.execSQL("alter table special_rules add column village_panchayat INTEGER");
                    try {
                        db.execSQL(FPSDBTables.CREATE_SPECIAL_RULES_TABLE_TEMP);
                        db.execSQL("INSERT INTO special_rules_temp SELECT * FROM special_rules");
                        db.execSQL("drop table if exists special_rules");
                        db.execSQL(FPSDBTables.CREATE_SPECIAL_RULES_TABLE);
                        db.execSQL("INSERT INTO special_rules SELECT * FROM special_rules_temp");
                        db.execSQL("drop table if exists special_rules_temp");
                        db.execSQL("Update users set entitlement_classification = 'District Head Quarter' where entitlement_classification = 'Head Quarter'");
                        db.execSQL("Update users set entitlement_classification = 'Village Panchayats' where entitlement_classification = 'Village Panchayat'");
                        db.execSQL("Update users set entitlement_classification = 'Municipality' where entitlement_classification = 'Other Districts' OR entitlement_classification = 'Belt Area'");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                case 4:
                    newVersion = 5;
                    try {
                        db.execSQL("Delete from " + FPSDBTables.TABLE_BENEFICIARY_MEMBER);
                        db.execSQL(FPSDBTables.CREATE_ACTIVE_FPS_TABLE);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("Exception", "while creating active_fps table");
                    }


                case 5:
                    newVersion = 6;

                    try {
                        try {
                            db.execSQL(FPSDBTables.CREATE_BENEFICIARY_TABLE_TEMP);
                        } catch (Exception e) {
                            Log.e("$$--FPSDBHelper--$$ ", "FPSDBTables.CREATE_BENEFICIARY_TABLE_TEMP Exception ->");
                        }
                        try {
                            db.execSQL("INSERT INTO beneficiary_temp SELECT * FROM beneficiary");
                        } catch (Exception e) {
                            Log.e("$$--FPSDBHelper--$$ ", "INSERT INTO beneficiary_temp SELECT * FROM beneficiary Exception ->");
                        }
                        try {
                            db.execSQL("drop table if exists beneficiary");
                        } catch (Exception e) {
                            Log.e("$$--FPSDBHelper--$$ ", "drop table if exists beneficiary Exception ->");
                        }
                        try {
                            db.execSQL(FPSDBTables.CREATE_BENEFICIARY_TABLE);
                        } catch (Exception e) {
                            Log.e("$$--FPSDBHelper--$$ ", "FPSDBTables.CREATE_BENEFICIARY_TABLE Exception ->");
                        }
                        try {
                            db.execSQL("INSERT INTO beneficiary SELECT * FROM beneficiary_temp");
                        } catch (Exception e) {
                            Log.e("$$--FPSDBHelper--$$ ", "INSERT INTO beneficiary SELECT * FROM beneficiary_temp Exception ->");
                        }
                        try {
                            db.execSQL("drop table if exists beneficiary_temp");
                        } catch (Exception e) {
                            Log.e("$$--FPSDBHelper--$$ ", "drop table if exists beneficiary_temp Exception ->");
                        }
                    } catch (Exception e) {
                        Log.e("FPSDBHelper ", "beneficiary_temp Excweption  " + e.toString());
                    }
                case 6:
                    newVersion = 7;
                    try{
                        db.execSQL(FPSDBTables.CREATE_GIVE_IT_UP_TABLE);
                        db.execSQL(FPSDBTables.CREATE_GIVE_IT_UP_DETAIL_TABLE);
                    }catch (Exception e){
                        Log.e("FPSDBHelper","Exception while creating Give_up_table"+e.toString());
                    }

                default:
                    break;
            }
            SharedPreferences sharedpreferences;
            if (SplashActivity.context == null) {
                Log.e("Null", "Context null");
            }
            sharedpreferences = SplashActivity.context.getSharedPreferences("DBData", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putInt("version", newVersion);
            editor.apply();
        } catch (Exception e) {
            Log.e("Upgrade Error", e.toString(), e);
        }
    }

    private void checkTable(SQLiteDatabase db) {
        if (!isTableExists(db, "fps_stock_adjustment")) {
            db.execSQL(FPSDBTables.CREATE_FPS_ADVANCE_ADJUST_TABLE);
        }
        db.execSQL("alter table beneficiary_in add column aadharNumber VARCHAR(30)");
        db.execSQL("drop table if exists indent_request_product");
        db.execSQL("drop table if exists bill_item_offline");
        db.execSQL("drop table if exists lpg_provider");
        db.execSQL("drop table if exists offline_bill");
        db.execSQL("drop table if exists stock_allotment_details");
        db.execSQL("drop table if exists manual_stock_inward");
        db.execSQL("drop table if exists manual_stock_inward_product");
        db.execSQL("drop table if exists registration");
        db.execSQL("drop table if exists offline_activation");
        db.execSQL("drop table if exists ration_card_images");
    }

    public boolean isTableExists(SQLiteDatabase db, String tableName) {
        String selectQuery = "SELECT * FROM sqlite_master WHERE name ='" + tableName + "' and type='table'";
        if (db == null) {
            db = this.getReadableDatabase();
        }
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            return true;
        }
        cursor.close();
        return false;
    }

    //This function loads data to language table
    public void insertErrorMessages(MessageDto message) {
        try {
            ContentValues values = new ContentValues();
            values.put(FPSDBConstants.KEY_LANGUAGE_CODE, message.getLanguageCode());
            values.put(FPSDBConstants.KEY_LANGUAGE_MESSAGE, message.getDescription());
            values.put(FPSDBConstants.KEY_LANGUAGE_L_MESSAGE, message.getLocalDescription());
            database.insert(FPSDBTables.TABLE_LANGUAGE, null, values);
        } catch (Exception e) {
            Log.e("Error Message", e.toString(), e);
        }
    }

    // This function inserts details to FPSDBTables.TABLE_FPS_STOCK_INWARD
    public void insertFpsStockInwardDetails(Set<GodownStockOutwardDto> fpsStoInwardDto) {
        try {
            if (!fpsStoInwardDto.isEmpty()) {
                for (GodownStockOutwardDto fpsStockInward : fpsStoInwardDto) {
                    ContentValues values = new ContentValues();
                    if (fpsStockInward.getId() != null)
                        values.put("_id", fpsStockInward.getId());
                    values.put(FPSDBConstants.KEY_FPS_STOCK_INWARD_GODOWNID, fpsStockInward.getGodownId());
                    values.put(FPSDBConstants.KEY_FPS_STOCK_INWARD_FPSID, fpsStockInward.getFpsId());
                    values.put(FPSDBConstants.KEY_FPS_STOCK_INWARD_OUTWARD_DATE, fpsStockInward.getOutwardDate());
                    values.put(FPSDBConstants.KEY_FPS_STOCK_INWARD_PRODUCTID, fpsStockInward.getProductId());
                    NumberFormat formatter = new DecimalFormat("#0.000");
                    values.put(FPSDBConstants.KEY_FPS_STOCK_INWARD_QUANTITY, formatter.format(fpsStockInward.getQuantity()));
                    values.put(FPSDBConstants.KEY_FPS_STOCK_INWARD_UNIT, fpsStockInward.getUnit());
                    values.put(FPSDBConstants.KEY_FPS_STOCK_INWARD_BATCH_NO, fpsStockInward.getBatchno());
                    values.put(FPSDBConstants.KEY_FPS_STOCK_INWARD_DELIEVERY_CHELLANID, fpsStockInward.getDeliveryChallanId());
                    values.put("godown_name", fpsStockInward.getGodownName());
                    values.put("godown_code", fpsStockInward.getGodownCode());
                    values.put("referenceNo", fpsStockInward.getReferenceNo());
                    if (fpsStockInward.getVehicleN0() != null)
                        values.put("vehicleN0", fpsStockInward.getVehicleN0());
                    if (fpsStockInward.getDeliveryChallanId() != null)
                        values.put("challanId", fpsStockInward.getDeliveryChallanId());
                    if (fpsStockInward.getDriverName() != null)
                        values.put("driverName", fpsStockInward.getDriverName());
                    if (fpsStockInward.getTransportName() != null)
                        values.put("transportName", fpsStockInward.getTransportName());
                    if (fpsStockInward.getDriverMobileNumber() != null)
                        values.put("driverMobileNumber", fpsStockInward.getDriverMobileNumber());
                    if (!fpsStockInward.isFpsAckStatus()) {
                        values.put(FPSDBConstants.KEY_FPS_STOCK_INWARD_IS_FPSACKSTATUS, 0);
                    } else {
                        values.put(FPSDBConstants.KEY_FPS_STOCK_INWARD_IS_FPSACKSTATUS, 1);
                    }
                    SimpleDateFormat dateFormat = new SimpleDateFormat(
                            "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    Date date = new Date(fpsStockInward.getFpsAckDate());
                    values.put(FPSDBConstants.KEY_FPS_STOCK_INWARD_FPSACKDATE, dateFormat.format(date));
                    if (fpsStockInward.getFpsReceiQuantity() != null) {
                        values.put(FPSDBConstants.KEY_FPS_STOCK_INWARD_FPSRECEIVEIQUANTITY, formatter.format(fpsStockInward.getFpsReceiQuantity()));
                    } else
                        values.put(FPSDBConstants.KEY_FPS_STOCK_INWARD_FPSRECEIVEIQUANTITY, 0);
                    values.put(FPSDBConstants.KEY_FPS_STOCK_INWARD_CREATEDBY, fpsStockInward.getCreatedby());
                    Log.e("stock inward", fpsStockInward.toString());
                    database.insertWithOnConflict(FPSDBTables.TABLE_FPS_STOCK_INWARD, "_id", values, SQLiteDatabase.CONFLICT_REPLACE);
                }
            }
        } catch (Exception e) {
            Log.e("Error_ex", e.toString());
        }
    }
//    public void stockAdjustmentData() {
//        String selectQuery = "SELECT *  FROM fps_stock_adjustment where isAdjusted = 0";
//        Cursor cursor = database.rawQuery(selectQuery, null);
//        cursor.moveToFirst();
//        for (int i = 0; i < cursor.getCount(); i++) {
//            adjustStocks(new POSStockAdjustmentDto(cursor));
//            cursor.moveToNext();
//        }
//        cursor.close();
//    }

    public void stockAdjustmentData(List<POSStockAdjustmentDto> StockAdjustmentList) {
        /*String selectQuery = "SELECT *  FROM fps_stock_adjustment where isAdjusted = 0";
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        for(int i=0;i<cursor.getCount();i++) {
            adjustStocks(new POSStockAdjustmentDto(cursor));
            cursor.moveToNext();
        }
        cursor.close();*/
        Log.e("dbhelper", "fpsStockAdjustmentList.size()..." + StockAdjustmentList.size());
        for (int i = 0; i < StockAdjustmentList.size(); i++) {
            Log.e("dbhelper", "fpsStockAdjustmentList.get(i)..." + StockAdjustmentList.get(i));
            adjustStocks(StockAdjustmentList.get(i));
        }
    }

    private void adjustStocks(POSStockAdjustmentDto posStockAdjustmentDto) {
        FPSStockDto stocks = getAllProductStockDetails(posStockAdjustmentDto.getProductId());
        Double quantity = stocks.getQuantity();
        String typeAdjust;
        if (posStockAdjustmentDto.getRequestType().equalsIgnoreCase("STOCK_INCREMENT")) {
            quantity = quantity + posStockAdjustmentDto.getQuantity();
            typeAdjust = "INCRIMENT";
        } else {
            quantity = quantity - posStockAdjustmentDto.getQuantity();
            typeAdjust = "DECRIMENT";
        }
        stocks.setQuantity(quantity);
        stockUpdate(stocks);
        stockAdjustmentAcknowledge(posStockAdjustmentDto);
        insertStockHistory(stocks.getQuantity(), quantity, "STOCK ADJUSTMENT " + typeAdjust, posStockAdjustmentDto.getQuantity(), posStockAdjustmentDto.getProductId());
    }

    public List<POSStockAdjustmentDto> showFpsStockAdjustment(boolean ackStatus, int position) {
        String selectQuery;
        long offSet = position * 50;
//        selectQuery = "SELECT  * FROM fps_stock_adjustment where isAdjusted = 0 group by _id order by createdDate desc limit 50  OFFSET " + offSet;
        selectQuery = "SELECT  * FROM fps_stock_adjustment where isAdjusted = 0";
        Cursor cursor = database.rawQuery(selectQuery, null);
        Log.e("Query value", "Cursor:" + cursor.getCount());
        List<POSStockAdjustmentDto> fpsAdjustmentList = new ArrayList<>();
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            fpsAdjustmentList.add(new POSStockAdjustmentDto(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return fpsAdjustmentList;
    }

    private void stockAdjustmentAcknowledge(POSStockAdjustmentDto stockAdjustmentDto) {
        ContentValues values = new ContentValues();
        try {
            SimpleDateFormat billDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
            values.put("dateOfAck", billDate.format(new Date()));
            values.put("isServerAdded", 0);
            values.put("isAdjusted", 1);
            database.update("fps_stock_adjustment", values, KEY_ID + "=" + stockAdjustmentDto.getId(), null);
        } catch (Exception e) {
            Log.e("Error", e.toString(), e);
        }
    }

    // This function inserts details to FPSDBTables.TABLE_FPS_STOCK_INWARD
    public void insertTempFpsStockInwardDetails(List<GodownStockOutward> fpsStoInwardDto) {
        if (!fpsStoInwardDto.isEmpty()) {
            for (GodownStockOutward fpsStockInward : fpsStoInwardDto) {
                ContentValues values = new ContentValues();
                values.put(FPSDBConstants.KEY_FPS_STOCK_INWARD_GODOWNID, fpsStockInward.getGodownId());
                values.put(FPSDBConstants.KEY_FPS_STOCK_INWARD_FPSID, fpsStockInward.getFpsId());
                values.put(FPSDBConstants.KEY_FPS_STOCK_INWARD_OUTWARD_DATE, fpsStockInward.getOutwardDate());
                values.put(FPSDBConstants.KEY_FPS_STOCK_INWARD_PRODUCTID, fpsStockInward.getProductId());
                NumberFormat formatter = new DecimalFormat("#0.000");
                values.put(FPSDBConstants.KEY_FPS_STOCK_INWARD_QUANTITY, formatter.format(fpsStockInward.getQuantity()));
                values.put(FPSDBConstants.KEY_FPS_STOCK_INWARD_UNIT, fpsStockInward.getUnit());
                values.put(FPSDBConstants.KEY_FPS_STOCK_INWARD_BATCH_NO, fpsStockInward.getBatchno());
                values.put(FPSDBConstants.KEY_FPS_STOCK_INWARD_DELIEVERY_CHELLANID, fpsStockInward.getDeliveryChallanId());
                values.put("godown_name", fpsStockInward.getGodownName());
                values.put("godown_code", fpsStockInward.getGodownCode());
                values.put("referenceNo", fpsStockInward.getReferenceNo());
                values.put(FPSDBConstants.KEY_FPS_STOCK_INWARD_IS_FPSACKSTATUS, fpsStockInward.getFpsAckStatus());
                SimpleDateFormat dateFormat = new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Date date = new Date(fpsStockInward.getFpsAckDate());
                values.put(FPSDBConstants.KEY_FPS_STOCK_INWARD_FPSACKDATE, dateFormat.format(date));
                if (fpsStockInward.getFpsReceiQuantity() != null) {
                    values.put(FPSDBConstants.KEY_FPS_STOCK_INWARD_FPSRECEIVEIQUANTITY, formatter.format(fpsStockInward.getFpsReceiQuantity()));
                } else
                    values.put(FPSDBConstants.KEY_FPS_STOCK_INWARD_FPSRECEIVEIQUANTITY, 0);
                values.put(FPSDBConstants.KEY_FPS_STOCK_INWARD_CREATEDBY, fpsStockInward.getCreatedby());
                Log.e("stock inward", fpsStockInward.toString());
                database.insert(FPSDBTables.TABLE_FPS_STOCK_INWARD, null, values);
            }
        }
    }

    //This function inserts details to TABLE_CARD_TYPE.
    public void insertCardTypeData(Set<CardTypeDto> cardTypeDto) {
        ContentValues values = new ContentValues();
        if (!cardTypeDto.isEmpty()) {
            for (CardTypeDto cardType : cardTypeDto) {
                values.put(KEY_ID, cardType.getId());
                values.put(FPSDBConstants.KEY_CARD_TYPE, String.valueOf(cardType.getType()));
                if (String.valueOf(cardType.getDescription()) != null) {
                    values.put(FPSDBConstants.KEY_CARD_DESCRIPTION, cardType.getDescription());
                }
                if (cardType.isDeleted()) {
                    values.put("isDeleted", 1);
                } else {
                    values.put("isDeleted", 0);
                }
                database.insertWithOnConflict(FPSDBTables.TABLE_CARD_TYPE, KEY_ID, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
        }
    }

    //This function inserts details to FPSDBTables.TABLE_USERS,
    public void insertUserDetailData(Set<UserDetailDto> userdetailDto) {
        ContentValues values = new ContentValues();
        if (!userdetailDto.isEmpty()) {
            for (UserDetailDto userDetail : userdetailDto) {
                values.put(KEY_ID, userDetail.getId());
                values.put(FPSDBConstants.KEY_USERS_NAME, userDetail.getUserId().toLowerCase());
                values.put(FPSDBConstants.KEY_USERS_ID, userDetail.getUsername().toLowerCase());
                values.put(FPSDBConstants.KEY_USERS_PASS_HASH, userDetail.getPassword());
                values.put(FPSDBConstants.KEY_USERS_PROFILE, userDetail.getProfile());
                //  values.put(FPSDBConstants.KEY_USERS_DISTRICT_ID, userDetail.getKerosene().getTalukDto().getDistrictDto().getId());
                // values.put(FPSDBConstants.KEY_USERS_TALUK_ID, userDetail.getKerosene().getTalukDto().getId());
              /*  values.put(FPSDBConstants.KEY_USERS_FPS_ID, userDetail.getFpsStore().getId());
                values.put(FPSDBConstants.KEY_USERS_CONTACT_PERSON, userDetail.getFpsStore().getContactPerson());
                values.put(FPSDBConstants.KEY_USERS_PHONE_NUMBER, userDetail.getFpsStore().getPhoneNumber());
                values.put(FPSDBConstants.KEY_USERS_ADDRESS_LINE1, userDetail.getFpsStore().getAddressLine1());
                values.put(FPSDBConstants.KEY_USERS_ADDRESS_LINE2, userDetail.getFpsStore().getAddressLine2());
                values.put(FPSDBConstants.KEY_USERS_ADDRESS_LINE3, userDetail.getFpsStore().getAddressLine3());
                values.put(FPSDBConstants.KEY_USERS_CODE, userDetail.getFpsStore().getGeneratedCode());
                values.put("entitlement_classification", userDetail.getFpsStore().getEntitlementClassification());
                values.put("village_name", userDetail.getFpsStore().getVillageName());
                values.put("village_code", userDetail.getFpsStore().getVillageCode());
                values.put("taluk_name", userDetail.getFpsStore().getTalukName());
                values.put("taluk_code", userDetail.getFpsStore().getTalukCode());
                values.put("district_name", userDetail.getFpsStore().getDistrictName());
                if (userDetail.getFpsStore().getFpsCategory() != null)
                    values.put("fps_category", userDetail.getFpsStore().getFpsCategory().toString());
                if (userDetail.getFpsStore().getFpsType() != null)
                    values.put("fps_type", userDetail.getFpsStore().getFpsType().toString());
                values.put("district_code", userDetail.getFpsStore().getDistrictCode());
                values.put("device_sim_no", userDetail.getFpsStore().getDeviceSimNo());
                values.put("agency_name", userDetail.getFpsStore().getAgencyName());
                values.put("agency_code", userDetail.getFpsStore().getAgencyCode());
                values.put("operation_closing_time", userDetail.getFpsStore().getOperationClosingTime());
                values.put("operation_opening_time", userDetail.getFpsStore().getOperationOpeningTime());
                int status = 0;
                if (userDetail.getFpsStore().isActive()) {
                    status = 1;
                }
                values.put(FPSDBConstants.KEY_USERS_IS_ACTIVE, status);*/
                database.insertWithOnConflict(FPSDBTables.TABLE_USERS, KEY_ID, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
        }
    }

    // This function inserts details to TABLE_STOCK
    public void insertFpsStockData(Set<KeroseneBunkStockDto> fpsStockDto) {
        if (!fpsStockDto.isEmpty()) {
            for (KeroseneBunkStockDto fpsStock : fpsStockDto) {
                ContentValues values = new ContentValues();
                values.put(FPSDBConstants.KEY_STOCK_FPS_ID, fpsStock.getBunkId());
                values.put(FPSDBConstants.KEY_STOCK_PRODUCT_ID, fpsStock.getProductId());
                NumberFormat formatter = new DecimalFormat("#0.000");
                values.put(FPSDBConstants.KEY_STOCK_QUANTITY, formatter.format(fpsStock.getQuantity()));
                values.put(FPSDBConstants.KEY_STOCK_REORDER_LEVEL, fpsStock.getReorderLevel());
                if (fpsStock.isEmailAction()) {
                    values.put(FPSDBConstants.KEY_STOCK_EMAIL_ACTION, 0);
                } else {
                    values.put(FPSDBConstants.KEY_STOCK_EMAIL_ACTION, 1);
                }
                if (fpsStock.isSmsMSAction()) {
                    values.put(FPSDBConstants.KEY_STOCK_SMS_ACTION, 0);
                } else {
                    values.put(FPSDBConstants.KEY_STOCK_SMS_ACTION, 1);
                }
                database.insertWithOnConflict(FPSDBTables.TABLE_STOCK, FPSDBConstants.KEY_STOCK_PRODUCT_ID, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
        }
    }

    public void insertRoles(long userId, Set<AppfeatureDto> roleFeatures) {
        ContentValues values = new ContentValues();
        for (AppfeatureDto features : roleFeatures) {
            try {
                values.put(FPSDBConstants.KEY_ROLE_USERID, userId);
                values.put(FPSDBConstants.KEY_ROLE_FEATUREID, features.getFeatureId());
                values.put("role_id", features.getFeatureId());
                values.put(FPSDBConstants.KEY_ROLE_NAME, features.getFeatureName());
                values.put(FPSDBConstants.KEY_ROLE_PARENTID, features.getParentId());
                values.put(FPSDBConstants.KEY_ROLE_TYPE, features.getName());
                values.put("isDeleted", 1);
                database.insert(FPSDBTables.TABLE_ROLE_FEATURE, null, values);
            } catch (Exception e) {
                Log.e("roleFeature", e.toString(), e);
            }
        }
    }

    public void insertFpsStockDataAdmin(List<FPSStockDto> fpsStockList) {
        if (!fpsStockList.isEmpty()) {
            for (FPSStockDto fpsStock : fpsStockList) {
                ContentValues values = new ContentValues();
                Log.e("fps_stock_product_id", "" + fpsStock.getProductId());
                values.put(FPSDBConstants.KEY_STOCK_FPS_ID, fpsStock.getFpsId());
                values.put(FPSDBConstants.KEY_STOCK_PRODUCT_ID, fpsStock.getProductId());
                NumberFormat formatter = new DecimalFormat("#0.000");
                values.put(FPSDBConstants.KEY_STOCK_QUANTITY, formatter.format(fpsStock.getQuantity()));
                values.put(FPSDBConstants.KEY_STOCK_REORDER_LEVEL, fpsStock.getReorderLevel());
                if (fpsStock.isEmailAction()) {
                    values.put(FPSDBConstants.KEY_STOCK_EMAIL_ACTION, 0);
                } else {
                    values.put(FPSDBConstants.KEY_STOCK_EMAIL_ACTION, 1);
                }
                if (fpsStock.isSmsMSAction()) {
                    values.put(FPSDBConstants.KEY_STOCK_SMS_ACTION, 0);
                } else {
                    values.put(FPSDBConstants.KEY_STOCK_SMS_ACTION, 1);
                }
                insertStockHistory(0.0, fpsStock.getQuantity(), "INITIAL STOCK", fpsStock.getQuantity(), fpsStock.getProductId());
                database.insertWithOnConflict(FPSDBTables.TABLE_STOCK, FPSDBConstants.KEY_STOCK_PRODUCT_ID, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
        }
    }

    public void insertSyncValue(List<UserFistSyncDto> userSync) {
        if (!userSync.isEmpty()) {
            for (UserFistSyncDto fpsStock : userSync) {
                ContentValues values = new ContentValues();
                values.put("name", fpsStock.getValue());
                values.put("value", fpsStock.getMasterValue());
                database.insert("master_first_sync", null, values);
            }
        }
    }

    public void insertValues() {
        insertMaserData("serverUrl", "http://192.168.1.53:9097");
        insertMaserData("purgeBill", "30");
        insertMaserData("syncTime", null);
        insertMaserData("status", null);
        insertMaserData("printer", null);
        insertMaserData("language", "ta");
    }

    public void insertMaserData(String name, String value) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("value", value);
        if (database == null) {
            database = dbHelper.getWritableDatabase();
        }
        database.insertWithOnConflict(FPSDBTables.TABLE_CONFIG_TABLE, "name", values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    //insert login data inside database
    public void insertLoginUserData(LoginResponseDto loginResponse, String password) {
        ContentValues values = new ContentValues();
        try {
            values.put(KEY_ID, loginResponse.getUserDetailDto().getId());
            values.put(FPSDBConstants.KEY_USERS_NAME, loginResponse.getUserDetailDto().getUserId().toLowerCase());
            values.put(FPSDBConstants.KEY_USERS_ID, loginResponse.getUserDetailDto().getUsername().toLowerCase());
            values.put(FPSDBConstants.KEY_USERS_PASS_HASH, loginResponse.getUserDetailDto().getPassword());
            values.put(FPSDBConstants.KEY_USERS_PROFILE, loginResponse.getUserDetailDto().getProfile());
            values.put("encrypted_password", Util.EncryptPassword(password));
            if (loginResponse.getKeroseneBunkDto() != null) {
                values.put(FPSDBConstants.KEY_USERS_FPS_ID, loginResponse.getKeroseneBunkDto().getId());
                values.put(FPSDBConstants.KEY_USERS_CODE, loginResponse.getKeroseneBunkDto().getGeneratedCode());
                values.put(FPSDBConstants.KEY_USERS_PHONE_NUMBER, loginResponse.getKeroseneBunkDto().getContactNumber());
                values.put(FPSDBConstants.KEY_USERS_ADDRESS_LINE1, loginResponse.getKeroseneBunkDto().getAddress());
                values.put(FPSDBConstants.KEY_USERS_CONTACT_PERSON, loginResponse.getKeroseneBunkDto().getContactPersonName());
                values.put("entitlement_classification", loginResponse.getKeroseneBunkDto().getEntitlementClassification());
                values.put("operation_closing_time", loginResponse.getKeroseneBunkDto().getOperationClosingTime());
                values.put("operation_opening_time", loginResponse.getKeroseneBunkDto().getOperationOpeningTime());
                if (loginResponse.getKeroseneBunkDto().getKeroseneBunkCategory() != null)
                    values.put("agency_name", loginResponse.getKeroseneBunkDto().getKeroseneBunkCategory().toString());
                int status = 0;
                if (loginResponse.getKeroseneBunkDto().isStatus()) {
                    status = 1;
                }
                values.put(FPSDBConstants.KEY_USERS_IS_ACTIVE, status);
                if (loginResponse.getKeroseneBunkDto().getTalukDto() != null) {
                    values.put("taluk_name", loginResponse.getKeroseneBunkDto().getTalukDto().getId());
                    values.put(FPSDBConstants.KEY_USERS_TALUK_ID, loginResponse.getKeroseneBunkDto().getTalukDto().getId());
                    if (loginResponse.getKeroseneBunkDto().getTalukDto().getDistrictDto() != null)
                        values.put("district_name", loginResponse.getKeroseneBunkDto().getTalukDto().getDistrictDto().getId());
                    values.put(FPSDBConstants.KEY_USERS_DISTRICT_ID, loginResponse.getKeroseneBunkDto().getTalukDto().getDistrictDto().getId());
                }
            }
        } catch (Exception e) {
            Log.e("User data", e.toString(), e);
        } finally {
            database.insertWithOnConflict(FPSDBTables.TABLE_USERS, KEY_ID, values, SQLiteDatabase.CONFLICT_REPLACE);
        }
    }

    public long insertStockInward(StockRequestDto stock) {
        try {
            ContentValues values = new ContentValues();
            values.put(FPSDBConstants.KEY_FPS_STOCK_INWARD_GODOWNID, stock.getGodownId());
            values.put(FPSDBConstants.KEY_FPS_STOCK_INWARD_FPSID, stock.getFpsId());
            values.put(FPSDBConstants.KEY_FPS_STOCK_INWARD_IS_FPSACKSTATUS, 1);
            SimpleDateFormat dateFormat = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = new Date(stock.getDate());
            values.put(FPSDBConstants.KEY_FPS_STOCK_INWARD_FPSACKDATE, dateFormat.format(date));
            values.put("status", "R");
            values.put(FPSDBConstants.KEY_FPS_STOCK_INWARD_CREATEDBY, stock.getCreatedBy());
            values.put(FPSDBConstants.KEY_FPS_STOCK_INWARD_RETRY_COUNT, 0);
            values.put(FPSDBConstants.KEY_FPS_STOCK_INWARD_RETRY_TIME, System.currentTimeMillis());
            if (!database.isOpen()) {
                database = dbHelper.getWritableDatabase();
            }
            database.insert(FPSDBTables.TABLE_FPS_MANUAL_STOCK_INWARD, null, values);
            String selectQuery = "SELECT " + KEY_ID + " FROM " + FPSDBTables.TABLE_FPS_MANUAL_STOCK_INWARD + " order by " + KEY_ID + " DESC limit 1";
            Cursor cursor = database.rawQuery(selectQuery, null);
            cursor.moveToFirst();
            long id = cursor.getLong(cursor.getColumnIndex(KEY_ID));
            insertManualInwardProduct(id, stock);
            cursor.close();
            return id;
        } catch (Exception e) {
            Log.e("Stock Inward", e.toString(), e);
            return 0;
        }
    }

    public void insertLoginHistory(LoginHistoryDto loginHistory) {
        try {
            ContentValues values = new ContentValues();
            values.put("login_time", loginHistory.getLoginTime());
            values.put("login_type", loginHistory.getLoginType());
            values.put("user_id", loginHistory.getUserId());
            values.put("bunk_id", loginHistory.getBunkId());
            values.put("transaction_id", loginHistory.getTransactionId());
            values.put("created_time", new Date().getTime());
            values.put("is_sync", 0);
            if (!database.isOpen()) {
                database = dbHelper.getWritableDatabase();
            }
            database.insert(FPSDBTables.TABLE_LOGIN_HISTORY, null, values);
        } catch (Exception e) {
            Log.e("Login History", e.toString(), e);
        }
    }

    public List<POSStockAdjustmentDto> stockAdjustmentDataToServer() {
        List<POSStockAdjustmentDto> posAcknowledge = new ArrayList<>();
        String selectQuery = "SELECT *  FROM fps_stock_adjustment where isAdjusted = 1 and isServerAdded = 0";
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            posAcknowledge.add(new POSStockAdjustmentDto(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return posAcknowledge;
    }

    //Update the bill
    public void adjustmentUpdate(POSStockAdjustmentDto bill) {
        ContentValues values = new ContentValues();
        values.put("isServerAdded", 1);
        database.update("fps_stock_adjustment", values, KEY_ID + "='" + bill.getId() + "'", null);
    }

    public void insertBackGroundLoginHistory(LoginHistoryDto loginHistory) {
        try {
            ContentValues values = new ContentValues();
            values.put("login_time", loginHistory.getLoginTime());
            values.put("login_type", loginHistory.getLoginType());
            values.put("user_id", loginHistory.getUserId());
            values.put("fps_id", "" + loginHistory.getBunkId());
            values.put("created_time", new Date().getTime());
            values.put("is_sync", 1);
            values.put("is_logout_sync", 1);
            if (!database.isOpen()) {
                database = dbHelper.getWritableDatabase();
            }
            database.insert(FPSDBTables.TABLE_LOGIN_HISTORY, null, values);
        } catch (Exception e) {
            Log.e("Login History", e.toString(), e);
        }
    }

    private long insertManualInwardProduct(long id, StockRequestDto stock) {
        ContentValues values = new ContentValues();
        for (StockRequestDto.ProductList products : stock.getProductLists()) {
            values.put("refId", id);
            values.put(FPSDBConstants.KEY_FPS_STOCK_INWARD_PRODUCTID, products.getId());
            NumberFormat formatter = new DecimalFormat("#0.000");
            values.put(FPSDBConstants.KEY_FPS_STOCK_INWARD_QUANTITY, formatter.format(products.getQuantity()));
            values.put(FPSDBConstants.KEY_FPS_STOCK_INWARD_FPSRECEIVEIQUANTITY, formatter.format(products.getRecvQuantity()));
            database.insert(FPSDBTables.TABLE_FPS_MANUAL_STOCK_INWARD_PRODUCT_TABLE, null, values);
            FPSStockDto stockList = getAllProductStockDetails(products.getId());
            double closing = stockList.getQuantity() + products.getRecvQuantity();
            insertStockHistory(stockList.getQuantity(), closing, "MANUAL INWARD", products.getRecvQuantity(), products.getId());
            stockList.setQuantity(closing);
            stockUpdate(stockList);
        }
        return id;
    }

    //Insert into Stock history
    public void insertStockHistory(double openingBalance, double closingBalance, String action, double changeInBalance, long productId) {
        ContentValues values = new ContentValues();
        Log.e("Stock History", "Insert History");
        try {
            NumberFormat formatter = new DecimalFormat("#0.000");
            values.put(FPSDBConstants.KEY_STOCK_HISTORY_OPEN_BALANCE, Double.parseDouble(formatter.format(openingBalance)));
            values.put(FPSDBConstants.KEY_STOCK_HISTORY_CLOSE_BALANCE, Double.parseDouble(formatter.format(closingBalance)));
            values.put(FPSDBConstants.KEY_STOCK_HISTORY_ACTION, action);
            values.put(FPSDBConstants.KEY_STOCK_HISTORY_CHANGE_BALANCE, Double.parseDouble(formatter.format(changeInBalance)));
            values.put(FPSDBConstants.KEY_STOCK_HISTORY_PRODUCT_ID, productId);
            values.put(FPSDBConstants.KEY_STOCK_HISTORY_DATE, new Date().getTime());
            SimpleDateFormat billDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
            values.put(FPSDBConstants.KEY_STOCK_DATE, billDate.format(new Date()));
            database.insert(FPSDBTables.TABLE_STOCK_HISTORY, null, values);
        } catch (Exception e) {
            Log.e("Stock History", e.toString(), e);
        }
    }

    public void stockAdjustment(Set<POSStockAdjustmentDto> stockAdjustment) {
        try {
            Log.e("Stock Adj", stockAdjustment.toString());
            for (POSStockAdjustmentDto stockAdjustmentDto : stockAdjustment) {
                String typeAdjust;
                FPSStockDto stocks = getAllProductStockDetails(stockAdjustmentDto.getProductId());
                Log.e("FPSStockDto", stockAdjustment.toString());
                Double quantity = stocks.getQuantity();
                quantity = quantity + stockAdjustmentDto.getQuantity();
                if (stockAdjustmentDto.getQuantity() >= 0.0) {
                    typeAdjust = "INCRIMENT";
                } else {
                    typeAdjust = "DECRIMENT";
                }
                stocks.setQuantity(quantity);
                stockUpdate(stocks);
                insertStockHistory(stocks.getQuantity(), quantity, "STOCK ADJUSTMENT " + typeAdjust, stockAdjustmentDto.getQuantity(), stockAdjustmentDto.getProductId());
            }
        } catch (Exception e) {
            Log.e("Adjustment Error", "Stock Adjustment Error", e);
        }
    }

    public void stockAdjustmentFirstSync(Set<POSStockAdjustmentDto> stockAdjustment) {
        try {
            Log.e("Stock Adj", stockAdjustment.toString());
            for (POSStockAdjustmentDto stockAdjustmentDto : stockAdjustment) {
                ContentValues values = new ContentValues();
                values.put(KEY_ID, stockAdjustmentDto.getId());
                values.put("product_id", stockAdjustmentDto.getProductId());
                values.put("quantity", stockAdjustmentDto.getQuantity());
                SimpleDateFormat billDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
                if (stockAdjustmentDto.getPosAckDate() != null)
                    values.put("dateOfAck", billDate.format(new Date(stockAdjustmentDto.getPosAckDate())));
                if (stockAdjustmentDto.getPosAckStatus() == null || !stockAdjustmentDto.getPosAckStatus()) {
                    values.put("isServerAdded", 0);
                    values.put("isAdjusted", 0);
                } else {
                    values.put("isServerAdded", 1);
                    values.put("isAdjusted", 1);
                }
                values.put("requestType", stockAdjustmentDto.getRequestType());
                Log.e("DBhelper", "created date " + stockAdjustmentDto.getCreatedDate());
                values.put("createdDate", stockAdjustmentDto.getCreatedDate());
                database.insertWithOnConflict("fps_stock_adjustment", null, values, SQLiteDatabase.CONFLICT_IGNORE);
            }
        } catch (Exception e) {
            Log.e("Adjustment Error", "Stock Adjustment Error", e);
        }
    }
// public void stockAdjustmentFirstSync(Set<POSStockAdjustmentDto> stockAdjustment) {
//    try {
//        Log.e("Stock Adj", stockAdjustment.toString());
//        for (POSStockAdjustmentDto stockAdjustmentDto : stockAdjustment) {
//            try {
//                ContentValues values = new ContentValues();
//                values.put(KEY_ID, stockAdjustmentDto.getId());
//                values.put("product_id", stockAdjustmentDto.getProductId());
//                values.put("quantity", stockAdjustmentDto.getQuantity());
//                SimpleDateFormat billDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
//                SimpleDateFormat billDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
//                if (stockAdjustmentDto.getPosAckDate() != null)
//                    values.put("dateOfAck", billDate.format(new Date(stockAdjustmentDto.getPosAckDate())));
//                if (stockAdjustmentDto.getPosAckStatus() == null || !stockAdjustmentDto.getPosAckStatus()) {
//                    values.put("isServerAdded", 0);
//                    values.put("isAdjusted", 0);
//                } else {
//                    values.put("isServerAdded", 1);
//                    values.put("isAdjusted", 1);
//                }
//                values.put("requestType", stockAdjustmentDto.getRequestType());
//                values.put("referenceNo", stockAdjustmentDto.getReferenceNumber());
//                values.put("godownReferenceNo", stockAdjustmentDto.getGodownStockOutwardReferenceNumber());
//                values.put("createdDate", stockAdjustmentDto.getCreatedDate());
//                database.insertWithOnConflict("fps_stock_adjustment", null, values, SQLiteDatabase.CONFLICT_IGNORE);
//            }
//            catch(Exception e) {
//                com.omneAgate.Util.Util.LoggingQueue(contextValue, "fps_stock_adjustment Exception...", e.toString());
//
//            }
//        }
//    } catch (Exception e) {
//        Log.e("Adjustment Error", "Stock Adjustment Error", e);
//    }
//
//}

    public void insertMigrations(Set<FPSMigrationDto> migration) {
        for (FPSMigrationDto migrate : migration) {
            if (migrate.getType().equalsIgnoreCase("IN")) {
                insertMigrateIn(migrate);
                insertBeneficiaryNewIn(migrate.getBeneficiaryDto());
            } else {
                insertMigrateOut(migrate);
            }
        }
    }

    //Insert into Person Rules
    public void insertPersonRules(Set<PersonBasedRule> masterRules) {
        try {
            for (PersonBasedRule personBasedRule : masterRules) {
                ContentValues values = new ContentValues();
                values.put(KEY_ID, personBasedRule.getId());
                if (personBasedRule.getIsDeleted() != null && personBasedRule.getIsDeleted())
                    values.put("isDeleted", 1);
                else {
                    values.put("isDeleted", 0);
                }
                values.put("groupId", personBasedRule.getGroupDto().getId());
                values.put(FPSDBConstants.KEY_RULES_PRODUCT_ID, personBasedRule.getProductDto().getId());
                if (personBasedRule.getCardTypeDto() != null)
                    values.put(FPSDBConstants.KEY_RULES_CARD_TYPE_ID, personBasedRule.getCardTypeDto().getId());
                NumberFormat formatter = new DecimalFormat("#0.000");
                values.put(FPSDBConstants.KEY_PERSON_MIN, formatter.format(personBasedRule.getMin()));
                values.put(FPSDBConstants.KEY_PERSON_MAX, formatter.format(personBasedRule.getMax()));
                values.put(FPSDBConstants.KEY_PERSON_CHILD, formatter.format(personBasedRule.getPerChild()));
                values.put(FPSDBConstants.KEY_PERSON_ADULT, formatter.format(personBasedRule.getPerAdult()));
                database.insertWithOnConflict(FPSDBTables.TABLE_PERSON_RULES, KEY_ID, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
        } catch (Exception e) {
            Log.e("Person Rules", e.toString(), e);
        }
    }

    // /Insert into Person Rules
    private void insertMigrateIn(FPSMigrationDto fpsMigrationDto) {
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_ID, fpsMigrationDto.getId());
            if (fpsMigrationDto.getIsMigrated() != null && fpsMigrationDto.getIsMigrated())
                values.put("isAdded", 1);
            else {
                values.put("isAdded", 0);
            }
            values.put("ration_card_number", fpsMigrationDto.getBeneficiaryDto().getOldRationNumber());
            values.put("a_register_no", fpsMigrationDto.getBeneficiaryDto().getAregisterNum());
            values.put("ufc_code", fpsMigrationDto.getBeneficiaryDto().getEncryptedUfc());
            values.put("beneficiary_id", fpsMigrationDto.getBeneficiaryDto().getId());
            values.put("month_in", fpsMigrationDto.getMonth());
            values.put("year_in", fpsMigrationDto.getYear());
            database.insertWithOnConflict(FPSDBTables.TABLE_FPS_MIGRATION_IN, KEY_ID, values, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (Exception e) {
            Log.e("Person Rules", e.toString(), e);
        }
    }

    // /Insert into Person Rules
    private void insertMigrateOut(FPSMigrationDto fpsMigrationDto) {
        ContentValues values = new ContentValues();
        try {
            values.put(KEY_ID, fpsMigrationDto.getId());
            if (fpsMigrationDto.getIsMigrated() != null && fpsMigrationDto.getIsMigrated())
                values.put("isAdded", 1);
            else {
                values.put("isAdded", 0);
            }
            values.put("beneficiary_id", fpsMigrationDto.getBeneficiaryId());
            values.put("month_out", fpsMigrationDto.getMonth());
            values.put("year_out", fpsMigrationDto.getYear());
        } catch (Exception e) {
            Log.e("MigrationOut", e.toString(), e);
        } finally {
            database.insertWithOnConflict(FPSDBTables.TABLE_FPS_MIGRATION_OUT, KEY_ID, values, SQLiteDatabase.CONFLICT_REPLACE);
        }
    }

    //Insert into Person Rules
    public void insertProductGroup(Set<GroupDto> groupProduct) {
        try {
            for (GroupDto productGroup : groupProduct) {
                ContentValues values = new ContentValues();
                values.put("group_id", productGroup.getId());
                if (productGroup.getDeleted() != null && productGroup.getDeleted())
                    values.put("is_deleted", 1);
                else {
                    values.put("is_deleted", 0);
                }
                values.put("name", productGroup.getGroupName());
                for (ProductDto products : productGroup.getProductDto()) {
                    values.put("product_id", products.getId());
                    database.insert(FPSDBTables.TABLE_PRODUCT_GROUP, null, values);
                }
            }
        } catch (Exception e) {
            Log.e("Person Rules", e.toString(), e);
        }
    }

    //Insert into Person Rules
    public void insertProductPriceOverride(Set<ProductPriceOverrideDto> groupProduct) {
        try {
            for (ProductPriceOverrideDto productGroup : groupProduct) {
                ContentValues values = new ContentValues();
                values.put(KEY_ID, productGroup.getId());
                if (productGroup.getIsDeleted() != null && productGroup.getIsDeleted())
                    values.put("is_deleted", 1);
                else {
                    values.put("is_deleted", 0);
                }
                values.put("percentage", productGroup.getPercentage());
                values.put("card_type_id", productGroup.getCardTypeId());
                values.put("card_type", productGroup.getCardType());
                values.put("product_id", productGroup.getProductId());
                database.insertWithOnConflict(FPSDBTables.TABLE_PRODUCT_PRICE_OVERRIDE, KEY_ID, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
        } catch (Exception e) {
            Log.e("Person Rules", e.toString(), e);
        }
    }

    //Insert into Person Rules
    public void insertRegionRules(Set<RegionBasedRule> masterRules) {
        try {
            for (RegionBasedRule regionBasedRule : masterRules) {
                ContentValues values = new ContentValues();
                values.put(KEY_ID, regionBasedRule.getId());
                if (regionBasedRule.getIsDeleted() != null && regionBasedRule.getIsDeleted())
                    values.put("isDeleted", 1);
                else {
                    values.put("isDeleted", 0);
                }
                values.put("groupId", regionBasedRule.getGroupDto().getId());
                values.put(FPSDBConstants.KEY_RULES_PRODUCT_ID, regionBasedRule.getProductDto().getId());
                NumberFormat formatter = new DecimalFormat("#0.000");
                values.put(FPSDBConstants.KEY_PERSON_QUANTITY, formatter.format(regionBasedRule.getQuantity()));
                values.put(FPSDBConstants.KEY_PERSON_CYLINDER, regionBasedRule.getCylinderCount());
                values.put(FPSDBConstants.KEY_PERSON_TALUK, returnInteger(regionBasedRule.isTaluk()));
                values.put(FPSDBConstants.KEY_PERSON_HILLAREA, returnInteger(regionBasedRule.isHillyArea()));
                values.put(FPSDBConstants.KEY_PERSON_SPLAREA, returnInteger(regionBasedRule.isSplArea()));
                values.put(FPSDBConstants.KEY_PERSON_TOWNPANCHAYAT, returnInteger(regionBasedRule.isTownPanchayat()));
                values.put(FPSDBConstants.KEY_PERSON_VILLAGEPANCHAYAT, returnInteger(regionBasedRule.isVillagePanchayat()));
                values.put(FPSDBConstants.KEY_PERSON_CITY, returnInteger(regionBasedRule.isCity()));
                values.put(FPSDBConstants.KEY_PERSON_MUNICIPALITY, returnInteger(regionBasedRule.isMunicipality()));
                values.put(FPSDBConstants.KEY_PERSON_HEAD, returnInteger(regionBasedRule.isCityHeadQuarter()));
                database.insertWithOnConflict(FPSDBTables.TABLE_REGION_RULES, KEY_ID, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
        } catch (Exception e) {
            Log.e("Region Rules", e.toString(), e);
        }
    }

    //Insert into Person Rules
    public void insertSmsProvider(Set<SmsProviderDto> smsProviderDtoSet) {
        try {
            if (smsProviderDtoSet == null) {
                return;
            }
            for (SmsProviderDto smsProviderDto : smsProviderDtoSet) {
                ContentValues values = new ContentValues();
                values.put(KEY_ID, smsProviderDto.getId());
                values.put(FPSDBConstants.KEY_SMS_PROVIDER_NAME, smsProviderDto.getProviderName());
                values.put(FPSDBConstants.KEY_SMS_PROVIDER_NUMBER, smsProviderDto.getIncomingNumber());
                int status = 0;
                if (smsProviderDto.isEnabledStatus()) {
                    status = 1;
                }
                values.put("status", status);
                values.put(FPSDBConstants.KEY_SMS_PROVIDER_PREFIX, smsProviderDto.getPrefixKey());
                values.put(FPSDBConstants.KEY_SMS_PROVIDER_PREFERENCE, smsProviderDto.getPreference());
                database.insertWithOnConflict(FPSDBTables.TABLE_SMS_PROVIDER, KEY_ID, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
        } catch (Exception e) {
            Log.e("Sms Provider", e.toString(), e);
        }
    }
    public void insert_giveitup(Set<GiveItUpRequestDto> setlist) {
        if (!setlist.isEmpty()) {
            try {
                for (GiveItUpRequestDto dto : setlist) {

                    ContentValues values = new ContentValues();
                    values.put(FPSDBConstants.KEY_GIVEITUP_KEY_ID, dto.getId());
                    values.put(FPSDBConstants.KEY_GIVEITUP_CHANNEl, dto.getChannel());
                    values.put(FPSDBConstants.KEY_GIVEITUP_CREATED_DATE, dto.getCreatedDate());
                    values.put(FPSDBConstants.KEY_GIVEITUP_END_DATE, dto.getEndDate());
                    values.put(FPSDBConstants.KEY_GIVEITUP_FOREVER, dto.isForever());
                    values.put(FPSDBConstants.KEY_GIVEITUP_MODIFIED_DATE, dto.getModifiedDate());
                    values.put(FPSDBConstants.KEY_GIVEITUP_REFERENCE_NO, dto.getReferenceNo());
                    values.put(FPSDBConstants.KEY_GIVEITUP_REVOKE_STATUS, dto.isRevokeStatus());
                    values.put(FPSDBConstants.KEY_GIVEITUP_START_DATE, dto.getStartDate());
                    values.put(FPSDBConstants.KEY_GIVEITUP_BENEFICIARY_ID, dto.getBeneficiaryDto().getId());
                    for (GiveItUpRequestDetailDto giveitdto : dto.getGiveItUpRequestDetailDtoList()) {
                        insert_giveitup_detail(giveitdto, dto.getId());
                    }
                    database.insertWithOnConflict(FPSDBTables.TABLE_GIVE_IT_UP_REQUEST, FPSDBConstants.KEY_GIVEITUP_KEY_ID, values, SQLiteDatabase.CONFLICT_REPLACE);
                }

            } catch (Exception e) {

            }
        }
    }

    public void insert_giveitup_detail(GiveItUpRequestDetailDto dto, Long id) {
        try {
            ContentValues values = new ContentValues();
            values.put(FPSDBConstants.KEY_GIVEITUP_REQUEST_DETAIL_ID, dto.getId());
            values.put(FPSDBConstants.KEY_GIVEITUP_REQUEST_ID, id);
            values.put(FPSDBConstants.KEY_GIVEITUP_GROUP_ID, dto.getGroupDto().getId());

            database.insertWithOnConflict(FPSDBTables.TABLE_GIVE_IT_UP_REQUEST_DETAIL, FPSDBConstants.KEY_GIVEITUP_REQUEST_DETAIL_ID, values, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (Exception e) {
            Log.e("Error Message", e.toString(), e);
        }

    }

    public List<GiveItUpRequestDto> check_has_giveitup(long beneficiary_id) {

        Calendar calendar = Calendar.getInstance();
        long current_date = calendar.getTimeInMillis();
        List<GiveItUpRequestDto> listOthersDto = new ArrayList<>();
        try {
            String selectQuery = "SELECT  * FROM " + FPSDBTables.TABLE_GIVE_IT_UP_REQUEST + " where " + FPSDBConstants.KEY_GIVEITUP_BENEFICIARY_ID + " = " + beneficiary_id + " and " + FPSDBConstants.KEY_GIVEITUP_START_DATE + " < " + current_date + " and (" + FPSDBConstants.KEY_GIVEITUP_END_DATE + " > " + current_date + " OR " + FPSDBConstants.KEY_GIVEITUP_FOREVER + " = 1) and " + FPSDBConstants.KEY_GIVEITUP_REVOKE_STATUS + " = 0";
            Log.e("DBHELPER","Give_it_up query "+selectQuery);

            Cursor cursor = database.rawQuery(selectQuery, null);
            Log.e("CursorSize", "" + cursor.getCount());
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                GiveItUpRequestDto othersDto = new GiveItUpRequestDto(cursor);
                listOthersDto.add(othersDto);

                try{
                    othersDto.setGiveItUpRequestDetailDtoList(getAllGiveItUpDetails(othersDto.getId()));
                }catch (Exception e){
                    e.printStackTrace();
                }
                cursor.moveToNext();
            }
            cursor.close();
        } catch (Exception e) {
            Log.e("Error", "check_has_give_it_up" + e.toString());
        }
        Log.e("GiveItUpRequestDetail ","Total size of the Give It Up list "+listOthersDto.size());
        return listOthersDto;
    }

    public List<GiveItUpRequestDetailDto> getAllGiveItUpDetails(long id){
        List<GiveItUpRequestDetailDto> giveItUpDetailsList =new ArrayList<>();
        try{
            String selectQuery = "SELECT  * FROM " + FPSDBTables.TABLE_GIVE_IT_UP_REQUEST_DETAIL + " where " + FPSDBConstants.KEY_GIVEITUP_REQUEST_ID + " = " +id;
            Cursor cursor = database.rawQuery(selectQuery, null);
            Log.e("CursorSize", "" + cursor.getCount());
            cursor.moveToFirst();
            for(int i=0; i<cursor.getCount();i++){
                GiveItUpRequestDetailDto giveItUpDetailDto =new GiveItUpRequestDetailDto();
                giveItUpDetailDto.setId(cursor.getLong(cursor.getColumnIndex(FPSDBConstants.KEY_GIVEITUP_REQUEST_DETAIL_ID)));
                GroupDto groupDto =new GroupDto();
                groupDto.setId(cursor.getLong(cursor.getColumnIndex(FPSDBConstants.KEY_GIVEITUP_GROUP_ID)));
                giveItUpDetailDto.setGroupDto(groupDto);
               /* GiveItUpRequestDto giveItUpRequestDto =new GiveItUpRequestDto(cursor);
                giveItUpDetailDto.setGiveItUpRequestDto(giveItUpRequestDto);*/
                giveItUpDetailsList.add(giveItUpDetailDto);

                cursor.moveToNext();

            }
            cursor.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        Log.e("GiveItUpRequestDetail ","Total size of the Details list "+giveItUpDetailsList.size());
        return giveItUpDetailsList;
    }
    //Insert into Master Rules
    public void insertMasterRules(Set<EntitlementMasterRule> masterRules) {
        try {
            for (EntitlementMasterRule entitlementMasterRule : masterRules) {
                ContentValues values = new ContentValues();
                values.put(KEY_ID, entitlementMasterRule.getId());
                if (entitlementMasterRule.getProductDto().getId().toString() != null)
                    values.put(FPSDBConstants.KEY_RULES_PRODUCT_ID, entitlementMasterRule.getProductDto().getId());
                values.put(FPSDBConstants.KEY_RULES_CARD_TYPE, entitlementMasterRule.getCardTypeDto().getId());
                if (entitlementMasterRule.getIsDeleted() != null && entitlementMasterRule.getIsDeleted())
                    values.put("isDeleted", 1);
                else {
                    values.put("isDeleted", 0);
                }
                values.put("groupId", entitlementMasterRule.getGroupDto().getId());
                NumberFormat formatter = new DecimalFormat("#0.000");
                values.put(FPSDBConstants.KEY_RULES_IS_CALC, returnInteger(entitlementMasterRule.isCalcRequired()));
                values.put(FPSDBConstants.KEY_SPECIAL_MINIMUM, returnInteger(entitlementMasterRule.isMinimumQty()));
                values.put(FPSDBConstants.KEY_SPECIAL_OVERRIDE, returnInteger(entitlementMasterRule.isOverridePrice()));
                values.put(FPSDBConstants.KEY_RULES_IS_PERSON, returnInteger(entitlementMasterRule.isPersonBased()));
                values.put(FPSDBConstants.KEY_RULES_IS_REGION, returnInteger(entitlementMasterRule.isRegionBased()));
                values.put(FPSDBConstants.KEY_RULES_HAS_SPECIAL, returnInteger(entitlementMasterRule.isHasSpecialRule()));
                values.put(FPSDBConstants.KEY_RULES_QUANTITY, formatter.format(entitlementMasterRule.getQuantity()));
                database.insertWithOnConflict(FPSDBTables.TABLE_ENTITLEMENT_RULES, KEY_ID, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
        } catch (Exception e) {
            Log.e("Master Rules", e.toString(), e);
        }
    }

    //Insert into Master Rules
    public void insertSpecialRules(Set<SplEntitlementRule> masterRules) {
        try {
            for (SplEntitlementRule splEntitlementRule : masterRules) {
                ContentValues values = new ContentValues();
                values.put(KEY_ID, splEntitlementRule.getId());
                if (splEntitlementRule.getIsDeleted() != null && splEntitlementRule.getIsDeleted())
                    values.put("isDeleted", 1);
                else {
                    values.put("isDeleted", 0);
                }
                if (splEntitlementRule.getGroupDto().getId() != null)
                    values.put("groupId", splEntitlementRule.getGroupDto().getId());
                values.put(FPSDBConstants.KEY_RULES_PRODUCT_ID, splEntitlementRule.getProductDto().getId());
                values.put(FPSDBConstants.KEY_SPECIAL_DISTRICT, splEntitlementRule.getDistrictId());
                values.put(FPSDBConstants.KEY_SPECIAL_TALUK, splEntitlementRule.getTalukId());
                values.put(FPSDBConstants.KEY_SPECIAL_VILLAGE, splEntitlementRule.getVillageId());
                NumberFormat formatter = new DecimalFormat("#0.000");
                values.put(FPSDBConstants.KEY_SPECIAL_IS_MUNICIPALITY, returnInteger(splEntitlementRule.isMunicipality()));
                values.put(FPSDBConstants.KEY_SPECIAL_IS_ADD, returnInteger(splEntitlementRule.isAdd()));
                values.put(FPSDBConstants.KEY_SPECIAL_IS_CITY_HEAD, returnInteger(splEntitlementRule.isCityHeadQuarter()));
                values.put(FPSDBConstants.KEY_SPECIAL_IS_CITY, returnInteger(splEntitlementRule.isCity()));
                values.put(FPSDBConstants.KEY_SPECIAL_IS_TALUK, returnInteger(splEntitlementRule.isTaluk()));
                values.put(FPSDBConstants.KEY_SPECIAL_IS_HILLAREA, returnInteger(splEntitlementRule.isHillyArea()));
                values.put(FPSDBConstants.KEY_SPECIAL_IS_SPLAREA, returnInteger(splEntitlementRule.isSplArea()));
                values.put(FPSDBConstants.KEY_SPECIAL_IS_TOWNPANCHAYAT, returnInteger(splEntitlementRule.isTownPanchayat()));
                values.put(FPSDBConstants.KEY_SPECIAL_IS_VILLAGE_PANCHAYAT, returnInteger(splEntitlementRule.isVillagePanchayat()));
                values.put(FPSDBConstants.KEY_SPECIAL_CYLINDER, splEntitlementRule.getCylinderCount());
                values.put(FPSDBConstants.KEY_RULES_CARD_TYPE_ID, splEntitlementRule.getCardTypeId());
                values.put(FPSDBConstants.KEY_SPECIAL_QUANTITY, formatter.format(splEntitlementRule.getQuantity()));
                database.insertWithOnConflict(FPSDBTables.TABLE_SPECIAL_RULES, KEY_ID, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
        } catch (Exception e) {
            Log.e("Special Rules", e.toString(), e);
        }
    }

    private int returnInteger(boolean value) {
        if (value)
            return 1;
        return 0;
    }

    public void deleteFromActiveFps() {
        try {
            String sqlquery = " Delete from " + FPSDBTables.TABLE_ACTIVE_FPS;
            database.execSQL(sqlquery);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertActiveFpsDetails(Set<FpsStoreDto> fpsStoresto) {
        try {
            Log.e("DBhelper", "Total number of Fps stores " + fpsStoresto.size());
            for (FpsStoreDto fpsStore : fpsStoresto) {
                ContentValues values = new ContentValues();
                values.put(FPSDBConstants.KEY_USERS_FPS_ID, fpsStore.getId());
                database.insertWithOnConflict(FPSDBTables.TABLE_ACTIVE_FPS, FPSDBConstants.KEY_USERS_FPS_ID, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("DBhelper", "Exception while inserting the fps store details" + e.toString());
        }
    }

    public void UpdateInactiveFPsStore() {
        try {
            //  String sqlquery = "update beneficiary SET active='0'where fps_id NOT IN (select fps_id from active_fps)";
            String sqlquery = "Delete from beneficiary where fps_id NOT IN (select fps_id from " + FPSDBTables.TABLE_ACTIVE_FPS + ")";
            database.execSQL(sqlquery);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("DBhelper", "Exception while Updating the Inactive fps store" + e.toString());
        }
    }

    //This function inserts details to TABLE_BILL,;
    public void insertBillData(Set<BillDto> billDto) {
        try {
            Log.e("Db helper ", "Bill data size " + billDto);
            for (BillDto bill : billDto) {
                ContentValues values = new ContentValues();
                values.put(FPSDBConstants.KEY_BILL_SERVER_ID, bill.getId());
                values.put(FPSDBConstants.KEY_BILL_SERVER_REF_ID, bill.getBillRefId());
                values.put(FPSDBConstants.KEY_BILL_FPS_ID, bill.getFpsId());
                values.put(FPSDBConstants.KEY_BENEFICIARY_UFC, bill.getUfc());
                values.put(FPSDBConstants.KEY_BILL_DATE, bill.getBillDate());
                values.put(FPSDBConstants.KEY_BILL_CREATED_BY, bill.getCreatedby());
                values.put(FPSDBConstants.KEY_BILL_TRANSACTION_ID, bill.getTransactionId());
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
                Date convertedDate = dateFormat.parse(bill.getBillDate());
                int month = 0;
                DateTime date = new DateTime();
                DateTime newDate = new DateTime(convertedDate);
                if (date.getYear() == newDate.getYear()) {
                    month = newDate.getMonthOfYear();
                    values.put(FPSDBConstants.KEY_BILL_TIME_MONTH, month);
                }
             /*   DateTime dateTime = new DateTime(convertedDate.getTime());
                int month = 0;
                if (new DateTime().getYear() == dateTime.getYear()) {
                    month=dateTime.getMonthOfYear();
                    values.put(FPSDBConstants.KEY_BILL_TIME_MONTH,month);
                }*/
                values.put(FPSDBConstants.KEY_BILL_AMOUNT, bill.getAmount());
                values.put(FPSDBConstants.KEY_BILL_MODE, String.valueOf(bill.getMode()));
                values.put(FPSDBConstants.KEY_BILL_CHANNEL, String.valueOf(bill.getChannel()));
                values.put(FPSDBConstants.KEY_BILL_BENEFICIARY, bill.getBeneficiaryId());
                values.put(FPSDBConstants.KEY_BILL_CREATED_DATE, bill.getCreatedDate());
                values.put(FPSDBConstants.KEY_BILL_STATUS, "T");
                database.insertWithOnConflict(FPSDBTables.TABLE_BILL, FPSDBConstants.KEY_BILL_TRANSACTION_ID, values, SQLiteDatabase.CONFLICT_REPLACE);
                insertBillItems(bill.getBillItemDto(), month, bill.getBeneficiaryId(), bill.getBillDate(), bill.getTransactionId());
            }
        } catch (Exception e) {
            Log.e("Bill Data", e.toString(), e);
        }
    }

    public boolean insertBill(BillDto bill) {
        try {
            ContentValues values = new ContentValues();
            values.put(FPSDBConstants.KEY_BILL_SERVER_ID, bill.getId());
            values.put(FPSDBConstants.KEY_BILL_FPS_ID, bill.getFpsId());
            values.put(FPSDBConstants.KEY_BILL_CREATED_BY, bill.getCreatedby());
            values.put(FPSDBConstants.KEY_BENEFICIARY_UFC, bill.getUfc());
            values.put(FPSDBConstants.KEY_BILL_AMOUNT, bill.getAmount());
            values.put(FPSDBConstants.KEY_BILL_MODE, String.valueOf(bill.getMode()));
            values.put(FPSDBConstants.KEY_BILL_CHANNEL, String.valueOf(bill.getChannel()));
            SimpleDateFormat billDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
            values.put(FPSDBConstants.KEY_BILL_DATE, billDate.format(new Date()));
            values.put(FPSDBConstants.KEY_BILL_CREATED_DATE, new Date().getTime());
            values.put(FPSDBConstants.KEY_BILL_TIME_MONTH, new DateTime().getMonthOfYear());
            values.put(FPSDBConstants.KEY_BILL_BENEFICIARY, bill.getBeneficiaryId());
            values.put(FPSDBConstants.KEY_BILL_TRANSACTION_ID, bill.getTransactionId());
            values.put(FPSDBConstants.KEY_BILL_STATUS, "R");
            values.put(FPSDBConstants.KEY_BENEFICIARY_UFC, bill.getUfc());
            values.put("otpTime", bill.getOtpTime());
            values.put("otpId", bill.getOtpId());
            if (!database.isOpen()) {
                database = dbHelper.getWritableDatabase();
            }
            database.insertWithOnConflict(FPSDBTables.TABLE_BILL, FPSDBConstants.KEY_BILL_TRANSACTION_ID, values, SQLiteDatabase.CONFLICT_REPLACE);
            insertBillItems(bill.getBillItemDto(), new DateTime().getMonthOfYear(), bill.getBeneficiaryId(), bill.getBillDate(), bill.getTransactionId());
            return true;
        } catch (Exception e) {
            Util.LoggingQueue(contextValue, "insertBill", e.toString());
            Log.e("insert Bill", e.toString(), e);
            return false;
        }
    }

    //This function inserts details to FPSDBTables.TABLE_BILL_ITEM,;
    private void insertBillItems(Set<BillItemProductDto> billItem, int month, long beneficiaryId, String billDate, String transactionId) {
        try {
            ContentValues values = new ContentValues();
            for (BillItemProductDto billItems : billItem) {
                Log.e("dbhelper", "bill item records : " + billItem.toString());
                values.put(FPSDBConstants.KEY_BILL_ITEM_PRODUCT_ID, billItems.getProductId());
                NumberFormat formatter = new DecimalFormat("#0.000");
                values.put(FPSDBConstants.KEY_BILL_ITEM_QUANTITY, billItems.getQuantity());
                values.put("totalCost", formatter.format(billItems.getCost() * billItems.getQuantity()));
                values.put(FPSDBConstants.KEY_BILL_ITEM_COST, formatter.format(billItems.getCost()));
                values.put(FPSDBConstants.KEY_BILL_TRANSACTION_ID, transactionId);
                values.put(FPSDBConstants.KEY_BILL_TIME_MONTH, month);
                values.put(FPSDBConstants.KEY_BILL_BENEFICIARY, beneficiaryId);
                values.put(FPSDBConstants.KEY_BILL_ITEM_DATE, billDate);
                database.insert(FPSDBTables.TABLE_BILL_ITEM, null, values);
            }
        } catch (Exception e) {
            Log.e("Dbhelper", "Exception while inserting the Bill items : " + e.toString());
        }
    }

    public void insertCloseSale(double amount, int totalBills) {
        try {
            SimpleDateFormat regDate = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss", Locale.getDefault());
            ContentValues values = new ContentValues();
            values.put("total_bills", totalBills);
            values.put("created_date", regDate.format(new Date()));
            values.put("amount", amount);
            database.insert(FPSDBTables.TABLE_CLOSE_SALE, null, values);
        } catch (Exception e) {
            Log.e("Close Sale", e.toString(), e);
        }
    }

    public void insertTableUpgrade(int android_version, String userLog, String status, String state, int androidNewVersion, String refId, String serverRefId) {
        ContentValues values = new ContentValues();
        try {
            values.put("android_old_version", android_version);
            values.put("ref_id", refId);
            values.put("android_new_version", androidNewVersion);
            values.put("description", userLog);
            values.put("status", status.toUpperCase());
            values.put("state", state);
            values.put("refer_id", serverRefId);
            SimpleDateFormat regDate = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss a", Locale.getDefault());
            values.put("created_date", regDate.format(new Date()));
            Log.e("updateinfo", "" + values);
            database.insert(FPSDBTables.TABLE_UPGRADE, null, values);
        } catch (Exception e) {
            Log.e("Table Upgrade", "Exception", e);
        }
    }

    public void insertTableUpgradeExec(int android_version, String userLog, String status, String state, int androidNewVersion, String refId, String serverRefId) {
        ContentValues values = new ContentValues();
        try {
            values.put("android_old_version", android_version);
            values.put("ref_id", refId);
            values.put("android_new_version", androidNewVersion);
            values.put("description", userLog);
            values.put("status", status.toUpperCase());
            values.put("state", state);
            SimpleDateFormat regDate = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss a", Locale.getDefault());
            values.put("created_date", "" + regDate.format(new Date()));
            values.put("server_status", 0);
            values.put("refer_id", serverRefId);
            database.insert(FPSDBTables.TABLE_UPGRADE, null, values);
        } catch (Exception e) {
            Log.e("Table Upgrade", e.toString(), e);
        }
    }

    //This function inserts details to TABLE_PRODUCTS;
    public void insertProductData(Set<ProductDto> productDto) {
        try {
            for (ProductDto products : productDto) {
                ContentValues values = new ContentValues();
                values.put(KEY_ID, products.getId());
                values.put(FPSDBConstants.KEY_PRODUCT_NAME, products.getName().toUpperCase());
                values.put(FPSDBConstants.KEY_LPRODUCT_NAME, products.getLocalProdName());
                if (products.isDeleted()) {
                    values.put("isDeleted", 1);
                } else {
                    values.put("isDeleted", 0);
                }
                values.put(FPSDBConstants.KEY_PRODUCT_CODE, products.getCode());
                values.put("groupId", products.getGroupId());
                values.put(FPSDBConstants.KEY_LPRODUCT_UNIT, products.getLocalProdUnit());
                values.put(FPSDBConstants.KEY_PRODUCT_UNIT, products.getProductUnit().toUpperCase());
                values.put(FPSDBConstants.KEY_PRODUCT_PRICE, products.getProductPrice());
                if (products.isNegativeIndicator())
                    values.put(FPSDBConstants.KEY_NEGATIVE_INDICATOR, 0);
                else {
                    values.put(FPSDBConstants.KEY_NEGATIVE_INDICATOR, 1);
                }
                values.put(FPSDBConstants.KEY_PRODUCT_MODIFIED_DATE, products.getModifiedDate());
                if (products.getModifiedby() != null)
                    values.put(FPSDBConstants.KEY_MODIFIED_BY, products.getModifiedby());
                values.put(FPSDBConstants.KEY_CREATED_DATE, products.getCreatedDate());
                if (products.getCreatedby() != null)
                    values.put(FPSDBConstants.KEY_CREATED_BY, products.getCreatedby());
                database.insertWithOnConflict(FPSDBTables.TABLE_PRODUCTS, KEY_ID, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //This function inserts details to FPSDBTables.TABLE_BENEFICIARY;
    public void insertBeneficiaryData(Set<BeneficiaryDto> beneficiaryDtos) {
        try {
            ContentValues values = new ContentValues();
            for (BeneficiaryDto beneficiary : beneficiaryDtos) {
                values.put(KEY_ID, beneficiary.getId());
                values.put(FPSDBConstants.KEY_BENEFICIARY_UFC, beneficiary.getEncryptedUfc());
                values.put(FPSDBConstants.KEY_BENEFICIARY_FPS_ID, beneficiary.getFpsId());
                if (StringUtils.isNotEmpty(beneficiary.getAregisterNum()))
                    values.put("aRegister", Integer.parseInt(beneficiary.getAregisterNum()));
                else {
                    values.put("aRegister", -1);
                }
                values.put(FPSDBConstants.KEY_BENEFICIARY_TIN, beneficiary.getTin());
                values.put(FPSDBConstants.KEY_BENEFICIARY_MOBILE, beneficiary.getMobileNumber());
                values.put(FPSDBConstants.KEY_BENEFICIARY_CREATED_DATE, beneficiary.getCreatedDate());
                values.put(FPSDBConstants.KEY_BENEFICIARY_MODIFIED_BY, beneficiary.getModifiedBy());
                values.put(FPSDBConstants.KEY_BENEFICIARY_MODIFIED_DATE, beneficiary.getModifiedDate());
                values.put(FPSDBConstants.KEY_BENEFICIARY_STATE_ID, beneficiary.getStateId());
                values.put(FPSDBConstants.KEY_BENEFICIARY_CARD_TYPE_ID, beneficiary.getCardTypeId());
                values.put(FPSDBConstants.KEY_BENEFICIARY_DISTRICT_ID, beneficiary.getDistrictId());
                values.put(FPSDBConstants.KEY_BENEFICIARY_TALUK_ID, beneficiary.getTalukId());
                values.put(FPSDBConstants.KEY_BENEFICIARY_VILLAGE_ID, beneficiary.getVillageId());
                String cardNumber = beneficiary.getOldRationNumber().replaceAll("[^a-zA-Z0-9]", "");
                values.put(FPSDBConstants.KEY_BENEFICIARY_OLD_RATION, cardNumber.toUpperCase());
                values.put(FPSDBConstants.KEY_BENEFICIARY_CYLINDER_NO, beneficiary.getNumOfCylinder());
                values.put(FPSDBConstants.KEY_BENEFICIARY_ADULT_NO, beneficiary.getNumOfAdults());
                values.put(FPSDBConstants.KEY_BENEFICIARY_CHILD_NO, beneficiary.getNumOfChild());
                int active = 0;
                if (beneficiary.isActive()) {
                    active = 1;
                }
                values.put(FPSDBConstants.KEY_BENEFICIARY_ACTIVE, active);
                setBeneficiaryMemberData(beneficiary);
                database.insertWithOnConflict(FPSDBTables.TABLE_BENEFICIARY, KEY_ID, values, SQLiteDatabase.CONFLICT_REPLACE);
                Util.LoggingQueue(contextValue, "FPS DB Helper", "Beneficiary insert success  for card number :" + beneficiary.getOldRationNumber());
            }
        } catch (Exception e) {
            Log.e("BeneficiaryData", e.toString(), e);
            Util.LoggingQueue(contextValue, "Ration Card Registration", "Beneficiary insert failed" + e.getMessage());
        }
    }

    /*--------------------------------------------------------------------------------------------*/
    //This function inserts details to FPSDBTables.TABLE_BENEFICIARY;
    public void insertBeneficiaryNew(BeneficiaryDto beneficiary) {
        try {
            ContentValues values = new ContentValues();
            Log.i("Beneficiary", beneficiary.toString());
            values.put(KEY_ID, beneficiary.getId());
            values.put(FPSDBConstants.KEY_BENEFICIARY_UFC, beneficiary.getEncryptedUfc());
            values.put(FPSDBConstants.KEY_BENEFICIARY_FPS_ID, beneficiary.getFpsId());
            if (StringUtils.isNotEmpty(beneficiary.getAregisterNum()))
                values.put("aRegister", Integer.parseInt(beneficiary.getAregisterNum()));
            else {
                values.put("aRegister", -1);
            }
            values.put(FPSDBConstants.KEY_BENEFICIARY_TIN, beneficiary.getTin());
            values.put(FPSDBConstants.KEY_BENEFICIARY_MOBILE, beneficiary.getMobileNumber());
            values.put(FPSDBConstants.KEY_BENEFICIARY_CREATED_DATE, beneficiary.getCreatedDate());
            values.put(FPSDBConstants.KEY_BENEFICIARY_MODIFIED_BY, beneficiary.getModifiedBy());
            values.put(FPSDBConstants.KEY_BENEFICIARY_MODIFIED_DATE, beneficiary.getModifiedDate());
            values.put(FPSDBConstants.KEY_BENEFICIARY_STATE_ID, beneficiary.getStateId());
            values.put(FPSDBConstants.KEY_BENEFICIARY_CARD_TYPE_ID, beneficiary.getCardTypeId());
            values.put(FPSDBConstants.KEY_BENEFICIARY_DISTRICT_ID, beneficiary.getDistrictId());
            values.put(FPSDBConstants.KEY_BENEFICIARY_TALUK_ID, beneficiary.getTalukId());
            values.put(FPSDBConstants.KEY_BENEFICIARY_VILLAGE_ID, beneficiary.getVillageId());
            if (beneficiary.getOldRationNumber() != null) {
                String cardNumber = beneficiary.getOldRationNumber().replaceAll("[^a-zA-Z0-9]", "");
                values.put(FPSDBConstants.KEY_BENEFICIARY_OLD_RATION, cardNumber.toUpperCase());
            }
            values.put(FPSDBConstants.KEY_BENEFICIARY_CYLINDER_NO, beneficiary.getNumOfCylinder());
            values.put(FPSDBConstants.KEY_BENEFICIARY_ADULT_NO, beneficiary.getNumOfAdults());
            values.put(FPSDBConstants.KEY_BENEFICIARY_CHILD_NO, beneficiary.getNumOfChild());
            int active = 0;
            if (beneficiary.isActive()) {
                active = 1;
            }
            values.put(FPSDBConstants.KEY_BENEFICIARY_ACTIVE, active);
            setBeneficiaryMemberData(beneficiary);
            database.insertWithOnConflict(FPSDBTables.TABLE_BENEFICIARY, KEY_ID, values, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (Exception e) {
            Log.e("BeneficiaryData", e.toString(), e);
        }
    } //This function inserts details to FPSDBTables.TABLE_BENEFICIARY;

    public void insertBeneficiaryNewIn(BeneficiaryDto beneficiary) {
        try {
            ContentValues values = new ContentValues();
            Log.i("Beneficiary", beneficiary.toString());
            values.put(KEY_ID, beneficiary.getId());
            values.put(FPSDBConstants.KEY_BENEFICIARY_UFC, beneficiary.getEncryptedUfc());
            values.put(FPSDBConstants.KEY_BENEFICIARY_FPS_ID, beneficiary.getFpsId());
            if (StringUtils.isNotEmpty(beneficiary.getAregisterNum()))
                values.put("aRegister", Integer.parseInt(beneficiary.getAregisterNum()));
            else {
                values.put("aRegister", -1);
            }
            values.put(FPSDBConstants.KEY_BENEFICIARY_TIN, beneficiary.getTin());
            values.put(FPSDBConstants.KEY_BENEFICIARY_MOBILE, beneficiary.getMobileNumber());
            values.put(FPSDBConstants.KEY_BENEFICIARY_CREATED_DATE, beneficiary.getCreatedDate());
            values.put(FPSDBConstants.KEY_BENEFICIARY_MODIFIED_BY, beneficiary.getModifiedBy());
            values.put(FPSDBConstants.KEY_BENEFICIARY_MODIFIED_DATE, beneficiary.getModifiedDate());
            values.put(FPSDBConstants.KEY_BENEFICIARY_STATE_ID, beneficiary.getStateId());
            values.put(FPSDBConstants.KEY_BENEFICIARY_CARD_TYPE_ID, beneficiary.getCardTypeId());
            values.put(FPSDBConstants.KEY_BENEFICIARY_DISTRICT_ID, beneficiary.getDistrictId());
            values.put(FPSDBConstants.KEY_BENEFICIARY_TALUK_ID, beneficiary.getTalukId());
            values.put(FPSDBConstants.KEY_BENEFICIARY_VILLAGE_ID, beneficiary.getVillageId());
            String cardNumber = beneficiary.getOldRationNumber().replaceAll("[^a-zA-Z0-9]", "");
            values.put(FPSDBConstants.KEY_BENEFICIARY_OLD_RATION, cardNumber.toUpperCase());
            values.put(FPSDBConstants.KEY_BENEFICIARY_CYLINDER_NO, beneficiary.getNumOfCylinder());
            values.put(FPSDBConstants.KEY_BENEFICIARY_ADULT_NO, beneficiary.getNumOfAdults());
            values.put(FPSDBConstants.KEY_BENEFICIARY_CHILD_NO, beneficiary.getNumOfChild());
            int active = 0;
            if (beneficiary.isActive()) {
                active = 1;
            }
            values.put(FPSDBConstants.KEY_BENEFICIARY_ACTIVE, active);
            setBeneficiaryMemberDataIn(beneficiary);
            database.insertWithOnConflict(FPSDBTables.TABLE_BENEFICIARY_IN, KEY_ID, values, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (Exception e) {
            Log.e("BeneficiaryData", e.toString(), e);
        }
    }

    public List<CloseOfProductDto> getCloseSale() {
        List<CloseOfProductDto> closeProduct = new ArrayList<>();
        String selectQuery = "SELECT round(sum(totalCost),2) as total,round(sum(quantity),3) as quantity,product_id FROM bill_item where date (createdDate) = date('now','localtime') group by product_id";
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        Log.i("Total Count", "number of Records" + cursor.getCount());
        for (int i = 0; i < cursor.getCount(); i++) {
            CloseOfProductDto closeSale = new CloseOfProductDto();
            closeSale.setTotalCost(cursor.getString(cursor.getColumnIndex("total")));
            closeSale.setTotalQuantity(cursor.getString(cursor.getColumnIndex("quantity")));
            closeSale.setProductId(cursor.getLong(cursor.getColumnIndex("product_id")));
            FPSStockHistoryDto fpsStockHistory = getAllProductStockHistoryDetails(cursor.getLong(cursor.getColumnIndex("product_id")));
            closeSale.setOpeningStock(fpsStockHistory.getCurrQuantity());
            getCloseStock(closeSale, String.valueOf(cursor.getLong(cursor.getColumnIndex("product_id"))));
            BillItemDto productInwardToday = getAllInwardListTodayTwo(cursor.getLong(cursor.getColumnIndex("product_id")));
            closeSale.setInward(productInwardToday.getQuantity());
            closeProduct.add(closeSale);
            cursor.moveToNext();
        }
        cursor.close();
        return closeProduct;
    }

    private void getCloseStock(CloseOfProductDto close, String productId) {
        try {
            Double closingBal = 0.0;
            String selectQuery = "select opening_balance, closing_balance from stock_history where product_id = '" + productId + "' order by _id desc limit 1";
            Cursor cursor = database.rawQuery(selectQuery, null);
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                closingBal = cursor.getDouble(cursor.getColumnIndex("closing_balance"));
                cursor.moveToNext();
            }
            cursor.close();
            close.setClosingStock(closingBal);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean updateCloseDate(CloseSaleTransactionDto closeSaleTransactionDto) {
        try {
            ContentValues values = new ContentValues();
            Log.i("Beneficiary", closeSaleTransactionDto.toString());
            values.put("isServerAdded", 0);
            database.update("close_sale", values, "transactionId=" + closeSaleTransactionDto.getTransactionId(), null);
            return true;
        } catch (Exception e) {
            Log.e("BeneficiaryData", e.toString(), e);
            return false;
        }
    }

    public BillItemDto getAllInwardListTodayTwo(long productId) {
        BillItemDto billItemDto = new BillItemDto();
        DateTime date = new DateTime();
        String selectQuery;
        int processFlag = getStatus(productId);
        if (processFlag == 0) {
            Double quantity, inwardQuantity, advanceQuantity = 0.0;
            // getting regular inward quantity
            selectQuery = "SELECT product_id,SUM(quantity) as total FROM stock_inward where  product_id = " + productId + " and date(fps_ack_date) = date('now', 'localtime')  and month = " + date.getMonthOfYear();
            Cursor cursor = database.rawQuery(selectQuery, null);
            cursor.moveToFirst();
            billItemDto.setProductId(productId);
            if (cursor.getCount() > 0) {
                inwardQuantity = cursor.getDouble(cursor.getColumnIndex("total"));
            } else {
                inwardQuantity = 0.0;
            }
            cursor.close();
            // getting advance stock processed quantity
            selectQuery = "SELECT product_id,SUM(quantity) as total FROM advance_stock_inward where  product_id = " + productId + " and date(fps_ack_date) = date('now', 'localtime') and isAdded = '0' and month = " + date.getMonthOfYear();
            Cursor cursor2 = database.rawQuery(selectQuery, null);
            cursor2.moveToFirst();
            billItemDto.setProductId(productId);
            if (cursor2.getCount() > 0) {
                advanceQuantity = cursor2.getDouble(cursor2.getColumnIndex("total"));
            } else {
                advanceQuantity = 0.0;
            }
            cursor2.close();
            quantity = inwardQuantity + advanceQuantity;
            billItemDto.setQuantity(quantity);
        } else {
            // getting regular inward quantity
            selectQuery = "SELECT product_id,SUM(quantity) as total FROM stock_inward where  product_id = " + productId + " and date(fps_ack_date) = date('now', 'localtime')  and month = " + date.getMonthOfYear();
            Cursor cursor = database.rawQuery(selectQuery, null);
            cursor.moveToFirst();
            billItemDto.setProductId(productId);
            if (cursor.getCount() > 0) {
                billItemDto.setQuantity(cursor.getDouble(cursor.getColumnIndex("total")));
            } else {
                billItemDto.setQuantity(0.0);
            }
            cursor.close();
        }
        return billItemDto;
    }

    public int getStatus(long productId) {
        int status = 1;
        try {
//            String selectQuery = "SELECT fps_ack_status as status FROM stock_inward where  product_id = " + productId + " and date(fps_ack_date_new) = date('now', 'localtime')";
            String selectQuery = "select isAdded as status FROM advance_stock_inward where product_id = " + productId + " and date(fps_ack_date) = date('now', 'localtime') order by rowid desc limit 1";
            Cursor cursor = database.rawQuery(selectQuery, null);
            cursor.moveToFirst();
            status = cursor.getInt(cursor.getColumnIndex("status"));
            cursor.close();
        } catch (Exception e) {
        }
        return status;
    }

    public void insertIntoCloseSale(CloseSaleTransactionDto closeSale) {
        try {
            ContentValues values = new ContentValues();
            values.put("dateOfTxn", closeSale.getDateOfTxn());
            values.put("numofTrans", closeSale.getNumofTrans());
            values.put("transactionId", closeSale.getTransactionId());
            values.put("totalSaleCost", closeSale.getTotalSaleCost());
            values.put("isServerAdded", closeSale.getIsServerAdded());
            database.insert("close_sale", null, values);
            insertCloseSaleProduct(closeSale);
        } catch (Exception e) {
            Log.e("Empty", e.toString());
        } finally {
        }
    }

    private void insertCloseSaleProduct(CloseSaleTransactionDto closeSale) {
//    public void insertCloseSaleProduct(CloseSaleTransactionDto closeSale) {
        Log.e("close sale product 1...", "");
        try {
            for (CloseOfProductDto product : closeSale.getCloseOfProductDtoList()) {
                Log.e("close sale product 2...", "");
                ContentValues values = new ContentValues();
                values.put("dateOfTxn", closeSale.getDateOfTxn());
                values.put("transactionId", closeSale.getTransactionId());
                values.put("totalCost", product.getTotalCost());
                values.put("totalQuantity", product.getTotalQuantity());
                values.put("productId", product.getProductId());
                values.put("opening_balance", product.getOpeningStock());
                values.put("closing_balance", product.getClosingStock());
                values.put("inward", product.getInward());
                database.insert("close_sale_product", null, values);
            }
        } catch (Exception e) {
            Log.e("Empty", e.toString());
        }
    }

    public boolean updateBeneficiary(MigrationOutDTO beneficiary) {
        try {
            ContentValues values = new ContentValues();
            Log.i("Beneficiary", beneficiary.toString());
            values.put(FPSDBConstants.KEY_BENEFICIARY_ACTIVE, 0);
            database.update(FPSDBTables.TABLE_BENEFICIARY, values, KEY_ID + "=" + beneficiary.getBeneficiaryId(), null);
            return true;
        } catch (Exception e) {
            Log.e("BeneficiaryData", e.toString(), e);
            return false;
        }
    }

  /*  public boolean updateBluetooth(String mac) {
        try {
            ContentValues values = new ContentValues();
            Log.i("Beneficiary", mac);
            values.put("values", mac);
            database.update("configuration", values, "name = 'printer'", null);
            return true;
        } catch (Exception e) {
            Log.e("BeneficiaryData", e.toString(), e);
            return false;

        }
    }*/

    public boolean updateMigrationOut(MigrationOutDTO beneficiary) {
        try {
            ContentValues values = new ContentValues();
            Log.i("Beneficiary", beneficiary.toString());
            values.put("isAdded", 1);
            database.update(FPSDBTables.TABLE_FPS_MIGRATION_OUT, values, KEY_ID + " =" + beneficiary.getId(), null);
            return true;
        } catch (Exception e) {
            Log.e("BeneficiaryData", e.toString(), e);
            return false;
        }
    }

    public boolean updateMigrationIn(MigrationOutDTO beneficiary) {
        try {
            ContentValues values = new ContentValues();
            Log.i("Beneficiary", beneficiary.toString());
            values.put("isAdded", 1);
            database.update(FPSDBTables.TABLE_FPS_MIGRATION_IN, values, KEY_ID + " =" + beneficiary.getId(), null);
            return true;
        } catch (Exception e) {
            Log.e("BeneficiaryData", e.toString(), e);
            return false;
        }
    }

    /*//This function inserts details to FPSDBTables.TABLE_BENEFICIARY;
    public void insertTempBeneficiaryData(List<EntitlementMasterRule> rules) {
        for (EntitlementMasterRule entitlementMasterRule : rules) {
            try {
                ContentValues values = new ContentValues();
                values.put(KEY_ID, entitlementMasterRule.getId());
                values.put(FPSDBConstants.KEY_RULES_PRODUCT_ID, entitlementMasterRule.getProductId());
                values.put(FPSDBConstants.KEY_RULES_CARD_TYPE, entitlementMasterRule.getCardTypeId());
                if (entitlementMasterRule.getIsDeleted() != null && entitlementMasterRule.getIsDeleted())
                    values.put("isDeleted", 1);
                else {
                    values.put("isDeleted", 0);
                }
                values.put("groupId", entitlementMasterRule.getGroupId());
                NumberFormat formatter = new DecimalFormat("#0.000");
                values.put(FPSDBConstants.KEY_RULES_IS_CALC, returnInteger(entitlementMasterRule.isCalcRequired()));
                values.put(FPSDBConstants.KEY_SPECIAL_MINIMUM, returnInteger(entitlementMasterRule.isMinimumQty()));
                values.put(FPSDBConstants.KEY_SPECIAL_OVERRIDE, returnInteger(entitlementMasterRule.isOverridePrice()));
                values.put(FPSDBConstants.KEY_RULES_IS_PERSON, returnInteger(entitlementMasterRule.isPersonBased()));
                values.put(FPSDBConstants.KEY_RULES_IS_REGION, returnInteger(entitlementMasterRule.isRegionBased()));
                values.put(FPSDBConstants.KEY_RULES_HAS_SPECIAL, returnInteger(entitlementMasterRule.isHasSpecialRule()));
                values.put(FPSDBConstants.KEY_RULES_QUANTITY, formatter.format(entitlementMasterRule.getQuantity()));
                database.insertWithOnConflict(FPSDBTables.TABLE_ENTITLEMENT_RULES, KEY_ID, values, SQLiteDatabase.CONFLICT_REPLACE);
            } catch (Exception e) {
                Log.e("Master Rules", e.toString(), e);
            }
        }

    }*/

   /* public void insertTempPersonData(List<PersonBasedRule> rules) {
        for (PersonBasedRule personBasedRule : rules) {
            try {
                ContentValues values = new ContentValues();
                values.put(KEY_ID, personBasedRule.getId());
                if (personBasedRule.getIsDeleted() != null && personBasedRule.getIsDeleted())
                    values.put("isDeleted", 1);
                else {
                    values.put("isDeleted", 0);
                }
                values.put("groupId", personBasedRule.getGroupId());
                values.put(FPSDBConstants.KEY_RULES_PRODUCT_ID, personBasedRule.getProductId());
                NumberFormat formatter = new DecimalFormat("#0.000");
                values.put(FPSDBConstants.KEY_PERSON_MIN, formatter.format(personBasedRule.getMin()));
                values.put(FPSDBConstants.KEY_PERSON_MAX, formatter.format(personBasedRule.getMax()));
                values.put(FPSDBConstants.KEY_PERSON_CHILD, formatter.format(personBasedRule.getPerChild()));
                values.put(FPSDBConstants.KEY_PERSON_ADULT, formatter.format(personBasedRule.getPerAdult()));
                database.insertWithOnConflict(FPSDBTables.TABLE_PERSON_RULES, KEY_ID, values, SQLiteDatabase.CONFLICT_REPLACE);
            } catch (Exception e) {
                Log.e("Master Rules", e.toString(), e);
            }
        }

    }
*/

    //This function inserts details to FPSDBTables.TABLE_BENEFICIARY_MEMBER;
    private void setBeneficiaryMemberData(BeneficiaryDto fpsDataDto) {
        ContentValues values = new ContentValues();
        try {
            if (fpsDataDto.getBenefMembersDto() != null) {
                List<BeneficiaryMemberDto> beneficiaryMemberList = new ArrayList<>(fpsDataDto.getBenefMembersDto());
                for (BeneficiaryMemberDto beneficiaryMember : beneficiaryMemberList) {
                    values.put(KEY_ID, beneficiaryMember.getId());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_UFC, fpsDataDto.getEncryptedUfc());
                    if (beneficiaryMember.getTin() != null) {
                        values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_TIN, beneficiaryMember.getTin());
                    }
                    if (beneficiaryMember.getUid() != null) {
                        values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_UID, beneficiaryMember.getUid());
                    }
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_EID, beneficiaryMember.getEid());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_LNAME, beneficiaryMember.getLocalName());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_FIRST_NAME, beneficiaryMember.getFirstName());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_MIDDLE_NAME, beneficiaryMember.getMiddleName());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_LAST_NAME, beneficiaryMember.getLastName());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_FATHER_NAME, beneficiaryMember.getFatherName());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_MOTHER_NAME, beneficiaryMember.getMotherName());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_GENDER, String.valueOf(beneficiaryMember.getGender()));//gender char
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_CREATED_BY, beneficiaryMember.getCreatedBy());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_CREATED_BY, beneficiaryMember.getCreatedBy());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_CREATED_DATE, beneficiaryMember.getCreatedDate());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_MODIFIED_BY, beneficiaryMember.getModifiedBy());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_MODIFIED_DATE, beneficiaryMember.getModifiedDate());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_NAME, beneficiaryMember.getName());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_REL_NAME, beneficiaryMember.getRelName());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_GENDER_ID, String.valueOf(beneficiaryMember.getGender())); //gender id
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_DOB, beneficiaryMember.getDob());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_STATUS_ID, String.valueOf(beneficiaryMember.getMstatusId()));
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_EDU_NAME, beneficiaryMember.getEduName());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_OCCUPATION_NAME, beneficiaryMember.getOccuName());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_FATHER_CODE, beneficiaryMember.getFatherCode());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_FATHER_NM, beneficiaryMember.getLocalFatherName());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_MOTHER_CODE, beneficiaryMember.getMotherCode());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_MOTHER_NM, beneficiaryMember.getLocalMotherName());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_SPOUSE_CODE, beneficiaryMember.getSpouseCode());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_SPOUSE_NM, beneficiaryMember.getSpouseName());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_NAT_NAME, beneficiaryMember.getNatname());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_ADDRESS_LINE_1, beneficiaryMember.getAddressLine1());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_ADDRESS_LINE_2, beneficiaryMember.getAddressLine2());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_ADDRESS_LINE_3, beneficiaryMember.getAddressLine3());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_ADDRESS_LINE_4, beneficiaryMember.getAddressLine4());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_ADDRESS_LINE_5, beneficiaryMember.getAddressLine5());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_LADDRESS_LINE_1, beneficiaryMember.getLocalAddressLine1());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_LADDRESS_LINE_2, beneficiaryMember.getLocalAddressLine2());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_LADDRESS_LINE_3, beneficiaryMember.getLocalAddressLine3());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_LADDRESS_LINE_4, beneficiaryMember.getLocalAddressLine4());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_LADDRESS_LINE_5, beneficiaryMember.getLocalAddressLine5());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_PIN_CODE, beneficiaryMember.getPincode());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_DURATION_IN_YEAR, beneficiaryMember.getDurationInYear());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_P_ADDRESS_LINE_1, beneficiaryMember.getPaddressLine1());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_P_ADDRESS_LINE_2, beneficiaryMember.getPaddressLine2());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_P_ADDRESS_LINE_3, beneficiaryMember.getPaddressLine3());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_P_ADDRESS_LINE_4, beneficiaryMember.getPaddressLine4());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_P_ADDRESS_LINE_5, beneficiaryMember.getPaddressLine5());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_LP_ADDRESS_LINE_1, beneficiaryMember.getLocalPaddressLine1());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_LP_ADDRESS_LINE_2, beneficiaryMember.getLocalPaddressLine2());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_LP_ADDRESS_LINE_3, beneficiaryMember.getLocalPaddressLine3());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_LP_ADDRESS_LINE_4, beneficiaryMember.getLocalPaddressLine4());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_LP_ADDRESS_LINE_5, beneficiaryMember.getLocalPaddressLine5());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_P_PIN_CODE, beneficiaryMember.getPpincode());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_DATE_DATA_ENTERED, beneficiaryMember.getDateDataEntered());
                    if (beneficiaryMember.isAliveStatus())
                        values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_ALIVE_STATUS, 0);
                    else {
                        values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_ALIVE_STATUS, 1);
                    }
                    if (beneficiaryMember.isAdult())
                        values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_IS_ADULT, 0);
                    else {
                        values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_IS_ADULT, 1);
                    }
                    database.insertWithOnConflict(FPSDBTables.TABLE_BENEFICIARY_MEMBER, KEY_ID, values, SQLiteDatabase.CONFLICT_REPLACE);
                }
            }
        } catch (Exception e) {
            Log.e("Bene_member", e.toString(), e);
        }
    }

    //This function inserts details to FPSDBTables.TABLE_BENEFICIARY_MEMBER;
    private void setBeneficiaryMemberDataIn(BeneficiaryDto fpsDataDto) {
        ContentValues values = new ContentValues();
        try {
            if (fpsDataDto.getBenefMembersDto() != null) {
                List<BeneficiaryMemberDto> beneficiaryMemberList = new ArrayList<>(fpsDataDto.getBenefMembersDto());
                for (BeneficiaryMemberDto beneficiaryMember : beneficiaryMemberList) {
                    values.put(KEY_ID, beneficiaryMember.getId());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_UFC, fpsDataDto.getEncryptedUfc());
                    if (beneficiaryMember.getTin() != null) {
                        values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_TIN, beneficiaryMember.getTin());
                    }
                    if (beneficiaryMember.getUid() != null) {
                        values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_UID, beneficiaryMember.getUid());
                    }
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_EID, beneficiaryMember.getEid());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_LNAME, beneficiaryMember.getLocalName());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_FIRST_NAME, beneficiaryMember.getFirstName());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_MIDDLE_NAME, beneficiaryMember.getMiddleName());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_LAST_NAME, beneficiaryMember.getLastName());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_FATHER_NAME, beneficiaryMember.getFatherName());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_MOTHER_NAME, beneficiaryMember.getMotherName());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_GENDER, String.valueOf(beneficiaryMember.getGender()));//gender char
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_CREATED_BY, beneficiaryMember.getCreatedBy());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_CREATED_BY, beneficiaryMember.getCreatedBy());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_CREATED_DATE, beneficiaryMember.getCreatedDate());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_MODIFIED_BY, beneficiaryMember.getModifiedBy());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_MODIFIED_DATE, beneficiaryMember.getModifiedDate());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_NAME, beneficiaryMember.getName());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_REL_NAME, beneficiaryMember.getRelName());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_GENDER_ID, String.valueOf(beneficiaryMember.getGender())); //gender id
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_DOB, beneficiaryMember.getDob());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_STATUS_ID, String.valueOf(beneficiaryMember.getMstatusId()));
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_EDU_NAME, beneficiaryMember.getEduName());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_OCCUPATION_NAME, beneficiaryMember.getOccuName());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_FATHER_CODE, beneficiaryMember.getFatherCode());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_FATHER_NM, beneficiaryMember.getLocalFatherName());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_MOTHER_CODE, beneficiaryMember.getMotherCode());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_MOTHER_NM, beneficiaryMember.getLocalMotherName());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_SPOUSE_CODE, beneficiaryMember.getSpouseCode());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_SPOUSE_NM, beneficiaryMember.getSpouseName());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_NAT_NAME, beneficiaryMember.getNatname());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_ADDRESS_LINE_1, beneficiaryMember.getAddressLine1());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_ADDRESS_LINE_2, beneficiaryMember.getAddressLine2());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_ADDRESS_LINE_3, beneficiaryMember.getAddressLine3());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_ADDRESS_LINE_4, beneficiaryMember.getAddressLine4());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_ADDRESS_LINE_5, beneficiaryMember.getAddressLine5());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_LADDRESS_LINE_1, beneficiaryMember.getLocalAddressLine1());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_LADDRESS_LINE_2, beneficiaryMember.getLocalAddressLine2());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_LADDRESS_LINE_3, beneficiaryMember.getLocalAddressLine3());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_LADDRESS_LINE_4, beneficiaryMember.getLocalAddressLine4());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_LADDRESS_LINE_5, beneficiaryMember.getLocalAddressLine5());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_PIN_CODE, beneficiaryMember.getPincode());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_DURATION_IN_YEAR, beneficiaryMember.getDurationInYear());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_P_ADDRESS_LINE_1, beneficiaryMember.getPaddressLine1());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_P_ADDRESS_LINE_2, beneficiaryMember.getPaddressLine2());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_P_ADDRESS_LINE_3, beneficiaryMember.getPaddressLine3());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_P_ADDRESS_LINE_4, beneficiaryMember.getPaddressLine4());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_P_ADDRESS_LINE_5, beneficiaryMember.getPaddressLine5());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_LP_ADDRESS_LINE_1, beneficiaryMember.getLocalPaddressLine1());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_LP_ADDRESS_LINE_2, beneficiaryMember.getLocalPaddressLine2());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_LP_ADDRESS_LINE_3, beneficiaryMember.getLocalPaddressLine3());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_LP_ADDRESS_LINE_4, beneficiaryMember.getLocalPaddressLine4());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_LP_ADDRESS_LINE_5, beneficiaryMember.getLocalPaddressLine5());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_P_PIN_CODE, beneficiaryMember.getPpincode());
                    values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_DATE_DATA_ENTERED, beneficiaryMember.getDateDataEntered());
                    if (beneficiaryMember.isAliveStatus())
                        values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_ALIVE_STATUS, 0);
                    else {
                        values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_ALIVE_STATUS, 1);
                    }
                    if (beneficiaryMember.isAdult())
                        values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_IS_ADULT, 0);
                    else {
                        values.put(FPSDBConstants.KEY_BENEFICIARY_MEMBER_IS_ADULT, 1);
                    }
                    database.insertWithOnConflict(FPSDBTables.TABLE_BENEFICIARY_MEMBER_IN, KEY_ID, values, SQLiteDatabase.CONFLICT_REPLACE);
                }
            }
        } catch (Exception e) {
            Log.e("Bene_member", e.toString(), e);
        }
    }

    public void updateRoles(long userId) {
        ContentValues values = new ContentValues();
        values.put("isDeleted", 0);
        database.update(FPSDBTables.TABLE_ROLE_FEATURE, values, "user_id=" + userId, null);
    }

    //SELECT count(*) as bill_count FROM bill ;
    public void setLastLoginTime(long userId) {
        try {
            ContentValues values = new ContentValues();
            if (SessionId.getInstance().getLoginTime() != null)
                values.put("last_login_time", SessionId.getInstance().getLoginTime().getTime() + "");
            else {
                values.put("last_login_time", new Date().getTime() + "");
            }
            if (!database.isOpen()) {
                database = dbHelper.getWritableDatabase();
            }
            database.update(FPSDBTables.TABLE_USERS, values, KEY_ID + " = " + userId, null);
        } catch (Exception e) {
            Log.e("Login Time Error", e.toString(), e);
        }
    }

    public void updateLoginHistory(String transactionId, String logoutType) {
        try {
            Log.e("updatelginhistroy", "loignHistroy :" + logoutType + "transactionId :" + transactionId);
            ContentValues values = new ContentValues();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
            values.put("logout_time", df.format(new Date()));
            values.put("logout_type", logoutType);
            values.put("is_logout_sync", 0);
//            if (logoutType.equalsIgnoreCase("ONLINE_LOGOUT") || logoutType.equalsIgnoreCase("CLOSE_SALE_LOGOUT_ONLINE")) {
//                values.put("is_logout_sync", 1);
//            } else {
//                values.put("is_logout_sync", 0);
//            }
            if (!database.isOpen()) {
                database = dbHelper.getWritableDatabase();
            }
            database.update(FPSDBTables.TABLE_LOGIN_HISTORY, values, "transaction_id='" + transactionId + "'", null);
        } catch (Exception e) {
            Log.e("Login History", e.toString(), e);
        }
    }

    public void updateLoginHistory(String transactionId) {
        try {
            ContentValues values = new ContentValues();
            values.put("is_logout_sync", 1);
            values.put("is_sync", 1);
            if (!database.isOpen()) {
                database = dbHelper.getWritableDatabase();
            }
            database.update(FPSDBTables.TABLE_LOGIN_HISTORY, values, "transaction_id='" + transactionId + "'", null);
        } catch (Exception e) {
            Log.e("Login History", e.toString(), e);
        }
    }

    //Update Stock Inward
    public void updateStockInward(String referenceNo) {
        ContentValues values = new ContentValues();
        Log.e("Srock Req Dto", referenceNo);
        values.put(FPSDBConstants.KEY_FPS_STOCK_INWARD_IS_FPSACKSTATUS, 1);
        values.put("is_server_add", 0);
        database.update(FPSDBTables.TABLE_FPS_STOCK_INWARD, values, "referenceNo = '" + referenceNo + "'", null);
    }

    //Insert into Stock history
    public void updateRegistration(String cardNumber, String status, String description) {
        ContentValues values = new ContentValues();
        try {
            values.put(FPSDBConstants.KEY_REGISTRATION_STATUS, status);
            values.put(FPSDBConstants.KEY_REGISTRATION_DESC, description);
            values.put(FPSDBConstants.KEY_REGISTRATION_IS_ACTIVATED, 0);
            database.update(FPSDBTables.TABLE_REGISTRATION, values, FPSDBConstants.KEY_REGISTRATION_CARD_REF_NO + "='" + cardNumber.toUpperCase() + "'", null);
        } catch (Exception e) {
            Log.e("update registration", e.toString(), e);
        }
    }

    //Update the stock
    public void stockUpdate(List<FPSStockDto> stock) {
        for (FPSStockDto fpsStockDto : stock) {
            ContentValues values = new ContentValues();
            NumberFormat formatter = new DecimalFormat("#0.000");
            values.put(FPSDBConstants.KEY_STOCK_QUANTITY, formatter.format(fpsStockDto.getQuantity()));
            database.update(FPSDBTables.TABLE_STOCK, values, FPSDBConstants.KEY_STOCK_PRODUCT_ID + "=" + fpsStockDto.getProductId(), null);
        }
    }

    //Update the bill
    public void billUpdate(BillDto bill) {
        ContentValues values = new ContentValues();
        values.put(FPSDBConstants.KEY_BILL_SERVER_ID, bill.getId());
        values.put(FPSDBConstants.KEY_BILL_SERVER_REF_ID, bill.getBillRefId());
        values.put(FPSDBConstants.KEY_BILL_CHANNEL, String.valueOf(bill.getChannel()));
        values.put(FPSDBConstants.KEY_BILL_STATUS, "T");
        database.update(FPSDBTables.TABLE_BILL, values, FPSDBConstants.KEY_BILL_TRANSACTION_ID + "='" + bill.getTransactionId() + "'", null);
    }

    //Update the bill
    public void updateCardRegistration(String billNo) {
        ContentValues values = new ContentValues();
        values.put("active", 1);
        database.update("offline_activation", values, "old_ration_card_num='" + billNo + "'", null);
    }

/*

    //Get Beneficiary data by QR Code
    public BeneficiaryDto beneficiaryUnregistered(String qrCode) {
        String selectQuery = "SELECT  * FROM " + FPSDBTables.TABLE_OFFLINE_ACTIVATION + " where " + FPSDBConstants.KEY_BENEFICIARY_OLD_RATION + "='" + qrCode.toUpperCase() + "'";
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        if (cursor.getCount() == 0) {
            cursor.close();
            return null;
        } else {
            BeneficiaryDto beneficiary = new BeneficiaryDto();
            beneficiary.setId(cursor.getLong(cursor.getColumnIndex(KEY_ID)));
            beneficiary.setAregisterNum(cursor.getString(cursor.getColumnIndex("aRegister")));
            beneficiary.setOldRationNumber(cursor.getString(cursor.getColumnIndex(FPSDBConstants.KEY_BENEFICIARY_OLD_RATION)));
            beneficiary.setNumOfChild(cursor.getInt(cursor.getColumnIndex(FPSDBConstants.KEY_BENEFICIARY_CHILD_NO)));
            beneficiary.setNumOfAdults(cursor.getInt(cursor.getColumnIndex(FPSDBConstants.KEY_BENEFICIARY_ADULT_NO)));
            beneficiary.setNumOfCylinder(cursor.getInt(cursor.getColumnIndex(FPSDBConstants.KEY_BENEFICIARY_CYLINDER_NO)));
            beneficiary.setMobileNumber(cursor.getString(cursor.getColumnIndex(FPSDBConstants.KEY_BENEFICIARY_MOBILE)));
            beneficiary.setCardTypeId(cursor.getString(cursor.getColumnIndex(FPSDBConstants.KEY_BENEFICIARY_CARD_TYPE_ID)));
            cursor.close();
            return beneficiary;
        }
    }
*/

    public boolean checkUpgradeFinished() {
        try {
            String selectQuery = "select case when state = 'EXECUTION' and server_status = 0 then 1 else 0 end as status from table_upgrade order by _id desc limit 1";
            Cursor cursor = database.rawQuery(selectQuery, null);
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                int state = cursor.getInt(cursor.getColumnIndex("status"));
                cursor.close();
                if (state == 1) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public void updateUpgradeExec() {
        ContentValues values = new ContentValues();
        try {
            SimpleDateFormat regDate = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss a", Locale.getDefault());
            values.put("execution_time", regDate.format(new Date()));
            values.put("server_status", 1);
            database.update(FPSDBTables.TABLE_UPGRADE, values, " _id in (select max(_id) from table_upgrade)", null);
        } catch (Exception e) {
            Log.e("DB ERROR", e.toString(), e);
        }
    }

    public Cursor getCurerntVersonExec() {
        String selectQuery = "select * from table_upgrade order by _id desc limit 1";
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        return cursor;
    }

    //Bill for background sync
    public List<StockRequestDto> getAllStockSync() {
        List<StockRequestDto> stockRequestDtos = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + FPSDBTables.TABLE_FPS_STOCK_INWARD + " where is_server_add = 1 group by referenceNo";
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            StockRequestDto manualStocks = new StockRequestDto();
            manualStocks.setType(StockTransactionType.INWARD);
            manualStocks.setFpsId(cursor.getLong(cursor.getColumnIndex(FPSDBConstants.KEY_FPS_STOCK_INWARD_FPSID)));
            manualStocks.setBatchNo(cursor.getString(cursor.getColumnIndex(FPSDBConstants.KEY_FPS_STOCK_INWARD_BATCH_NO)) + "");
            manualStocks.setUnit(cursor.getString(cursor.getColumnIndex(FPSDBConstants.KEY_FPS_STOCK_INWARD_UNIT)) + "");
            String refNo = cursor.getString(cursor.getColumnIndex("referenceNo")) + "";
            manualStocks.setReferenceNo(refNo);
            manualStocks.setGodownId(cursor.getLong(cursor.getColumnIndex(FPSDBConstants.KEY_FPS_STOCK_INWARD_GODOWNID)));
            manualStocks.setDeliveryChallanId(cursor.getString(cursor.getColumnIndex(FPSDBConstants.KEY_FPS_STOCK_INWARD_DELIEVERY_CHELLANID)) + "");
            String value = cursor.getString(cursor.getColumnIndex(FPSDBConstants.KEY_FPS_STOCK_INWARD_FPSACKDATE));
            try {
                Long fpsAckDate = 0l;
                if (value != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    Date date = sdf.parse(value);
                    fpsAckDate = date.getTime();
                }
                manualStocks.setDate(fpsAckDate);
            } catch (Exception e) {
                Log.e("Error", e.toString(), e);
            }
            manualStocks.setCreatedBy(cursor.getLong(cursor.getColumnIndex(FPSDBConstants.KEY_FPS_STOCK_INWARD_CREATEDBY)) + "");
            manualStocks.setProductLists(getProduct(refNo));
            stockRequestDtos.add(manualStocks);
            cursor.moveToNext();
        }
        cursor.close();
        return stockRequestDtos;
    }

    //Bill for background sync
    public StockRequestDto getAllStockSync(String referenceNumber) {
        String selectQuery = "SELECT  * FROM " + FPSDBTables.TABLE_FPS_STOCK_INWARD + " where is_server_add = 1 and referenceNo='" + referenceNumber + "' group by referenceNo";
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            StockRequestDto manualStocks = new StockRequestDto();
            manualStocks.setDeviceId(Settings.Secure.getString(
                    contextValue.getContentResolver(), Settings.Secure.ANDROID_ID).toUpperCase());
            manualStocks.setType(StockTransactionType.INWARD);
            manualStocks.setFpsId(cursor.getLong(cursor.getColumnIndex(FPSDBConstants.KEY_FPS_STOCK_INWARD_FPSID)));
            manualStocks.setBatchNo(cursor.getString(cursor.getColumnIndex(FPSDBConstants.KEY_FPS_STOCK_INWARD_BATCH_NO)) + "");
            manualStocks.setUnit(cursor.getString(cursor.getColumnIndex(FPSDBConstants.KEY_FPS_STOCK_INWARD_UNIT)) + "");
            manualStocks.setReferenceNo(referenceNumber);
            manualStocks.setGodownId(cursor.getLong(cursor.getColumnIndex(FPSDBConstants.KEY_FPS_STOCK_INWARD_GODOWNID)));
            manualStocks.setDeliveryChallanId(cursor.getString(cursor.getColumnIndex(FPSDBConstants.KEY_FPS_STOCK_INWARD_DELIEVERY_CHELLANID)) + "");
            String value = cursor.getString(cursor.getColumnIndex(FPSDBConstants.KEY_FPS_STOCK_INWARD_FPSACKDATE));
            try {
                Long fpsAckDate = 0l;
                if (value != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    Date date = sdf.parse(value);
                    fpsAckDate = date.getTime();
                }
                manualStocks.setDate(fpsAckDate);
            } catch (Exception e) {
                Log.e("Error", e.toString(), e);
            }
            manualStocks.setCreatedBy(cursor.getLong(cursor.getColumnIndex(FPSDBConstants.KEY_FPS_STOCK_INWARD_CREATEDBY)) + "");
            manualStocks.setProductLists(getProduct(referenceNumber));
            cursor.close();
            return manualStocks;
        } else {
            return null;
        }
    }

    private List<StockRequestDto.ProductList> getProduct(String referenceNo) {
        String selectQuery = "SELECT * FROM stock_inward where referenceNo ='" + referenceNo + "'";
        List<StockRequestDto.ProductList> productList = new ArrayList<>();
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            StockRequestDto.ProductList manualProduct = new StockRequestDto.ProductList();
            manualProduct.setServerId(cursor.getLong(cursor.getColumnIndex("_id")));
            manualProduct.setId(cursor.getLong(cursor.getColumnIndex("product_id")));
            manualProduct.setQuantity(cursor.getDouble(cursor.getColumnIndex("quantity")));
            manualProduct.setRecvQuantity(cursor.getDouble(cursor.getColumnIndex("quantity")));
            productList.add(manualProduct);
            cursor.moveToNext();
        }
        cursor.close();
        return productList;
    }

    //Bill from local db
    public List<BillItemDto> getAllBillItems(long beneId, int month) {
        List<BillItemDto> billItems = new ArrayList<>();
        String selectQuery = "SELECT " + FPSDBConstants.KEY_BILL_ITEM_PRODUCT_ID + ",SUM(quantity) as total FROM " + FPSDBTables.TABLE_BILL_ITEM
                + " where " + FPSDBConstants.KEY_BILL_BENEFICIARY + " = " + beneId + " AND " + FPSDBConstants.KEY_BILL_TIME_MONTH + " = "
                + month + " group by " + FPSDBConstants.KEY_BILL_ITEM_PRODUCT_ID;
        Log.e("selected query ", "entitlment" + selectQuery);
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            BillItemDto billItemDto = new BillItemDto();
            billItemDto.setProductId(cursor.getLong(cursor.getColumnIndex(FPSDBConstants.KEY_BILL_ITEM_PRODUCT_ID)));
            billItemDto.setQuantity(cursor.getDouble(cursor.getColumnIndex("total")));
            billItems.add(billItemDto);
            cursor.moveToNext();
        }
        cursor.close();
        return billItems;
    }

    public List<CloseSaleTransactionDto> getAllCloseSaleForSync() {
        List<CloseSaleTransactionDto> bills = new ArrayList<>();
        String selectQuery = "SELECT  * FROM close_sale where isServerAdded<>0";
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            CloseSaleTransactionDto bill = new CloseSaleTransactionDto(cursor);
            bill.setCloseOfProductDtoList(getCloseSaleItems(bill.getTransactionId()));
            bills.add(bill);
            Log.i("bills", bill.toString());
            cursor.moveToNext();
        }
        cursor.close();
        return bills;
    }

    public Set<CloseOfProductDto> getCloseSaleItems(long referenceId) {
        List<CloseOfProductDto> billItems = new ArrayList<>();
        String selectQuery = "SELECT * FROM close_sale_product where transactionId=" + referenceId;
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            CloseOfProductDto billItemDto = new CloseOfProductDto(cursor);
            billItems.add(billItemDto);
            cursor.moveToNext();
        }
        cursor.close();
        return new HashSet<>(billItems);
    }

/*
    //Bill from local db
    public List<BillItemDto> getAllBillItemsOffLine(String cardNumber, int month) {
        List<BillItemDto> billItems = new ArrayList<>();
        String selectQuery = "SELECT " + FPSDBConstants.KEY_BILL_ITEM_PRODUCT_ID + ",SUM(quantity) as total FROM " + FPSDBTables.TABLE_BILL_OFFLINE_ITEM
                + " where old_ration_card_num = '" + cardNumber + "' AND " + FPSDBConstants.KEY_BILL_TIME_MONTH + " = "
                + month + " group by " + FPSDBConstants.KEY_BILL_ITEM_PRODUCT_ID;
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            BillItemDto billItemDto = new BillItemDto();
            billItemDto.setProductId(cursor.getLong(cursor.getColumnIndex(FPSDBConstants.KEY_BILL_ITEM_PRODUCT_ID)));
            billItemDto.setQuantity(cursor.getDouble(cursor.getColumnIndex("total")));
            billItems.add(billItemDto);
            cursor.moveToNext();
        }
        cursor.close();
        return billItems;
    }*/

    //Bill from local db
    public List<BillItemProductDto> getAllBillItems(String transactionId) {
        List<BillItemProductDto> billItems = new ArrayList<>();
        String selectQuery = "SELECT  product_id,transaction_id,name,quantity,cost,unit,local_unit,local_name FROM bill_item a inner join products b on a.product_id = b._id where " + FPSDBConstants.KEY_BILL_TRANSACTION_ID + " = '" + transactionId + "'  order by groupId,b._id";
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            BillItemProductDto billItemDto = new BillItemProductDto(cursor);
            billItems.add(billItemDto);
            cursor.moveToNext();
        }
        cursor.close();
        return billItems;
    }

    //Bill from local db
    public List<BillDto> getAllBillsUser(long id) {
        List<BillDto> bills = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + FPSDBTables.TABLE_BILL + " where " + FPSDBConstants.KEY_BILL_BENEFICIARY + " = " + id
                + " order by date desc";
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            BillDto billDto = new BillDto(cursor);
            bills.add(billDto);
            cursor.moveToNext();
        }
        cursor.close();
        return bills;
    }

    //Bill from local db
    public List<BillDto> getAllBillByDate(String dateToday, int position) {
        List<BillDto> bills = new ArrayList<>();
        long offSet = position * 50;
        String selectQuery = "SELECT ref_id,aRegister,server_bill_id,date,amount,transaction_id,bill_status,old_ration_card_num FROM bill  a inner join beneficiary b on a.beneficiary_id = b._id where date like '" + dateToday + "%' "
                + " order by date desc limit 50  OFFSET " + offSet;
        Log.i("selectQuery", selectQuery);
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            BillDto billDto = new BillDto();
            billDto.setBillRefId(cursor.getLong(cursor.getColumnIndex("ref_id")));
            billDto.setARegisterNo(cursor.getString(cursor.getColumnIndex("aRegister")));
            billDto.setBillStatus(cursor.getString(cursor.getColumnIndex("bill_status")));
            billDto.setBillDate(cursor.getString(cursor.getColumnIndex("date")));
            billDto.setAmount(cursor.getDouble(cursor.getColumnIndex("amount")));
            billDto.setTransactionId(cursor.getString(cursor.getColumnIndex("transaction_id")));
            billDto.setRationCardNumber(cursor.getString(cursor.getColumnIndex("old_ration_card_num")));
            bills.add(billDto);
            cursor.moveToNext();
        }
        cursor.close();
        return bills;
    }

    public long getAllUnsyncBills() {
        long count;
        String selectQuery = "SELECT count(*) as count FROM bill where bill_status = 'R'";
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        count = cursor.getInt(cursor.getColumnIndex("count"));
        cursor.close();
        return count;
    }

    //Bill from local db
    public List<BillDto> getAllBillById(long beneId, int position) {
        List<BillDto> bills = new ArrayList<>();
        long offSet = position * 50;
        String selectQuery = "SELECT ref_id,server_bill_id,date,amount,transaction_id,old_ration_card_num,aRegister,bill_status FROM bill a inner join beneficiary b on a.beneficiary_id = b._id where beneficiary_id ="
                + beneId + " order by date desc limit 50 OFFSET " + offSet;
        Log.i("selectQuery", selectQuery);
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            BillDto billDto = new BillDto();
            billDto.setBillRefId(cursor.getLong(cursor.getColumnIndex("ref_id")));
            billDto.setBillDate(cursor.getString(cursor.getColumnIndex("date")));
            billDto.setAmount(cursor.getDouble(cursor.getColumnIndex("amount")));
            billDto.setTransactionId(cursor.getString(cursor.getColumnIndex("transaction_id")));
            billDto.setRationCardNumber(cursor.getString(cursor.getColumnIndex("old_ration_card_num")));
            billDto.setARegisterNo(cursor.getString(cursor.getColumnIndex("aRegister")));
            billDto.setBillStatus(cursor.getString(cursor.getColumnIndex("bill_status")));
            bills.add(billDto);
            cursor.moveToNext();
        }
        cursor.close();
        return bills;
    }

    //Bill from local db
    public List<BillDto> getAllBillByUnsync(int position) {
        List<BillDto> bills = new ArrayList<>();
        long offSet = position * 50;
        String selectQuery = "SELECT ref_id,server_bill_id,date,amount,transaction_id,old_ration_card_num,aRegister,bill_status FROM bill a inner join beneficiary b " +
                "on a.beneficiary_id = b._id where bill_status <>'T' order by date desc limit 50 OFFSET " + offSet;
        Log.i("selectQuery", selectQuery);
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            BillDto billDto = new BillDto();
            billDto.setBillRefId(cursor.getLong(cursor.getColumnIndex("ref_id")));
            billDto.setBillDate(cursor.getString(cursor.getColumnIndex("date")));
            billDto.setAmount(cursor.getDouble(cursor.getColumnIndex("amount")));
            billDto.setTransactionId(cursor.getString(cursor.getColumnIndex("transaction_id")));
            billDto.setRationCardNumber(cursor.getString(cursor.getColumnIndex("old_ration_card_num")));
            billDto.setARegisterNo(cursor.getString(cursor.getColumnIndex("aRegister")));
            billDto.setBillStatus(cursor.getString(cursor.getColumnIndex("bill_status")));
            bills.add(billDto);
            cursor.moveToNext();
        }
        cursor.close();
        return bills;
    }

    public Set<BillItemProductDto> getBillItems(String referenceId) {
        List<BillItemProductDto> billItems = new ArrayList<>();
        String selectQuery = "SELECT  product_id,transaction_id,name,quantity,cost,unit,local_unit,local_name FROM bill_item a inner join products b on a.product_id = b._id where " + FPSDBConstants.KEY_BILL_TRANSACTION_ID + "='" + referenceId + "'";
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            BillItemProductDto billItemDto = new BillItemProductDto(cursor);
            billItems.add(billItemDto);
            cursor.moveToNext();
        }
        cursor.close();
        return new HashSet<>(billItems);
    }

    //Bill from local db
    public BillUserDto getBill(long billId) {
        BillUserDto billDto;
        String selectQuery = "SELECT a.ref_id,a.server_bill_id,a.fps_id,a.date,a.transaction_id,a.server_ref_id,a.mode,b.ufc_code,a.channel,a.bill_status," +
                "a.beneficiary_id,a.amount,a.created_by,a.otpId,a.otpTime,a.created_date,b.old_ration_card_num,b.aRegister  from bill a inner join beneficiary b on a.beneficiary_id = b._id where " + FPSDBConstants.KEY_BILL_REF_ID + "=" + billId;
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        billDto = new BillUserDto(cursor);
        billDto.setBillItemDto(getBillItems(billDto.getTransactionId()));
        cursor.close();
        return billDto;
    }

    //Bill from local db
    public BillDto getBillByTransactionId(String billId) {
        BillDto billDto;
        String selectQuery = "SELECT a.ref_id,a.month,a.server_bill_id,a.fps_id,a.date,a.transaction_id,a.server_ref_id,a.mode,b.ufc_code,a.channel,a.bill_status," +
                "a.beneficiary_id,a.amount,a.created_by,a.otpId,a.otpTime,a.created_date,b.old_ration_card_num,b.aRegister from bill a inner join beneficiary b on a.beneficiary_id = b._id where transaction_id = '" + billId + "'";
        Log.e("selectQuery", selectQuery);
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        billDto = new BillDto(cursor);
        billDto.setBillItemDto(getBillItems(billDto.getTransactionId()));
        cursor.close();
        return billDto;
    }

    //BeneCount from local db
    public int getBeneficiaryCount() {
        String selectQuery = "SELECT  count(*) as count FROM " + FPSDBTables.TABLE_BENEFICIARY;
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        int count = cursor.getInt(cursor.getColumnIndex("count"));
        cursor.close();
        return count;
    }

    //BeneCount from local db
    public int getBeneficiaryUnSyncCount() {
        String selectQuery = "Select count(*) as count from offline_activation where old_ration_card_num not in (Select old_ration_card_num from beneficiary)";
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        int count = cursor.getInt(cursor.getColumnIndex("count"));
        cursor.close();
        return count;
    }

    //BeneCount from local db
    public int getBillUnSyncCount() {
        String selectQuery = "Select count(*) as count from bill where bill_status<>'T'";
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        int count = cursor.getInt(cursor.getColumnIndex("count"));
        cursor.close();
        return count;
    }

    public Set<BeneficiaryMemberDto> getAllBeneficiaryMembers(String qrCode) {
        List<BeneficiaryMemberDto> beneficiaryMembers = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + FPSDBTables.TABLE_BENEFICIARY_MEMBER + " where " + FPSDBConstants.KEY_BENEFICIARY_UFC + "='"
                + qrCode + "' AND " + FPSDBConstants.KEY_BENEFICIARY_MEMBER_ALIVE_STATUS + "= 0 order by tin";
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            beneficiaryMembers.add(new BeneficiaryMemberDto(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return new HashSet<>(beneficiaryMembers);
    }

    // Used to BeneficiaryDto beneficiary details
    public BeneficiaryDto retrieveBeneficiary(long id) {
        String selectQuery = "SELECT  * FROM " + FPSDBTables.TABLE_BENEFICIARY + " where " + KEY_ID + "=" + id;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        BeneficiaryDto beneficiaryDto;
        if (cursor.moveToFirst()) {
            beneficiaryDto = new BeneficiaryDto(cursor);
            Log.e("kerosene_bunk", "Ufc code :" + beneficiaryDto.getEncryptedUfc());
            beneficiaryDto.setBenefMembersDto(getAllBeneficiaryMembers(beneficiaryDto.getEncryptedUfc()));
            cursor.close();
            return beneficiaryDto;
        }
        cursor.close();
        return null;
    }

    public List<POSStockAdjustmentDto> allStockAdjustmentData(int position) {
        long offSet = position * 50;
        String selectQuery = "SELECT * FROM fps_stock_adjustment where dateOfAck is Not Null limit 50  OFFSET " + offSet;
        List<POSStockAdjustmentDto> fpsInwardList = new ArrayList<>();
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            fpsInwardList.add(new POSStockAdjustmentDto(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return fpsInwardList;
    }
//    public List<POSStockAdjustmentDto>  allStockAdjustmentData() {
//       long offSet = position * 50;
//        String selectQuery = "SELECT * FROM fps_stock_adjustment order by dateOfAck desc";
//        List<POSStockAdjustmentDto> fpsInwardList = new ArrayList<>();
//        Cursor cursor = database.rawQuery(selectQuery, null);
//        if(cursor != null) {
//            cursor.moveToFirst();
//            for (int i = 0; i < cursor.getCount(); i++) {
//                fpsInwardList.add(new POSStockAdjustmentDto(cursor));
//                cursor.moveToNext();
//            }
//        }
//        cursor.close();
//        return  fpsInwardList;
//    }
//

    // Used to BeneficiaryDto beneficiary details
    public BeneficiaryDto retrieveBeneficiaryIn(long ufc) {
        String selectQuery = "SELECT  * FROM " + FPSDBTables.TABLE_BENEFICIARY_IN + " where _id=" + ufc;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        BeneficiaryDto beneficiaryDto;
        if (cursor.moveToFirst()) {
            beneficiaryDto = new BeneficiaryDto(cursor);
            beneficiaryDto.setBenefMembersDto(getAllBeneficiaryMembersIn(beneficiaryDto.getUfc()));
            cursor.close();
            return beneficiaryDto;
        }
        cursor.close();
        return null;
    }

    private Set<BeneficiaryMemberDto> getAllBeneficiaryMembersIn(String qrCode) {
        List<BeneficiaryMemberDto> beneficiaryMembers = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + FPSDBTables.TABLE_BENEFICIARY_MEMBER_IN + " where " + FPSDBConstants.KEY_BENEFICIARY_UFC + "='"
                + qrCode + "' AND " + FPSDBConstants.KEY_BENEFICIARY_MEMBER_ALIVE_STATUS + "= 0 order by tin";
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            beneficiaryMembers.add(new BeneficiaryMemberDto(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return new HashSet<>(beneficiaryMembers);
    }

    // Used to BeneficiaryDto beneficiary details
    public List<BeneficiaryDto> retrieveAllBeneficiary(String cardNumber) {
        Cursor cursor = null;
        try {
            // String selectQuery = "SELECT old_ration_card_num,aRegister FROM " + FPSDBTables.TABLE_BENEFICIARY + " where old_ration_card_num like '%" + cardNumber + "' ";
            String selectQuery = "SELECT old_ration_card_num, aRegister, active, fps_id FROM " + FPSDBTables.TABLE_BENEFICIARY + " where old_ration_card_num like '%" + cardNumber + "' and active = 1";
            SQLiteDatabase db = this.getReadableDatabase();
            cursor = db.rawQuery(selectQuery, null);
            List<BeneficiaryDto> beneficiaryDto = new ArrayList<>();
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                BeneficiaryDto benef = new BeneficiaryDto();
                benef.setOldRationNumber(cursor.getString(cursor.getColumnIndex("old_ration_card_num")));
                benef.setFpsId(cursor.getLong(cursor.getColumnIndex("fps_id")));
                String aRegisterNumber = cursor.getString(cursor.getColumnIndex("aRegister"));
                if (aRegisterNumber == null || aRegisterNumber.equals("-1")) {
                    aRegisterNumber = "";
                }
                benef.setAregisterNum(aRegisterNumber);
                String active = cursor.getString(cursor.getColumnIndex("active"));
                if (active.equalsIgnoreCase("0")) {
                    benef.setActive(false);
                } else if (active.equalsIgnoreCase("1")) {
                    benef.setActive(true);
                }
                beneficiaryDto.add(benef);
                cursor.moveToNext();
            }
            return beneficiaryDto;
        } catch (Exception e) {
            return null;
        } finally {
            if (cursor != null)
                cursor.close();
        }
    }

    // Used to BeneficiaryDto beneficiary details
    public List<BeneficiarySearchDto> retrieveAllBeneficiarySearch(String cardNumber, String aRegister, int limitSize) {
        Cursor cursor = null;
        try {
            String selectQuery;
            int limitStart = limitSize * 100;
            if (aRegister.length() > 0)
                selectQuery = "SELECT old_ration_card_num,aRegister,mobile,num_of_adults,num_of_child,num_of_cylinder,description FROM beneficiary  a inner join card_type b on a.card_type_id = b._id where substr(old_ration_card_num,4,7) like '%" + cardNumber + "%' AND aRegister like '%" + aRegister + "%'  order by aRegister Asc";
            else {
                selectQuery = "SELECT old_ration_card_num,aRegister,mobile,num_of_adults,num_of_child,num_of_cylinder,description FROM beneficiary a inner join card_type b on a.card_type_id = b._id where substr(old_ration_card_num,4,7) like '%" + cardNumber + "%'  order by aRegister Asc";
            }
            SQLiteDatabase db = this.getReadableDatabase();
            cursor = db.rawQuery(selectQuery, null);
            List<BeneficiarySearchDto> beneficiaryDto = new ArrayList<>();
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                beneficiaryDto.add(new BeneficiarySearchDto(cursor));
                cursor.moveToNext();
            }
            cursor.close();
            return beneficiaryDto;
        } catch (Exception e) {
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public boolean isTableExists(SQLiteDatabase db) {
        String selectQuery = "SELECT * FROM sqlite_master WHERE name ='temp_InwardValue' and type='table'";
        if (db == null) {
            db = this.getReadableDatabase();
        }
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            return true;
        }
        cursor.close();
        return false;
    }

    public boolean isTableValue() {
        String selectQuery = "insert into stock_inward select godown_id,fps_id,outward_date,product_id,quantity,unit,godown_name,godown_code,batch_no,referenceNo,fps_ack_status,fps_ack_date,fps_receive_quantity,created_by,delivery_challan_id,is_server_add,null,null,null,null,null,null from temp_InwardValue";
        database.execSQL(selectQuery);
        Log.e("Query Executed", "Query Executed");
        return true;
    }

    //Get Beneficiary data by QR Code
    public BeneficiaryDto beneficiaryDto(String qrCode) {
        String selectQuery = "SELECT  * FROM " + FPSDBTables.TABLE_BENEFICIARY + " where " + FPSDBConstants.KEY_BENEFICIARY_UFC + "='" + qrCode
                + "' AND " + FPSDBConstants.KEY_BENEFICIARY_ACTIVE + "=1";
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        if (cursor.getCount() == 0) {
            cursor.close();
            return null;
        } else {
            BeneficiaryDto beneficiary = new BeneficiaryDto(cursor);
            beneficiary.setBenefMembersDto(getAllBeneficiaryMembers(beneficiary.getUfc()));
            cursor.close();
            return beneficiary;
        }
    }

    //Get Beneficiary data by QR Code
    public BeneficiaryDto beneficiaryFromOldCard(String qrCode, long fpsid) {
        /*String selectQuery = "SELECT  * FROM " + FPSDBTables.TABLE_BENEFICIARY + " where " + FPSDBConstants.KEY_BENEFICIARY_OLD_RATION + "='"
                + qrCode.toUpperCase() + "' AND " + FPSDBConstants.KEY_BENEFICIARY_ACTIVE + "=1";*/
        String selectQuery = "SELECT  * FROM " + FPSDBTables.TABLE_BENEFICIARY + " where " + FPSDBConstants.KEY_BENEFICIARY_OLD_RATION + "='"
                + qrCode.toUpperCase() + "' AND " + FPSDBConstants.KEY_BENEFICIARY_FPS_ID + " = " + fpsid + " AND " + FPSDBConstants.KEY_BENEFICIARY_ACTIVE + "=1";
        Log.i("beneficiary query:  ", selectQuery);
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        if (cursor.getCount() == 0) {
            cursor.close();
            return null;
        } else {
            BeneficiaryDto beneficiary = new BeneficiaryDto(cursor);
            beneficiary.setBenefMembersDto(getAllBeneficiaryMembers(beneficiary.getUfc()));
            cursor.close();
            return beneficiary;
        }
    }
    public BeneficiaryDto beneficiaryFromOldCard(String qrCode) {
        String selectQuery = "SELECT  * FROM " + FPSDBTables.TABLE_BENEFICIARY + " where " + FPSDBConstants.KEY_BENEFICIARY_OLD_RATION + "='"
                + qrCode.toUpperCase() + "' AND " + FPSDBConstants.KEY_BENEFICIARY_ACTIVE + "=1";
        /*String selectQuery = "SELECT  * FROM " + FPSDBTables.TABLE_BENEFICIARY + " where " + FPSDBConstants.KEY_BENEFICIARY_OLD_RATION + "='"
                + qrCode.toUpperCase() + "' AND " + FPSDBConstants.KEY_BENEFICIARY_FPS_ID + " = " + fpsid + " AND " + FPSDBConstants.KEY_BENEFICIARY_ACTIVE + "=1";
       */ Log.i("query:  ", selectQuery);
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        if (cursor.getCount() == 0) {
            cursor.close();
            return null;
        } else {
            BeneficiaryDto beneficiary = new BeneficiaryDto(cursor);
            beneficiary.setBenefMembersDto(getAllBeneficiaryMembers(beneficiary.getUfc()));
            cursor.close();
            return beneficiary;
        }
    }
    public BeneficiaryDto beneficiaryFromUFCCode(String qrCode) {
        String selectQuery = "SELECT  * FROM " + FPSDBTables.TABLE_BENEFICIARY + " where " + FPSDBConstants.KEY_BENEFICIARY_UFC + "='"
                + qrCode + "' AND " + FPSDBConstants.KEY_BENEFICIARY_ACTIVE + "=1";
        Log.i("query:  ", selectQuery);
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        if (cursor.getCount() == 0) {
            cursor.close();
            return null;
        } else {
            BeneficiaryDto beneficiary = new BeneficiaryDto(cursor);
            beneficiary.setBenefMembersDto(getAllBeneficiaryMembers(beneficiary.getUfc()));
            cursor.close();
            return beneficiary;
        }
    }

    //Get Beneficiary data by QR Code
    public double productOverridePercentage(long productId, long cardType) {
        String selectQuery = "select  b.percentage as percent from card_type a inner join product_price_override b on b.card_type = a.type where a.isDeleted = 0 and b.is_deleted = 0 and " +
                "b.product_id = " + productId + " and  a._id = " + cardType;
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        double percent = 0;
        if (cursor.getCount() > 0) {
            percent = cursor.getDouble(cursor.getColumnIndex("percent"));
        }
        cursor.close();
        return percent;
    }

    //Get Product data by Product Id
    public ProductDto getProductDetails(long _id) {
        String selectQuery = "SELECT  * FROM " + FPSDBTables.TABLE_PRODUCTS + " where " + KEY_ID + "=" + _id;//+" AND " +FPSDBConstants.KEY_BENEFICIARY_ACTIVE +"=0";
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        ProductDto product = null;
        if (cursor.getCount() > 0) {
            product = new ProductDto(cursor);
        }
        cursor.close();
        return product;
    }

    //Get Product data
    public List<ProductDto> getAllProductDetails() {
        String selectQuery = "SELECT  * FROM " + FPSDBTables.TABLE_PRODUCTS + " where isDeleted = 0  order by  groupId";
        List<ProductDto> products = new ArrayList<>();
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            products.add(new ProductDto(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return products;
    }

    //Get Product stock data
    public FPSStockDto getAllProductStockDetails(long productId) {
        String selectQuery = "SELECT  * FROM " + FPSDBTables.TABLE_STOCK + " where " + FPSDBConstants.KEY_STOCK_PRODUCT_ID + " = " + productId;
        FPSStockDto productStock;
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        if (cursor.getCount() > 0)
            productStock = new FPSStockDto(cursor);
        else {
            productStock = null;
        }
        cursor.close();
        return productStock;
    }

    //Get Product stock data
    public List<StockCheckDto> getAllProductStockDetails(String date) {
        String selectQuery = " SELECT a.product_id,SUM(Case when createdDate like '" + date + "%' then c.quantity else 0 end) as sold ,a.quantity,name,unit,local_unit,local_name FROM stock a left join products b on a.product_id = b._id  left join bill_item c on  a.product_id = c.product_id group by a.product_id order by b.groupId,b._id";
        List<StockCheckDto> productStock = new ArrayList<>();
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            productStock.add(new StockCheckDto(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return productStock;
    }

    //Get Product stock data
    public FPSStockHistoryDto getAllProductStockHistoryDetails(long productId, String dateToday) {
        String selectQuery = "SELECT  * FROM stock_history where date_creation like '" + dateToday + "%' AND product_id = "
                + productId + " AND action <> 'INITIAL STOCK' order by created_date limit 1";
        FPSStockHistoryDto productStock = null;
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        if (cursor.getCount() > 0)
            productStock = new FPSStockHistoryDto(cursor);
        cursor.close();
        return productStock;
    }

    public FPSStockHistoryDto getAllProductOpeningStockDetails(long productId) {
        String selectQuery = "SELECT * FROM stock_history where action = 'INITIAL STOCK' and product_id =" + productId;
        FPSStockHistoryDto productStock;
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        if (cursor.getCount() > 0)
            productStock = new FPSStockHistoryDto(cursor);
        else {
            productStock = null;
        }
        cursor.close();
        return productStock;
    }

    //Bill for background sync
    public List<POSStockAdjustmentDto> getStockAdjustment(long productId) {
        List<POSStockAdjustmentDto> closeProduct = new ArrayList<>();
        String selectQuery = "select sum(quantity) as sum,requestType from fps_stock_adjustment where isAdjusted = 1 and product_id = " + productId + " and date(dateOfAck)=date('now','localtime') group by requestType";
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            POSStockAdjustmentDto closeSale = new POSStockAdjustmentDto();
            closeSale.setQuantity(cursor.getDouble(cursor.getColumnIndex("sum")));
            closeSale.setRequestType(cursor.getString(cursor.getColumnIndex("requestType")));
            closeProduct.add(closeSale);
            cursor.moveToNext();
        }
        cursor.close();
        return closeProduct;
    }

    //Get Product stock data
    public FPSStockHistoryDto getAllProductStockHistoryDetails(long productId) {
        String selectQuery = "SELECT * FROM stock_history where date(date_creation) <  date('now','localtime') and product_id = "
                + productId + " order by _id desc limit 1";
        Log.e("selectQuery", selectQuery);
        FPSStockHistoryDto productStock;
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            productStock = new FPSStockHistoryDto();
            productStock.setCurrQuantity(cursor.getDouble(cursor.getColumnIndex("closing_balance")));
            productStock.setPrevQuantity(cursor.getDouble(cursor.getColumnIndex("closing_balance")));
            productStock.setAction(cursor.getString(cursor.getColumnIndex("action")));
            productStock.setProductId(cursor.getLong(cursor.getColumnIndex("product_id")));
        } else {
            cursor.close();
            selectQuery = "SELECT opening_balance,closing_balance,product_id,action FROM stock_history where date(date_creation)  = date('now','localtime') and product_id="
                    + productId + " order by _id limit 1";
            Log.e("selectQuery", selectQuery);
            cursor = database.rawQuery(selectQuery, null);
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                productStock = new FPSStockHistoryDto();
                productStock.setCurrQuantity(cursor.getDouble(cursor.getColumnIndex("opening_balance")));
                productStock.setPrevQuantity(cursor.getDouble(cursor.getColumnIndex("opening_balance")));
                productStock.setAction(cursor.getString(cursor.getColumnIndex("action")));
                productStock.setProductId(cursor.getLong(cursor.getColumnIndex("product_id")));
            } else {
                selectQuery = "SELECT * FROM stock where product_id =" + productId;
                Log.e("selectQuery", selectQuery);
                cursor = database.rawQuery(selectQuery, null);
                cursor.moveToFirst();
                if (cursor.getCount() > 0) {
                    productStock = new FPSStockHistoryDto();
                    productStock.setCurrQuantity(cursor.getDouble(cursor.getColumnIndex("quantity")));
                    productStock.setPrevQuantity(cursor.getDouble(cursor.getColumnIndex("quantity")));
                    productStock.setAction("Stock");
                    productStock.setProductId(cursor.getLong(cursor.getColumnIndex("product_id")));
                } else {
                    cursor.close();
                    productStock = new FPSStockHistoryDto();
                    productStock.setCurrQuantity(0.0);
                    productStock.setPrevQuantity(0.0);
                    productStock.setAction("Empty");
                    productStock.setProductId(productId);
                }
            }
        }
        cursor.close();
        return productStock;
    }

    //This function loads data to language table
    public boolean getFirstSync() {
        String selectQuery = "SELECT  * FROM " + FPSDBTables.TABLE_LANGUAGE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        boolean value = cursor.getCount() == 0;
        cursor.close();
        return value;
    }
/*
    //This function loads data to language table
    public void insertImageData(String cardNumber, String imageId) {
        ContentValues values = new ContentValues();
        values.put("card_number", cardNumber);
        values.put("image_id", imageId);
        values.put("status", "R");
        database.insertWithOnConflict(FPSDBTables.TABLE_CARD_IMAGE, "card_number", values, SQLiteDatabase.CONFLICT_REPLACE);
    }*/

    //This function retrieve error description from language table
    public MessageDto retrieveLanguageTable(int errorCode) {
        String selectQuery = "SELECT  * FROM " + FPSDBTables.TABLE_LANGUAGE + " where  " + FPSDBConstants.KEY_LANGUAGE_CODE + " = " + errorCode;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        MessageDto messageDto = new MessageDto(cursor);
        cursor.close();
        return messageDto;
    }

    public List<GodownStockOutwardDto> showFpsStockInvard(boolean ackStatus, int position) {
        String selectQuery;
        long offSet = position * 50;
        if (!ackStatus) {
            selectQuery = "SELECT  * FROM " + FPSDBTables.TABLE_FPS_STOCK_INWARD + " where " + FPSDBConstants.KEY_FPS_STOCK_INWARD_IS_FPSACKSTATUS
                    + " = 0 group by referenceNo limit 50  OFFSET " + offSet;
        } else {
            selectQuery = "SELECT  * FROM " + FPSDBTables.TABLE_FPS_STOCK_INWARD + " where " + FPSDBConstants.KEY_FPS_STOCK_INWARD_IS_FPSACKSTATUS
                    + "  = 1 group by referenceNo limit 50  OFFSET " + offSet;
        }
        Cursor cursor = database.rawQuery(selectQuery, null);
        Log.e("Query value", "Cursor:" + cursor.getCount());
        List<GodownStockOutwardDto> fpsInwardList = new ArrayList<>();
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            fpsInwardList.add(new GodownStockOutwardDto(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return fpsInwardList;
    }

    public List<GodownStockOutwardDto> showFpsStockInvardDetail(String chellanId, boolean ackStatus) {
        List<GodownStockOutwardDto> fpsInwardList = new ArrayList<>();
        int ack = 0;
        if (ackStatus)
            ack = 1;
        try {
            String selectQuery = "SELECT _id,challanId,rowid,vehicleN0,driverName,SUM(quantity) as quantity,product_id,godown_id,fps_id,outward_date,unit,godown_name,godown_code," +
                    " batch_no,transportName,driverMobileNumber,referenceNo,fps_ack_status,fps_ack_date,fps_receive_quantity, created_by," +
                    "delivery_challan_id FROM " + FPSDBTables.TABLE_FPS_STOCK_INWARD + " where referenceNo='" + chellanId + "' AND fps_ack_status=" + ack + " group by product_id";
            Cursor cursor = database.rawQuery(selectQuery, null);
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                fpsInwardList.add(new GodownStockOutwardDto(cursor));
                cursor.moveToNext();
            }
            cursor.close();
            Log.e("stock inward ", fpsInwardList.toString());
        } catch (Exception e) {
            Log.e("stock inward excep", e.toString(), e);
        }
        return fpsInwardList;
    }

    public List<GodownStockOutward> allFpsStockInwardDetail() {
        List<GodownStockOutward> fpsInwardList = new ArrayList<>();
        try {
            String selectQuery = "SELECT * FROM temp_InwardValue";
            Cursor cursor = database.rawQuery(selectQuery, null);
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                fpsInwardList.add(new GodownStockOutward(cursor));
                cursor.moveToNext();
            }
            cursor.close();
            Log.e("stock inward ", fpsInwardList.toString());
        } catch (Exception e) {
            Log.e("stock inward excep", e.toString(), e);
        }
        return fpsInwardList;
    }

    public boolean getStockExists(GodownStockOutwardDto godownStockOutwardDto) {
        String selectQuery = "SELECT * from stock where product_id =" + godownStockOutwardDto.getProductId();
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            Log.i("Stock", "Empty");
        } else {
            insertFpsStockInward(godownStockOutwardDto);
        }
        cursor.close();
        return true;
    }

    // This function inserts details to TABLE_STOCK
    private void insertFpsStockInward(GodownStockOutwardDto godownStockOutwardDto) {
        for (ChellanProductDto fpsStock : godownStockOutwardDto.getProductDto()) {
            ContentValues values = new ContentValues();
            values.put(FPSDBConstants.KEY_STOCK_FPS_ID, godownStockOutwardDto.getFpsId());
            values.put(FPSDBConstants.KEY_STOCK_PRODUCT_ID, fpsStock.getProductId());
            NumberFormat formatter = new DecimalFormat("#0.000");
            values.put(FPSDBConstants.KEY_STOCK_QUANTITY, formatter.format(0.0));
            values.put(FPSDBConstants.KEY_STOCK_EMAIL_ACTION, 0);
            values.put(FPSDBConstants.KEY_STOCK_SMS_ACTION, 0);
            database.insert(FPSDBTables.TABLE_STOCK, null, values);
        }
    }

    public void updateReceivedQuantity(GodownStockOutwardDto godownStockOutwardDto, boolean isOffline) {
        ContentValues values = new ContentValues();
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date(godownStockOutwardDto.getFpsAckDate());
        values.put(FPSDBConstants.KEY_FPS_STOCK_INWARD_FPSACKDATE, dateFormat.format(date));
        for (ChellanProductDto chellanProductDto : godownStockOutwardDto.getProductDto()) {
            values.put(FPSDBConstants.KEY_FPS_STOCK_INWARD_IS_FPSACKSTATUS, 1);
            if (isOffline) {
                values.put("is_server_add", 1);
            } else {
                values.put("is_server_add", 2);
            }
            database.update(FPSDBTables.TABLE_FPS_STOCK_INWARD, values, "referenceNo = '" + godownStockOutwardDto.getReferenceNo() + "'", null);
            FPSStockDto stockList = getAllProductStockDetails(chellanProductDto.getProductId());
            if (stockList == null) {
                stockList = new FPSStockDto();
                stockList.setQuantity(0.0);
                stockList.setProductId(chellanProductDto.getProductId());
            }
            double closing = stockList.getQuantity() + chellanProductDto.getReceiProQuantity();
            insertStockHistory(stockList.getQuantity(), closing, "INWARD", chellanProductDto.getReceiProQuantity(), chellanProductDto.getProductId());
            stockList.setQuantity(closing);
            stockUpdate(stockList);
        }
    }

    //Update the stock
    public void stockUpdate(FPSStockDto stock) {
        ContentValues values = new ContentValues();
        NumberFormat formatter = new DecimalFormat("#0.000");
        values.put(FPSDBConstants.KEY_STOCK_QUANTITY, formatter.format(stock.getQuantity()));
        Log.e("Stock stock update", stock.toString());
        database.update(FPSDBTables.TABLE_STOCK, values, FPSDBConstants.KEY_STOCK_PRODUCT_ID + "=" + stock.getProductId(), null);
    }

    //Get Product Name by Product Id
    public String getProductName(long _id) {
        String selectQuery = "SELECT  * FROM " + FPSDBTables.TABLE_PRODUCTS + " where " + KEY_ID + "=" + _id;
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        String value = null;
        if (cursor.getCount() > 0)
            value = new ProductDto(cursor).getName();
        cursor.close();
        return value;
    }

    // database connection  close
    public synchronized void closeConnection() {
        if (dbHelper != null) {
            dbHelper.close();
            database.close();
            dbHelper = null;
            database = null;
        }
    }

    /* // Used to retrieve beneficiary details
     public String retrieveDataFromBeneficiary(long userName) {
         String selectQuery = "SELECT  * FROM " + FPSDBTables.TABLE_BENEFICIARY + " where " + KEY_ID + "=" + userName;
         SQLiteDatabase db = this.getReadableDatabase();
         Cursor cursor = db.rawQuery(selectQuery, null);
         BeneficiaryDto beneficiaryDto;
         String data = "";
         if (cursor.moveToFirst()) {
             beneficiaryDto = new BeneficiaryDto(cursor);
             data = beneficiaryDto.getEncryptedUfc();
         }
         cursor.close();
         return data;
     }

     // Used to retrieve beneficiary details
     public boolean retrieveARegNoFromBeneficiary(String aRegNo) {
         String selectQuery = "SELECT distinct(aRegister) FROM " + FPSDBTables.TABLE_BENEFICIARY + " where aRegister=" + Integer.parseInt(aRegNo);
         SQLiteDatabase db = this.getReadableDatabase();
         Cursor cursor = db.rawQuery(selectQuery, null);
         boolean isExists = false;
         if (cursor.getCount() > 0) {
             isExists = true;
         }
         cursor.close();
         return isExists;
     }

     // Used to retrieve beneficiary details
     public boolean retrieveCardNoBeneficiary(String rationCardNo) {
         String selectQuery = "SELECT distinct(old_ration_card_num) FROM " + FPSDBTables.TABLE_BENEFICIARY + " where " + FPSDBConstants.KEY_BENEFICIARY_OLD_RATION + "='" + rationCardNo + "'";
         SQLiteDatabase db = this.getReadableDatabase();
         Cursor cursor = db.rawQuery(selectQuery, null);
         boolean isExists = false;
         if (cursor.getCount() > 0) {
             isExists = true;
         }
         cursor.close();
         return isExists;
     }
 */
    // Used to retrieve beneficiary details
    public BeneficiaryDto retrieveIdFromBeneficiary(String mobile) {
        String selectQuery = "SELECT  * FROM " + FPSDBTables.TABLE_BENEFICIARY + " where " + FPSDBConstants.KEY_BENEFICIARY_MOBILE + "='" + mobile + "'";
        Log.i("query:  ", selectQuery);
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        if (cursor.getCount() == 0) {
            cursor.close();
            return null;
        } else {
            BeneficiaryDto beneficiary = new BeneficiaryDto(cursor);
            beneficiary.setBenefMembersDto(getAllBeneficiaryMembers(beneficiary.getUfc()));
            cursor.close();
            return beneficiary;
        }
    }

    // Used to retrieve beneficiary details
    public BeneficiaryDto retrieveIdFromBeneficiaryReg(int aReg) {
        String selectQuery = "SELECT  * FROM " + FPSDBTables.TABLE_BENEFICIARY + " where aRegister=" + aReg;
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        if (cursor.getCount() == 0) {
            cursor.close();
            return null;
        } else {
            BeneficiaryDto beneficiary = new BeneficiaryDto(cursor);
            beneficiary.setBenefMembersDto(getAllBeneficiaryMembers(beneficiary.getUfc()));
            cursor.close();
            return beneficiary;
        }
    }

    //Get Product data
    public PersonBasedRule getAllPersonBasedRule(long productId) {
        String selectQuery = "SELECT  * FROM " + FPSDBTables.TABLE_PERSON_RULES + " where groupId = " + productId + " AND isDeleted = 0";
        PersonBasedRule products = new PersonBasedRule();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            products = new PersonBasedRule(cursor);
        }
        cursor.close();
        return products;
    }

    //Get Product data
    public List<RegionBasedRule> getAllRegionBasedRule(long productId) {
        String selectQuery = "SELECT  * FROM " + FPSDBTables.TABLE_REGION_RULES + " where groupId = " + productId + " AND isDeleted = 0";
        List<RegionBasedRule> region = new ArrayList<>();
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            region.add(new RegionBasedRule(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return region;
    }

    public void updateMaserData(String name, String value) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("value", value);
        Log.e("checkdata", "" + value);
        database.update(FPSDBTables.TABLE_CONFIG_TABLE, values, "name='" + name + "'", null);
    }

    public String getMasterData(String key) {
        String selectQuery = "SELECT  * FROM " + FPSDBTables.TABLE_CONFIG_TABLE + " where name = '" + key + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        String value = null;
        try {
            if (cursor.moveToFirst()) {
                value = cursor.getString(cursor
                        .getColumnIndex("value"));
            }
        } catch (Exception e) {
            Log.e("Error", e.toString(), e);
        }
        cursor.close();
        return value;
    }

    public List<Long> getAllDistinctFps() {
        List<Long> bunkerfpsList = new ArrayList<>();
        try {
            String selectQuery = "select distinct(fps_id) from beneficiary";
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery(selectQuery, null);
            cursor.moveToFirst();
            Log.e("Dbhelper", "Get all Distinct fps_id :" + cursor.getCount());
            for (int i = 0; i < cursor.getCount(); i++) {
                Long fps_id = cursor.getLong(cursor.getColumnIndex("fps_id"));
                bunkerfpsList.add(fps_id);
                cursor.moveToNext();
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Exception", "while getallfpsid" + e.toString());
        }
        return bunkerfpsList;
    }

    public String getLastLoginTime(long userId) {
        String selectQuery = "SELECT  last_login_time as login_time FROM users where _id = " + userId;
        SQLiteDatabase db = this.getReadableDatabase();
        String loginTime = null;
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            loginTime = cursor.getString(cursor.getColumnIndex("login_time"));
        }
        cursor.close();
        return loginTime;
    }

    public String getOpeningTime(long userId) {
        String selectQuery = "SELECT  operation_opening_time as open_time FROM users where _id = " + userId;
        SQLiteDatabase db = this.getReadableDatabase();
        String loginTime = "9.00";
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            loginTime = cursor.getString(cursor.getColumnIndex("open_time"));
        }
        cursor.close();
        return loginTime;
    }

    public UpgradeDetailsDto getUpgradeData() {
        try {
            UpgradeDetailsDto upgradeDto = new UpgradeDetailsDto();
            String unSinkQuery = "SELECT count(*) as bill_count FROM bill where bill_status <> 'T'";
            String billQuery = "SELECT count(*) as bill_count FROM bill";
            String productQuery = "SELECT count(*) as product_count FROM products";
            String beneficiaryQuery = "SELECT count(*) as bene_count FROM beneficiary";
            //  String beneficiaryOfflineQuery = "SELECT count(*) as bene_count FROM offline_activation where active=0";
            String cardQuery = "SELECT count(*) as card_count FROM card_type";
            String stockQuery = "SELECT count(*) as stock_count FROM Stock";
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursorBill = db.rawQuery(unSinkQuery, null);
            cursorBill.moveToFirst();
            upgradeDto.setBillUnsyncCount(cursorBill.getInt(cursorBill.getColumnIndex("bill_count")));
            cursorBill.close();
            Cursor cursorProduct = db.rawQuery(productQuery, null);
            cursorProduct.moveToFirst();
            upgradeDto.setProductCount(cursorProduct.getInt(cursorProduct.getColumnIndex("product_count")));
            cursorProduct.close();
            Cursor cursorCard = db.rawQuery(cardQuery, null);
            cursorCard.moveToFirst();
            upgradeDto.setCardTypeCount(cursorCard.getInt(cursorCard.getColumnIndex("card_count")));
            cursorCard.close();
            Cursor cursorStock = db.rawQuery(stockQuery, null);
            cursorStock.moveToFirst();
            upgradeDto.setFpsStockCount(cursorStock.getInt(cursorStock.getColumnIndex("stock_count")));
            cursorStock.close();
            Cursor cursorBillQuery = db.rawQuery(billQuery, null);
            cursorBillQuery.moveToFirst();
            upgradeDto.setBillCount(cursorBillQuery.getInt(cursorBillQuery.getColumnIndex("bill_count")));
            cursorBillQuery.close();
            Cursor cursorBq = db.rawQuery(beneficiaryQuery, null);
            cursorBq.moveToFirst();
            //upgradeDto.setBeneficiaryCount(cursorBq.getInt(cursorBq.getColumnIndex("bene_count")));
            //  cursorBq.close();
            // Cursor cursorBene = db.rawQuery(beneficiaryOfflineQuery, null);
            //  cursorBene.moveToFirst();
            // upgradeDto.setBeneficiaryUnsyncCount(cursorBene.getInt(cursorBene.getColumnIndex("bene_count")));
            //  cursorBene.close();
            Log.e("upgradeDto", upgradeDto.toString());
            return upgradeDto;
        } catch (Exception e) {
            Log.e("Exception", "" + e.toString());
        }
        return null;
    }

    //Insert into Person Rules
    public void insertProductHistory(Set<KeroseneBunkStockHistoryDto> groupProduct) {
        try {
            for (KeroseneBunkStockHistoryDto productGroup : groupProduct) {
                insertStockHistory(productGroup.getCurrQuantity(), getClosingStock(productGroup.getProductDto().getId()), "INITIAL STOCK", 0.0, productGroup.getProductDto().getId());
            }
        } catch (Exception e) {
            Log.e("Person Rules", e.toString(), e);
        }
    }

    private double getClosingStock(long productId) {
        double closingStock = 0l;
        String selectQuery = "SELECT * FROM stock where product_id = " + productId;
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            closingStock = cursor.getDouble(cursor.getColumnIndex("quantity"));
        }
        cursor.close();
        return closingStock;
    }

    // Used to retrieve data when no network available in device
    public LoginResponseDto retrieveData(String userName) {
        String selectQuery = "SELECT  * FROM " + FPSDBTables.TABLE_USERS + " where " + FPSDBConstants.KEY_USERS_NAME + " = '" + userName.toLowerCase() + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        LoginResponseDto loginResponse;
        if (cursor.moveToFirst()) {
            loginResponse = new LoginResponseDto(cursor);
            loginResponse.setUserDetailDto(new UserDetailDto(cursor));
            return loginResponse;
        }
        cursor.close();
        return null;
    }

    // Used to retrieve user name
    public LoginResponseDto getUserDetails(long userId) {
        String selectQuery = "SELECT  * FROM " + FPSDBTables.TABLE_USERS + " where " + KEY_ID + " = " + userId;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        LoginResponseDto loginResponse;
        if (cursor.moveToFirst()) {
            loginResponse = new LoginResponseDto(cursor);
            loginResponse.setUserDetailDto(new UserDetailDto(cursor));
            Log.i("UserDetails", loginResponse.toString());
            return loginResponse;
        }
        cursor.close();
        return null;
    }

    // Used to retrieve data when no network available in device
    public List<FpsStoreDto> retrieveDataStore() {
        String selectQuery = "SELECT  * FROM " + FPSDBTables.TABLE_USERS;
        List<FpsStoreDto> data = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            FpsStoreDto loginResponse = new FpsStoreDto(cursor);
            data.add(loginResponse);
        }
        cursor.close();
        return data;
    }

    //Get Product data
    public List<EntitlementMasterRuleDtod> getAllEntitlementMasterRuleProduct(long cardType) {
        String selectQuery = "SELECT a._id,a.is_person_based,a.override_price,a.minimum_qty,b.group_id,a.is_calc_required,a.is_region_based,a.has_special_rule,a.isDeleted,a.quantity,c._id as product_id,c.name,c.price,c.unit,c.local_unit,c.local_name from entitlement_rules a inner join product_group b ON a.groupId = b.group_id  inner join products c  on b.product_id = c._id where a.card_type = " + cardType + " and a.isDeleted = 0 and b.is_deleted = 0 and c.isDeleted = 0 group by b.product_id order by b.group_id";
        Log.e("selectQuery", selectQuery);
        SQLiteDatabase db = this.getReadableDatabase();
        List<EntitlementMasterRuleDtod> products = new ArrayList<>();
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        Log.e("Error in Count", "Cursor count:" + cursor.getCount());
        for (int i = 0; i < cursor.getCount(); i++) {
            products.add(new EntitlementMasterRuleDtod(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return products;
    }

  /*  public long getCardTypeIdFromType(String cardType) {
        String selectQuery = "SELECT " + KEY_ID + " from card_type where type = '" + cardType + "'";
        long id = 0l;
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            id = cursor.getLong(cursor.getColumnIndex(KEY_ID));
        }
        cursor.close();
        return id;
    }*/

    //Get Product data
    public List<SplEntitlementRule> getAllSpecialRule(long productId, String cardTypeId) {
        String selectQuery = "SELECT  * FROM " + FPSDBTables.TABLE_SPECIAL_RULES + " where groupId = " + productId
                + " AND " + FPSDBConstants.KEY_RULES_CARD_TYPE_ID + " = '" + cardTypeId + "' AND isDeleted = 0";
        List<SplEntitlementRule> region = new ArrayList<>();
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            region.add(new SplEntitlementRule(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return region;
    }

    public String lastBillToday() {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMyy", Locale.getDefault());
            //      String selectQuery = "SELECT transaction_id FROM bill where transaction_id LIKE '" + dateFormat.format(new Date()) + "%' order by date desc";
            String selectQuery = "SELECT max(transaction_id) as transaction_id FROM bill where transaction_id LIKE '" + dateFormat.format(new Date()) + "%' order by ref_id desc";
            Log.e("Dbhelper", "last login today " + selectQuery);
            Cursor cursor = database.rawQuery(selectQuery, null);
            cursor.moveToFirst();
            String maxDate = cursor.getString(cursor.getColumnIndex("transaction_id"));
            cursor.close();
            return maxDate;
        } catch (Exception e) {
            Log.e("last Bill Today", e.toString(), e);
            return null;
        }
    }

/*
    //Get Beneficiary data by QR Code
    public String lastGenId() {
        String selectQuery = "SELECT  * FROM " + FPSDBTables.TABLE_REGISTRATION + " order by " + FPSDBConstants.KEY_REGISTRATION_CARD_REF_NO + " DESC limit 1";
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        String transId = null;
        if (cursor.getCount() > 0) {
            transId = cursor.getString(cursor.getColumnIndex(FPSDBConstants.KEY_REGISTRATION_CARD_REF_NO));
        }
        cursor.close();
        return transId;
    }*/

    //Get Product data
    public SmsProviderDto getSmsProvider() {
        String selectQuery = "SELECT  * FROM " + FPSDBTables.TABLE_SMS_PROVIDER + " where status = 1 AND " + FPSDBConstants.KEY_SMS_PROVIDER_PREFERENCE + " = 'PRIMARY'";
        SmsProviderDto products = new SmsProviderDto();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            products = new SmsProviderDto(cursor);
        }
        cursor.close();
        return products;
    }

    //Bill for background sync
    public BillItemDto getAllInwardListToday(String toDate, long productId) {
        BillItemDto billItemDto = new BillItemDto();
        String selectQuery = "SELECT product_id,SUM(change_in_balance) as total FROM stock_history where date_creation LIKE '"
                + toDate + " %' AND action = 'INWARD' and product_id=" + productId;
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        billItemDto.setProductId(productId);
        if (cursor.getCount() > 0) {
            billItemDto.setQuantity(cursor.getDouble(cursor.getColumnIndex("total")));
        } else {
            billItemDto.setQuantity(0.0);
        }
        cursor.close();
        return billItemDto;
    }

    public BillItemDto getAllInwardListTodaylist(long productId) {
        DateTime date = new DateTime();
        BillItemDto billItemDto = new BillItemDto();
        String selectQuery = "SELECT product_id,SUM(quantity) as total FROM stock_inward where  product_id = " + productId + " and date(fps_ack_date) = date('now', 'localtime') ";
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        billItemDto.setProductId(productId);
        if (cursor.getCount() > 0) {
            billItemDto.setQuantity(cursor.getDouble(cursor.getColumnIndex("total")));
        } else {
            billItemDto.setQuantity(0.0);
        }
        cursor.close();
        return billItemDto;
    }

    //Bill for background sync
    public int totalBillsToday(String toDate) {
        String selectQuery = "SELECT COUNT(*)  as count FROM bill where date like '" + toDate + "%'";
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        int count = cursor.getInt(cursor.getColumnIndex("count"));
        cursor.close();
        return count;
    }

    //Bill for background sync
    public Double totalAmountToday(String toDate) {
        String selectQuery = "SELECT sum(totalCost) as cost FROM bill_item where createdDate like '" + toDate + "%'";
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        Double count = cursor.getDouble(cursor.getColumnIndex("cost"));
        cursor.close();
        return count;
    }

    public void insertFpsIntentReqProQuantity(FPSIndentRequestDto fpsIndentRequestDto) {
        try {
            ContentValues values = new ContentValues();
            values.put(FPSDBConstants.KEY_FPS_INTENT_REQUEST_GODOWN_ID, fpsIndentRequestDto.getGodownId());
            values.put(FPSDBConstants.KEY_FPS_INTENT_REQUEST_FPS_ID, fpsIndentRequestDto.getFpsId());
            if (!fpsIndentRequestDto.isTalukOffiApproval()) {
                values.put(FPSDBConstants.KEY_FPS_INTENT_REQUEST_TALUK_OFFICER_APPROVAL, 0);
            } else {
                values.put(FPSDBConstants.KEY_FPS_INTENT_REQUEST_TALUK_OFFICER_APPROVAL, 1);
            }
            values.put(FPSDBConstants.KEY_FPS_INTENT_REQUEST_DATE_OF_APPROVAL, fpsIndentRequestDto.getDateOfApproval());
            NumberFormat formatter = new DecimalFormat("#0.000");
            values.put(FPSDBConstants.KEY_FPS_INTENT_REQUEST_MODIFIED_QUANTITY, formatter.format(fpsIndentRequestDto.getModifiedQuantity()));
            if (!fpsIndentRequestDto.isStatus()) {
                values.put(FPSDBConstants.KEY_FPS_INTENT_REQUEST_STATUS, 0);
            } else {
                values.put(FPSDBConstants.KEY_FPS_INTENT_REQUEST_STATUS, 1);
            }
            values.put(FPSDBConstants.KEY_FPS_INTENT_REQUEST_REASON, fpsIndentRequestDto.getReason());
            values.put(FPSDBConstants.KEY_FPS_INTENT_REQUEST_DESCRIPTION, fpsIndentRequestDto.getDescription());
            for (FpsIntentReqProdDto fpsIntentReqProdDto : fpsIndentRequestDto.getProdDtos()) {
                values.put(FPSDBConstants.KEY_FPS_INTENT_REQUEST_PRODUCT_ID, fpsIntentReqProdDto.getProductId());
                values.put(FPSDBConstants.KEY_FPS_INTENT_REQUEST_QUANTITY, formatter.format(fpsIntentReqProdDto.getQuantity()));
                database.insert(FPSDBTables.TABLE_FPS_INTENT_REQUEST_PRODUCT, null, values);
            }
        } catch (Exception e) {
            Log.e("Empty", e.toString());
        }
    }

    /*public void purgeBillBItemDetails(String transmitted) {
        String sql = "DELETE FROM " + FPSDBTables.TABLE_BILL + " WHERE " + FPSDBConstants.KEY_BILL_DATE + " <= date('now','-31 day')" + " AND "
                + FPSDBConstants.KEY_BILL_STATUS + " ='" + transmitted + "'";
        try {
            database.beginTransaction();
            database.execSQL(sql);
            purgeBillItemDetails();
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("purge Bill Details", e.toString(), e);
        } finally {
            database.endTransaction();
        }

    }*/

    public List<FPSIndentRequestDto> showFpsIntentRequestProduct(long fpsId) {
        List<ProductDto> productDtoList = getAllProductDetails();
        List<FPSIndentRequestDto> fpsIndentRequestDtoList = new ArrayList<>();
        Set<FpsIntentReqProdDto> fpsIntentReqProdDtoSet = new HashSet<>();
        FPSIndentRequestDto fpsIndentRequestDto = new FPSIndentRequestDto();
        fpsIndentRequestDto.setFpsId(fpsId);
        for (ProductDto productDto : productDtoList) {
            FpsIntentReqProdDto fpsIntentReqProdDto = new FpsIntentReqProdDto();
            fpsIntentReqProdDto.setProductId(productDto.getId());
            fpsIntentReqProdDto.setName(productDto.getName());
            fpsIntentReqProdDtoSet.add(fpsIntentReqProdDto);
            fpsIndentRequestDto.setProdDtos(fpsIntentReqProdDtoSet);
            fpsIndentRequestDtoList.add(fpsIndentRequestDto);
        }
        return fpsIndentRequestDtoList;
    }

    public List<StockAllotmentDto> getReceivedQuantityStockInwardMonthYear(int year, int month) {
        String selectQuery = "select A.product_id as aproduct_id,A.month as amonth, case when I.fps_receive_quantity is not null then sum(I.fps_receive_quantity) else null end  as total_quantity,A.alloted_quantity as total_aquantity"
                + " from  " + FPSDBTables.TABLE_STOCK_ALLOTMENT_DETAILS + " A LEFT OUTER JOIN  " + FPSDBTables.TABLE_FPS_STOCK_INWARD + "  I ON   (A.product_id = I.product_id) "
                + " where  A.year = I.year "
                + " and A.month = I.month "
                + " and A.year = " + year
                + " and A.month = " + month
                + " group by A.product_id,A.month "
                + " union all "
                + " select A.product_id as aproduct_id,A.month as amonth,null as total_quantity,A.alloted_quantity as total_aquantity from " + FPSDBTables.TABLE_STOCK_ALLOTMENT_DETAILS + " A "
                + " where A.month not in (select fsi.month from " + FPSDBTables.TABLE_FPS_STOCK_INWARD + " fsi where fsi.year = " + year + ")"
                + " and A.year = " + year
                + " and A.month = " + month
                + " order by A.month,A.product_id";
        Log.i("query", selectQuery);
        List<StockAllotmentDto> stockAllotmentDtoList = new ArrayList<>();
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        Log.i("Cursor Size", "Count:" + cursor.getCount());
        for (int i = 0; i < cursor.getCount(); i++) {
            StockAllotmentDto stockAllotmentDto = new StockAllotmentDto();
            Integer productId = cursor.getInt(cursor.getColumnIndex("aproduct_id"));
            Integer mo = cursor.getInt(cursor.getColumnIndex("amonth"));
            Double receivedTotal = cursor.getDouble(cursor.getColumnIndex("total_quantity"));
            Double allottedTotal = cursor.getDouble(cursor.getColumnIndex("total_aquantity"));
            stockAllotmentDto.setRecivQuantity(receivedTotal);
            stockAllotmentDto.setAllotedQuantity(allottedTotal);
            stockAllotmentDto.setProductId(productId);
            stockAllotmentDto.setMonth(mo);
            stockAllotmentDtoList.add(stockAllotmentDto);
            cursor.moveToNext();
        }
        cursor.close();
        return stockAllotmentDtoList;
    }

    public void purgeBill() {
        String sqlPurgeQuery = "Delete from bill where date <=date ('now','-61 day') and bill_status<>'R'";
        try {
            database.beginTransaction();
            database.execSQL(sqlPurgeQuery);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("purge BillItem Details", e.toString(), e);
        } finally {
            database.endTransaction();
            purgeBillItemDetails();
        }
    }

    private void purgeBillItemDetails() {
        String sql = "Delete from bill_item where transaction_id NOT IN (select transaction_id from bill)";
        try {
            database.beginTransaction();
            database.execSQL(sql);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("purge BillItem Details", e.toString(), e);
        } finally {
            database.endTransaction();
            purgeLoginHistoryDetails();
        }
    }

    private void purgeStockHistoryDetails() {
        String sql = "Delete from stock_history where date_creation <=date ('now','-61 day')";
        try {
            database.beginTransaction();
            database.execSQL(sql);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("purge BillItem Details", e.toString(), e);
        } finally {
            database.endTransaction();
            purgeInwardDetails();
        }
    }

    private void purgeLoginHistoryDetails() {
        String sql = "Delete from login_history where login_time <=date ('now','-61 day') and is_sync=1";
        try {
            database.beginTransaction();
            database.execSQL(sql);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("purge BillItem Details", e.toString(), e);
        } finally {
            database.endTransaction();
            purgeStockHistoryDetails();
        }
    }

    private void purgeInwardDetails() {
        String sql = "DELETE FROM " + FPSDBTables.TABLE_FPS_STOCK_INWARD + " WHERE " + FPSDBConstants.KEY_FPS_STOCK_INWARD_FPSACKDATE + " <= date ('now','-61 day')  and is_server_add <>1";
        try {
            database.beginTransaction();
            database.execSQL(sql);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("purge Inward Details", e.toString(), e);
        } finally {
            database.endTransaction();
        }
    }

    public void stockAdjustmentUpdate(List<StockRequestDto.ProductList> prodsList) {
        try {
            for (StockRequestDto.ProductList product : prodsList) {
                ContentValues values = new ContentValues();
                double closing;
                NumberFormat formatter = new DecimalFormat("#0.000");
                if (product.getAdjustmentItem() == 1) {
                    closing = product.getQuantity() - product.getRecvQuantity();
                } else {
                    closing = product.getQuantity() + product.getRecvQuantity();
                }
                values.put(FPSDBConstants.KEY_STOCK_QUANTITY, formatter.format(closing));
                database.beginTransaction();
                FPSStockDto stockList = getAllProductStockDetails(product.getId());
                insertStockHistory(stockList.getQuantity(), closing, "ADJUSTMENT", product.getRecvQuantity(), product.getId());
                database.update(FPSDBTables.TABLE_STOCK, values, FPSDBConstants.KEY_STOCK_PRODUCT_ID + "=" + product.getId(), null);
            }
            database.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("stock update", e.toString(), e);
        } finally {
            database.endTransaction();
        }
    }

    //roleFeature Retrivelist
    public List<RoleFeatureDto> retrieveData(long userId) {
        String selectQuery = "SELECT  * FROM " + FPSDBTables.TABLE_ROLE_FEATURE + " WHERE " + FPSDBConstants.KEY_ROLE_PARENTID + "= 0 AND isDeleted = 1 AND " + FPSDBConstants.KEY_ROLE_USERID + "= " + userId
                + "  group by role_type order by " + FPSDBConstants.KEY_ROLE_FEATUREID;
        Log.e("Query", selectQuery);
        List<RoleFeatureDto> roleFeature = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            roleFeature.add(new RoleFeatureDto(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return roleFeature;
    }

    //roleFeature Retrivelist
    public boolean retrievePrintAllowed(long userId) {
        String selectQuery = "SELECT  * FROM " + FPSDBTables.TABLE_ROLE_FEATURE + " WHERE " + FPSDBConstants.KEY_ROLE_USERID + "= " + userId + " and isDeleted = 1 AND role_name = 'PRINT_RECEIPT' ";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }

/*
    //printer Retrivelist
    public String retrievePrinter() {
        String selectQuery = "SELECT * FROM configuration where name = 'printer'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        String printerMac = "";
        if (cursor.getCount() > 0) {
            printerMac = cursor.getString(cursor.getColumnIndex("value"));
        }
        cursor.close();
        return printerMac;
    }*/

    //roleFeature retrieveRolesData
    public Set<String> retrieveRolesDataString(long userId) {
        String selectQuery = "SELECT  * FROM " + FPSDBTables.TABLE_ROLE_FEATURE + " WHERE " + FPSDBConstants.KEY_ROLE_PARENTID + "= 0 AND isDeleted = 1 AND " + FPSDBConstants.KEY_ROLE_USERID + "= " + userId
                + " order by " + FPSDBConstants.KEY_ROLE_FEATUREID;
        Log.e("RetrieveRolesDataString", "SelectedRoleFeaturequery" + selectQuery);
        Set<String> roleFeature = new HashSet<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            roleFeature.add(cursor.getString(cursor.getColumnIndex(FPSDBConstants.KEY_ROLE_NAME)));
            cursor.moveToNext();
        }
        cursor.close();
        return roleFeature;
    }

    public List<RoleFeatureDto> retrieveSalesOrderData(long roleId, long userId) {
        String selectQuery = "SELECT  * FROM " + FPSDBTables.TABLE_ROLE_FEATURE + " WHERE role_parent_id = " + roleId + " AND isDeleted = 1 AND user_id = " + userId + " order by role_id";
        List<RoleFeatureDto> roleFeature = new ArrayList<>();
        Log.e("select", selectQuery);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            roleFeature.add(new RoleFeatureDto(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return roleFeature;
    }

    //Bill for background sync
    public List<BillDto> getAllBillsForSync() {
        List<BillDto> bills = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + FPSDBTables.TABLE_BILL + " where " + FPSDBConstants.KEY_BILL_STATUS + "<>'T'";
        Log.e("getAllBillsForSync", "query" + selectQuery);
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            BillDto bill = new BillDto(cursor);
            bill.setBillItemDto(getBillItems(bill.getTransactionId()));
            bills.add(bill);
            Log.i("bills", bill.toString());
            cursor.moveToNext();
        }
        cursor.close();
        return bills;
    }

    //Bill for background sync
    public List<LoginHistoryDto> getAllLoginHistory() {
        List<LoginHistoryDto> loginHistoryList = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + FPSDBTables.TABLE_LOGIN_HISTORY + " where (is_sync=0 OR is_logout_sync = 0) AND (login_type ='OFFLINE_LOGIN' OR logout_type ='OFFLINE_LOGOUT' OR logout_type =' CLOSE_SALE_LOGOUT_OFFLINE') ";
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            LoginHistoryDto loginHistory = new LoginHistoryDto(cursor);
            loginHistoryList.add(loginHistory);
            cursor.moveToNext();
        }
        cursor.close();
        return loginHistoryList;
    }

    //Bill for background sync
    public List<OfflineActivationSynchDto> getAllRegistrationForSync() {
        List<OfflineActivationSynchDto> offlineData = new ArrayList<>();
        String selectQuery = "SELECT * FROM offline_activation where active = 0";
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            OfflineActivationSynchDto bill = new OfflineActivationSynchDto(cursor);
            bill.setBillDtos(getAllOfflineBills(bill.getRationCardNumber()));
            offlineData.add(bill);
            Log.i("bills", bill.toString());
            cursor.moveToNext();
        }
        cursor.close();
        return offlineData;
    }

    public List<MigrationOutDTO> getMigrationOut() {
        DateTime month = new DateTime();
        String selectQuery = "SELECT * FROM " + FPSDBTables.TABLE_FPS_MIGRATION_OUT + " where isAdded = 0 and month_out <=" + month.getMonthOfYear() + " and year_out <=" + month.getYear();
        Log.e("Migration Select", selectQuery);
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        List<MigrationOutDTO> oldRationCard = new ArrayList<>();
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            oldRationCard.add(new MigrationOutDTO(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return oldRationCard;
    }

    public List<MigrationOutDTO> getMigrationIn() {
        DateTime month = new DateTime();
        String selectQuery = "SELECT * FROM " + FPSDBTables.TABLE_FPS_MIGRATION_IN + " where isAdded = 0 and month_in=" + month.getMonthOfYear() + " and year_in=" + month.getYear();
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        List<MigrationOutDTO> oldRationCard = new ArrayList<>();
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            oldRationCard.add(new MigrationOutDTO(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return oldRationCard;
    }

    //Bill for background sync
    public List<BillDto> getAllOfflineBills(String cardNo) {
        List<BillDto> bills = new ArrayList<>();
        String selectQuery = "SELECT * FROM offline_bill  where ufc_code = '" + cardNo + "'";
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            BillDto bill = new BillDto(cursor);
            bill.setBillItemDto(getAllOfflineBillItems(bill.getTransactionId()));
            bills.add(bill);
            cursor.moveToNext();
        }
        cursor.close();
        return bills;
    }

    public void dropTableExists() {
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("drop table if exists temp_InwardValue");
    }

    //Bill for background sync
    public Set<BillItemProductDto> getAllOfflineBillItems(String cardNo) {
        List<BillItemProductDto> billItems = new ArrayList<>();
        String selectQuery = " SELECT  product_id,transaction_id,name,quantity,cost,unit,local_unit,local_name FROM bill_item_offline a inner join products b on a.product_id = b._id where transaction_id ='" + cardNo + "'";
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            BillItemProductDto bill = new BillItemProductDto(cursor);
            billItems.add(bill);
            cursor.moveToNext();
        }
        Set<BillItemProductDto> billItemAll = new HashSet<>(billItems);
        cursor.close();
        return billItemAll;
    }

    public long retrieveId(String roleId) {
        String selectQuery = "SELECT  role_id FROM " + FPSDBTables.TABLE_ROLE_FEATURE + " WHERE " + FPSDBConstants.KEY_ROLE_NAME + "= '" + roleId + "'";
        long id = 0l;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            id = cursor.getLong(cursor.getColumnIndex("role_id"));
            cursor.moveToNext();
        }
        cursor.close();
        return id;
    }

    //Get VersionInfo data
    public List<VersionDto> getVersionInfo() {
        String selectQuery = "SELECT  * FROM " + FPSDBTables.TABLE_UPGRADE + " ORDER BY android_new_version DESC;";
        List<VersionDto> versionInfo = new ArrayList<>();
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            versionInfo.add(new VersionDto(cursor));
            cursor.moveToNext();
        }
        Log.e("version_detail", "--->" + versionInfo.toString());
        cursor.close();
        return versionInfo;
    }

    public void purge(int days) {
        String sql1 = "Delete from bill where date <=date ('now','-" + days + " day') and bill_status<>'R'";
        String sql2 = "Delete from bill_item where transaction_id NOT IN (select transaction_id from bill)";
        String sql3 = "Delete from login_history where login_time <= date ('now','-" + days + " day') and is_sync=1";
        String sql4 = "Delete from stock_history where date_creation <=date ('now','-" + days + " day')";
        String sql5 = "Delete from stock_inward where fps_ack_date <= date ('now','-" + days + " day') and is_server_add <>1";
        String[] statements = new String[]{sql1, sql2, sql3, sql4, sql5};
        for (String sql : statements) {
            try {
                Log.e("purging...........", sql);
                database.beginTransaction();
                database.execSQL(sql);
                database.setTransactionSuccessful();
                database.endTransaction();
            } catch (Exception e) {
                Log.e("purge exception...", e.toString(), e);
            }
        }
    }

    public PersonBasedRule findByGroupAndCardType(long groupId, String cardType) {
        // Util.LoggingQueue(contextValue, "FPSDBHelper ", "findByGroupAndCardType() called groupId -> " + groupId + " cardType ->" + cardType);
        //SELECT  * FROM  person_rules where groupId = 15 AND card_type_id  = 14 AND isDeleted = 0
        String selectQuery = "SELECT  * FROM " + FPSDBTables.TABLE_PERSON_RULES + " where groupId = " + groupId + " AND isDeleted = 0 AND card_type_id = " + cardType;
        Util.LoggingQueue(contextValue, "FPSDBHelper ", "PERSON findByGroupAndCardType selectQuery -> " + selectQuery);
        //SELECT b FROM PersonBasedRule b where b.group=?1 and b.cardType=?2 and b.isDeleted= false
        PersonBasedRule products = new PersonBasedRule();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            products = new PersonBasedRule(cursor);
        }
        cursor.close();
        if (cursor.getCount() > 0) {
            return products;
        } else {
            return null;
        }
    }

    public PersonBasedRule findByGroupWithoutCardType(long groupId) {
        // Util.LoggingQueue(contextValue, "FPSDBHelper ", "findByGroupWithoutCardType() called groupId -> " + groupId );
        //SELECT  * FROM  person_rules where groupId = 1 AND card_type_id IS NULL AND isDeleted = 0
        String selectQuery = "SELECT  * FROM " + FPSDBTables.TABLE_PERSON_RULES + " where groupId = " + groupId + " AND card_type_id IS NULL AND isDeleted = 0 ";
        Util.LoggingQueue(contextValue, "FPSDBHelper ", "PERSON findByGroupWithoutCardType() selectQuery -> " + selectQuery);
        //SELECT b FROM PersonBasedRule b where b.group=?1 and b.cardType=?2 and b.isDeleted= false
        PersonBasedRule products = new PersonBasedRule();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            products = new PersonBasedRule(cursor);
        }
        cursor.close();
        return products;
    }

    public List<SplEntitlementRule> findByGroupAndCardTypeAndDistrict(long groupId, String cardTypeId, long districtId) {

        /*
        SELECT b FROM SplEntitlementRule b where b.group.id =?1 and b.cardTypeId=?2 and b.districtId=?3 and b.isDeleted=false
			splEntitlementRules=splEntitlementRepo.findByGroupAndCardTypeAndDistrict(group.getId(),cardType.getId(), district.getId());

        */
        //   Util.LoggingQueue(contextValue, "FPSDBHelper ", "findByGroupAndCardTypeAndDistrict() called groupId -> " + groupId  + " cardTypeId ->" +cardTypeId + " districtId ->"+ districtId);

        /*
SELECT  * FROM special_rules where groupId = 6
                AND  card_type_id = 1 AND district_id = 14 AND isDeleted = 0

        */
        String selectQuery = "SELECT  * FROM " + FPSDBTables.TABLE_SPECIAL_RULES + " where groupId = " + groupId
                + " AND " + FPSDBConstants.KEY_RULES_CARD_TYPE_ID + " = '" + cardTypeId + "' AND district_id = " +
                districtId +
                " AND isDeleted = 0";
        Util.LoggingQueue(contextValue, "FPSDBHelper ", "SPECIAL findByGroupAndCardTypeAndDistrict() QUERY ->" + selectQuery);
        List<SplEntitlementRule> special = new ArrayList<>();
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            special.add(new SplEntitlementRule(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        if (cursor.getCount() > 0) {
            return special;
        } else {
            return null;
        }
    }

    public List<SplEntitlementRule> findByGroupAndCardTypeAndVillage(long groupId, String cardTypeId, long village_id) {
        String selectQuery = "SELECT  * FROM " + FPSDBTables.TABLE_SPECIAL_RULES + " where groupId = " + groupId
                + " AND " + FPSDBConstants.KEY_RULES_CARD_TYPE_ID + " = '" + cardTypeId + "' AND village_id = " +
                village_id +
                " AND isDeleted = 0";
        Util.LoggingQueue(contextValue, "FPSDBHelper ", "SPECIAL QUERY With village_id ->" + selectQuery);
        List<SplEntitlementRule> special = new ArrayList<>();
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            special.add(new SplEntitlementRule(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        if (cursor.getCount() > 0) {
            return special;
        } else {
            return null;
        }
    }

    public List<SplEntitlementRule> findByGroupAndCardTypeAndTaluk(long groupId, String cardTypeId, long taluk_id) {
        String selectQuery = "SELECT  * FROM " + FPSDBTables.TABLE_SPECIAL_RULES + " where groupId = " + groupId
                + " AND " + FPSDBConstants.KEY_RULES_CARD_TYPE_ID + " = '" + cardTypeId + "' AND taluk_id = " +
                taluk_id +
                " AND isDeleted = 0";
        Util.LoggingQueue(contextValue, "FPSDBHelper ", "SPECIAL QUERY With taluk_id ->" + selectQuery);
        List<SplEntitlementRule> special = new ArrayList<>();
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            special.add(new SplEntitlementRule(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        if (cursor.getCount() > 0) {
            return special;
        } else {
            return null;
        }
    }

    public List<SplEntitlementRule> findByGroupAndCardTypeAndNullDistrict(long groupId, String cardTypeId) {
        String selectQuery = "SELECT  * FROM " + FPSDBTables.TABLE_SPECIAL_RULES + " where groupId = " + groupId
                + " AND " + FPSDBConstants.KEY_RULES_CARD_TYPE_ID + " = '" + cardTypeId + "' AND district_id IS NULL " +
                " AND isDeleted = 0";
        Log.e("FPSDBHelper ", "SPECIAL findByGroupAndCardTypeAndNullDistrict() QUERY ->" + selectQuery);
        List<SplEntitlementRule> region = new ArrayList<>();
        Cursor cursor = database.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            region.add(new SplEntitlementRule(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        Log.e("FPSDBHelper ", "SPECIAL findByGroupAndCardTypeAndNullDistrict() region" + region.toString());
        if (cursor.getCount() > 0) {
            return region;
        } else {
            return null;
        }
    }

    public void delete_inactive_beneficary(String cardtype_id) {
        try {
            //  String sqlquery = "update beneficiary SET active='0'where fps_id NOT IN (select fps_id from active_fps)";
            String sqlquery = "Delete from beneficiary where card_type_id NOT IN (" + cardtype_id + ") OR num_of_cylinder = 2";
            database.beginTransaction();
            database.execSQL(sqlquery);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("DBhelper", "Exception while Updating the Inactive fps store" + e.toString());
        }
        database.endTransaction();
    }
}

