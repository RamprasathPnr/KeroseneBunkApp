package com.omneAgate.Util;

import android.app.Activity;
import android.util.Log;

import com.omneAgate.DTO.BeneficiaryDto;
import com.omneAgate.DTO.BillItemDto;
import com.omneAgate.DTO.EntitlementDTO;
import com.omneAgate.DTO.EntitlementMasterRuleDtod;
import com.omneAgate.DTO.KeroseneDto;
import com.omneAgate.DTO.PersonBasedRule;
import com.omneAgate.DTO.QRTransactionResponseDto;
import com.omneAgate.DTO.RegionBasedRule;
import com.omneAgate.DTO.SplEntitlementRule;
import com.omneAgate.Bunker.GlobalAppState;
import com.omneAgate.Bunker.R;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Beneficiary sales transaction class
 */
public class BeneficiarySalesQRTransaction {

    private static final String MUNICIPALITY = "Municipality";
    private static final String OTHER_DISTRICTS = "Other Districts";
    private static final String HEAD_QUARTER = "Head Quarter";
    private static final String BELT_AREA = "Belt Area";
    private static final String TALUK = "Taluk";
    Activity context; //Context for this class
    BeneficiaryDto beneficiary;//Beneficiary of card
    String qrCode; //qr code of user card
    private List<BillItemDto> billItems; //Bill items bought

    public BeneficiarySalesQRTransaction(Activity activity) {
        context = activity;
        Util.changeLanguage(context, GlobalAppState.language);
    }



    public QRTransactionResponseDto getBeneficiaryDetails(String qrCode) {
        this.qrCode = qrCode;
        if (getBeneficiaryAndMemberDetails()) {
            Log.e("Beneficiary", beneficiary.toString());
            return returnQRResponse();
        }

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
        // beneficiary = DBHelper.getInstance(context).beneficiaryFromOldCard(qrCode);
        beneficiary = FPSDBHelper.getInstance(context).beneficiaryDto(qrCode);
        DateTime month = new DateTime();
        billItems = new ArrayList<>();
        if (beneficiary == null) {

            Util.messageBar(context, context.getString(R.string.fpsBeneficiaryMismatch));
            return false;
        } else {
            if (beneficiary.isActive()) {
                billItems = FPSDBHelper.getInstance(context).getAllBillItems(beneficiary.getId(), month.getMonthOfYear());
                return true;
            } else {

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
        // qrResponse.setMaskedUfc(Util.DecryptedBeneficiary(context, beneficiary.getEncryptedUfc()));
        qrResponse.setRationCardNo(beneficiary.getOldRationNumber());
        qrResponse.setBenficiaryId(beneficiary.getId());
        qrResponse.setFpsId(beneficiary.getFpsId());
        List<EntitlementDTO> entitlementDTOs = returnEntitlements();
        qrResponse.setEntitlementList(entitlementDTOs);
        Map<Long, List<EntitlementDTO>> userEntitle = new HashMap<>();
        Log.e("User Entitle",userEntitle.toString());
        for (EntitlementDTO entitle : entitlementDTOs) {
            if (userEntitle.containsKey(entitle.getGroupId())) {
                userEntitle.get(entitle.getGroupId()).add(entitle);
                Log.e("User Entitle", userEntitle.toString());
            } else {
                List<EntitlementDTO> entitles = new ArrayList<>();
                entitles.add(entitle);
                userEntitle.put(entitle.getGroupId(), entitles);
            }
        }
        Log.e("User Entitle",userEntitle.toString());
        qrResponse.setUserEntitlement(userEntitle);
        qrResponse.setEntitlementList(setValueForList(userEntitle));
        Log.e("qrResponse",qrResponse.toString());
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
        Log.e("CardTypeId",""+beneficiary.getCardTypeId());
        List<EntitlementMasterRuleDtod> entitlementRules = FPSDBHelper.getInstance(context).getAllEntitlementMasterRuleProduct(Long.parseLong(beneficiary.getCardTypeId()));
        Log.e("entitlements", entitlementRules.toString());

        double entitledQty = 0.0;
        double currentQty = 0.0;
        if (entitlementRules.isEmpty()) {
            Log.e("EmptyList", "Rule is Empty");
            return new ArrayList<>();
        }
        for (EntitlementMasterRuleDtod masterRule : entitlementRules) {
            Log.e("Processing rule :", masterRule.toString());
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
        }
        Log.e("Product entitlements", entitlements.toString());
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



        Log.e("MasterRule",masterRule.toString());
        if (masterRule.isCalcRequired()) {

            if (masterRule.isPersonBased()) {
                quantity = processPersonBasedRule(beneficiary, masterRule.getGroupId());
                Log.e("isPersonBased",""+quantity);
            }


            if (masterRule.isRegionBased()) {
                quantity = processRegionBasedRuleDto(beneficiary, masterRule.getGroupId());
                Log.e("isRegionBased",""+quantity);
            }
            if (masterRule.isHasSpecialRule()) {
                quantity = processSpecialRule(beneficiary, masterRule.getGroupId(), quantity);
                Log.e("isHasSpecialRule",""+quantity);
            }
        } else {

            quantity = masterRule.getQuantity();
            Log.e("returnbased",""+quantity);
        }
        Log.e("totalQuantity",""+quantity);
        return quantity;
    }

    /**
     * This method processes person based rules.
     *
     * @param beneficiary - beneficiary details
     * @param product     - product information
     * @return - entitled quantity
     */
    public double processPersonBasedRule(BeneficiaryDto beneficiary, long product) {
        PersonBasedRule personBasedRule = FPSDBHelper.getInstance(context).getAllPersonBasedRule(product);
        Log.e("sdfsdf",personBasedRule.toString());
        //personBasedRuleRepo.findByProduct(product);
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
        return quantity;
    }


    /**
     * This method processes region based rule
     *
     * @param beneficiary
     * @param product
     */
    public double processRegionBasedRuleDto(BeneficiaryDto beneficiary, long product) {
        KeroseneDto rrcDto = LoginData.getInstance().getLoginData().getKeroseneBunkDto();
        String entitlementClassification = SessionId.getInstance().getEntitlementClassification();
        Log.e("EntitlementClaRegionRules",entitlementClassification);

        if (StringUtils.isEmpty(entitlementClassification)) {
            // RrcLoginResponseDto resp = DBHelper.getInstance(context).getUserDetails(SessionId.getInstance().getUserId());
            entitlementClassification =  SessionId.getInstance().getEntitlementClassification();
            Log.e("Entitlement Classfication Region Rules",entitlementClassification);
        }
        List<RegionBasedRule> RegionBasedRuleDtos = FPSDBHelper.getInstance(context).getAllRegionBasedRule(product);

        Log.e("RegRuleFromTab", RegionBasedRuleDtos.toString());
        if (RegionBasedRuleDtos == null || RegionBasedRuleDtos.size() == 0) {
            return 0.0;
        }
        double quantity = 0.0;
        for (RegionBasedRule RegionBasedRuleDto : RegionBasedRuleDtos) {
            boolean checkCylinderCount = false;
            if (RegionBasedRuleDto.getCylinderCount() == -1) {
                checkCylinderCount = true;
            }
            Log.e("Region Based Rule", RegionBasedRuleDto.toString());
            if (RegionBasedRuleDto.isCity() && entitlementClassification.equals(BELT_AREA)) {
                if (checkCylinderCount || beneficiary.getNumOfCylinder() == RegionBasedRuleDto.getCylinderCount()) {
                    quantity = RegionBasedRuleDto.getQuantity();
                    Log.e("RegionRuleCityQuantity",""+quantity);
                    break;
                }
            } else if (RegionBasedRuleDto.isCityHeadQuarter() && entitlementClassification.equals(HEAD_QUARTER)) {
                if (checkCylinderCount || beneficiary.getNumOfCylinder() == RegionBasedRuleDto.getCylinderCount()) {
                    quantity = RegionBasedRuleDto.getQuantity();
                    break;
                }
            } else if (RegionBasedRuleDto.isMunicipality() && entitlementClassification.equals(MUNICIPALITY)) {
                if (checkCylinderCount || beneficiary.getNumOfCylinder() == RegionBasedRuleDto.getCylinderCount()) {
                    quantity = RegionBasedRuleDto.getQuantity();
                    break;
                }
            } else if (RegionBasedRuleDto.isTaluk() && entitlementClassification.equals(TALUK)) {
                if (checkCylinderCount || beneficiary.getNumOfCylinder() == RegionBasedRuleDto.getCylinderCount()) {
                    quantity = RegionBasedRuleDto.getQuantity();
                    break;
                }
            } else {
                quantity = 0.0;
                Log.e("RegionRuleQuantity",""+quantity);
            }
        }
        return quantity;
    }

    /**
     * This method processes special rules
     *
     * @param beneficiary
     * @param product
     */
    public double processSpecialRule(BeneficiaryDto beneficiary, long product, double entitledQty) {
        KeroseneDto rrcDto = LoginData.getInstance().getLoginData().getKeroseneBunkDto();
        List<SplEntitlementRule> splEntitlementRules = FPSDBHelper.getInstance(context).getAllSpecialRule(product, beneficiary.getCardTypeId());
        Log.e("special rules", splEntitlementRules.toString());
        if (splEntitlementRules == null || splEntitlementRules.size() == 0) {
            return entitledQty;
        }
        double quantity = 0.0;
        for (SplEntitlementRule splEntitlementRule : splEntitlementRules) {
            boolean checkArea = false;
            boolean checkCylinderCount = false;
            boolean checkFpsClassification = false;

            if (splEntitlementRule.getDistrictId() != 0) {
                checkArea = true;
            }

            if (splEntitlementRule.getCylinderCount() != -1) {
                checkCylinderCount = true;
            }

            if (splEntitlementRule.isCity() ||splEntitlementRule.isTaluk()||splEntitlementRule.isMunicipality() ||splEntitlementRule.isCityHeadQuarter() ) {
                checkFpsClassification = true;
            }


            if (checkArea) {


                if (splEntitlementRule.getDistrictId() != 0 && splEntitlementRule.getDistrictId() == SessionId.getInstance().getRrc_districtid()) {
                    if(splEntitlementRule.getTalukId() == 0 || splEntitlementRule.getTalukId() == SessionId.getInstance().getRrc_talukid()) {
                        quantity = splEntitlementRule.getQuantity();
                    } else {
                        quantity = 0.0;
                    }
                }

                quantity = splEntitlementRule.getQuantity();

                Log.e("SplAreaBasedquantity",""+quantity);
            }
            if (checkCylinderCount) {
                if (beneficiary.getNumOfCylinder() == splEntitlementRule.getCylinderCount()) {
                    quantity = splEntitlementRule.getQuantity();
                } else {
                    quantity = 0.0;
                }
            }

            checkFpsClassification = true;
            if (checkFpsClassification) {

                String entitlementClassification =  SessionId.getInstance().getEntitlementClassification();
                Log.e("EntitlementClassificationRegionRule",entitlementClassification);


                if (splEntitlementRule.isCity() && entitlementClassification.equals(BELT_AREA)) {
                    Log.e("CylinderCount",""+checkCylinderCount);
                    Log.e("BeneficiaryCylinderCount",""+beneficiary.getNumOfCylinder() );
                    Log.e("SplEntitlementRuleCylinderCount",""+beneficiary.getNumOfCylinder() );

                    if (!checkCylinderCount || beneficiary.getNumOfCylinder() == splEntitlementRule.getCylinderCount()) {
                        quantity = splEntitlementRule.getQuantity();
                        Log.e("City_Quantity",""+quantity);
                    }
                } else if (splEntitlementRule.isCityHeadQuarter() && entitlementClassification.equals(HEAD_QUARTER)) {
                    if (!checkCylinderCount || beneficiary.getNumOfCylinder() == splEntitlementRule.getCylinderCount()) {
                        quantity = splEntitlementRule.getQuantity();
                    }
                } else if (splEntitlementRule.isMunicipality() && entitlementClassification.equals(MUNICIPALITY)) {
                    if (!checkCylinderCount || beneficiary.getNumOfCylinder() == splEntitlementRule.getCylinderCount()) {
                        quantity = splEntitlementRule.getQuantity();
                    }
                } else if (splEntitlementRule.isTaluk() && entitlementClassification.equals(TALUK)) {
                    if (!checkCylinderCount || beneficiary.getNumOfCylinder() == splEntitlementRule.getCylinderCount()) {
                        quantity = splEntitlementRule.getQuantity();
                    }
                } else {
                    quantity = 0.0;
                }
            }
            if (splEntitlementRule.isAdd()) {
                entitledQty += quantity;
            }
        }
        Log.e("SplTotalQuantity",""+entitledQty);
        return entitledQty;

    }


}