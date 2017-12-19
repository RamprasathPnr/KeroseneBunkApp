package com.omneAgate.DTO.EnumDTO;

import com.omneAgate.Bunker.R;

/**
 * Created for Roll_Feature
 */
public enum Roll_Feature {

    KB_SALES_ORDER_MENU(R.string.sales_order, R.drawable.pink_bg, R.drawable.icon_sale_order, "SaleOrderActivity"),

    KB_STOCK_MANAGEMENT_MENU(R.string.stock_management, R.drawable.green_bg, R.drawable.icon_stocks, "StockManagementActivity"),

    KB_TRANSACTIONS_MENU(R.string.transactions, R.drawable.orange_bg, R.drawable.icon_transaction, "BillSearchActivity"),

    KB_CLOSE_SALES_MENU(R.string.close_sale, R.drawable.brown_bg, R.drawable.icon_closesale, "TransactionCommodityActivity"),

    KB_OTHER_MENUS(R.string.other_menus, R.drawable.teal_bg, R.drawable.icon_othermenu, "BeneficiaryMenuActivity"),

    KB_SALES_ORDER(R.string.ration_based, R.drawable.red_bg, R.drawable.icon_ration_card, "RationCardSalesActivity"),

    KB_QR_BASED(R.string.qr_based, R.drawable.green_bg, R.drawable.img_qr_code, "QRCodeSalesActivity"),

    KB_RMN_BASED(R.string.otp_based, R.drawable.purple_bg, R.drawable.icon_otp_base, "MobileOTPOptionsActivity"),

    KB_RATION_CARD_BASED(R.string.ration_based, R.drawable.red_bg, R.drawable.icon_ration_card, "RationCardSalesActivity"),

    KB_STOCK_INWARD(R.string.stock_inward_tv, R.drawable.green_bg, R.drawable.icon_inward, "FpsStockInwardActivity"),

    KB_STOCK_STATUS(R.string.stockCheck, R.drawable.pink_bg, R.drawable.icon_stock_check, "StockCheckActivity"),

    KB_TRANSACTIONS(R.string.retrieve, R.drawable.purple_bg, R.drawable.icon_retrive_db, "RationCardSalesActivity"),

    KB_RESTORE_DB(R.string.restoration, R.drawable.pink_bg, R.drawable.icon_restore_db, "restoreDB"),

    KB_RETRIEVE_DB(R.string.retrieve, R.drawable.purple_bg, R.drawable.icon_retrive_db, "retrieveDB"),

    KB_STATISTICS(R.string.statistics, R.drawable.brown_bg, R.drawable.icon_pos_stats, "getStatistics"),

    KB_BENIFICIARY_VIEW(R.string.benedetails, R.drawable.red_bg, R.drawable.icon_ration_card, "BeneficiaryMenuActivity"),

    KB_GEO_LOCATION(R.string.geolocation, R.drawable.green_bg, R.drawable.icon_lang_lat, "findLocation"),

    KB_OPEN_STOCK(R.string.opening_stock, R.drawable.orange_bg, R.drawable.icon_opening_balance, "openStock"),

    KB_VERSION_UPGRADE(R.string.version_upgrade,R.drawable.orange_bg, R.drawable.version_upgrade,"versionUpgrade"),

    RESTORE_DB(R.string.restoration, R.drawable.pink_bg, R.drawable.icon_restore_db, "restoreDB"),

    RETRIEVE_DB(R.string.retrieve, R.drawable.purple_bg, R.drawable.icon_retrive_db, "retrieveDB"),

    STATISTICS(R.string.statistics, R.drawable.brown_bg, R.drawable.icon_pos_stats, "getStatistics"),

    BENIFICIARY_VIEW(R.string.benedetails, R.drawable.red_bg, R.drawable.icon_ration_card, "BeneficiaryMenuActivity"),

    GEO_LOCATION(R.string.geolocation, R.drawable.green_bg, R.drawable.icon_lang_lat, "findLocation"),

    OPEN_STOCK(R.string.opening_stock, R.drawable.orange_bg, R.drawable.icon_opening_balance, "openStock"),

    VERSION_UPGRADE(R.string.version_upgrade,R.drawable.orange_bg, R.drawable.version_upgrade,"versionUpgrade");

    final private int rollName;

    final private int colorCode;

    final private int background;
    final private String description;

    Roll_Feature(int rollName, int colorCode, int background, String description) {

        this.rollName = rollName;
        this.colorCode = colorCode;
        this.background = background;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public int getRollName() {
        return rollName;
    }

    public int getColorCode() {
        return colorCode;
    }

    public int getBackground() {
        return background;
    }
}
