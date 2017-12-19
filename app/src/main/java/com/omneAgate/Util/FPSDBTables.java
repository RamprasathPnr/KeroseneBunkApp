package com.omneAgate.Util;

import com.omneAgate.Util.Constants.FPSDBConstants;

/**
 * Created for tables creation
 */
public class FPSDBTables {

    //Key for id in tables
    public final static String KEY_ID = "_id";

    // fpsUsers table name
    public static final String TABLE_USERS = "users";

    // Products table name
    public static final String TABLE_PRODUCTS = "products";

    // Entitlement Rules table name
    public static final String TABLE_ENTITLEMENT_RULES = "entitlement_rules";

    // Card Images Address table name
    public static final String TABLE_SMS_PROVIDER = "sms_provider";

    // Entitlement Rules table name
    public static final String TABLE_SPECIAL_RULES = "special_rules";

    public static final String TABLE_CLOSE_SALE = "close_sale";

    // Person Rules table name
    public static final String TABLE_PERSON_RULES = "person_rules";

    // Person Rules table name
    public static final String TABLE_REGION_RULES = "region_rules";

    // beneficiary table name
    public static final String TABLE_BENEFICIARY = "beneficiary";

    public static final String TABLE_BENEFICIARY_TEMP = "beneficiary_temp";

    public static final String TABLE_BENEFICIARY_IN = "beneficiary_in";


    public static final String TABLE_PRODUCT_PRICE_OVERRIDE = "product_price_override";

    public static final String TABLE_PRODUCT_GROUP = "product_group";

    // Beneficiary Member table name
    public static final String TABLE_BENEFICIARY_MEMBER = "beneficiary_member";

    public static final String TABLE_BENEFICIARY_MEMBER_IN = "beneficiary_member_in";

    // STOCK table name
    public static final String TABLE_STOCK = "stock";

    public static final String TABLE_UPGRADE = "table_upgrade";

    // STOCK table name
    public static final String TABLE_REGISTRATION = "registration";

    // CardType table name
    public static final String TABLE_CARD_TYPE = "card_type";

    public static final String TABLE_CONFIG_TABLE = "configuration";

    // CardType table name
    public static final String TABLE_BILL_ITEM = "bill_item";

    public static final String TABLE_LOGIN_HISTORY = "login_history";

    // bill table name
    public static final String TABLE_BILL = "bill";

    //Lanuage Database Table for Error Message
    public static final String TABLE_LANGUAGE = "error_messages";

    public static final String TABLE_STOCK_HISTORY = "stock_history";

    public static final String TABLE_STOCK_ALLOTMENT_DETAILS = "stock_allotment_details";

    //Godown Stock inward table
    public static final String TABLE_FPS_STOCK_INWARD = "stock_inward";

    //Godown Stock inward table
    public static final String TABLE_FPS_ADVANCE_STOCK_INWARD = "advance_stock_inward";

    public static final String TABLE_FPS_MIGRATION_IN = "migration_in";

    public static final String TABLE_FPS_MIGRATION_OUT = "migration_out";

    //Fps Intent Request Product Table
    public static final String TABLE_FPS_INTENT_REQUEST_PRODUCT = "indent_request_product";

    //Fps Manual Stock inward table
    public static final String TABLE_FPS_MANUAL_STOCK_INWARD = "manual_stock_inward";

    //Fps Manual Stock Inward Product Table
    public static final String TABLE_FPS_MANUAL_STOCK_INWARD_PRODUCT_TABLE = "manual_stock_inward_product";

    //Roll future table
    public static final String TABLE_ROLE_FEATURE = "role_feature";

    public static final String TABLE_ACTIVE_FPS="active_associated_fps";

    public static final String TABLE_GIVE_IT_UP_REQUEST="give_it_up_request";

    public static final String TABLE_GIVE_IT_UP_REQUEST_DETAIL="give_it_up_request_detail";

    // Users table with username and passwordHash
    public static final String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
            + KEY_ID + " INTEGER PRIMARY KEY ,gen_code VARCHAR(20)," + FPSDBConstants.KEY_USERS_NAME + " VARCHAR(150) NOT NULL UNIQUE," + FPSDBConstants.KEY_USERS_ID + " VARCHAR(150) NOT NULL,"
            + FPSDBConstants.KEY_USERS_PASS_HASH + " VARCHAR(150)," + FPSDBConstants.KEY_USERS_FPS_ID + " VARCHAR(30),"
            + FPSDBConstants.KEY_USERS_CONTACT_PERSON + "  VARCHAR(30)," + FPSDBConstants.KEY_USERS_PHONE_NUMBER + " VARCHAR(15), "
            + FPSDBConstants.KEY_USERS_ADDRESS_LINE1 + " VARCHAR(60)," + FPSDBConstants.KEY_USERS_ADDRESS_LINE2 + " VARCHAR(60),"
            + "village_name VARCHAR(30),taluk_name VARCHAR(30),district_name VARCHAR(30),device_sim_no VARCHAR(30),agency_name VARCHAR(30),agency_code VARCHAR(30),"
            + "operation_closing_time VARCHAR(30),operation_opening_time VARCHAR(30),village_code VARCHAR(30),taluk_code VARCHAR(30),district_code VARCHAR(30),last_login_time VARCHAR(30),"
            + "fps_category VARCHAR(30),fps_type VARCHAR(30),encrypted_password VARCHAR(300),"
            + FPSDBConstants.KEY_USERS_ADDRESS_LINE3 + " VARCHAR(30), " + FPSDBConstants.KEY_USERS_ENTITLE_CLASSIFICATION + " VARCHAR(30), " + FPSDBConstants.KEY_USERS_PROFILE + " VARCHAR(150),"
            + FPSDBConstants.KEY_USERS_DISTRICT_ID + " INTEGER," + FPSDBConstants.KEY_USERS_TALUK_ID + " INTEGER, "
            + FPSDBConstants.KEY_USERS_VILLAGE_ID + " INTEGER," + FPSDBConstants.KEY_USERS_IS_ACTIVE + " INTEGER," + FPSDBConstants.KEY_USERS_CODE + " VARCHAR(15))";


    // Products  table with unique product name and unique product code
    public static final String CREATE_PRODUCTS_TABLE = "CREATE TABLE " + TABLE_PRODUCTS + "("
            + KEY_ID + " INTEGER PRIMARY KEY," + FPSDBConstants.KEY_PRODUCT_NAME + " VARCHAR(150) NOT NULL UNIQUE,"
            + FPSDBConstants.KEY_PRODUCT_PRICE + " DOUBLE NOT NULL," + FPSDBConstants.KEY_PRODUCT_UNIT + " VARCHAR(150) NOT NULL,"
            + FPSDBConstants.KEY_PRODUCT_CODE + " VARCHAR(150) NOT NULL UNIQUE,isDeleted INTEGER,groupId INTEGER," + FPSDBConstants.KEY_LPRODUCT_UNIT + " VARCHAR(150) NOT NULL,"
            + FPSDBConstants.KEY_LPRODUCT_NAME + " VARCHAR(250) NOT NULL UNIQUE," + FPSDBConstants.KEY_NEGATIVE_INDICATOR + " INTEGER NOT NULL,"
            + FPSDBConstants.KEY_PRODUCT_MODIFIED_DATE + " INTEGER," + FPSDBConstants.KEY_MODIFIED_BY + " VARCHAR(150)," + FPSDBConstants.KEY_CREATED_DATE + " INTEGER,"
            + FPSDBConstants.KEY_CREATED_BY + " VARCHAR(150)" + ")";

    public static final String CREATE_TABLE_LOGIN_HISTORY = "CREATE TABLE " + TABLE_LOGIN_HISTORY + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,login_time VARCHAR(60),login_type VARCHAR(50),user_id INTEGER,logout_time VARCHAR(60),logout_type VARCHAR(50)," +
            "bunk_id INTEGER,transaction_id VARCHAR(50),created_time INTEGER,is_sync INTEGER,is_logout_sync INTEGER)";

    // Entitlement Master  table
    public static final String CREATE_ENTITLEMENT_RULES_TABLE = "CREATE TABLE " + TABLE_ENTITLEMENT_RULES + "("
            + KEY_ID + " INTEGER PRIMARY KEY," + FPSDBConstants.KEY_RULES_PRODUCT_ID + " INTEGER NOT NULL,"
            + FPSDBConstants.KEY_RULES_CARD_TYPE + " INTEGER NOT NULL," + FPSDBConstants.KEY_RULES_IS_PERSON + " INTEGER NOT NULL,"
            + FPSDBConstants.KEY_SPECIAL_OVERRIDE + " INTEGER NOT NULL," + FPSDBConstants.KEY_SPECIAL_MINIMUM + " INTEGER NOT NULL," + FPSDBConstants.KEY_RULES_IS_CALC + " INTEGER NOT NULL,"
            + FPSDBConstants.KEY_RULES_IS_REGION + " INTEGER NOT NULL,isDeleted INTEGER,groupId INTEGER," + FPSDBConstants.KEY_RULES_HAS_SPECIAL + " INTEGER NOT NULL,"
            + FPSDBConstants.KEY_RULES_QUANTITY + " INTEGER NOT NULL)";

    // Entitlement Master  table
   /* public static final String CREATE_SPECIAL_RULES_TABLE = "CREATE TABLE " + TABLE_SPECIAL_RULES + "("
            + KEY_ID + " INTEGER PRIMARY KEY," + FPSDBConstants.KEY_RULES_PRODUCT_ID + " INTEGER NOT NULL," + FPSDBConstants.KEY_RULES_CARD_TYPE_ID + " INTEGER NOT NULL,"
            + FPSDBConstants.KEY_SPECIAL_DISTRICT + " INTEGER ," + FPSDBConstants.KEY_SPECIAL_TALUK + " INTEGER ,"
            + FPSDBConstants.KEY_SPECIAL_VILLAGE + " INTEGER ,"
            + FPSDBConstants.KEY_SPECIAL_IS_MUNICIPALITY + " INTEGER NOT NULL," + FPSDBConstants.KEY_SPECIAL_IS_ADD + " INTEGER NOT NULL,"
            + FPSDBConstants.KEY_SPECIAL_IS_CITY_HEAD + " INTEGER NOT NULL,"
            + FPSDBConstants.KEY_SPECIAL_QUANTITY + " DOUBLE NOT NULL,isDeleted INTEGER,groupId INTEGER,"
            + FPSDBConstants.KEY_SPECIAL_CYLINDER + " INTEGER NOT NULL," + FPSDBConstants.KEY_SPECIAL_IS_TALUK + " INTEGER NOT NULL,"
            + FPSDBConstants.KEY_SPECIAL_IS_CITY + " INTEGER NOT NULL)";*/

    public static final String CREATE_SPECIAL_RULES_TABLE = "CREATE TABLE " + TABLE_SPECIAL_RULES + "("
            + KEY_ID + " INTEGER PRIMARY KEY," + FPSDBConstants.KEY_RULES_PRODUCT_ID + " INTEGER ," + FPSDBConstants.KEY_RULES_CARD_TYPE_ID + " INTEGER ,"
            + FPSDBConstants.KEY_SPECIAL_DISTRICT + " INTEGER ," + FPSDBConstants.KEY_SPECIAL_TALUK + " INTEGER ,"
            + FPSDBConstants.KEY_SPECIAL_VILLAGE + " INTEGER ,"
            + FPSDBConstants.KEY_SPECIAL_IS_MUNICIPALITY + " INTEGER ," + FPSDBConstants.KEY_SPECIAL_IS_ADD + " INTEGER ,"
            + FPSDBConstants.KEY_SPECIAL_IS_CITY_HEAD + " INTEGER ,"
            + FPSDBConstants.KEY_SPECIAL_QUANTITY + " DOUBLE ,isDeleted INTEGER,groupId INTEGER,"
            + FPSDBConstants.KEY_SPECIAL_CYLINDER + " INTEGER ," + FPSDBConstants.KEY_SPECIAL_IS_TALUK + " INTEGER ,"
            + FPSDBConstants.KEY_SPECIAL_IS_CITY + " INTEGER , " +
            " hill_area INTEGER , " +
            " spl_area INTEGER , " +
            " town_panchayat INTEGER , " +
            " village_panchayat INTEGER )";

    public static final String CREATE_SPECIAL_RULES_TABLE_TEMP = "CREATE TABLE special_rules_temp" +  "("
            + KEY_ID + " INTEGER PRIMARY KEY," + FPSDBConstants.KEY_RULES_PRODUCT_ID + " INTEGER ," + FPSDBConstants.KEY_RULES_CARD_TYPE_ID + " INTEGER ,"
            + FPSDBConstants.KEY_SPECIAL_DISTRICT + " INTEGER ," + FPSDBConstants.KEY_SPECIAL_TALUK + " INTEGER ,"
            + FPSDBConstants.KEY_SPECIAL_VILLAGE + " INTEGER ,"
            + FPSDBConstants.KEY_SPECIAL_IS_MUNICIPALITY + " INTEGER ," + FPSDBConstants.KEY_SPECIAL_IS_ADD + " INTEGER ,"
            + FPSDBConstants.KEY_SPECIAL_IS_CITY_HEAD + " INTEGER ,"
            + FPSDBConstants.KEY_SPECIAL_QUANTITY + " DOUBLE ,isDeleted INTEGER,groupId INTEGER,"
            + FPSDBConstants.KEY_SPECIAL_CYLINDER + " INTEGER ," + FPSDBConstants.KEY_SPECIAL_IS_TALUK + " INTEGER ,"
            + FPSDBConstants.KEY_SPECIAL_IS_CITY + " INTEGER  , " +
            " hill_area INTEGER , " +
            " spl_area INTEGER , " +
            " town_panchayat INTEGER , " +
            " village_panchayat INTEGER )";

    // Entitlement Master  table
    public static final String CREATE_PERSON_RULES_TABLE = "CREATE TABLE " + TABLE_PERSON_RULES + "("
            + KEY_ID + " INTEGER PRIMARY KEY," + FPSDBConstants.KEY_RULES_PRODUCT_ID + " INTEGER NOT NULL,"
            + FPSDBConstants.KEY_PERSON_MIN + " DOUBLE NOT NULL,isDeleted INTEGER,groupId INTEGER UNIQUE," + FPSDBConstants.KEY_PERSON_MAX + " DOUBLE NOT NULL,"
            + FPSDBConstants.KEY_PERSON_CHILD + " DOUBLE NOT NULL,"
            + FPSDBConstants.KEY_PERSON_ADULT + " DOUBLE NOT NULL)";


    // Close Sale Master  table
  //  public static final String CREATE_CLOSE_SALE_TABLE = "CREATE TABLE " + TABLE_CLOSE_SALE + "("
   //         + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + " created_date VARCHAR(100),amount DOUBLE,total_bills INTEGER)";

    public static final String CREATE_CLOSE_SALE_PRODUCT_TABLE = "CREATE TABLE IF NOT EXISTS close_sale_product("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,productId INTEGER,dateOfTxn INTEGER,transactionId INTEGER,totalCost VARCHAR(10),totalQuantity VARCHAR(10),"
            + FPSDBConstants.KEY_STOCK_HISTORY_OPEN_BALANCE + " DOUBLE," + FPSDBConstants.KEY_STOCK_HISTORY_CLOSE_BALANCE + " DOUBLE,inward DOUBLE" + ")";

    public static final String CREATE_CLOSE_SALE_TABLE = "CREATE TABLE IF NOT EXISTS close_sale("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,numofTrans INTEGER,transactionId INTEGER,totalSaleCost DOUBLE,dateOfTxn INTEGER,"
            +  "isServerAdded INTEGER" + ")";
    // Close Sale Master  table
    public static final String CREATE_TABLE_PRODUCT_GROUP = "CREATE TABLE " + TABLE_PRODUCT_GROUP + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,group_id INTEGER,name VARCHAR(100),product_id INTEGER,is_deleted INTEGER,UNIQUE (product_id,group_id)  ON CONFLICT REPLACE) ";

    // Close Sale Master  table
    public static final String CREATE_TABLE_PRODUCT_OVERRIDE = "CREATE TABLE " + TABLE_PRODUCT_PRICE_OVERRIDE + "("
            + KEY_ID + " INTEGER PRIMARY KEY ,card_type VARCHAR(3),card_type_id INTEGER,percentage DOUBLE,product_id INTEGER,is_deleted INTEGER,UNIQUE (product_id,card_type_id)  ON CONFLICT REPLACE)";


    // Entitlement Master  table
    public static final String CREATE_REGION_RULES_TABLE = "CREATE TABLE " + TABLE_REGION_RULES + "("
            + KEY_ID + " INTEGER PRIMARY KEY," + FPSDBConstants.KEY_RULES_PRODUCT_ID + " INTEGER NULL,"
            + FPSDBConstants.KEY_PERSON_CYLINDER + " INTEGER NOT NULL," + FPSDBConstants.KEY_PERSON_QUANTITY + " DOUBLE NOT NULL,"
            + FPSDBConstants.KEY_PERSON_TALUK + " INTEGER NOT NULL,isDeleted INTEGER,groupId INTEGER,"
            + FPSDBConstants.KEY_PERSON_CITY + " INTEGER NOT NULL,"
            + FPSDBConstants.KEY_PERSON_MUNICIPALITY + " INTEGER NOT NULL,"
            + FPSDBConstants.KEY_PERSON_HEAD + " INTEGER NOT NULL)";


    public static final String CREATE_BENEFICIARY_TABLE = "CREATE TABLE " + TABLE_BENEFICIARY + "("
            + KEY_ID + " INTEGER PRIMARY KEY," + FPSDBConstants.KEY_BENEFICIARY_UFC + " VARCHAR(50),aRegister INTEGER,"
            + FPSDBConstants.KEY_BENEFICIARY_TIN + " VARCHAR(150)," + FPSDBConstants.KEY_BENEFICIARY_FPS_ID + " INTEGER NOT NULL,"
            + FPSDBConstants.KEY_BENEFICIARY_OLD_RATION + " VARCHAR(150)," + FPSDBConstants.KEY_BENEFICIARY_CYLINDER_NO + " INTEGER NOT NULL,"
            + FPSDBConstants.KEY_BENEFICIARY_ADULT_NO + " INTEGER NOT NULL," + FPSDBConstants.KEY_BENEFICIARY_CHILD_NO + " INTEGER NOT NULL,"
            + FPSDBConstants.KEY_BENEFICIARY_MOBILE + " VARCHAR(20) UNIQUE ," + FPSDBConstants.KEY_BENEFICIARY_VILLAGE_ID + " INTEGER,"
            + FPSDBConstants.KEY_BENEFICIARY_TALUK_ID + " INTEGER," + FPSDBConstants.KEY_BENEFICIARY_DISTRICT_ID + " INTEGER,"
            + FPSDBConstants.KEY_BENEFICIARY_CARD_TYPE_ID + " VARCHAR(10)," + FPSDBConstants.KEY_BENEFICIARY_STATE_ID + " INTEGER,"
            + FPSDBConstants.KEY_BENEFICIARY_CREATED_DATE + " INTEGER," + FPSDBConstants.KEY_BENEFICIARY_MODIFIED_BY + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MODIFIED_DATE + " INTEGER," + FPSDBConstants.KEY_BENEFICIARY_ACTIVE + " INTEGER NOT NULL" + ")";




    public static final String CREATE_BENEFICIARY_TABLE_TEMP = "CREATE TABLE " + TABLE_BENEFICIARY_TEMP + "("
            + KEY_ID + " INTEGER PRIMARY KEY," + FPSDBConstants.KEY_BENEFICIARY_UFC + " VARCHAR(50),aRegister INTEGER,"
            + FPSDBConstants.KEY_BENEFICIARY_TIN + " VARCHAR(150)," + FPSDBConstants.KEY_BENEFICIARY_FPS_ID + " INTEGER NOT NULL,"
            + FPSDBConstants.KEY_BENEFICIARY_OLD_RATION + " VARCHAR(150) UNIQUE," + FPSDBConstants.KEY_BENEFICIARY_CYLINDER_NO + " INTEGER NOT NULL,"
            + FPSDBConstants.KEY_BENEFICIARY_ADULT_NO + " INTEGER NOT NULL," + FPSDBConstants.KEY_BENEFICIARY_CHILD_NO + " INTEGER NOT NULL,"
            + FPSDBConstants.KEY_BENEFICIARY_MOBILE + " VARCHAR(20)," + FPSDBConstants.KEY_BENEFICIARY_VILLAGE_ID + " INTEGER,"
            + FPSDBConstants.KEY_BENEFICIARY_TALUK_ID + " INTEGER," + FPSDBConstants.KEY_BENEFICIARY_DISTRICT_ID + " INTEGER,"
            + FPSDBConstants.KEY_BENEFICIARY_CARD_TYPE_ID + " VARCHAR(10)," + FPSDBConstants.KEY_BENEFICIARY_STATE_ID + " INTEGER,"
            + FPSDBConstants.KEY_BENEFICIARY_CREATED_DATE + " INTEGER," + FPSDBConstants.KEY_BENEFICIARY_MODIFIED_BY + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MODIFIED_DATE + " INTEGER," + FPSDBConstants.KEY_BENEFICIARY_ACTIVE + " INTEGER NOT NULL" + ")";





    public static final String CREATE_BENEFICIARY_TABLE_IN = "CREATE TABLE " + TABLE_BENEFICIARY_IN + "("
            + KEY_ID + " INTEGER PRIMARY KEY," + FPSDBConstants.KEY_BENEFICIARY_UFC + " VARCHAR(50),aRegister INTEGER,"
            + FPSDBConstants.KEY_BENEFICIARY_TIN + " VARCHAR(150)," + FPSDBConstants.KEY_BENEFICIARY_FPS_ID + " INTEGER NOT NULL,"
            + FPSDBConstants.KEY_BENEFICIARY_OLD_RATION + " VARCHAR(150) UNIQUE," + FPSDBConstants.KEY_BENEFICIARY_CYLINDER_NO + " INTEGER NOT NULL,"
            + FPSDBConstants.KEY_BENEFICIARY_ADULT_NO + " INTEGER NOT NULL," + FPSDBConstants.KEY_BENEFICIARY_CHILD_NO + " INTEGER NOT NULL,"
            + FPSDBConstants.KEY_BENEFICIARY_MOBILE + " VARCHAR(20) UNIQUE ," + FPSDBConstants.KEY_BENEFICIARY_VILLAGE_ID + " INTEGER,"
            + FPSDBConstants.KEY_BENEFICIARY_TALUK_ID + " INTEGER," + FPSDBConstants.KEY_BENEFICIARY_DISTRICT_ID + " INTEGER,"
            + FPSDBConstants.KEY_BENEFICIARY_CARD_TYPE_ID + " VARCHAR(10)," + FPSDBConstants.KEY_BENEFICIARY_STATE_ID + " INTEGER,"
            + FPSDBConstants.KEY_BENEFICIARY_CREATED_DATE + " INTEGER," + FPSDBConstants.KEY_BENEFICIARY_MODIFIED_BY + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MODIFIED_DATE + " INTEGER," + FPSDBConstants.KEY_BENEFICIARY_ACTIVE + " INTEGER NOT NULL" + ")";


    // Beneficiary MEMBER
    public static final String CREATE_BENEFICIARY_MEMBER_TABLE = "CREATE TABLE " + TABLE_BENEFICIARY_MEMBER + "("
            + KEY_ID + " INTEGER PRIMARY KEY," + FPSDBConstants.KEY_BENEFICIARY_MEMBER_TIN + " VARCHAR(150), "
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_UID + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_LNAME + " VARCHAR(100),"
            + FPSDBConstants.KEY_BENEFICIARY_UFC + " VARCHAR(100) ,"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_EID + " VARCHAR(150)," + FPSDBConstants.KEY_BENEFICIARY_MEMBER_FIRST_NAME + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_MIDDLE_NAME + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_LAST_NAME + " VARCHAR(150)," + FPSDBConstants.KEY_BENEFICIARY_MEMBER_FATHER_NAME + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_MOTHER_NAME + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_GENDER + " VARCHAR(1)," + FPSDBConstants.KEY_BENEFICIARY_MEMBER_PERMANENT_ADDRESS + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_TEMP_ADDRESS + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_CREATED_BY + " VARCHAR(150), " + FPSDBConstants.KEY_BENEFICIARY_MEMBER_CREATED_DATE + " INTEGER,"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_MODIFIED_BY + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_MODIFIED_DATE + " INTEGER," + FPSDBConstants.KEY_BENEFICIARY_MEMBER_NAME + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_RESIDENT_ID + " VARCHAR(1),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_REL_NAME + " VARCHAR(150)," + FPSDBConstants.KEY_BENEFICIARY_MEMBER_GENDER_ID + " VARCHAR(1),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_DOB + " VARCHAR(30) ,"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_DOB_TYPE + " VARCHAR(150)," + FPSDBConstants.KEY_BENEFICIARY_MEMBER_STATUS_ID + " VARCHAR(1),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_EDU_NAME + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_OCCUPATION_NAME + " VARCHAR(150)," + FPSDBConstants.KEY_BENEFICIARY_MEMBER_FATHER_CODE + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_FATHER_NM + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_MOTHER_CODE + " VARCHAR(150)," + FPSDBConstants.KEY_BENEFICIARY_MEMBER_MOTHER_NM + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_SPOUSE_CODE + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_SPOUSE_NM + " VARCHAR(150)," + FPSDBConstants.KEY_BENEFICIARY_MEMBER_NAT_NAME + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_ADDRESS_LINE_1 + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_ADDRESS_LINE_2 + " VARCHAR(150)," + FPSDBConstants.KEY_BENEFICIARY_MEMBER_ADDRESS_LINE_3 + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_ADDRESS_LINE_4 + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_LADDRESS_LINE_1 + " VARCHAR(150)," + FPSDBConstants.KEY_BENEFICIARY_MEMBER_LADDRESS_LINE_2 + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_LADDRESS_LINE_3 + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_LADDRESS_LINE_4 + " VARCHAR(150)," + FPSDBConstants.KEY_BENEFICIARY_MEMBER_LADDRESS_LINE_5 + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_ADDRESS_LINE_5 + " VARCHAR(150)," + FPSDBConstants.KEY_BENEFICIARY_MEMBER_PIN_CODE + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_DURATION_IN_YEAR + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_P_ADDRESS_LINE_1 + " VARCHAR(150)," + FPSDBConstants.KEY_BENEFICIARY_MEMBER_P_ADDRESS_LINE_2 + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_P_ADDRESS_LINE_3 + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_LP_ADDRESS_LINE_1 + " VARCHAR(150)," + FPSDBConstants.KEY_BENEFICIARY_MEMBER_LP_ADDRESS_LINE_2 + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_LP_ADDRESS_LINE_3 + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_LP_ADDRESS_LINE_4 + " VARCHAR(150)," + FPSDBConstants.KEY_BENEFICIARY_MEMBER_LP_ADDRESS_LINE_5 + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_P_ADDRESS_LINE_4 + " VARCHAR(150)," + FPSDBConstants.KEY_BENEFICIARY_MEMBER_P_ADDRESS_LINE_5 + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_P_PIN_CODE + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_DATE_DATA_ENTERED + " INTEGER," + FPSDBConstants.KEY_BENEFICIARY_MEMBER_ALIVE_STATUS + " INTEGER ,"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_IS_ADULT + " INTEGER " + ")";

    // Beneficiary MEMBER
    public static final String CREATE_BENEFICIARY_MEMBER_TABLE_IN = "CREATE TABLE " + TABLE_BENEFICIARY_MEMBER_IN + "("
            + KEY_ID + " INTEGER PRIMARY KEY," + FPSDBConstants.KEY_BENEFICIARY_MEMBER_TIN + " VARCHAR(150), "
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_UID + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_LNAME + " VARCHAR(100),"
            + FPSDBConstants.KEY_BENEFICIARY_UFC + " VARCHAR(100) ,"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_EID + " VARCHAR(150)," + FPSDBConstants.KEY_BENEFICIARY_MEMBER_FIRST_NAME + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_MIDDLE_NAME + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_LAST_NAME + " VARCHAR(150)," + FPSDBConstants.KEY_BENEFICIARY_MEMBER_FATHER_NAME + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_MOTHER_NAME + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_GENDER + " VARCHAR(1)," + FPSDBConstants.KEY_BENEFICIARY_MEMBER_PERMANENT_ADDRESS + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_TEMP_ADDRESS + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_CREATED_BY + " VARCHAR(150), " + FPSDBConstants.KEY_BENEFICIARY_MEMBER_CREATED_DATE + " INTEGER,"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_MODIFIED_BY + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_MODIFIED_DATE + " INTEGER," + FPSDBConstants.KEY_BENEFICIARY_MEMBER_NAME + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_RESIDENT_ID + " VARCHAR(1),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_REL_NAME + " VARCHAR(150)," + FPSDBConstants.KEY_BENEFICIARY_MEMBER_GENDER_ID + " VARCHAR(1),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_DOB + " VARCHAR(30) ,"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_DOB_TYPE + " VARCHAR(150)," + FPSDBConstants.KEY_BENEFICIARY_MEMBER_STATUS_ID + " VARCHAR(1),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_EDU_NAME + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_OCCUPATION_NAME + " VARCHAR(150)," + FPSDBConstants.KEY_BENEFICIARY_MEMBER_FATHER_CODE + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_FATHER_NM + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_MOTHER_CODE + " VARCHAR(150)," + FPSDBConstants.KEY_BENEFICIARY_MEMBER_MOTHER_NM + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_SPOUSE_CODE + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_SPOUSE_NM + " VARCHAR(150)," + FPSDBConstants.KEY_BENEFICIARY_MEMBER_NAT_NAME + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_ADDRESS_LINE_1 + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_ADDRESS_LINE_2 + " VARCHAR(150)," + FPSDBConstants.KEY_BENEFICIARY_MEMBER_ADDRESS_LINE_3 + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_ADDRESS_LINE_4 + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_LADDRESS_LINE_1 + " VARCHAR(150)," + FPSDBConstants.KEY_BENEFICIARY_MEMBER_LADDRESS_LINE_2 + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_LADDRESS_LINE_3 + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_LADDRESS_LINE_4 + " VARCHAR(150)," + FPSDBConstants.KEY_BENEFICIARY_MEMBER_LADDRESS_LINE_5 + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_ADDRESS_LINE_5 + " VARCHAR(150)," + FPSDBConstants.KEY_BENEFICIARY_MEMBER_PIN_CODE + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_DURATION_IN_YEAR + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_P_ADDRESS_LINE_1 + " VARCHAR(150)," + FPSDBConstants.KEY_BENEFICIARY_MEMBER_P_ADDRESS_LINE_2 + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_P_ADDRESS_LINE_3 + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_LP_ADDRESS_LINE_1 + " VARCHAR(150)," + FPSDBConstants.KEY_BENEFICIARY_MEMBER_LP_ADDRESS_LINE_2 + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_LP_ADDRESS_LINE_3 + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_LP_ADDRESS_LINE_4 + " VARCHAR(150)," + FPSDBConstants.KEY_BENEFICIARY_MEMBER_LP_ADDRESS_LINE_5 + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_P_ADDRESS_LINE_4 + " VARCHAR(150)," + FPSDBConstants.KEY_BENEFICIARY_MEMBER_P_ADDRESS_LINE_5 + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_P_PIN_CODE + " VARCHAR(150),"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_DATE_DATA_ENTERED + " INTEGER," + FPSDBConstants.KEY_BENEFICIARY_MEMBER_ALIVE_STATUS + " INTEGER ,"
            + FPSDBConstants.KEY_BENEFICIARY_MEMBER_IS_ADULT + " INTEGER " + ")";


    // card type table with card types
    public static final String CREATE_CARD_TABLE = "CREATE TABLE " + TABLE_CARD_TYPE + "(" + KEY_ID + " INTEGER PRIMARY KEY NOT NULL,"
            + FPSDBConstants.KEY_CARD_TYPE + " VARCHAR(1) NOT NULL UNIQUE," + FPSDBConstants.KEY_CARD_DESCRIPTION + " VARCHAR(150)  UNIQUE,isDeleted INTEGER )";

    // card type table with card types
    public static final String CREATE_MASTER_TABLE = "CREATE TABLE " + TABLE_CONFIG_TABLE + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
            + "name VARCHAR(50) NOT NULL UNIQUE,value VARCHAR(150)  UNIQUE" + " )";


    // card type table with card types
    public static final String CREATE_SYNC_MASTER_TABLE = "CREATE TABLE  master_first_sync (" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
            + "name VARCHAR(50) NOT NULL UNIQUE,value VARCHAR(20)" + " )";


    // Stock  table with unique bill item id, Quantity
    public static final String CREATE_STOCK_TABLE = "CREATE TABLE " + TABLE_STOCK + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT  NOT NULL," + FPSDBConstants.KEY_STOCK_FPS_ID + " INTEGER NOT NULL,"
            + FPSDBConstants.KEY_STOCK_PRODUCT_ID + " INTEGER NOT NULL UNIQUE," + FPSDBConstants.KEY_STOCK_QUANTITY + " DOUBLE NOT NULL, "
            + FPSDBConstants.KEY_STOCK_REORDER_LEVEL + " DOUBLE, " + FPSDBConstants.KEY_STOCK_EMAIL_ACTION + " INTEGER,"
            + FPSDBConstants.KEY_STOCK_SMS_ACTION + " INTEGER )";


    public static final String CREATE_FPS_STOCK_INWARD_TABLE = "CREATE TABLE " + TABLE_FPS_STOCK_INWARD + "("
            + FPSDBConstants.KEY_FPS_STOCK_INWARD_GODOWNID + " INTEGER NOT NULL," + FPSDBConstants.KEY_FPS_STOCK_INWARD_FPSID + " INTEGER NOT NULL,"
            + FPSDBConstants.KEY_FPS_STOCK_INWARD_OUTWARD_DATE + " INTEGER NOT NULL," + FPSDBConstants.KEY_FPS_STOCK_INWARD_PRODUCTID + " INTEGER NOT NULL,"
            + FPSDBConstants.KEY_FPS_STOCK_INWARD_QUANTITY + " DOUBLE NOT NULL, " + FPSDBConstants.KEY_FPS_STOCK_INWARD_UNIT + " VARCHAR(150),godown_name VARCHAR(100),godown_code VARCHAR(100),"
            + FPSDBConstants.KEY_FPS_STOCK_INWARD_BATCH_NO + " INTEGER NOT NULL,referenceNo VARCHAR(150)," + FPSDBConstants.KEY_FPS_STOCK_INWARD_IS_FPSACKSTATUS + " INTEGER(1),is_server_add INTEGER(1)," +
            FPSDBConstants.KEY_FPS_STOCK_INWARD_FPSACKDATE + " INTEGER," + FPSDBConstants.KEY_FPS_STOCK_INWARD_FPSRECEIVEIQUANTITY + " DOUBLE ,"
            + FPSDBConstants.KEY_FPS_STOCK_INWARD_CREATEDBY + " INTEGER," + FPSDBConstants.KEY_FPS_STOCK_INWARD_DELIEVERY_CHELLANID + " INTEGER, _id INTEGER UNIQUE,challanId VARCHAR(30),vehicleN0 VARCHAR(20),driverName VARCHAR(20)," +
            "month INTEGER,year INTEGER,transportName VARCHAR(20),driverMobileNumber VARCHAR(20))";


    public static final String CREATE_FPS_ADVANCE_STOCK_INWARD_TABLE = "CREATE TABLE " + TABLE_FPS_ADVANCE_STOCK_INWARD + "("
            + FPSDBConstants.KEY_FPS_STOCK_INWARD_GODOWNID + " INTEGER NOT NULL," + FPSDBConstants.KEY_FPS_STOCK_INWARD_FPSID + " INTEGER NOT NULL,"
            + FPSDBConstants.KEY_FPS_STOCK_INWARD_OUTWARD_DATE + " INTEGER NOT NULL," + FPSDBConstants.KEY_FPS_STOCK_INWARD_PRODUCTID + " INTEGER NOT NULL,"
            + FPSDBConstants.KEY_FPS_STOCK_INWARD_QUANTITY + " DOUBLE NOT NULL, " + FPSDBConstants.KEY_FPS_STOCK_INWARD_UNIT + " VARCHAR(150),godown_name VARCHAR(100),godown_code VARCHAR(100),"
            + FPSDBConstants.KEY_FPS_STOCK_INWARD_BATCH_NO + " INTEGER NOT NULL,referenceNo VARCHAR(150)," + FPSDBConstants.KEY_FPS_STOCK_INWARD_IS_FPSACKSTATUS + " INTEGER(1)," +
            FPSDBConstants.KEY_FPS_STOCK_INWARD_FPSACKDATE + " INTEGER," + FPSDBConstants.KEY_FPS_STOCK_INWARD_FPSRECEIVEIQUANTITY + " DOUBLE ,"+"month INTEGER,year INTEGER,isAdded INTEGER,"
            + FPSDBConstants.KEY_FPS_STOCK_INWARD_CREATEDBY + " INTEGER,_id INTEGER UNIQUE," + FPSDBConstants.KEY_FPS_STOCK_INWARD_DELIEVERY_CHELLANID + " INTEGER" + ")";

    // Beneficiary  table with unique UFC code, FPS id ,QRCode  and unique mobile number
    public static final String CREATE_BILL_TABLE = "CREATE TABLE " + TABLE_BILL + "("
            + FPSDBConstants.KEY_BILL_REF_ID + " INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL," + FPSDBConstants.KEY_BILL_SERVER_ID + " INTEGER," + FPSDBConstants.KEY_BILL_FPS_ID + " INTEGER ,"
            + FPSDBConstants.KEY_BILL_DATE + " INTEGER NOT NULL," + FPSDBConstants.KEY_BILL_TRANSACTION_ID + " VARCHAR(150) NOT NULL UNIQUE,"
            + FPSDBConstants.KEY_BILL_SERVER_REF_ID + " INTEGER UNIQUE," + FPSDBConstants.KEY_BILL_MODE + " VARCHAR(1) NOT NULL," + FPSDBConstants.KEY_BENEFICIARY_UFC + " VARCHAR(100),"
            + FPSDBConstants.KEY_BILL_CHANNEL + " VARCHAR(1) NOT NULL," + FPSDBConstants.KEY_BILL_STATUS + " VARCHAR(1) NOT NULL," + FPSDBConstants.KEY_BILL_BENEFICIARY + " INTEGER,"
            + FPSDBConstants.KEY_BILL_AMOUNT + " DOUBLE NOT NULL," + FPSDBConstants.KEY_BILL_TIME_MONTH
            + " INTEGER," + FPSDBConstants.KEY_BILL_CREATED_BY + " VARCHAR(150),otpId INTEGER, otpTime VARCHAR(150)," + FPSDBConstants.KEY_BILL_CREATED_DATE + " INTEGER " + ")";

    // Bill item  table with unique bill item id, Quantity ,bill item cost  and transmitted ir not
    public static final String CREATE_BILL_ITEM_TABLE = "CREATE TABLE " + TABLE_BILL_ITEM + "(" +
            FPSDBConstants.KEY_BILL_ITEM_PRODUCT_ID + " INTEGER NOT NULL," + FPSDBConstants.KEY_BILL_TRANSACTION_ID + " VARCHAR(150) NOT NULL,"
            + FPSDBConstants.KEY_BILL_ITEM_DATE + " VARCHAR(150)," + FPSDBConstants.KEY_BILL_TIME_MONTH + " INTEGER ," + FPSDBConstants.KEY_BILL_BENEFICIARY + " INTEGER NOT NULL,"
            + FPSDBConstants.KEY_BILL_ITEM_QUANTITY + " DOUBLE NOT NULL," + FPSDBConstants.KEY_BILL_ITEM_COST + "  DOUBLE NOT NULL,totalCost  DOUBLE NOT NULL," +
            " PRIMARY KEY (" + FPSDBConstants.KEY_BILL_ITEM_PRODUCT_ID + "," + FPSDBConstants.KEY_BILL_TRANSACTION_ID + ")" + ")";

    public static final String CREATE_TABLE_LANGUAGE = "CREATE TABLE " + TABLE_LANGUAGE + "("
            + KEY_ID + " INTEGER  PRIMARY KEY AUTOINCREMENT  NOT NULL," + FPSDBConstants.KEY_LANGUAGE_CODE + " INTEGER UNIQUE,"
            + FPSDBConstants.KEY_LANGUAGE_L_MESSAGE + "  VARCHAR(1000)," + FPSDBConstants.KEY_LANGUAGE_MESSAGE + " VARCHAR(1000) )";

    public static final String CREATE_TABLE_SMS_PROVIDER = "CREATE TABLE " + TABLE_SMS_PROVIDER + "("
            + KEY_ID + " INTEGER PRIMARY KEY NOT NULL," + FPSDBConstants.KEY_SMS_PROVIDER_NAME + " VARCHAR(50),"
            + FPSDBConstants.KEY_SMS_PROVIDER_NUMBER + " VARCHAR(100) UNIQUE,status VARCHAR(2)," + FPSDBConstants.KEY_SMS_PROVIDER_PREFIX + " VARCHAR(20),"
            + FPSDBConstants.KEY_SMS_PROVIDER_PREFERENCE + " VARCHAR(20))";

    public static final String CREATE_TABLE_UPGRADE = "CREATE TABLE if not exists " + TABLE_UPGRADE + "(_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,ref_id VARCHAR(30),android_old_version INTEGER,android_new_version INTEGER,"
            + "state VARCHAR(30),description VARCHAR(250),status VARCHAR(20),refer_id VARCHAR(30),created_date VARCHAR(30),server_status INTEGER,execution_time VARCHAR(30))";


    public static final String CREATE_TABLE_FPS_STOCK_HISTORY = "CREATE TABLE " + TABLE_STOCK_HISTORY + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," + FPSDBConstants.KEY_STOCK_HISTORY_PRODUCT_ID + " INTEGER,"
            + FPSDBConstants.KEY_STOCK_HISTORY_DATE + " INTEGER,"
            + FPSDBConstants.KEY_STOCK_DATE + " VARCHAR(100),"
            + FPSDBConstants.KEY_STOCK_HISTORY_OPEN_BALANCE + " DOUBLE," + FPSDBConstants.KEY_STOCK_HISTORY_CLOSE_BALANCE + " DOUBLE,"
            + FPSDBConstants.KEY_STOCK_HISTORY_CHANGE_BALANCE + " DOUBLE,"
            + FPSDBConstants.KEY_STOCK_HISTORY_ACTION + " VARCHAR(100)" + ")";


    public static final String CREATE_TABLE_ROLE_FEATURE = "CREATE TABLE " + TABLE_ROLE_FEATURE + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,isDeleted INTEGER," + FPSDBConstants.KEY_ROLE_USERID + " INTEGER,role_id INTEGER," +
            FPSDBConstants.KEY_ROLE_FEATUREID + " INTEGER NOT NULL," + FPSDBConstants.KEY_ROLE_NAME + " VARCHAR(150)," + FPSDBConstants.KEY_ROLE_PARENTID + " VARCHAR(150)," +
            FPSDBConstants.KEY_ROLE_TYPE + " VARCHAR(150),UNIQUE (" + FPSDBConstants.KEY_ROLE_USERID + ",role_id) ON CONFLICT REPLACE) ";



    public static final String CREATE_FPS_ADVANCE_MIGRATION_IN_TABLE= "CREATE TABLE " + TABLE_FPS_MIGRATION_IN+"("
            + KEY_ID + " INTEGER PRIMARY KEY NOT NULL,ration_card_number VARCHAR(150),beneficiary_id INTEGER,a_register_no  VARCHAR(150),ufc_code  VARCHAR(150) UNIQUE,month_in INTEGER,year_in INTEGER,isAdded INTEGER)";

    public static final String CREATE_FPS_ADVANCE_MIGRATION_OUT_TABLE= "CREATE TABLE " + TABLE_FPS_MIGRATION_OUT+"("
            + KEY_ID + " INTEGER PRIMARY KEY NOT NULL,beneficiary_id INTEGER UNIQUE,month_out INTEGER,year_out INTEGER,isAdded INTEGER)";


    public static final String CREATE_FPS_ADVANCE_ADJUST_TABLE= "CREATE TABLE fps_stock_adjustment ("
            + KEY_ID + " INTEGER PRIMARY KEY NOT NULL,product_id INTEGER ,quantity DOUBLE,dateOfAck VARCHAR(150),requestType VARCHAR(20),isServerAdded INTEGER,isAdjusted INTEGER)";

    public static final String CREATE_ACTIVE_FPS_TABLE=" CREATE TABLE "+ TABLE_ACTIVE_FPS + "("
            + FPSDBConstants.KEY_USERS_FPS_ID + " INTEGER PRIMARY KEY NOT NULL"
            + ")";

    public static final String CREATE_GIVE_IT_UP_TABLE =" CREATE TABLE "+ TABLE_GIVE_IT_UP_REQUEST + "("
            + FPSDBConstants.KEY_GIVEITUP_KEY_ID + " INTEGER PRIMARY KEY NOT NULL,"
            + FPSDBConstants.KEY_GIVEITUP_CHANNEl + " VARCHAR(255) NOT NULL,"
            + FPSDBConstants.KEY_GIVEITUP_CREATED_DATE + " INTEGER NOT NULL,"
            + FPSDBConstants.KEY_GIVEITUP_END_DATE + " INTEGER,"
            + FPSDBConstants.KEY_GIVEITUP_FOREVER +  " INTEGER NOT NULL,"
            + FPSDBConstants.KEY_GIVEITUP_MODIFIED_DATE + " INTEGER NOT NULL,"
            + FPSDBConstants.KEY_GIVEITUP_REFERENCE_NO + " VARCHAR(255) NOT NULL,"
            + FPSDBConstants.KEY_GIVEITUP_REVOKE_STATUS + " INTEGER NOT NULL ,"
            + FPSDBConstants.KEY_GIVEITUP_START_DATE + " INTEGER NOT NULL,"
            + FPSDBConstants.KEY_GIVEITUP_BENEFICIARY_ID + " INTEGER"
            + ")";

    public static final String CREATE_GIVE_IT_UP_DETAIL_TABLE =" CREATE TABLE "+ TABLE_GIVE_IT_UP_REQUEST_DETAIL + "("
            + FPSDBConstants.KEY_GIVEITUP_REQUEST_DETAIL_ID + " INTEGER PRIMARY KEY NOT NULL,"
            + FPSDBConstants.KEY_GIVEITUP_REQUEST_ID +" INTEGER,"
            + FPSDBConstants.KEY_GIVEITUP_GROUP_ID +" INTEGER"
            + ")";
}
