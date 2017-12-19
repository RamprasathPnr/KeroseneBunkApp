package com.omneAgate.DTO;

import android.database.Cursor;

import java.io.Serializable;
import lombok.Data;

@Data
public class GiveItUpRequestDetailDto extends BaseDto implements Serializable{

	private static final long serialVersionUID = 1L;

	/** Primary Key column. */
	private Long id;
	
	/** relation with GiveItUpRequest*/
	private GiveItUpRequestDto giveItUpRequestDto;
	
	/** relation with product*/
	private GroupDto groupDto;
	
	/** created on date and time */
	private long createdDate;
	
	/** modified on date and time */
	private long modifiedDate;


	
}	