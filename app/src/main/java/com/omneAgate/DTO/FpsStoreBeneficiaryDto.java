package com.omneAgate.DTO;

import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class FpsStoreBeneficiaryDto  extends BaseDto implements Serializable{

	private static final long serialVersionUID = 1L;
	
	@Setter @Getter
	FpsStoreDto fpsStoreDto;
	
	@Setter @Getter
	List<BeneficiaryDto> beneficiaryDtoList;
}
