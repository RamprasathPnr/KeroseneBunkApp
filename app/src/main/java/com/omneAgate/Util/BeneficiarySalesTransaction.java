package com.omneAgate.Util;

import android.app.Activity;
import android.util.Log;

import com.omneAgate.DTO.BeneficiaryDto;
import com.omneAgate.DTO.BillItemDto;
import com.omneAgate.DTO.EntitlementDTO;
import com.omneAgate.DTO.EntitlementMasterRuleDtod;
import com.omneAgate.DTO.KeroseneDto;
import com.omneAgate.DTO.LoginResponseDto;
import com.omneAgate.DTO.PersonBasedRule;
import com.omneAgate.DTO.QRTransactionResponseDto;
import com.omneAgate.DTO.RegionBasedRule;
import com.omneAgate.DTO.SplEntitlementRule;
import com.omneAgate.Bunker.GlobalAppState;
import com.omneAgate.Bunker.R;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Beneficiary sales transaction class
 */
public class BeneficiarySalesTransaction {

    private static final String MUNICIPALITY = "Municipality";
    //	private static final String HEAD_QUARTER="Head Quarter";
    private static final String HEAD_QUARTER = "District Head Quarter";
    //	private static  final String BELT_AREA="Belt Area";
    private static final String VILLAGE_PANCHAYAT = "Village Panchayats";
    //	private static final String TALUK="Taluk";
    private static final String TOWN_PANCHAYAT = "Township and Town Panchayats";
    private static final String HILL_AREA = "Hilly Areas";
    private static final String SPECIAL_AREA = "Special Areas";

    private static final String STAGE_VILLAGE = "STAGE_VILLAGE";
    private static final String STAGE_TALUK = "STAGE_TALUK";
    private static final String STAGE_DISTRICT = "STAGE_DISTRICT";
    private static final String STAGE_GENERIC = "STAGE_GENERIC";

    Activity context; //Context for this class
    BeneficiaryDto beneficiary;//Beneficiary of card
    String qrCode; //qr code of user card
    long fpsId;
    private List<BillItemDto> billItems; //Bill items bought


    public BeneficiarySalesTransaction(Activity activity) {
        context = activity;
        Util.changeLanguage(context, GlobalAppState.language);
    }

    /**
     * Returns the transaction response for qrCode
     *
     * @param qrCode
     */
    public QRTransactionResponseDto getBeneficiaryDetails(String qrCode,long fpsId) {

        this.qrCode = qrCode;
        this.fpsId = fpsId;
        if (getBeneficiaryAndMemberDetails()) {
            Util.LoggingQueue(context, "BeneficiaryTransaction", "Beneficiary Found for ID ->" + beneficiary.getId());
            return returnQRResponse();
        }
        Util.LoggingQueue(context, "BeneficiaryTransaction", "Invalid Beneficiary");
        return null;
    }

    /**
     * Check and return the details of beneficiary
     * <p/>
     * if beneficiary family members size is zero returns false
     * if beneficiary card is not active or blocked returns false
     * else returns true
     */
    private boolean getBeneficiaryAndMemberDetails() {
        beneficiary = FPSDBHelper.getInstance(context).beneficiaryFromOldCard(qrCode,fpsId);
        DateTime month = new DateTime();
        billItems = new ArrayList<>();
        if (beneficiary == null) {
            // Util.LoggingQueue(context, "QR TransactionResponse", "Beneficiary not found in db");
            Util.messageBar(context, context.getString(R.string.fpsBeneficiaryMismatch));
            return false;
        } else {
            if (beneficiary.isActive()) {
                billItems = FPSDBHelper.getInstance(context).getAllBillItems(beneficiary.getId(), month.getMonthOfYear());
                return true;
            } else {
                // Util.LoggingQueue(context, "QR TransactionResponse", "Beneficiary is inactive");
                Util.messageBar(context, context.getString(R.string.cardBlocked));
                return false;
            }
        }
    }

    //returns the qrCode response
    private QRTransactionResponseDto returnQRResponse() {
        QRTransactionResponseDto qrResponse = new QRTransactionResponseDto();
        qrResponse.setMobileNumber(beneficiary.getMobileNumber());
        qrResponse.setUfc(beneficiary.getEncryptedUfc());
        qrResponse.setMaskedUfc(Util.DecryptedBeneficiary(context, beneficiary.getEncryptedUfc()));
        qrResponse.setRationCardNo(beneficiary.getOldRationNumber());
        qrResponse.setBenficiaryId(beneficiary.getId());
        qrResponse.setFpsId(beneficiary.getFpsId());
        List<EntitlementDTO> entitlementDTOs = returnEntitlements();
        qrResponse.setEntitlementList(entitlementDTOs);
        Map<Long, List<EntitlementDTO>> userEntitle = new HashMap<>();
        for (EntitlementDTO entitle : entitlementDTOs) {
            if (userEntitle.containsKey(entitle.getGroupId())) {
                userEntitle.get(entitle.getGroupId()).add(entitle);
            } else {
                List<EntitlementDTO> entitles = new ArrayList<>();
                entitles.add(entitle);
                userEntitle.put(entitle.getGroupId(), entitles);
            }
        }
        qrResponse.setUserEntitlement(userEntitle);
        qrResponse.setEntitlementList(setValueForList(userEntitle));
        //Util.LoggingQueue(context, "BeneficiarySalesTransaction returnQRResponse", qrResponse.toString());
        return qrResponse;
    }

    private List<EntitlementDTO> setValueForList(Map<Long, List<EntitlementDTO>> userEntitle) {
        List<EntitlementDTO> entitlements = new ArrayList<>();
        for (Long keys : userEntitle.keySet()) {
            List<EntitlementDTO> entitles = userEntitle.get(keys);
            double bought = getListBySize(entitles);
            for (EntitlementDTO entitlementDTO : entitles) {
                entitlementDTO.setPurchasedQuantity(bought);
                entitlementDTO.setCurrentQuantity(entitlementDTO.getEntitledQuantity() - bought);
                entitlementDTO.setProductPrice(productPrice(entitlementDTO.getProductPrice(), entitlementDTO.getProductId()));
                entitlements.add(entitlementDTO);
            }

        }
        return entitlements;
    }

    private double productPrice(double productPrice, long productId) {
        double value = 0.0;
        double valuePercentage = FPSDBHelper.getInstance(context).productOverridePercentage(productId, Long.parseLong(beneficiary.getCardTypeId()));
        if (valuePercentage == 0) {
            return productPrice;
        } else {
            value = productPrice * (valuePercentage / 100);
        }
        return value;
    }

    private double getListBySize(List<EntitlementDTO> entitles) {
        double totalSize = 0.0;
        for (EntitlementDTO entitlementDTO : entitles) {
            totalSize = totalSize + entitlementDTO.getPurchasedQuantity();
        }
        return totalSize;

    }

    //return the entitlement of product to user
    private List<EntitlementDTO> returnEntitlements() {
        List<EntitlementDTO> entitlements = new ArrayList<EntitlementDTO>();
        //Util.LoggingQueue(context, "Entitlement rules", "beneficiary found:" + beneficiary.getCardTypeId());

        List<EntitlementMasterRuleDtod> entitlementRules = FPSDBHelper.getInstance(context).getAllEntitlementMasterRuleProduct(Long.parseLong(beneficiary.getCardTypeId()));
        // Log.e("entitlements", entitlementRules.toString());
        // Util.LoggingQueue(context, "Entitlement rules", "Rules found:" + entitlementRules.toString());
        //  Util.LoggingQueue(context, "BeneficiarySalesTransaction", "Total No of Entitlement ->" + entitlementRules.size());


        int count = 0;
        double entitledQty = 0.0;
        double currentQty = 0.0;
        if (entitlementRules.isEmpty()) {
            Util.LoggingQueue(context, "Rules", "Entitlement rules empty");
            return new ArrayList<>();
        }
        Util.LoggingQueue(context, "BeneficiaryTransaction","entitlement rule size "+entitlementRules.size());
        for (EntitlementMasterRuleDtod masterRule : entitlementRules) {
            Log.e("Processing rule :", masterRule.toString());
            count ++;
            Util.LoggingQueue(context, "BeneficiaryTransaction", "---------------------Entitlement for group ID  ->" + masterRule.getGroupId());

            entitledQty = processEntitlementRule(beneficiary, masterRule);
            EntitlementDTO entitlement = new EntitlementDTO();
            double purchased = findCurrentTransactions(masterRule.getProductId());
            currentQty = entitledQty - purchased;
            entitlement.setCurrentQuantity(currentQty >= 0 ? currentQty : 0);
            entitlement.setEntitledQuantity(entitledQty);/**/
            entitlement.setPurchasedQuantity(entitledQty >= purchased ? purchased : entitledQty);
            entitlement.setProductId(masterRule.getProductId());
            entitlement.setProductName(masterRule.getName());
            entitlement.setLproductName(masterRule.getLocalProductName());
            entitlement.setProductPrice(masterRule.getProductPrice());
            entitlement.setProductUnit(masterRule.getProductUnit());
            entitlement.setLproductUnit(masterRule.getLocalProductUnit());
            entitlement.setGroupId(masterRule.getGroupId());

            entitlements.add(entitlement);

            /*Util.LoggingQueue(context, "BeneficiarySalesTransaction", " totally ' " +count + " ' "+
                    "entitlement rules  Processed  ->");*/

            //  Util.LoggingQueue(context, "Entitlement", "Current entitlement:" + entitlement.toString());
        }
        // Log.e("Product entitlements", entitlements.toString());
        return entitlements;
    }


    /**
     * This method calculates current month transactions for the beneficiary
     *
     * @param productId - product id
     * @return value        - total value transacted for the current month
     */
    private double findCurrentTransactions(long productId) {
        for (int i = 0; i < billItems.size(); i++) {
            if (billItems.get(i).getProductId() == productId) {
                return billItems.get(i).getQuantity();
            }
        }

        return 0.0;

    }


    /**
     * This methods process master entitlement rule and calls the appropriate
     * methods to process region based and person based and spl rules
     *
     * @param beneficiary - beneficiary
     * @param masterRule  - master rule
     * @return double - quantity
     */
    public double processEntitlementRule(BeneficiaryDto beneficiary, EntitlementMasterRuleDtod masterRule) {

        double quantity = 0.0;
        if (masterRule.isCalcRequired()) {
            Log.e("BeneficiaryTransaction", "Calucation Required for Group ID " + masterRule.getGroupId());

            if (masterRule.isPersonBased()) {
                quantity = processPersonBasedRule(beneficiary, masterRule.getGroupId(), beneficiary.getCardTypeId());
            } else if (masterRule.isRegionBased()) {
                quantity = processRegionBasedRule(beneficiary, masterRule.getGroupId());
            }

            if (masterRule.isHasSpecialRule()) {
              //  quantity = processSpecialRule(beneficiary, masterRule.getGroupId(), quantity);
                quantity = processGenericSpecialRule(beneficiary, masterRule.getGroupId(), quantity, STAGE_TALUK);
            }
        } else {
            Log.e("BeneficiaryTransaction", "Calculation Not required for Group ID" + masterRule.getGroupId());
            quantity = masterRule.getQuantity();
        }
        return quantity;
    }

    /**
     * This method processes person based rules.
     *
     * @param beneficiary - beneficiary details
     * @param product     - product information
     * @param cardTypeId     - card type information
     * @return - entitled quantity
     */
    public double processPersonBasedRule(BeneficiaryDto beneficiary, long product, String cardTypeId) {


        Util.LoggingQueue(context, "BeneficiaryTransaction", "PERSON Calculation Started , BeneID =  " +beneficiary.getId()
                + " & product = " + product + " & cardTypeID  = " + cardTypeId);


        PersonBasedRule personBasedRule = null;

        PersonBasedRule personBasedRuleWithCardType = FPSDBHelper.getInstance(context).findByGroupAndCardType(product, cardTypeId);

        if (personBasedRuleWithCardType == null) {
            Log.e("BeneficiaryTransaction", "NO PERSON  is found card type id = " + cardTypeId);

            personBasedRule = FPSDBHelper.getInstance(context).findByGroupWithoutCardType(product);
        } else {
            Log.e("BeneficiaryTransaction", "PERSON  is found for card type id = " + cardTypeId);

            personBasedRule = personBasedRuleWithCardType;
        }

        Log.e("BeneficiaryTransaction", "Final PERSON  - Details " + personBasedRule);

        double min = 0, max = 0, perChild = 0, perAdult = 0, quantity = 0;

        min = personBasedRule.getMin();
        max = personBasedRule.getMax();
        perAdult = personBasedRule.getPerAdult();
        perChild = personBasedRule.getPerChild();

        int numOfAdults = beneficiary.getNumOfAdults();
        int numOfChildren = beneficiary.getNumOfChild();

        if (numOfAdults >= 1) {
            quantity += min;
        }
        quantity += (numOfAdults - 1) * perAdult;
        quantity += numOfChildren * perChild;

        // Quantity is less than min value set minimum value
        if (quantity < min)
            quantity = min;

        // Quantity is less than min value set minimum value
        if (quantity > max)
            quantity = max;

        Log.e("BeneficiaryTransaction", "PERSON Calculation  finished  quantity -> " + quantity);

        return quantity;
    }

    /**
     * This method processes region based rules.
     *
     * @param beneficiary - beneficiary details
     * @param product     - product information
     * @return - entitled quantity
     */
    public double processRegionBasedRule(BeneficiaryDto beneficiary, long product) {
        Log.e("BeneficiaryTransaction", "REGION Calculation Started , BeneID =  " + beneficiary.getId()
                + " & product = " + product);

        KeroseneDto fpsStore = LoginData.getInstance().getLoginData().getKeroseneBunkDto();
        String entitlementClassification = fpsStore.getEntitlementClassification();
        Log.e("BeneficiaryTransaction", "REGION entitlementClassification =  " + entitlementClassification);

        if (StringUtils.isEmpty(entitlementClassification)) {
            LoginResponseDto resp = FPSDBHelper.getInstance(context).getUserDetails(SessionId.getInstance().getUserId());
            entitlementClassification = resp.getUserDetailDto().getKerosene().getEntitlementClassification();
        }
        List<RegionBasedRule> regionBasedRules = FPSDBHelper.getInstance(context).getAllRegionBasedRule(product);
        //Util.LoggingQueue(context, "Region based rule", regionBasedRules.toString());
        if (regionBasedRules == null || regionBasedRules.size() == 0) {
            return 0.0;
        }
        double quantity = 0.0;
        for (RegionBasedRule regionBasedRule : regionBasedRules) {
            boolean checkCylinderCount = false;
            if (regionBasedRule.getCylinderCount() == -1) {
                checkCylinderCount = true;
            }
            //Log.e("Region Based Rule", regionBasedRule.toString());
            if (regionBasedRule.isVillagePanchayat()
                    && entitlementClassification.equals(VILLAGE_PANCHAYAT)) {
                if (checkCylinderCount
                        || beneficiary.getNumOfCylinder() == regionBasedRule
                        .getCylinderCount()) {
                    quantity = regionBasedRule.getQuantity();
                    break;
                }
            } else if (regionBasedRule.isCityHeadQuarter()
                    && entitlementClassification.equals(HEAD_QUARTER)) {
                if (checkCylinderCount
                        || beneficiary.getNumOfCylinder() == regionBasedRule
                        .getCylinderCount()) {
                    quantity = regionBasedRule.getQuantity();
                    break;
                }
            } else if (regionBasedRule.isMunicipality()
                    && entitlementClassification.equals(MUNICIPALITY)) {
                if (checkCylinderCount
                        || beneficiary.getNumOfCylinder() == regionBasedRule
                        .getCylinderCount()) {
                    quantity = regionBasedRule.getQuantity();
                    break;
                }
            } else if (regionBasedRule.isTownPanchayat()
                    && entitlementClassification.equals(TOWN_PANCHAYAT)) {
                if (checkCylinderCount
                        || beneficiary.getNumOfCylinder() == regionBasedRule
                        .getCylinderCount()) {
                    quantity = regionBasedRule.getQuantity();
                    break;
                }
            } else if (regionBasedRule.isHillyArea()
                    && entitlementClassification.equals(HILL_AREA)) {
                if (checkCylinderCount
                        || beneficiary.getNumOfCylinder() == regionBasedRule
                        .getCylinderCount()) {
                    quantity = regionBasedRule.getQuantity();
                    break;
                }
            } else if (regionBasedRule.isSplArea()
                    && entitlementClassification.equals(SPECIAL_AREA)) {
                if (checkCylinderCount
                        || beneficiary.getNumOfCylinder() == regionBasedRule
                        .getCylinderCount()) {
                    quantity = regionBasedRule.getQuantity();
                    break;
                }
            } else {
                quantity = 0.0;
            }
        }

        Util.LoggingQueue(context, "BeneficiaryTransaction", "REGION Calculation  finished  quantity -> " + quantity);

        return quantity;
    }

    /**
     * This method processes person based rules.
     *
     * @param beneficiary - beneficiary details
     * @param product     - product information
     * @param entitledQty - previous calculated entitled quantity
     * @return - entitled quantity
     */
    public double processSpecialRule(BeneficiaryDto beneficiary, long product, double entitledQty) {
        Util.LoggingQueue(context, "BeneficiaryTransaction", "SPECIAL Calculation Started , BeneID =  " + beneficiary.getId()
                + " & product = " + product + " & entitledQty  = " + entitledQty);
        KeroseneDto fpsStore = LoginData.getInstance().getLoginData().getKeroseneBunkDto();
         Long districtid=null;
        if(fpsStore.getTalukDto() != null && fpsStore.getTalukDto().getDistrictDto() != null) {
            districtid = fpsStore.getTalukDto().getDistrictDto().getId();
        }
        Log.e("kerosene Dto","kerosene Dto"+fpsStore.toString());
        String entitlementClassification = fpsStore.getEntitlementClassification();
        Log.e("BeneficiaryTransaction", "SPECIAL entitlementClassification =  " + entitlementClassification);

        List<SplEntitlementRule> splEntitlementRules = null;

        if (districtid != null && districtid != 0) {
            Log.e("BeneficiaryTransaction", "SPECIAL with District ID  =  " + districtid);

            splEntitlementRules = FPSDBHelper.getInstance(context).findByGroupAndCardTypeAndDistrict(product, beneficiary.getCardTypeId(), districtid);
        }

        if (splEntitlementRules == null || splEntitlementRules.isEmpty()) {
            Log.e("BeneficiaryTransaction", "SPECIAL with out District ID   ");

            splEntitlementRules = FPSDBHelper.getInstance(context).findByGroupAndCardTypeAndNullDistrict(product, beneficiary.getCardTypeId());
        }

        if (splEntitlementRules == null || splEntitlementRules.size() == 0) {
            return entitledQty;
        }

        if (splEntitlementRules != null) {

            double quantity = 0.0;
            for (SplEntitlementRule splEntitlementRule : splEntitlementRules) {
                // Util.LoggingQueue(context, "BeneficiarySalesTransaction", "SPECIAL Details- >" + splEntitlementRule);

                boolean checkArea = false;
                boolean checkCylinderCount = false;
                boolean checkFpsClassification = false;

                if (splEntitlementRule.getDistrictId() != null && splEntitlementRule.getDistrictId() != 0) {
                    checkArea = true;
                }

                if (splEntitlementRule.getCylinderCount() != -1) {
                    checkCylinderCount = true;
                }

                if (splEntitlementRule.isVillagePanchayat()
                        || splEntitlementRule.isCityHeadQuarter()
                        || splEntitlementRule.isMunicipality()
                        || splEntitlementRule.isTownPanchayat()
                        || splEntitlementRule.isHillyArea()
                        || splEntitlementRule.isSplArea()) {
                    checkFpsClassification = true;
                }

                /*if (checkArea) {
                    if (splEntitlementRule.getDistrictId() != null && splEntitlementRule.getDistrictId() != 0 && splEntitlementRule.getDistrictId() == districtid) {
                        if (splEntitlementRule.getTalukId() == null || splEntitlementRule.getTalukId() == 0 || splEntitlementRule.getTalukId() == fpsStore.getTalukDto().getId() &&
                                splEntitlementRule.getVillageId() == null || splEntitlementRule.getVillageId() == 0 ) {
                            //quantity = splEntitlementRule.getQuantity();
                        } else {
                            quantity = 0.0;
                            continue;
                        }
                    }
                }*/



                if (checkArea) {
                    if (splEntitlementRule.getDistrictId() != null && splEntitlementRule.getDistrictId() != 0 && splEntitlementRule.getDistrictId() == districtid) {

                        Log.e("BeneficiaryTransaction", "checkArea SPl  dist  " + splEntitlementRule.getDistrictId());
                        Log.e("BeneficiaryTransaction", "checkArea kerosenebunk dist  " + districtid);
                        Log.e("BeneficiaryTransaction", "checkArea SPL Taluk " + splEntitlementRule.getTalukId());
                        Log.e("BeneficiaryTransaction", "checkArea kerosenebunk Taluk " + fpsStore.getTalukDto().getId());

                        if (splEntitlementRule.getTalukId() == null || splEntitlementRule.getTalukId().equals(0L) || splEntitlementRule.getTalukId().equals(fpsStore.getTalukDto().getId())) {
                            Util.LoggingQueue(context, "BeneficiaryTransaction", "checkArea  IF executed  " + quantity);


                        } else {

                            quantity = 0.0;
                            Util.LoggingQueue(context, "BeneficiaryTransaction", "checkArea  else executed and quantity =  " + quantity  );

                            continue;

                        }
                    }
                }


                if (checkCylinderCount) {
                    if (beneficiary.getNumOfCylinder() == splEntitlementRule.getCylinderCount()) {
                        //quantity = splEntitlementRule.getQuantity();
                    } else {
                        quantity = 0.0;
                        continue;
                    }
                }

                if (checkFpsClassification) {
                    if (splEntitlementRule.isVillagePanchayat()
                            && entitlementClassification
                            .equals(VILLAGE_PANCHAYAT)) {
                        quantity = splEntitlementRule.getQuantity();
                    } else if (splEntitlementRule.isCityHeadQuarter()
                            && entitlementClassification
                            .equals(HEAD_QUARTER)) {
                        quantity = splEntitlementRule.getQuantity();
                    } else if (splEntitlementRule.isMunicipality()
                            && entitlementClassification
                            .equals(MUNICIPALITY)) {
                        quantity = splEntitlementRule.getQuantity();
                    } else if (splEntitlementRule.isTownPanchayat()
                            && entitlementClassification
                            .equals(TOWN_PANCHAYAT)) {
                        quantity = splEntitlementRule.getQuantity();
                    } else if (splEntitlementRule.isHillyArea()
                            && entitlementClassification
                            .equals(HILL_AREA)) {
                        quantity = splEntitlementRule.getQuantity();
                    } else if (splEntitlementRule.isSplArea()
                            && entitlementClassification
                            .equals(SPECIAL_AREA)) {
                        quantity = splEntitlementRule.getQuantity();
                    } else {
                        quantity = 0.0;
                        continue;
                    }

                    if (splEntitlementRule.isAdd()) {
                        entitledQty += quantity;
                        break;
                    } else {
                        entitledQty = quantity;
                        break;
                    }
                }


            }

            Util.LoggingQueue(context, "BeneficiaryTransaction", "SPECIAL Calculation finished  entitledQty-> " + entitledQty);
        }
        return entitledQty;
    }
    public double processGenericSpecialRule(BeneficiaryDto beneficiary, long product, double entitledQty, String currentStage) {

        Util.LoggingQueue(context, "BeneficiaryTransaction", "SPECIAL Calculation Started , BeneID =  " +beneficiary.getId()
                + " & group id = " + product + " & entitledQty  = " + entitledQty +" & currentStage = "+currentStage);

        KeroseneDto fpsStore = LoginData.getInstance().getLoginData().getKeroseneBunkDto();
        Long districtid=null;
        Long talukid=null;
        Long villageid=null;

        if(fpsStore.getTalukDto() != null && fpsStore.getTalukDto().getDistrictDto() != null) {
            districtid = fpsStore.getTalukDto().getDistrictDto().getId();
            talukid=fpsStore.getTalukDto().getId();

        }
        Log.e("BeneficiaryTransaction","GenericSpecialRule Calculation talukid" +talukid);
        Log.e("BeneficiaryTransaction","GenericSpecialRule Calculation districtid"+districtid );
        Log.e("BeneficiaryTransaction","GenericSpecialRule Calculation villageid"+villageid);
        Log.e("BeneficiaryTransaction","kerosene Dto"+fpsStore.toString());
        String entitlementClassification = fpsStore.getEntitlementClassification();

        boolean ruleSatisfied = false;
        String nextStage = "";

        Util.LoggingQueue(context,"","initial nextStage : "+nextStage);

        List<SplEntitlementRule> splEntitlementRules = null;

        if(currentStage.equalsIgnoreCase(STAGE_VILLAGE)) {
            if(villageid != null) {
                Util.LoggingQueue(context, "BeneficiaryTransaction", "fpsVillageID != null -> fpsVillageID = " +villageid );
                splEntitlementRules=FPSDBHelper.getInstance(context).findByGroupAndCardTypeAndVillage(product,beneficiary.getCardTypeId(), villageid);
                Util.LoggingQueue(context, "BeneficiaryTransaction", "fpsVillageID splEntitlementRules = " +splEntitlementRules );

                nextStage = getNextStage(currentStage);
                Util.LoggingQueue(context,"BeneficiaryTransaction","modified nextStage : "+nextStage);
                if(splEntitlementRules == null || splEntitlementRules.isEmpty()) {
                    currentStage = nextStage;
                    Util.LoggingQueue(context,"BeneficiaryTransaction","modified currentStage : "+nextStage);
                }
            }else {
                currentStage = getNextStage(currentStage);;
                Util.LoggingQueue(context,"BeneficiaryTransaction","modified currentStage : "+currentStage);
            }
        }

        if(currentStage.equalsIgnoreCase(STAGE_TALUK)) {
            if(talukid != null && (splEntitlementRules == null || splEntitlementRules.isEmpty())) {
                Util.LoggingQueue(context, "BeneficiaryTransaction", "fpsTalukID != null -> fpsTalukID = " +talukid );
                splEntitlementRules=FPSDBHelper.getInstance(context).findByGroupAndCardTypeAndTaluk(product,beneficiary.getCardTypeId(), talukid);
                Util.LoggingQueue(context, "BeneficiaryTransaction", "fpsTalukID splEntitlementRules = " +splEntitlementRules );

                nextStage = getNextStage(currentStage);
                Util.LoggingQueue(context,"BeneficiaryTransaction","modified nextStage : "+nextStage);
                if(splEntitlementRules == null || splEntitlementRules.isEmpty()) {
                    currentStage = nextStage;
                    Util.LoggingQueue(context,"BeneficiaryTransaction","modified currentStage : "+nextStage);
                }
            }else {
                currentStage = getNextStage(currentStage);;
                Util.LoggingQueue(context,"BeneficiaryTransaction","modified currentStage : "+currentStage);
            }
        }

        if(currentStage.equalsIgnoreCase(STAGE_DISTRICT)) {
            if(districtid != null && (splEntitlementRules == null || splEntitlementRules.isEmpty())) {
                Util.LoggingQueue(context, "BeneficiaryTransaction", "SPECIAL with District ID  =  " +districtid );
                splEntitlementRules = FPSDBHelper.getInstance(context).findByGroupAndCardTypeAndDistrict(product, beneficiary.getCardTypeId(), districtid);

                nextStage = getNextStage(currentStage);
                Util.LoggingQueue(context,"BeneficiaryTransaction","modified nextStage : "+nextStage);
                if(splEntitlementRules == null || splEntitlementRules.isEmpty()) {
                    currentStage = nextStage;
                    Util.LoggingQueue(context,"BeneficiaryTransaction","modified currentStage : "+nextStage);
                }
            }else {
                currentStage = getNextStage(currentStage);;
                Util.LoggingQueue(context,"BeneficiaryTransaction","modified currentStage : "+currentStage);
            }
        }

        if (splEntitlementRules == null || splEntitlementRules.isEmpty() && currentStage.equalsIgnoreCase(STAGE_GENERIC)) {
            Log.e("BeneficiaryTransaction", "SPECIAL with out District ID   ");
            splEntitlementRules = FPSDBHelper.getInstance(context).findByGroupAndCardTypeAndNullDistrict(product, beneficiary.getCardTypeId());
        }

        if (splEntitlementRules != null) {

            double quantity = 0.0;
            for (SplEntitlementRule splEntitlementRule : splEntitlementRules) {

                Util.LoggingQueue(context, "BeneficiaryTransaction", "SPECIAL splEntitlementRule in Iteration  " + splEntitlementRule  );
                Util.LoggingQueue(context, "BeneficiaryTransaction", "Iteration SPl  Village  " + splEntitlementRule.getVillageId()  );
                Util.LoggingQueue(context, "BeneficiaryTransaction", "Iteration SPL Taluk " + splEntitlementRule.getTalukId()  );

                boolean checkArea = false;
                boolean checkCylinderCount = false;
                boolean checkFpsClassification = false;

                if (splEntitlementRule.getDistrictId() != null && splEntitlementRule.getDistrictId() != 0) {
                    checkArea = true;
                    Util.LoggingQueue(context, "BeneficiaryTransaction", "SPECIAL checkArea is true set  " + checkArea  );
                }

                if (splEntitlementRule.getCylinderCount() != -1) {
                    checkCylinderCount = true;
                    Util.LoggingQueue(context, "BeneficiaryTransaction", "SPECIAL checkCylinderCount is true set  " + checkCylinderCount  );
                }

                if (splEntitlementRule.isVillagePanchayat()
                        || splEntitlementRule.isCityHeadQuarter()
                        || splEntitlementRule.isMunicipality()
                        || splEntitlementRule.isTownPanchayat()
                        || splEntitlementRule.isHillyArea()
                        || splEntitlementRule.isSplArea()) {
                    checkFpsClassification = true;
                    Util.LoggingQueue(context, "BeneficiaryTransaction", "SPECIAL checkFpsClassification is true set  " + checkFpsClassification  );
                }

                if (checkArea) {
                    if (splEntitlementRule.getDistrictId() != null && splEntitlementRule.getDistrictId() != 0 && splEntitlementRule.getDistrictId() == districtid) {
                        Util.LoggingQueue(context, "BeneficiaryTransaction", "checkArea SPl  Village  " + splEntitlementRule.getVillageId()  );
                        Util.LoggingQueue(context, "BeneficiaryTransaction", "checkArea FPS Store Village  " + villageid );
                        Util.LoggingQueue(context, "BeneficiaryTransaction", "checkArea SPL Taluk " + splEntitlementRule.getTalukId()  );
                        Util.LoggingQueue(context, "BeneficiaryTransaction", "checkArea FPS Store Taluk " + talukid  );

                        if ((splEntitlementRule.getTalukId() == null || splEntitlementRule.getTalukId().equals(0L) || splEntitlementRule.getTalukId().equals(talukid))  &&
                                (splEntitlementRule.getVillageId() == null || splEntitlementRule.getVillageId().equals(0L))) {
                            Util.LoggingQueue(context, "BeneficiaryTransaction", "checkArea  IF executed  " + quantity  );

                        } else {
                            quantity = 0.0;
                            Util.LoggingQueue(context, "BeneficiaryTransaction", "checkArea  else executed and quantity =  " + quantity  );
                            Util.LoggingQueue(context, "BeneficiaryTransaction", "checkArea : Skipping rule " );
                            continue;
                        }
                    }
                }
                if (checkCylinderCount) {
                    if (beneficiary.getNumOfCylinder() == splEntitlementRule.getCylinderCount()) {
                        //quantity = splEntitlementRule.getQuantity();
                        Util.LoggingQueue(context, "BeneficiaryTransaction", "checkCylinderCount  IF executed  " + quantity  );
                    } else {
                        quantity = 0.0;
                        Util.LoggingQueue(context, "BeneficiaryTransaction", "checkCylinderCount  else executed and quantity =  " + quantity  );
                        Util.LoggingQueue(context, "BeneficiaryTransaction", "checkCylinderCount : Skipping rule " );
                        continue;
                    }
                }

                if (checkFpsClassification) {
                    Util.LoggingQueue(context, "BeneficiaryTransaction", "checkFpsClassification  IF executed  " + quantity  );

                    if (splEntitlementRule.isVillagePanchayat()
                            && entitlementClassification
                            .equals(VILLAGE_PANCHAYAT)) {
                        quantity = splEntitlementRule.getQuantity();
                        Util.LoggingQueue(context, "BeneficiaryTransaction", "VILLAGE_PANCHAYAT  IF executed  " + quantity  );
                    } else if (splEntitlementRule.isCityHeadQuarter()
                            && entitlementClassification
                            .equals(HEAD_QUARTER)) {
                        quantity = splEntitlementRule.getQuantity();
                        Util.LoggingQueue(context, "BeneficiaryTransaction", "HEAD_QUARTER  IF executed  " + quantity  );
                    } else if (splEntitlementRule.isMunicipality()
                            && entitlementClassification
                            .equals(MUNICIPALITY)) {
                        quantity = splEntitlementRule.getQuantity();
                        Util.LoggingQueue(context, "BeneficiaryTransaction", "MUNICIPALITY  IF executed  " + quantity  );
                    } else if (splEntitlementRule.isTownPanchayat()
                            && entitlementClassification
                            .equals(TOWN_PANCHAYAT)) {
                        quantity = splEntitlementRule.getQuantity();
                        Util.LoggingQueue(context, "BeneficiaryTransaction", "TOWN_PANCHAYAT  IF executed  " + quantity  );
                    } else if (splEntitlementRule.isHillyArea()
                            && entitlementClassification
                            .equals(HILL_AREA)) {
                        quantity = splEntitlementRule.getQuantity();
                        Util.LoggingQueue(context, "BeneficiaryTransaction", "HILL_AREA  IF executed  " + quantity  );
                    } else if (splEntitlementRule.isSplArea()
                            && entitlementClassification
                            .equals(SPECIAL_AREA)) {
                        quantity = splEntitlementRule.getQuantity();
                        Util.LoggingQueue(context, "BeneficiaryTransaction", "SPECIAL_AREA  IF executed  " + quantity  );
                    } else {
                        Util.LoggingQueue(context, "BeneficiaryTransaction", "  NONE OF THE AREA IS SATISFIED SET quantity = 0 "   );
                        Util.LoggingQueue(context, "BeneficiaryTransaction", "checkFpsClassification : Skipping rule " );
                        quantity = 0.0;
                        continue;
                    }

                    if (splEntitlementRule.isAdd()) {
                        entitledQty += quantity;
                        Util.LoggingQueue(context, "BeneficiaryTransaction", "isAdd  IF executed  " + quantity  );
                        Util.LoggingQueue(context, "BeneficiaryTransaction", "splEntitlementRule satisfied. Hence break the loop"  );
                        ruleSatisfied = true;
                        break;
                    } else {
                        entitledQty = quantity;
                        Util.LoggingQueue(context, "BeneficiaryTransaction", "isAdd  else executed  " + quantity  );
                        Util.LoggingQueue(context, "BeneficiaryTransaction", "splEntitlementRule satisfied. Hence break the loop"  );
                        ruleSatisfied = true;
                        break;
                    }
                }
            }

            if(!ruleSatisfied && !currentStage.equalsIgnoreCase(STAGE_GENERIC)) {
                Util.LoggingQueue(context,"BeneficiaryTransaction","No matching spl rules found. Hence checking for generic spl rule");
                entitledQty = processGenericSpecialRule(beneficiary,product, entitledQty, nextStage);
            }

            Util.LoggingQueue(context, "BeneficiaryTransaction", "XX--------------- SPECIAL Calculation finished  entitledQty-> " + entitledQty+" -----------XX");
        }
        return entitledQty;
    }

    public String getNextStage(String currentStage) {
        Util.LoggingQueue(context,"BeneficiaryTransaction","getNextStage called : currentStage : "+currentStage);
        String nextStage = "";

        if(currentStage.equalsIgnoreCase(STAGE_VILLAGE)) {
            nextStage = STAGE_TALUK;
        } else if(currentStage.equalsIgnoreCase(STAGE_TALUK)) {
            nextStage = STAGE_DISTRICT;
        } else if(currentStage.equalsIgnoreCase(STAGE_DISTRICT)) {
            nextStage = STAGE_GENERIC;
        }
        Util.LoggingQueue(context,"BeneficiaryTransaction","getNextStage end : currentStage : "+currentStage+" and its nextStage : "+nextStage);
        return nextStage;
    }

}

