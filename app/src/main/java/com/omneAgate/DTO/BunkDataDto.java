package com.omneAgate.DTO;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class BunkDataDto extends BaseDto implements Serializable{


	private static final long serialVersionUID = 1L;

	/** collection bill data*/
  @Getter @Setter
  Set<BillDto>  billDto;
  
  /** collection product data*/
  @Getter @Setter
  Set<ProductDto> productDto;
  
  /** collection beneficiary data*/
  @Getter @Setter
  Set<BeneficiaryDto> beneficiaryDto;
  
   /** collection cardtype data*/
  @Getter @Setter
  Set<CardTypeDto>  cardtypeDto;
  
  /** collection userdetail data*/
  @Getter @Setter
  Set<UserDetailDto> userdetailDto;
  
  /** collection fpsstore data*/
  @Getter @Setter
  Set<FpsStoreDto> fpsstoreDto;  
  
  /** collection transaction data*/
  @Getter @Setter
  Set<TransactionDto> transactionDto;
  
  /** collection godownStock outward data
   * fpsstockInward and godownstockoutward using same dto*/
  @Getter @Setter
  Set<GodownStockOutwardDto> fpsStoInwardDto;
  
  /** collection godownStock outward data*/
  @Getter @Setter
  Set<FPSStockDto> fpsStockDto;
  
  /** collection serviceProvider data*/
  @Getter @Setter
  Set<ServiceProviderDto> serviceProviderDto;
  
  @Getter @Setter
  Set<RegionBasedRule> regionBasedRulesDto;
  
  @Getter @Setter
  Set<EntitlementMasterRule> entitlementMasterRulesDto;
  
  @Getter @Setter
  Set<PersonBasedRule>  personBasedRulesDto;
  
  @Getter @Setter
  Set<SplEntitlementRule> splEntitlementRulesDto;
  
  @Getter @Setter
  Set<BeneficiaryRegistrationData> benefRegReqDto ;
  
  @Getter @Setter
  Set<SmsProviderDto> smsProviderDtos ;
  
  @Getter @Setter
  Set<GodownDto> godownDtos ;  
  
  @Getter @Setter
  Set<ProductPriceOverrideDto> overrideDto;
  
  @Getter @Setter
  Map<String,Integer> tableDetails;
  
  @Getter @Setter
  List<String> tableList;
  
  /** last synch time recieved at server*/
  @Getter @Setter
  String lastSyncTime;  
 
  /** to send the List of fpsStore and associated Beneficiaries*/
  @Setter @Getter
  List<FpsStoreBeneficiaryDto> fpsStoreBeneficiaryDtoList;
}
