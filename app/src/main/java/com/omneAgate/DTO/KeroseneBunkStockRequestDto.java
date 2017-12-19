package com.omneAgate.DTO;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.omneAgate.DTO.EnumDTO.StockTransactionType;

@Data
public class KeroseneBunkStockRequestDto extends BaseDto implements Serializable{

	private static final long serialVersionUID = 1L;
	@ToString
	public static class ProductList {

		@Getter
		@Setter
		public Double quantity;

		@Getter
		@Setter
		Long id;

		@Getter
		@Setter
		String name;

		@Getter
		@Setter
		String unit;

		@Getter
		@Setter
		Double recvQuantity;

		@Getter
		@Setter
		int year;

		@Getter
		@Setter
		int month;

		@Getter
		@Setter
		StockTransactionType type;
		
	}

	StockTransactionType type;

	Long bunkId;

	Long keroseneWholeSalerId;

	List<ProductList> productLists;

	long deliveryChallanId;

	long batchNo;

	Date date;

	long createdBy;

	String unit;

	/**reference of the particular stock*/
	long inwardKey;

	String vehicleNo;

	String stackNo;

	String keroseneWholeSalerCode;

	String keroseneWholeSalerName;

	String referenceNo;

	long districtId;

	long talukId;

	long villageId;

	String keroseneBunkCode;

	String keroseneBunkName;

	boolean Status;

	String keroseneWholeSalerLocation;

	String keroseneWholeSalerContactPerson;

	String mobileNumber;

	String driverName;

	String transportName;

	/** used for godown to godown transfer */	
	long sendingkeroseneWholeSalerId;

	long recGodownId;
	/** End used for godown to godown transfer */

	int year;

	int month;

	Long billId;

	String bunkGeneratedCode;
}
