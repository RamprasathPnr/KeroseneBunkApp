package com.omneAgate.DTO;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class KeroseneBunkStockDto extends BaseDto{

	/** FPS identifier*/
	Long bunkId;
	
	/**product identifier*/
	Long productId;

	/**Stock quantity*/
	Double quantity;
	
	/**Reorder Level quantity for a product*/
	Double reorderLevel;
	
	/**Action flag to send email if true*/ 
	boolean emailAction;
	
	/**Action flag to send SMS if true*/
	boolean smsMSAction;
}
